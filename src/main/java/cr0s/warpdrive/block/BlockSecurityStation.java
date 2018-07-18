package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.TileEntitySecurityStation;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumTier;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSecurityStation extends BlockAbstractContainer {
	
	public BlockSecurityStation(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier, Material.IRON);
		
		setUnlocalizedName("warpdrive.machines.security_station");
		
		setDefaultState(getDefaultState());
		registerTileEntity(TileEntitySecurityStation.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntitySecurityStation(enumTier);
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
		if (!(tileEntity instanceof TileEntitySecurityStation)) {
			return false;
		}
		final TileEntitySecurityStation tileEntitySecurityStation = (TileEntitySecurityStation) tileEntity;
		
		if (itemStackHeld.isEmpty()) {
			if (entityPlayer.isSneaking()) {
				Commons.addChatMessage(entityPlayer, tileEntitySecurityStation.getStatus());
			} else {
				Commons.addChatMessage(entityPlayer, tileEntitySecurityStation.attachPlayer(entityPlayer));
			}
			return true;
		}
		
		return false;
	}
}
