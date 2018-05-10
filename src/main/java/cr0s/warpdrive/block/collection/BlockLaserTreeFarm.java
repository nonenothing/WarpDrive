package cr0s.warpdrive.block.collection;

import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLaserTreeFarm extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	public static final int ICON_IDLE = 0;
	public static final int ICON_FARMING_LOW_POWER = 1;
	public static final int ICON_FARMING_POWERED = 2;
	public static final int ICON_SCANNING_LOW_POWER = 3;
	public static final int ICON_SCANNING_POWERED = 4;
	public static final int ICON_PLANTING_LOW_POWER = 5;
	public static final int ICON_PLANTING_POWERED = 6;
	private static final int ICON_BOTTOM = 7;
	private static final int ICON_TOP = 8;
	
	public BlockLaserTreeFarm() {
		super(Material.iron);
		setBlockName("warpdrive.collection.laser_tree_farm");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[16];
		iconBuffer[ICON_IDLE              ] = iconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_idle");
		iconBuffer[ICON_FARMING_LOW_POWER ] = iconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_farmingLowPower");
		iconBuffer[ICON_FARMING_POWERED   ] = iconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_farmingPowered");
		iconBuffer[ICON_SCANNING_LOW_POWER] = iconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_scanningLowPower");
		iconBuffer[ICON_SCANNING_POWERED  ] = iconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_scanningPowered");
		iconBuffer[ICON_PLANTING_LOW_POWER] = iconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_plantingLowPower");
		iconBuffer[ICON_PLANTING_POWERED  ] = iconRegister.registerIcon("warpdrive:collection/laserTreeFarmSide_plantingPowered");
		iconBuffer[ICON_BOTTOM            ] = iconRegister.registerIcon("warpdrive:collection/laserTreeFarmBottom");
		iconBuffer[ICON_TOP               ] = iconRegister.registerIcon("warpdrive:collection/laserTreeFarmTop");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata  = blockAccess.getBlockMetadata(x, y, z);
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		}
		if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		if (metadata < iconBuffer.length) {
			return iconBuffer[metadata];
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		}
		if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		return iconBuffer[ICON_PLANTING_LOW_POWER];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityLaserTreeFarm();
	}
}