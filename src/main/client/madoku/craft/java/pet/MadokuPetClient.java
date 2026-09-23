package madoku.craft.java.pet;

import madoku.craft.java.pet.entity.MadokuEntitiesClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import madoku.craft.java.pet.PetComponentsAPIManager.PetHolder;
import madoku.craft.java.pet.PetComponentsAPIManager.PetInventory;
import madoku.craft.java.pet.trade.MerchantEggVariantsClient;

/** Owns all client initialization for the Pets module. */
public final class MadokuPetClient {
	private MadokuPetClient() {
	}

	public static void initialize() {
		PetMenuClient.initialize();
		PetHudManagerClient.initialize();
		MadokuEntitiesClient.initialize();
		PetRendererManager.initialize();
		MerchantEggVariantsClient.initialize();
		ClientPlayNetworking.registerGlobalReceiver(PetPayloadAPIManager.PetAbilityHudPayload.TYPE,
			(payload, context) -> PetHudManagerClient.setAbilityCooldowns(payload.asArray()));
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
		});
	}
}
