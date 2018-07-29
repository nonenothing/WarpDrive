package cr0s.warpdrive.block.decoration;

import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.data.EnumDecorativeType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumTier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDecorative extends BlockAbstractBase {
	
	public static final PropertyEnum<EnumDecorativeType> TYPE = PropertyEnum.create("type", EnumDecorativeType.class);
	private static ItemStack[] itemStackCache;
	
	public BlockDecorative(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier, Material.IRON);
		
		setHardness(1.5f);
		setTranslationKey("warpdrive.decoration.decorative.");
		
		setDefaultState(getDefaultState().withProperty(TYPE, EnumDecorativeType.PLAIN));
		itemStackCache = new ItemStack[EnumDecorativeType.length];
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				.withProperty(TYPE, EnumDecorativeType.get(metadata));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(TYPE).ordinal();
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockDecorative(this);
	}
	
	@Override
	public void getSubBlocks(final CreativeTabs creativeTab, final NonNullList<ItemStack> list) {
		for (final EnumDecorativeType enumDecorativeType : EnumDecorativeType.values()) {
			list.add(new ItemStack(this, 1, enumDecorativeType.ordinal()));
		}
	}
	
	@Override
	public int damageDropped(final IBlockState blockState) {
		return blockState.getBlock().getMetaFromState(blockState);
	}
	
	public static ItemStack getItemStack(final EnumDecorativeType enumDecorativeType) {
		if (enumDecorativeType != null) {
			int damage = enumDecorativeType.ordinal();
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.blockDecorative, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(final EnumDecorativeType enumDecorativeType, final int amount) {
		return new ItemStack(WarpDrive.blockDecorative, amount, enumDecorativeType.ordinal());
	}
}
