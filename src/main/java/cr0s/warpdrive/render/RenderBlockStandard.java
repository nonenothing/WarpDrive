package cr0s.warpdrive.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

// wrapper to native classes so renderId is non-zero so we don't render faces when player camera is inside the block
public class RenderBlockStandard implements ISimpleBlockRenderingHandler {
	
	public static int renderId = 0;
	public static RenderBlockStandard instance = new RenderBlockStandard();
	
	@Override
	public void renderInventoryBlock(final Block block, final int metadata, final int modelId, final RenderBlocks renderer) {
		RenderCommons.renderInventoryBlock(block, metadata, renderer);
	}
	
	@Override
	public boolean renderWorldBlock(final IBlockAccess blockAccess, final int x, final int y, final int z, final Block block, final int modelId, final RenderBlocks renderer) {
		return renderer.renderStandardBlock(block, x, y, z);
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
