package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IAirContainerItem;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumTier;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAirGeneratorTiered extends BlockAbstractContainer {
	
	public BlockAirGeneratorTiered(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier, Material.IRON);
		
		setUnlocalizedName("warpdrive.breathing.air_generator." + enumTier.getName());
		registerTileEntity(TileEntityAirGeneratorTiered.class, new ResourceLocation(WarpDrive.MODID, registryName));
		
		setDefaultState(getDefaultState()
		                .withProperty(BlockProperties.FACING, EnumFacing.DOWN)
		                .withProperty(BlockProperties.ACTIVE, false));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockProperties.FACING, BlockProperties.ACTIVE);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
		       .withProperty(BlockProperties.FACING, EnumFacing.getFront(metadata & 7))
		       .withProperty(BlockProperties.ACTIVE, (metadata & 8) != 0);
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(BlockProperties.FACING).getIndex() + (blockState.getValue(BlockProperties.ACTIVE) ? 8 : 0);
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityAirGeneratorTiered(enumTier);
	}
	
	@Override
	public boolean onBlockActivated(final World world, final BlockPos blockPos, final IBlockState blockState,
	                                final EntityPlayer entityPlayer, final EnumHand enumHand,
	                                final EnumFacing enumFacing, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (enumHand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		// get context
		final ItemStack itemStackHeld = entityPlayer.getHeldItem(enumHand);
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityAirGeneratorTiered) {
			final TileEntityAirGeneratorTiered airGenerator = (TileEntityAirGeneratorTiered) tileEntity;
			if (itemStackHeld.isEmpty()) {
				Commons.addChatMessage(entityPlayer, airGenerator.getStatus());
				return true;
			} else {
				final Item itemHeld = itemStackHeld.getItem();
				if (itemHeld instanceof IAirContainerItem) {
					final IAirContainerItem airCanister = (IAirContainerItem) itemHeld;
					if (airCanister.canContainAir(itemStackHeld) && airGenerator.energy_consume(WarpDriveConfig.BREATHING_ENERGY_PER_CANISTER, true)) {
						entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
						final ItemStack toAdd = airCanister.getFullAirContainer(itemStackHeld);
						if (toAdd != null) {
							if (!entityPlayer.inventory.addItemStackToInventory(toAdd)) {
								final EntityItem entityItem = new EntityItem(entityPlayer.world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, toAdd);
								entityPlayer.world.spawnEntity(entityItem);
							}
							((EntityPlayerMP)entityPlayer).sendContainerToPlayer(entityPlayer.inventoryContainer);
							airGenerator.energy_consume(WarpDriveConfig.BREATHING_ENERGY_PER_CANISTER, false);
						}
					}
				}
			}
		}
		
		return false;
	}
}
