package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLaser extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	private static final int ICON_SIDE = 0;
	
	public BlockLaser() {
		super(Material.iron);
		setHardness(50.0F);
		setResistance(20.0F * 5 / 3);
		setBlockName("warpdrive.machines.laser");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[1];
		iconBuffer[ICON_SIDE] = iconRegister.registerIcon("warpdrive:laser-side");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return iconBuffer[ICON_SIDE];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityLaser();
	}
	
	@Override
	public int quantityDropped(final Random random) {
		return 1;
	}
	
	@Override
	public Item getItemDropped(final int metadata, final Random random, final int fortune) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityLaser) {
				Commons.addChatMessage(entityPlayer, ((TileEntityLaser) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}