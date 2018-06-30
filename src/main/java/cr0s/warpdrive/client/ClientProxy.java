package cr0s.warpdrive.client;

import cr0s.warpdrive.CommonProxy;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.api.IItemBase;
import cr0s.warpdrive.event.ClientHandler;
import cr0s.warpdrive.event.ModelBakeEventHandler;
import cr0s.warpdrive.render.*;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void onForgePreInitialisation() {
		super.onForgePreInitialisation();
		
		OBJLoader.INSTANCE.addDomain(WarpDrive.MODID);
		
		ModelLoaderRegistry.registerLoader(MyCustomModelLoader.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ModelBakeEventHandler.instance);
	}
	
	@Override
	public void onForgeInitialisation() {
		super.onForgeInitialisation();
		
		// creative tab
		WarpDrive.creativeTabWarpDrive.setBackgroundImageName("items.png");
		
		// event handlers
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		
		// generic rendering
		// MinecraftForge.EVENT_BUS.register(new WarpDriveKeyBindings());
		MinecraftForge.EVENT_BUS.register(new RenderOverlayAir());
		MinecraftForge.EVENT_BUS.register(new RenderOverlayCamera());
		MinecraftForge.EVENT_BUS.register(new RenderOverlayLocation());
		
		MinecraftForge.EVENT_BUS.register(new ClientCameraHandler());
		
		// entity rendering
		// RenderingRegistry.registerEntityRenderingHandler(EntityXXX.class, RenderXXX::new);
		// RenderingRegistry.registerEntityRenderingHandler(EntityParticleBunch.class, new RenderEntityParticleBunch());
		// @TODO MC1.10 force field rendering
		/*
		RenderBlockStandard.renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(RenderBlockStandard.instance);
		
		RenderBlockForceField.renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(RenderBlockForceField.instance);
		
		RenderBlockOmnipanel.renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(RenderBlockOmnipanel.instance);
		
		RenderBlockShipScanner.renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(RenderBlockShipScanner.instance);
		
		RenderBlockTransporterBeacon.renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(RenderBlockTransporterBeacon.instance);
		/**/
	}
	
	@Override
	public void onModelInitialisation(final Object object) {
		if (object instanceof IBlockBase) {
			((IBlockBase) object).modelInitialisation();
		} else if (object instanceof Block) {
			final Item item = Item.getItemFromBlock((Block) object);
			modelInitialisation(item);
		} else if (object instanceof Item) {
			modelInitialisation((Item) object);
		} else if (object == null) {
			WarpDrive.logger.info("Ignoring null object ModelInitialisation...");
		} else {
			throw new RuntimeException("Invalid object " + object);
		}
	}
	
	@Nonnull
	public static ModelResourceLocation getModelResourceLocation(final ItemStack itemStack) {
		final Item item = itemStack.getItem();
		ResourceLocation resourceLocation = item.getRegistryName();
		assert resourceLocation != null;
		if (item.getHasSubtypes()) {
			resourceLocation = new ResourceLocation(resourceLocation.getResourceDomain(), resourceLocation.getResourcePath() + "-" + itemStack.getItemDamage());
		}
		return new ModelResourceLocation(resourceLocation, "inventory");
	}
	
	public static void modelInitialisation(final Item item) {
		if (item == null) {
			throw new RuntimeException("Unable to ModelInitialize a null item");
		} else if (item == Items.AIR) {
			throw new RuntimeException("Unable to ModelInitialize an air item");
		} else if (!item.getHasSubtypes()) {
			assert item.getRegistryName() != null;
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		} else {
			NonNullList<ItemStack> listItemStacks = NonNullList.create();
			assert item.getCreativeTab() != null;
			item.getSubItems(item.getCreativeTab(), listItemStacks);
			for (ItemStack itemStack : listItemStacks) {
				ModelResourceLocation modelResourceLocation; 
				if (item instanceof IItemBase) {
					modelResourceLocation = ((IItemBase) item).getModelResourceLocation(itemStack);
				} else {
					modelResourceLocation = getModelResourceLocation(itemStack);
				}
				ModelLoader.setCustomModelResourceLocation(item, itemStack.getMetadata(), modelResourceLocation);
			}
		}
	}
}