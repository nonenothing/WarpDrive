package cr0s.warpdrive.item;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.block.TileEntityAbstractBase;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemEnergyWrapper {
	
	// WarpDrive methods
	public static boolean isEnergyContainer(ItemStack itemStack) {
		boolean bResult = false;
		// IndustrialCraft2
		if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			bResult = IC2_isContainer(itemStack);
		}
		
		// Thermal Expansion
		if (!bResult && WarpDriveConfig.isCoFHCoreLoaded) {
			bResult = CoFH_isContainer(itemStack);
		}
		return bResult;
	}
	public static boolean canInput(ItemStack itemStack) {
		boolean bResult = false;
		// IndustrialCraft2
		if (WarpDriveConfig.isIndustrialCraft2Loaded && IC2_isContainer(itemStack)) {
			bResult = IC2_canInput(itemStack);
		}
		
		// Thermal Expansion
		if (!bResult && WarpDriveConfig.isCoFHCoreLoaded && CoFH_isContainer(itemStack)) {
			bResult = CoFH_canInput(itemStack);
		}
		return bResult;
	}
	public static boolean canOutput(ItemStack itemStack) {
		boolean bResult = false;
		// IndustrialCraft2
		if (WarpDriveConfig.isIndustrialCraft2Loaded && IC2_isContainer(itemStack)) {
			bResult = IC2_canOutput(itemStack);
		}
		
		// Thermal Expansion
		if (!bResult && WarpDriveConfig.isCoFHCoreLoaded && CoFH_isContainer(itemStack)) {
			bResult = CoFH_canOutput(itemStack);
		}
		return bResult;
	}
	public static int getEnergyStored(ItemStack itemStack) {
		// IndustrialCraft2
		if (WarpDriveConfig.isIndustrialCraft2Loaded && IC2_isContainer(itemStack)) {
			double amount_EU = TileEntityAbstractBase.clamp(0, IC2_getMaxEnergyStorage(itemStack), IC2_getEnergyStored(itemStack));
			return TileEntityAbstractEnergy.convertEUtoInternal_floor(amount_EU);
		}
		
		// Thermal Expansion
		if (WarpDriveConfig.isCoFHCoreLoaded && CoFH_isContainer(itemStack)) {
			int amount_RF = TileEntityAbstractBase.clamp(0, CoFH_getMaxEnergyStorage(itemStack), CoFH_getEnergyStored(itemStack));
			return TileEntityAbstractEnergy.convertRFtoInternal_floor(amount_RF);
		}
		return 0;
	}
	public static int getMaxEnergyStorage(ItemStack itemStack) {
		// IndustrialCraft2
		if (WarpDriveConfig.isIndustrialCraft2Loaded && IC2_isContainer(itemStack)) {
			double amount_EU = IC2_getMaxEnergyStorage(itemStack);
			return TileEntityAbstractEnergy.convertEUtoInternal_floor(amount_EU);
		}
		
		// Thermal Expansion
		if (WarpDriveConfig.isCoFHCoreLoaded && CoFH_isContainer(itemStack)) {
			int amount_RF = CoFH_getMaxEnergyStorage(itemStack);
			return TileEntityAbstractEnergy.convertRFtoInternal_floor(amount_RF);
		}
		return 0;
	}
	public static ItemStack consume(ItemStack itemStack, final int amount, final boolean simulate) {
		// IndustrialCraft2
		if (WarpDriveConfig.isIndustrialCraft2Loaded && IC2_isContainer(itemStack)) {
			double amount_EU = TileEntityAbstractEnergy.convertInternalToEU_ceil(amount);
			return IC2_consume(itemStack, amount_EU, simulate);
		}
		
		// Thermal Expansion
		if (WarpDriveConfig.isCoFHCoreLoaded && CoFH_isContainer(itemStack)) {
			int amount_RF = TileEntityAbstractEnergy.convertInternalToRF_ceil(amount);
			return CoFH_consume(itemStack, amount_RF, simulate);
		}
		return null;
	}
	public static ItemStack charge(ItemStack itemStack, final int amount, final boolean simulate) {
		// IndustrialCraft2
		if (WarpDriveConfig.isIndustrialCraft2Loaded && IC2_isContainer(itemStack)) {
			double amount_EU = TileEntityAbstractEnergy.convertInternalToEU_floor(amount);
			return IC2_charge(itemStack, amount_EU, simulate);
		}
		
		// Thermal Expansion
		if (WarpDriveConfig.isCoFHCoreLoaded && CoFH_isContainer(itemStack)) {
			int amount_RF = TileEntityAbstractEnergy.convertInternalToRF_floor(amount);
			return CoFH_charge(itemStack, amount_RF, simulate);
		}
		return null;
	}
	
	// IndustrialCraft IElectricItem interface
	@Optional.Method(modid = "IC2")
	private static IElectricItemManager IC2_getManager(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item == null) {
			return null;
		}
		if (item instanceof ISpecialElectricItem) {
			return ((ISpecialElectricItem) item).getManager(itemStack);
		}
		if (item instanceof IElectricItem) {
			return ElectricItem.rawManager;
		}
		return ElectricItem.getBackupManager(itemStack);
	}
	
	@Optional.Method(modid = "IC2")
	private static boolean IC2_isContainer(ItemStack itemStack) {
		return itemStack.getItem() instanceof IElectricItem;
	}
	
	@Optional.Method(modid = "IC2")
	private static boolean IC2_canOutput(ItemStack itemStack) {
		return ((IElectricItem)itemStack.getItem()).canProvideEnergy(itemStack);
	}
	
	@Optional.Method(modid = "IC2")
	private static boolean IC2_canInput(ItemStack itemStack) {
		return false;
	}
	
	@Optional.Method(modid = "IC2")
	private static double IC2_getEnergyStored(ItemStack itemStack) {
		IElectricItemManager electricItemManager = IC2_getManager(itemStack);
		if (electricItemManager == null) {
			return 0.0D;
		}
		return electricItemManager.getCharge(itemStack);
	}
	
	@Optional.Method(modid = "IC2")
	private static double IC2_getMaxEnergyStorage(ItemStack itemStack) {
		return ((IElectricItem)itemStack.getItem()).getMaxCharge(itemStack);
	}
	
	@Optional.Method(modid = "IC2")
	private static ItemStack IC2_consume(ItemStack itemStack, final double amount_EU, final boolean simulate) {
		IElectricItemManager electricItemManager = IC2_getManager(itemStack);
		if (electricItemManager == null) {
			return null;
		}
		if (amount_EU <= electricItemManager.getCharge(itemStack)) {
			if (!simulate) {
				electricItemManager.discharge(itemStack, amount_EU, ((IElectricItem)itemStack.getItem()).getTier(itemStack), true, true, simulate);
				return itemStack;
			}
		}
		return null;
	}
	
	@Optional.Method(modid = "IC2")
	private static ItemStack IC2_charge(ItemStack itemStack, final double amount_EU, final boolean simulate) {
		IElectricItemManager electricItemManager = IC2_getManager(itemStack);
		if (electricItemManager == null) {
			return null;
		}
		if (amount_EU >= IC2_getMaxEnergyStorage(itemStack)) {
			if (!simulate) {
				electricItemManager.charge(itemStack, amount_EU, ((IElectricItem)itemStack.getItem()).getTier(itemStack), true, simulate);
				return itemStack;
			}
		}
		return null;
	}
	
	
	// Thermal Expansion IEnergyContainerItem interface
	@Optional.Method(modid = "CoFHCore")
	private static boolean CoFH_isContainer(ItemStack itemStack) {
		return itemStack.getItem() instanceof IEnergyContainerItem;
	}
	
	@Optional.Method(modid = "CoFHCore")
	private static boolean CoFH_canOutput(ItemStack itemStack) {
		return ((IEnergyContainerItem) itemStack.getItem()).getEnergyStored(itemStack) > 0;
	}
	
	@Optional.Method(modid = "CoFHCore")
	private static boolean CoFH_canInput(ItemStack itemStack) {
		return ((IEnergyContainerItem) itemStack.getItem()).getEnergyStored(itemStack) < ((IEnergyContainerItem) itemStack.getItem()).getMaxEnergyStored(itemStack);
	}
	
	@Optional.Method(modid = "CoFHCore")
	private static int CoFH_getEnergyStored(ItemStack itemStack) {
		return (int) Math.floor( ((IEnergyContainerItem)itemStack.getItem()).getEnergyStored(itemStack) );
	}
	
	@Optional.Method(modid = "CoFHCore")
	private static int CoFH_getMaxEnergyStorage(ItemStack itemStack) {
		return (int) Math.floor( ((IEnergyContainerItem)itemStack.getItem()).getMaxEnergyStored(itemStack) );
	}
	
	@Optional.Method(modid = "CoFHCore")
	private static ItemStack CoFH_consume(ItemStack itemStack, final int amount_RF, final boolean simulate) {
		if (((IEnergyContainerItem)itemStack.getItem()).extractEnergy(itemStack, amount_RF, simulate) > 0) {
			return itemStack;
		}
		return null;
	}
	
	@Optional.Method(modid = "CoFHCore")
	private static ItemStack CoFH_charge(ItemStack itemStack, final int amount_RF, final boolean simulate) {
		if ( ((IEnergyContainerItem)itemStack.getItem()).receiveEnergy(itemStack, amount_RF, simulate) > 0) {
			return itemStack;
		}
		return null;
	}
}
