package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.api.IDamageReceiver;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStairs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class BlockHullStairs extends BlockStairs implements IBlockBase, IDamageReceiver {
	protected final byte tier;
	private final int metaHull;
	
	public BlockHullStairs(final Block blockHull, final int metaHull, final byte tier) {
		super(blockHull, metaHull);
		this.metaHull = metaHull;
		this.tier = tier;
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.hull" + tier + ".stairs." + ItemDye.field_150923_a[BlockColored.func_150031_c(metaHull)]);
	}
	
	@Override
	public int getMobilityFlag() {
		return 2;
	}
	
	@Override
	public byte getTier(ItemStack itemStack) {
		return tier;
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		switch (getTier(itemStack)) {
		case 0:	return EnumRarity.epic;
		case 1:	return EnumRarity.common;
		case 2:	return EnumRarity.uncommon;
		case 3:	return EnumRarity.rare;
		default: return rarity;
		}
	}
	
	@Override
	public float getBlockHardness(World world, int x, int y, int z, DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		// TODO: adjust hardness to damage type/color
		return WarpDriveConfig.HULL_HARDNESS[tier - 1];
	}
	
	@Override
	public int applyDamage(World world, int x, int y, int z, DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		if (damageLevel <= 0) {
			return 0;
		}
		if (tier == 1) {
			world.setBlockToAir(x, y, z);
		} else {
			int metadata = world.getBlockMetadata(x, y, z);
			world.setBlock(x, y, z, WarpDrive.blockHulls_stairs[tier - 2][metaHull], metadata, 2);
		}
		return 0;
	}
}
