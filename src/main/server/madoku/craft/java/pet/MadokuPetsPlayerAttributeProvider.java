package madoku.craft.java.pet;

import madoku.craft.java.core.attribute.PlayerAttributeProvider;
import madoku.craft.java.core.attribute.PlayerAttributeStat;
import net.minecraft.server.level.ServerPlayer;

/** Supplies passive pet health and strength contributions to the shared resolver. */
public final class MadokuPetsPlayerAttributeProvider implements PlayerAttributeProvider {
	@Override
	public String id() {
		return "pets";
	}

	@Override
	public double bonus(ServerPlayer player, PlayerAttributeStat stat) {
		return switch (stat) {
			case HEALTH -> PetAbilitiesManager.playerAttributeBonus(player, PlayerAttributeStat.HEALTH);
			case STRENGTH -> PetAbilitiesManager.playerAttributeBonus(player, PlayerAttributeStat.STRENGTH);
			default -> 0.0D;
		};
	}
}
