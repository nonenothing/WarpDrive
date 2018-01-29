package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockIC2reactorLaserMonitor extends BlockAbstractContainer {
	static IIcon[] icons;
	
	public BlockIC2reactorLaserMonitor() {
		super(Material.iron);
		setBlockName("warpdrive.energy.IC2ReactorLaserMonitor");
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1, int i) {
		return new TileEntityIC2reactorLaserMonitor();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[3];
		icons[0] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-disconnected");
		icons[1] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-connected-invalid");
		icons[2] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-connected-valid");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		final TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityIC2reactorLaserMonitor)) {
			return icons[0];
		}
		
		if ((metadata & 0x7) == 6) {// "unknown" direction
			return icons[0];
		}
		if ((metadata & 8) == 0) {
			return icons[1];
		} else {
			return icons[2];
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 4) {
			return icons[1];
		} else {
			return icons[0];
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityIC2reactorLaserMonitor) {
				Commons.addChatMessage(entityPlayer, ((TileEntityIC2reactorLaserMonitor) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}
