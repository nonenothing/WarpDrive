package cr0s.warpdrive.block.hull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;

public class BlockHullGlass extends BlockColored {
	public BlockHullGlass(final int tier) {
		super(Material.glass);
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] * 5 / 3);
		setStepSound(Block.soundTypeGlass);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.hull" + tier + ".glass.");
		setBlockTextureName("warpdrive:hull/glass");
	}
	
	@Override
	public int getMobilityFlag() {
		return 2;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public int getRenderBlockPass() {
		return 1;
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		Block sideBlock = world.getBlock(x, y, z);
		if (sideBlock == this) {
			return false;
		}
		
		return world.isAirBlock(x, y, z);
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}
