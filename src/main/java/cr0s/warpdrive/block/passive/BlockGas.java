package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.data.CelestialObjectManager;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGas extends BlockAbstractBase {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;

	public BlockGas() {
		super(Material.fire);
		setHardness(0.0F);
		setBlockName("warpdrive.decoration.gas");
	}
	
	@Override
	public void getSubBlocks(final Item item, final CreativeTabs creativeTabs, final List list) {
		for (int index = 0; index < 12; index++) {
			list.add(new ItemStack(item, 1, index));
		}
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isAir(final IBlockAccess blockAccess, final int x, final int y, final int z) {
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(final World world, final int x, final int y, final int z) {
		return null;
	}

	@Override
	public boolean isReplaceable(final IBlockAccess blockAccess, final int x, final int y, final int z) {
		return true;
	}

	@Override
	public boolean canPlaceBlockAt(final World world, final int x, final int y, final int z) {
		return true;
	}

	@Override
	public boolean canCollideCheck(final int metadata, final boolean hitIfLiquid) {
		return false;
	}

	@Override
	public int getRenderBlockPass() {
		return 1; // transparency enabled
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[12];
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:decoration/gas-blue");
		iconBuffer[1] = iconRegister.registerIcon("warpdrive:decoration/gas-red");
		iconBuffer[2] = iconRegister.registerIcon("warpdrive:decoration/gas-green");
		iconBuffer[3] = iconRegister.registerIcon("warpdrive:decoration/gas-yellow");
		iconBuffer[4] = iconRegister.registerIcon("warpdrive:decoration/gas-dark");
		iconBuffer[5] = iconRegister.registerIcon("warpdrive:decoration/gas-darkness");
		iconBuffer[6] = iconRegister.registerIcon("warpdrive:decoration/gas-white");
		iconBuffer[7] = iconRegister.registerIcon("warpdrive:decoration/gas-milk");
		iconBuffer[8] = iconRegister.registerIcon("warpdrive:decoration/gas-orange");
		iconBuffer[9] = iconRegister.registerIcon("warpdrive:decoration/gas-siren");
		iconBuffer[10] = iconRegister.registerIcon("warpdrive:decoration/gas-gray");
		iconBuffer[11] = iconRegister.registerIcon("warpdrive:decoration/gas-violet");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return iconBuffer[metadata % iconBuffer.length];
	}

	@Override
	public int getMobilityFlag() {
		return 1;
	}

	@Override
	public Item getItemDropped(final int metadata, final Random random, final int fortune) {
		return null;
	}

	@Override
	public int quantityDropped(final Random random) {
		return 0;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final Block blockSide = blockAccess.getBlock(x, y, z);
		if (blockSide.isAssociatedBlock(this)) {
			return false;
		}
		return blockAccess.isAirBlock(x, y, z);
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public void onBlockAdded(final World world, final int x, final int y, final int z) {
		// Gas blocks are only allowed in space
		if (CelestialObjectManager.hasAtmosphere(world, x, z)) {
			world.setBlockToAir(x, y, z);
		}
	}
}