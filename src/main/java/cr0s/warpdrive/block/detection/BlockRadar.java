package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRadar extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	private static final int ICON_SIDE_INACTIVE = 0;
	private static final int ICON_BOTTOM = 1;
	private static final int ICON_TOP = 2;
	private static final int ICON_SIDE_ACTIVATE = 3;
	private static final int ICON_SIDE_SCANNING = 4;
	
	public BlockRadar() {
		super(Material.iron);
		setBlockName("warpdrive.detection.radar");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[16];
		iconBuffer[ICON_SIDE_INACTIVE] = iconRegister.registerIcon("warpdrive:detection/radar-side_inactive");
		iconBuffer[ICON_BOTTOM       ] = iconRegister.registerIcon("warpdrive:detection/radar-bottom");
		iconBuffer[ICON_TOP          ] = iconRegister.registerIcon("warpdrive:detection/radar-top");
		iconBuffer[ICON_SIDE_ACTIVATE] = iconRegister.registerIcon("warpdrive:detection/radar-side_active");
		iconBuffer[ICON_SIDE_SCANNING] = iconRegister.registerIcon("warpdrive:detection/radar-side_scanning");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata  = blockAccess.getBlockMetadata(x, y, z);
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		} else if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		
		if (metadata == 0) {// Inactive state
			return iconBuffer[ICON_SIDE_INACTIVE];
		} else if (metadata == 1) { // Attached state
			return iconBuffer[ICON_SIDE_ACTIVATE];
		} else if (metadata == 2) { // Scanning state
			return iconBuffer[ICON_SIDE_SCANNING];
		}
		
		return iconBuffer[ICON_SIDE_INACTIVE];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		} else if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		
		return iconBuffer[ICON_SIDE_SCANNING];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityRadar();
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return 2;
	}
}
