package cr0s.warpdrive.block.movement;

import java.util.ArrayList;
import java.util.Random;

import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.item.ItemComponent;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

public class BlockShipCore extends BlockAbstractContainer {
	private IIcon[] iconBuffer;
	
	private static final int ICON_BOTTOM = 1;
	private static final int ICON_SIDE_INACTIVE = 0;
	private static final int ICON_TOP = 2;
	private static final int ICON_SIDE_ACTIVATED = 3;
	private static final int ICON_SIDE_HEATED = 4;
	
	public BlockShipCore() {
		super(Material.iron);
		setBlockName("warpdrive.movement.ShipCore");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[5];
		iconBuffer[ICON_SIDE_INACTIVE] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreSideInactive");
		iconBuffer[ICON_BOTTOM] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreBottom");
		iconBuffer[ICON_TOP] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreTop");
		iconBuffer[ICON_SIDE_ACTIVATED] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreSideActive");
		iconBuffer[ICON_SIDE_HEATED] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreSideHeated");
	}
	
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		int metadata  = world.getBlockMetadata(x, y, z);
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		} else if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		
		if (metadata == 0) { // Inactive state
			return iconBuffer[ICON_SIDE_INACTIVE];
		} else if (metadata == 1) { // Activated state
			return iconBuffer[ICON_SIDE_ACTIVATED];
		} else if (metadata == 2) { // Heated state
			return iconBuffer[ICON_SIDE_HEATED];
		}
		
		return null;
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		} else if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		
		// return iconBuffer[ICON_SIDE_ACTIVATED];
		return iconBuffer[ICON_SIDE_HEATED];
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1, int i) {
		return new TileEntityShipCore();
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityShipCore) {
			if (((TileEntityShipCore)tileEntity).jumpCount == 0) {
				return super.getDrops(world, x, y, z, metadata, fortune);
			}
		}
		// trigger explosion
		EntityTNTPrimed entityTNTPrimed = new EntityTNTPrimed(world, x + 0.5F, y + 0.5F, z + 0.5F, null);
		entityTNTPrimed.fuse = 10 + world.rand.nextInt(10);
		world.spawnEntityInWorld(entityTNTPrimed);
		
		// get a chance to get the drops
		ArrayList<ItemStack> itemStacks = new ArrayList<>();
		itemStacks.add(ItemComponent.getItemStackNoCache(EnumComponentType.CAPACITIVE_CRYSTAL, 1));
		if (fortune > 0 && world.rand.nextBoolean()) {
			itemStacks.add(ItemComponent.getItemStackNoCache(EnumComponentType.CAPACITIVE_CRYSTAL, 1));
		}
		if (fortune > 1 && world.rand.nextBoolean()) {
			itemStacks.add(ItemComponent.getItemStackNoCache(EnumComponentType.CAPACITIVE_CRYSTAL, 1));
		}
		if (fortune > 1 & world.rand.nextBoolean()) {
			itemStacks.add(ItemComponent.getItemStackNoCache(EnumComponentType.POWER_INTERFACE, 1));
		}
		return itemStacks;
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer entityPlayer, World world, int x, int y, int z) {
		boolean willBreak = true;
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityShipCore) {
			if (((TileEntityShipCore)tileEntity).jumpCount == 0) {
				willBreak = false;
			}
		}
		return (willBreak ? 0.02F : 1.0F) * super.getPlayerRelativeBlockHardness(entityPlayer, world, x, y, z);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityShipCore) {
				WarpDrive.addChatMessage(entityPlayer, ((TileEntityShipCore)tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}