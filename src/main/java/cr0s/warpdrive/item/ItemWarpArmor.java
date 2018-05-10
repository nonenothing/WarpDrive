package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBreathingHelmet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ItemWarpArmor extends ItemArmor implements IBreathingHelmet {
	
	public static final String[] suffixes = { "helmet", "chestplate", "leggings", "boots" };
	
	public ItemWarpArmor(final ArmorMaterial armorMaterial, final int renderIndex, final int armorPart) {
		super(armorMaterial, renderIndex, armorPart);
		setUnlocalizedName("warpdrive.armor." + suffixes[armorPart]);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setTextureName("warpdrive:warp_armor_" + suffixes[armorPart]);
	}
	
	@Override
	public String getArmorTexture(final ItemStack itemStack, final Entity entity, final int slot, final String renderingType) {
		return "warpdrive:textures/armor/warp_armor_" + (this.armorType == 2 ? 2 : 1) + ".png";
	}
	
	@Override
	public boolean canBreath(final EntityLivingBase entityLivingBase) {
		return armorType == 0;
	}
}