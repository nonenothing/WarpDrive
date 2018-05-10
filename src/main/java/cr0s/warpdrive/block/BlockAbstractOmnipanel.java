package cr0s.warpdrive.block;

import cr0s.warpdrive.render.RenderBlockOmnipanel;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

public abstract class BlockAbstractOmnipanel extends BlockAbstractBase {
	
	public static final float CENTER_MIN = 7.0F / 16.0F;
	public static final float CENTER_MAX = 9.0F / 16.0F;
	
	public BlockAbstractOmnipanel(final Material material) {
		super(material);
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public int getRenderType() {
		return RenderBlockOmnipanel.renderId;
	}
	
	@Override
	public int getRenderBlockPass() {
		return 1;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		return blockAccess.getBlock(x, y, z) != this && super.shouldSideBeRendered(blockAccess, x, y, z, side);
	}
	
	@Override
	public void addCollisionBoxesToList(final World world, final int x, final int y, final int z, final AxisAlignedBB axisAlignedBB, final List list, final Entity entity) {
		// get direct connections
		final int maskConnectY_neg = getConnectionMask(world, x, y - 1, z, ForgeDirection.DOWN);
		final int maskConnectY_pos = getConnectionMask(world, x, y + 1, z, ForgeDirection.UP);
		final int maskConnectZ_neg = getConnectionMask(world, x, y, z - 1, ForgeDirection.NORTH);
		final int maskConnectZ_pos = getConnectionMask(world, x, y, z + 1, ForgeDirection.SOUTH);
		final int maskConnectX_neg = getConnectionMask(world, x - 1, y, z, ForgeDirection.WEST);
		final int maskConnectX_pos = getConnectionMask(world, x + 1, y, z, ForgeDirection.EAST);
		
		final boolean canConnectY_neg = maskConnectY_neg > 0;
		final boolean canConnectY_pos = maskConnectY_pos > 0;
		final boolean canConnectZ_neg = maskConnectZ_neg > 0;
		final boolean canConnectZ_pos = maskConnectZ_pos > 0;
		final boolean canConnectX_neg = maskConnectX_neg > 0;
		final boolean canConnectX_pos = maskConnectX_pos > 0;
		final boolean canConnectNone = !canConnectY_neg && !canConnectY_pos && !canConnectZ_neg && !canConnectZ_pos && !canConnectX_neg && !canConnectX_pos;
		
		// get diagonal connections
		final boolean canConnectXn_Y_neg = (maskConnectX_neg > 1 && maskConnectY_neg > 1) || getConnectionMask(world, x - 1, y - 1, z, ForgeDirection.DOWN ) > 0;
		final boolean canConnectXn_Y_pos = (maskConnectX_neg > 1 && maskConnectY_pos > 1) || getConnectionMask(world, x - 1, y + 1, z, ForgeDirection.UP   ) > 0;
		final boolean canConnectXn_Z_neg = (maskConnectX_neg > 1 && maskConnectZ_neg > 1) || getConnectionMask(world, x - 1, y, z - 1, ForgeDirection.NORTH) > 0;
		final boolean canConnectXn_Z_pos = (maskConnectX_neg > 1 && maskConnectZ_pos > 1) || getConnectionMask(world, x - 1, y, z + 1, ForgeDirection.SOUTH) > 0;
		final boolean canConnectZn_Y_neg = (maskConnectZ_neg > 1 && maskConnectY_neg > 1) || getConnectionMask(world, x, y - 1, z - 1, ForgeDirection.DOWN ) > 0;
		final boolean canConnectZn_Y_pos = (maskConnectZ_neg > 1 && maskConnectY_pos > 1) || getConnectionMask(world, x, y + 1, z - 1, ForgeDirection.UP   ) > 0;
		
		final boolean canConnectXp_Y_neg = (maskConnectX_pos > 1 && maskConnectY_neg > 1) || getConnectionMask(world, x + 1, y - 1, z, ForgeDirection.DOWN ) > 0;
		final boolean canConnectXp_Y_pos = (maskConnectX_pos > 1 && maskConnectY_pos > 1) || getConnectionMask(world, x + 1, y + 1, z, ForgeDirection.UP   ) > 0;
		final boolean canConnectXp_Z_neg = (maskConnectX_pos > 1 && maskConnectZ_neg > 1) || getConnectionMask(world, x + 1, y, z - 1, ForgeDirection.NORTH) > 0;
		final boolean canConnectXp_Z_pos = (maskConnectX_pos > 1 && maskConnectZ_pos > 1) || getConnectionMask(world, x + 1, y, z + 1, ForgeDirection.SOUTH) > 0;
		final boolean canConnectZp_Y_neg = (maskConnectZ_pos > 1 && maskConnectY_neg > 1) || getConnectionMask(world, x, y - 1, z + 1, ForgeDirection.DOWN ) > 0;
		final boolean canConnectZp_Y_pos = (maskConnectZ_pos > 1 && maskConnectY_pos > 1) || getConnectionMask(world, x, y + 1, z + 1, ForgeDirection.UP   ) > 0;
		
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
				setBlockBounds(0.0F, 0.0F, CENTER_MIN, CENTER_MAX, CENTER_MAX, CENTER_MAX);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
			
			if (hasXpYn) {
				setBlockBounds(CENTER_MIN, 0.0F, CENTER_MIN, 1.0F, CENTER_MAX, CENTER_MAX);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
			
			if (hasXnYp) {
				setBlockBounds(0.0F, CENTER_MIN, CENTER_MIN, CENTER_MAX, 1.0F, CENTER_MAX);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
			
			if (hasXpYp) {
				setBlockBounds(CENTER_MIN, CENTER_MIN, CENTER_MIN, 1.0F, 1.0F, CENTER_MAX);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
		}
		
		{// x plane
			if (hasZnYn) {
				setBlockBounds(CENTER_MIN, 0.0F, 0.0F, CENTER_MAX, CENTER_MAX, CENTER_MAX);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
			
			if (hasZpYn) {
				setBlockBounds(CENTER_MIN, 0.0F, CENTER_MIN, CENTER_MAX, CENTER_MAX, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
			
			if (hasZnYp) {
				setBlockBounds(CENTER_MIN, CENTER_MIN, 0.0F, CENTER_MAX, 1.0F, CENTER_MAX);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
			
			if (hasZpYp) {
				setBlockBounds(CENTER_MIN, CENTER_MIN, CENTER_MIN, CENTER_MAX, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
		}
		
		{// z plane
			if (hasXnZn) {
				setBlockBounds(0.0F, CENTER_MIN, 0.0F, CENTER_MAX, CENTER_MAX, CENTER_MAX);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
			
			if (hasXpZn) {
				setBlockBounds(CENTER_MIN, CENTER_MIN, 0.0F, 1.0F, CENTER_MAX, CENTER_MAX);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
			
			if (hasXnZp) {
				setBlockBounds(0.0F, CENTER_MIN, CENTER_MIN, CENTER_MAX, CENTER_MAX, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
			
			if (hasXpZp) {
				setBlockBounds(CENTER_MIN, CENTER_MIN, CENTER_MIN, 1.0F, CENTER_MAX, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
			}
		}
		
		// central nodes
		if (canConnectY_neg && !hasXnYn && !hasXpYn && !hasZnYn && !hasZpYn) {
			setBlockBounds(CENTER_MIN, 0.0F, CENTER_MIN, CENTER_MAX, CENTER_MAX, CENTER_MAX);
			super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
		}
		if (canConnectY_pos && !hasXnYp && !hasXpYp && !hasZnYp && !hasZpYp) {
			setBlockBounds(CENTER_MIN, CENTER_MIN, CENTER_MIN, CENTER_MAX, 1.0F, CENTER_MAX);
			super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
		}
		if (canConnectZ_neg && !hasXnZn && !hasXpZn && !hasZnYn && !hasZnYp) {
			setBlockBounds(CENTER_MIN, CENTER_MIN, 0.0F, CENTER_MAX, CENTER_MAX, CENTER_MAX);
			super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
		}
		if (canConnectZ_pos && !hasXnZp && !hasXpZp && !hasZpYn && !hasZpYp) {
			setBlockBounds(CENTER_MIN, CENTER_MIN, CENTER_MIN, CENTER_MAX, CENTER_MAX, 1.0F);
			super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
		}
		if (canConnectX_neg && !hasXnYn && !hasXnYp && !hasXnZn && !hasXnZp) {
			setBlockBounds(0.0F, CENTER_MIN, CENTER_MIN, CENTER_MAX, CENTER_MAX, CENTER_MAX);
			super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
		}
		if (canConnectX_pos && !hasXpYn && !hasXpYp && !hasXpZn && !hasXpZp) {
			setBlockBounds(CENTER_MIN, CENTER_MIN, CENTER_MIN, 1.0F, CENTER_MAX, CENTER_MAX);
			super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
		}
	}
	
	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	public void setBlockBoundsBasedOnState(final IBlockAccess blockAccess, final int x, final int y, final int z) {
		// get direct connections
		final int maskConnectY_neg = getConnectionMask(blockAccess, x, y - 1, z, ForgeDirection.DOWN);
		final int maskConnectY_pos = getConnectionMask(blockAccess, x, y + 1, z, ForgeDirection.UP);
		final int maskConnectZ_neg = getConnectionMask(blockAccess, x, y, z - 1, ForgeDirection.NORTH);
		final int maskConnectZ_pos = getConnectionMask(blockAccess, x, y, z + 1, ForgeDirection.SOUTH);
		final int maskConnectX_neg = getConnectionMask(blockAccess, x - 1, y, z, ForgeDirection.WEST);
		final int maskConnectX_pos = getConnectionMask(blockAccess, x + 1, y, z, ForgeDirection.EAST);
		
		final boolean canConnectY_neg = maskConnectY_neg > 0;
		final boolean canConnectY_pos = maskConnectY_pos > 0;
		final boolean canConnectZ_neg = maskConnectZ_neg > 0;
		final boolean canConnectZ_pos = maskConnectZ_pos > 0;
		final boolean canConnectX_neg = maskConnectX_neg > 0;
		final boolean canConnectX_pos = maskConnectX_pos > 0;
		final boolean canConnectNone = !canConnectY_neg && !canConnectY_pos && !canConnectZ_neg && !canConnectZ_pos && !canConnectX_neg && !canConnectX_pos;
		
		// x axis
		final float xMin = canConnectNone || canConnectX_neg ? 0.0F : CENTER_MIN;
		final float xMax = canConnectNone || canConnectX_pos ? 1.0F : CENTER_MAX;
		
		// y axis
		final float yMin = canConnectNone || canConnectY_neg ? 0.0F : CENTER_MIN;
		final float yMax = canConnectNone || canConnectY_pos ? 1.0F : CENTER_MAX;
		
		// z axis
		final float zMin = canConnectNone || canConnectZ_neg ? 0.0F : CENTER_MIN;
		final float zMax = canConnectNone || canConnectZ_pos ? 1.0F : CENTER_MAX;
		
		setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);
	}
	
	@Override
	protected boolean canSilkHarvest()
	{
		return true;
	}
	
	public int getConnectionMask(final IBlockAccess blockAccess, final int x, final int y, final int z, final ForgeDirection forgeDirection) {
		final Block block = blockAccess.getBlock(x, y, z);
		return (block.func_149730_j() || block == this || block.getMaterial() == Material.glass || block instanceof BlockPane ? 1 : 0)
		     + (block.isSideSolid(blockAccess, x, y, z, forgeDirection.getOpposite()) ? 2 : 0);
	}
}