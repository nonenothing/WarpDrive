package cr0s.warpdrive.block.building;

import cr0s.warpdrive.Commons;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockShipScanner extends BlockAbstractContainer {
	
	public static int passCurrent;
	
	public BlockShipScanner(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.building.ship_scanner");
		GameRegistry.registerTileEntity(TileEntityShipScanner.class, WarpDrive.PREFIX + registryName);
	}
	
/* @TODO camouflage	
	@Override
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		final TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityShipScanner && ((TileEntityShipScanner) tileEntity).blockCamouflage != null) {
			return ((TileEntityShipScanner) tileEntity).colorMultiplierCamouflage;
		}
		
		return super.colorMultiplier(blockAccess, x, y, z);
	}
	
	@Override
	public int getLightValue(final IBlockAccess blockAccess, final int x, final int y, final int z) {
		final TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityShipScanner) {
			return ((TileEntityShipScanner) tileEntity).lightCamouflage;
		}
		
		return 0;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	/**/

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityShipScanner();
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		if (itemStack == null || itemStack.getItem() != Item.getItemFromBlock(this)) {
			return 1;
		}
		return 0;
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
			if (tileEntity instanceof TileEntityShipScanner) {
				final BlockPos blockPosAbove = blockPos.add(0, 2, 0);
				final IBlockState blockStateAbove = world.getBlockState(blockPosAbove);
				if ( blockStateAbove.getBlock().isAir(blockStateAbove, world, blockPosAbove)
				  || !entityPlayer.isSneaking() ) {
					Commons.addChatMessage(entityPlayer, ((TileEntityShipScanner) tileEntity).getStatus());
					return true;
				} else if (blockStateAbove.getBlock() != this) {
					((TileEntityShipScanner) tileEntity).blockCamouflage = blockStateAbove.getBlock();
					((TileEntityShipScanner) tileEntity).metadataCamouflage = blockStateAbove.getBlock().getMetaFromState(blockStateAbove);
					((TileEntityShipScanner) tileEntity).colorMultiplierCamouflage = 0x808080; // blockAbove.colorMultiplier(world, x, y + 2, z);
					((TileEntityShipScanner) tileEntity).lightCamouflage = blockStateAbove.getLightValue(world, blockPosAbove);
					tileEntity.markDirty();
					// @TODO MC1.10 camouflage world.setBlockMetadataWithNotify(blockPos, ((TileEntityShipScanner) tileEntity).metadataCamouflage, 2);
				} else {
					((TileEntityShipScanner) tileEntity).blockCamouflage = null;
					((TileEntityShipScanner) tileEntity).metadataCamouflage = 0;
					((TileEntityShipScanner) tileEntity).colorMultiplierCamouflage = 0;
					((TileEntityShipScanner) tileEntity).lightCamouflage = 0;
					tileEntity.markDirty();
					// @TODO MC1.10 camouflage world.setBlockMetadataWithNotify(blockPos, ((TileEntityShipScanner) tileEntity).metadataCamouflage, 2);
				}
			}
		}
		
		return false;
	}
}