package cr0s.warpdrive.block.weapon;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.block.TileEntityLaser;
import cr0s.warpdrive.render.ClientCameraHandler;

public class BlockLaserCamera extends BlockAbstractContainer {
	private IIcon[] iconBuffer;
	
	private final int ICON_SIDE = 0;
	
	public BlockLaserCamera() {
		super(Material.rock);
		setBlockName("warpdrive.weapon.LaserCamera");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[1];
		// Solid textures
		iconBuffer[ICON_SIDE] = par1IconRegister.registerIcon("warpdrive:weapon/laserCameraSide");
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		return iconBuffer[ICON_SIDE];
	}
	
	@Override
	public TileEntity createNewTileEntity(World parWorld, int i) {
		return new TileEntityLaser();
	}
	
	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return Item.getItemFromBlock(this);
	}
	
	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return false;
		}
		
		// Report status
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!ClientCameraHandler.isOverlayEnabled && tileEntity != null && tileEntity instanceof TileEntityLaser && (entityPlayer.getHeldItem() == null)) {
			WarpDrive.addChatMessage(entityPlayer, ((TileEntityLaser)tileEntity).getStatus());
			return true;
		}
		
		return false;
	}
}