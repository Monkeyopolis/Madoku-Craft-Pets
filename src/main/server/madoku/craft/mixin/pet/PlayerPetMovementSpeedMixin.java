package madoku.craft.mixin.pet;

import madoku.craft.java.pet.PetAbilitiesAPIManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerPetMovementSpeedMixin {
	@Inject(method = "getSpeed", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$scalePetMovementSpeed(CallbackInfoReturnable<Float> cir) {
		Player player = (Player) (Object) this;
		cir.setReturnValue(PetAbilitiesAPIManager.scalePlayerMovementSpeed(player, cir.getReturnValue()));
	}
}
