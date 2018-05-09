package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.block.BlockAbstractBase;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class BlockWarpIsolation extends BlockAbstractBase {
	
	public BlockWarpIsolation(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(3.5F);
		setUnlocalizedName("warpdrive.detection.warp_isolation");
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockWarpIsolation(this);
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		return EnumRarity.UNCOMMON;
	}
}