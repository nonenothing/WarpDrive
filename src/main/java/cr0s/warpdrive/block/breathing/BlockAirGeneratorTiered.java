package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.api.IAirContainerItem;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.config.WarpDriveConfig;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAirGeneratorTiered extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	private static final int ICON_INACTIVE_SIDE = 0;
	private static final int ICON_BOTTOM = 1;
	private static final int ICON_SIDE_ACTIVATED = 2;
	
	protected byte tier;
	
	public BlockAirGeneratorTiered(final byte tier) {
		super(Material.iron);
		this.tier = tier;
		isRotating = true;
		setBlockName("warpdrive.breathing.air_generator" + tier);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[3];
		iconBuffer[ICON_INACTIVE_SIDE] = iconRegister.registerIcon("warpdrive:breathing/air_generator-side_inactive");
		iconBuffer[ICON_BOTTOM] = iconRegister.registerIcon("warpdrive:breathing/air_generator-connection");
		iconBuffer[ICON_SIDE_ACTIVATED] = iconRegister.registerIcon("warpdrive:breathing/air_generator-side_active");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		if (side == (metadata & 7)) {
			if ((metadata & 8) == 0) { // Inactive state
				return iconBuffer[ICON_INACTIVE_SIDE];
			} else {
				return iconBuffer[ICON_SIDE_ACTIVATED];
			}
		}
		
		return iconBuffer[ICON_BOTTOM];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		if (side == 3) {
			return iconBuffer[ICON_SIDE_ACTIVATED];
		}
		
		return iconBuffer[ICON_BOTTOM];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityAirGeneratorTiered();
	}
		
	@Override
	public int quantityDropped(final Random random) {
		return 1;
	}
	
	@Override
	public Item getItemDropped(final int metadata, final Random random, final int fortune) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return tier;
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
	                                final EntityPlayer entityPlayer, final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityAirGeneratorTiered) {
			final TileEntityAirGeneratorTiered airGenerator = (TileEntityAirGeneratorTiered)tileEntity;
			final ItemStack itemStackHeld = entityPlayer.getHeldItem();
			if (itemStackHeld == null) {
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
								final EntityItem entityItem = new EntityItem(entityPlayer.worldObj, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, toAdd);
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
