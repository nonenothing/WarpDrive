package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.block.BlockAbstractBase;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class BlockIridium extends BlockAbstractBase {
	
	public BlockIridium(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(3.4F);
		setResistance(360.0F * 5 / 3);
		setUnlocalizedName("warpdrive.passive.iridium_block");
	}
	
	@Nonnull
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		return EnumRarity.RARE;
	}
}