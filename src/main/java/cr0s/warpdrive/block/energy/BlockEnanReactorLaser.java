package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumTier;
import cr0s.warpdrive.data.EnumValidPowered;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockEnanReactorLaser extends BlockAbstractContainer {
	
	public BlockEnanReactorLaser(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier, Material.IRON);
		
		setResistance(60.0F * 5 / 3);
		setUnlocalizedName("warpdrive.energy.enan_reactor_laser");
		registerTileEntity(TileEntityEnanReactorLaser.class, new ResourceLocation(WarpDrive.MODID, registryName));
		
		setDefaultState(getDefaultState()
				                .withProperty(BlockProperties.ACTIVE, false)
				                .withProperty(BlockProperties.FACING, EnumFacing.DOWN)
		               );
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockProperties.ACTIVE, BlockProperties.FACING);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				       .withProperty(BlockProperties.ACTIVE, (metadata & 0x8) != 0)
				       .withProperty(BlockProperties.FACING, EnumFacing.getFront(metadata & 0x7));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return (blockState.getValue(BlockProperties.ACTIVE) ? 0x8 : 0x0)
		       | (blockState.getValue(BlockProperties.FACING).getIndex());
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityEnanReactorLaser(enumTier);
	}
	
	@Override
	public void onBlockPlacedBy(final World world, final BlockPos blockPos, final IBlockState blockState,
	                            final EntityLivingBase entityLiving, final ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLiving, itemStack);
		world.setBlockState(blockPos, blockState
		                              .withProperty(BlockProperties.FACING, EnumFacing.NORTH)
		                              .withProperty(BlockProperties.VALID_POWERED, EnumValidPowered.INVALID));
	}
}