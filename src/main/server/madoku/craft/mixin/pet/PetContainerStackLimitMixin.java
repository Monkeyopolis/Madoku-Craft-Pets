package madoku.craft.mixin.pet;

import madoku.craft.java.pet.PetAbilitiesManager;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies Storage Efficiency only to the inventory owned by the affected player. */
@Mixin(Container.class)
public interface PetContainerStackLimitMixin {
	@Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$applyStorageEfficiency(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (!((Object) this instanceof Inventory inventory)) {
			return;
		}

		int original = cir.getReturnValue();
		int adjusted = PetAbilitiesManager.adjustPlayerInventoryStackLimit(inventory.player, stack, original);
		if (stack != null && !stack.isEmpty()) {
			adjusted = Math.max(adjusted, stack.getCount());
		}
		if (adjusted != original) {
			cir.setReturnValue(adjusted);
		}
	}
}
