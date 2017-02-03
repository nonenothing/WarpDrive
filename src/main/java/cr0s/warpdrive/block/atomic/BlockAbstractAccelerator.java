package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.config.WarpDriveConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public class BlockAbstractAccelerator extends Block implements IBlockBase {
	public final byte tier;
	
	public BlockAbstractAccelerator(final byte tier) {
		super(Material.iron);
		this.tier = tier;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1] / 5);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] / 6 * 5 / 3);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
	}
	
	@Override
	public boolean isOpaqueCube() {
		return true;
	}
	
	@Override
	public int getRenderBlockPass() {
		return 0;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return true;
	}
	
	@Override
	public int damageDropped(int metadata) {
		return 0;
	}
	
	public byte getTier(final ItemStack itemStack) {
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
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
		return false;
	}
}
