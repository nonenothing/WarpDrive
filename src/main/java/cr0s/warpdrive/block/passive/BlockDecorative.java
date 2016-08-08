package cr0s.warpdrive.block.passive;

import java.util.List;

import cr0s.warpdrive.data.EnumDecorativeType;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cr0s.warpdrive.WarpDrive;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class BlockDecorative extends Block {
	private static ItemStack[] itemStackCache;
	
	public BlockDecorative(final String registryName) {
		super(Material.IRON);
		setHardness(1.5f);
		setSoundType(SoundType.METAL);
		setUnlocalizedName("warpdrive.passive.Plain");
		setRegistryName(registryName);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlockDecorative(this));
		
		itemStackCache = new ItemStack[EnumDecorativeType.length];
	}
	
	@Override
	public void getSubBlocks(@Nonnull Item item, CreativeTabs creativeTabs, List<ItemStack> list) {
		for (EnumDecorativeType enumDecorativeType : EnumDecorativeType.values()) {
			list.add(new ItemStack(item, 1, enumDecorativeType.ordinal()));
		}
	}
	
	@Override
	public int damageDropped(IBlockState blockState) {
		return blockState.getBlock().getMetaFromState(blockState);
	}
	
	public static ItemStack getItemStack(EnumDecorativeType enumDecorativeType) {
		if (enumDecorativeType != null) {
			int damage = enumDecorativeType.ordinal();
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.blockDecorative, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(EnumDecorativeType enumDecorativeType, int amount) {
		return new ItemStack(WarpDrive.blockDecorative, amount, enumDecorativeType.ordinal());
	}
}
