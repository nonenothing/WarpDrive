package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.block.BlockAbstractOmnipanel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAirShield extends BlockAbstractOmnipanel {
	
	public BlockAirShield(final String registryName) {
		super(registryName, Material.CLOTH);
		setUnlocalizedName("warpdrive.breathing.air_shield");
	}
	
	/* @TODO rendering
	@Override
	public int getRenderColor(final int metadata) {
		switch (metadata) {
		case 1:
			return 0xFF5A02;    // orange
		case 5:
			return 0x90E801;    // lime green
		case 6:
			return 0xFB0680;    // pink
		case 9:
		default:
			return 0x0FD7FF;    // SciFi cyan
		case 10:
			return 0x5D1072;    // purple
		}
	}
	/**/
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullyOpaque(IBlockState state) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull World world, @Nonnull BlockPos blockPos) {
		return null;
	}
	
	@Override
	public boolean canCollideCheck(IBlockState blockState, boolean hitIfLiquid) {
		return !hitIfLiquid;
	}
	
	@Override
	public boolean isCollidable() {
		return false;
	}
}