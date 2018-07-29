package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBreathingHelmet;
import cr0s.warpdrive.api.IItemBase;
import cr0s.warpdrive.client.ClientProxy;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemWarpArmor extends ItemArmor implements IItemBase, IBreathingHelmet {
	
	public static final String[] suffixes = {  "boots", "leggings", "chestplate", "helmet" };
	
	public ItemWarpArmor(final String registryName, final ArmorMaterial armorMaterial, final int renderIndex, final EntityEquipmentSlot entityEquipmentSlot) {
		super(armorMaterial, renderIndex, entityEquipmentSlot);
		
		setTranslationKey("warpdrive.armor." + suffixes[entityEquipmentSlot.getIndex()]);
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
	public void onEntityExpireEvent(final EntityItem entityItem, final ItemStack itemStack) {
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void modelInitialisation() {
		ClientProxy.modelInitialisation(this);
	}
	
	@Nonnull
	@SideOnly(Side.CLIENT)
	@Override
	public ModelResourceLocation getModelResourceLocation(final ItemStack itemStack) {
		return ClientProxy.getModelResourceLocation(itemStack);
	}
	
	@Override
	public boolean canBreath(final EntityLivingBase entityLivingBase) {
		return armorType == EntityEquipmentSlot.HEAD;
	}
}