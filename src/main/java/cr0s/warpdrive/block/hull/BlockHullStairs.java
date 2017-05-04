package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.api.IDamageReceiver;
import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.client.ClientProxy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockHullStairs extends BlockStairs implements IBlockBase, IDamageReceiver {
	
	protected final byte tier;
	private final IBlockState blockStateHull;
	
	public BlockHullStairs(final String registryName, final IBlockState blockStateHull, final byte tier) {
		super(blockStateHull);
		this.blockStateHull = blockStateHull;
		this.tier = tier;
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setUnlocalizedName("warpdrive.hull" + tier + ".stairs." + EnumDyeColor.byMetadata(blockStateHull.getBlock().getMetaFromState(blockStateHull)).getName());
		setRegistryName(registryName);
		WarpDrive.register(this, new ItemBlockHull(this));
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
	
	@Override
	public byte getTier(ItemStack itemStack) {
		return tier;
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		switch (getTier(itemStack)) {
		case 0:	return EnumRarity.EPIC;
		case 1:	return EnumRarity.COMMON;
		case 2:	return EnumRarity.UNCOMMON;
		case 3:	return EnumRarity.RARE;
		default: return rarity;
		}
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockHull(this);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void modelInitialisation() {
		Item item = Item.getItemFromBlock(this);
		ClientProxy.modelInitialisation(item);
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World world, BlockPos blockPos,
	                              DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		// TODO: adjust hardness to damage type/color
		return WarpDriveConfig.HULL_HARDNESS[tier - 1];
	}
	
	@Override
	public int applyDamage(IBlockState blockState, World world, BlockPos blockPos,
	                       DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		if (damageLevel <= 0) {
			return 0;
		}
		if (tier == 1) {
			world.setBlockToAir(blockPos);
		} else {
			world.setBlockState(blockPos, WarpDrive.blockHulls_stairs[tier - 2][blockStateHull.getBlock().getMetaFromState(blockStateHull)]
			                              .getDefaultState()
			                              .withProperty(FACING, blockState.getValue(FACING)), 2);
		}
		return 0;
	}
}
