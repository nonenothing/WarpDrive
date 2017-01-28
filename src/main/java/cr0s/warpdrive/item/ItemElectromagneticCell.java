package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IParticleContainerItem;
import cr0s.warpdrive.api.Particle;
import cr0s.warpdrive.api.ParticleRegistry;
import cr0s.warpdrive.api.ParticleStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemElectromagneticCell extends ItemAbstractBase implements IParticleContainerItem {
	
	public ItemElectromagneticCell(final String registryName) {
		super(registryName);
		setMaxDamage(0);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.atomic.electromagnetic_cell");
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
	public void getSubItems(@Nonnull Item item, CreativeTabs creativeTab, List<ItemStack> list) {
		if (WarpDrive.isDev) {
			for (int metadata = 0; metadata < 32; metadata++) {
				list.add(new ItemStack(item, 1, metadata));
			}
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
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List<String> list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		if (!(itemStack.getItem() instanceof  ItemElectromagneticCell)) {
			WarpDrive.logger.error("Invalid ItemStack passed, expecting ItemElectromagneticCell: " + itemStack);
			return;
		}
		final ItemElectromagneticCell itemElectromagneticCell = (ItemElectromagneticCell) itemStack.getItem();
		final ParticleStack particleStack = itemElectromagneticCell.getParticle(itemStack);
		String tooltip;
		if (particleStack == null || particleStack.getParticle() == null) {
			tooltip = new TextComponentTranslation("item.warpdrive.atomic.electromagnetic_cell.tooltip.empty").getFormattedText();
			WarpDrive.addTooltip(list, tooltip);
			
		} else {
			final Particle particle = particleStack.getParticle();
			
			tooltip = new TextComponentTranslation("item.warpdrive.atomic.electromagnetic_cell.tooltip.filled",
				particleStack.amount, particle.getLocalizedName()).getFormattedText();
			WarpDrive.addTooltip(list, tooltip);
			
			String particleTooltip = particle.getLocalizedTooltip();
			if (!particleTooltip.isEmpty()) {
				WarpDrive.addTooltip(list, particleTooltip);
			}
		}
	}
}
