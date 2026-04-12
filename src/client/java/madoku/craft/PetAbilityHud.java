package madoku.craft;

import madoku.craft.pet.PlayerEntitiesHolder;
import madoku.craft.pet.PlayerEntitiesInventory;
import madoku.craft.pet.PlayerEntitiesSystem;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class PetAbilityHud {
	private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("madoku-craft-pets", "pet_ability_hud");
	private static final Identifier ABILITY_SLOT_TEXTURE =
		Identifier.fromNamespaceAndPath("madoku-craft-pets", "textures/gui/interface/ability_slot.png");
	private static final int ABILITY_SLOT_TEXTURE_SIZE = 16;
	private static final int HOTBAR_HALF_WIDTH = 91;
	private static final int HOTBAR_SLOT_ROW_HEIGHT = 22;
	private static final int OFFHAND_SLOT_WIDTH = 29;
	private static final int OFFHAND_TO_ABILITY_SPACING = 7;
	private static final int ABILITY_SLOT_SIZE = 16;
	private static final int ABILITY_SLOT_SPACING = 1;
	private static final int ABILITY_SLOT_Y_OFFSET = (HOTBAR_SLOT_ROW_HEIGHT - ABILITY_SLOT_SIZE) / 2;
	private static final int ABILITY_ITEM_RENDER_SIZE = 12;
	private static final float ABILITY_ITEM_SCALE = 0.75F;
	private static final int ABILITY_COOLDOWN_OVERLAY_COLOR = 0x7FFFFFFF;
	private static final float ABILITY_COOLDOWN_TEXT_SCALE = 0.75F;
	private static final int ABILITY_COOLDOWN_TEXT_COLOR = 0xFFFFFFFF;
	private static final int ABILITY_COOLDOWN_TEXT_Y_SPACING = 2;
	private static final long[] PET_ABILITY_COOLDOWN_END_TICKS = new long[PlayerEntitiesSystem.SLOT_COUNT];
	private static boolean initialized;

	private PetAbilityHud() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}
		initialized = true;
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, HUD_ID, PetAbilityHud::render);
	}

	private static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || client.level == null || client.options.hideGui || player.isSpectator() || !PlayerEntitiesSystem.isEnabled()) {
			return;
		}
		if (!(player instanceof PlayerEntitiesHolder holder)) {
			return;
		}

		PlayerEntitiesInventory inventory = holder.madokuCraft$getPlayerEntitiesInventory();
		if (inventory == null) {
			return;
		}

		List<Integer> visibleSlots = visibleAbilitySlots(inventory);
		if (visibleSlots.isEmpty()) {
			return;
		}

		int slotY = context.guiHeight() - HOTBAR_SLOT_ROW_HEIGHT + ABILITY_SLOT_Y_OFFSET;
		int[] slotXs = computeAbilitySlotXs(context, player, visibleSlots.size());
		for (int index = 0; index < visibleSlots.size(); index++) {
			int slot = visibleSlots.get(index);
			int slotX = slotXs[index];
			context.blit(
				RenderPipelines.GUI_TEXTURED,
				ABILITY_SLOT_TEXTURE,
				slotX,
				slotY,
				0.0F,
				0.0F,
				ABILITY_SLOT_SIZE,
				ABILITY_SLOT_SIZE,
				ABILITY_SLOT_TEXTURE_SIZE,
				ABILITY_SLOT_TEXTURE_SIZE
			);

			ItemStack stack = inventory.getItem(slot);
			if (stack == null || stack.isEmpty()) {
				continue;
			}

			int itemX = slotX + ((ABILITY_SLOT_SIZE - ABILITY_ITEM_RENDER_SIZE) / 2);
			int itemY = slotY + ((ABILITY_SLOT_SIZE - ABILITY_ITEM_RENDER_SIZE) / 2);
			renderScaledAbilityItem(context, stack, itemX, itemY);
			renderAbilityCooldownOverlay(context, client, stack, slot, slotX, slotY, itemX, itemY);
		}
	}

	private static List<Integer> visibleAbilitySlots(PlayerEntitiesInventory inventory) {
		List<Integer> visible = new ArrayList<>(PlayerEntitiesSystem.SLOT_COUNT);
		for (int slot = 0; slot < Math.min(PlayerEntitiesSystem.SLOT_COUNT, inventory.getContainerSize()); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack == null || stack.isEmpty() || !PlayerEntitiesSystem.hasAbility(stack)) {
				continue;
			}
			visible.add(slot);
		}
		return visible;
	}

	private static void renderScaledAbilityItem(GuiGraphicsExtractor context, ItemStack stack, int x, int y) {
		context.pose().pushMatrix();
		context.pose().translate(x, y);
		context.pose().scale(ABILITY_ITEM_SCALE, ABILITY_ITEM_SCALE);
		context.item(stack, 0, 0);
		context.pose().popMatrix();
	}

	private static void renderAbilityCooldownOverlay(
		GuiGraphicsExtractor context,
		Minecraft client,
		ItemStack stack,
		int slot,
		int slotX,
		int slotY,
		int itemX,
		int itemY
	) {
		if (client.level == null) {
			return;
		}

		int totalCooldownTicks = PlayerEntitiesSystem.abilityCooldownTicks(stack);
		if (totalCooldownTicks <= 0 || slot < 0 || slot >= PET_ABILITY_COOLDOWN_END_TICKS.length) {
			return;
		}

		long remainingTicks = Math.max(0L, PET_ABILITY_COOLDOWN_END_TICKS[slot] - client.level.getGameTime());
		if (remainingTicks <= 0L) {
			return;
		}

		float cooldownPercent = Math.min(1.0F, remainingTicks / (float) totalCooldownTicks);
		int overlayTop = itemY + Mth.floor(ABILITY_ITEM_RENDER_SIZE * (1.0F - cooldownPercent));
		int overlayBottom = overlayTop + Mth.ceil(ABILITY_ITEM_RENDER_SIZE * cooldownPercent);
		context.fill(RenderPipelines.GUI, itemX, overlayTop, itemX + ABILITY_ITEM_RENDER_SIZE, overlayBottom, ABILITY_COOLDOWN_OVERLAY_COLOR);
		renderAbilityCooldownLabel(context, client, slotX, slotY, remainingTicks);
	}

	private static void renderAbilityCooldownLabel(
		GuiGraphicsExtractor context,
		Minecraft client,
		int slotX,
		int slotY,
		long remainingTicks
	) {
		String cooldownText = Integer.toString(Math.max(1, Mth.ceil(remainingTicks / 20.0F)));
		int textWidth = Math.round(client.font.width(cooldownText) * ABILITY_COOLDOWN_TEXT_SCALE);
		int textX = slotX + ((ABILITY_SLOT_SIZE - textWidth) / 2);
		int textY = slotY - Math.round(client.font.lineHeight * ABILITY_COOLDOWN_TEXT_SCALE) - ABILITY_COOLDOWN_TEXT_Y_SPACING;
		context.pose().pushMatrix();
		context.pose().scale(ABILITY_COOLDOWN_TEXT_SCALE, ABILITY_COOLDOWN_TEXT_SCALE);
		context.text(
			client.font,
			cooldownText,
			Math.round(textX / ABILITY_COOLDOWN_TEXT_SCALE),
			Math.round(textY / ABILITY_COOLDOWN_TEXT_SCALE),
			ABILITY_COOLDOWN_TEXT_COLOR,
			true
		);
		context.pose().popMatrix();
	}

	private static int[] computeAbilitySlotXs(GuiGraphicsExtractor context, LocalPlayer player, int slotCount) {
		int[] xs = new int[Math.max(0, slotCount)];
		if (slotCount <= 0) {
			return xs;
		}

		int centerX = context.guiWidth() / 2;
		boolean offhandOnLeft = player.getMainArm() == HumanoidArm.RIGHT;
		boolean offhandVisible = !player.getOffhandItem().isEmpty();
		int hotbarLeftEdge = centerX - HOTBAR_HALF_WIDTH;
		int hotbarRightEdge = centerX + HOTBAR_HALF_WIDTH;
		if (offhandOnLeft) {
			int anchorX = offhandVisible ? hotbarLeftEdge - OFFHAND_SLOT_WIDTH : hotbarLeftEdge;
			int startX = anchorX - OFFHAND_TO_ABILITY_SPACING - ABILITY_SLOT_SIZE;
			for (int slot = 0; slot < xs.length; slot++) {
				xs[slot] = startX - (slot * (ABILITY_SLOT_SIZE + ABILITY_SLOT_SPACING));
			}
			return xs;
		}

		int anchorX = offhandVisible ? hotbarRightEdge + OFFHAND_SLOT_WIDTH : hotbarRightEdge;
		int startX = anchorX + OFFHAND_TO_ABILITY_SPACING;
		for (int slot = 0; slot < xs.length; slot++) {
			xs[slot] = startX + (slot * (ABILITY_SLOT_SIZE + ABILITY_SLOT_SPACING));
		}
		return xs;
	}

	public static void setPetAbilityCooldowns(int[] remainingTicks) {
		Minecraft client = Minecraft.getInstance();
		long now = client.level == null ? 0L : client.level.getGameTime();
		for (int slot = 0; slot < PET_ABILITY_COOLDOWN_END_TICKS.length; slot++) {
			int remaining = remainingTicks != null && slot < remainingTicks.length ? Math.max(0, remainingTicks[slot]) : 0;
			PET_ABILITY_COOLDOWN_END_TICKS[slot] = remaining <= 0 ? 0L : now + remaining;
		}
	}

	public static void clearPetAbilityHudState() {
		for (int slot = 0; slot < PET_ABILITY_COOLDOWN_END_TICKS.length; slot++) {
			PET_ABILITY_COOLDOWN_END_TICKS[slot] = 0L;
		}
	}
}
