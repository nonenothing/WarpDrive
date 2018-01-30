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
	private static final int ICON_HEAD_CONNECTED = 1;
	private static final int ICON_SIDE_CONNECTED = 2;
	private static final int ICON_HEAD_VALID = 3;
	private static final int ICON_SIDE_VALID = 4;
	
	
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
		icons = new IIcon[5];
		icons[ICON_DISCONNECTED  ] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-disconnected");
		icons[ICON_HEAD_CONNECTED] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-head-connected");
		icons[ICON_SIDE_CONNECTED] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-side-connected");
		icons[ICON_HEAD_VALID    ] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-head-valid");
		icons[ICON_SIDE_VALID    ] = iconRegister.registerIcon("warpdrive:energy/ic2_reactor_laser_cooler-side-valid");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
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
			return icons[facing == side ? ICON_HEAD_CONNECTED : ICON_SIDE_CONNECTED];
		} else {
			return icons[facing == side ? ICON_HEAD_VALID : ICON_SIDE_VALID];
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
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack) {
		super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);
		world.setBlockMetadataWithNotify(x, y, z, 6, 3);
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
