package madoku.craft.mixin.pet;

import madoku.craft.java.pet.PetComponentsAPIManager;
import madoku.craft.java.pet.PetAbilitiesAPIManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class LivingEntityPetBehaviorMixin {
	@Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$scalePlayerJumpPower(CallbackInfoReturnable<Float> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (entity instanceof Player player) {
			cir.setReturnValue(PetAbilitiesAPIManager.scalePlayerJumpPower(player, cir.getReturnValue()));
		}
	}

	@Inject(method = "travel", at = @At("TAIL"))
	private void madokuCraft$stopWebStunnedHorizontalMovement(Vec3 travelVector, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (PetAbilitiesAPIManager.isWebStunned(entity)) {
			Vec3 movement = entity.getDeltaMovement();
			double verticalVelocity = movement.y;
			if (entity.isNoGravity()) {
				verticalVelocity = Math.max(-0.5D, Math.min(-0.08D, movement.y - 0.08D));
			}
			entity.setDeltaMovement(new Vec3(0.0D, verticalVelocity, 0.0D));
		} else if (entity.isNoGravity()) {
			entity.setDeltaMovement(PetAbilitiesAPIManager.scaleWebMovement(entity, entity.getDeltaMovement()));
		}
	}

	@ModifyArgs(
		method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"
		)
	)
	private void madokuCraft$applyRampageDamageBoost(Args args) {
		DamageSource source = (DamageSource) args.get(0);
		float amount = (Float) args.get(1);
		args.set(1, PetAbilitiesAPIManager.applyOutgoingDamage(source, amount));
	}

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$preventManagedPetDamage(
		ServerLevel level,
		DamageSource source,
		float amount,
		CallbackInfoReturnable<Boolean> cir
	) {
		Entity entity = (Entity) (Object) this;
		if (PetComponentsAPIManager.isManaged(entity)
			|| PetAbilitiesAPIManager.isWolfpackFriendlyFire(entity, source)
			|| PetAbilitiesAPIManager.isReflectiveTauntTarget(entity)) {
			PetAbilitiesAPIManager.recordManagedPetDamage(entity, source, amount);
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "getSpeed", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$scaleWebSlowMovement(CallbackInfoReturnable<Float> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		float speed = PetAbilitiesAPIManager.scaleWebMovementSpeed(entity, cir.getReturnValue());
		speed = PetAbilitiesAPIManager.scaleTauntedMovementSpeed(entity, speed);
		cir.setReturnValue(PetAbilitiesAPIManager.scalePlayerMovementSpeed(entity, speed));
	}

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$preventWebStunnedAttacks(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (source != null && source.getEntity() instanceof LivingEntity attacker && PetAbilitiesAPIManager.isWebStunned(attacker)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$disableManagedPetPickability(CallbackInfoReturnable<Boolean> cir) {
		if (PetComponentsAPIManager.isManaged((Entity) (Object) this)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$disableManagedPetPushability(CallbackInfoReturnable<Boolean> cir) {
		if (PetComponentsAPIManager.isManaged((Entity) (Object) this)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "push", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$cancelManagedPetPush(Entity entity, CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		if (PetComponentsAPIManager.isManaged(self) || PetComponentsAPIManager.isManaged(entity)) {
			ci.cancel();
		}
	}
}


