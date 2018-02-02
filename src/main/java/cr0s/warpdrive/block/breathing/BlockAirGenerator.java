package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IAirContainerItem;
import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.BlockProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockAirGenerator extends BlockAbstractContainer {
	
	public BlockAirGenerator(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.breathing.air_generator");
		GameRegistry.registerTileEntity(TileEntityAirGenerator.class, WarpDrive.PREFIX + registryName);
		
		setDefaultState(getDefaultState().withProperty(BlockProperties.ACTIVE, false));
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
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(BlockProperties.FACING).getIndex() + (blockState.getValue(BlockProperties.ACTIVE) ? 8 : 0);
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityAirGenerator();
	}
	
	@Nonnull
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int metadata, EntityLivingBase entityLiving) {
		EnumFacing enumFacing = BlockAbstractBase.getFacingFromEntity(pos, entityLiving).getOpposite();
		return this.getDefaultState().withProperty(BlockProperties.FACING, enumFacing);
	}
	
	@Override
	public boolean onBlockActivated(final World world, final BlockPos blockPos, final IBlockState blockState,
	                                final EntityPlayer entityPlayer, final EnumHand hand, @Nullable final ItemStack itemStackHeld,
	                                final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityAirGenerator) {
			TileEntityAirGenerator airGenerator = (TileEntityAirGenerator)tileEntity;
			if (itemStackHeld == null) {
				Commons.addChatMessage(entityPlayer, airGenerator.getStatus());
				return true;
			} else {
				Item itemHeld = itemStackHeld.getItem();
				if (itemHeld instanceof IAirContainerItem) {
					IAirContainerItem airCanister = (IAirContainerItem) itemHeld;
					if (airCanister.canContainAir(itemStackHeld) && airGenerator.energy_consume(WarpDriveConfig.BREATHING_ENERGY_PER_CANISTER, true)) {
						entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
						ItemStack toAdd = airCanister.getFullAirContainer(itemStackHeld);
						if (toAdd != null) {
							if (!entityPlayer.inventory.addItemStackToInventory(toAdd)) {
								EntityItem entityItem = new EntityItem(entityPlayer.worldObj, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, toAdd);
								entityPlayer.worldObj.spawnEntityInWorld(entityItem);
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
