package madoku.craft.mixin.pet;

import madoku.craft.java.pet.PetAbilitiesManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps inventory return/drop placement aligned with the player-specific stack limit. */
@Mixin(Inventory.class)
public abstract class PetInventoryStackLimitMixin {
	@Redirect(
		method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I")
	)
	private int madokuCraft$useStorageEfficiencyLimit(ItemStack stack) {
		Inventory inventory = (Inventory) (Object) this;
		return PetAbilitiesManager.adjustPlayerInventoryStackLimit(
			inventory.player,
			stack,
			stack.getMaxStackSize()
		);
	}
}
