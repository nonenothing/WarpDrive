package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.data.SoundEvents;
import cr0s.warpdrive.item.ItemComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockShipCore extends BlockAbstractContainer {
	
	public BlockShipCore(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.movement.ShipCore");
		GameRegistry.registerTileEntity(TileEntityShipCore.class, WarpDrive.PREFIX + registryName);
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityShipCore();
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLiving, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLiving, itemStack);
		
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityShipCore) {
			((TileEntityShipCore) tileEntity).facing = Commons.getHorizontalDirectionFromEntity(entityLiving).getOpposite();
			// @TODO MC1.10 world.markBlockForUpdate(x, y, z);
		}
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos blockPos, IBlockState blockState, int fortune) {
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityShipCore) {
			if (((TileEntityShipCore)tileEntity).jumpCount == 0) {
				return super.getDrops(world, blockPos, blockState, fortune);
			}
		}
		ArrayList<ItemStack> itemStacks = new ArrayList<>();
		if (world instanceof WorldServer) {
			// trigger explosion
			EntityTNTPrimed entityTNTPrimed = new EntityTNTPrimed(((WorldServer) world),
				blockPos.getX() + 0.5F, blockPos.getY() + 0.5F, blockPos.getZ() + 0.5F, null);
			entityTNTPrimed.setFuse(10 + ((WorldServer) world).rand.nextInt(10));
			((WorldServer) world).spawnEntityInWorld(entityTNTPrimed);
			
			// get a chance to get the drops
			itemStacks.add(ItemComponent.getItemStackNoCache(EnumComponentType.CAPACITIVE_CRYSTAL, 1));
			if (fortune > 0 && ((WorldServer) world).rand.nextBoolean()) {
				itemStacks.add(ItemComponent.getItemStackNoCache(EnumComponentType.CAPACITIVE_CRYSTAL, 1));
			}
			if (fortune > 1 && ((WorldServer) world).rand.nextBoolean()) {
				itemStacks.add(ItemComponent.getItemStackNoCache(EnumComponentType.CAPACITIVE_CRYSTAL, 1));
			}
			if (fortune > 1 & ((WorldServer) world).rand.nextBoolean()) {
				itemStacks.add(ItemComponent.getItemStackNoCache(EnumComponentType.POWER_INTERFACE, 1));
			}
		}
		return itemStacks;
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(IBlockState blockState, EntityPlayer entityPlayer, World world, BlockPos blockPos) {
		boolean willBreak = true;
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityShipCore) {
			if (((TileEntityShipCore)tileEntity).jumpCount == 0) {
				willBreak = false;
			}
		}
		return (willBreak ? 0.02F : 1.0F) * super.getPlayerRelativeBlockHardness(blockState, entityPlayer, world, blockPos);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer, EnumHand hand, @Nullable ItemStack itemStackHeld, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		if (itemStackHeld == null) {
			final TileEntity tileEntity = world.getTileEntity(blockPos);
			if (tileEntity instanceof TileEntityShipCore) {
				final TileEntityShipCore tileEntityShipCore = (TileEntityShipCore) tileEntity;
				if ( world.isRemote
				  && entityPlayer.isSneaking() ) {
					tileEntityShipCore.showBoundingBox = !tileEntityShipCore.showBoundingBox;
					if (tileEntityShipCore.showBoundingBox) {
						world.playSound(null, blockPos, SoundEvents.LASER_LOW, SoundCategory.BLOCKS, 4.0F, 2.0F);
					} else {
						world.playSound(null, blockPos, SoundEvents.LASER_LOW, SoundCategory.BLOCKS, 4.0F, 1.4F);
					}
					Commons.addChatMessage(entityPlayer, tileEntityShipCore.getBoundingBoxStatus());
					return true;
				} else if ( !world.isRemote
				         && !entityPlayer.isSneaking() ) {
					Commons.addChatMessage(entityPlayer, tileEntityShipCore.getStatus());
					return true;
				}
			}
		}
		
		return false;
	}
}