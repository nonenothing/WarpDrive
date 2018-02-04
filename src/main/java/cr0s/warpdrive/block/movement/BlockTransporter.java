package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTransporter extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;

	public BlockTransporter() {
		super(Material.iron);
		setBlockName("warpdrive.movement.Transporter");
	}

	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityTransporter();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[3];
		// Solid textures
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:movement/transporterBottom");
		iconBuffer[1] = iconRegister.registerIcon("warpdrive:movement/transporterTop");
		iconBuffer[2] = iconRegister.registerIcon("warpdrive:movement/transporterSide");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0 || side == 1) {
			return iconBuffer[side];
		}

		return iconBuffer[2];
	}
	
	
	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityTransporter) {
				Commons.addChatMessage(entityPlayer, ((TileEntityTransporter) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}