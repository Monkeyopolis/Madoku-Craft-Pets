package madoku.craft.pets.client;

import madoku.craft.entity.MadokuEntitiesClient;
import madoku.craft.inventory.PetInventoryClient;
import madoku.craft.pet.PetHudManagerClient;
import madoku.craft.pet.PetPayloadManager;
import madoku.craft.pet.PetRendererManager;
import madoku.craft.trade.MerchantEggVariantsClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MadokucraftpetsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register before installing the receiver; Fabric validates the payload type at receiver registration time.
		PetPayloadManager.initialize();
		MadokuEntitiesClient.initialize();
		PetRendererManager.initialize();
		PetHudManagerClient.initialize();
		PetInventoryClient.initialize();
		MerchantEggVariantsClient.initialize();

		ClientPlayNetworking.registerGlobalReceiver(PetPayloadManager.PetAbilityHudPayload.TYPE, (payload, context) ->
			PetHudManagerClient.setAbilityCooldowns(payload.asArray())
		);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			PetHudManagerClient.reset();
		});
	}
}
