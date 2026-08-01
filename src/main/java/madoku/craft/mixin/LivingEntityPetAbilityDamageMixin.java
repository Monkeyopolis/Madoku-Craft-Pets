package madoku.craft.mixin;

import madoku.craft.pet.PetAbilitiesManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityPetAbilityDamageMixin {
	@Inject(method = "getDamageAfterArmorAbsorb", at = @At("RETURN"), cancellable = true)
	private void madokuCraftPets$applyPetDamageAbilities(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		float adjusted = cir.getReturnValueF();
		adjusted = PetAbilitiesManager.applyFallDamage(entity, source, adjusted);
		adjusted = PetAbilitiesManager.applyDamageBlock(entity, source, adjusted);
		adjusted = PetAbilitiesManager.applyMobScanDamage(entity, adjusted);
		cir.setReturnValue(adjusted);
	}
}
