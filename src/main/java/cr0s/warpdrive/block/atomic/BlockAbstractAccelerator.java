package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAbstractAccelerator extends Block {
	public final byte tier;
	
	public BlockAbstractAccelerator(final byte tier) {
		super(Material.iron);
		this.tier = tier;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1] / 5);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] / 6 * 5 / 3);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
	}
	
	@Override
	public boolean isOpaqueCube() {
		return true;
	}
	
	@Override
	public int getRenderBlockPass() {
		return 0;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return true;
	}
	
	@Override
	public int damageDropped(int metadata) {
		return 0;
	}
	
	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
		return false;
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
		WarpDrive.starMap.onBlockUpdated(world, x, y, z);
		super.breakBlock(world, x, y, z, block, metadata);
	}
}
