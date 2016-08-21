package cr0s.warpdrive.block.detection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class BlockSiren extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
    private IIcon[] iconBuffer;
	
	public static final int METADATA_TYPE_INDUSTRIAL = 0;
	public static final int METADATA_TYPE_RAID = 4;
	public static final int METADATA_RANGE_BASIC = 0;
	public static final int METADATA_RANGE_ADVANCED = 1;
	public static final int METADATA_RANGE_SUPERIOR = 2;
	
	private static final int ICON_INDUSTRIAL = 0;
	private static final int ICON_RAID_BASIC = 1;
	private static final int ICON_RAID_ADVANCED = 2;
	private static final int ICON_RAID_SUPERIOR = 3;
	
	public BlockSiren() {
		super(Material.iron);
		hasSubBlocks = true;
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.detection.Siren");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		iconBuffer = new IIcon[4];
		// Solid textures
		iconBuffer[ICON_INDUSTRIAL] = iconRegister.registerIcon("warpdrive:detection/siren_industrial");
		iconBuffer[ICON_RAID_BASIC] = iconRegister.registerIcon("warpdrive:detection/siren_raid_basic");
		iconBuffer[ICON_RAID_ADVANCED] = iconRegister.registerIcon("warpdrive:detection/siren_raid_advanced");
		iconBuffer[ICON_RAID_SUPERIOR] = iconRegister.registerIcon("warpdrive:detection/siren_raid_superior");
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (!getIsRaid(metadata)) {
			return iconBuffer[ICON_INDUSTRIAL];
		}
		switch (metadata & 0x3) {
			case METADATA_RANGE_BASIC   : return iconBuffer[ICON_RAID_BASIC];
			case METADATA_RANGE_ADVANCED: return iconBuffer[ICON_RAID_ADVANCED];
			case METADATA_RANGE_SUPERIOR: return iconBuffer[ICON_RAID_SUPERIOR];
			default: return iconBuffer[ICON_RAID_BASIC];
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativeTab, List list) {
		list.add(new ItemStack(item, 1, BlockSiren.METADATA_TYPE_INDUSTRIAL));
		list.add(new ItemStack(item, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_BASIC));
		list.add(new ItemStack(item, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_ADVANCED));
		list.add(new ItemStack(item, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_SUPERIOR));
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
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
	public int damageDropped(int metadata) {
		return metadata;
	}
	
	// Silences the siren if the block is destroyed.
	// If this fails, the siren will still be stopped when it's invalidated.
	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		if (!world.isRemote) {
			super.onBlockPreDestroy(world, x, y, z, meta);
			return;
		}
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntitySiren) {
			TileEntitySiren tileEntitySiren = (TileEntitySiren) tileEntity;
			
			if (tileEntitySiren.isPlaying()) {
				tileEntitySiren.stopSound();
			}
		}
	}
}
