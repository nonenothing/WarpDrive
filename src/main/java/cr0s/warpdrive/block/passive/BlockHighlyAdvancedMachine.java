package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.block.BlockAbstractBase;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class BlockHighlyAdvancedMachine extends BlockAbstractBase {
	
	public BlockHighlyAdvancedMachine(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(5.0F);
		setUnlocalizedName("warpdrive.passive.HighlyAdvancedMachine");
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		return EnumRarity.COMMON;
	}
}