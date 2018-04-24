package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.WarpDrive;

import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.EnumTransporterBeaconState;
import cr0s.warpdrive.render.RenderBlockTransporterBeacon;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTransporterBeacon extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	public BlockTransporterBeacon() {
		super(Material.iron);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.movement.transporter_beacon");
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(final World world, final int x, final int y, final int z) {
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[4];
		iconBuffer[EnumTransporterBeaconState.PACKED_INACTIVE  .getMetadata()] = iconRegister.registerIcon("warpdrive:movement/transporter_beacon-packed_inactive");
		iconBuffer[EnumTransporterBeaconState.PACKED_ACTIVE    .getMetadata()] = iconRegister.registerIcon("warpdrive:movement/transporter_beacon-packed_active");
		iconBuffer[EnumTransporterBeaconState.DEPLOYED_INACTIVE.getMetadata()] = iconRegister.registerIcon("warpdrive:movement/transporter_beacon-deployed_inactive");
		iconBuffer[EnumTransporterBeaconState.DEPLOYED_ACTIVE  .getMetadata()] = iconRegister.registerIcon("warpdrive:movement/transporter_beacon-deployed_active");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return iconBuffer[metadata & 3];
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public int getRenderType() {
		return RenderBlockTransporterBeacon.renderId;
	}
	
	@Override
	public MovingObjectPosition collisionRayTrace(final World world, final int x, final int y, final int z, final Vec3 par5Vec3, final Vec3 par6Vec3) {
		final float radius = 0.09375F;
		setBlockBounds(0.5F - radius, 0.0F, 0.5F - radius, 0.5F + radius, 0.6F, 0.5F + radius);
		
		return super.collisionRayTrace(world, x, y, z, par5Vec3, par6Vec3);
	}
	
	@Override
	public int getLightValue(final IBlockAccess blockAccess, final int x, final int y, final int z) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		return metadata == 0 ? 0 : 6;
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityTransporterBeacon();
	}
	
}
