package cr0s.warpdrive.event;

import cr0s.warpdrive.render.BakedModelCamouflage;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModelBakeEventHandler {
	
	public static final ModelBakeEventHandler instance = new ModelBakeEventHandler();
	private static final Collection<ModelResourceLocation> modelResourceLocationCamouflages = new ArrayList<>(64); 
	
	private ModelBakeEventHandler() {
		
	}
	
	public static void registerCamouflage(final ModelResourceLocation modelResourceLocation) {
		modelResourceLocationCamouflages.add(modelResourceLocation);
	}
	
	// Called after all the other baked block models have been added to the modelRegistry, before BlockModelShapes caches the models.
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		// add a camouflage wrapper around automatically registered models (from JSON)
		for (final ModelResourceLocation modelResourceLocation : modelResourceLocationCamouflages) {
			final Object object =  event.getModelRegistry().getObject(modelResourceLocation);
			if (object != null) {
				final IBakedModel bakedModelExisting = (IBakedModel) object;
				final BakedModelCamouflage bakedModelCamouflage = new BakedModelCamouflage(bakedModelExisting);
				event.getModelRegistry().putObject(modelResourceLocation, bakedModelCamouflage);
			}
		}
	}
}