package madoku.craft.java.pet;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Renders the taunt boundary as vanilla-style debug geometry rather than particles. */
final class PetReflectiveTauntRenderer {
	private static final int BORDER_WHITE = 0xD8FFFFFF;
	private static final float INNER_RING_OFFSET = 0.10F;
	private static final float RING_HEIGHT = 0.92F;
	private static final double MIN_RADIUS = 0.35D;
	private static final Map<UUID, VisualState> ACTIVE = new HashMap<>();

	private PetReflectiveTauntRenderer() {
	}

	static void initialize() {
		LevelRenderEvents.BEFORE_GIZMOS.register(context -> render());
	}

	static void accept(PetPayloadAPIManager.ReflectiveTauntVisualPayload payload) {
		Minecraft client = Minecraft.getInstance();
		client.execute(() -> {
			UUID centerEntityUuid;
			try {
				centerEntityUuid = UUID.fromString(payload.centerEntityUuid());
			} catch (IllegalArgumentException exception) {
				return;
			}
			if (!payload.active()) {
				ACTIVE.remove(centerEntityUuid);
				return;
			}
			ACTIVE.put(centerEntityUuid, new VisualState(
				payload.dimensionId(),
				Math.max(1L, payload.durationTicks()),
				Math.max(MIN_RADIUS, payload.radius()),
				client.level == null ? 0L : client.level.getGameTime(),
				payload.outwardWave()
			));
		});
	}

	private static void render() {
		Minecraft client = Minecraft.getInstance();
		ClientLevel level = client.level;
		if (level == null || ACTIVE.isEmpty()) {
			return;
		}

		long now = level.getGameTime();
		float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
		Gizmos.TemporaryCollection collection = client.levelRenderer.collectPerFrameRenderThreadGizmos();
		try {
			Iterator<Map.Entry<UUID, VisualState>> iterator = ACTIVE.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<UUID, VisualState> entry = iterator.next();
				VisualState state = entry.getValue();
				if (state == null || now - state.startedClientTick >= state.durationTicks
					|| !state.dimensionId.equals(level.dimension().toString())) {
					iterator.remove();
					continue;
				}

				Entity center = level.getEntity(entry.getKey());
				if (center == null || !center.isAlive()) {
					continue;
				}

				double elapsed = Math.max(0.0D, now + partialTick - state.startedClientTick);
				double progress = Math.min(1.0D, elapsed / state.durationTicks);
				double radius = state.outwardWave
					? MIN_RADIUS + (state.radius - MIN_RADIUS) * progress
					: state.radius - (state.radius - MIN_RADIUS) * progress;
				Vec3 centerPosition = center.getPosition(partialTick);
				GizmoStyle whiteStyle = GizmoStyle.stroke(BORDER_WHITE, 1.75F);
				Gizmos.circle(centerPosition.add(0.0D, 0.10D, 0.0D), (float) radius, whiteStyle).setAlwaysOnTop();
				Gizmos.circle(centerPosition.add(0.0D, 0.10D + RING_HEIGHT, 0.0D), (float) radius, whiteStyle).setAlwaysOnTop();
				Gizmos.circle(centerPosition.add(0.0D, 0.10D + RING_HEIGHT * 2.0D, 0.0D), (float) radius, whiteStyle).setAlwaysOnTop();
				Gizmos.circle(centerPosition.add(0.0D, 0.10D, 0.0D), (float) Math.max(MIN_RADIUS, radius - INNER_RING_OFFSET), whiteStyle).setAlwaysOnTop();
			}
		} finally {
			collection.close();
		}
	}

	private record VisualState(
		String dimensionId,
		long durationTicks,
		double radius,
		long startedClientTick,
		boolean outwardWave
	) {
	}
}
