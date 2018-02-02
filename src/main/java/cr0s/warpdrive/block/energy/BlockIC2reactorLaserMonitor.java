package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumValidPowered;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockIC2reactorLaserMonitor extends BlockAbstractContainer {
	
	public BlockIC2reactorLaserMonitor(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.energy.IC2ReactorLaserMonitor");
		GameRegistry.registerTileEntity(TileEntityIC2reactorLaserMonitor.class, WarpDrive.PREFIX + registryName);
		
		setDefaultState(blockState.getBaseState()
		                .withProperty(BlockProperties.FACING, EnumFacing.DOWN)
		                .withProperty(BlockProperties.VALID_POWERED, EnumValidPowered.INVALID));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockProperties.FACING, BlockProperties.VALID_POWERED);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		final int facing = (metadata & 7) < 6 ? (metadata & 7) : 0;
		final EnumValidPowered enumValidPowered = EnumValidPowered.get(metadata - facing);
		return getDefaultState()
		       .withProperty(BlockProperties.FACING, EnumFacing.getFront(facing))
		       .withProperty(BlockProperties.VALID_POWERED, enumValidPowered != null ? enumValidPowered : EnumValidPowered.INVALID);
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(BlockProperties.FACING).getIndex()
		     + blockState.getValue(BlockProperties.VALID_POWERED).getIndex();
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world,final  int metadata) {
		return new TileEntityIC2reactorLaserMonitor();
	}
	
	@Override
	public void onBlockPlacedBy(final World world, final BlockPos blockPos, final IBlockState blockState,
	                            final EntityLivingBase entityLiving, final ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLiving, itemStack);
		world.setBlockState(blockPos, blockState
		                              .withProperty(BlockProperties.FACING, EnumFacing.NORTH)
		                              .withProperty(BlockProperties.VALID_POWERED, EnumValidPowered.INVALID));
	}
	
	@Override
	public boolean onBlockActivated(final World world, final BlockPos blockPos, final IBlockState blockState,
	                                final EntityPlayer entityPlayer, final EnumHand hand, @Nullable final ItemStack itemStackHeld,
	                                final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		if (itemStackHeld == null) {
			TileEntity tileEntity = world.getTileEntity(blockPos);
			if (tileEntity instanceof TileEntityIC2reactorLaserMonitor) {
				Commons.addChatMessage(entityPlayer, ((TileEntityIC2reactorLaserMonitor) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}
