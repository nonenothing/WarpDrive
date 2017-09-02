package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
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
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativeTab, List<ItemStack> list) {
		list.add(new ItemStack(item, 1, BlockSiren.METADATA_TYPE_INDUSTRIAL));
		list.add(new ItemStack(item, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_BASIC));
		list.add(new ItemStack(item, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_ADVANCED));
		list.add(new ItemStack(item, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_SUPERIOR));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
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
	public int damageDropped(IBlockState blockState) {
		return getMetaFromState(blockState);
	}
}
