package madoku.craft.mixin.pet;

import madoku.craft.java.pet.PetAbilitiesAPIManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enderman.class)
public abstract class EndermanPetBehaviorMixin {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$prioritizeReflectiveTaunt(LivingEntity target, CallbackInfo ci) {
		Enderman enderman = (Enderman) (Object) this;
		LivingEntity reflectiveTauntTarget = PetAbilitiesAPIManager.reflectiveTauntTargetFor(enderman);
		if (reflectiveTauntTarget != null && target != reflectiveTauntTarget) {
			ci.cancel();
		}
	}
}
