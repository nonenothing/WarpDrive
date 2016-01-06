package cr0s.warpdrive.block.collection;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;

public class BlockMiningLaser extends BlockContainer {
	private IIcon[] iconBuffer;
	public final static int ICON_IDLE = 0;
	public final static int ICON_MININGLOWPOWER = 1;
	public final static int ICON_MININGPOWERED = 2;
	public final static int ICON_SCANNINGLOWPOWER = 3;
	public final static int ICON_SCANNINGPOWERED = 4;
	private final static int ICON_BOTTOM = 5;
	private final static int ICON_TOP = 6;
	
	public BlockMiningLaser() {
		super(Material.rock);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		this.setBlockName("warpdrive.collection.MiningLaser");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[16];
		iconBuffer[ICON_IDLE            ] = par1IconRegister.registerIcon("warpdrive:collection/miningLaserSide_idle");
		iconBuffer[ICON_MININGLOWPOWER  ] = par1IconRegister.registerIcon("warpdrive:collection/miningLaserSide_miningLowPower");
		iconBuffer[ICON_MININGPOWERED   ] = par1IconRegister.registerIcon("warpdrive:collection/miningLaserSide_miningPowered");
		iconBuffer[ICON_SCANNINGLOWPOWER] = par1IconRegister.registerIcon("warpdrive:collection/miningLaserSide_scanningLowPower");
		iconBuffer[ICON_SCANNINGPOWERED ] = par1IconRegister.registerIcon("warpdrive:collection/miningLaserSide_scanningPowered");
		iconBuffer[ICON_BOTTOM          ] = par1IconRegister.registerIcon("warpdrive:collection/miningLaserBottom");
		iconBuffer[ICON_TOP             ] = par1IconRegister.registerIcon("warpdrive:collection/miningLaserTop");
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
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityMiningLaser();
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 1;
	}
	
	/**
	 * Returns the item to drop on destruction.
	 */
	@Override
	public Item getItemDropped(int par1, Random random, int par3) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityMiningLaser) {
				WarpDrive.addChatMessage(entityPlayer, ((TileEntityMiningLaser)tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}