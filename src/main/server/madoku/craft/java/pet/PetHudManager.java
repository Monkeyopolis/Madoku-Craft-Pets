package madoku.craft.java.pet;

import madoku.craft.java.core.sync.SyncPlayerAPIManager;
import madoku.craft.java.pet.PetComponentsAPIManager.PetHolder;
import madoku.craft.java.pet.PetComponentsAPIManager.PetInventory;
import madoku.craft.java.pet.PetConfigManager.PetRule;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Owns pet ability lore and the server-side HUD synchronization boundary. */
public final class PetHudManager {
	private static final int MAX_LORE_LINE_CHARACTERS = 64;
	private static final Set<UUID> DIRTY_PLAYERS = new HashSet<>();
	private PetHudManager() {
	}

	public static void initialize() {
		PetHudAPIManager.registerProvider(new MadokuPetHudProvider());
	}

	static void clear() {
		DIRTY_PLAYERS.clear();
	}

	static void clearPlayer(UUID playerId) {
		if (playerId != null) DIRTY_PLAYERS.remove(playerId);
	}

	static void markAbilityHudDirty(UUID playerId) {
		if (playerId != null) DIRTY_PLAYERS.add(playerId);
	}

	static void flushAbilityHudSyncs(MinecraftServer server) {
		if (server == null || DIRTY_PLAYERS.isEmpty()) return;
		List<UUID> dirtyPlayers = new ArrayList<>(DIRTY_PLAYERS);
		DIRTY_PLAYERS.clear();
		for (UUID playerId : dirtyPlayers) {
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if (player != null) {
				sendPetInventory(player);
				sendAbilityCooldowns(player, PetAbilitiesManager.currentAbilityCooldowns(player));
			}
		}
	}

	private static void sendPetInventory(ServerPlayer player) {
		if (!(player instanceof PetHolder holder)) return;
		PetInventory inventory = holder.madokuCraft$getPetInventory();
		if (inventory == null) return;
		List<ItemStack> slots = new ArrayList<>(inventory.getContainerSize());
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			slots.add(inventory.getItem(slot).copy());
		}
		SyncPlayerAPIManager.send(player, new PetPayloadAPIManager.PetInventoryPayload(slots));
	}

	public static void applyAbilityLore(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return;
		PetRule rule = PetConfigManager.resolvePetRule(stack);
		if (rule == null) return;
		stack.remove(DataComponents.LORE);
		List<Component> lines = new ArrayList<>();
		if (PetEntitiesManager.isPetItem(stack)) {
			addWrappedLoreLines(lines, "Level: " + PetEntitiesManager.petLevel(stack), ChatFormatting.AQUA);
		}
		for (String description : rule.abilityDescriptions()) {
			addWrappedLoreLines(lines, description, ChatFormatting.GOLD);
		}
		for (String cooldownDescription : rule.cooldownDescriptions()) {
			addWrappedLoreLines(lines, cooldownDescription, ChatFormatting.GRAY);
		}
		if (!lines.isEmpty()) stack.set(DataComponents.LORE, new ItemLore(lines));
	}

	private static void addWrappedLoreLines(List<Component> lines, String text, ChatFormatting formatting) {
		for (String line : wrapLoreText(text)) {
			lines.add(Component.literal(line).withStyle(formatting));
		}
	}

	private static List<String> wrapLoreText(String text) {
		List<String> wrapped = new ArrayList<>();
		if (text == null || text.isBlank()) {
			return wrapped;
		}

		String remaining = text.trim();
		while (remaining.length() > MAX_LORE_LINE_CHARACTERS) {
			int breakAt = remaining.lastIndexOf(' ', MAX_LORE_LINE_CHARACTERS);
			if (breakAt <= 0) {
				breakAt = MAX_LORE_LINE_CHARACTERS;
			}
			wrapped.add(remaining.substring(0, breakAt));
			remaining = remaining.substring(breakAt).trim();
		}
		if (!remaining.isEmpty()) {
			wrapped.add(remaining);
		}
		return wrapped;
	}

	public static void applySupportedPetLore(ItemStack stack) {
		if (PetConfigManager.isEnabled() && PetConfigManager.isValidPet(stack)) applyAbilityLore(stack);
	}

	static void sendAbilityCooldowns(ServerPlayer player, int[] remainingTicks) {
		if (player != null) {
			SyncPlayerAPIManager.send(player, PetPayloadAPIManager.PetAbilityHudPayload.fromArray(remainingTicks));
		}
	}

}
