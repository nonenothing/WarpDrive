package cr0s.warpdrive.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IParticleContainerItem;
import cr0s.warpdrive.api.Particle;
import cr0s.warpdrive.api.ParticleRegistry;
import cr0s.warpdrive.api.ParticleStack;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemElectromagneticCell extends Item implements IParticleContainerItem {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons = new IIcon[31];
	
	public ItemElectromagneticCell() {
		super();
		setMaxDamage(0);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.atomic.electromagnetic_cell");
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icons[ 0] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-empty");
		icons[ 1] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-blue-20");
		icons[ 2] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-blue-40");
		icons[ 3] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-blue-60");
		icons[ 4] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-blue-80");
		icons[ 5] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-blue-100");
		icons[ 6] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-blue-full");
		icons[ 7] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-green-20");
		icons[ 8] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-green-40");
		icons[ 9] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-green-60");
		icons[10] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-green-80");
		icons[11] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-green-100");
		icons[12] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-green-full");
		icons[13] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-pink-20");
		icons[14] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-pink-40");
		icons[15] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-pink-60");
		icons[16] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-pink-80");
		icons[17] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-pink-100");
		icons[18] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-pink-full");
		icons[19] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-red-20");
		icons[20] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-red-40");
		icons[21] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-red-60");
		icons[22] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-red-80");
		icons[23] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-red-100");
		icons[24] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-red-full");
		icons[25] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-yellow-20");
		icons[26] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-yellow-40");
		icons[27] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-yellow-60");
		icons[28] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-yellow-80");
		icons[29] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-yellow-100");
		icons[30] = iconRegister.registerIcon("warpdrive:atomic/electromagnetic_cell-yellow-full");
	}
	
	@Override
	public IIcon getIconFromDamage(int damage) {
		return icons[damage % icons.length];
	}
	
	public static ItemStack getItemStackNoCache(final Particle particle, final int amount) {
		ItemStack itemStack = new ItemStack(WarpDrive.itemElectromagneticCell, 1, 0);
		ParticleStack particleStack = null;
		if (particle != null && amount != 0) {
			particleStack = new ParticleStack(particle, amount);
			NBTTagCompound tagCompound = new NBTTagCompound();
			tagCompound.setTag("particle", particleStack.writeToNBT(new NBTTagCompound()));
			itemStack.setTagCompound(tagCompound);
		}
		updateDamageLevel(itemStack, particleStack);
		return itemStack;
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list) {
		for(int metadata = 0; metadata < icons.length; metadata++) {
			list.add(new ItemStack(item, 1, metadata));
		}
		list.add(getItemStackNoCache(ParticleRegistry.ION, 100));
		list.add(getItemStackNoCache(ParticleRegistry.PROTON, 100));
		list.add(getItemStackNoCache(ParticleRegistry.ANTIMATTER, 100));
		list.add(getItemStackNoCache(ParticleRegistry.STRANGE_MATTER, 100));
		// list.add(getItemStackNoCache(ParticleRegistry.TACHYONS, 100));
	}
	
	private static int getDamageLevel(ItemStack itemStack, final ParticleStack particleStack) {
		if (!(itemStack.getItem() instanceof  ItemElectromagneticCell)) {
			WarpDrive.logger.error("Invalid ItemStack passed, expecting ItemElectromagneticCell: " + itemStack);
			return itemStack.getItemDamage();
		}
		if (particleStack == null || particleStack.getParticle() == null) {
			return 0;
		}
		final ItemElectromagneticCell itemElectromagneticCell = (ItemElectromagneticCell) itemStack.getItem();
		final int type = particleStack.getParticle().getColorIndex() % 5;
		final double ratio = particleStack.amount / (double) itemElectromagneticCell.getCapacity(itemStack);
		final int offset = (ratio < 0.2) ? 0 : (ratio < 0.4) ? 1 : (ratio < 0.6) ? 2 : (ratio < 0.8) ? 3 : (ratio < 1.0) ? 4 : 5;
		return (1 + type * 6 + offset);
	}
	
	private static void updateDamageLevel(ItemStack itemStack, final ParticleStack particleStack) {
		itemStack.setItemDamage(getDamageLevel(itemStack, particleStack));
	}
	
	@Override
	public ParticleStack getParticle(ItemStack itemStack) {
		if (itemStack.getItem() != this || !itemStack.hasTagCompound()) {
			return null;
		}
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (!tagCompound.hasKey("particle")) {
			return null;
		}
		return ParticleStack.loadFromNBT(tagCompound.getCompoundTag("particle"));
	}
	
	@Override
	public int getCapacity(ItemStack container) {
		return 1000;
	}
	
	@Override
	public int fill(ItemStack itemStack, ParticleStack resource, boolean doFill) {
		ParticleStack particleStack = getParticle(itemStack);
		if (particleStack == null || particleStack.getParticle() == null) {
			particleStack = new ParticleStack(resource.getParticle(), 0);
		} else if (!particleStack.containsParticle(resource) || particleStack.amount >= getCapacity(itemStack)) {
			return 0;
		}
		int consumable = Math.min(resource.amount, getCapacity(itemStack) - particleStack.amount);
		if (!doFill) {
			particleStack.amount += consumable;
			
			NBTTagCompound tagCompound = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
			tagCompound.setTag("particle", particleStack.writeToNBT(new NBTTagCompound()));
			updateDamageLevel(itemStack, particleStack);
		}
		return consumable;
	}
	
	@Override
	public ParticleStack drain(ItemStack container, int maxDrain, boolean doDrain) {
		return null;
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		if (!(itemStack.getItem() instanceof  ItemElectromagneticCell)) {
			WarpDrive.logger.error("Invalid ItemStack passed, expecting ItemElectromagneticCell: " + itemStack);
			return;
		}
		final ItemElectromagneticCell itemElectromagneticCell = (ItemElectromagneticCell) itemStack.getItem();
		final ParticleStack particleStack = itemElectromagneticCell.getParticle(itemStack);
		String tooltip;
		if (particleStack == null || particleStack.getParticle() == null) {
			tooltip = StatCollector.translateToLocalFormatted("item.warpdrive.atomic.electromagnetic_cell.tooltip.empty");
			WarpDrive.addTooltip(list, tooltip);
			
		} else {
			final Particle particle = particleStack.getParticle();
			
			tooltip = StatCollector.translateToLocalFormatted("item.warpdrive.atomic.electromagnetic_cell.tooltip.filled",
				particleStack.amount, particle.getLocalizedName());
			WarpDrive.addTooltip(list, tooltip);
			
			String particleTooltip = particle.getLocalizedTooltip();
			if (!particleTooltip.isEmpty()) {
				WarpDrive.addTooltip(list, particleTooltip);
			}
		}
	}
}
