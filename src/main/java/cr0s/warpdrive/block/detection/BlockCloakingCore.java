package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import cr0s.warpdrive.data.BlockProperties;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCloakingCore extends BlockAbstractContainer {
	
	public BlockCloakingCore(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.detection.cloaking_core");
		registerTileEntity(TileEntityCloakingCore.class, new ResourceLocation(WarpDrive.MODID, registryName));
		
		setDefaultState(getDefaultState().withProperty(BlockProperties.ACTIVE, false));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockProperties.ACTIVE);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				.withProperty(BlockProperties.ACTIVE, metadata != 0);
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(BlockProperties.ACTIVE) ? 1 : 0;
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityCloakingCore();
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return 3;
	}
	
	@Override
	public boolean onBlockActivated(final World world, final BlockPos blockPos, final IBlockState blockState,
	                                final EntityPlayer entityPlayer, final EnumHand enumHand,
	                                final EnumFacing enumFacing, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (enumHand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		// get context
		final ItemStack itemStackHeld = entityPlayer.getHeldItem(enumHand);
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (!(tileEntity instanceof TileEntityCloakingCore)) {
			return false;
		}
		
		final TileEntityCloakingCore cloakingCore = (TileEntityCloakingCore) tileEntity;
		if (itemStackHeld.isEmpty()) {
			Commons.addChatMessage(entityPlayer, cloakingCore.getStatus());
			// + " isInvalid? " + te.isInvalid() + " Valid? " + te.isValid + " Cloaking? " + te.isCloaking + " Enabled? " + te.isEnabled
			return true;
		} else if (itemStackHeld.getItem() == Item.getItemFromBlock(Blocks.REDSTONE_TORCH)) {
			cloakingCore.isEnabled = !cloakingCore.isEnabled;
			Commons.addChatMessage(entityPlayer, cloakingCore.getStatus());
			return true;
		// } else if (xxx) {// TODO if player has advanced tool
			// WarpDrive.addChatMessage(entityPlayer, cloakingCore.getStatus() + "\n" + cloakingCore.getEnergyStatus());
			// return true;
		}
		
		return false;
	}
	
	@Override
	public void breakBlock(final World world, @Nonnull final BlockPos blockPos, @Nonnull final IBlockState blockState) {
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		
		if (tileEntity instanceof TileEntityCloakingCore) {
			((TileEntityCloakingCore) tileEntity).isEnabled = false;
			((TileEntityCloakingCore) tileEntity).disableCloakingField();
		}
		
		super.breakBlock(world, blockPos, blockState);
	}
}
