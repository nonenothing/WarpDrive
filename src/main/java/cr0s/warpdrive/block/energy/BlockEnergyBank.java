package cr0s.warpdrive.block.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.item.ItemComponent;
import cr0s.warpdrive.item.ItemTuningFork;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import java.util.List;

public class BlockEnergyBank extends BlockAbstractContainer {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	public BlockEnergyBank() {
		super(Material.iron);
		setBlockName("warpdrive.energy.EnergyBank.");
		hasSubBlocks = true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityEnergyBank((byte)(metadata % 4));
	}
	
	@Override
	public int damageDropped(int metadata) {
		return metadata;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativeTab, List list) {
		for (byte tier = 0; tier < 4; tier++) {
			ItemStack itemStack = new ItemStack(item, 1, tier);
			list.add(itemStack);
			if (tier > 0) {
				itemStack = new ItemStack(item, 1, tier);
				NBTTagCompound nbtTagCompound = new NBTTagCompound();
				nbtTagCompound.setByte("tier", tier);
				nbtTagCompound.setInteger("energy", WarpDriveConfig.ENERGY_BANK_MAX_ENERGY_STORED[tier - 1]);
				itemStack.setTagCompound(nbtTagCompound);
				list.add(itemStack);
			}
		}
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[12];
		icons[ 0] = iconRegister.registerIcon("warpdrive:energy/energyBankCreativeDisabled");
		icons[ 1] = iconRegister.registerIcon("warpdrive:energy/energyBankCreativeInput");
		icons[ 2] = iconRegister.registerIcon("warpdrive:energy/energyBankCreativeOutput");
		icons[ 3] = iconRegister.registerIcon("warpdrive:energy/energyBankBasicDisabled");
		icons[ 4] = iconRegister.registerIcon("warpdrive:energy/energyBankBasicInput");
		icons[ 5] = iconRegister.registerIcon("warpdrive:energy/energyBankBasicOutput");
		icons[ 6] = iconRegister.registerIcon("warpdrive:energy/energyBankAdvancedDisabled");
		icons[ 7] = iconRegister.registerIcon("warpdrive:energy/energyBankAdvancedInput");
		icons[ 8] = iconRegister.registerIcon("warpdrive:energy/energyBankAdvancedOutput");
		icons[ 9] = iconRegister.registerIcon("warpdrive:energy/energyBankSuperiorDisabled");
		icons[10] = iconRegister.registerIcon("warpdrive:energy/energyBankSuperiorInput");
		icons[11] = iconRegister.registerIcon("warpdrive:energy/energyBankSuperiorOutput");
	}
	
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity == null || !(tileEntity instanceof TileEntityEnergyBank)) {
			return icons[3];
		}
		
		return icons[3 * ((TileEntityEnergyBank) tileEntity).getTier() + ((TileEntityEnergyBank) tileEntity).getMode(EnumFacing.getFront(side))];
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		return icons[metadata * 3 + (side == 1 ? 1 : 2)];
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		if (itemStack == null || itemStack.getItem() != Item.getItemFromBlock(this)) {
			return 1;
		}
		NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
		if (nbtTagCompound != null && nbtTagCompound.hasKey("tier")) {
			return nbtTagCompound.getByte("tier");
		} else {
			return (byte) itemStack.getItemDamage();
		}
	}
		
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityEnergyBank)) {
			return false;
		}
		TileEntityEnergyBank tileEntityEnergyBank = (TileEntityEnergyBank) tileEntity;
		ItemStack itemStackHeld = entityPlayer.getHeldItem();
		EnumFacing facing = EnumFacing.getFront(side);
		
		if (itemStackHeld != null && itemStackHeld.getItem() instanceof ItemTuningFork) {
			if (entityPlayer.isSneaking()) {
				tileEntityEnergyBank.setMode(facing, (byte)((tileEntityEnergyBank.getMode(facing) + 2) % 3));
			} else {
				tileEntityEnergyBank.setMode(facing, (byte)((tileEntityEnergyBank.getMode(facing) + 1) % 3));
			}
			ItemStack itemStack = new ItemStack(Item.getItemFromBlock(this), 1, world.getBlockMetadata(x, y, z));
			switch (tileEntityEnergyBank.getMode(facing)) {
				case TileEntityEnergyBank.MODE_INPUT:
					WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
						StatCollector.translateToLocalFormatted(itemStack.getUnlocalizedName() + ".name"))
					    + StatCollector.translateToLocalFormatted("warpdrive.energy.side.changedToInput", facing.name()));
					return true;
				case TileEntityEnergyBank.MODE_OUTPUT:
					WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
						StatCollector.translateToLocalFormatted(itemStack.getUnlocalizedName() + ".name"))
					    + StatCollector.translateToLocalFormatted("warpdrive.energy.side.changedToOutput", facing.name()));
					return true;
				case TileEntityEnergyBank.MODE_DISABLED:
				default:
					WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
						StatCollector.translateToLocalFormatted(itemStack.getUnlocalizedName() + ".name"))
					    + StatCollector.translateToLocalFormatted("warpdrive.energy.side.changedToDisabled", facing.name()));
					return true;
			}
		}
		
		EnumComponentType enumComponentType = null;
		if (itemStackHeld != null && itemStackHeld.getItem() instanceof ItemComponent) {
			enumComponentType = EnumComponentType.get(itemStackHeld.getItemDamage());
		}
		
		// sneaking with an empty hand or an upgrade item in hand to dismount current upgrade
		if (entityPlayer.isSneaking()) {
			// using an upgrade item or an empty means dismount upgrade
			if (itemStackHeld == null || enumComponentType != null) {
				// find a valid upgrade to dismount
				if (itemStackHeld == null || !tileEntityEnergyBank.hasUpgrade(enumComponentType)) {
					enumComponentType = (EnumComponentType)tileEntityEnergyBank.getFirstUpgradeOfType(EnumComponentType.class, null);
				}
				
				if (enumComponentType == null) {
					// no more upgrades to dismount
					WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.noUpgradeToDismount"));
					return true;
				}
				
				if (!entityPlayer.capabilities.isCreativeMode) {
					// dismount the current upgrade item
					ItemStack itemStackDrop = ItemComponent.getItemStackNoCache(enumComponentType, 1);
					EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.delayBeforeCanPickup = 0;
					world.spawnEntityInWorld(entityItem);
				}
				
				tileEntityEnergyBank.dismountUpgrade(enumComponentType);
				// upgrade dismounted
				WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.dismounted", enumComponentType.name()));
				return false;
				
			}
			
		} else if (itemStackHeld == null) {// no sneaking and no item in hand => show status
			WarpDrive.addChatMessage(entityPlayer, tileEntityEnergyBank.getStatus());
			return true;
			
		} else if (enumComponentType != null) {// no sneaking and an upgrade in hand => mounting an upgrade
			// validate type
			if (tileEntityEnergyBank.getUpgradeMaxCount(enumComponentType) <= 0) {
				// invalid upgrade type
				WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.invalidUpgrade"));
				return true;
			}
			if (!tileEntityEnergyBank.canUpgrade(enumComponentType)) {
				// too many upgrades
				WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.tooManyUpgrades",
					tileEntityEnergyBank.getUpgradeMaxCount(enumComponentType)));
				return true;
			}
			
			if (!entityPlayer.capabilities.isCreativeMode) {
				// validate quantity
				if (itemStackHeld.stackSize < 1) {
					// not enough upgrade items
					WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.notEnoughUpgrades"));
					return true;
				}
				
				// update player inventory
				itemStackHeld.stackSize -= 1;
			}
			
			// mount the new upgrade item
			tileEntityEnergyBank.mountUpgrade(enumComponentType);
			// upgrade mounted
			WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.mounted", enumComponentType.name()));
		}
		
		return false;
	}
}