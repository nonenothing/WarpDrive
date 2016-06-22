package cr0s.warpdrive.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cr0s.warpdrive.block.forcefield.TileEntityForceField;
import cr0s.warpdrive.data.ForceFieldSetup;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

// wrapper to native classes to renderId is non-zero so we don't render faces when player camera is inside the block
public class RenderBlockForceField implements ISimpleBlockRenderingHandler {
	public static int renderId = 0;
	public static RenderBlockForceField instance = new RenderBlockForceField();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		// this is not supposed to happen
		assert(false);
	}
	
	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityForceField)) {
			return false;
		}
		ForceFieldSetup forceFieldSetup = ((TileEntityForceField)tileEntity).getForceFieldSetup();
		if (forceFieldSetup == null) {
			return false;
		}
		
		int renderType = -1;
		Block blockCamouflage = forceFieldSetup.getCamouflageBlock();
		int metaCamouflage = forceFieldSetup.getCamouflageMetadata();
		if (blockCamouflage != null) {
			renderType = blockCamouflage.getRenderType();
		}
		
		if (renderType >= 0) {
			try {
				blockCamouflage.setBlockBoundsBasedOnState(renderer.blockAccess, x, y, z);
				renderer.setRenderBoundsFromBlock(blockCamouflage);
				
				switch (renderType) {
				case 0 :
					renderer.renderStandardBlock(blockCamouflage, x, y, z);
					break;
				case 1 :
					renderer.renderCrossedSquares(blockCamouflage, x, y, z);
					break;
				case 4 :
					renderer.renderBlockLiquid(blockCamouflage, x, y, z);
					break;
				case 5 :
					renderer.renderBlockRedstoneWire(blockCamouflage, x, y, z);
					break;
				case 6 :
					renderer.renderBlockCrops(blockCamouflage, x, y, z);
					break;
				case 7 :
					renderer.renderBlockDoor(blockCamouflage, x, y, z);
					break;
				case 12 :
					renderer.renderBlockLever(blockCamouflage, x, y, z);
					break;
				case 13 :
					renderer.renderBlockCactus(blockCamouflage, x, y, z);
					break;
				case 14 :
					renderer.renderBlockBed(blockCamouflage, x, y, z);
					break;
				case 16 :
					renderer.renderPistonBase(blockCamouflage, x, y, z, false);
					break;
				case 17 :
					renderer.renderPistonExtension(blockCamouflage, x, y, z, true);
					break;
				case 20 :
					renderer.renderBlockVine(blockCamouflage, x, y, z);
					break;
				case 23 :
					renderer.renderBlockLilyPad(blockCamouflage, x, y, z);
					break;
				case 29 :
					renderer.renderBlockTripWireSource(blockCamouflage, x, y, z);
					break;
				case 30 :
					renderer.renderBlockTripWire(blockCamouflage, x, y, z);
					break;
				case 31 :
					renderer.renderBlockLog(blockCamouflage, x, y, z);
					break;
				case 39 :
					renderer.renderBlockQuartz(blockCamouflage, x, y, z);
					break;
				default:
					return false;
				}
			} catch(Exception exception) {
				renderer.renderBlockAsItem(blockCamouflage, metaCamouflage, 1);
			}
			return true;
		}
		
		return renderer.renderStandardBlock(block, x, y, z);
	}
	
	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}
	
	@Override
	public int getRenderId() {
		return renderId;
	}
}
