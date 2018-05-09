package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.EnumTransporterState;

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

public class BlockTransporterCore extends BlockAbstractContainer {
	
	public static final PropertyEnum<EnumTransporterState> VARIANT = PropertyEnum.create("variant", EnumTransporterState.class);
	
	public BlockTransporterCore(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.movement.transporter_core");
		
		setDefaultState(getDefaultState()
				                .withProperty(VARIANT, EnumTransporterState.DISABLED)
		               );
		GameRegistry.registerTileEntity(TileEntityTransporterCore.class, WarpDrive.PREFIX + registryName);
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityTransporterCore();
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				       .withProperty(VARIANT, EnumTransporterState.get(metadata & 0x3));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(VARIANT).getMetadata();
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
			final TileEntity tileEntity = world.getTileEntity(blockPos);
			if (tileEntity instanceof TileEntityTransporterCore) {
				Commons.addChatMessage(entityPlayer, ((TileEntityTransporterCore) tileEntity).getStatus());
				return true;
			}
		}
		
		return super.onBlockActivated(world, blockPos, blockState, entityPlayer, hand, itemStackHeld, side, hitX, hitY, hitZ);
	}
}