package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCloakingCore extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	public BlockCloakingCore() {
		super(Material.iron);
		setBlockName("warpdrive.detection.cloaking_core");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[2];
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:detection/cloaking_core-side_inactive");
		iconBuffer[1] = iconRegister.registerIcon("warpdrive:detection/cloaking_core-side_active");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata  = blockAccess.getBlockMetadata(x, y, z);
		if (metadata < iconBuffer.length) {
			return iconBuffer[metadata];
		}
		
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return iconBuffer[1];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityCloakingCore();
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return 3;
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
	                                final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityCloakingCore) {
			final TileEntityCloakingCore cloakingCore = (TileEntityCloakingCore)tileEntity;
			if (entityPlayer.getHeldItem() == null) {
				Commons.addChatMessage(entityPlayer, cloakingCore.getStatus());
				// + " isInvalid? " + te.isInvalid() + " Valid? " + te.isValid + " Cloaking? " + te.isCloaking + " Enabled? " + te.isEnabled
				return true;
			} else if (entityPlayer.getHeldItem().getItem() == Item.getItemFromBlock(Blocks.redstone_torch)) {
				cloakingCore.isEnabled = !cloakingCore.isEnabled;
				Commons.addChatMessage(entityPlayer, cloakingCore.getStatus());
				return true;
			// } else if (xxx) {// TODO if player has advanced tool
				// WarpDrive.addChatMessage(entityPlayer, cloakingCore.getStatus() + "\n" + cloakingCore.getEnergyStatus());
				// return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void breakBlock(final World world, final int x, final int y, final int z, final Block block, final int metadata) {
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		
		if (tileEntity instanceof TileEntityCloakingCore) {
			((TileEntityCloakingCore) tileEntity).isEnabled = false;
			((TileEntityCloakingCore) tileEntity).disableCloakingField();
		}
		
		super.breakBlock(world, x, y, z, block, metadata);
	}
}
