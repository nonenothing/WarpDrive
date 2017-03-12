package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockAbstractAccelerator extends BlockAbstractBase implements IBlockBase {
	
	public final byte tier;
	
	BlockAbstractAccelerator(final String registryName, final byte tier) {
		super(registryName, Material.IRON);
		this.tier = tier;
		setHardness(4 + tier);
		setResistance((2 + 2 * tier) * 5 / 3);
		setSoundType(SoundType.METAL);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
	}
	
	@Override
	public int damageDropped(IBlockState blockState) {
		return 0;
	}
	
	public byte getTier(final ItemStack itemStack) {
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
	
	@Override
	public boolean canCreatureSpawn(@Nonnull IBlockState blockState, @Nonnull IBlockAccess world, @Nonnull BlockPos blockPos, SpawnPlacementType type) {
		return false;
	}
}
