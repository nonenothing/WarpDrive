package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.BlockAbstractContainer;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLift extends BlockAbstractContainer {

	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	public BlockLift() {
		super(Material.iron);
		setBlockName("warpdrive.movement.lift");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[6];
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:movement/liftSideOffline");
		iconBuffer[1] = iconRegister.registerIcon("warpdrive:movement/liftSideUp");
		iconBuffer[2] = iconRegister.registerIcon("warpdrive:movement/liftSideDown");
		iconBuffer[3] = iconRegister.registerIcon("warpdrive:movement/liftUpInactive");
		iconBuffer[4] = iconRegister.registerIcon("warpdrive:movement/liftUpOut");
		iconBuffer[5] = iconRegister.registerIcon("warpdrive:movement/liftUpIn");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata  = blockAccess.getBlockMetadata(x, y, z);
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
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		if (metadata > 2) {
			return iconBuffer[0];
		}
		if (side == 1) {
			return iconBuffer[3 + 1];
		} else if (side == 0) {
			if (metadata == 0) {
				return iconBuffer[3];
			} else {
				return iconBuffer[6 - 1];
			}
		}
		
		return iconBuffer[1];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityLift();
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	@Override
	public Item getItemDropped(final int metadata, final Random random, final int fortune) {
		return Item.getItemFromBlock(this);
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
			if (tileEntity instanceof TileEntityLift) {
				Commons.addChatMessage(entityPlayer, ((TileEntityLift) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}