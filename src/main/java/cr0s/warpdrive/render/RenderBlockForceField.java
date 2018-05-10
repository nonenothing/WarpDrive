package cr0s.warpdrive.render;

import cr0s.warpdrive.block.forcefield.TileEntityForceField;
import cr0s.warpdrive.config.Dictionary;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

// wrapper to camouflage block
public class RenderBlockForceField implements ISimpleBlockRenderingHandler {
	
	public static int renderId = 0;
	public static RenderBlockForceField instance = new RenderBlockForceField();
	
	@Override
	public void renderInventoryBlock(final Block block, final int metadata, final int modelId, final RenderBlocks renderer) {
		RenderCommons.renderInventoryBlock(block, metadata, renderer);
	}
	
	@Override
	public boolean renderWorldBlock(final IBlockAccess blockAccess, final int x, final int y, final int z, final Block block, final int modelId, final RenderBlocks renderer) {
		final TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityForceField)) {
			return false;
		}
		
		int renderType = -1;
		final Block blockCamouflage = ((TileEntityForceField) tileEntity).cache_blockCamouflage;
		if (blockCamouflage != null && !Dictionary.BLOCKS_NOCAMOUFLAGE.contains(blockCamouflage)) {
			renderType = blockCamouflage.getRenderType();
		}
		
		return RenderCommons.renderWorldBlockCamouflaged(x, y, z, block, renderer, renderType, blockCamouflage);
	}
	
	@Override
	public boolean shouldRender3DInInventory(final int modelId) {
		return true;
	}
	
	@Override
	public int getRenderId() {
		return renderId;
	}
}
