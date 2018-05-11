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
		setUnlocalizedName("warpdrive.movement.ship_core");
		GameRegistry.registerTileEntity(TileEntityShipCore.class, WarpDrive.PREFIX + registryName);
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityShipCore();
	}
	
	@Override
	public void onBlockPlacedBy(final World world, final BlockPos blockPos, final IBlockState blockState,
	                            final EntityLivingBase entityLiving, final ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLiving, itemStack);
		
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityShipCore) {
			((TileEntityShipCore) tileEntity).facing = Commons.getHorizontalDirectionFromEntity(entityLiving).getOpposite();
			// @TODO MC1.10 world.markBlockForUpdate(x, y, z);
		}
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getDrops(final IBlockAccess world, final BlockPos blockPos, final IBlockState blockState, final int fortune) {
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityShipCore) {
			if (((TileEntityShipCore)tileEntity).jumpCount == 0) {
				return super.getDrops(world, blockPos, blockState, fortune);
			}
		}
		final ArrayList<ItemStack> itemStacks = new ArrayList<>();
		if (world instanceof WorldServer) {
			// trigger explosion
			final EntityTNTPrimed entityTNTPrimed = new EntityTNTPrimed(((WorldServer) world),
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
	
	@SuppressWarnings("deprecation")
	@Override
	public float getPlayerRelativeBlockHardness(final IBlockState blockState, @Nonnull final EntityPlayer entityPlayer, @Nonnull final World world, @Nonnull final BlockPos blockPos) {
		boolean willBreak = true;
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityShipCore) {
			if (((TileEntityShipCore)tileEntity).jumpCount == 0) {
				willBreak = false;
			}
		}
		return (willBreak ? 0.02F : 1.0F) * super.getPlayerRelativeBlockHardness(blockState, entityPlayer, world, blockPos);
	}
	
	@Override
	public boolean onBlockActivated(final World world, final BlockPos blockPos, final IBlockState blockState,
	                                final EntityPlayer entityPlayer, final EnumHand hand, @Nullable final ItemStack itemStackHeld,
	                                final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
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