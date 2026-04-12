package madoku.craft.mixin;

import madoku.craft.pet.PlayerEntitiesHolder;
import madoku.craft.pet.PlayerEntitiesInventory;
import madoku.craft.pet.PlayerEntitiesSystem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntitiesInventoryMixin implements PlayerEntitiesHolder {
	@Unique
	private final PlayerEntitiesInventory madokuCraft$playerEntitiesInventory = new PlayerEntitiesInventory();

	@Override
	public PlayerEntitiesInventory madokuCraft$getPlayerEntitiesInventory() {
		return madokuCraft$playerEntitiesInventory;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void madokuCraft$savePlayerEntities(ValueOutput output, CallbackInfo ci) {
		for (int slot = 0; slot < madokuCraft$playerEntitiesInventory.getContainerSize(); slot++) {
			ItemStack stack = madokuCraft$playerEntitiesInventory.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}

			output.store(madokuCraft$slotDataKey(slot), ItemStack.CODEC, stack);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void madokuCraft$loadPlayerEntities(ValueInput input, CallbackInfo ci) {
		for (int slot = 0; slot < madokuCraft$playerEntitiesInventory.getContainerSize(); slot++) {
			ItemStack stack = input.read(madokuCraft$slotDataKey(slot), ItemStack.CODEC).orElse(ItemStack.EMPTY);
			if (stack.isEmpty()) {
				stack = madokuCraft$legacyPlayerEntity(input, slot);
			}
			if (!stack.isEmpty()) {
				PlayerEntitiesSystem.applyAbilityLore(stack);
			}
			madokuCraft$playerEntitiesInventory.setItem(slot, PlayerEntitiesSystem.isValidPlayerEntity(stack) ? stack : ItemStack.EMPTY);
		}
		madokuCraft$playerEntitiesInventory.setChanged();
	}

	@Unique
	private static ItemStack madokuCraft$legacyPlayerEntity(ValueInput input, int slot) {
		String itemId = input.getStringOr(madokuCraft$slotKey(slot), "");
		if (itemId.isBlank()) {
			itemId = input.getStringOr(madokuCraft$legacySlotKey(slot), "");
		}
		if (itemId.isBlank()) {
			return ItemStack.EMPTY;
		}

		Identifier identifier = Identifier.tryParse(itemId);
		Item item = identifier == null ? null : BuiltInRegistries.ITEM.getValue(identifier);
		return item == null ? ItemStack.EMPTY : new ItemStack(item);
	}

	@Unique
	private static String madokuCraft$slotDataKey(int slot) {
		return PlayerEntitiesSystem.SAVE_KEY + ".stack." + slot;
	}

	@Unique
	private static String madokuCraft$slotKey(int slot) {
		return PlayerEntitiesSystem.SAVE_KEY + "." + slot;
	}

	@Unique
	private static String madokuCraft$legacySlotKey(int slot) {
		return PlayerEntitiesSystem.legacySaveKey() + "." + slot;
	}
}
