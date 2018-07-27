package cr0s.warpdrive.config;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.ParticleRegistry;
import cr0s.warpdrive.block.decoration.BlockDecorative;
import cr0s.warpdrive.data.EnumAirTankTier;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.data.EnumDecorativeType;
import cr0s.warpdrive.data.EnumForceFieldShape;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import cr0s.warpdrive.data.EnumTier;
import cr0s.warpdrive.item.ItemComponent;
import cr0s.warpdrive.item.ItemElectromagneticCell;
import cr0s.warpdrive.item.ItemForceFieldShape;
import cr0s.warpdrive.item.ItemForceFieldUpgrade;
import cr0s.warpdrive.item.ItemTuningDriver;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.ForgeRegistry;

/**
 * Hold the different recipe sets
 */
public class Recipes {
	
	private static ResourceLocation groupComponents   = new ResourceLocation("components");
	private static ResourceLocation groupDecorations  = new ResourceLocation("decoration");
	private static ResourceLocation groupMachines     = new ResourceLocation("machines");
	private static ResourceLocation groupTools        = new ResourceLocation("tools");
	
	private static ResourceLocation groupHulls        = new ResourceLocation("hulls");
	private static ResourceLocation groupTaintedHulls = new ResourceLocation("tainted_hulls");
	
	public static final HashMap<EnumDyeColor, String> oreDyes = new HashMap<>(16);
	static {
		oreDyes.put(EnumDyeColor.WHITE     , "dyeWhite");
		oreDyes.put(EnumDyeColor.ORANGE    , "dyeOrange");
		oreDyes.put(EnumDyeColor.MAGENTA   , "dyeMagenta");
		oreDyes.put(EnumDyeColor.LIGHT_BLUE, "dyeLightBlue");
		oreDyes.put(EnumDyeColor.YELLOW    , "dyeYellow");
		oreDyes.put(EnumDyeColor.LIME      , "dyeLime");
		oreDyes.put(EnumDyeColor.PINK      , "dyePink");
		oreDyes.put(EnumDyeColor.GRAY      , "dyeGray");
		oreDyes.put(EnumDyeColor.SILVER    , "dyeLightGray");
		oreDyes.put(EnumDyeColor.CYAN      , "dyeCyan");
		oreDyes.put(EnumDyeColor.PURPLE    , "dyePurple");
		oreDyes.put(EnumDyeColor.BLUE      , "dyeBlue");
		oreDyes.put(EnumDyeColor.BROWN     , "dyeBrown");
		oreDyes.put(EnumDyeColor.GREEN     , "dyeGreen");
		oreDyes.put(EnumDyeColor.RED       , "dyeRed");
		oreDyes.put(EnumDyeColor.BLACK     , "dyeBlack");
	}
	
	public static Ingredient getIngredient(final Object object) {
		if (object instanceof ItemStack) {
			return Ingredient.fromStacks((ItemStack) object);
		}
		if (object instanceof Item) {
			return Ingredient.fromItem((Item) object);
		}
		if (object instanceof String) {
			return new OreIngredient((String) object);
		}
		final ItemStack itemStack = new ItemStack(Blocks.FIRE);
		if (object != null) {
			itemStack.setStackDisplayName(object.toString());
		}
		return Ingredient.fromStacks(itemStack);
	}
	
	public static void initOreDictionary() {
		// air shields
		for (final EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
			registerOreDictionary("blockAirShield", new ItemStack(WarpDrive.blockAirShield, 1, enumDyeColor.getMetadata()));
		}
		
		// decoration
		registerOreDictionary("warpDecorative", BlockDecorative.getItemStack(EnumDecorativeType.PLAIN));
		registerOreDictionary("warpDecorative", BlockDecorative.getItemStack(EnumDecorativeType.ENERGIZED));
		registerOreDictionary("warpDecorative", BlockDecorative.getItemStack(EnumDecorativeType.NETWORK));
		
		// tuning fork
		for (int dyeColor = 0; dyeColor < 16; dyeColor++) {
			registerOreDictionary("itemTuningFork", new ItemStack(WarpDrive.itemTuningFork, 1, dyeColor));
		}
		
		// accelerator
		if (WarpDriveConfig.ACCELERATOR_ENABLE) {
			registerOreDictionary("blockVoidShell", new ItemStack(WarpDrive.blockVoidShellPlain, 1));
			registerOreDictionary("blockVoidShell", new ItemStack(WarpDrive.blockVoidShellGlass, 1));
			for (int tier = 1; tier <= 3; tier++) {
				int index = tier - 1;
				registerOreDictionary("blockElectromagnet" + tier, new ItemStack(WarpDrive.blockElectromagnetPlain[index], 1));
				registerOreDictionary("blockElectromagnet" + tier, new ItemStack(WarpDrive.blockElectromagnetGlass[index], 1));
			}
		}
		
		// hull
		for (final EnumTier enumTier : EnumTier.nonCreative()) {
			final int index = enumTier.getIndex();
			for (int woolColor = 0; woolColor < 16; woolColor++) {
				registerOreDictionary("blockHull" + index + "_plain", new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, woolColor));
				registerOreDictionary("blockHull" + index + "_glass", new ItemStack(WarpDrive.blockHulls_glass[index], 1, woolColor));
				registerOreDictionary("blockHull" + index + "_stairs", new ItemStack(WarpDrive.blockHulls_stairs[index][woolColor], 1));
				registerOreDictionary("blockHull" + index + "_tiled", new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, woolColor));
				registerOreDictionary("blockHull" + index + "_slab", new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 0));
				registerOreDictionary("blockHull" + index + "_slab", new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 2));
				registerOreDictionary("blockHull" + index + "_slab", new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 6));
				registerOreDictionary("blockHull" + index + "_slab", new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 8));
				registerOreDictionary("blockHull" + index + "_omnipanel", new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor));
			}
		}
		
		// Add Reinforced iridium plate to ore registry as applicable (it's missing in IC2 without GregTech)
		if (!OreDictionary.doesOreNameExist("plateAlloyIridium") || OreDictionary.getOres("plateAlloyIridium").isEmpty()) {
			if (WarpDriveConfig.isIndustrialCraft2Loaded) {
				final ItemStack iridiumAlloy = WarpDriveConfig.getItemStackOrFire("ic2:crafting", 4); // IC2 Experimental Iridium alloy plate
				OreDictionary.registerOre("plateAlloyIridium", iridiumAlloy);
			}
			if (WarpDriveConfig.isThermalFoundationLoaded) {
				final ItemStack iridiumAlloy = WarpDriveConfig.getItemStackOrFire("thermalfoundation:material", 327);   // Thermal Foundation Iridium Plate
				OreDictionary.registerOre("plateAlloyIridium", iridiumAlloy);
			}
		}
	}
	
	private static void registerOreDictionary(final String name, final ItemStack itemStack) {
		if (!itemStack.isEmpty()) {
			OreDictionary.registerOre(name, itemStack);
		}
	}
	
	@SuppressWarnings("UnusedAssignment")
	public static void initDynamic() {
		// Support iron bars from all mods
		Object ironBars = Blocks.IRON_BARS;
		if (OreDictionary.doesOreNameExist("barsIron") && !OreDictionary.getOres("barsIron").isEmpty()) {
			ironBars = "barsIron";
		}
		
		// Get the machine casing to use
		ItemStack itemStackMachineCasingLV;
		ItemStack itemStackMachineCasingMV;
		ItemStack itemStackMachineCasingHV;
		ItemStack itemStackMachineCasingEV;
		ItemStack itemStackMotorLV = ItemComponent.getItemStack(EnumComponentType.MOTOR);
		ItemStack itemStackMotorMV = ItemComponent.getItemStack(EnumComponentType.MOTOR);
		ItemStack itemStackMotorHV = ItemComponent.getItemStack(EnumComponentType.MOTOR);
		ItemStack itemStackMotorEV = ItemComponent.getItemStack(EnumComponentType.MOTOR);
		
		if (WarpDriveConfig.isGregTechLoaded) {
			itemStackMachineCasingLV = WarpDriveConfig.getItemStackOrFire("gregtech:machine_casing", 1); // LV machine casing (Steel)
			itemStackMachineCasingMV = WarpDriveConfig.getItemStackOrFire("gregtech:machine_casing", 2); // MV machine casing (Aluminium)
			itemStackMachineCasingHV = WarpDriveConfig.getItemStackOrFire("gregtech:machine_casing", 3); // HV machine casing (Stainless Steel)
			itemStackMachineCasingEV = WarpDriveConfig.getItemStackOrFire("gregtech:machine_casing", 4); // EV machine casing (Titanium)
			
			itemStackMotorLV = WarpDriveConfig.getItemStackOrFire("gregtech:meta_item_1", 32600); // LV Motor
			itemStackMotorMV = WarpDriveConfig.getItemStackOrFire("gregtech:meta_item_1", 32601); // MV Motor
			itemStackMotorHV = WarpDriveConfig.getItemStackOrFire("gregtech:meta_item_1", 32602); // HV Motor
			itemStackMotorEV = WarpDriveConfig.getItemStackOrFire("gregtech:meta_item_1", 32603); // EV Motor
			
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			itemStackMachineCasingLV = WarpDriveConfig.getItemStackOrFire("ic2:resource", 12); // Basic machine casing
			itemStackMachineCasingMV = WarpDriveConfig.getItemStackOrFire("ic2:resource", 13); // Advanced machine casing
			itemStackMachineCasingHV = new ItemStack(WarpDrive.blockHighlyAdvancedMachine);
			itemStackMachineCasingEV = new ItemStack(WarpDrive.blockHighlyAdvancedMachine);
			
			final ItemStack itemStackMotor = WarpDriveConfig.getItemStackOrFire("ic2:crafting", 6);      // IC2 Experimental Electric motor @MC1.10 update
			itemStackMotorHV = itemStackMotor;
			itemStackMotorEV = itemStackMotor;
			
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       new ItemStack(WarpDrive.blockHighlyAdvancedMachine), false, "iii", "imi", "iii",
			                                       'i', "plateAlloyIridium",
			                                       'm', itemStackMachineCasingMV));
			
		} else if (WarpDriveConfig.isThermalFoundationLoaded) {  // These are upgrade kits, there is only 1 machine frame tier as of Thermal Foundation 1.12.2-5.5.0.29
			itemStackMachineCasingLV = WarpDriveConfig.getItemStackOrFire("thermalfoundation:upgrade", 0);
			itemStackMachineCasingMV = WarpDriveConfig.getItemStackOrFire("thermalfoundation:upgrade", 1);
			itemStackMachineCasingHV = WarpDriveConfig.getItemStackOrFire("thermalfoundation:upgrade", 2);
			itemStackMachineCasingEV = WarpDriveConfig.getItemStackOrFire("thermalfoundation:upgrade", 3);
			
		} else if (WarpDriveConfig.isEnderIOLoaded) { // As of EnderIO on MC 1.12.2 there are 5 machine chassis
			itemStackMachineCasingLV = WarpDriveConfig.getItemStackOrFire("enderio:item_material", 0);     // Simple Machine chassis
			itemStackMachineCasingMV = WarpDriveConfig.getItemStackOrFire("enderio:item_material", 1);     // Industrial Machine chassis
			itemStackMachineCasingHV = WarpDriveConfig.getItemStackOrFire("enderio:item_material", 54);    // Enhanced Machine chassis
			itemStackMachineCasingEV = WarpDriveConfig.getItemStackOrFire("enderio:item_material", 55);    // Soulless Machine chassis
			// itemStackMachineCasingEV = WarpDriveConfig.getModItemStack("enderio", "item_material", 53); // Soul Machine chassis 
		} else {// vanilla
			itemStackMachineCasingLV = new ItemStack(Blocks.IRON_BLOCK);
			itemStackMachineCasingMV = new ItemStack(Blocks.DIAMOND_BLOCK);
			itemStackMachineCasingHV = new ItemStack(WarpDrive.blockHighlyAdvancedMachine);
			itemStackMachineCasingEV = new ItemStack(Blocks.BEACON);
			
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       new ItemStack(WarpDrive.blockHighlyAdvancedMachine, 4), "pep", "ede", "pep",
				'e', Items.EMERALD,
				'p', Items.ENDER_EYE,
				'd', Blocks.DIAMOND_BLOCK));
		}
		
		final ItemStack[] itemStackMachineCasings = { itemStackMachineCasingLV, itemStackMachineCasingMV, itemStackMachineCasingHV, itemStackMachineCasingEV };
		final ItemStack[] itemStackMotors  = { itemStackMotorLV, itemStackMotorMV, itemStackMotorHV, itemStackMotorEV };
		
		// Base tuning crystals
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL), false, " e ", "BBB", "qrq",
		                                       'e', Items.EMERALD,
		                                       'B', ironBars,
		                                       'r', Items.REDSTONE,
		                                       'q', Items.QUARTZ));
		
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL), false, " e ", "BBB", "grg",
		                                       'e', Items.ENDER_PEARL,
		                                       'B', ironBars,
		                                       'r', Items.REDSTONE,
		                                       'g', "nuggetGold"));
		
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL), false, " d ", "BBB", "prp",
		                                       'd', Items.DIAMOND,
		                                       'B', ironBars,
		                                       'r', Items.REDSTONE,
		                                       'p', Items.PAPER));
		
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStack(EnumComponentType.DIFFRACTION_GRATING), false, " t ", "iii", "ggg",
		                                       't', Items.GHAST_TEAR,
		                                       'i', ironBars,
		                                       'g', Blocks.GLOWSTONE));
		
		// Intermediary component for reactor core
		if (!WarpDriveConfig.ACCELERATOR_ENABLE) {
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       ItemComponent.getItemStack(EnumComponentType.REACTOR_CORE), false, "shs", "hmh", "shs",
			                                       's', Items.NETHER_STAR,
			                                       'h', "blockHull3_plain",
			                                       'm', itemStackMachineCasings[2]));
		} else {
			WarpDrive.register(new RecipeParticleShapedOre(groupMachines,
			                                               ItemComponent.getItemStack(EnumComponentType.REACTOR_CORE), false, "chc", "hph", "cec",
			                                               'p', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.PROTON, 1000),
			                                               'h', "blockHull3_plain",
			                                               'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
			                                               'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL)));
		}
		
		
		// Computer interface is 2 gold ingot, 2 wired modems (or redstone), 1 lead/tin ingot
		{
			Object redstoneOrModem = Items.REDSTONE;
			if (WarpDriveConfig.isComputerCraftLoaded) {
				redstoneOrModem = WarpDriveConfig.getItemStackOrFire("computercraft:cable", 1); // Wired modem
			}
			
			Object oreCircuitOrHeavyPressurePlate = Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE;
			int outputFactor = 1;
			if (OreDictionary.doesOreNameExist("oc:materialCU") && !OreDictionary.getOres("oc:materialCU").isEmpty()) {
				oreCircuitOrHeavyPressurePlate = "oc:materialCU";	// Control circuit is 5 redstone, 5 gold ingot, 3 paper
				outputFactor = 2;
			} else if (OreDictionary.doesOreNameExist("circuitBasic") && !OreDictionary.getOres("circuitBasic").isEmpty()) {// comes with IndustrialCraft2, GregTech, ICBM-Classic
				oreCircuitOrHeavyPressurePlate = "circuitBasic";
				outputFactor = 2;
			}
			
			String oreSlimeOrTinOrLead = "slimeball";
			// Computer interface: double output with Soldering alloy
			if (OreDictionary.doesOreNameExist("ingotSolderingAlloy") && !OreDictionary.getOres("ingotSolderingAlloy").isEmpty()) {
				WarpDrive.register(new ShapedOreRecipe(groupComponents,
				                                       ItemComponent.getItemStackNoCache(EnumComponentType.COMPUTER_INTERFACE, 2 * outputFactor), false, "   ", "rar", "gGg",
				                                       'G', oreCircuitOrHeavyPressurePlate,
				                                       'g', "ingotGold",
				                                       'r', redstoneOrModem,
				                                       'a', "ingotSolderingAlloy"));
			}
			// Computer interface: simple output
			if (OreDictionary.doesOreNameExist("ingotTin") && !OreDictionary.getOres("ingotTin").isEmpty()) {
				oreSlimeOrTinOrLead = "ingotTin";
			} else if (OreDictionary.doesOreNameExist("ingotLead") && !OreDictionary.getOres("ingotLead").isEmpty()) {
				oreSlimeOrTinOrLead = "ingotLead";
			}
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       ItemComponent.getItemStackNoCache(EnumComponentType.COMPUTER_INTERFACE, outputFactor), false, "   ", "rar", "gGg",
			                                       'G', oreCircuitOrHeavyPressurePlate,
			                                       'g', "ingotGold",
			                                       'r', redstoneOrModem,
			                                       'a', oreSlimeOrTinOrLead));
		}
		
		// Power interface is 4 redstone, 2 iron ingot, 3 gold ingot
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStackNoCache(EnumComponentType.POWER_INTERFACE, 2), false, "rgr", "igi", "rgr",
		                                       'g', "ingotGold",
		                                       'r', Items.REDSTONE,
		                                       'i', Items.IRON_INGOT));
		
		// Capacitive crystal is 2 redstone block, 4 paper, 1 regeneration potion, 2 (lithium dust or electrum dust or electrical steel ingot or gold ingot)
		Object lithiumOrElectrum = "ingotGold";
		if (OreDictionary.doesOreNameExist("dustLithium") && !OreDictionary.getOres("dustLithium").isEmpty()) {// comes with GregTech, Industrial Craft 2 and Mekanism
			// (Lithium is processed from nether quartz)
			// (IC2 Experimental is 1 Lithium dust from 18 nether quartz)
			lithiumOrElectrum = "dustLithium";
		} else if (OreDictionary.doesOreNameExist("dustElectrum") && !OreDictionary.getOres("dustElectrum").isEmpty()) {// comes with ImmersiveEngineering, ThermalFoundation, Metallurgy
			lithiumOrElectrum = "dustElectrum";
		} else if (OreDictionary.doesOreNameExist("ingotElectricalSteel") && !OreDictionary.getOres("ingotElectricalSteel").isEmpty()) {// comes with EnderIO
			lithiumOrElectrum = "ingotElectricalSteel";
		}
		// Regeneration II (ghast tear + glowstone)
		final ItemStack itemStackStrongRegeneration = new ItemStack(Items.POTIONITEM, 1, 0);
		itemStackStrongRegeneration.setTagCompound(new NBTTagCompound());
		itemStackStrongRegeneration.getTagCompound().setString("Potion", "minecraft:strong_regeneration");
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStackNoCache(EnumComponentType.CAPACITIVE_CRYSTAL, 2), false, "prp", "lRl", "prp",
		                                       'R', itemStackStrongRegeneration,
		                                       'r', "blockRedstone",
		                                       'l', lithiumOrElectrum,
		                                       'p', Items.PAPER));
		
		// Air canister is 4 iron bars, 2 leather/rubber, 2 yellow wool, 1 tank
		Object rubberOrLeather = Items.LEATHER;
		if (OreDictionary.doesOreNameExist("plateRubber") && !OreDictionary.getOres("plateRubber").isEmpty()) {// comes with GregTech
			rubberOrLeather = "plateRubber";
		} else if (OreDictionary.doesOreNameExist("itemRubber") && !OreDictionary.getOres("itemRubber").isEmpty()) {// comes with IndustrialCraft2
			rubberOrLeather = "itemRubber";
		}
		Object woolPurple = new ItemStack(Blocks.WOOL, 1, 10);
		if (OreDictionary.doesOreNameExist("blockWoolPurple") && !OreDictionary.getOres("blockWoolPurple").isEmpty()) {
			woolPurple = "blockWoolPurple";
		}
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 4), false, "iyi", "rgr", "iyi",
		                                       'r', rubberOrLeather,
		                                       'g', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
		                                       'y', woolPurple,
		                                       'i', ironBars));
		
		// Lens is 1 diamond, 2 gold ingots, 2 glass panels
		if (OreDictionary.doesOreNameExist("lensDiamond") && !OreDictionary.getOres("lensDiamond").isEmpty()) {
			if (OreDictionary.doesOreNameExist("craftingLensWhite") && !OreDictionary.getOres("craftingLensWhite").isEmpty()) {
				WarpDrive.register(new ShapedOreRecipe(groupComponents,
				                                       ItemComponent.getItemStackNoCache(EnumComponentType.LENS, 3), false, "ggg", "pdp", "ggg",
				                                       'g', "nuggetGold",
				                                       'p', "craftingLensWhite",
				                                       'd', "lensDiamond"));
			} else {
				WarpDrive.register(new ShapedOreRecipe(groupComponents,
				                                       ItemComponent.getItemStack(EnumComponentType.LENS), false, " g ", "pdp", " g ",
				                                       'g', "ingotGold",
				                                       'p', "paneGlassColorless",
				                                       'd', "lensDiamond"));
			}
		} else if (WarpDriveConfig.isAdvancedRepulsionSystemLoaded) {
			final ItemStack diamondLens = WarpDriveConfig.getItemStackOrFire("AdvancedRepulsionSystems:{A8F3AF2F-0384-4EAA-9486-8F7E7A1B96E7}", 1);
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       ItemComponent.getItemStack(EnumComponentType.LENS), false, " g ", "pdp", " g ",
			                                       'g', "ingotGold",
			                                       'p', "paneGlassColorless",
			                                       'd', diamondLens));
		} else {
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       ItemComponent.getItemStackNoCache(EnumComponentType.LENS, 2), false, " g ", "pdp", " g ",
			                                       'g', "ingotGold",
			                                       'p', "paneGlassColorless",
			                                       'd', "gemDiamond"));
		}
		
		// Zoom is 3 lenses, 2 iron ingot, 2 dyes, 2 redstone
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStack(EnumComponentType.ZOOM), false, "dir", "lll", "dit",
		                                       'r', Items.REDSTONE,
		                                       'i', Items.IRON_INGOT,
		                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
		                                       't', itemStackMotors[0],
		                                       'd', "dye"));
		
		// Glass tank is 4 slime balls, 4 glass
		// slimeball && blockGlass are defined by forge itself
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStackNoCache(EnumComponentType.GLASS_TANK, 4), false, "sgs", "g g", "sgs",
		                                       's', "slimeball",
		                                       'g', "blockGlass"));
		
		// Flat screen is 3 dyes, 1 glowstone dust, 2 paper, 3 glass panes
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStack(EnumComponentType.FLAT_SCREEN), false, "gRp", "gGd", "gBp",
		                                       'R', "dyeRed",
		                                       'G', "dyeLime",
		                                       'B', "dyeBlue",
		                                       'd', "dustGlowstone",
		                                       'g', "paneGlassColorless",
		                                       'p', Items.PAPER));
		
		// Memory crystal is 2 papers, 2 iron bars, 4 comparators, 1 redstone
		if (OreDictionary.doesOreNameExist("circuitPrimitive") && !OreDictionary.getOres("circuitPrimitive").isEmpty()) { // GregTech
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL), false, "cic", "cic", "prp",
			                                       'i', ironBars,
			                                       'c', "circuitPrimitive",
			                                       'r', Items.REDSTONE,
			                                       'p', Items.PAPER));
		} else if (OreDictionary.doesOreNameExist("oc:ram3") && !OreDictionary.getOres("oc:ram3").isEmpty()) {
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       ItemComponent.getItemStackNoCache(EnumComponentType.MEMORY_CRYSTAL, 4), false, "cic", "cic", "prp",
			                                       'i', ironBars,
			                                       'c', "oc:ram3",
			                                       'r', Items.REDSTONE,
			                                       'p', Items.PAPER));
		} else {
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL), false, "cic", "cic", "prp",
			                                       'i', ironBars,
			                                       'c', Items.COMPARATOR,
			                                       'r', Items.REDSTONE,
			                                       'p', Items.PAPER));
		}
		
		// Motor is 2 gold nuggets (wires), 3 iron ingots (steel rods), 4 iron bars (coils)
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStack(EnumComponentType.MOTOR), false, "bbn", "iii", "bbn",
		                                       'b', ironBars,
		                                       'i', Items.IRON_INGOT,
		                                       'n', "nuggetGold"));
		
		// Basic Air Tank is 2 air canisters, 1 pump, 1 gold nugget, 1 basic circuit, 4 rubber
		Object goldNuggetOrBasicCircuit = "nuggetGold";
		if (OreDictionary.doesOreNameExist("circuitBasic") && !OreDictionary.getOres("circuitBasic").isEmpty()) {// comes with IndustrialCraft2, Mekanism, VoltzEngine
			goldNuggetOrBasicCircuit = "circuitBasic";
		}
		WarpDrive.register(new ShapedOreRecipe(groupTools,
		                                       WarpDrive.itemAirTanks[EnumAirTankTier.BASIC.getIndex()], false, "rnr", "tpt", "rcr",
		                                       'r', rubberOrLeather,
		                                       'p', itemStackMotors[0],
		                                       't', ItemComponent.getItemStack(EnumComponentType.AIR_CANISTER),
		                                       'c', goldNuggetOrBasicCircuit,
		                                       'n', "nuggetGold"));
		
		// Advanced Air Tank is 2 basic air tank, 1 pump, 1 gold nugget, 1 advanced circuit, 4 rubber
		Object goldIngotOrAdvancedCircuit = "nuggetGold";
		if (OreDictionary.doesOreNameExist("circuitAdvanced") && !OreDictionary.getOres("circuitAdvanced").isEmpty()) {// comes with IndustrialCraft2, Mekanism, VoltzEngine
			goldIngotOrAdvancedCircuit = "circuitAdvanced";
		}
		WarpDrive.register(new ShapedOreRecipe(groupTools,
		                                       WarpDrive.itemAirTanks[EnumAirTankTier.ADVANCED.getIndex()], false, "rnr", "tpt", "rcr",
		                                       'r', rubberOrLeather,
		                                       'p', itemStackMotors[1],
		                                       't', WarpDrive.itemAirTanks[EnumAirTankTier.BASIC.getIndex()],
		                                       'c', goldIngotOrAdvancedCircuit,
		                                       'n', "nuggetGold"));
		
		// Superior Air Tank is 2 advanced air tank, 1 pump, 1 gold nugget, 1 elite circuit, 4 rubber
		Object emeraldOrSuperiorCircuit = "gemEmerald";
		if (OreDictionary.doesOreNameExist("circuitElite") && !OreDictionary.getOres("circuitElite").isEmpty()) {// comes with Mekanism, VoltzEngine
			emeraldOrSuperiorCircuit = "circuitElite";
		}
		WarpDrive.register(new ShapedOreRecipe(groupTools,
		                                       WarpDrive.itemAirTanks[EnumAirTankTier.SUPERIOR.getIndex()], false, "rnr", "tpt", "rcr",
		                                       'r', rubberOrLeather,
		                                       'p', itemStackMotors[2],
		                                       't', WarpDrive.itemAirTanks[EnumAirTankTier.ADVANCED.getIndex()],
		                                       'c', emeraldOrSuperiorCircuit,
		                                       'n', "nuggetGold"));
		
		// Uncrafting air tanks and canister
		WarpDrive.register(new ShapelessOreRecipe(groupComponents,
		                                          ItemComponent.getItemStackNoCache(EnumComponentType.GLASS_TANK, 1),
		                                          WarpDrive.itemAirTanks[EnumAirTankTier.CANISTER.getIndex()],
		                                          WarpDrive.itemAirTanks[EnumAirTankTier.CANISTER.getIndex()],
		                                          WarpDrive.itemAirTanks[EnumAirTankTier.CANISTER.getIndex()],
		                                          WarpDrive.itemAirTanks[EnumAirTankTier.CANISTER.getIndex()] ), "_uncrafting");
		WarpDrive.register(new ShapelessOreRecipe(groupComponents,
				                                  ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 2),
				                                  WarpDrive.itemAirTanks[EnumAirTankTier.BASIC.getIndex()]), "_uncrafting");
		WarpDrive.register(new ShapelessOreRecipe(groupComponents,
				                                  ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 4),
				                                  WarpDrive.itemAirTanks[EnumAirTankTier.ADVANCED.getIndex()]), "_uncrafting");
		WarpDrive.register(new ShapelessOreRecipe(groupComponents,
				                                  ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 8),
				                                  WarpDrive.itemAirTanks[EnumAirTankTier.SUPERIOR.getIndex()]), "_uncrafting");
		
		// Bone charcoal is smelting 1 bone
		GameRegistry.addSmelting(Items.BONE, ItemComponent.getItemStackNoCache(EnumComponentType.BONE_CHARCOAL, 1), 1);
		
		// Activated carbon is 1 bone charcoal, 4 sticks, 4 leaves
		Object leaves = Blocks.LEAVES;
		if (OreDictionary.doesOreNameExist("treeLeaves") && !OreDictionary.getOres("treeLeaves").isEmpty()) {
			leaves = "treeLeaves";
		}
		if (OreDictionary.doesOreNameExist("dustSulfur") && !OreDictionary.getOres("dustSulfur").isEmpty()) {
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       ItemComponent.getItemStack(EnumComponentType.ACTIVATED_CARBON), false, "lll", "aaa", "fwf",
			                                       'l', leaves,
			                                       'a', ItemComponent.getItemStack(EnumComponentType.BONE_CHARCOAL),
			                                       'w', new ItemStack(Items.POTIONITEM, 1, 0),
			                                       'f', "dustSulfur"));
		} else {
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       ItemComponent.getItemStack(EnumComponentType.ACTIVATED_CARBON), false, "lll", "aaa", "wgw",
			                                       'l', leaves,
			                                       'a', ItemComponent.getItemStack(EnumComponentType.BONE_CHARCOAL),
			                                       'w', new ItemStack(Items.POTIONITEM, 1, 0),
			                                       'g', Items.GUNPOWDER));
		}
		
		// Laser medium (empty) is 3 glass tanks, 1 power interface, 1 computer interface, 1 MV Machine casing
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       ItemComponent.getItemStack(EnumComponentType.LASER_MEDIUM_EMPTY), false, "   ", "ggg", "pmc",
		                                       'g', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'm', itemStackMachineCasings[2],
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		
		// Coil crystal is 6 iron bars, 2 gold ingots, 1 diamond crystal, return 12x
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStackNoCache(EnumComponentType.COIL_CRYSTAL, 12), false, "bbg", "bdb", "gbb",
		                                       'b', ironBars,
		                                       'g', "ingotGold",
		                                       'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL)));
		
		// Electromagnetic Projector is 5 coil crystals, 1 power interface, 1 computer interface, 2 motors
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR), false, "CCm", "Cpc", "CCm",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MOTOR),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		
		// Superconductor is 1 Ender crystal, 4 Power interface, 4 Cryotheum dust/Lapis block/10k Coolant cell
		Object oreCoolant = "blockLapis";
		if (WarpDriveConfig.isThermalExpansionLoaded) {
			oreCoolant = "dustCryotheum";
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			oreCoolant = WarpDriveConfig.getItemStackOrFire("ic2:heat_storage", 0);       // IC2 Experimental 10k Coolant Cell
		}
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemComponent.getItemStack(EnumComponentType.SUPERCONDUCTOR), false, "pcp", "cec", "pcp",
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
		                                       'c', oreCoolant ));
		
		// *** Force field shapes
		// Force field shapes are 1 Memory crystal, 3 to 5 Coil crystal
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldShape.getItemStack(EnumForceFieldShape.SPHERE), false, "   ", "CmC", "CCC",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldShape.getItemStack(EnumForceFieldShape.CYLINDER_H), false, "C C", " m ", "C C",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldShape.getItemStack(EnumForceFieldShape.CYLINDER_V), false, " C ", "CmC", " C ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldShape.getItemStack(EnumForceFieldShape.CUBE), false, "CCC", "CmC", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldShape.getItemStack(EnumForceFieldShape.PLANE), false, "CCC", " m ", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldShape.getItemStack(EnumForceFieldShape.TUBE), false, "   ", "CmC", "C C",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldShape.getItemStack(EnumForceFieldShape.TUNNEL), false, "C C", "CmC", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		
		// *** Force field upgrades
		// Force field attraction upgrade is 3 Coil crystal, 1 Iron block, 2 Redstone block, 1 MV motor
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.ATTRACTION), false, "CCC", "rir", " m ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'r', "blockRedstone",
		                                       'i', Blocks.IRON_BLOCK,
		                                       'm', itemStackMotorMV));
		// Force field breaking upgrade is 3 Coil crystal, 1 Diamond axe, 1 diamond shovel, 1 diamond pick
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.BREAKING), false, "CCC", "sap", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       's', Items.DIAMOND_AXE,
		                                       'a', Items.DIAMOND_SHOVEL,
		                                       'p', Items.DIAMOND_PICKAXE));
		// Force field camouflage upgrade is 3 Coil crystal, 2 Diffraction grating, 1 Zoom, 1 Emerald crystal
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.CAMOUFLAGE), false, "CCC", "zre", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'z', ItemComponent.getItemStack(EnumComponentType.ZOOM),
		                                       'r', Blocks.DAYLIGHT_DETECTOR,
		                                       'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL)));
		// Force field cooling upgrade is 3 Coil crystal, 2 Ice, 1 MV Motor
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.COOLING), false, "CCC", "imi", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'i', Blocks.ICE,
		                                       'm', itemStackMotors[1]));
		// Force field fusion upgrade is 3 Coil crystal, 2 Computer interface, 1 Emerald crystal
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.FUSION), false, "CCC", "cec", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL)));
		// Force field heating upgrade is 3 Coil crystal, 2 Blaze rod, 1 MV Motor
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.HEATING), false, "CCC", "bmb", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'b', Items.BLAZE_ROD,
		                                       'm', itemStackMotors[1]));
		// Force field inversion upgrade is 3 Coil crystal, 1 Gold nugget, 2 Redstone
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.INVERSION), false, "rgr", "CCC", "CCC",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'r', Items.REDSTONE,
		                                       'g', Items.GOLD_NUGGET));
		// Force field item port upgrade is 3 Coil crystal, 3 Chests, 1 MV motor
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.ITEM_PORT), false, "CCC", "cmc", " c ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'c', Blocks.CHEST,
		                                       'm', itemStackMotorMV));
		// Force field silencer upgrade is 3 Coil crystal, 3 Wool
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.SILENCER), false, "CCC", "www", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'w', Blocks.WOOL));
		// Force field pumping upgrade is 3 Coil crystal, 1 MV Motor, 2 glass tanks
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.PUMPING), false, "CCC", "tmt", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', itemStackMotors[1],
		                                       't', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK)));
		// Force field range upgrade is 3 Coil crystal, 2 Memory crystal, 1 Redstone block
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.RANGE), false, "CCC", "RMR", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'M', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL),
		                                       'R', "blockRedstone"));
		// Force field repulsion upgrade is 3 Coil crystal, 1 Iron block, 2 Redstone block, 1 MV motor
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.REPULSION), false, " m ", "rir", "CCC",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'r', "blockRedstone",
		                                       'i', Blocks.IRON_BLOCK,
		                                       'm', itemStackMotorMV));
		// Force field rotation upgrade is 3 Coil crystal, 2 MV Motors, 1 Computer interface
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStackNoCache(EnumForceFieldUpgrade.ROTATION, 2), false, "CCC", " m ", " mc",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', itemStackMotors[1],
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		// Force field shock upgrade is 3 Coil crystal, 1 Power interface
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.SHOCK), false, "CCC", " p ", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE)));
		// Force field speed upgrade is 3 Coil crystal, 2 Ghast tear, 1 Emerald crystal
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.SPEED), false, "CCC", "geg", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'g', Items.GHAST_TEAR,
		                                       'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL)));
		// Force field stabilization upgrade is 3 Coil crystal, 1 Memory crystal, 2 Lapis block
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.STABILIZATION), "CCC", "lMl", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'M', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL),
		                                       'l', "blockLapis"));
		// Force field thickness upgrade is 8 Coil crystal, 1 Diamond crystal
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.THICKNESS), false, "CCC", "CpC", "   ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'p', ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR)));
		// Force field translation upgrade is 3 Coil crystal, 2 MV Motor, 1 Computer interface
		WarpDrive.register(new ShapedOreRecipe(groupComponents,
		                                       ItemForceFieldUpgrade.getItemStackNoCache(EnumForceFieldUpgrade.TRANSLATION, 2), false, "CCC", "m m", " c ",
		                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                       'm', itemStackMotors[1],
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		
		// *** Blocks
		// Ship core
		// basic    (fighter)  is               4 Redstone dust     , 2 Ender pearls , 1 Power interface, 1 basic Ship controller
		// advanced (corvette) is 1 Ghast tear , 4 Capacitive crystal, 2 Ender crystal, 1 Power interface, 1 advanced Ship controller
		// superior (capital)  is 1 Nether star, 4 Capacitive block  , 2 Ender block  , 1 Power interface, 1 superior Ship controller
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockShipCore[EnumTier.BASIC.getIndex()]),"c c", "eme", "cpc",
		                                       'c', Items.REDSTONE,
		                                       'e', Items.ENDER_PEARL,
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'm', new ItemStack(WarpDrive.blockShipController[EnumTier.BASIC.getIndex()])));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockShipCore[EnumTier.ADVANCED.getIndex()]),"csc", "eme", "cpc",
		                                       's', Items.GHAST_TEAR,
		                                       'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
		                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'm', new ItemStack(WarpDrive.blockShipController[EnumTier.ADVANCED.getIndex()])));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockShipCore[EnumTier.SUPERIOR.getIndex()]),"csc", "eme", "cpc",
		                                       's', Items.NETHER_STAR,
		                                       'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
		                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'm', new ItemStack(WarpDrive.blockShipController[EnumTier.SUPERIOR.getIndex()])));
		
		// Ship controller
		// basic    is 1 Computer interface, 1 Tuning emerald, 1 LV Machine casing, 2 Memory crystal
		// advanced is 1 Computer interface, 1 Tuning emerald, 1 MV Machine casing, 4 Memory crystal
		// superior is 1 Computer interface, 1 Tuning emerald, 1 HV Machine casing, 6 Memory crystal
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockShipController[EnumTier.BASIC.getIndex()]), false, " e ", "bmb", " c ",
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
		                                       'm', itemStackMachineCasings[0],
		                                       'b', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockShipController[EnumTier.ADVANCED.getIndex()]), false, "beb", " m ", "bcb",
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
		                                       'm', itemStackMachineCasings[1],
		                                       'b', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockShipController[EnumTier.SUPERIOR.getIndex()]), false, "beb", "bmb", "bcb",
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
		                                       'm', itemStackMachineCasings[2],
		                                       'b', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		
		// Radar is 1 motor, 4 Titanium plate (diamond), 1 quarztite rod (nether quartz), 1 computer interface, 1 HV Machine casing, 1 power interface
		final Object oreCloakingPlate = WarpDriveConfig.getOreOrItemStack("ore:plateTitanium", 0,     // GregTech
		                                                                  "ore:ingotEnderium", 0,     // ThermalExpansion
		                                                                  "ore:ingotPhasedGold", 0,   // EnderIO
		                                                                  "ore:plateAlloyIridium", 0, // IndustrialCraft2
		                                                                  "ore:gemQuartz", 0);        // vanilla
		final Object oreAntenna = WarpDriveConfig.getOreOrItemStack("ore:stickQuartzite", 0,      // GregTech
		                                                            "ore:ingotSignalum", 0,       // ThermalExpansion
		                                                            "ore:nuggetPulsatingIron", 0, // EnderIO
		                                                            "minecraft:ghast_tear", 0);   // vanilla
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockRadar), false, "PAP", "PtP", "pmc",
		                                       't', itemStackMotors[2],
		                                       'P', oreCloakingPlate,
		                                       'A', oreAntenna,
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'm', itemStackMachineCasings[2],
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE)));
		
		// Warp isolation is 1 EV Machine casing (Ti), 4 Titanium plate/Enderium ingot/Vibrant alloy/Iridium plate/quartz
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockWarpIsolation), false, "i i", " m ", "i i",
		                                       'i', oreCloakingPlate,
		                                       'm', itemStackMachineCasings[3]));
		
		// Air generator is 1 power interface, 4 activated carbon, 1 motor, 1 MV Machine casing, 2 tanks
		final Object compressorOrTank = WarpDriveConfig.getOreOrItemStack("gregtech:meta_item_2", 18095,   // GregTech CE Bronze rotor, ore:rotorBronze
		                                                                  "ic2:te", 43,                    // IC2 Compressor
		                                                                  "thermalexpansion:machine", 8,   // ThermalExpansion Fluid transposer
		                                                                  "enderio:block_reservoir", 0,    // EnderIO Reservoir
		                                                                  "warpdrive:component", EnumComponentType.GLASS_TANK.ordinal()); // WarpDrive Glass tank
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockAirGeneratorTiered[EnumTier.BASIC.getIndex()]), false, "aca", "ata", "gmp",
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'a', ItemComponent.getItemStack(EnumComponentType.ACTIVATED_CARBON),
		                                       't', itemStackMotors[1],
		                                       'g', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
		                                       'm', itemStackMachineCasings[1],
		                                       'c', compressorOrTank));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockAirGeneratorTiered[EnumTier.ADVANCED.getIndex()]), false, "aaa", "ata", "ama",
		                                       'a', WarpDrive.blockAirGeneratorTiered[EnumTier.BASIC.getIndex()],
		                                       't', itemStackMotors[2],
		                                       'm', itemStackMachineCasings[2]));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockAirGeneratorTiered[EnumTier.SUPERIOR.getIndex()]), false, "aaa", "ata", "ama",
		                                       'a', WarpDrive.blockAirGeneratorTiered[EnumTier.ADVANCED.getIndex()],
		                                       't', itemStackMotors[3],
		                                       'm', itemStackMachineCasings[3]));
		
		// Air shield is 4 glowstones, 4 omnipanels and 1 coil crystal 
		for (final EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
			final int metadataColor = enumDyeColor.getMetadata();
			
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockAirShield, 4, metadataColor), false, "gog", "oco", "gog",
			                                       'g', Items.GLOWSTONE_DUST,
			                                       'o', new ItemStack(WarpDrive.blockHulls_omnipanel[EnumTier.BASIC.getIndex()], 1, metadataColor),
			                                       'c', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL) ));
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockAirShield, 6, metadataColor), false, "###", "gXg", "###",
			                                       '#', "blockAirShield",
			                                       'g', Items.GOLD_NUGGET,
			                                       'X', oreDyes.get(enumDyeColor) ));
			WarpDrive.register(new ShapelessOreRecipe(groupMachines,
			                                          new ItemStack(WarpDrive.blockAirShield, 1, metadataColor),
			                                          "blockAirShield",
			                                          oreDyes.get(enumDyeColor) ));
		}
		
		// Laser cannon is 2 motors, 1 diffraction grating, 1 lens, 1 computer interface, 1 HV Machine casing, 1 redstone dust, 2 glass pane
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockLaser), false, "gtr", "ldm", "gtc",
		                                       't', itemStackMotors[2],
		                                       'd', ItemComponent.getItemStack(EnumComponentType.DIFFRACTION_GRATING),
		                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'm', itemStackMachineCasings[1],
		                                       'r', Items.REDSTONE,
		                                       'g', "paneGlassColorless"));
		
		// Mining laser is 2 motors, 1 diffraction grating, 1 lens, 1 computer interface, 1 MV Machine casing, 1 diamond pick, 2 glass pane
		ItemStack itemStackDiamondPick = new ItemStack(Items.DIAMOND_PICKAXE);
		if ( WarpDriveConfig.isGregTechLoaded ) {
			itemStackDiamondPick = WarpDriveConfig.getItemStackOrFire("ic2:mining_laser", 0);       // IC2 Experimental Mining laser
		}
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockMiningLaser), false, "cmr", "tdt", "glg",
		                                       't', itemStackMotors[1],
		                                       'd', ItemComponent.getItemStack(EnumComponentType.DIFFRACTION_GRATING),
		                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'm', itemStackMachineCasings[1],
		                                       'r', itemStackDiamondPick,
		                                       'g', "paneGlassColorless"));
		
		// Laser medium
		// basic    is 1 red dye, 1 green dye, 1 yellow dye, 3 glass bottles, 1 power interface, 1 LV casing, 1 computer interface
		// advanced is 2 redstone dust, 1 nether wart, 2 lapis, 1 potion (any),  1 power interface, 1 LV casing, 1 computer interface
		// superior is 1 laser medium (empty), 4 redstone blocks, 4 lapis blocks
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockLaserMedium[EnumTier.BASIC.getIndex()]), false, "rgy", "BBB", "pmc",
		                                       'r', "dyeRed",
		                                       'g', "dyeGreen",
		                                       'y', "dyeYellow",
		                                       'B', Items.GLASS_BOTTLE,
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'm', itemStackMachineCasings[0],
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockLaserMedium[EnumTier.ADVANCED.getIndex()]), false, "rnr", "lBl", "pmc",
		                                       'r', "dustRedstone",
		                                       'n', "cropNetherWart",
		                                       'l', "gemLapis",
		                                       'B', Items.POTIONITEM,
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'm', itemStackMachineCasings[1],
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockLaserMedium[EnumTier.SUPERIOR.getIndex()]), false, "lrl", "rmr", "lrl",
		                                       'm', ItemComponent.getItemStack(EnumComponentType.LASER_MEDIUM_EMPTY),
		                                       'r', "blockRedstone",
		                                       'l', "blockLapis"));
		
		// Laser lift is ...
		Object oreMagnetizer = itemStackMachineCasings[0];
		if (WarpDriveConfig.isGregTechLoaded) {
			oreMagnetizer = WarpDriveConfig.getItemStackOrFire("gregtech:machine", 420);	// Basic polarizer
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			oreMagnetizer = WarpDriveConfig.getItemStackOrFire("ic2:te", 37); // Magnetizer
		} else if (WarpDriveConfig.isThermalExpansionLoaded) {
			oreMagnetizer = WarpDriveConfig.getItemStackOrFire("forge:bucketfilled", 0, "{FluidName: \"ender\", Amount: 1000}"); // Ender bucket
		} else if (OreDictionary.doesOreNameExist("ingotRedstoneAlloy") && !OreDictionary.getOres("ingotRedstoneAlloy").isEmpty()) {// EnderIO
			oreMagnetizer = "ingotRedstoneAlloy";
		}
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockLift), false, "rmw", "plc", "glg",
		                                       'r', Items.REDSTONE,
		                                       'w', Blocks.WOOL,
		                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'm', oreMagnetizer,
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                       'g', "paneGlassColorless"));
		
		// Iridium block is just that
		if (WarpDriveConfig.isGregTechLoaded) {
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       new ItemStack(WarpDrive.blockIridium), "iii", "iii", "iii",
			                                           'i', "xxx"));
			ItemStack itemStackIridiumAlloy = WarpDriveConfig.getItemStackOrFire("gregtech:meta_item_1", 12032); // ore:plateIridium
			WarpDrive.register(new ShapelessOreRecipe(groupComponents,
			                                          new ItemStack(itemStackIridiumAlloy.getItem(), 9), new ItemStack(WarpDrive.blockIridium)));
			
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       new ItemStack(WarpDrive.blockIridium), "iii", "iii", "iii",
			                                       'i', "plateAlloyIridium"));
			ItemStack itemStackIridiumAlloy = WarpDriveConfig.getOreDictionaryEntry("plateAlloyIridium");
			WarpDrive.register(new ShapelessOreRecipe(groupComponents,
			                                          new ItemStack(itemStackIridiumAlloy.getItem(), 9), new ItemStack(WarpDrive.blockIridium)));
			
		} else if (WarpDriveConfig.isThermalExpansionLoaded) {
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       new ItemStack(WarpDrive.blockIridium), "ses", "ele", "ses",
			                                       'l', "ingotLumium",
			                                       's', "ingotSignalum",
			                                       'e', "ingotEnderium"));
			// no uncrafting
			
		} else if (WarpDriveConfig.isEnderIOLoaded) {
			final ItemStack itemStackVibrantAlloy = WarpDriveConfig.getItemStackOrFire("enderio:item_alloy_ingot", 2);
			final ItemStack itemStackRedstoneAlloy = WarpDriveConfig.getItemStackOrFire("enderio:item_alloy_ingot", 3);
			final ItemStack itemStackFranckNZombie = WarpDriveConfig.getItemStackOrFire("enderio:item_material", 42);
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       new ItemStack(WarpDrive.blockIridium, 4), "ses", "ele", "ses",
			                                       'l', itemStackFranckNZombie,
			                                       's', itemStackRedstoneAlloy,
			                                       'e', itemStackVibrantAlloy));
			// no uncrafting
			
		} else {
			WarpDrive.register(new ShapedOreRecipe(groupComponents,
			                                       new ItemStack(WarpDrive.blockIridium), "ded", "yty", "ded",
			                                       't', Items.GHAST_TEAR,
			                                       'd', Items.DIAMOND,
			                                       'e', Items.EMERALD,
			                                       'y', Items.ENDER_EYE));
		}
		
		// Laser camera is just Laser + Camera
		if (OreDictionary.doesOreNameExist("circuitBasic") && !OreDictionary.getOres("circuitBasic").isEmpty()) {// comes with IndustrialCraft2
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockLaserCamera), false, "rlr", "rsr", "rcr",
			                                       'r', rubberOrLeather,
			                                       's', "circuitBasic",
			                                       'l', WarpDrive.blockLaser,
			                                       'c', WarpDrive.blockCamera));
		} else {
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockLaserCamera), false, "rlr", "rsr", "rcr",
			                                       'r', rubberOrLeather,
			                                       's', "nuggetGold",
			                                       'l', WarpDrive.blockLaser,
			                                       'c', WarpDrive.blockCamera));
		}
		
		// Weapon controller is diamond sword with Ship controller
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockWeaponController), false, "rwr", "rsr", "rcr",
		                                       'r', rubberOrLeather,
		                                       's', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
		                                       'w', Items.DIAMOND_SWORD,
		                                       'c', WarpDrive.blockShipController[EnumTier.ADVANCED.getIndex()]));
		
		// Camera is 1 daylight sensor, 2 motors, 1 computer interface, 2 glass panel, 1 Tuning diamond, 1 LV Machine casing
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockCamera), false, "gtd", "zlm", "gtc",
		                                       't', itemStackMotors[0],
		                                       'z', ItemComponent.getItemStack(EnumComponentType.ZOOM),
		                                       'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'm', itemStackMachineCasings[0],
		                                       'l', Blocks.DAYLIGHT_DETECTOR,
		                                       'g', "paneGlassColorless"));
		
		// Monitor is 3 flat screen, 1 computer interface, 1 Tuning diamond, 1 LV Machine casing
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockMonitor), false, "fd ", "fm ", "fc ",
		                                       'f', ItemComponent.getItemStack(EnumComponentType.FLAT_SCREEN),
		                                       'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'm', itemStackMachineCasings[0]));
		
		// Ship scanner is creative only => no recipe
		/*
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockShipScanner), false, "ici", "isi", "mcm",
		                                       'm', mfsu,
		                                       'i', iridiumAlloy,
		                                       'c', "circuitAdvanced",
		                                       's', WarpDriveConfig.getModItemStack("ic2", "te", 64) )); // Scanner
		/**/
		
		// Laser tree farm is 2 motors, 2 lenses, 1 computer interface, 1 LV Machine casing, 1 diamond axe, 2 glass pane
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockLaserTreeFarm), false, "glg", "tlt", "amc",
		                                       't', itemStackMotors[0],
		                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'm', itemStackMachineCasings[0],
		                                       'a', Items.DIAMOND_AXE,
		                                       'g', "paneGlassColorless"));
		
		// Transporter Beacon is 1 Ender pearl, 1 Memory crystal, 1 Diamond crystal, 2 sticks
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockTransporterBeacon), false, " e ", " m ", "sds",
		                                       'e', Items.ENDER_PEARL,
		                                       'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL),
		                                       's', Items.STICK));
		
		// Transporter containment is 1 HV Machine casing, 2 Ender crystal, gives 2
		if (!WarpDriveConfig.ACCELERATOR_ENABLE) {
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockTransporterContainment, 2), false, " e ", " m ", " e ",
			                                       'm', itemStackMachineCasings[2],
			                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL)));
		} else {
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockTransporterContainment, 2), false, " e ", " m ", " e ",
			                                       'm', "blockElectromagnet2",
			                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL)));
		}
		
		// Transporter core is 1 HV Machine casing, 1 Emerald crystal, 1 Capacitive crystal, 1 Diamond crystal, 1 Power interface, 1 Computer interface
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockTransporterCore), false, " E ", "pmd", " c ",
		                                       'm', itemStackMachineCasings[2],
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
		                                       'E', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE)));
		
		// Transporter scanner is 1 HV Machine casing, 1 Emerald crystal, 3 Capacitive crystal, 2 Ender crystal
		if (!WarpDriveConfig.ACCELERATOR_ENABLE) {
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockTransporterScanner), false, " E ", "eme", "CCC",
			                                       'm', itemStackMachineCasings[2],
			                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                       'E', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
			                                       'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL)));
		} else {
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockTransporterScanner), false, " E ", "eme", "CCC",
			                                       'm', "blockElectromagnet2",
			                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                       'E', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
			                                       'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL)));
		}
		
		// IC2 needs to be loaded for the following 2 recipes
		if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			final ItemStack itemStackOverclockedHeatVent = WarpDriveConfig.getItemStackOrFire("ic2:overclocked_heat_vent", 0); // IC2 Overclocked heat vent
			// (there's no coolant in GT6 version 6.06.05, nor in GregTech CE version 1.12.2-0.4.5.9, so we're falling back to IC2)
			final ItemStack itemStackReactorCoolant1 = WarpDriveConfig.getItemStackOrFire("ic2:hex_heat_storage", 0);          // IC2 60k coolant cell
			final ItemStack itemStackReactorCoolant2 = WarpDriveConfig.getItemStackOrFire("ic2:hex_heat_storage", 0);          // IC2 60k coolant cell
			
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.itemIC2reactorLaserFocus), false, "cld", "lhl", "dlc",
			                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
			                                       'h', itemStackOverclockedHeatVent,
			                                       'c', itemStackReactorCoolant1,
			                                       'd', itemStackReactorCoolant2));
			
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockIC2reactorLaserCooler), false, "gCp", "lme", "gCc",
			                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
			                                       'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
			                                       'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
			                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
			                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			                                       'g', "paneGlassColorless",
			                                       'm', itemStackMachineCasings[1]));
		}
		
		// Cloaking core is 3 Cloaking coils, 4 iridium blocks, 1 ship controller, 1 power interface
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockCloakingCore), false, "ici", "csc", "ipi",
		                                       'i', WarpDrive.blockIridium,
		                                       'c', WarpDrive.blockCloakingCoil,
		                                       's', WarpDrive.blockShipController[EnumTier.SUPERIOR.getIndex()],
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE)));
		
		// Cloaking coil is 1 Titanium plate, 4 reinforced iridium plate, 1 EV Machine casing (Ti) or 1 Beacon, 4 emerald, 4 diamond
		final Object oreGoldIngotOrCoil = WarpDriveConfig.getOreOrItemStack("gregtech:wire_coil", 3, // GregTech Tungstensteel Coil block
		                                                                    "ic2:crafting", 5,                    // IC2 Coil
		                                                                    "thermalfoundation:material", 515,           // ThermalFoundation Redstone conductance coil
		                                                                    "immersiveengineering:connector", 8,         // ImmersiveEngineering HV Transformer (coils wires are too cheap)
		                                                                    "enderio:item_power_conduit", 2,             // EnderIO Ender energy conduit
		                                                                    "minecraft:gold_ingot", 0);
		final Object oreGoldIngotOrTitaniumPlate = WarpDriveConfig.getOreOrItemStack("ore:plateTitanium", 0,
		                                                                             "advanced_solar_panels:crafting", 0,	// ASP Sunnarium
		                                                                             "ore:plateDenseSteel", 0,
		                                                                             "thermalfoundation:glass", 6,                // ThermalFoundation Hardened Platinum Glass
		                                                                             "immersiveengineering:metal_device1", 3,     // ImmersiveEngineering Thermoelectric Generator
		                                                                             "enderio:item_alloy_ingot", 2,	            // EnderIO Vibrant alloy (ore:ingotVibrantAlloy)
		                                                                             "minecraft:gold_ingot", 0);
		final Object oreEmeraldOrIridiumPlate = WarpDriveConfig.getOreOrItemStack("ore:plateAlloyIridium", 0, // IndustrialCraft2 and GregTech
		                                                                          "enderio:item_material", 42,             // EnderIO Frank'N'Zombie
		                                                                          "ore:ingotLumium", 0,                           // ThermalFoundation lumium ingot
		                                                                          "ore:gemEmerald", 0);
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockCloakingCoil), false, "iti", "cmc", "iti",
		                                       't', oreGoldIngotOrTitaniumPlate,
		                                       'i', oreEmeraldOrIridiumPlate,
		                                       'c', oreGoldIngotOrCoil,
		                                       'm', itemStackMachineCasings[3] ));
		
		// Enantiomorphic reactor core is 1 EV Machine casing, 4 Capacitive crystal, 1 Computer interface, 1 Power interface, 2 Lenses
		if (!WarpDriveConfig.ACCELERATOR_ENABLE) {
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       WarpDrive.blockEnanReactorCore[EnumTier.BASIC.getIndex()], false, "CpC", "lml", "CcC",
			                                       'm', ItemComponent.getItemStack(EnumComponentType.REACTOR_CORE),
			                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
			                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
			                                       'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL)));
		} else {
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       WarpDrive.blockEnanReactorCore[EnumTier.BASIC.getIndex()], false, " p ", "lCl", "cpm",
			                                       'C', ItemComponent.getItemStack(EnumComponentType.REACTOR_CORE),
			                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
			                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
			                                       'm', itemStackMachineCasings[2]));
		}
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockEnanReactorCore[EnumTier.ADVANCED.getIndex()], false, " c ", "lRl", " c ",
		                                       'R', WarpDrive.blockEnanReactorCore[EnumTier.BASIC.getIndex()],
		                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL) ));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockEnanReactorCore[EnumTier.SUPERIOR.getIndex()], false, "lSl", " R ", "lSl",
		                                       'R', WarpDrive.blockEnanReactorCore[EnumTier.ADVANCED.getIndex()],
		                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
		                                       'S', ItemComponent.getItemStack(EnumComponentType.SUPERCONDUCTOR) ));
		
		// Enantiomorphic reactor frame is 1 Hull block, 4 Iron bars, gives 4
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockEnanReactorFramePlain[EnumTier.BASIC.getIndex()], 4, 0), false, " b ", "bhb", " b ",
		                                       'h', "blockHull1_plain",
		                                       'b', ironBars ));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockEnanReactorFramePlain[EnumTier.ADVANCED.getIndex()], 4, 0), false, " b ", "bhb", " b ",
		                                       'h', "blockHull2_plain",
		                                       'b', ironBars ));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockEnanReactorFramePlain[EnumTier.SUPERIOR.getIndex()], 4, 0), false, " b ", "bhb", " b ",
		                                       'h', "blockHull3_plain",
		                                       'b', ironBars ));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockEnanReactorFrameGlass[EnumTier.BASIC.getIndex()], 4, 0), false, " b ", "bhb", " b ",
		                                       'h', "blockHull1_glass",
		                                       'b', ironBars ));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockEnanReactorFrameGlass[EnumTier.ADVANCED.getIndex()], 4, 0), false, " b ", "bhb", " b ",
		                                       'h', "blockHull2_glass",
		                                       'b', ironBars ));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockEnanReactorFrameGlass[EnumTier.SUPERIOR.getIndex()], 4, 0), false, " b ", "bhb", " b ",
		                                       'h', "blockHull3_glass",
		                                       'b', ironBars ));
		
		// Enantiomorphic reactor frame symbols are from dies
		for (final EnumTier enumTier : EnumTier.nonCreative()) {
			final Block blockFrame = WarpDrive.blockEnanReactorFramePlain[enumTier.getIndex()];
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(blockFrame, 1, 1), false, " y ", " f ", "y y",
			                                       'f', blockFrame,
			                                       'y', "dyeYellow" ));
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(blockFrame, 1, 2), false, " y ", "yf ", " y ",
			                                       'f', blockFrame,
			                                       'y', "dyeYellow" ));
		}
		
		// Enantiomorphic reactor stabilization laser is 1 HV Machine casing, 2 Advanced hull, 1 Computer interface, 1 Power interface, 1 Lense, 1 Redstone, 2 glass pane
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       new ItemStack(WarpDrive.blockEnanReactorLaser), false, "ghr", "ldm", "ghc",
		                                       'd', ItemComponent.getItemStack(EnumComponentType.DIFFRACTION_GRATING),
		                                       'l', ItemComponent.getItemStack(EnumComponentType.LENS),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                       'm', itemStackMachineCasings[1],
		                                       'r', Items.REDSTONE,
		                                       'g', "paneGlassColorless",
		                                       'h', "blockHull2_plain"));
		
		// Basic subspace capacitor is 1 Capacitive crystal + 1 Power interface + 3 paper + 4 iron bars
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockCapacitor[EnumTier.BASIC.getIndex()], false, "iPi", "pcp", "ipi",
		                                       'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
		                                       'i', ironBars,
		                                       'p', Items.PAPER,
		                                       'P', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE) ));
		
		// Advanced subspace capacitor is 4 Basic subspace capacitor + 1 Power interface
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockCapacitor[EnumTier.ADVANCED.getIndex()], false, " c ", "cpc", " c ",
		                                       'c', new ItemStack(WarpDrive.blockCapacitor[EnumTier.BASIC.getIndex()]),
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE) ), "_upgrade");
		// or 4 Capacitive crystal + 1 Gold ingot + 4 Power interface
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockCapacitor[EnumTier.ADVANCED.getIndex()], false, "pcp", "cgc", "pcp",
		                                       'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
		                                       'g', "ingotGold",
		                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE) ), "_direct");
		
		// Superior subspace capacitor is 4 Advanced subspace capacitor + 1 Ender tuned crystal + 4 Iron ingot
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockCapacitor[EnumTier.SUPERIOR.getIndex()], false, "ici", "cec", "ici",
		                                       'c', new ItemStack(WarpDrive.blockCapacitor[EnumTier.ADVANCED.getIndex()]),
		                                       'i', "ingotIron",
		                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL) ));
		// or 4 Capacitive crystal block + 1 Superconductor + 4 Iron ingot
		/*
		WarpDrive.register(new ShapedOreRecipe(WarpDrive.blockCapacitor[EnumTier.SUPERIOR.getIndex()], false, "ici", "csc", "ici",
		                                       'c', @TODO MC1.10 Capacitive crystal block,
		                                       'i', "ingotIron",
		                                       's', ItemComponent.getItemStack(EnumComponentType.SUPERCONDUCTOR) ));
		/**/
		
		// Force field projector is 1 or 2 Electromagnetic Projector + 1 LV/MV/HV Machine casing + 1 Ender crystal + 1 Redstone
		for (final EnumTier enumTier : EnumTier.nonCreative()) {
			final int index = enumTier.getIndex();
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockForceFieldProjectors[index], 1, 0), false, " e ", "pm ", " r ",
			                                       'p', ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR),
			                                       'm', itemStackMachineCasings[index],
			                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                       'r', Items.REDSTONE), "_left");
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockForceFieldProjectors[index], 1, 0), false, " e ", " mp", " r ",
			                                       'p', ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR),
			                                       'm', itemStackMachineCasings[index],
			                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                       'r', Items.REDSTONE), "_right");
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockForceFieldProjectors[index], 1, 1), false, " e ", "pmp", " r ",
			                                       'p', ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR),
			                                       'm', itemStackMachineCasings[index],
			                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                       'r', Items.REDSTONE));
		}
		
		// Force field relay is 2 Coil crystals + 1 LV/MV/HV Machine casing + 1 Ender crystal + 1 Redstone
		for (final EnumTier enumTier : EnumTier.nonCreative()) {
			final int index = enumTier.getIndex();
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockForceFieldRelays[index]), false, " e ", "CmC", " r ",
			                                       'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
			                                       'm', itemStackMachineCasings[index],
			                                       'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                       'r', Items.REDSTONE));
		}
		
		// Decorative blocks
		WarpDrive.register(new ShapedOreRecipe(groupDecorations,
		                                       BlockDecorative.getItemStackNoCache(EnumDecorativeType.PLAIN, 8), false, "sss", "scs", "sss",
		                                       's', Blocks.STONE,
		                                       'c', Items.PAPER));
		WarpDrive.register(new ShapedOreRecipe(groupDecorations,
		                                       BlockDecorative.getItemStackNoCache(EnumDecorativeType.PLAIN, 8), false, "sss", "scs", "sss",
		                                       's', "warpDecorative",
		                                       'c', "dyeWhite"), "_dye");
		WarpDrive.register(new ShapedOreRecipe(groupDecorations,
		                                       BlockDecorative.getItemStackNoCache(EnumDecorativeType.ENERGIZED, 8), false, "sss", "scs", "sss",
		                                       's', "warpDecorative",
		                                       'c', "dyeRed"), "_dye");
		WarpDrive.register(new ShapedOreRecipe(groupDecorations,
		                                       BlockDecorative.getItemStackNoCache(EnumDecorativeType.NETWORK, 8), false, "sss", "scs", "sss",
		                                       's', "warpDecorative",
		                                       'c', "dyeBlue"), "_dye");
		
		// Lamp
		WarpDrive.register(new ShapedOreRecipe(groupDecorations,
		                                       WarpDrive.blockLamp_bubble, false, " g ", "glg", "h  ",
		                                       'g', "blockGlass",
		                                       'l', Blocks.REDSTONE_LAMP,
		                                       'h', "blockHull1_plain"));
		WarpDrive.register(new ShapedOreRecipe(groupDecorations,
		                                       WarpDrive.blockLamp_flat, false, " g ", "glg", " h ",
		                                       'g', "blockGlass",
		                                       'l', Blocks.REDSTONE_LAMP,
		                                       'h', "blockHull1_plain"));
		WarpDrive.register(new ShapedOreRecipe(groupDecorations,
		                                       WarpDrive.blockLamp_long, false, " g ", "glg", "  h",
		                                       'g', "blockGlass",
		                                       'l', Blocks.REDSTONE_LAMP,
		                                       'h', "blockHull1_plain"));
		
		// Warp helmet
		WarpDrive.register(new ShapedOreRecipe(groupTools,
		                                       WarpDrive.itemWarpArmor[EntityEquipmentSlot.HEAD.getIndex()], false, "ggg", "gig", "wcw",
		                                       'i', Items.IRON_HELMET,
		                                       'w', Blocks.WOOL,
		                                       'g', "blockGlass",
		                                       'c', ItemComponent.getItemStack(EnumComponentType.AIR_CANISTER)));
		
		// Warp chestplate
		WarpDrive.register(new ShapedOreRecipe(groupTools,
		                                       WarpDrive.itemWarpArmor[EntityEquipmentSlot.CHEST.getIndex()], false, "gcg", "wiw", "GmG",
		                                       'i', Items.IRON_CHESTPLATE,
		                                       'w', Blocks.WOOL,
		                                       'g', "blockHull3_glass",
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MOTOR),
		                                       'G', "ingotGold",
		                                       'c', ItemComponent.getItemStack(EnumComponentType.AIR_CANISTER)));
		
		// Warp Leggings
		WarpDrive.register(new ShapedOreRecipe(groupTools,
		                                       WarpDrive.itemWarpArmor[EntityEquipmentSlot.LEGS.getIndex()], false, "gig", "m m", "w w",
		                                       'i', Items.IRON_LEGGINGS,
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MOTOR),
		                                       'w', Blocks.WOOL,
		                                       'g', "blockHull2_glass"));
		
		// Warp boots
		WarpDrive.register(new ShapedOreRecipe(groupTools,
		                                       WarpDrive.itemWarpArmor[EntityEquipmentSlot.FEET.getIndex()], false, "wiw", "r r", "   ",
		                                       'i', Items.IRON_BOOTS,
		                                       'w', Blocks.WOOL,
		                                       'r', rubberOrLeather));
		
		// Tuning fork variations
		for (final EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
			final int damageColor = enumDyeColor.getDyeDamage();
			
			// crafting tuning fork
			WarpDrive.register(new ShapedOreRecipe(groupTools,
			                                       new ItemStack(WarpDrive.itemTuningFork, 1, damageColor), false, "  q", "iX ", " i ",
			                                       'q', "gemQuartz",
			                                       'i', "ingotIron",
			                                       'X', oreDyes.get(enumDyeColor) ));
			
			// changing colors
			WarpDrive.register(new ShapelessOreRecipe(groupTools,
			                                          new ItemStack(WarpDrive.itemTuningFork, 1, damageColor),
			                                          oreDyes.get(enumDyeColor),
			                                          "itemTuningFork"), "_dye");
		}
		
		// Tuning driver crafting
		WarpDrive.register(new ShapedOreRecipe(groupTools,
		                                       new ItemStack(WarpDrive.itemTuningDriver, 1, ItemTuningDriver.MODE_VIDEO_CHANNEL), false, "  q", "pm ", "d  ",
		                                       'q', "gemQuartz",
		                                       'p', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
		                                       'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
		                                       'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL) ));
		
		// Tuning driver configuration
		WarpDrive.register(new RecipeTuningDriver(groupTools,
		                                          new ItemStack(WarpDrive.itemTuningDriver, 1, ItemTuningDriver.MODE_VIDEO_CHANNEL),
		                                          new ItemStack(Items.REDSTONE), 7, "_video1"), "_video2");
		WarpDrive.register(new RecipeTuningDriver(groupTools,
		                                          new ItemStack(WarpDrive.itemTuningDriver, 1, ItemTuningDriver.MODE_BEAM_FREQUENCY),
		                                          new ItemStack(Items.REDSTONE), 4, "_bream_frequency1"), "_bream_frequency2");
		WarpDrive.register(new RecipeTuningDriver(groupTools,
		                                          new ItemStack(WarpDrive.itemTuningDriver, 1, ItemTuningDriver.MODE_CONTROL_CHANNEL),
		                                          new ItemStack(Items.REDSTONE), 7, "_control_channel1"), "_control_channel2");
		
		// HULL blocks and variations
		initDynamicHull();
		
		// Sirens
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockSirenIndustrial[EnumTier.BASIC.getIndex()], "pip", "pip", "NcN",
		                                       'p', "plankWood",
		                                       'i', "ingotIron",
		                                       'N', new ItemStack(Blocks.NOTEBLOCK, 1),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockSirenIndustrial[EnumTier.ADVANCED.getIndex()], " I ", "ISI", " I ",
		                                       'I', "ingotGold",
		                                       'S', WarpDrive.blockSirenIndustrial[EnumTier.BASIC.getIndex()]));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockSirenIndustrial[EnumTier.SUPERIOR.getIndex()], " I ", "ISI", " I ",
		                                       'I', "gemDiamond",
		                                       'S', WarpDrive.blockSirenIndustrial[EnumTier.ADVANCED.getIndex()]));
		
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockSirenMilitary[EnumTier.BASIC.getIndex()], "ipi", "ipi", "NcN",
		                                       'p', "plankWood",
		                                       'i', "ingotIron",
		                                       'N', new ItemStack(Blocks.NOTEBLOCK, 1),
		                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockSirenMilitary[EnumTier.ADVANCED.getIndex()], " I ", "ISI", " I ",
		                                       'I', "ingotGold",
		                                       'S', WarpDrive.blockSirenMilitary[EnumTier.BASIC.getIndex()]));
		WarpDrive.register(new ShapedOreRecipe(groupMachines,
		                                       WarpDrive.blockSirenMilitary[EnumTier.SUPERIOR.getIndex()], " I ", "ISI", " I ",
		                                       'I', "gemDiamond",
		                                       'S', WarpDrive.blockSirenMilitary[EnumTier.ADVANCED.getIndex()]));
		
		// iron or steel
		Object ingotIronOrSteel = "ingotIron";
		if (OreDictionary.doesOreNameExist("ingotSteel") && !OreDictionary.getOres("ingotSteel").isEmpty()) {
			ingotIronOrSteel = "ingotSteel";
		}
		
		if (WarpDriveConfig.ACCELERATOR_ENABLE) {
			
			// Void shells is hull + power interface + steel or iron
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockVoidShellPlain, 6), "psh", "s s", "hsp",
			                                       'h', "blockHull1_plain",
			                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			                                       's', ingotIronOrSteel));
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockVoidShellGlass, 6), "psh", "s s", "hsp",
			                                       'h', "blockHull1_glass",
			                                       'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			                                       's', ingotIronOrSteel));
			
			// Electromagnetic cell
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.itemElectromagneticCell, 2), "iri", "i i", "ici",
			                                       'i', ironBars,
			                                       'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
			                                       'r', Items.REDSTONE));
			
			// Accelerator control point
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockAcceleratorControlPoint), "hd ", "vc ", "he ",
			                                       'h', Blocks.HOPPER,
			                                       'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
			                                       'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
			                                       'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
			                                       'v', "blockVoidShell"));
			
			// Particles injector
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockParticlesInjector), "mm ", "vvp", "mmc",
			                                       'p', Blocks.PISTON,
			                                       'm', "blockElectromagnet1",
			                                       'c', WarpDrive.blockAcceleratorControlPoint,
			                                       'v', "blockVoidShell"));
			
			// Accelerator controller
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockAcceleratorController), "MmM", "mcm", "MmM",
			                                       'M', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL),
			                                       'm', "blockElectromagnet1",
			                                       'c', WarpDrive.blockAcceleratorControlPoint));
			
			// Particles collider
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockParticlesCollider), "hoh", "odo", "hoh",
			                                       'h', "blockHull1_plain",
			                                       'o', Blocks.OBSIDIAN,
			                                       'd', Items.DIAMOND));
			
			// Chillers
			Object snowOrIce = Blocks.SNOW;
			if (OreDictionary.doesOreNameExist("dustCryotheum") && !OreDictionary.getOres("dustCryotheum").isEmpty()) {
				snowOrIce = Blocks.ICE;
			}
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockChillers[EnumTier.BASIC.getIndex()]), "wgw", "sms", "bMb",
			                                       'w', snowOrIce,
			                                       'g', Items.GHAST_TEAR,
			                                       's', ingotIronOrSteel,
			                                       'm', itemStackMotorLV,
			                                       'b', ironBars,
			                                       'M', "blockElectromagnet1"));
			
			Object nitrogen = Blocks.ICE;
			if (OreDictionary.doesOreNameExist("dustCryotheum") && !OreDictionary.getOres("dustCryotheum").isEmpty()) {
				nitrogen = Blocks.PACKED_ICE;
			}
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockChillers[EnumTier.ADVANCED.getIndex()]), "ngn", "dmd", "bMb",
			                                       'n', nitrogen,
			                                       'g', Items.GHAST_TEAR,
			                                       'd', Items.DIAMOND,
			                                       'm', itemStackMotorMV,
			                                       'b', ironBars,
			                                       'M', "blockElectromagnet2"));
			
			Object helium = Blocks.PACKED_ICE;
			if (OreDictionary.doesOreNameExist("dustCryotheum") && !OreDictionary.getOres("dustCryotheum").isEmpty()) {
				helium = "dustCryotheum";
			}
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockChillers[EnumTier.SUPERIOR.getIndex()]), "hgh", "eme", "bMb",
			                                       'h', helium,
			                                       'g', Items.GHAST_TEAR,
			                                       'e', Items.EMERALD,
			                                       'm', itemStackMotorHV,
			                                       'b', ironBars,
			                                       'M', "blockElectromagnet3"));
			
			// Lower tier coil is iron, copper or coil
			final Object ironIngotOrCopperIngotOrCoil = WarpDriveConfig.getOreOrItemStack("gregtech:wire_coil", 0, // GregTech Cupronickel Coil block
			                                                                              "ic2:crafting", 5,                    // IC2 Coil
			                                                                              "thermalfoundation:material", 513,           // ThermalFoundation Redstone reception coil
			                                                                              "immersiveengineering:wirecoil", 1,          // ImmersiveEngineering MV wire coil
			                                                                              "enderio:item_power_conduit", 1,             // EnderIO Enhanced energy conduit
			                                                                              "ore:ingotCopper", 0,
			                                                                              "minecraft:iron_ingot", 0);
			
			// Normal electromagnets
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockElectromagnetPlain[EnumTier.BASIC.getIndex()], 4), "   ", "ccc", "Cmt",
			                                       'c', ironIngotOrCopperIngotOrCoil,
			                                       't', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
			                                       'm', itemStackMotorLV,
			                                       'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL)));
			WarpDrive.register(new ShapedOreRecipe(groupMachines,
			                                       new ItemStack(WarpDrive.blockElectromagnetGlass[EnumTier.BASIC.getIndex()], 4), "mgm", "g g", "mgm",
			                                       'g', Blocks.GLASS,
			                                       'm', WarpDrive.blockElectromagnetPlain[EnumTier.BASIC.getIndex()]));
			
			// Advanced electromagnets
			WarpDrive.register(new RecipeParticleShapedOre(groupMachines,
			                                               new ItemStack(WarpDrive.blockElectromagnetPlain[EnumTier.ADVANCED.getIndex()], 6), "mpm", "pip", "mpm",
			                                               'i', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.ION, 200),
			                                               'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			                                               'm', WarpDrive.blockElectromagnetPlain[EnumTier.BASIC.getIndex()]));
			WarpDrive.register(new RecipeParticleShapedOre(groupMachines,
			                                               new ItemStack(WarpDrive.blockElectromagnetGlass[EnumTier.ADVANCED.getIndex()], 6), "mpm", "pip", "mpm",
			                                               'i', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.ION, 200),
			                                               'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			                                               'm', WarpDrive.blockElectromagnetGlass[EnumTier.BASIC.getIndex()]));
			
			// Superior electromagnets
			WarpDrive.register(new RecipeParticleShapedOre(groupMachines,
			                                               new ItemStack(WarpDrive.blockElectromagnetPlain[EnumTier.SUPERIOR.getIndex()], 6), "mtm", "sps", "mMm",
			                                               't', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
			                                               's', ItemComponent.getItemStack(EnumComponentType.SUPERCONDUCTOR),
			                                               'p', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.PROTON, 24),
			                                               'M', itemStackMotorHV,
			                                               'm', WarpDrive.blockElectromagnetPlain[EnumTier.ADVANCED.getIndex()]));
			WarpDrive.register(new RecipeParticleShapedOre(groupMachines,
			                                               new ItemStack(WarpDrive.blockElectromagnetGlass[EnumTier.SUPERIOR.getIndex()], 6), "mtm", "sps", "mMm",
			                                               't', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
			                                               's', ItemComponent.getItemStack(EnumComponentType.SUPERCONDUCTOR),
			                                               'p', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.PROTON, 24),
			                                               'M', itemStackMotorHV,
			                                               'm', WarpDrive.blockElectromagnetGlass[EnumTier.ADVANCED.getIndex()]));
			
			// ICBM classic
			if (WarpDriveConfig.isICBMClassicLoaded) {
				// antimatter
				final ItemStack itemStackAntimatterExplosive = WarpDriveConfig.getItemStackOrFire("icbmclassic:explosives", 22); // Antimatter Explosive
				removeRecipe(itemStackAntimatterExplosive);
				WarpDrive.register(new RecipeParticleShapedOre(groupComponents,
				                                               itemStackAntimatterExplosive, "aaa", "ana", "aaa",
				                                               'a', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.ANTIMATTER, 1000),
				                                               'n', WarpDriveConfig.getItemStackOrFire("icbmclassic:icbmCExplosive", 15)));
				
				// red matter
				final ItemStack itemStackRedMatterExplosive = WarpDriveConfig.getItemStackOrFire("icbmclassic:explosives", 23); // Red Matter Explosive
				removeRecipe(itemStackRedMatterExplosive);
				WarpDrive.register(new RecipeParticleShapedOre(groupComponents,
				                                               itemStackRedMatterExplosive, "sss", "sas", "sss",
				                                               's', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.STRANGE_MATTER, 1000),
						                                       'a', WarpDriveConfig.getItemStackOrFire("icbmclassic:icbmCExplosive", 22)));
			}
		}
	}
	
	private static void initDynamicHull() {
		// Hull blocks plain
		// (BlockColored.func_150031_c is converting wool metadata into dye metadata)
		// Tier 1 = 5 obsidian + 4 reinforced stone gives 10
		// Tier 1 = 5 stone + 4 steel ingots gives 10
		// Tier 1 = 5 stone + 4 iron ingots gives 10
		// Tier 1 = 5 stone + 4 bronze ingots gives 5
		// Tier 1 = 5 stone + 4 aluminium ingots gives 3
		if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			final ItemStack reinforcedStone = WarpDriveConfig.getItemStackOrFire("ic2:resource", 11); // IC2 reinforced stone
			WarpDrive.register(new ShapedOreRecipe(groupHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.BASIC.getIndex()][0], 10, 0), false, "cbc", "bcb", "cbc",
			                                       'b', reinforcedStone,
			                                       'c', Blocks.OBSIDIAN ));
		} else if (OreDictionary.doesOreNameExist("ingotSteel") && !OreDictionary.getOres("ingotSteel").isEmpty()) {
			WarpDrive.register(new ShapedOreRecipe(groupHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.BASIC.getIndex()][0], 10, 0), false, "cbc", "bcb", "cbc",
			                                       'b', "ingotSteel",
			                                       'c', "stone" ));
		} else {
			WarpDrive.register(new ShapedOreRecipe(groupHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.BASIC.getIndex()][0], 10, 0), false, "cbc", "bcb", "cbc",
			                                       'b', "ingotIron",
			                                       'c', "stone" ));
		}
		if (OreDictionary.doesOreNameExist("ingotBronze") && !OreDictionary.getOres("ingotBronze").isEmpty()) {
			WarpDrive.register(new ShapedOreRecipe(groupHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.BASIC.getIndex()][0], 5, 0), false, "cbc", "bcb", "cbc",
			                                       'b', "ingotBronze",
			                                       'c', "stone" ));
		}
		if (OreDictionary.doesOreNameExist("ingotAluminium") && !OreDictionary.getOres("ingotAluminium").isEmpty()) {
			WarpDrive.register(new ShapedOreRecipe(groupHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.BASIC.getIndex()][0], 3, 0), false, "cbc", "bcb", "cbc",
			                                       'b', "ingotAluminium",
			                                       'c', "stone" ));
		} else if (OreDictionary.doesOreNameExist("ingotAluminum") && !OreDictionary.getOres("ingotAluminum").isEmpty()) {
			WarpDrive.register(new ShapedOreRecipe(groupHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.BASIC.getIndex()][0], 3, 0), false, "cbc", "bcb", "cbc",
			                                       'b', "ingotAluminum",
			                                       'c', "stone" ));
		}
		
		// Tier 2 = 4 Tier 1 + 4 GregTech 5 TungstenSteel reinforced block, IC2 Carbon plate, DarkSteel ingots or obsidian gives 4
		final Object oreObsidianTungstenSteelPlate = WarpDriveConfig.getOreOrItemStack("ore:plateTungstenSteel", 0, // GregTech CE TungstenSteel Plate
		                                                                               "ic2:crafting", 15,                       // IC2 Carbon plate
		                                                                               "thermalfoundation:glass", 3,                    // ThermalFoundation Hardened glass
		                                                                               "ore:ingotDarkSteel", 0,                         // EnderIO DarkSteel ingot
		                                                                               "minecraft:obsidian", 0);
		for (EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
			final int metadataColor = enumDyeColor.getMetadata();
			WarpDrive.register(new ShapedOreRecipe(groupTaintedHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.ADVANCED.getIndex()][0], 4, metadataColor), false, "cbc", "b b", "cbc",
			                                       'b', new ItemStack(WarpDrive.blockHulls_plain[EnumTier.BASIC.getIndex()][0], 1, metadataColor),
			                                       'c', oreObsidianTungstenSteelPlate ));
			WarpDrive.register(new ShapedOreRecipe(groupTaintedHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.ADVANCED.getIndex()][0], 4, metadataColor), false, "cbc", "bXb", "cbc",
			                                       'b', "blockHull1_plain",
			                                       'c', oreObsidianTungstenSteelPlate,
			                                       'X', oreDyes.get(enumDyeColor) ), "_dye");
		}
		
		// Tier 3 = 4 Tier 2 + 1 GregTech naquadah plate, IC2 iridium plate, EnderIO pulsating crystal or diamond gives 4
		final Object oreDiamondOrNaquadahPlate = WarpDriveConfig.getOreOrItemStack("ore:plateNaquadah", 0, // GregTech CE Naquadah plate
		                                                                           "ore:plateAlloyIridium", 0,          // IC2 Iridium alloy
		                                                                           "ore:itemPulsatingCrystal", 0,                  // EnderIO Pulsating crystal
		                                                                           "ore:gemDiamond", 0);
		for (EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
			final int metadataColor = enumDyeColor.getMetadata();
			WarpDrive.register(new ShapedOreRecipe(groupTaintedHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.SUPERIOR.getIndex()][0], 4, metadataColor), false, " b ", "bcb", " b ",
			                                       'b', new ItemStack(WarpDrive.blockHulls_plain[EnumTier.ADVANCED.getIndex()][0], 1, metadataColor),
			                                       'c', oreDiamondOrNaquadahPlate ));
			WarpDrive.register(new ShapedOreRecipe(groupTaintedHulls,
			                                       new ItemStack(WarpDrive.blockHulls_plain[EnumTier.SUPERIOR.getIndex()][0], 4, metadataColor), false, "Xb ", "bcb", " b ",
			                                       'b', "blockHull2_plain",
			                                       'c', oreDiamondOrNaquadahPlate,
			                                       'X', oreDyes.get(enumDyeColor) ), "_dye");
		}
		
		// Hull blocks variation
		for (final EnumTier enumTier : EnumTier.nonCreative()) {
			int index = enumTier.getIndex();
			for (EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
				final int metadataColor = enumDyeColor.getMetadata();
				
				// crafting glass
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_glass[index], 4, metadataColor), false, "gpg", "pFp", "gpg",
				                                       'g', "blockGlass",
				                                       'p', new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, metadataColor),
				                                       'F', "dustGlowstone" ));
				
				// crafting stairs
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_stairs[index][metadataColor], 4), false, "p  ", "pp ", "ppp",
				                                       'p', new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, metadataColor) ));
				
				// uncrafting
				WarpDrive.register(new ShapelessOreRecipe(groupHulls,
				                                          new ItemStack(WarpDrive.blockHulls_plain[index][0], 6, metadataColor),
				                                          WarpDrive.blockHulls_stairs[index][metadataColor],
				                                          WarpDrive.blockHulls_stairs[index][metadataColor],
				                                          WarpDrive.blockHulls_stairs[index][metadataColor],
				                                          WarpDrive.blockHulls_stairs[index][metadataColor] ));
				
				// smelting tiled
				GameRegistry.addSmelting(
						new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, metadataColor),
						new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, metadataColor),
						0);
				
				// uncrafting tiled
				WarpDrive.register(new ShapelessOreRecipe(groupHulls,
				                                          new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, metadataColor),
				        new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, metadataColor)));
				/*
				// crafting omnipanel
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_omnipanel[index], 16, metadataColor), false, "ggg", "ggg",
						'g', new ItemStack(WarpDrive.blockHulls_glass[index], 1, metadataColor)));
				
				// uncrafting omnipanel
				WarpDrive.register(new ShapelessOreRecipe(groupHulls,
				                                          new ItemStack(WarpDrive.blockHulls_glass[index], 3, metadataColor),
				                                          new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, metadataColor),
				                                          new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, metadataColor),
				                                          new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, metadataColor),
				                                          new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, metadataColor),
				                                          new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, metadataColor),
				                                          new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, metadataColor),
				                                          new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, metadataColor),
				                                          new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, metadataColor) ));
				/**/
				// crafting slab
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 6, 0), false, "bbb",
				                                       'b', new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, metadataColor)));
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 6, 2), false, "b", "b", "b",
				                                       'b', new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, metadataColor)));
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 6, 6), false, "bbb",
				                                       'b', new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, metadataColor)));
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 6, 8), false, "b", "b", "b",
				                                       'b', new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, metadataColor)));
				
				// uncrafting slab
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, metadataColor), false, "s", "s",
				                                       's', new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 1, 0)), "_uncrafting");
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, metadataColor), false, "ss",
				                                       's', new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 1, 2)), "_uncrafting_A");
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, metadataColor), false, "s", "s",
				                                       's', new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 1, 6)), "_uncrafting_B");
				WarpDrive.register(new ShapedOreRecipe(groupHulls,
				                                       new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, metadataColor), false, "ss",
				                                       's', new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 1, 8)), "_uncrafting_C");
				WarpDrive.register(new ShapelessOreRecipe(groupHulls,
				                                          new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 2, 0),
				                                          new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 1, 12)), "_uncrafting");
				WarpDrive.register(new ShapelessOreRecipe(groupHulls,
				                                          new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 2, 6),
				                                          new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 1, 13)), "_uncrafting_A");
				WarpDrive.register(new ShapelessOreRecipe(groupHulls,
				                                          new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 2, 8),
				                                          new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 1, 14)), "_uncrafting_B");
				WarpDrive.register(new ShapelessOreRecipe(groupHulls,
				                                          new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 2, 8),
				                                          new ItemStack(WarpDrive.blockHulls_slab[index][metadataColor], 1, 15)), "_uncrafting_C");
				
				// changing colors
				WarpDrive.register(new ShapelessOreRecipe(groupTaintedHulls,
				                                          new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, metadataColor),
				                                          oreDyes.get(enumDyeColor),
				                                          "blockHull" + index + "_plain"), "_dye" );
				WarpDrive.register(new ShapelessOreRecipe(groupTaintedHulls,
				                                          new ItemStack(WarpDrive.blockHulls_glass[index], 1, metadataColor),
				                                          oreDyes.get(enumDyeColor),
				                                          "blockHull" + index + "_glass"), "_dye" );
				WarpDrive.register(new ShapelessOreRecipe(groupTaintedHulls,
				                                          new ItemStack(WarpDrive.blockHulls_stairs[index][metadataColor], 1),
				                                          oreDyes.get(enumDyeColor),
				                                          "blockHull" + index + "_stairs"), "_dye" );
				WarpDrive.register(new ShapedOreRecipe(groupTaintedHulls,
				                                       new ItemStack(WarpDrive.blockHulls_plain[index][0], 8, metadataColor), false, "###", "#X#", "###",
				                                       '#', "blockHull" + index + "_plain",
				                                       'X', oreDyes.get(enumDyeColor) ), "_dye");
				WarpDrive.register(new ShapedOreRecipe(groupTaintedHulls,
				                                       new ItemStack(WarpDrive.blockHulls_glass[index], 8, metadataColor), false, "###", "#X#", "###",
				                                       '#', "blockHull" + index + "_glass",
				                                       'X', oreDyes.get(enumDyeColor) ), "_dye");
				WarpDrive.register(new ShapedOreRecipe(groupTaintedHulls,
				                                       new ItemStack(WarpDrive.blockHulls_stairs[index][metadataColor], 8), false, "###", "#X#", "###",
				                                       '#', "blockHull" + index + "_stairs",
				                                       'X', oreDyes.get(enumDyeColor) ), "_dye");
			}
		}
	}
	
	private static void removeRecipe(final ItemStack itemStackOutputOfRecipeToRemove) {
		ResourceLocation recipeToRemove = null;
		for (final Entry<ResourceLocation, IRecipe> entryRecipe : ForgeRegistries.RECIPES.getEntries()) {
			final IRecipe recipe = entryRecipe.getValue();
			final ItemStack itemStackRecipeOutput = recipe.getRecipeOutput();
			if ( !itemStackRecipeOutput.isEmpty()
			  && itemStackRecipeOutput.isItemEqual(itemStackOutputOfRecipeToRemove) ) {
				recipeToRemove = entryRecipe.getKey();
				break;
			}
		}
		if (recipeToRemove == null) {
			WarpDrive.logger.error(String.format("Unable to find any recipe to remove with output %s", itemStackOutputOfRecipeToRemove));
		} else {
			WarpDrive.logger.info(String.format("Removing recipe %s with output %s", recipeToRemove, itemStackOutputOfRecipeToRemove));
			((ForgeRegistry<IRecipe>) ForgeRegistries.RECIPES).remove(recipeToRemove);
		}
	}
}
