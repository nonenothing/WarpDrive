package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBreathingHelmet;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ItemWarpArmor extends ItemArmor implements IBreathingHelmet {
	
	public static final String[] suffixes = {  "boots", "leggings", "chestplate", "helmet" };
	
	public ItemWarpArmor(final String registryName, final ArmorMaterial armorMaterial, final int renderIndex, final EntityEquipmentSlot entityEquipmentSlot) {
		super(armorMaterial, renderIndex, entityEquipmentSlot);
		setUnlocalizedName("warpdrive.armor." + suffixes[entityEquipmentSlot.getIndex()]);
		setRegistryName(registryName);
		setCreativeTab(WarpDrive.creativeTabMain);
		WarpDrive.register(this);
	}
	
	@Nonnull
	@Override
	public String getArmorTexture(final ItemStack itemStack, final Entity entity, final EntityEquipmentSlot slot, final String renderingType) {
		return "warpdrive:textures/armor/warp_armor_" + (armorType == EntityEquipmentSlot.LEGS ? 2 : 1) + ".png";
	}
	
	@Override
	public boolean canBreath(final EntityLivingBase entityLivingBase) {
		return armorType == EntityEquipmentSlot.HEAD;
	}
}