package madoku.craft.java.pet;

import madoku.craft.java.pet.PetComponentsAPIManager.PetHolder;
import madoku.craft.java.pet.PetComponentsAPIManager.PetInventory;
import madoku.craft.java.pet.PetComponentsAPIManager.PetSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Dedicated synchronized menu for pet equipment and pet upgrades. */
public final class PetMenu extends AbstractContainerMenu {
	public static final int PET_SLOT_START = 0;
	public static final int UPGRADE_SLOT_START = PET_SLOT_START + PetEntitiesAPIManager.SLOT_COUNT;
	public static final int UPGRADE_SLOT_COUNT = 4;
	public static final int PLAYER_INVENTORY_START = UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT;
	public static final int HOTBAR_START = PLAYER_INVENTORY_START + 27;
	public static final int MENU_SLOT_COUNT = HOTBAR_START + 9;

	private final SimpleContainer upgradeInventory = new SimpleContainer(UPGRADE_SLOT_COUNT);

	public PetMenu(int containerId, Inventory playerInventory) {
		super(PetMenuManager.PET_MENU, containerId);
		PetInventory petInventory = ((PetHolder) playerInventory.player).madokuCraft$getPetInventory();

		for (int petSlot = 0; petSlot < PetEntitiesAPIManager.SLOT_COUNT; petSlot++) {
			this.addSlot(new PetSlot(
				petInventory,
				petSlot,
				PetEntitiesAPIManager.SLOT_X,
				PetEntitiesAPIManager.SLOT_YS[petSlot]
			));
		}
		this.addSlot(new PetSlot(upgradeInventory, 0, 95, 26));
		this.addSlot(displayOnlySlot(1, 69, 74));
		this.addSlot(displayOnlySlot(2, 95, 74));
		this.addSlot(displayOnlySlot(3, 121, 74));

		for (int inventoryIndex = 9; inventoryIndex < 36; inventoryIndex++) {
			int slot = inventoryIndex - 9;
			this.addSlot(new Slot(playerInventory, inventoryIndex, 8 + slot % 9 * 18, 134 + slot / 9 * 18));
		}
		for (int hotbarIndex = 0; hotbarIndex < 9; hotbarIndex++) {
			this.addSlot(new Slot(playerInventory, hotbarIndex, 8 + hotbarIndex * 18, 192));
		}
	}

	public UpgradeRequirements getUpgradeRequirements() {
		ItemStack target = upgradeInventory.getItem(0);
		if (!PetEntitiesAPIManager.isValid(target)) return UpgradeRequirements.empty();

		int level = PetEntitiesAPIManager.petLevel(target);
		UpgradeRequirement petItems = new UpgradeRequirement(countPetItems(target.getItem()), level);
		UpgradeRequirement experienceBottles = new UpgradeRequirement(countItems(Items.EXPERIENCE_BOTTLE), powerOfTwo(level));
		UpgradeRequirement emeralds = new UpgradeRequirement(countItems(Items.EMERALD), powerOfTwo(level + 1));
		boolean belowMaximum = level < PetAPIManager.maxPetLevel();
		boolean canUpgrade = belowMaximum && petItems.isMet() && experienceBottles.isMet() && emeralds.isMet();
		return new UpgradeRequirements(true, canUpgrade, petItems, experienceBottles, emeralds);
	}

	/** Applies an upgrade on the server after rechecking all costs against the active player's inventory. */
	public boolean upgrade(ServerPlayer player) {
		if (player == null || player.containerMenu != this) return false;

		UpgradeRequirements requirements = getUpgradeRequirements();
		if (!requirements.canUpgrade()) return false;

		ItemStack target = upgradeInventory.getItem(0);
		int nextLevel = PetEntitiesAPIManager.petLevel(target) + 1;
		consumePetItems(target.getItem(), requirements.petItems().required());
		consumeItems(Items.EXPERIENCE_BOTTLE, requirements.experienceBottles().required());
		consumeItems(Items.EMERALD, requirements.emeralds().required());
		PetEntitiesAPIManager.setPetLevel(target, nextLevel);
		PetHudManager.applySupportedPetLore(target);
		upgradeInventory.setChanged();
		this.broadcastChanges();
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		if (slotIndex < 0 || slotIndex >= this.slots.size()) return ItemStack.EMPTY;

		Slot slot = this.slots.get(slotIndex);
		if (!slot.hasItem()) return ItemStack.EMPTY;

		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		boolean moved;
		if (slotIndex < PLAYER_INVENTORY_START) {
			moved = this.moveItemStackTo(stack, PLAYER_INVENTORY_START, this.slots.size(), false);
		} else if (PetEntitiesAPIManager.isValid(stack)) {
			moved = this.moveItemStackTo(stack, PET_SLOT_START, UPGRADE_SLOT_START, false);
		} else if (slotIndex < HOTBAR_START) {
			moved = this.moveItemStackTo(stack, HOTBAR_START, this.slots.size(), false);
		} else {
			moved = this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_START, false);
		}

		if (!moved) return ItemStack.EMPTY;
		if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
		else slot.setChanged();
		if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;

		slot.onTake(player, stack);
		return original;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (!(player instanceof ServerPlayer)) return;

		for (int index = 0; index < upgradeInventory.getContainerSize(); index++) {
			ItemStack stack = upgradeInventory.getItem(index);
			if (stack.isEmpty()) continue;
			player.getInventory().placeItemBackInInventory(stack.copy(), Prediction.SERVER_ONLY);
			upgradeInventory.setItem(index, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	private int countPetItems(Item petItem) {
		int count = 0;
		for (int index = PLAYER_INVENTORY_START; index < this.slots.size(); index++) {
			ItemStack stack = this.slots.get(index).getItem();
			if (stack.isEmpty() || stack.getItem() != petItem || !PetEntitiesAPIManager.isValid(stack)) continue;
			if (PetEntitiesAPIManager.petLevel(stack) == 1) count += stack.getCount();
		}
		return count;
	}

	private Slot displayOnlySlot(int inventoryIndex, int x, int y) {
		return new Slot(upgradeInventory, inventoryIndex, x, y) {
			@Override public boolean mayPlace(ItemStack stack) { return false; }
			@Override public boolean mayPickup(Player player) { return false; }
		};
	}

	private int countItems(Item item) {
		int count = 0;
		for (int index = PLAYER_INVENTORY_START; index < this.slots.size(); index++) {
			ItemStack stack = this.slots.get(index).getItem();
			if (stack.is(item)) count += stack.getCount();
		}
		return count;
	}

	private void consumePetItems(Item petItem, int amount) {
		consumeItemsMatching(amount, stack -> stack.getItem() == petItem
			&& PetEntitiesAPIManager.isValid(stack)
			&& PetEntitiesAPIManager.petLevel(stack) == 1);
	}

	private void consumeItems(Item item, int amount) {
		consumeItemsMatching(amount, stack -> stack.is(item));
	}

	private void consumeItemsMatching(int amount, java.util.function.Predicate<ItemStack> predicate) {
		int remaining = amount;
		for (int index = PLAYER_INVENTORY_START; index < this.slots.size() && remaining > 0; index++) {
			Slot slot = this.slots.get(index);
			ItemStack stack = slot.getItem();
			if (stack.isEmpty() || !predicate.test(stack)) continue;

			int consumed = Math.min(remaining, stack.getCount());
			stack.shrink(consumed);
			remaining -= consumed;
			if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
			else slot.setChanged();
		}
	}

	private static int powerOfTwo(int exponent) {
		return exponent >= 31 ? Integer.MAX_VALUE : 1 << Math.max(0, exponent);
	}

	public record UpgradeRequirement(int owned, int required) {
		public boolean isMet() { return owned >= required; }
		public String displayText() { return owned + "/" + required; }
	}

	public record UpgradeRequirements(
		boolean hasTarget,
		boolean canUpgrade,
		UpgradeRequirement petItems,
		UpgradeRequirement experienceBottles,
		UpgradeRequirement emeralds
	) {
		private static UpgradeRequirements empty() {
			UpgradeRequirement emptyRequirement = new UpgradeRequirement(0, 0);
			return new UpgradeRequirements(false, false, emptyRequirement, emptyRequirement, emptyRequirement);
		}
	}
}
