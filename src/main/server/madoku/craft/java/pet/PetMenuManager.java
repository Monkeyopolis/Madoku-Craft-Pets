package madoku.craft.java.pet;

import madoku.craft.java.pet.PetPayloadAPIManager.OpenPetMenuPayload;
import madoku.craft.java.pet.PetPayloadAPIManager.PetInventoryPayload;
import madoku.craft.java.pet.PetPayloadAPIManager.UpgradePetPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/** Registers and opens the dedicated pet menu. */
public final class PetMenuManager {
	public static final MenuType<PetMenu> PET_MENU = Registry.register(
		BuiltInRegistries.MENU,
		Identifier.fromNamespaceAndPath("madoku-craft", "pet_menu"),
		new MenuType<>(PetMenu::new, FeatureFlags.VANILLA_SET)
	);

	private static boolean initialized;

	private PetMenuManager() { }

	public static void initialize() {
		if (initialized) return;
		PayloadTypeRegistry.clientboundPlay().register(PetInventoryPayload.TYPE, PetInventoryPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(OpenPetMenuPayload.TYPE, OpenPetMenuPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(UpgradePetPayload.TYPE, UpgradePetPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenPetMenuPayload.TYPE, (payload, context) ->
			context.player().openMenu(new SimpleMenuProvider(
				(containerId, inventory, player) -> new PetMenu(containerId, inventory),
				Component.translatable("menu.madoku-craft.pets.title")
			))
		);
		ServerPlayNetworking.registerGlobalReceiver(UpgradePetPayload.TYPE, (payload, context) -> {
			if (context.player().containerMenu instanceof PetMenu petMenu) petMenu.upgrade(context.player());
		});
		initialized = true;
	}
}
