package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockIC2reactorLaserMonitor extends BlockAbstractContainer {
	
	private static IIcon[] icons;
	private static final int ICON_DISCONNECTED = 0;
	private static final int ICON_HEAD_VALID   = 1;
	private static final int ICON_SIDE_VALID   = 2;
	private static final int ICON_HEAD_POWERED = 3;
	private static final int ICON_SIDE_POWERED = 4;
	
	public BlockIC2reactorLaserMonitor() {
		super(Material.iron);
		setBlockName("warpdrive.energy.ic2_reactor_laser_monitor");
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityIC2reactorLaserMonitor();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		icons = new IIcon[5];
		icons[ICON_DISCONNECTED] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-invalid");
		icons[ICON_HEAD_VALID  ] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-head-valid");
		icons[ICON_SIDE_VALID  ] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-side-valid");
		icons[ICON_HEAD_POWERED] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-head-powered");
		icons[ICON_SIDE_POWERED] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-side-powered");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		final TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityIC2reactorLaserMonitor)) {
			return icons[ICON_DISCONNECTED];
		}
		
		final int facing = metadata & 0x7;  
		if (facing == 6) {// "unknown" direction
			return icons[ICON_DISCONNECTED];
		}
		if ((metadata & 8) == 0) {
			return icons[facing == side ? ICON_HEAD_VALID : ICON_SIDE_VALID];
		} else {
			return icons[facing == side ? ICON_HEAD_POWERED : ICON_SIDE_POWERED];
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		if (side == 4) {
			return icons[1];
		} else {
			return icons[0];
		}
	}
	
	@Override
	public void onBlockPlacedBy(final World world, final int x, final int y, final int z,
	                            final EntityLivingBase entityLiving, final ItemStack itemStack) {
		super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);
		world.setBlockMetadataWithNotify(x, y, z, 6, 3);
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
	                                final EntityPlayer entityPlayer, final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityIC2reactorLaserMonitor) {
				Commons.addChatMessage(entityPlayer, ((TileEntityIC2reactorLaserMonitor) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}
