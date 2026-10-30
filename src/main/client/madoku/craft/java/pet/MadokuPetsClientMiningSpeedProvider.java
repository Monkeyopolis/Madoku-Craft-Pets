package madoku.craft.java.pet;

import madoku.craft.java.core.attribute.PlayerAttributeProvider;
import madoku.craft.java.core.attribute.PlayerAttributeStat;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Provides the client-side synchronized Silverfish Mining Efficiency bonus. */
public final class MadokuPetsClientMiningSpeedProvider implements PlayerAttributeProvider {
	@Override public String id() { return "madoku-pets-client"; }

	@Override
	public double bonus(Player player, PlayerAttributeStat stat, BlockState state) {
		return stat == PlayerAttributeStat.MINING && state != null && player instanceof LocalPlayer
			? PetAbilitiesAPIManager.miningEfficiencyBonus(player)
			: 0.0D;
	}
}
