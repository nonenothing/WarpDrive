package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.config.WarpDriveConfig;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockAbstractAccelerator extends BlockAbstractBase {
	public final byte tier;
	
	BlockAbstractAccelerator(final String registryName, final byte tier) {
		super(registryName, Material.IRON);
		this.tier = tier;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1] / 5);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] / 6 * 5 / 3);
	}
	
	@Override
	public int damageDropped(IBlockState blockState) {
		return 0;
	}
	
	@Override
	public boolean canCreatureSpawn(@Nonnull IBlockState blockState, @Nonnull IBlockAccess world, @Nonnull BlockPos blockPos, SpawnPlacementType type) {
		return false;
	}
}
