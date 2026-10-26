package madoku.craft.mixin.pet;

import madoku.craft.java.pet.PetAbilitiesManager;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes menu transfers use the same player-specific limit as direct inventory insertion. */
@Mixin(Slot.class)
public abstract class PetSlotStackLimitMixin {
	@Shadow @Final public Container container;

	@Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$applyStorageEfficiency(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (!(this.container instanceof Inventory inventory)) {
			return;
		}

		int original = cir.getReturnValue();
		int adjusted = PetAbilitiesManager.adjustPlayerInventoryStackLimit(inventory.player, stack, original);
		ItemStack existing = ((Slot) (Object) this).getItem();
		if (stack != null && !stack.isEmpty()
			&& existing != null && !existing.isEmpty()
			&& ItemStack.isSameItemSameComponents(existing, stack)) {
			adjusted = Math.max(adjusted, existing.getCount());
		}
		if (adjusted != original) {
			cir.setReturnValue(adjusted);
		}
	}
}
