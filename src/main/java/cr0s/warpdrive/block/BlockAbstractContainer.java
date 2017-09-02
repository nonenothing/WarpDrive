package cr0s.warpdrive.block;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.api.IBlockUpdateDetector;
import cr0s.warpdrive.client.ClientProxy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.BlockProperties;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Optional.InterfaceList({
	@Optional.Interface(iface = "defense.api.IEMPBlock", modid = "DefenseTech"),
	@Optional.Interface(iface = "resonant.api.explosion.IEMPBlock", modid = "icbmclassic")
})
public abstract class BlockAbstractContainer extends BlockContainer implements IBlockBase, defense.api.IEMPBlock, resonant.api.explosion.IEMPBlock {
	
	protected boolean hasSubBlocks = false; // @TODO: code review
	
	protected BlockAbstractContainer(final String registryName, final Material material) {
		super(material);
		setHardness(5.0F);
		setResistance(6.0F * 5 / 3);
		setSoundType(SoundType.METAL);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setRegistryName(registryName);
		WarpDrive.register(this);
		
		setDefaultState(blockState.getBaseState());
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockAbstractBase(this);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void modelInitialisation() {
		Item item = Item.getItemFromBlock(this);
		ClientProxy.modelInitialisation(item);
	}
	
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof IBlockUpdateDetector) {
			((IBlockUpdateDetector) tileEntity).updatedNeighbours();
		}
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLiving, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLiving, itemStack);
		final boolean isRotating = blockState.getProperties().containsKey(BlockProperties.FACING);
		if (isRotating) {
			final EnumFacing enumFacing = BlockAbstractBase.getFacingFromEntity(blockPos, entityLiving);
			world.setBlockState(blockPos, blockState.withProperty(BlockProperties.FACING, enumFacing));
		}
		
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity != null && itemStack.getTagCompound() != null) {
			NBTTagCompound nbtTagCompound = itemStack.getTagCompound().copy();
			nbtTagCompound.setInteger("x", blockPos.getX());
			nbtTagCompound.setInteger("y", blockPos.getY());
			nbtTagCompound.setInteger("z", blockPos.getZ());
			tileEntity.readFromNBT(nbtTagCompound);
			world.notifyBlockUpdate(blockPos, blockState, blockState, 3);
		}
	}
	
	@Override
	public boolean removedByPlayer(@Nonnull IBlockState blockState, World world, @Nonnull BlockPos blockPos, @Nonnull EntityPlayer player, boolean willHarvest) {
		return willHarvest || super.removedByPlayer(blockState, world, blockPos, player, false);
	}
	
	@Override
	public void dropBlockAsItemWithChance(World world, @Nonnull BlockPos blockPos, @Nonnull IBlockState blockState, float chance, int fortune) {
		// @TODO: to be tested with ship core explosion on breaking
		ItemStack itemStack = new ItemStack(this);
		itemStack.setItemDamage(damageDropped(blockState));
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity == null) {
			WarpDrive.logger.error("Missing tile entity for " + this + " at " + world + " " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
		} else if (tileEntity instanceof TileEntityAbstractBase) {
			NBTTagCompound nbtTagCompound = new NBTTagCompound();
			((TileEntityAbstractBase) tileEntity).writeItemDropNBT(nbtTagCompound);
			itemStack.setTagCompound(nbtTagCompound);
		}
		world.setBlockToAir(blockPos);
		super.dropBlockAsItemWithChance(world, blockPos, blockState, chance, fortune);
	}
	
	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos blockPos, EntityPlayer entityPlayer) {
		ItemStack itemStack = super.getPickBlock(state, target, world, blockPos, entityPlayer);
		TileEntity tileEntity = world.getTileEntity(blockPos);
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		if (tileEntity instanceof TileEntityAbstractBase) {
			((TileEntityAbstractBase) tileEntity).writeItemDropNBT(nbtTagCompound);
			itemStack.setTagCompound(nbtTagCompound);
		}
		return itemStack;
	}
	
	@Override
	public boolean rotateBlock(final World world, @Nonnull final BlockPos blockPos, final EnumFacing axis) {
		// already handled by vanilla
		return super.rotateBlock(world, blockPos, axis);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState blockState, World world, BlockPos blockPos, Block block) {
		super.neighborChanged(blockState, world, blockPos, block);
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof IBlockUpdateDetector) {
			((IBlockUpdateDetector) tileEntity).updatedNeighbours();
		}
	}
	
	@Override
	@Optional.Method(modid = "DefenseTech")
	public void onEMP(World world, int x, int y, int z, defense.api.IExplosion explosiveEMP) {
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info(String.format("EMP received @ DIM%d (%d %d %d) from %s with energy %d and radius %.1f",
			                                    world.provider.getDimension(), x, y, z,
			                                    explosiveEMP, explosiveEMP.getEnergy(), explosiveEMP.getRadius()));
		}
		// EMP tower = 3k Energy, 60 radius
		// EMP explosive = 3k Energy, 50 radius
		if (explosiveEMP.getRadius() == 60.0F) {// compensate tower stacking effect
			onEMP(world, new BlockPos(x, y, z), 0.02F);
		} else if (explosiveEMP.getRadius() == 50.0F) {
			onEMP(world, new BlockPos(x, y, z), 0.70F);
		} else {
			WarpDrive.logger.warn(String.format("EMP received @ DIM%d (%d %d %d) from %s with energy %d and unsupported radius %.1f",
			                                    world.provider.getDimension(), x, y, z,
			                                    explosiveEMP, explosiveEMP.getEnergy(), explosiveEMP.getRadius()));
			onEMP(world, new BlockPos(x, y, z), 0.02F);
		}
	}
	
	@Override
	@Optional.Method(modid = "icbmclassic")
	public void onEMP(World world, int x, int y, int z, resonant.api.explosion.IExplosion explosiveEMP) {
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info(String.format("EMP received @ DIM%d (%d %d %d) from %s with energy %d and radius %.1f",
			                                    world.provider.getDimension(), x, y, z,
			                                    explosiveEMP, explosiveEMP.getEnergy(), explosiveEMP.getRadius()));
		}
		// EMP tower = 3k Energy, 60 radius
		// EMP explosive = 3k Energy, 50 radius
		if (explosiveEMP.getRadius() == 60.0F) {// compensate tower stacking effect
			onEMP(world, new BlockPos(x, y, z), 0.02F);
		} else if (explosiveEMP.getRadius() == 50.0F) {
			onEMP(world, new BlockPos(x, y, z), 0.70F);
		} else {
			WarpDrive.logger.warn(String.format("EMP received @ DIM%d (%d %d %d) from %s with energy %d and unsupported radius %.1f",
			                                    world.provider.getDimension(), x, y, z,
			                                    explosiveEMP, explosiveEMP.getEnergy(), explosiveEMP.getRadius()));
			onEMP(world, new BlockPos(x, y, z), 0.02F);
		}
	}
	
	public void onEMP(World world, final BlockPos blockPos, final float efficiency) {
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityAbstractEnergy) {
			TileEntityAbstractEnergy tileEntityAbstractEnergy = (TileEntityAbstractEnergy) tileEntity;
			if (tileEntityAbstractEnergy.energy_getMaxStorage() > 0) {
				tileEntityAbstractEnergy.energy_consume(Math.round(tileEntityAbstractEnergy.energy_getEnergyStored() * efficiency), false);
			}
		}
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return 1;
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		switch (getTier(itemStack)) {
			case 0:	return EnumRarity.EPIC;
			case 1:	return EnumRarity.COMMON;
			case 2:	return EnumRarity.UNCOMMON;
			case 3:	return EnumRarity.RARE;
			default: return rarity;
		}
	}
}
