package cr0s.warpdrive.render;


public class RenderBlockOmnipanel {
	
}

/* @TODO MC1.10 ISBRH
import cr0s.warpdrive.block.BlockAbstractOmnipanel;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public class RenderBlockOmnipanel implements ISimpleBlockRenderingHandler {
	
	public static int renderId = 0;
	public static RenderBlockOmnipanel instance = new RenderBlockOmnipanel();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		// not supposed to happen
	}
	
	@Override
	public boolean renderWorldBlock(IBlockAccess blockAccess, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		final Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(block.getMixedBrightnessForBlock(blockAccess, x, y, z));
		
		// apply coloring
		final int colorMultiplier = block.colorMultiplier(blockAccess, x, y, z);
		final float fRed   = (float) (colorMultiplier >> 16 & 255) / 255.0F;
		final float fGreen = (float) (colorMultiplier >> 8 & 255) / 255.0F;
		final float fBlue  = (float) (colorMultiplier & 255) / 255.0F;
		tessellator.setColorOpaque_F(fRed, fGreen, fBlue);
		
		// get icon
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		final IIcon icon = block.getIcon(0, metadata);
		
		// pre-compute coordinates
		final double dX_min = (double) x;
		final double dX_max = x + 1;
		final double dY_min = y + 0.0D;
		final double dY_max = y + 1.0D;
		final double dZ_min = (double) z;
		final double dZ_max = z + 1;
		final double dX_neg = x + BlockAbstractOmnipanel.CENTER_MIN;
		final double dX_pos = x + BlockAbstractOmnipanel.CENTER_MAX;
		final double dY_neg = y + BlockAbstractOmnipanel.CENTER_MIN;
		final double dY_pos = y + BlockAbstractOmnipanel.CENTER_MAX;
		final double dZ_neg = z + BlockAbstractOmnipanel.CENTER_MIN;
		final double dZ_pos = z + BlockAbstractOmnipanel.CENTER_MAX;
		
		final double dU_min = icon.getMinU();
		final double dU_neg = icon.getInterpolatedU(7.0D);
		final double dU_pos = icon.getInterpolatedU(9.0D);
		final double dU_max = icon.getMaxU();
		
		final double dV_min = icon.getMinV();
		final double dV_neg = icon.getInterpolatedV(7.0D);
		final double dV_pos = icon.getInterpolatedV(9.0D);
		final double dV_max = icon.getMaxV();
		
		// get direct connections
		BlockAbstractOmnipanel blockAbstractOmnipanel = (BlockAbstractOmnipanel) block;
		final int maskConnectY_neg = blockAbstractOmnipanel.getConnectionMask(blockAccess, x, y - 1, z, EnumFacing.DOWN);
		final int maskConnectY_pos = blockAbstractOmnipanel.getConnectionMask(blockAccess, x, y + 1, z, EnumFacing.UP);
		final int maskConnectZ_neg = blockAbstractOmnipanel.getConnectionMask(blockAccess, x, y, z - 1, EnumFacing.NORTH);
		final int maskConnectZ_pos = blockAbstractOmnipanel.getConnectionMask(blockAccess, x, y, z + 1, EnumFacing.SOUTH);
		final int maskConnectX_neg = blockAbstractOmnipanel.getConnectionMask(blockAccess, x - 1, y, z, EnumFacing.WEST);
		final int maskConnectX_pos = blockAbstractOmnipanel.getConnectionMask(blockAccess, x + 1, y, z, EnumFacing.EAST);
		
		final boolean canConnectY_neg = maskConnectY_neg > 0;
		final boolean canConnectY_pos = maskConnectY_pos > 0;
		final boolean canConnectZ_neg = maskConnectZ_neg > 0;
		final boolean canConnectZ_pos = maskConnectZ_pos > 0;
		final boolean canConnectX_neg = maskConnectX_neg > 0;
		final boolean canConnectX_pos = maskConnectX_pos > 0;
		final boolean canConnectNone = !canConnectY_neg && !canConnectY_pos && !canConnectZ_neg && !canConnectZ_pos && !canConnectX_neg && !canConnectX_pos;
		
		// get diagonal connections
		final boolean canConnectXn_Y_neg = (maskConnectX_neg > 1 && maskConnectY_neg > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x - 1, y - 1, z, EnumFacing.DOWN ) > 0;
		final boolean canConnectXn_Y_pos = (maskConnectX_neg > 1 && maskConnectY_pos > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x - 1, y + 1, z, EnumFacing.UP   ) > 0;
		final boolean canConnectXn_Z_neg = (maskConnectX_neg > 1 && maskConnectZ_neg > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x - 1, y, z - 1, EnumFacing.NORTH) > 0;
		final boolean canConnectXn_Z_pos = (maskConnectX_neg > 1 && maskConnectZ_pos > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x - 1, y, z + 1, EnumFacing.SOUTH) > 0;
		final boolean canConnectZn_Y_neg = (maskConnectZ_neg > 1 && maskConnectY_neg > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x, y - 1, z - 1, EnumFacing.DOWN ) > 0;
		final boolean canConnectZn_Y_pos = (maskConnectZ_neg > 1 && maskConnectY_pos > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x, y + 1, z - 1, EnumFacing.UP   ) > 0;
		
		final boolean canConnectXp_Y_neg = (maskConnectX_pos > 1 && maskConnectY_neg > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x + 1, y - 1, z, EnumFacing.DOWN ) > 0;
		final boolean canConnectXp_Y_pos = (maskConnectX_pos > 1 && maskConnectY_pos > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x + 1, y + 1, z, EnumFacing.UP   ) > 0;
		final boolean canConnectXp_Z_neg = (maskConnectX_pos > 1 && maskConnectZ_neg > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x + 1, y, z - 1, EnumFacing.NORTH) > 0;
		final boolean canConnectXp_Z_pos = (maskConnectX_pos > 1 && maskConnectZ_pos > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x + 1, y, z + 1, EnumFacing.SOUTH) > 0;
		final boolean canConnectZp_Y_neg = (maskConnectZ_pos > 1 && maskConnectY_neg > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x, y - 1, z + 1, EnumFacing.DOWN ) > 0;
		final boolean canConnectZp_Y_pos = (maskConnectZ_pos > 1 && maskConnectY_pos > 1) || blockAbstractOmnipanel.getConnectionMask(blockAccess, x, y + 1, z + 1, EnumFacing.UP   ) > 0;
		
		// get panels
		final boolean hasXnYn = canConnectNone || (canConnectX_neg && canConnectY_neg && canConnectXn_Y_neg);
		final boolean hasXpYn = canConnectNone || (canConnectX_pos && canConnectY_neg && canConnectXp_Y_neg);
		final boolean hasXnYp = canConnectNone || (canConnectX_neg && canConnectY_pos && canConnectXn_Y_pos);
		final boolean hasXpYp = canConnectNone || (canConnectX_pos && canConnectY_pos && canConnectXp_Y_pos);
		
		final boolean hasXnZn = canConnectNone || (canConnectX_neg && canConnectZ_neg && canConnectXn_Z_neg);
		final boolean hasXpZn = canConnectNone || (canConnectX_pos && canConnectZ_neg && canConnectXp_Z_neg);
		final boolean hasXnZp = canConnectNone || (canConnectX_neg && canConnectZ_pos && canConnectXn_Z_pos);
		final boolean hasXpZp = canConnectNone || (canConnectX_pos && canConnectZ_pos && canConnectXp_Z_pos);
		
		final boolean hasZnYn = canConnectNone || (canConnectZ_neg && canConnectY_neg && canConnectZn_Y_neg);
		final boolean hasZpYn = canConnectNone || (canConnectZ_pos && canConnectY_neg && canConnectZp_Y_neg);
		final boolean hasZnYp = canConnectNone || (canConnectZ_neg && canConnectY_pos && canConnectZn_Y_pos);
		final boolean hasZpYp = canConnectNone || (canConnectZ_pos && canConnectY_pos && canConnectZp_Y_pos);
		
		{// z plane
			if (hasXnYn) {
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				tessellator.addVertexWithUV(dX_neg, dY_min, dZ_neg, dU_neg, dV_max);
				tessellator.addVertexWithUV(dX_min, dY_min, dZ_neg, dU_min, dV_max);
				tessellator.addVertexWithUV(dX_min, dY_neg, dZ_neg, dU_min, dV_pos);
				
				tessellator.addVertexWithUV(dX_min, dY_neg, dZ_pos, dU_min, dV_pos);
				tessellator.addVertexWithUV(dX_min, dY_min, dZ_pos, dU_min, dV_max);
				tessellator.addVertexWithUV(dX_neg, dY_min, dZ_pos, dU_neg, dV_max);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_neg, dV_pos);
			} else {
				if (canConnectX_neg) {
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_neg, dV_neg);
					tessellator.addVertexWithUV(dX_min, dY_neg, dZ_pos, dU_min, dV_neg);
					tessellator.addVertexWithUV(dX_min, dY_neg, dZ_neg, dU_min, dV_pos);
				}
				if (canConnectY_neg) {
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
					tessellator.addVertexWithUV(dX_neg, dY_min, dZ_neg, dU_neg, dV_max);
					tessellator.addVertexWithUV(dX_neg, dY_min, dZ_pos, dU_pos, dV_max);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_pos, dV_pos);
				}
			}
			
			if (hasXpYn) {
				tessellator.addVertexWithUV(dX_max, dY_neg, dZ_neg, dU_max, dV_pos);
				tessellator.addVertexWithUV(dX_max, dY_min, dZ_neg, dU_max, dV_max);
				tessellator.addVertexWithUV(dX_pos, dY_min, dZ_neg, dU_pos, dV_max);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_pos, dV_pos);
				
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_pos);
				tessellator.addVertexWithUV(dX_pos, dY_min, dZ_pos, dU_pos, dV_max);
				tessellator.addVertexWithUV(dX_max, dY_min, dZ_pos, dU_max, dV_max);
				tessellator.addVertexWithUV(dX_max, dY_neg, dZ_pos, dU_max, dV_pos);
			} else {
				if (canConnectX_pos) {
					tessellator.addVertexWithUV(dX_max, dY_neg, dZ_neg, dU_max, dV_pos);
					tessellator.addVertexWithUV(dX_max, dY_neg, dZ_pos, dU_max, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_pos, dV_pos);
				}
				if (canConnectY_neg) {
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_pos);
					tessellator.addVertexWithUV(dX_pos, dY_min, dZ_pos, dU_pos, dV_max);
					tessellator.addVertexWithUV(dX_pos, dY_min, dZ_neg, dU_neg, dV_max);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_neg, dV_pos);
				}
			}
			
			if (hasXnYp) {
				tessellator.addVertexWithUV(dX_neg, dY_max, dZ_neg, dU_neg, dV_min);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_min, dY_pos, dZ_neg, dU_min, dV_neg);
				tessellator.addVertexWithUV(dX_min, dY_max, dZ_neg, dU_min, dV_min);
				
				tessellator.addVertexWithUV(dX_min, dY_max, dZ_pos, dU_min, dV_min);
				tessellator.addVertexWithUV(dX_min, dY_pos, dZ_pos, dU_min, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_max, dZ_pos, dU_neg, dV_min);
			} else {
				if (canConnectX_neg) {
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_neg, dV_neg);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_pos);
					tessellator.addVertexWithUV(dX_min, dY_pos, dZ_neg, dU_min, dV_pos);
					tessellator.addVertexWithUV(dX_min, dY_pos, dZ_pos, dU_min, dV_neg);
				}
				if (canConnectY_pos) {
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_pos, dV_neg);
					tessellator.addVertexWithUV(dX_neg, dY_max, dZ_pos, dU_pos, dV_min);
					tessellator.addVertexWithUV(dX_neg, dY_max, dZ_neg, dU_neg, dV_min);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_neg);
				}
			}
			
			if (hasXpYp) {
				tessellator.addVertexWithUV(dX_max, dY_max, dZ_neg, dU_max, dV_min);
				tessellator.addVertexWithUV(dX_max, dY_pos, dZ_neg, dU_max, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_max, dZ_neg, dU_pos, dV_min);
				
				tessellator.addVertexWithUV(dX_pos, dY_max, dZ_pos, dU_pos, dV_min);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_max, dY_pos, dZ_pos, dU_max, dV_neg);
				tessellator.addVertexWithUV(dX_max, dY_max, dZ_pos, dU_max, dV_min);
			} else {
				if (canConnectX_pos) {
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_pos, dV_pos);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
					tessellator.addVertexWithUV(dX_max, dY_pos, dZ_pos, dU_max, dV_neg);
					tessellator.addVertexWithUV(dX_max, dY_pos, dZ_neg, dU_max, dV_pos);
				}
				if (canConnectY_pos) {
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_neg, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_max, dZ_neg, dU_neg, dV_min);
					tessellator.addVertexWithUV(dX_pos, dY_max, dZ_pos, dU_pos, dV_min);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
				}
			}
		}
		
		{// x plane
			if (hasZnYn) {
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_min, dU_min, dV_pos);
				tessellator.addVertexWithUV(dX_neg, dY_min, dZ_min, dU_min, dV_max);
				tessellator.addVertexWithUV(dX_neg, dY_min, dZ_neg, dU_neg, dV_max);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_neg, dV_pos);
				tessellator.addVertexWithUV(dX_pos, dY_min, dZ_neg, dU_neg, dV_max);
				tessellator.addVertexWithUV(dX_pos, dY_min, dZ_min, dU_min, dV_max);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_min, dU_min, dV_pos);
			} else {
				if (canConnectZ_neg) {
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_min, dU_neg, dV_max);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_min, dU_pos, dV_max);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_pos, dV_pos);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				}
				if (canConnectY_neg) {
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_pos, dV_pos);
					tessellator.addVertexWithUV(dX_pos, dY_min, dZ_neg, dU_pos, dV_max);
					tessellator.addVertexWithUV(dX_neg, dY_min, dZ_neg, dU_neg, dV_max);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				}
			}
			
			if (hasZpYn) {
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_pos, dV_pos);
				tessellator.addVertexWithUV(dX_neg, dY_min, dZ_pos, dU_pos, dV_max);
				tessellator.addVertexWithUV(dX_neg, dY_min, dZ_max, dU_max, dV_max);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_max, dU_max, dV_pos);
				
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_max, dU_max, dV_pos);
				tessellator.addVertexWithUV(dX_pos, dY_min, dZ_max, dU_max, dV_max);
				tessellator.addVertexWithUV(dX_pos, dY_min, dZ_pos, dU_pos, dV_max);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_pos);
			} else {
				if (canConnectZ_pos) {
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_neg, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_max, dU_pos, dV_min);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_max, dU_neg, dV_min);
				}
				if (canConnectY_neg) {
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_neg, dV_pos);
					tessellator.addVertexWithUV(dX_neg, dY_min, dZ_pos, dU_neg, dV_max);
					tessellator.addVertexWithUV(dX_pos, dY_min, dZ_pos, dU_pos, dV_max);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_pos);
				}
			}
			
			if (hasZnYp) {
				tessellator.addVertexWithUV(dX_neg, dY_max, dZ_min, dU_min, dV_min);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_min, dU_min, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_max, dZ_neg, dU_neg, dV_min);
				
				tessellator.addVertexWithUV(dX_pos, dY_max, dZ_neg, dU_neg, dV_min);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_min, dU_min, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_max, dZ_min, dU_min, dV_min);
			} else {
				if (canConnectZ_neg) {
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_min, dU_pos, dV_max);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_min, dU_neg, dV_max);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_pos);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_pos, dV_pos);
				}
				if (canConnectY_pos) {
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_neg);
					tessellator.addVertexWithUV(dX_neg, dY_max, dZ_neg, dU_neg, dV_min);
					tessellator.addVertexWithUV(dX_pos, dY_max, dZ_neg, dU_pos, dV_min);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_pos, dV_neg);
				}
			}
			
			if (hasZpYp) {
				tessellator.addVertexWithUV(dX_neg, dY_max, dZ_pos, dU_pos, dV_min);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_max, dU_max, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_max, dZ_max, dU_max, dV_min);
				
				tessellator.addVertexWithUV(dX_pos, dY_max, dZ_max, dU_max, dV_min);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_max, dU_max, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_max, dZ_pos, dU_pos, dV_min);
			} else {
				if (canConnectZ_pos) {
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_max, dU_neg, dV_min);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_max, dU_pos, dV_min);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_neg, dV_neg);
				}
				if (canConnectY_pos) {
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_max, dZ_pos, dU_pos, dV_min);
					tessellator.addVertexWithUV(dX_neg, dY_max, dZ_pos, dU_neg, dV_min);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_neg, dV_neg);
				}
			}
		}
		
		{// z plane
			if (hasXnZn) {
				tessellator.addVertexWithUV(dX_min, dY_neg, dZ_neg, dU_min, dV_pos);
				tessellator.addVertexWithUV(dX_min, dY_neg, dZ_min, dU_min, dV_max);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_min, dU_neg, dV_max);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_pos);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_min, dU_neg, dV_max);
				tessellator.addVertexWithUV(dX_min, dY_pos, dZ_min, dU_min, dV_max);
				tessellator.addVertexWithUV(dX_min, dY_pos, dZ_neg, dU_min, dV_pos);
			} else {
				if (canConnectX_neg) {
					tessellator.addVertexWithUV(dX_min, dY_neg, dZ_neg, dU_min, dV_pos);
					tessellator.addVertexWithUV(dX_min, dY_pos, dZ_neg, dU_min, dV_neg);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_neg);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				}
				if (canConnectZ_neg) {
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_neg);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_min, dU_min, dV_neg);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_min, dU_min, dV_pos);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				}
			}
			
			if (hasXpZn) {
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_pos, dV_pos);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_min, dU_pos, dV_max);
				tessellator.addVertexWithUV(dX_max, dY_neg, dZ_min, dU_max, dV_max);
				tessellator.addVertexWithUV(dX_max, dY_neg, dZ_neg, dU_max, dV_pos);
				
				tessellator.addVertexWithUV(dX_max, dY_pos, dZ_neg, dU_max, dV_pos);
				tessellator.addVertexWithUV(dX_max, dY_pos, dZ_min, dU_max, dV_max);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_min, dU_pos, dV_max);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_pos, dV_pos);
			} else {
				if (canConnectX_pos) {
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_pos, dV_pos);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_pos, dV_neg);
					tessellator.addVertexWithUV(dX_max, dY_pos, dZ_neg, dU_max, dV_neg);
					tessellator.addVertexWithUV(dX_max, dY_neg, dZ_neg, dU_max, dV_pos);
				}
				if (canConnectZ_neg) {
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_neg, dV_pos);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_min, dU_min, dV_pos);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_min, dU_min, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_neg, dV_neg);
				}
			}
			
			if (hasXnZp) {
				tessellator.addVertexWithUV(dX_min, dY_neg, dZ_max, dU_min, dV_min);
				tessellator.addVertexWithUV(dX_min, dY_neg, dZ_pos, dU_min, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_max, dU_neg, dV_min);
				
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_max, dU_neg, dV_min);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_min, dY_pos, dZ_pos, dU_min, dV_neg);
				tessellator.addVertexWithUV(dX_min, dY_pos, dZ_max, dU_min, dV_min);
			} else {
				if (canConnectX_neg) {
					tessellator.addVertexWithUV(dX_min, dY_pos, dZ_pos, dU_min, dV_neg);
					tessellator.addVertexWithUV(dX_min, dY_neg, dZ_pos, dU_min, dV_pos);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_neg, dV_pos);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_neg, dV_neg);
				}
				if (canConnectZ_pos) {
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_pos, dV_pos);
					tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_max, dU_max, dV_pos);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_max, dU_max, dV_neg);
					tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_pos, dV_neg);
				}
			}
			
			if (hasXpZp) {
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_max, dU_pos, dV_min);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_max, dY_neg, dZ_pos, dU_max, dV_neg);
				tessellator.addVertexWithUV(dX_max, dY_neg, dZ_max, dU_max, dV_min);
				
				tessellator.addVertexWithUV(dX_max, dY_pos, dZ_max, dU_max, dV_min);
				tessellator.addVertexWithUV(dX_max, dY_pos, dZ_pos, dU_max, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_max, dU_pos, dV_min);
			} else {
				if (canConnectX_pos) {
					tessellator.addVertexWithUV(dX_max, dY_neg, dZ_pos, dU_max, dV_pos);
					tessellator.addVertexWithUV(dX_max, dY_pos, dZ_pos, dU_max, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_pos);
				}
				if (canConnectZ_pos) {
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_max, dU_max, dV_neg);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_max, dU_max, dV_pos);
					tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_pos);
				}
			}
		}
		
		if (canConnectNone) {
			// x min
			tessellator.addVertexWithUV(dX_min, dY_max, dZ_neg, dU_neg, dV_min);
			tessellator.addVertexWithUV(dX_min, dY_min, dZ_neg, dU_neg, dV_max);
			tessellator.addVertexWithUV(dX_min, dY_min, dZ_pos, dU_pos, dV_max);
			tessellator.addVertexWithUV(dX_min, dY_max, dZ_pos, dU_pos, dV_min);
			
			tessellator.addVertexWithUV(dX_min, dY_pos, dZ_max, dU_max, dV_neg);
			tessellator.addVertexWithUV(dX_min, dY_pos, dZ_pos, dU_pos, dV_neg);
			tessellator.addVertexWithUV(dX_min, dY_neg, dZ_pos, dU_pos, dV_pos);
			tessellator.addVertexWithUV(dX_min, dY_neg, dZ_max, dU_max, dV_pos);
			
			tessellator.addVertexWithUV(dX_min, dY_pos, dZ_neg, dU_neg, dV_neg);
			tessellator.addVertexWithUV(dX_min, dY_pos, dZ_min, dU_min, dV_neg);
			tessellator.addVertexWithUV(dX_min, dY_neg, dZ_min, dU_min, dV_pos);
			tessellator.addVertexWithUV(dX_min, dY_neg, dZ_neg, dU_neg, dV_pos);
			
			// x max
			tessellator.addVertexWithUV(dX_max, dY_max, dZ_pos, dU_pos, dV_min);
			tessellator.addVertexWithUV(dX_max, dY_min, dZ_pos, dU_pos, dV_max);
			tessellator.addVertexWithUV(dX_max, dY_min, dZ_neg, dU_neg, dV_max);
			tessellator.addVertexWithUV(dX_max, dY_max, dZ_neg, dU_neg, dV_min);
			
			tessellator.addVertexWithUV(dX_max, dY_neg, dZ_max, dU_max, dV_pos);
			tessellator.addVertexWithUV(dX_max, dY_neg, dZ_pos, dU_pos, dV_pos);
			tessellator.addVertexWithUV(dX_max, dY_pos, dZ_pos, dU_pos, dV_neg);
			tessellator.addVertexWithUV(dX_max, dY_pos, dZ_max, dU_max, dV_neg);
			
			tessellator.addVertexWithUV(dX_max, dY_neg, dZ_neg, dU_neg, dV_pos);
			tessellator.addVertexWithUV(dX_max, dY_neg, dZ_min, dU_min, dV_pos);
			tessellator.addVertexWithUV(dX_max, dY_pos, dZ_min, dU_min, dV_neg);
			tessellator.addVertexWithUV(dX_max, dY_pos, dZ_neg, dU_neg, dV_neg);
			
			// z min
			tessellator.addVertexWithUV(dX_pos, dY_max, dZ_min, dU_pos, dV_min);
			tessellator.addVertexWithUV(dX_pos, dY_min, dZ_min, dU_pos, dV_max);
			tessellator.addVertexWithUV(dX_neg, dY_min, dZ_min, dU_neg, dV_max);
			tessellator.addVertexWithUV(dX_neg, dY_max, dZ_min, dU_neg, dV_min);
			
			tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_min, dU_neg, dV_neg);
			tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_min, dU_neg, dV_pos);
			tessellator.addVertexWithUV(dX_min, dY_neg, dZ_min, dU_min, dV_pos);
			tessellator.addVertexWithUV(dX_min, dY_pos, dZ_min, dU_min, dV_neg);
			
			tessellator.addVertexWithUV(dX_max, dY_pos, dZ_min, dU_max, dV_neg);
			tessellator.addVertexWithUV(dX_max, dY_neg, dZ_min, dU_max, dV_pos);
			tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_min, dU_pos, dV_pos);
			tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_min, dU_pos, dV_neg);
			
			// z max
			tessellator.addVertexWithUV(dX_neg, dY_max, dZ_max, dU_neg, dV_min);
			tessellator.addVertexWithUV(dX_neg, dY_min, dZ_max, dU_neg, dV_max);
			tessellator.addVertexWithUV(dX_pos, dY_min, dZ_max, dU_pos, dV_max);
			tessellator.addVertexWithUV(dX_pos, dY_max, dZ_max, dU_pos, dV_min);
			
			tessellator.addVertexWithUV(dX_min, dY_pos, dZ_max, dU_min, dV_neg);
			tessellator.addVertexWithUV(dX_min, dY_neg, dZ_max, dU_min, dV_pos);
			tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_max, dU_neg, dV_pos);
			tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_max, dU_neg, dV_neg);
			
			tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_max, dU_pos, dV_neg);
			tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_max, dU_pos, dV_pos);
			tessellator.addVertexWithUV(dX_max, dY_neg, dZ_max, dU_max, dV_pos);
			tessellator.addVertexWithUV(dX_max, dY_pos, dZ_max, dU_max, dV_neg);
			
			// y min
			tessellator.addVertexWithUV(dX_neg, dY_min, dZ_max, dU_neg, dV_min);
			tessellator.addVertexWithUV(dX_neg, dY_min, dZ_min, dU_neg, dV_max);
			tessellator.addVertexWithUV(dX_pos, dY_min, dZ_min, dU_pos, dV_max);
			tessellator.addVertexWithUV(dX_pos, dY_min, dZ_max, dU_pos, dV_min);
			
			tessellator.addVertexWithUV(dX_min, dY_min, dZ_pos, dU_min, dV_neg);
			tessellator.addVertexWithUV(dX_min, dY_min, dZ_neg, dU_min, dV_pos);
			tessellator.addVertexWithUV(dX_neg, dY_min, dZ_neg, dU_neg, dV_pos);
			tessellator.addVertexWithUV(dX_neg, dY_min, dZ_pos, dU_neg, dV_neg);
			
			tessellator.addVertexWithUV(dX_pos, dY_min, dZ_pos, dU_pos, dV_neg);
			tessellator.addVertexWithUV(dX_pos, dY_min, dZ_neg, dU_pos, dV_pos);
			tessellator.addVertexWithUV(dX_max, dY_min, dZ_neg, dU_max, dV_pos);
			tessellator.addVertexWithUV(dX_max, dY_min, dZ_pos, dU_max, dV_neg);
			
			// y max
			tessellator.addVertexWithUV(dX_pos, dY_max, dZ_max, dU_pos, dV_min);
			tessellator.addVertexWithUV(dX_pos, dY_max, dZ_min, dU_pos, dV_max);
			tessellator.addVertexWithUV(dX_neg, dY_max, dZ_min, dU_neg, dV_max);
			tessellator.addVertexWithUV(dX_neg, dY_max, dZ_max, dU_neg, dV_min);
			
			tessellator.addVertexWithUV(dX_neg, dY_max, dZ_pos, dU_neg, dV_neg);
			tessellator.addVertexWithUV(dX_neg, dY_max, dZ_neg, dU_neg, dV_pos);
			tessellator.addVertexWithUV(dX_min, dY_max, dZ_neg, dU_min, dV_pos);
			tessellator.addVertexWithUV(dX_min, dY_max, dZ_pos, dU_min, dV_neg);
			
			tessellator.addVertexWithUV(dX_max, dY_max, dZ_pos, dU_max, dV_neg);
			tessellator.addVertexWithUV(dX_max, dY_max, dZ_neg, dU_max, dV_pos);
			tessellator.addVertexWithUV(dX_pos, dY_max, dZ_neg, dU_pos, dV_pos);
			tessellator.addVertexWithUV(dX_pos, dY_max, dZ_pos, dU_pos, dV_neg);
		} else {
			
			// center cube
			if (!canConnectY_neg) {
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_pos, dV_pos);
			}
			if (!canConnectY_pos) {
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_pos, dV_pos);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_pos);
			}
			if (!canConnectZ_neg) {
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_pos, dV_pos);
			}
			if (!canConnectZ_pos) {
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_pos, dV_pos);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_neg, dV_pos);
			}
			if (!canConnectX_neg) {
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_neg, dU_neg, dV_pos);
				tessellator.addVertexWithUV(dX_neg, dY_neg, dZ_pos, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_neg, dY_pos, dZ_neg, dU_pos, dV_pos);
			}
			if (!canConnectX_pos) {
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_neg, dU_pos, dV_pos);
				tessellator.addVertexWithUV(dX_pos, dY_pos, dZ_pos, dU_pos, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_pos, dU_neg, dV_neg);
				tessellator.addVertexWithUV(dX_pos, dY_neg, dZ_neg, dU_neg, dV_pos);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}
	
	@Override
	public int getRenderId() {
		return renderId;
	}
}
/**/