package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.block.hull.BlockHullSlab.EnumVariant;
import cr0s.warpdrive.data.BlockProperties;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockHullSlab extends ItemBlockHull {
	
	private final Block blockSlab;
	
	public ItemBlockHullSlab(Block blockSlab) {
		super(blockSlab);
		this.blockSlab = blockSlab;
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return getUnlocalizedName();
	}
	
	@Nonnull
	@Override
	public EnumActionResult onItemUse(final ItemStack itemStack, @Nonnull final EntityPlayer entityPlayer, final World world,
	                                  @Nonnull final BlockPos blockPos,
	                                  final EnumHand enumHand, @Nonnull EnumFacing facing,
	                                  final float hitX, final float hitY, final float hitZ) {
		if (itemStack.stackSize == 0) {
			return EnumActionResult.FAIL;
		}
		
		// check if clicked block can be interacted with
		final IBlockState blockStateItem = blockSlab.getStateFromMeta(itemStack.getItemDamage());
		final int metadataItem = itemStack.getItemDamage();
		final EnumVariant variantItem = blockStateItem.getValue(BlockHullSlab.VARIANT);
		
		final IBlockState blockStateWorld = world.getBlockState(blockPos);
		final EnumVariant variantWorld = blockStateWorld.getBlock() == blockSlab ? blockStateWorld.getValue(BlockHullSlab.VARIANT) : EnumVariant.PLAIN_FULL;
		
		if ( blockStateWorld.getBlock() == blockSlab
		  && !variantItem.getIsDouble()
		  && !variantWorld.getIsDouble()
		  && variantWorld == variantItem ) {
			if (!entityPlayer.canPlayerEdit(blockPos, facing, itemStack)) {
				return EnumActionResult.FAIL;
			}
			
			// try to merge slabs when right-clicking directly the inner face
			if (blockStateWorld.getValue(BlockProperties.FACING) == facing) {
				final AxisAlignedBB boundingBox = blockStateWorld.getCollisionBoundingBox(world, blockPos);
				if (boundingBox != null && world.checkNoEntityCollision(boundingBox)) {
					EnumVariant variantNew;
					if ( variantWorld == EnumVariant.PLAIN_HORIZONTAL
					  || variantWorld == EnumVariant.PLAIN_VERTICAL ) {// plain
						variantNew = EnumVariant.PLAIN_FULL;
					} else {
						switch (facing) {
						default:
						case DOWN:
						case UP:
							variantNew = EnumVariant.TILED_FULL_Y;
							break;
						case NORTH:
						case SOUTH:
							variantNew = EnumVariant.TILED_FULL_Z;
							break;
						case WEST:
						case EAST:
							variantNew = EnumVariant.TILED_FULL_X;
							break;
						}
					}
					world.setBlockState(blockPos, blockSlab.getDefaultState().withProperty(BlockHullSlab.VARIANT, variantNew), 3);
					
					final SoundType soundtype = blockSlab.getSoundType(blockStateWorld, world, blockPos, entityPlayer);
					world.playSound(entityPlayer, blockPos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
					                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					itemStack.stackSize--;
				}
				
				return EnumActionResult.SUCCESS;
			}
			
		} else {
			// check is closer block can be interacted with
			final BlockPos blockPosSide = blockPos.offset(facing);
			final IBlockState blockStateSide = world.getBlockState(blockPosSide);
			final EnumVariant variantSide = blockStateSide.getBlock() == blockSlab ? blockStateSide.getValue(BlockHullSlab.VARIANT) : EnumVariant.PLAIN_FULL;
			if ( blockStateSide.getBlock() == blockSlab
			  && !variantItem.getIsDouble()
			  && !variantSide.getIsDouble()
			  && variantSide == variantItem ) {
				if (!entityPlayer.canPlayerEdit(blockPosSide, facing, itemStack)) {
					return EnumActionResult.FAIL;
				}
				
				// try to place ignoring the existing block
				final IBlockState blockStatePlaced = blockSlab.onBlockPlaced(world, blockPosSide, facing, hitX, hitY, hitZ, metadataItem, entityPlayer);
				final EnumFacing enumFacingPlaced = blockStatePlaced.getValue(BlockProperties.FACING).getOpposite();
				
				// try to merge slabs when right-clicking on a side block
				if (enumFacingPlaced == blockStateSide.getValue(BlockProperties.FACING)) {
					final AxisAlignedBB boundingBox = blockStateWorld.getCollisionBoundingBox(world, blockPosSide);
					if (boundingBox != null && world.checkNoEntityCollision(boundingBox)) {
						EnumVariant variantNew;
						if ( variantWorld == EnumVariant.PLAIN_HORIZONTAL
						     || variantWorld == EnumVariant.PLAIN_VERTICAL ) {// plain
							variantNew = EnumVariant.PLAIN_FULL;
						} else {
							switch (facing) {
							default:
							case DOWN:
							case UP:
								variantNew = EnumVariant.TILED_FULL_Y;
								break;
							case NORTH:
							case SOUTH:
								variantNew = EnumVariant.TILED_FULL_Z;
								break;
							case WEST:
							case EAST:
								variantNew = EnumVariant.TILED_FULL_X;
								break;
							}
						}
						world.setBlockState(blockPosSide, blockSlab.getDefaultState().withProperty(BlockHullSlab.VARIANT, variantNew), 3);
						
						final SoundType soundtype = blockSlab.getSoundType(blockStateWorld, world, blockPosSide, entityPlayer);
						world.playSound(entityPlayer, blockPosSide, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
						                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
						itemStack.stackSize--;
					}
					
					return EnumActionResult.SUCCESS;
				}
				
			}
		}
		
		return super.onItemUse(itemStack, entityPlayer, world, blockPos, enumHand, facing, hitX, hitY, hitZ);
	}
	
	/*
	// Should return true if item can be used
	@Override
	public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer entityPlayer, ItemStack itemStack) {
		// check if clicked block can be interacted with
		final int metadataItem = itemStack.getItemDamage();
		final Block blockWorld = world.getBlock(x, y, z);
		final int metadataWorld = world.getBlockMetadata(x, y, z);
		final int typeWorld = getType(metadataWorld);
		final int typeItem = getType(metadataItem);
		if (blockWorld == blockSlab && metadataItem < 12 && metadataWorld < 12 && typeWorld == typeItem) {
			return true;
		}
		
		// check the block on our side
		EnumFacing direction = EnumFacing.getFront(side);
		final int xSide = x + direction.offsetX;
		final int ySide = y + direction.offsetY;
		final int zSide = z + direction.offsetZ;
		final Block blockSide = world.getBlock(xSide, ySide, zSide);
		final int metadataSide = world.getBlockMetadata(xSide, ySide, zSide);
		final int typeSide = getType(metadataSide);
		if (blockSide == blockSlab && typeSide == typeItem) {
			return true;
		}
		
		// default behavior
		return super.func_150936_a(world, x, y, z, side, entityPlayer, itemStack);
	}
	/**/
}
