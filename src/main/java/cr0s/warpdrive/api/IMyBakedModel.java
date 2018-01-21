package cr0s.warpdrive.api;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.ResourceLocation;

public interface IMyBakedModel extends IBakedModel {
	
	void setResourceLocation(final ResourceLocation resourceLocation);
	
	void setOriginalBakedModel(final IBakedModel bakedModel);
}
