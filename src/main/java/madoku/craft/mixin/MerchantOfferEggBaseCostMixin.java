package madoku.craft.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferEggBaseCostMixin {
	@Inject(method = "getBaseCostA", at = @At("HEAD"), cancellable = true)
	private void madokuCraftPets$useActualEggCostAsBaseCost(CallbackInfoReturnable<ItemStack> cir) {
		MerchantOffer offer = (MerchantOffer) (Object) this;
		ItemStack actualCost = offer.getCostA();
		if (actualCost.is(Items.EGG)) {
			cir.setReturnValue(actualCost.copy());
		}
	}
}
