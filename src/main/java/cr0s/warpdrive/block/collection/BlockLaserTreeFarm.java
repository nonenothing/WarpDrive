package cr0s.warpdrive.block.collection;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;

public class BlockLaserTreeFarm extends BlockContainer {
	private IIcon[] iconBuffer;
	public final static int ICON_IDLE = 0;
	public final static int ICON_FARNINGLOWPOWER = 1;
	public final static int ICON_FARMINGPOWERED = 2;
	// 3 & 4 are reserved
	private final static int ICON_BOTTOM = 5;
	private final static int ICON_TOP = 6;
	
	public BlockLaserTreeFarm() {
		super(Material.rock);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		this.setBlockName("warpdrive.collection.LaserTreeFarm");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[16];
		iconBuffer[ICON_IDLE            ] = par1IconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_idle");
		iconBuffer[ICON_FARNINGLOWPOWER ] = par1IconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_farmingLowPower");
		iconBuffer[ICON_FARMINGPOWERED  ] = par1IconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_farmingPowered");
		iconBuffer[ICON_BOTTOM          ] = par1IconRegister.registerIcon("warpdrive:collection/laserTreeFarmBottom");
		iconBuffer[ICON_TOP             ] = par1IconRegister.registerIcon("warpdrive:collection/laserTreeFarmTop");
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		}
		if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		if (metadata < iconBuffer.length) {
			return iconBuffer[metadata];
		}
		return null;
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1, int i) {
		return new TileEntityLaserTreeFarm();
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	/**
	 * Returns the item to drop on destruction.
	 */
	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return Item.getItemFromBlock(this);
	}
}