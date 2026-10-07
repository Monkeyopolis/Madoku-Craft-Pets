package madoku.craft.java.pet.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public final class MadokuEntityTypes {
	public static final EntityType<?> WITCH = resolve("minecraft:witch");

	private MadokuEntityTypes() {
	}

	private static EntityType<?> resolve(String id) {
		Identifier identifier = Identifier.tryParse(id);
		return identifier == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(identifier);
	}
}

