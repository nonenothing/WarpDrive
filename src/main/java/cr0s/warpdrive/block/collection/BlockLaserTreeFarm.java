package cr0s.warpdrive.block.collection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import cr0s.warpdrive.data.EnumLaserTreeFarmMode;

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

public class BlockLaserTreeFarm extends BlockAbstractContainer {
	
	public static final PropertyEnum<EnumLaserTreeFarmMode> MODE = PropertyEnum.create("mode", EnumLaserTreeFarmMode.class);
	
	public BlockLaserTreeFarm(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.collection.laser_tree_farm");
		GameRegistry.registerTileEntity(TileEntityLaserTreeFarm.class, WarpDrive.PREFIX + registryName);

		setDefaultState(getDefaultState().withProperty(MODE, EnumLaserTreeFarmMode.INACTIVE));
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
				.withProperty(MODE, EnumLaserTreeFarmMode.get(metadata));
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(MODE).ordinal();
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityLaserTreeFarm();
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
			if (tileEntity instanceof TileEntityLaserTreeFarm) {
				Commons.addChatMessage(entityPlayer, ((TileEntityLaserTreeFarm) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}