package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.BlockAbstractContainer;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
	private static final int ICON_SIDE_ACTIVATED = 3;
	private static final int ICON_SIDE_ACTIVATED_SCAN = 4;
	
	public BlockRadar() {
		super(Material.iron);
		setBlockName("warpdrive.detection.Radar");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[16];
		iconBuffer[ICON_SIDE_INACTIVE      ] = iconRegister.registerIcon("warpdrive:detection/radarSideInactive");
		iconBuffer[ICON_BOTTOM             ] = iconRegister.registerIcon("warpdrive:detection/radarBottom");
		iconBuffer[ICON_TOP                ] = iconRegister.registerIcon("warpdrive:detection/radarTop");
		iconBuffer[ICON_SIDE_ACTIVATED     ] = iconRegister.registerIcon("warpdrive:detection/radarSideActive");
		iconBuffer[ICON_SIDE_ACTIVATED_SCAN] = iconRegister.registerIcon("warpdrive:detection/radarSideActiveScan");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		int metadata  = blockAccess.getBlockMetadata(x, y, z);
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		} else if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		
		if (metadata == 0) {// Inactive state
			return iconBuffer[ICON_SIDE_INACTIVE];
		} else if (metadata == 1) { // Attached state
			return iconBuffer[ICON_SIDE_ACTIVATED];
		} else if (metadata == 2) { // Scanning state
			return iconBuffer[ICON_SIDE_ACTIVATED_SCAN];
		}
		
		return iconBuffer[ICON_SIDE_INACTIVE];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		} else if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		
		// return iconBuffer[ICON_SIDE_ACTIVATED];
		return iconBuffer[ICON_SIDE_ACTIVATED_SCAN];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityRadar();
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return 2;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityRadar) {
				Commons.addChatMessage(entityPlayer, ((TileEntityRadar) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}
