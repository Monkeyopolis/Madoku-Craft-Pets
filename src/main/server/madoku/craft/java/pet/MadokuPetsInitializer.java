package madoku.craft.java.pet;

import madoku.craft.java.core.module.MadokuStandaloneModule;
import madoku.craft.java.core.module.MadokuStandaloneRuntime;
import madoku.craft.java.core.module.MadokuModuleIds;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;

/** Fabric entrypoint for the standalone Pets jar. */
public final class MadokuPetsInitializer implements ModInitializer, MadokuStandaloneModule {
	@Override public net.minecraft.resources.Identifier id() { return MadokuModuleIds.PETS; }
	@Override public void onInitialize() { MadokuStandaloneRuntime.initialize(this); }
	@Override public void initialize() { PetAPIManager.registerProvider(new MadokuPetProvider()); PetAPIManager.initialize(); }
	@Override public void reset() { PetAPIManager.reset(); }
	@Override public void loadPersistedData(MinecraftServer server) { PetAPIManager.loadPersistedData(server); }
	@Override public void onServerStarted(MinecraftServer server) { PetAPIManager.onServerStarted(server); }
	@Override public void onServerTick(MinecraftServer server) { PetAPIManager.onServerTick(server); }
}
