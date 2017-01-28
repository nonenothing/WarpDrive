package cr0s.warpdrive.item;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import cr0s.warpdrive.WarpDrive;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class ItemWarpArmor extends ItemArmor {
	public static final String[] suffixes = { "helmet", "chestplate", "leggings", "boots" };
	
	public ItemWarpArmor(final String registryName, ArmorMaterial armorMaterial, int renderIndex, EntityEquipmentSlot entityEquipmentSlot) {
		super(armorMaterial, renderIndex, entityEquipmentSlot);
		setUnlocalizedName("warpdrive.armor." + suffixes[entityEquipmentSlot.getIndex()]);
		setRegistryName(registryName);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		GameRegistry.register(this);
	}
	
	@Nonnull
	@Override
	public String getArmorTexture(ItemStack itemStack, Entity entity, EntityEquipmentSlot slot, String renderingType) {
		return "warpdrive:textures/armor/warp_armor_" + (armorType == EntityEquipmentSlot.LEGS ? 2 : 1) + ".png";
	}
}