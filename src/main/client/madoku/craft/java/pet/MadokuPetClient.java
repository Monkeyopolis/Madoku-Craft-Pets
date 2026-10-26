package madoku.craft.java.pet;

import madoku.craft.java.pet.entity.MadokuEntitiesClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import madoku.craft.java.pet.PetComponentsAPIManager.PetHolder;
import madoku.craft.java.pet.PetComponentsAPIManager.PetInventory;
import madoku.craft.java.pet.trade.MerchantEggVariantsClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.player.LocalPlayer;

/** Owns all client initialization for the Pets module. */
public final class MadokuPetClient {
	private static LocalPlayer trackedPlayer;
	private static boolean previousJumpPressed;
	private static boolean clientDoubleJumpUsed;

	private MadokuPetClient() {
	}

	public static void initialize() {
		PetMenuClient.initialize();
		PetHudManagerClient.initialize();
		PetReflectiveTauntRenderer.initialize();
		MadokuEntitiesClient.initialize();
		PetRendererManager.initialize();
		MerchantEggVariantsClient.initialize();
		ClientTickEvents.END_CLIENT_TICK.register(client -> PetAbilitiesManager.tickClientSprintBoosts(client.player));
		ClientTickEvents.START_CLIENT_TICK.register(MadokuPetClient::tickClientDoubleJump);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			PetHudManagerClient.reset();
			PetAbilitiesManager.invalidateClientStorageEfficiencyBonuses();
		});
		ClientPlayNetworking.registerGlobalReceiver(PetPayloadAPIManager.PetAbilityHudPayload.TYPE,
			(payload, context) -> PetHudManagerClient.setAbilityCooldowns(payload.asArray()));
		ClientPlayNetworking.registerGlobalReceiver(PetPayloadAPIManager.ReflectiveTauntVisualPayload.TYPE,
			(payload, context) -> PetReflectiveTauntRenderer.accept(payload));
		ClientPlayNetworking.registerGlobalReceiver(PetPayloadAPIManager.PetInventoryPayload.TYPE, (payload, context) -> {
			if (!(Minecraft.getInstance().player instanceof PetHolder holder)) return;
			PetInventory inventory = holder.madokuCraft$getPetInventory();
			if (inventory == null) return;
			inventory.runBulkUpdate(() -> {
				for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
					ItemStack stack = slot < payload.slots().size() ? payload.slots().get(slot) : ItemStack.EMPTY;
					inventory.setItem(slot, stack.copy());
				}
			});
			PetAbilitiesManager.invalidateClientStorageEfficiencyBonus(Minecraft.getInstance().player.getUUID());
		});
	}

	private static void tickClientDoubleJump(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) {
			if (trackedPlayer != null) {
				PetAbilitiesManager.clearClientDoubleJumpState(trackedPlayer.getUUID());
			}
			trackedPlayer = null;
			previousJumpPressed = false;
			clientDoubleJumpUsed = false;
			return;
		}
		if (trackedPlayer != player) {
			if (trackedPlayer != null) {
				PetAbilitiesManager.clearClientDoubleJumpState(trackedPlayer.getUUID());
			}
			trackedPlayer = player;
			previousJumpPressed = false;
			clientDoubleJumpUsed = false;
		}

		if (player.onGround()) {
			clientDoubleJumpUsed = false;
			PetAbilitiesManager.clearClientDoubleJumpState(player.getUUID());
		}

		boolean jumpPressed = client.options.keyJump.isDown();
		boolean jumpRisingEdge = jumpPressed && !previousJumpPressed;
		boolean hasDoubleJump = clientHasDoubleJump(player);
		if (jumpRisingEdge && !clientDoubleJumpUsed && !player.onGround()
			&& player.isAlive() && !player.isSpectator() && !player.isPassenger()
			&& !player.isFallFlying() && !player.getAbilities().flying
			&& hasDoubleJump) {
			double fallDistance = player.fallDistance;
			player.jumpFromGround();
			player.fallDistance = fallDistance;
			clientDoubleJumpUsed = true;
			PetAbilitiesManager.markClientDoubleJumpUsed(player.getUUID());
		}
		previousJumpPressed = jumpPressed;
	}

	private static boolean clientHasDoubleJump(LocalPlayer player) {
		if (!PetConfigManager.isEnabled() || !(player instanceof PetHolder holder)) {
			return false;
		}
		PetInventory inventory = holder.madokuCraft$getPetInventory();
		if (inventory == null) {
			return false;
		}
		for (int slot = 0; slot < Math.min(PetAPIManager.SLOT_COUNT, inventory.getContainerSize()); slot++) {
			ItemStack stack = inventory.getItem(slot);
			PetConfigManager.PetRule rule = PetConfigManager.resolvePetRule(stack);
			if (rule != null && rule.hasAbility(PetAPIManager.PET_ABILITY_DOUBLE_JUMP)) {
				return true;
			}
		}
		return false;
	}
}
