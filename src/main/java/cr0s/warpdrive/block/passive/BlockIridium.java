package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.block.BlockAbstractBase;

import javax.annotation.Nonnull;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockIridium extends BlockAbstractBase {
	
	public BlockIridium(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(3.4F);
		setResistance(360.0F * 5 / 3);
		setUnlocalizedName("warpdrive.passive.IridiumBlock");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	@Nonnull
	@Override
	public EnumRarity getRarity(ItemStack itemStack, EnumRarity rarity) {
		return EnumRarity.RARE;
	}
}