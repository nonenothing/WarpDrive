package cr0s.warpdrive.block.collection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import cr0s.warpdrive.data.EnumMiningLaserMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMiningLaser extends BlockAbstractContainer {
	
	public static final PropertyEnum<EnumMiningLaserMode> MODE = PropertyEnum.create("mode", EnumMiningLaserMode.class);
	
	public BlockMiningLaser(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.collection.mining_laser");
		GameRegistry.registerTileEntity(TileEntityMiningLaser.class, WarpDrive.PREFIX + registryName);

		setDefaultState(getDefaultState().withProperty(MODE, EnumMiningLaserMode.INACTIVE));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MODE);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				.withProperty(MODE, EnumMiningLaserMode.get(metadata));
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(MODE).ordinal();
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityMiningLaser();
	}
}