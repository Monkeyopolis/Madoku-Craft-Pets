package madoku.craft.pets;

import madoku.craft.entity.MadokuEntities;
import madoku.craft.api.MadokuAPIManager;
import madoku.craft.pet.MadokuPetManager;
import madoku.craft.pet.PetPayloadManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Madokucraftpets implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-pets";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PetPayloadManager.initialize();
		MadokuEntities.initialize();
		MadokuPetManager.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MadokuEntities.reset();
			MadokuPetManager.reset();
			MadokuEntities.loadPersistedData(server);
			MadokuPetManager.loadPersistedData(server);
			MadokuEntities.onServerStarted(server);
			MadokuPetManager.onServerStarted(server);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			MadokuEntities.savePersistedData(server);
			MadokuPetManager.savePersistedData(server);
			MadokuAPIManager.savePersistedData(server);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			MadokuEntities.reset();
			MadokuPetManager.reset();
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			MadokuEntities.autosavePersistedData(server);
			MadokuPetManager.autosavePersistedData(server);
			MadokuAPIManager.autosavePersistedData(server);
		});
	}
}
