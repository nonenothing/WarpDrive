package cr0s.warpdrive.block.collection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractLaser;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public abstract class TileEntityAbstractMiner extends TileEntityAbstractLaser {
	
	// machine type
	protected EnumFacing laserOutputSide = EnumFacing.NORTH;
	
	// machine state
	protected boolean		 enableSilktouch = false;
	
	// pre-computation
	protected Vector3        laserOutput = null;
	
	public TileEntityAbstractMiner() {
		super();
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
		laserOutput = new Vector3(this).translate(0.5D).translate(laserOutputSide, 0.5D);
	}
	
	protected void stop() {
		if (WarpDriveConfig.LOGGING_COLLECTION) {
			WarpDrive.logger.info(this + " Stop requested");
		}
	}
	
	protected void harvestBlock(final BlockPos valuable) {
		final IBlockState blockState = world.getBlockState(valuable);
		if (blockState.getBlock().isAir(blockState, world, valuable)) {
			return;
		}
		if (blockState.getBlock() instanceof BlockLiquid) {
			// Evaporate fluid
			world.playSound(null, valuable, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
					2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
			
			// remove without updating neighbours @TODO: add proper pump upgrade
			world.setBlockState(valuable, Blocks.AIR.getDefaultState(), 2);
		} else {
			final List<ItemStack> itemStacks = getItemStackFromBlock(valuable, blockState);
			if (addToConnectedInventories(itemStacks)) {
				stop();
			}
			// standard harvest block effect
			world.playEvent(2001, valuable, Block.getStateId(blockState));
			
			// remove while updating neighbours
			world.setBlockState(valuable, Blocks.AIR.getDefaultState(), 3);
		}
	}
	
	private List<ItemStack> getItemStackFromBlock(final BlockPos blockPos, final IBlockState blockState) {
		if (blockState == null) {
			WarpDrive.logger.error(String.format("%s Invalid block %s",
			                                     this, Commons.format(world, blockPos)));
			return null;
		}
		if (enableSilktouch) {
			boolean isSilkHarvestable = false;
			try {
				isSilkHarvestable = blockState.getBlock().canSilkHarvest(world, blockPos, blockState, null);
			} catch (final Exception exception) {// protect in case the mined block is corrupted
				exception.printStackTrace();
			}
			if (isSilkHarvestable) {
				final ArrayList<ItemStack> isBlock = new ArrayList<>();
				// @TODO MC1.10 silktouch mining
				// isBlock.add(blockState.getBlock().createStackedBlock(blockState));
				return isBlock;
			}
		}
		
		try {
			return blockState.getBlock().getDrops(world, blockPos, blockState, 0);
		} catch (final Exception exception) {// protect in case the mined block is corrupted
			exception.printStackTrace();
			return null;
		}
	}
	
	// NBT DATA
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		enableSilktouch = tagCompound.getBoolean("enableSilktouch");
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		tagCompound.setBoolean("enableSilktouch", enableSilktouch);
		return tagCompound;
	}
}
