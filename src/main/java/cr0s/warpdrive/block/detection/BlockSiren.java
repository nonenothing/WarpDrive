package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumSirenType;
import cr0s.warpdrive.data.EnumTier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockSiren extends BlockAbstractContainer {
	
	public static final int METADATA_TYPE_INDUSTRIAL = 0;
	public static final int METADATA_TYPE_RAID = 4;
	public static final int METADATA_RANGE_BASIC = 0;
	public static final int METADATA_RANGE_ADVANCED = 1;
	public static final int METADATA_RANGE_SUPERIOR = 2;
	
	public BlockSiren(final String registryName) {
		super(registryName, Material.IRON);
		hasSubBlocks = true;
		setUnlocalizedName("warpdrive.detection.Siren");
		GameRegistry.registerTileEntity(TileEntitySiren.class, WarpDrive.MODID + ":tileEntitySiren");
		
		setDefaultState(getDefaultState()
		                .withProperty(BlockProperties.SIREN_TYPE, EnumSirenType.INDUSTRIAL)
		                .withProperty(BlockProperties.TIER, EnumTier.BASIC));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(@Nonnull final Item item, final CreativeTabs creativeTab, final List<ItemStack> list) {
		list.add(new ItemStack(item, 1, EnumSirenType.INDUSTRIAL.getIndex()));
		list.add(new ItemStack(item, 1, EnumSirenType.RAID.getIndex() + EnumTier.BASIC.getIndex() - 1));
		list.add(new ItemStack(item, 1, EnumSirenType.RAID.getIndex() + EnumTier.ADVANCED.getIndex() - 1));
		list.add(new ItemStack(item, 1, EnumSirenType.RAID.getIndex() + EnumTier.SUPERIOR.getIndex() - 1));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockProperties.SIREN_TYPE, BlockProperties.TIER);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
		       .withProperty(BlockProperties.SIREN_TYPE, EnumSirenType.get(metadata & 4))
		       .withProperty(BlockProperties.TIER, EnumTier.get(1 + (metadata & 3)));
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(BlockProperties.SIREN_TYPE).getIndex() + Math.max(0, (blockState.getValue(BlockProperties.TIER).getIndex() - 1));
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockSiren(this);
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntitySiren();
	}
	
	static boolean getIsRaid(final int metadata) {
		switch (metadata & 0x4) {
			case METADATA_TYPE_INDUSTRIAL: return false;
			case METADATA_TYPE_RAID      : return true;
			default: return false;
		}
	}
	
	static float getRange(final int metadata) {
		switch (metadata & 0x3) {
			case METADATA_RANGE_BASIC   : return 32.0F;
			case METADATA_RANGE_ADVANCED: return 64.0F;
			case METADATA_RANGE_SUPERIOR: return 128.0F;
			default: return 0.0F;
		}
	}
	
	@Override
	public int damageDropped(final IBlockState blockState) {
		return getMetaFromState(blockState);
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		if (itemStack == null || itemStack.getItem() != Item.getItemFromBlock(this)) {
			return 1;
		}
		return (byte) getStateFromMeta(itemStack.getMetadata()).getValue(BlockProperties.TIER).getIndex();
	}
}
