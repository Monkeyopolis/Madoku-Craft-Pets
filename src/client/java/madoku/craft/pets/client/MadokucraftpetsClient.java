package madoku.craft.pets.client;

import madoku.craft.PetAbilityHud;
import madoku.craft.entity.MadokuEntitiesClient;
import madoku.craft.inventory.PlayerEntitiesInventoryClient;
import madoku.craft.network.PetAbilityHudPayload;
import madoku.craft.network.PetSoundStatePayload;
import madoku.craft.pet.PetSoundState;
import madoku.craft.trade.MerchantEggVariantsClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MadokucraftpetsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PetAbilityHud.initialize();
		MadokuEntitiesClient.initialize();
		PlayerEntitiesInventoryClient.initialize();
		MerchantEggVariantsClient.initialize();

		ClientPlayNetworking.registerGlobalReceiver(PetAbilityHudPayload.TYPE, (payload, context) ->
			PetAbilityHud.setPetAbilityCooldowns(payload.asArray())
		);
		ClientPlayNetworking.registerGlobalReceiver(PetSoundStatePayload.TYPE, (payload, context) ->
			PetSoundState.set(parseUuid(payload.petUuid()), payload.itemId())
		);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			PetAbilityHud.clearPetAbilityHudState();
			PetSoundState.clear();
		});
	}

	private static java.util.UUID parseUuid(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return java.util.UUID.fromString(value);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}
}
