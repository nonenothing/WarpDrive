package cr0s.warpdrive.block.movement;

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

public class BlockLift extends BlockContainer
{
	private IIcon[] iconBuffer;

	public BlockLift() {
		super(Material.rock);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		this.setBlockName("warpdrive.movement.Lift");
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[6];
		iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:movement/liftSideOffline");
		iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:movement/liftSideUp");
		iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:movement/liftSideDown");
		iconBuffer[3] = par1IconRegister.registerIcon("warpdrive:movement/liftUpInactive");
		iconBuffer[4] = par1IconRegister.registerIcon("warpdrive:movement/liftUpOut");
		iconBuffer[5] = par1IconRegister.registerIcon("warpdrive:movement/liftUpIn");
	}

	@Override
	public IIcon getIcon(int side, int metadata) {
		if (metadata > 2) {
			return iconBuffer[0];
		}
		if (side == 1) {
			return iconBuffer[3 + metadata];
		} else if (side == 0) {
			if (metadata == 0) {
				return iconBuffer[3];
			} else {
				return iconBuffer[6 - metadata];
			}
		}

		return iconBuffer[metadata];
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int i) {
		return new TileEntityLift();
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
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityLift) {
				WarpDrive.addChatMessage(entityPlayer, ((TileEntityLift)tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}