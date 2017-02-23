package cr0s.warpdrive.client;

import cr0s.warpdrive.CommonProxy;
import cr0s.warpdrive.entity.EntityParticleBunch;
import cr0s.warpdrive.render.RenderEntityParticleBunch;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void registerRendering() {
		super.registerRendering();
		RenderingRegistry.registerEntityRenderingHandler(EntityParticleBunch.class, new RenderEntityParticleBunch());
	}
}