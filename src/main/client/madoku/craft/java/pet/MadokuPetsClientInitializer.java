package madoku.craft.java.pet;

import madoku.craft.java.core.module.MadokuClientRuntime;
import madoku.craft.java.core.module.MadokuModule;
import madoku.craft.java.core.module.MadokuModuleIds;
import net.fabricmc.api.ClientModInitializer;

/** Fabric client entrypoint for the standalone Pets jar. */
public final class MadokuPetsClientInitializer implements ClientModInitializer, MadokuModule {
	@Override public net.minecraft.resources.Identifier id() { return MadokuModuleIds.PETS; }
	@Override public void onInitializeClient() { MadokuClientRuntime.initialize(this); }
	@Override public void initialize() { MadokuPetClient.initialize(); }
}
