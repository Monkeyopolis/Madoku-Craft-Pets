package madoku.craft.entity;

import madoku.craft.pets.Madokucraftpets;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;

public class HagRenderer extends WitchRenderer {
	private static final Identifier HAG_TEXTURE = Identifier.fromNamespaceAndPath(Madokucraftpets.MOD_ID, "textures/entities/hag.png");

	public HagRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(WitchRenderState renderState) {
		return HAG_TEXTURE;
	}
}
