package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.api.IWarpTool;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.config.WarpDriveConfig;

import java.util.List;

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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEnergyBank extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	public BlockEnergyBank() {
		super(Material.iron);
		setBlockName("warpdrive.energy.EnergyBank.");
		hasSubBlocks = true;
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityEnergyBank((byte)(metadata % 4));
	}
	
	@Override
	public int damageDropped(final int metadata) {
		return metadata;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(final Item item, final CreativeTabs creativeTab, final List list) {
		for (byte tier = 0; tier < 4; tier++) {
			ItemStack itemStack = new ItemStack(item, 1, tier);
			list.add(itemStack);
			if (tier > 0) {
				itemStack = new ItemStack(item, 1, tier);
				final NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setByte("tier", tier);
				tagCompound.setInteger("energy", WarpDriveConfig.ENERGY_BANK_MAX_ENERGY_STORED[tier - 1]);
				itemStack.setTagCompound(tagCompound);
				list.add(itemStack);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		icons = new IIcon[12];
		icons[ 0] = iconRegister.registerIcon("warpdrive:energy/energy_bank_creative-disabled");
		icons[ 1] = iconRegister.registerIcon("warpdrive:energy/energy_bank_creative-input");
		icons[ 2] = iconRegister.registerIcon("warpdrive:energy/energy_bank_creative-output");
		icons[ 3] = iconRegister.registerIcon("warpdrive:energy/energy_bank_basic-disabled");
		icons[ 4] = iconRegister.registerIcon("warpdrive:energy/energy_bank_basic-input");
		icons[ 5] = iconRegister.registerIcon("warpdrive:energy/energy_bank_basic-output");
		icons[ 6] = iconRegister.registerIcon("warpdrive:energy/energy_bank_advanced-disabled");
		icons[ 7] = iconRegister.registerIcon("warpdrive:energy/energy_bank_advanced-input");
		icons[ 8] = iconRegister.registerIcon("warpdrive:energy/energy_bank_advanced-output");
		icons[ 9] = iconRegister.registerIcon("warpdrive:energy/energy_bank_superior-disabled");
		icons[10] = iconRegister.registerIcon("warpdrive:energy/energy_bank_superior-input");
		icons[11] = iconRegister.registerIcon("warpdrive:energy/energy_bank_superior-output");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		final TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (tileEntity == null || !(tileEntity instanceof TileEntityEnergyBank)) {
			return icons[3];
		}
		
		return icons[3 * ((TileEntityEnergyBank) tileEntity).getTier() + ((TileEntityEnergyBank) tileEntity).getMode(EnumFacing.getFront(side))];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		return icons[(metadata * 3 + (side == 1 ? 1 : 2)) % icons.length];
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		if (itemStack == null || itemStack.getItem() != Item.getItemFromBlock(this)) {
			return 1;
		}
		final NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound != null && tagCompound.hasKey("tier")) {
			return tagCompound.getByte("tier");
		} else {
			return (byte) itemStack.getItemDamage();
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityEnergyBank)) {
			return false;
		}
		final TileEntityEnergyBank tileEntityEnergyBank = (TileEntityEnergyBank) tileEntity;
		final ItemStack itemStackHeld = entityPlayer.getHeldItem();
		final EnumFacing facing = EnumFacing.getFront(side);
		
		if ( itemStackHeld != null
		  && itemStackHeld.getItem() instanceof IWarpTool ) {
			if (entityPlayer.isSneaking()) {
				tileEntityEnergyBank.setMode(facing, (byte)((tileEntityEnergyBank.getMode(facing) + 2) % 3));
			} else {
				tileEntityEnergyBank.setMode(facing, (byte)((tileEntityEnergyBank.getMode(facing) + 1) % 3));
			}
			final ItemStack itemStack = new ItemStack(Item.getItemFromBlock(this), 1, world.getBlockMetadata(x, y, z));
			switch (tileEntityEnergyBank.getMode(facing)) {
				case TileEntityEnergyBank.MODE_INPUT:
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
						StatCollector.translateToLocalFormatted(itemStack.getUnlocalizedName() + ".name"))
					    + StatCollector.translateToLocalFormatted("warpdrive.energy.side.changedToInput", facing.name()));
					return true;
				case TileEntityEnergyBank.MODE_OUTPUT:
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
						StatCollector.translateToLocalFormatted(itemStack.getUnlocalizedName() + ".name"))
					    + StatCollector.translateToLocalFormatted("warpdrive.energy.side.changedToOutput", facing.name()));
					return true;
				case TileEntityEnergyBank.MODE_DISABLED:
				default:
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
						StatCollector.translateToLocalFormatted(itemStack.getUnlocalizedName() + ".name"))
					    + StatCollector.translateToLocalFormatted("warpdrive.energy.side.changedToDisabled", facing.name()));
					return true;
			}
		}
		
		return super.onBlockActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
	}
}