package madoku.craft.mixin.pet;

import madoku.craft.java.pet.PetAbilitiesAPIManager;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerPetLeftClickMixin {
	@Inject(method = "handleAttack", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$preventWebStunnedPlayerAttack(
		net.minecraft.network.protocol.game.ServerboundAttackPacket packet,
		CallbackInfo ci
	) {
		ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
		if (PetAbilitiesAPIManager.isWebStunned(listener.getPlayer())) {
			ci.cancel();
			return;
		}
		PetAbilitiesAPIManager.handlePlayerLeftClick(listener.getPlayer());
	}

	@Inject(
		method = "handlePlayerInput",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		)
	)
	private void madokuCraft$handleConditionalDoubleJump(
		net.minecraft.network.protocol.game.ServerboundPlayerInputPacket packet,
		CallbackInfo ci
	) {
		ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
		net.minecraft.server.level.ServerPlayer player = listener.getPlayer();
		net.minecraft.world.entity.player.Input previousInput = listener.getPlayer().getLastClientInput();
		net.minecraft.world.entity.player.Input currentInput = packet.input();
		boolean jumpRisingEdge = currentInput != null && currentInput.jump() && (previousInput == null || !previousInput.jump());
		if (jumpRisingEdge) {
			PetAbilitiesAPIManager.handlePlayerJump(player);
		}
	}

	@Inject(method = "handleInteract", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$preventWebStunnedPlayerInteraction(
		net.minecraft.network.protocol.game.ServerboundInteractPacket packet,
		CallbackInfo ci
	) {
		ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
		if (PetAbilitiesAPIManager.isWebStunned(listener.getPlayer())) {
			ci.cancel();
		}
	}

	@Inject(method = "handleUseItem", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$preventWebStunnedPlayerItemUse(
		net.minecraft.network.protocol.game.ServerboundUseItemPacket packet,
		CallbackInfo ci
	) {
		ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
		if (PetAbilitiesAPIManager.isWebStunned(listener.getPlayer())) {
			ci.cancel();
		}
	}

	@Inject(method = "handleUseItemOn", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$preventWebStunnedPlayerBlockUse(
		net.minecraft.network.protocol.game.ServerboundUseItemOnPacket packet,
		CallbackInfo ci
	) {
		ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
		if (PetAbilitiesAPIManager.isWebStunned(listener.getPlayer())) {
			ci.cancel();
		}
	}
}

