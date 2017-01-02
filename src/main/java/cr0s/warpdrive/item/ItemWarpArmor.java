package cr0s.warpdrive.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import cr0s.warpdrive.WarpDrive;

public class ItemWarpArmor extends ItemArmor {
	public static final String[] suffixes = { "helmet", "chestplate", "leggings", "boots" };
	
	public ItemWarpArmor(ArmorMaterial armorMaterial, int renderIndex, int armorPart) {
		super(armorMaterial, renderIndex, armorPart);
		setUnlocalizedName("warpdrive.armor." + suffixes[armorPart]);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setTextureName("warpdrive:warp_armor_" + suffixes[armorPart]);
	}
	
	@Override
	public String getArmorTexture(ItemStack itemStack, Entity entity, int slot, String renderingType) {
		return "warpdrive:textures/armor/warp_armor_" + (this.armorType == 2 ? 2 : 1) + ".png";
	}
}