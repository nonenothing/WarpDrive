package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.block.BlockAbstractBase;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBedrockGlass extends BlockAbstractBase {
	
	public BlockBedrockGlass() {
		super(Material.fire);
		setHardness(0.0F);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundTypePiston);
		disableStats();
		setBlockName("warpdrive.decoration.bedrock_glass");
		setBlockTextureName("warpdrive:decoration/bedrock_glass");
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isAir(final IBlockAccess blockAccess, final int x, final int y, final int z) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(final IBlockAccess blockAccess, final int x, final int y, final int z) {
		return false;
	}
	
	@Override
	public boolean canPlaceBlockAt(final World world, final int x, final int y, final int z) {
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		return false;
	}
	
	@Override
	public int getMobilityFlag() {
		return 2;
	}
	
	@Override
	public Item getItemDropped(final int metadata, final Random random, final int fortune) {
		return null;
	}
	
	@Override
	public int quantityDropped(final Random random) {
		return 0;
	}
	
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
	
	@Override
	public boolean isCollidable() {
		return true;
	}
}