package cr0s.warpdrive.block.weapon;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockWeaponController extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	private static final int ICON_TOP = 0;
	private static final int ICON_BOTTOM = 1;
	private static final int ICON_SIDE = 2;
	
	public BlockWeaponController() {
		super(Material.iron);
		setHardness(50.0F);
		setResistance(20.0F * 5 / 3);
		setBlockName("warpdrive.weapon.WeaponController");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[3];
		iconBuffer[ICON_TOP] = iconRegister.registerIcon("warpdrive:movement/ship_controller-top");
		iconBuffer[ICON_BOTTOM] = iconRegister.registerIcon("warpdrive:movement/ship_controller-bottom");
		iconBuffer[ICON_SIDE] = iconRegister.registerIcon("warpdrive:weapon/weapon_controller-side");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		} else if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		return iconBuffer[ICON_SIDE];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityWeaponController();
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
	                                final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityWeaponController) {
				Commons.addChatMessage(entityPlayer, ((TileEntityWeaponController) tileEntity).getStatus());
			} else {
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
						getLocalizedName()) + StatCollector.translateToLocalFormatted("warpdrive.error.bad_tile_entity"));
				WarpDrive.logger.error("Block " + this + " with invalid tile entity " + tileEntity);
			}
			return true;
		}
		
		return false;
	}
}