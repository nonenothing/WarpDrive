package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.block.BlockAbstractOmnipanel;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAirShield extends BlockAbstractOmnipanel {
	
	public BlockAirShield() {
		super(Material.cloth);
		setBlockName("warpdrive.breathing.air_shield");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon("warpdrive:breathing/air_shield");
	}
	
	@Override
	public int damageDropped(final int metadata) {
		return metadata;
	}
	
	@Override
	public int getRenderColor(final int metadata) {
		switch (metadata) {
		case 1:
			return 0xFF5A02;    // orange
		case 5:
			return 0x90E801;    // lime green
		case 6:
			return 0xFB0680;    // pink
		case 9:
		default:
			return 0x0FD7FF;    // SciFi cyan
		case 10:
			return 0x5D1072;    // purple
		}
	}
	
	@Override
	public int colorMultiplier(final IBlockAccess blockAccess, final int x, final int y, final int z) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		return getRenderColor(metadata);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(final World world, final int x, final int y, final int z) {
		return null;
	}
	
	@Override
	public boolean canCollideCheck(final int metadata, final boolean hitIfLiquid) {
		return !hitIfLiquid;
	}
}