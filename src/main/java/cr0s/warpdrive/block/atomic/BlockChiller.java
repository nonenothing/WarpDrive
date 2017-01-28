package cr0s.warpdrive.block.atomic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

public class BlockChiller extends BlockAbstractAccelerator {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	private static final float BOUNDING_TOLERANCE = 0.05F;
	
	public BlockChiller(final byte tier) {
		super(tier);
		setBlockName("warpdrive.atomic.chiller" + tier);
		setBlockTextureName("warpdrive:atomic/chiller" + tier);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return icons[metadata % 16];
	}
	
	@Override
	public int damageDropped(int metadata) {
		return 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[2];
		
		icons[0] = iconRegister.registerIcon(getTextureName() + "-off");
		icons[1] = iconRegister.registerIcon(getTextureName() + "-on");
	}
	
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(
			x + BOUNDING_TOLERANCE, y + BOUNDING_TOLERANCE, z + BOUNDING_TOLERANCE,
			x + 1 - BOUNDING_TOLERANCE, y + 1 - BOUNDING_TOLERANCE, z + 1 - BOUNDING_TOLERANCE);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		super.onEntityCollidedWithBlock(world, x, y, z, entity);
		onEntityEffect(world, x, y, z, entity);
	}
	
	@Override
	public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
		super.onEntityWalking(world, x, y, z, entity);
		onEntityEffect(world, x, y, z, entity);
	}
	
	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		super.onBlockClicked(world, x, y, z, entityPlayer);
		onEntityEffect(world, x, y, z, entityPlayer);
	}
	
	private void onEntityEffect(World world, final int x, final int y, final int z, Entity entity) {
		if (entity.isDead || !(entity instanceof EntityLivingBase)) {
			return;
		}
		if (world.getBlockMetadata(x, y, z) == 0) {
			return;
		}
		if (!entity.isImmuneToFire()) {
			entity.setFire(1);
		}
		entity.attackEntityFrom(WarpDrive.damageWarm, 1 + tier);
		
		Vector3 v3Entity = new Vector3(entity);
		Vector3 v3Chiller = new Vector3(x + 0.5D, y + 0.5D, z + 0.5D);
		Vector3 v3Direction = new Vector3(entity).subtract(v3Chiller).normalize();
		v3Chiller.translateFactor(v3Direction, 0.6D);
		v3Entity.translateFactor(v3Direction, -0.6D);
		
		// visual effect
		v3Direction.scale(0.20D);
		PacketHandler.sendSpawnParticlePacket(world, "snowshovel", v3Entity, v3Direction,
			0.90F + 0.10F * world.rand.nextFloat(), 0.35F + 0.25F * world.rand.nextFloat(), 0.30F + 0.15F * world.rand.nextFloat(),
			0.0F, 0.0F, 0.0F, 32);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
		if (world.getBlockMetadata(x, y, z) == 0) {
			return;
		}
		
		double dOffset = 0.0625D;
		
		for (int l = 0; l < 6; ++l) {
			double dX = (double)((float)x + random.nextFloat());
			double dY = (double)((float)y + random.nextFloat());
			double dZ = (double)((float)z + random.nextFloat());
			boolean isValidSide = false;
			
			if (l == 0 && !world.getBlock(x, y + 1, z).isOpaqueCube()) {
				dY = y + 1 + dOffset;
				isValidSide = true;
			}
			
			if (l == 1 && !world.getBlock(x, y - 1, z).isOpaqueCube()) {
				dY = y - dOffset;
				isValidSide = true;
			}
			
			if (l == 2 && !world.getBlock(x, y, z + 1).isOpaqueCube()) {
				dZ = z + 1 + dOffset;
				isValidSide = true;
			}
			
			if (l == 3 && !world.getBlock(x, y, z - 1).isOpaqueCube()) {
				dZ = z - dOffset;
				isValidSide = true;
			}
			
			if (l == 4 && !world.getBlock(x + 1, y, z).isOpaqueCube()) {
				dX = x + 1 + dOffset;
				isValidSide = true;
			}
			
			if (l == 5 && !world.getBlock(x - 1, y, z).isOpaqueCube()) {
				dX = x - dOffset;
				isValidSide = true;
			}
			
			if (isValidSide) {
				world.spawnParticle("reddust", dX, dY, dZ, 0.0D, 0.0D, 0.0D);
			}
		}
	}
	
}
