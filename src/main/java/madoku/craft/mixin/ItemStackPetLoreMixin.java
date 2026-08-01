package madoku.craft.mixin;

import madoku.craft.pet.PetHudManager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackPetLoreMixin {
	@Inject(method = "getTooltipLines", at = @At("HEAD"))
	private void madokuCraftPets$refreshPetLore(
		net.minecraft.world.item.Item.TooltipContext context,
		net.minecraft.world.entity.player.Player player,
		net.minecraft.world.item.TooltipFlag flag,
		CallbackInfoReturnable<java.util.List<net.minecraft.network.chat.Component>> cir
	) {
		PetHudManager.applySupportedPetLore((ItemStack) (Object) this);
	}
}
