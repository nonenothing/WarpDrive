package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IWarpTool;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.item.ItemComponent;
import ic2.api.energy.tile.IExplosionPowerOverride;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.InterfaceList({
	@Optional.Interface(iface = "ic2.api.energy.tile.IExplosionPowerOverride", modid = "IC2")
})
public class BlockEnergyBank extends BlockAbstractContainer implements IExplosionPowerOverride {
	
	public BlockEnergyBank(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.energy.EnergyBank.");
		hasSubBlocks = true;
		GameRegistry.registerTileEntity(TileEntityEnergyBank.class, WarpDrive.PREFIX + registryName);
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityEnergyBank((byte)(metadata % 4));
	}
	
	@Override
	public int damageDropped(IBlockState blockState) {
		return getMetaFromState(blockState);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(@Nonnull Item item, CreativeTabs creativeTab, List<ItemStack> list) {
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
	
	// IExplosionPowerOverride overrides
	@Override
	public boolean shouldExplode() {
		return false;
	}
	
	@Override
	public float getExplosionPower(int tier, float defaultPower) {
		return defaultPower;
	}
	
	
	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer, EnumHand hand, @Nullable ItemStack itemStackHeld, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (!(tileEntity instanceof TileEntityEnergyBank)) {
			return false;
		}
		TileEntityEnergyBank tileEntityEnergyBank = (TileEntityEnergyBank) tileEntity;
		
		if (itemStackHeld != null && itemStackHeld.getItem() instanceof IWarpTool) {
			if (entityPlayer.isSneaking()) {
				tileEntityEnergyBank.setMode(facing, (byte)((tileEntityEnergyBank.getMode(facing) + 2) % 3));
			} else {
				tileEntityEnergyBank.setMode(facing, (byte)((tileEntityEnergyBank.getMode(facing) + 1) % 3));
			}
			ItemStack itemStack = new ItemStack(Item.getItemFromBlock(this), 1, getMetaFromState(blockState));
			switch (tileEntityEnergyBank.getMode(facing)) {
				case TileEntityEnergyBank.MODE_INPUT:
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.guide.prefix")
					    .appendSibling(new TextComponentTranslation(itemStack.getUnlocalizedName() + ".name"))
					    .appendSibling(new TextComponentTranslation("warpdrive.energy.side.changedToInput", facing.name())) );
					return true;
				case TileEntityEnergyBank.MODE_OUTPUT:
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.guide.prefix")
					    .appendSibling(new TextComponentTranslation(itemStack.getUnlocalizedName() + ".name"))
					    .appendSibling(new TextComponentTranslation("warpdrive.energy.side.changedToOutput", facing.name())) );
					return true;
				case TileEntityEnergyBank.MODE_DISABLED:
				default:
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.guide.prefix")
					    .appendSibling(new TextComponentTranslation(itemStack.getUnlocalizedName() + ".name"))
					    .appendSibling(new TextComponentTranslation("warpdrive.energy.side.changedToDisabled", facing.name())) );
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
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.noUpgradeToDismount"));
					return true;
				}
				
				if (!entityPlayer.capabilities.isCreativeMode) {
					// dismount the current upgrade item
					ItemStack itemStackDrop = ItemComponent.getItemStackNoCache(enumComponentType, 1);
					EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.setNoPickupDelay();
					world.spawnEntityInWorld(entityItem);
				}
				
				tileEntityEnergyBank.dismountUpgrade(enumComponentType);
				// upgrade dismounted
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.dismounted", enumComponentType.name()));
				return false;
				
			}
			
		} else if (itemStackHeld == null) {// no sneaking and no item in hand => show status
			Commons.addChatMessage(entityPlayer, tileEntityEnergyBank.getStatus());
			return true;
			
		} else if (enumComponentType != null) {// no sneaking and an upgrade in hand => mounting an upgrade
			// validate type
			if (tileEntityEnergyBank.getUpgradeMaxCount(enumComponentType) <= 0) {
				// invalid upgrade type
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.invalidUpgrade"));
				return true;
			}
			if (!tileEntityEnergyBank.canUpgrade(enumComponentType)) {
				// too many upgrades
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.tooManyUpgrades",
					tileEntityEnergyBank.getUpgradeMaxCount(enumComponentType)));
				return true;
			}
			
			if (!entityPlayer.capabilities.isCreativeMode) {
				// validate quantity
				if (itemStackHeld.stackSize < 1) {
					// not enough upgrade items
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.notEnoughUpgrades"));
					return true;
				}
				
				// update player inventory
				itemStackHeld.stackSize -= 1;
			}
			
			// mount the new upgrade item
			tileEntityEnergyBank.mountUpgrade(enumComponentType);
			// upgrade mounted
			Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.mounted", enumComponentType.name()));
		}
		
		return false;
	}
}