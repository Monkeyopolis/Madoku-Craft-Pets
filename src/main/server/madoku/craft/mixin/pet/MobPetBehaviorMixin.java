package madoku.craft.mixin.pet;

import madoku.craft.java.pet.PetAbilitiesAPIManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobPetBehaviorMixin {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$prioritizeReflectiveTaunt(LivingEntity target, CallbackInfo ci) {
		Mob mob = (Mob) (Object) this;
		LivingEntity reflectiveTauntTarget = PetAbilitiesAPIManager.reflectiveTauntTargetFor(mob);
		if (reflectiveTauntTarget != null && target != reflectiveTauntTarget) {
			ci.cancel();
		}
	}
}
