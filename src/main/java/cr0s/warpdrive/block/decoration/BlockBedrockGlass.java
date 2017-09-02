package cr0s.warpdrive.block.decoration;

import java.util.Random;

import cr0s.warpdrive.block.BlockAbstractBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBedrockGlass extends BlockAbstractBase {
	
	public BlockBedrockGlass(final String registryName) {
		super(registryName, Material.FIRE);
		setHardness(0.0F);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setSoundType(SoundType.STONE);
		disableStats();
		setUnlocalizedName("warpdrive.decoration.bedrock_glass");
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullyOpaque(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, @Nonnull BlockPos blockPos) {
		return false;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, @Nonnull BlockPos blockPos) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
	
	@Nullable
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
	
	@Override
	public int quantityDropped(Random rand) {
		return 0;
	}
	
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos blockPos, EnumFacing facing) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
	
	@Override
	public boolean isCollidable() {
		return true;
	}
	
	@Override
	public EnumRarity getRarity(ItemStack itemStack, EnumRarity rarity) {
		return EnumRarity.EPIC;
	}
}