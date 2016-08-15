package cr0s.warpdrive.block.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.item.ItemTuningFork;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
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
		icons = new IIcon[3];
		icons[0] = iconRegister.registerIcon("warpdrive:energy/energyBank");
		icons[1] = iconRegister.registerIcon("warpdrive:energy/energyBankInput");
		icons[2] = iconRegister.registerIcon("warpdrive:energy/energyBankOutput");
	}
	
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity == null || !(tileEntity instanceof TileEntityEnergyBank)) {
			return icons[0];
		}
		
		return icons[((TileEntityEnergyBank) tileEntity).getMode(EnumFacing.getFront(side))];
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		return icons[side == 1 ? 1 : 2];
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
		
		if (itemStackHeld == null) {
			WarpDrive.addChatMessage(entityPlayer, tileEntityEnergyBank.getStatus());
			return true;
			
		} else if (itemStackHeld.getItem() instanceof ItemTuningFork) {
			tileEntityEnergyBank.setMode(facing, (byte)((tileEntityEnergyBank.getMode(facing) + 1) % 3));
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
		
		return false;
	}
}