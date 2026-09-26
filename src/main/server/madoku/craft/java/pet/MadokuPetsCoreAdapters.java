package madoku.craft.java.pet;

import madoku.craft.java.core.iteminput.ItemInputFeatureAPIManager;
import madoku.craft.java.core.iteminput.ItemInputFeatureAdapter;
import madoku.craft.java.core.loot.LootFeatureAPIManager;
import madoku.craft.java.core.loot.LootFeatureAdapter;
import madoku.craft.java.core.rarity.RarityAPIManager;
import madoku.craft.java.core.rarity.RarityEligibilityAPIManager;
import madoku.craft.java.core.rarity.RarityEligibilityAdapter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

/** Installs the Core adapters that are implemented by the Pets module. */
public final class MadokuPetsCoreAdapters {
	private MadokuPetsCoreAdapters() {
	}

	public static void initialize() {
		RarityEligibilityAPIManager.registerAdapter(new RarityEligibilityAdapter() {
			@Override
			public boolean isEligible(ItemStack stack) {
				return PetEntitiesAPIManager.isPetItem(stack);
			}
		});
		LootFeatureAPIManager.registerAdapter(new LootFeatureAdapter() {
			@Override
			public ServerPlayer resolvePlayerDamageSource(DamageSource damageSource) {
				return resolvePetOwner(damageSource);
			}

			@Override
			public void applyPetLore(ItemStack stack) {
				PetHagAPIManager.applyLore(stack);
			}

			@Override
			public boolean isPetsEnabled() {
				return PetAPIManager.isEnabled();
			}
		});
		ItemInputFeatureAPIManager.registerAdapter(new ItemInputFeatureAdapter() {
			@Override
			public boolean isPetItem(ItemStack stack) {
				return PetEntitiesAPIManager.isPetItem(stack);
			}

			@Override
			public RarityAPIManager.Tier petRarity(ItemStack stack) {
				return RarityAPIManager.fromString(PetHagAPIManager.rarity(stack));
			}

			@Override
			public void applyPetLore(ItemStack stack) {
				PetHagAPIManager.applyLore(stack);
			}
		});
	}

	private static ServerPlayer resolvePetOwner(DamageSource damageSource) {
		if (damageSource == null) return null;
		ServerPlayer owner = resolvePetOwner(damageSource.getEntity());
		if (owner != null) return owner;
		Entity directEntity = damageSource.getDirectEntity();
		owner = resolvePetOwner(directEntity);
		if (owner != null) return owner;
		if (directEntity instanceof Projectile projectile) {
			return resolvePetOwner(projectile.getOwner());
		}
		return null;
	}

	private static ServerPlayer resolvePetOwner(Entity entity) {
		if (!(entity instanceof MadokuPetEntity pet) || !(pet.level() instanceof ServerLevel level)) return null;
		if (pet.ownerUuid() == null || level.getServer() == null) return null;
		return level.getServer().getPlayerList().getPlayer(pet.ownerUuid());
	}
}
