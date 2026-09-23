package madoku.craft.java.pet;

import madoku.craft.java.core.menu.MenuAPIManager;
import madoku.craft.java.core.menu.MenuEntry;
import madoku.craft.java.pet.PetPayloadAPIManager.OpenPetMenuPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;

/** Registers the Pets route in Core's main menu. */
public final class PetMenuClient {
	private static boolean initialized;

	private PetMenuClient() { }

	public static void initialize() {
		if (initialized) return;
		MenuScreens.register(PetMenuManager.PET_MENU, PetMenuScreen::new);
		MenuAPIManager.registerEntry(new MenuEntry(
			"pets",
			"menu.madoku-craft.pets",
			texture("main-menu/pets-button.png"),
			texture("main-menu/pets-button-highlighted.png"),
			50,
			client -> {
				if (client != null && client.player != null) ClientPlayNetworking.send(new OpenPetMenuPayload());
			}
		));
		initialized = true;
	}

	private static Identifier texture(String path) {
		return Identifier.fromNamespaceAndPath("madoku-craft", "textures/" + path);
	}
}
