package cr0s.warpdrive.config;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.ParticleRegistry;
import cr0s.warpdrive.block.detection.BlockSiren;
import cr0s.warpdrive.block.passive.BlockDecorative;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.data.EnumDecorativeType;
import cr0s.warpdrive.data.EnumForceFieldShape;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import cr0s.warpdrive.item.ItemComponent;
import cr0s.warpdrive.item.ItemElectromagneticCell;
import cr0s.warpdrive.item.ItemForceFieldShape;
import cr0s.warpdrive.item.ItemForceFieldUpgrade;
import cr0s.warpdrive.item.ItemTuningDriver;

import java.util.ArrayList;

import net.minecraft.block.BlockColored;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Hold the different recipe sets
 */
public class Recipes {
	
	public static final String[] oreDyes = {
		"dyeBlack",
		"dyeRed",
		"dyeGreen",
		"dyeBrown",
		"dyeBlue",
		"dyePurple",
		"dyeCyan",
		"dyeLightGray",
		"dyeGray",
		"dyePink",
		"dyeLime",
		"dyeYellow",
		"dyeLightBlue",
		"dyeMagenta",
		"dyeOrange",
		"dyeWhite"
	};
	
	@SuppressWarnings("UnusedAssignment")
	public static void initDynamic() {
		// Support iron bars from all mods
		Object ironBars = Blocks.iron_bars;
		if (OreDictionary.doesOreNameExist("barsIron") && !OreDictionary.getOres("barsIron").isEmpty()) {
			ironBars = "barsIron";
		}
		
		// Add Reinforced iridium plate to ore registry as applicable (it's missing in IC2 without GregTech)
		if ( WarpDriveConfig.isIndustrialCraft2Loaded
		  && (!OreDictionary.doesOreNameExist("plateAlloyIridium") || OreDictionary.getOres("plateAlloyIridium").isEmpty()) ) {
			ItemStack iridiumAlloy = WarpDriveConfig.getModItemStack("IC2", "itemPartIridium", 0,   // IC2 Experimental Iridium alloy plate
			                                                         "IC2", "item.itemPartIridium", 0);   // IC2 Classic Iridium alloy plate
			OreDictionary.registerOre("plateAlloyIridium", iridiumAlloy);
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
		
		if (WarpDriveConfig.isGregTech6Loaded) {
			itemStackMachineCasingLV = WarpDriveConfig.getModItemStack("gregtech", "gt.meta.machine", 8651);    // ore:casingMachineSteelGalvanized
			itemStackMachineCasingMV = WarpDriveConfig.getModItemStack("gregtech", "gt.meta.machine", 130);     // ore:casingMachineAluminium
			itemStackMachineCasingHV = WarpDriveConfig.getModItemStack("gregtech", "gt.meta.machine", 8636);    // ore:casingMachineStainlessSteel
			itemStackMachineCasingEV = WarpDriveConfig.getModItemStack("gregtech", "gt.meta.machine", 240);     // ore:casingMachineChromium
			
			itemStackMotorLV = WarpDriveConfig.getModItemStack("gregtech", "gt.multiitem.technological", 12001);    // LV Motor
			itemStackMotorMV = WarpDriveConfig.getModItemStack("gregtech", "gt.multiitem.technological", 12002);    // MV Motor
			itemStackMotorHV = WarpDriveConfig.getModItemStack("gregtech", "gt.multiitem.technological", 12003);    // HV Motor
			itemStackMotorEV = WarpDriveConfig.getModItemStack("gregtech", "gt.multiitem.technological", 12004);    // EV Motor
			
		} else if (WarpDriveConfig.isGregTech5Loaded) {
			itemStackMachineCasingLV = WarpDriveConfig.getModItemStack("gregtech", "gt.blockcasings", 1);
			itemStackMachineCasingMV = WarpDriveConfig.getModItemStack("gregtech", "gt.blockcasings", 2);
			itemStackMachineCasingHV = WarpDriveConfig.getModItemStack("gregtech", "gt.blockcasings", 3);
			itemStackMachineCasingEV = WarpDriveConfig.getModItemStack("gregtech", "gt.blockcasings", 4);
			
			itemStackMotorLV = WarpDriveConfig.getModItemStack("gregtech", "gt.metaitem.01", 32600);    // LV Motor
			itemStackMotorMV = WarpDriveConfig.getModItemStack("gregtech", "gt.metaitem.01", 32601);    // MV Motor
			itemStackMotorHV = WarpDriveConfig.getModItemStack("gregtech", "gt.metaitem.01", 32602);    // HV Motor
			itemStackMotorEV = WarpDriveConfig.getModItemStack("gregtech", "gt.metaitem.01", 32603);    // EV Motor
			
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			itemStackMachineCasingLV = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 0);     // Basic machine casing
			itemStackMachineCasingMV = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 12);    // Advanced machine casing
			itemStackMachineCasingHV = new ItemStack(WarpDrive.blockHighlyAdvancedMachine);
			itemStackMachineCasingEV = new ItemStack(WarpDrive.blockHighlyAdvancedMachine);
			
			ItemStack itemStackMotor = WarpDriveConfig.getModItemStack("IC2", "itemRecipePart", 1,      // IC2 Experimental Electric motor
			                                                           "IC2", "item.itemToolDrill", 0); // IC2 Classic Mining drill
			itemStackMotorHV = itemStackMotor;
			itemStackMotorEV = itemStackMotor;
			
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHighlyAdvancedMachine), false, "iii", "imi", "iii",
				'i', "plateAlloyIridium",
				'm', itemStackMachineCasingMV));
			
		} else if (WarpDriveConfig.isThermalExpansionLoaded) {
			itemStackMachineCasingLV = WarpDriveConfig.getModItemStack("ThermalExpansion", "Frame", 0);
			itemStackMachineCasingMV = WarpDriveConfig.getModItemStack("ThermalExpansion", "Frame", 1);
			itemStackMachineCasingHV = WarpDriveConfig.getModItemStack("ThermalExpansion", "Frame", 2);
			itemStackMachineCasingEV = WarpDriveConfig.getModItemStack("ThermalExpansion", "Frame", 3);
			
		} else if (WarpDriveConfig.isEnderIOLoaded) {
			itemStackMachineCasingLV = WarpDriveConfig.getModItemStack("EnderIO", "itemMachinePart", 0);     // Machine chassis
			itemStackMachineCasingMV = WarpDriveConfig.getModItemStack("EnderIO", "blockVacuumChest", 0);    // Pulsating crystal => Vacuum chest
			itemStackMachineCasingHV = WarpDriveConfig.getModItemStack("EnderIO", "blockIngotStorage", 2);   // Vibrant alloy block
			itemStackMachineCasingEV = WarpDriveConfig.getModItemStack("EnderIO", "itemMaterial", 8);        // Ender crystal
			
		} else {// vanilla
			itemStackMachineCasingLV = new ItemStack(Blocks.iron_block);
			itemStackMachineCasingMV = new ItemStack(Blocks.diamond_block);
			itemStackMachineCasingHV = new ItemStack(WarpDrive.blockHighlyAdvancedMachine);
			itemStackMachineCasingEV = new ItemStack(Blocks.beacon);
			
			GameRegistry.addRecipe(new ItemStack(WarpDrive.blockHighlyAdvancedMachine, 4), "pep", "ede", "pep",
				'e', Items.emerald,
				'p', Items.ender_eye,
				'd', Blocks.diamond_block);
		}
		
		final ItemStack[] itemStackMachineCasings = { itemStackMachineCasingLV, itemStackMachineCasingMV, itemStackMachineCasingHV, itemStackMachineCasingEV };
		final ItemStack[] itemStackMotors  = { itemStackMotorLV, itemStackMotorMV, itemStackMotorHV, itemStackMotorEV };
		
		// Base tuning crystals
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL), false, " e ", "BBB", "qrq",
				'e', Items.emerald,
				'B', ironBars,
				'r', Items.redstone,
				'q', Items.quartz));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL), false, " e ", "BBB", "grg",
				'e', Items.ender_pearl,
				'B', ironBars,
				'r', Items.redstone,
				'g', "nuggetGold"));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL), false, " d ", "BBB", "prp",
				'd', Items.diamond,
				'B', ironBars,
				'r', Items.redstone,
				'p', Items.paper));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.DIFFRACTION_GRATING), false, " t ", "iii", "ggg",
				't', Items.ghast_tear,
				'i', ironBars,
				'g', Blocks.glowstone));
		
		// Intermediary component for reactor core
		if (!WarpDriveConfig.ACCELERATOR_ENABLE) {
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.REACTOR_CORE), false, "shs", "hmh", "shs",
			                                           's', Items.nether_star,
			                                           'h', "blockHull3_plain",
			                                           'm', itemStackMachineCasings[2]));
		} else {
			GameRegistry.addRecipe(new RecipeParticleShapedOre(ItemComponent.getItemStack(EnumComponentType.REACTOR_CORE), false, "chc", "hph", "cec",
			                                                   'p', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.PROTON, 1000),
			                                                   'h', "blockHull3_plain",
			                                                   'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
			                                                   'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL)));
		}
		
		
		// Computer interface is 2 gold ingot, 2 wired modems (or redstone), 1 lead/tin ingot
		{
			Object redstoneOrModem = Items.redstone;
			if (WarpDriveConfig.isComputerCraftLoaded) {
				redstoneOrModem = WarpDriveConfig.getModItemStack("ComputerCraft", "CC-Cable", 1); // Wired modem
			}
			// if (OreDictionary.doesOreNameExist("circuitPrimitive") && !OreDictionary.getOres("circuitPrimitive").isEmpty()) { // Gregtech
			Object oreCircuitOrHeavyPressurePlate = Blocks.heavy_weighted_pressure_plate;
			int outputFactor = 1;
			if (OreDictionary.doesOreNameExist("oc:materialCU") && !OreDictionary.getOres("oc:materialCU").isEmpty()) {
				oreCircuitOrHeavyPressurePlate = "oc:materialCU";	// Control circuit is 5 redstone, 5 gold ingot, 3 paper
				outputFactor = 2;
			} else if (OreDictionary.doesOreNameExist("circuitBasic") && !OreDictionary.getOres("circuitBasic").isEmpty()) {// comes with IndustrialCraft2
				oreCircuitOrHeavyPressurePlate = "circuitBasic";
				outputFactor = 2;
			}
			String oreSlimeOrTinOrLead = "slimeball";
			// Computer interface: double output with Soldering alloy
			if (OreDictionary.doesOreNameExist("ingotSolderingAlloy") && !OreDictionary.getOres("ingotSolderingAlloy").isEmpty()) {
				GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.COMPUTER_INTERFACE, 2 * outputFactor), false, "   ", "rar", "gGg",
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
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.COMPUTER_INTERFACE, outputFactor), false, "   ", "rar", "gGg",
					'G', oreCircuitOrHeavyPressurePlate,
					'g', "ingotGold",
					'r', redstoneOrModem,
					'a', oreSlimeOrTinOrLead));
		}
		
		// Power interface is 4 redstone, 2 iron ingot, 3 gold ingot
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.POWER_INTERFACE, 2), false, "rgr", "igi", "rgr",
				'g', "ingotGold",
				'r', Items.redstone,
				'i', Items.iron_ingot));
		
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
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.CAPACITIVE_CRYSTAL, 2), false, "prp", "lRl", "prp",
				'R', new ItemStack(Items.potionitem, 1, 8225),  // Regeneration II (ghast tear + glowstone)
				'r', "blockRedstone",
				'l', lithiumOrElectrum,
				'p', Items.paper));
		
		// Air canister is 4 iron bars, 2 leather/rubber, 2 yellow wool, 1 tank
		Object rubberOrLeather = Items.leather;
		if (OreDictionary.doesOreNameExist("plateRubber") && !OreDictionary.getOres("plateRubber").isEmpty()) {// comes with GregTech
			rubberOrLeather = "plateRubber";
		} else if (OreDictionary.doesOreNameExist("itemRubber") && !OreDictionary.getOres("itemRubber").isEmpty()) {// comes with IndustrialCraft2
			rubberOrLeather = "itemRubber";
		}
		Object woolPurple = new ItemStack(Blocks.wool, 1, 10);
		if (OreDictionary.doesOreNameExist("blockWoolPurple") && !OreDictionary.getOres("blockWoolPurple").isEmpty()) {
			woolPurple = "blockWoolPurple";
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 4), false, "iyi", "rgr", "iyi",
				'r', rubberOrLeather,
				'g', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
				'y', woolPurple,
				'i', ironBars));
		
		// Lens is 1 diamond, 2 gold ingots, 2 glass panels
		if (OreDictionary.doesOreNameExist("lensDiamond") && !OreDictionary.getOres("lensDiamond").isEmpty()) {
			if (OreDictionary.doesOreNameExist("craftingLensWhite") && !OreDictionary.getOres("craftingLensWhite").isEmpty()) {
				GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.LENS, 3), false, "ggg", "pdp", "ggg",
						'g', "nuggetGold",
						'p', "craftingLensWhite",
						'd', "lensDiamond"));
			} else {
				GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.LENS), false, " g ", "pdp", " g ",
						'g', "ingotGold",
						'p', "paneGlassColorless",
						'd', "lensDiamond"));
			}
		} else if (WarpDriveConfig.isAdvancedRepulsionSystemLoaded) {
			ItemStack diamondLens = WarpDriveConfig.getModItemStack("AdvancedRepulsionSystems", "{A8F3AF2F-0384-4EAA-9486-8F7E7A1B96E7}", 1);
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.LENS), false, " g ", "pdp", " g ",
					'g', "ingotGold",
					'p', "paneGlassColorless",
					'd', diamondLens));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.LENS, 2), false, " g ", "pdp", " g ",
					'g', "ingotGold",
					'p', "paneGlassColorless",
					'd', "gemDiamond"));
		}
		
		// Zoom is 3 lenses, 2 iron ingot, 2 dyes, 2 redstone
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.ZOOM), false, "dir", "lll", "dit",
				'r', Items.redstone,
				'i', Items.iron_ingot,
				'l', ItemComponent.getItemStack(EnumComponentType.LENS),
				't', itemStackMotors[0],
				'd', "dye"));
		
		// Glass tank is 4 slime balls, 4 glass
		// slimeball && blockGlass are defined by forge itself
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.GLASS_TANK, 4), false, "sgs", "g g", "sgs",
				's', "slimeball",
				'g', "blockGlass"));
		
		// Flat screen is 3 dyes, 1 glowstone dust, 2 paper, 3 glass panes
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.FLAT_SCREEN), false, "gRp", "gGd", "gBp",
				'R', "dyeRed",
				'G', "dyeLime",
				'B', "dyeBlue",
				'd', "dustGlowstone",
				'g', "paneGlassColorless",
				'p', Items.paper));
		
		// Memory bank is 2 papers, 2 iron bars, 4 comparators, 1 redstone
		if (OreDictionary.doesOreNameExist("circuitPrimitive") && !OreDictionary.getOres("circuitPrimitive").isEmpty()) { // Gregtech
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL), false, "cic", "cic", "prp",
					'i', ironBars,
					'c', "circuitPrimitive",
					'r', Items.redstone,
					'p', Items.paper));
		} else if (OreDictionary.doesOreNameExist("oc:ram3") && !OreDictionary.getOres("oc:ram3").isEmpty()) {
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.MEMORY_CRYSTAL, 4), false, "cic", "cic", "prp",
					'i', ironBars,
					'c', "oc:ram3",
					'r', Items.redstone,
					'p', Items.paper));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL), false, "cic", "cic", "prp",
					'i', ironBars,
					'c', Items.comparator,
					'r', Items.redstone,
					'p', Items.paper));
		}
		
		// Motor is 2 gold nuggets (wires), 3 iron ingots (steel rods), 4 iron bars (coils)
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.MOTOR), false, "bbn", "iii", "bbn",
				'b', ironBars,
				'i', Items.iron_ingot,
				'n', "nuggetGold"));
		
		// Basic Air Tank is 2 air canisters, 1 pump, 1 gold nugget, 1 basic circuit, 4 rubber
		Object goldNuggetOrBasicCircuit = "nuggetGold";
		if (OreDictionary.doesOreNameExist("circuitBasic") && !OreDictionary.getOres("circuitBasic").isEmpty()) {// comes with IndustrialCraft2, Mekanism, VoltzEngine
			goldNuggetOrBasicCircuit = "circuitBasic";
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(WarpDrive.itemAirTanks[1], false, "rnr", "tpt", "rcr",
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
		GameRegistry.addRecipe(new ShapedOreRecipe(WarpDrive.itemAirTanks[2], false, "rnr", "tpt", "rcr",
		                                           'r', rubberOrLeather,
		                                           'p', itemStackMotors[1],
		                                           't', WarpDrive.itemAirTanks[1],
		                                           'c', goldIngotOrAdvancedCircuit,
		                                           'n', "nuggetGold"));
		
		// Superior Air Tank is 2 advanced air tank, 1 pump, 1 gold nugget, 1 elite circuit, 4 rubber
		Object emeraldOrSuperiorCircuit = "gemEmerald";
		if (OreDictionary.doesOreNameExist("circuitElite") && !OreDictionary.getOres("circuitElite").isEmpty()) {// comes with Mekanism, VoltzEngine
			emeraldOrSuperiorCircuit = "circuitElite";
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(WarpDrive.itemAirTanks[3], false, "rnr", "tpt", "rcr",
		                                           'r', rubberOrLeather,
		                                           'p', itemStackMotors[2],
		                                           't', WarpDrive.itemAirTanks[2],
		                                           'c', emeraldOrSuperiorCircuit,
		                                           'n', "nuggetGold"));
		
		// Uncrafting air tanks and canister
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.GLASS_TANK, 1), WarpDrive.itemAirTanks[0], WarpDrive.itemAirTanks[0], WarpDrive.itemAirTanks[0], WarpDrive.itemAirTanks[0]));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 2), WarpDrive.itemAirTanks[1]));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 4), WarpDrive.itemAirTanks[2]));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 8), WarpDrive.itemAirTanks[3]));
		
		// Bone charcoal is smelting 1 bone
		GameRegistry.addSmelting(Items.bone, ItemComponent.getItemStackNoCache(EnumComponentType.BONE_CHARCOAL, 1), 1);
		
		// Activated carbon is 1 bone charcoal, 4 sticks, 4 leaves
		Object leaves = Blocks.leaves;
		if (OreDictionary.doesOreNameExist("treeLeaves") && !OreDictionary.getOres("treeLeaves").isEmpty()) {
			leaves = "treeLeaves";
		}
		if (OreDictionary.doesOreNameExist("dustSulfur") && !OreDictionary.getOres("dustSulfur").isEmpty()) {
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.ACTIVATED_CARBON), false, "lll", "aaa", "fwf",
					'l', leaves,
					'a', ItemComponent.getItemStack(EnumComponentType.BONE_CHARCOAL),
					'w', new ItemStack(Items.potionitem, 1, 0),
					'f', "dustSulfur"));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.ACTIVATED_CARBON), false, "lll", "aaa", "wgw",
					'l', leaves,
					'a', ItemComponent.getItemStack(EnumComponentType.BONE_CHARCOAL),
					'w', new ItemStack(Items.potionitem, 1, 0),
					'g', Items.gunpowder));
		}
		
		// Laser medium (empty) is 3 glass tanks, 1 power interface, 1 computer interface, 1 MV Machine casing
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.LASER_MEDIUM_EMPTY), false, "   ", "ggg", "pmc",
				'g', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
				'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
				'm', itemStackMachineCasings[1],
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		
		// Coil crystal is 6 iron bars, 2 gold ingots, 1 diamond crystal, return 12x
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStackNoCache(EnumComponentType.COIL_CRYSTAL, 12), false, "bbg", "bdb", "gbb",
		                                          'b', ironBars,
		                                          'g', "ingotGold",
		                                          'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL)));
		
		// Electromagnetic Projector is 5 coil crystals, 1 power interface, 1 computer interface, 2 motors
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR), false, "CCm", "Cpc", "CCm",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                          'm', ItemComponent.getItemStack(EnumComponentType.MOTOR),
		                                          'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		
		// Superconductor is 1 Ender crystal, 4 Power interface, 4 Cryotheum dust/Lapis block/10k Coolant cell
		Object oreCoolant = "blockLapis";
		if (WarpDriveConfig.isThermalExpansionLoaded) {
			oreCoolant = "dustCryotheum";
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			oreCoolant = WarpDriveConfig.getModItemStack("IC2", "reactorCoolantSimple", 1,       // IC2 Experimental 10k Coolant Cell
			                                             "IC2", "item.reactorCoolantSimple", 0); // IC2 Classic 10k Coolant Cell
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemComponent.getItemStack(EnumComponentType.SUPERCONDUCTOR), false, "pcp", "cec", "pcp",
		                                          'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		                                          'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
		                                          'c', oreCoolant ));
		
		// *** Force field shapes
		// Force field shapes are 1 Memory crystal, 3 to 5 Coil crystal
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldShape.getItemStack(EnumForceFieldShape.SPHERE), false, "   ", "CmC", "CCC",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldShape.getItemStack(EnumForceFieldShape.CYLINDER_H), false, "C C", " m ", "C C",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldShape.getItemStack(EnumForceFieldShape.CYLINDER_V), false, " C ", "CmC", " C ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldShape.getItemStack(EnumForceFieldShape.CUBE), false, "CCC", "CmC", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldShape.getItemStack(EnumForceFieldShape.PLANE), false, "CCC", " m ", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldShape.getItemStack(EnumForceFieldShape.TUBE), false, "   ", "CmC", "C C",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldShape.getItemStack(EnumForceFieldShape.TUNNEL), false, "C C", "CmC", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		
		// *** Force field upgrades
		// Force field attraction upgrade is 3 Coil crystal, 1 Iron block, 2 Redstone block, 1 MV motor
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.ATTRACTION), false, "CCC", "rir", " m ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'r', "blockRedstone",
		                                          'i', Blocks.iron_block,
		                                          'm', itemStackMotorMV));
		// Force field breaking upgrade is 3 Coil crystal, 1 Diamond axe, 1 diamond shovel, 1 diamond pick
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.BREAKING), false, "CCC", "sap", "   ",
			                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
			                                          's', Items.diamond_axe,
			                                          'a', Items.diamond_shovel,
			                                          'p', Items.diamond_pickaxe));
		// Force field camouflage upgrade is 3 Coil crystal, 2 Diffraction grating, 1 Zoom, 1 Emerald crystal
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.CAMOUFLAGE), false, "CCC", "zre", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'z', ItemComponent.getItemStack(EnumComponentType.ZOOM),
		                                          'r', Blocks.daylight_detector,
		                                          'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL)));
		// Force field cooling upgrade is 3 Coil crystal, 2 Ice, 1 MV Motor
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.COOLING), false, "CCC", "imi", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'i', Blocks.ice,
		                                          'm', itemStackMotors[1]));
		// Force field fusion upgrade is 3 Coil crystal, 2 Computer interface, 1 Emerald crystal
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.FUSION), false, "CCC", "cec", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
		                                          'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL)));
		// Force field heating upgrade is 3 Coil crystal, 2 Blaze rod, 1 MV Motor
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.HEATING), false, "CCC", "bmb", "   ",
			                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
			                                          'b', Items.blaze_rod,
			                                          'm', itemStackMotors[1]));
		// Force field inversion upgrade is 3 Coil crystal, 1 Gold nugget, 2 Redstone
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.INVERSION), false, "rgr", "CCC", "CCC",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'r', Items.redstone,
		                                          'g', Items.gold_nugget));
		// Force field item port upgrade is 3 Coil crystal, 3 Chests, 1 MV motor
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.ITEM_PORT), false, "CCC", "cmc", " c ",
			                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
			                                          'c', Blocks.chest,
			                                          'm', itemStackMotorMV));
		// Force field silencer upgrade is 3 Coil crystal, 3 Wool
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.SILENCER), false, "CCC", "www", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'w', Blocks.wool));
		// Force field pumping upgrade is 3 Coil crystal, 1 MV Motor, 2 glass tanks
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.PUMPING), false, "CCC", "tmt", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', itemStackMotors[1],
		                                          't', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK)));
		// Force field range upgrade is 3 Coil crystal, 2 Memory crystal, 1 Redstone block
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.RANGE), false, "CCC", "RMR", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'M', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL),
		                                          'R', "blockRedstone"));
		// Force field repulsion upgrade is 3 Coil crystal, 1 Iron block, 2 Redstone block, 1 MV motor
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.REPULSION), false, " m ", "rir", "CCC",
			                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
			                                          'r', "blockRedstone",
			                                          'i', Blocks.iron_block,
			                                          'm', itemStackMotorMV));
		// Force field rotation upgrade is 3 Coil crystal, 2 MV Motors, 1 Computer interface
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStackNoCache(EnumForceFieldUpgrade.ROTATION, 2), false, "CCC", " m ", " mc",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', itemStackMotors[1],
		                                          'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		// Force field shock upgrade is 3 Coil crystal, 1 Power interface
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.SHOCK), false, "CCC", " p ", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE)));
		// Force field speed upgrade is 3 Coil crystal, 2 Ghast tear, 1 Emerald crystal
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.SPEED), false, "CCC", "geg", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'g', Items.ghast_tear,
		                                          'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL)));
		// Force field stabilization upgrade is 3 Coil crystal, 1 Memory crystal, 2 Lapis block
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.STABILIZATION), "CCC", "lMl", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'M', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL),
		                                          'l', "blockLapis"));
		// Force field thickness upgrade is 8 Coil crystal, 1 Diamond crystal
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStack(EnumForceFieldUpgrade.THICKNESS), false, "CCC", "CpC", "   ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'p', ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR)));
		// Force field translation upgrade is 3 Coil crystal, 2 MV Motor, 1 Computer interface
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemForceFieldUpgrade.getItemStackNoCache(EnumForceFieldUpgrade.TRANSLATION, 2), false, "CCC", "m m", " c ",
		                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
		                                          'm', itemStackMotors[1],
		                                          'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE)));
		
		// *** Blocks
		// Ship core is 1 Ghast tear, 4 Capacitive crystal, 2 Tuning ender, 1 Power interface, 1 MV Machine casing
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockShipCore),"csc", "eme", "cpc",
				's', Items.ghast_tear,
				'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
				'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
				'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
				'm', itemStackMachineCasings[1]);
		
		// Ship controller is 1 Computer interface, 1 Tuning emerald, 1 LV Machine casing, 2 Memory bank
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockShipController), false, " e ", "bmb", " c ",
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
				'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
				'm', itemStackMachineCasings[0],
				'b', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL)));
		
		// Radar is 1 motor, 4 Titanium plate (diamond), 1 quarztite rod (nether quartz), 1 computer interface, 1 HV Machine casing, 1 power interface
		String oreCloakingPlate = "gemQuartz";
		if (OreDictionary.doesOreNameExist("plateTitanium") && !OreDictionary.getOres("plateTitanium").isEmpty()) {// Gregtech
			oreCloakingPlate = "plateTitanium";
		} else if (OreDictionary.doesOreNameExist("ingotEnderium") && !OreDictionary.getOres("ingotEnderium").isEmpty()) {// ThermalExpansion
			oreCloakingPlate = "ingotEnderium";
		} else if (OreDictionary.doesOreNameExist("ingotPhasedGold") && !OreDictionary.getOres("ingotPhasedGold").isEmpty()) {// EnderIO
			oreCloakingPlate = "ingotPhasedGold";
		} else if (OreDictionary.doesOreNameExist("plateAlloyIridium") && !OreDictionary.getOres("plateAlloyIridium").isEmpty()) {// IndustrialCraft2
			oreCloakingPlate = "plateAlloyIridium";
		}
		Object oreAntenna = Items.ghast_tear;
		if (OreDictionary.doesOreNameExist("stickQuartzite")) {// GregTech
			oreAntenna = "stickQuartzite";
		} else if (OreDictionary.doesOreNameExist("ingotSignalum") && !OreDictionary.getOres("ingotSignalum").isEmpty()) {// ThermalExpansion
			oreAntenna = "ingotSignalum";
		} else if (OreDictionary.doesOreNameExist("nuggetPulsatingIron") && !OreDictionary.getOres("nuggetPulsatingIron").isEmpty()) {// EnderIO
			oreAntenna = "nuggetPulsatingIron";
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockRadar), false, "PAP", "PtP", "pmc",
			't', itemStackMotors[2],
			'P', oreCloakingPlate,
			'A', oreAntenna,
			'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
			'm', itemStackMachineCasings[2],
			'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE)));
		
		// Warp isolation is 1 EV Machine casing (Ti), 4 Titanium plate/Enderium ingot/Vibrant alloy/Iridium plate/quartz
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockWarpIsolation), false, "i i", " m ", "i i",
				'i', oreCloakingPlate,
				'm', itemStackMachineCasings[3]));
		
		// Air generator is 1 power interface, 4 activated carbon, 1 motor, 1 MV Machine casing, 2 tanks
		ItemStack itemStackCompressorOrTank = ItemComponent.getItemStack(EnumComponentType.GLASS_TANK);
		if (WarpDriveConfig.isGregTech6Loaded) {
			itemStackCompressorOrTank = WarpDriveConfig.getModItemStack("gregtech", "gt.meta.rotor", 8610); // GT6 Bronze rotor, ore:rotorBronze
		} else if (WarpDriveConfig.isGregTech5Loaded) { 
			itemStackCompressorOrTank = WarpDriveConfig.getModItemStack("gregtech", "gt.metaitem.02", 21300); // GT5 Bronze rotor 
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			itemStackCompressorOrTank = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 5); // Compressor
		} else if (WarpDriveConfig.isThermalExpansionLoaded) {
			itemStackCompressorOrTank = WarpDriveConfig.getModItemStack("ThermalExpansion", "Machine", 5); // Fluid transposer
		} else if (WarpDriveConfig.isEnderIOLoaded) {
			itemStackCompressorOrTank = WarpDriveConfig.getModItemStack("EnderIO", "blockReservoir", 0);
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAirGenerator), false, "ata", "aca", "gmp",
				'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
				'a', ItemComponent.getItemStack(EnumComponentType.ACTIVATED_CARBON),
				't', itemStackMotors[1],
				'g', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
				'm', itemStackMachineCasings[1],
				'c', itemStackCompressorOrTank));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAirGeneratorTiered[0]), false, "aca", "ata", "gmp",
		        'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
		        'a', ItemComponent.getItemStack(EnumComponentType.ACTIVATED_CARBON),
		        't', itemStackMotors[1],
		        'g', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
		        'm', itemStackMachineCasings[1],
		        'c', itemStackCompressorOrTank));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAirGeneratorTiered[1]), false, "aaa", "ata", "ama",
		        'a', WarpDrive.blockAirGeneratorTiered[0],
		        't', itemStackMotors[2],
		        'm', itemStackMachineCasings[2]));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAirGeneratorTiered[2]), false, "aaa", "ata", "ama",
		        'a', WarpDrive.blockAirGeneratorTiered[1],
		        't', itemStackMotors[3],
		        'm', itemStackMachineCasings[3]));
		
		// Air shield is 4 glowstones, 4 omnipanels and 1 coil crystal 
		for (int woolColor = 0; woolColor < 16; woolColor++) {
			OreDictionary.registerOre("blockAirShield", new ItemStack(WarpDrive.blockAirShield, 1, woolColor));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAirShield, 4, woolColor), false, "gog", "oco", "gog",
			                                           'g', Items.glowstone_dust,
			                                           'o', new ItemStack(WarpDrive.blockHulls_omnipanel[0], 1, woolColor),
			                                           'c', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL) ));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAirShield, 6, BlockColored.func_150031_c(woolColor)), false, "###", "gXg", "###",
			                                           '#', "blockAirShield",
			                                           'g', Items.gold_nugget,
			                                           'X', oreDyes[woolColor] ));
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockAirShield, 1, BlockColored.func_150031_c(woolColor)),
			                                           "blockAirShield",
			                                           oreDyes[woolColor] ));
		}
		
		// Laser cannon is 2 motors, 1 diffraction grating, 1 lens, 1 computer interface, 1 HV Machine casing, 1 redstone dust, 2 glass pane
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaser), false, "gtr", "ldm", "gtc",
				't', itemStackMotors[2],
				'd', ItemComponent.getItemStack(EnumComponentType.DIFFRACTION_GRATING),
				'l', ItemComponent.getItemStack(EnumComponentType.LENS),
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
				'm', itemStackMachineCasings[1],
				'r', Items.redstone,
				'g', "paneGlassColorless"));
		
		// Mining laser is 2 motors, 1 diffraction grating, 1 lens, 1 computer interface, 1 MV Machine casing, 1 diamond pick, 2 glass pane
		ItemStack itemStackDiamondPick = new ItemStack(Items.diamond_pickaxe);
		if ( WarpDriveConfig.isGregTech6Loaded
		  || WarpDriveConfig.isGregTech5Loaded ) {
			itemStackDiamondPick = WarpDriveConfig.getModItemStack("IC2", "itemToolMiningLaser", 1,       // IC2 Experimental Mining laser
			                                                       "IC2", "item.itemToolMiningLaser", 0); // IC2 Classic Mining laser
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockMiningLaser), false, "cmr", "tdt", "glg",
				't', itemStackMotors[1],
				'd', ItemComponent.getItemStack(EnumComponentType.DIFFRACTION_GRATING),
				'l', ItemComponent.getItemStack(EnumComponentType.LENS),
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
				'm', itemStackMachineCasings[1],
				'r', itemStackDiamondPick,
				'g', "paneGlassColorless"));
		
		// Laser medium (full) is 1 laser medium (empty), 4 redstone blocks, 4 lapis blocks
		// TODO: add fluid transposer/canning support
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserMedium), false, "lrl", "rmr", "lrl",
				'm', ItemComponent.getItemStack(EnumComponentType.LASER_MEDIUM_EMPTY),
				'r', "blockRedstone",
				'l', "blockLapis"));
		
		// Laser lift is ...
		Object oreMagnetizer = itemStackMachineCasings[0];
		if (WarpDriveConfig.isGregTech6Loaded) {
			oreMagnetizer = WarpDriveConfig.getModItemStack("gregtech", "gt.multitileentity", 20221);	// Basic polarizer
		} else if (WarpDriveConfig.isGregTech5Loaded) {
			oreMagnetizer = WarpDriveConfig.getModItemStack("gregtech", "gt.blockmachines", 551);	// Basic polarizer
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			oreMagnetizer = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 9);
		} else if (WarpDriveConfig.isThermalExpansionLoaded) {
			oreMagnetizer = WarpDriveConfig.getModItemStack("ThermalExpansion", "Plate", 3);
		} else if (OreDictionary.doesOreNameExist("ingotRedstoneAlloy") && !OreDictionary.getOres("ingotRedstoneAlloy").isEmpty()) {// EnderIO
			oreMagnetizer = "ingotRedstoneAlloy";
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLift), false, "rmw", "plc", "glg",
				'r', Items.redstone,
				'w', Blocks.wool,
				'l', ItemComponent.getItemStack(EnumComponentType.LENS),
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
				'm', oreMagnetizer,
				'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
				'g', "paneGlassColorless"));
		
		// Iridium block is just that
		if (WarpDriveConfig.isGregTech6Loaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockIridium), "iii", "iii", "iii",
			                                           'i', "xxx"));
			ItemStack itemStackIridiumAlloy = WarpDriveConfig.getModItemStack("gregtech", "gt.meta.plate", 770); // ore:plateIridium
			GameRegistry.addShapelessRecipe(new ItemStack(itemStackIridiumAlloy.getItem(), 9), new ItemStack(WarpDrive.blockIridium));
			
		} else if (WarpDriveConfig.isGregTech5Loaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockIridium), "iii", "iii", "iii",
					'i', "plateAlloyIridium"));
			ItemStack itemStackIridiumAlloy = WarpDriveConfig.getOreDictionaryEntry("plateAlloyIridium");
			GameRegistry.addShapelessRecipe(new ItemStack(itemStackIridiumAlloy.getItem(), 9), new ItemStack(WarpDrive.blockIridium));
			
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockIridium), "iii", "iii", "iii",
					'i', "plateAlloyIridium"));
			ItemStack itemStackIridiumAlloy = WarpDriveConfig.getOreDictionaryEntry("plateAlloyIridium");
			GameRegistry.addShapelessRecipe(new ItemStack(itemStackIridiumAlloy.getItem(), 9), new ItemStack(WarpDrive.blockIridium));
			
		} else if (WarpDriveConfig.isThermalExpansionLoaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockIridium), "ses", "ele", "ses",
					'l', "ingotLumium",
					's', "ingotSignalum",
					'e', "ingotEnderium"));
			// GameRegistry.addShapelessRecipe(new ItemStack(itemStackIridiumAlloy.getItem(), 9), new ItemStack(WarpDrive.blockIridium));
			
		} else if (WarpDriveConfig.isEnderIOLoaded) {
			ItemStack itemStackVibrantAlloy = WarpDriveConfig.getModItemStack("EnderIO", "itemAlloy", 2);
			ItemStack itemStackRedstoneAlloy = WarpDriveConfig.getModItemStack("EnderIO", "itemAlloy", 3);
			ItemStack itemStackFranckNZombie = WarpDriveConfig.getModItemStack("EnderIO", "itemFrankenSkull", 2);
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockIridium, 4), "ses", "ele", "ses",
					'l', itemStackFranckNZombie,
					's', itemStackRedstoneAlloy,
					'e', itemStackVibrantAlloy));
			// GameRegistry.addShapelessRecipe(new ItemStack(itemStackIridiumAlloy.getItem(), 9), new ItemStack(WarpDrive.blockIridium));
			
		} else {
			GameRegistry.addRecipe(new ItemStack(WarpDrive.blockIridium), "ded", "yty", "ded",
					't', Items.ghast_tear,
					'd', Items.diamond,
					'e', Items.emerald,
					'y', Items.ender_eye);
		}
		
		// Laser camera is just Laser + Camera
		if (OreDictionary.doesOreNameExist("circuitBasic") && !OreDictionary.getOres("circuitBasic").isEmpty()) {// comes with IndustrialCraft2
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserCamera), false, "rlr", "rsr", "rcr",
					'r', rubberOrLeather,
					's', "circuitBasic",
					'l', WarpDrive.blockLaser,
					'c', WarpDrive.blockCamera));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserCamera), false, "rlr", "rsr", "rcr",
					'r', rubberOrLeather,
					's', "nuggetGold",
					'l', WarpDrive.blockLaser,
					'c', WarpDrive.blockCamera));
		}
		
		// Weapon controller is diamond sword with Ship controller
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockWeaponController), false, "rwr", "rsr", "rcr",
				'r', rubberOrLeather,
				's', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
				'w', Items.diamond_sword,
				'c', WarpDrive.blockShipController));
		
		// Camera is 1 daylight sensor, 2 motors, 1 computer interface, 2 glass panel, 1 Tuning diamond, 1 LV Machine casing
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockCamera), false, "gtd", "zlm", "gtc",
				't', itemStackMotors[0],
				'z', ItemComponent.getItemStack(EnumComponentType.ZOOM),
				'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
				'm', itemStackMachineCasings[0],
				'l', Blocks.daylight_detector,
				'g', "paneGlassColorless"));
		
		// Monitor is 3 flat screen, 1 computer interface, 1 Tuning diamond, 1 LV Machine casing
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockMonitor), false, "fd ", "fm ", "fc ",
				'f', ItemComponent.getItemStack(EnumComponentType.FLAT_SCREEN),
				'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
				'm', itemStackMachineCasings[0]));
		
		// Ship scanner is non-functional => no recipe
		/*
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockShipScanner), false, "ici", "isi", "mcm",
				'm', mfsu,
				'i', iridiumAlloy,
				'c', "circuitAdvanced",
				's', WarpDriveConfig.getModItemStack("IC2", "blockMachine2", 7) )); // Scanner
		/**/
		
		// Laser tree farm is 2 motors, 2 lenses, 1 computer interface, 1 LV Machine casing, 1 diamond axe, 2 glass pane
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserTreeFarm), false, "glg", "tlt", "amc",
				't', itemStackMotors[0],
				'l', ItemComponent.getItemStack(EnumComponentType.LENS),
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
				'm', itemStackMachineCasings[0],
				'a', Items.diamond_axe,
				'g', "paneGlassColorless"));
		
		// Transporter Beacon is 1 Ender pearl, 1 Memory crystal, 1 Diamond crystal, 2 sticks
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransporterBeacon), false, " e ", " m ", "sds",
				'e', Items.ender_pearl,
				'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
				'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL),
				's', Items.stick));
		
		// Transporter containment is 1 HV Machine casing, 2 Ender crystal, gives 2
		if (!WarpDriveConfig.ACCELERATOR_ENABLE) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransporterContainment, 2), false, " e ", " m ", " e ",
			                                           'm', itemStackMachineCasings[2],
			                                           'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL)));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransporterContainment, 2), false, " e ", " m ", " e ",
			                                           'm', "blockElectromagnet2",
			                                           'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL)));
		}
		
		// Transporter core is 1 HV Machine casing, 1 Emerald crystal, 1 Capacitive crystal, 1 Diamond crystal, 1 Power interface, 1 Computer interface
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransporterCore), false, " E ", "pmd", " c ",
				'm', itemStackMachineCasings[2],
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
				'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
				'E', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
				'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE)));
		
		// Transporter scanner is 1 HV Machine casing, 1 Emerald crystal, 3 Capacitive crystal, 2 Ender crystal
		if (!WarpDriveConfig.ACCELERATOR_ENABLE) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransporterScanner), false, " E ", "eme", "CCC",
			                                           'm', itemStackMachineCasings[2],
			                                           'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                           'E', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
			                                           'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL)));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransporterScanner), false, " E ", "eme", "CCC",
			                                           'm', "blockElectromagnet2",
			                                           'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                           'E', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
			                                           'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL)));
		}
		
		// IC2 needs to be loaded for the following 2 recipes
		if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			ItemStack itemStackOverclockedHeatVent = WarpDriveConfig.getModItemStack("IC2", "reactorVentGold", 1,       // IC2 Experimental Overclocked heat vent
			                                                                         "IC2", "item.reactorVentGold", 0); // IC2 Classic Overclocked heat vent
			// (there's no coolant in GT6 as of 6.06.05, so we're falling back to IC2)
			ItemStack itemStackReactorCoolant1 = WarpDriveConfig.getModItemStack("gregtech", "gt.360k_Helium_Coolantcell", 1, // GT5
			                                                                     "IC2", "reactorCoolantSix", 1,               // IC2 Experimental 60k coolant cell
			                                                                     "IC2", "item.reactorCoolantSix", 0);         // IC2 Classic 60k coolant cell
			ItemStack itemStackReactorCoolant2 = WarpDriveConfig.getModItemStack("gregtech", "gt.360k_NaK_Coolantcell", 1, // GT5
			                                                                     "IC2", "reactorCoolantSix", 1,            // IC2 Experimental 60k coolant cell
			                                                                     "IC2", "item.reactorCoolantSix", 0);      // IC2 Classic 60k coolant cell
			
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemIC2reactorLaserFocus), false, "cld", "lhl", "dlc",
					'l', ItemComponent.getItemStack(EnumComponentType.LENS),
					'h', itemStackOverclockedHeatVent,
					'c', itemStackReactorCoolant1,
					'd', itemStackReactorCoolant2));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockIC2reactorLaserMonitor), false, "gCp", "lme", "gCc",
					'l', ItemComponent.getItemStack(EnumComponentType.LENS),
					'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
					'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
					'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
					'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
					'g', "paneGlassColorless",
					'm', itemStackMachineCasings[1]));
		}
		
		// Cloaking core is 3 Cloaking coils, 4 iridium blocks, 1 ship controller, 1 power interface
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockCloakingCore), false, "ici", "csc", "ipi",
				'i', WarpDrive.blockIridium,
				'c', WarpDrive.blockCloakingCoil,
				's', WarpDrive.blockShipController,
				'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE)));
		
		// Cloaking coil is 1 Titanium plate, 4 reinforced iridium plate, 1 EV Machine casing (Ti) or 1 Beacon, 4 emerald, 4 diamond
		ItemStack itemStackGoldIngotOrCoil = new ItemStack(Items.gold_ingot);
		if (WarpDriveConfig.isGregTech6Loaded) {
			itemStackGoldIngotOrCoil = WarpDriveConfig.getModItemStack("gregtech", "gt.meta.storage.plate", 8663);	// Nichrome Coil block, ore:blockPlateNichrome
		} else if (WarpDriveConfig.isGregTech5Loaded) {
			itemStackGoldIngotOrCoil = WarpDriveConfig.getModItemStack("gregtech", "gt.blockcasings", 14);	// Nichrome Coil block
		} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			itemStackGoldIngotOrCoil = WarpDriveConfig.getModItemStack("IC2", "itemRecipePart", 0,            // IC2 Experimental Coil
			                                                           "IC2", "item.reactorPlatingHeat", 0);	// IC2 Classic Heat-capacity reactor plating
		} else if (WarpDriveConfig.isThermalExpansionLoaded) {
			itemStackGoldIngotOrCoil = WarpDriveConfig.getModItemStack("ThermalExpansion", "material", 3);	// Redstone conductance coil
		} else if (WarpDriveConfig.isImmersiveEngineeringLoaded) {
			itemStackGoldIngotOrCoil = WarpDriveConfig.getModItemStack("ImmersiveEngineering", "coil", 2);	// HV wire coil
		} else if (WarpDriveConfig.isEnderIOLoaded) {
			itemStackGoldIngotOrCoil = WarpDriveConfig.getModItemStack("EnderIO", "itemPowerConduit", 2);	// Ender energy conduit
		}
		ItemStack oreEmeraldOrTitaniumPlate = new ItemStack(Items.gold_ingot);
		if (OreDictionary.doesOreNameExist("plateTitanium") && !OreDictionary.getOres("plateTitanium").isEmpty()) {
			ArrayList<ItemStack> itemStackTitaniumPlates = OreDictionary.getOres("plateTitanium");
			oreEmeraldOrTitaniumPlate = itemStackTitaniumPlates.get(0);
		} else if (WarpDriveConfig.isAdvancedSolarPanelLoaded) {
			oreEmeraldOrTitaniumPlate = WarpDriveConfig.getModItemStack("AdvancedSolarPanel", "asp_crafting_items", 0);	// Sunnarium
		} else if (OreDictionary.doesOreNameExist("plateDenseSteel") && !OreDictionary.getOres("plateDenseSteel").isEmpty()) {// IndustrialCraft2
			ArrayList<ItemStack> itemStackPlateDenseSteel = OreDictionary.getOres("plateDenseSteel");
			oreEmeraldOrTitaniumPlate = itemStackPlateDenseSteel.get(0);
		} else if (WarpDriveConfig.isThermalExpansionLoaded) {
			oreEmeraldOrTitaniumPlate = WarpDriveConfig.getModItemStack("ThermalExpansion", "Frame", 11);
		} else if (WarpDriveConfig.isImmersiveEngineeringLoaded) {
			oreEmeraldOrTitaniumPlate = WarpDriveConfig.getModItemStack("ImmersiveEngineering", "metalDecoration", 5);	// Heavy engineering block
		} else if (WarpDriveConfig.isEnderIOLoaded) {
			oreEmeraldOrTitaniumPlate = WarpDriveConfig.getModItemStack("EnderIO", "itemAlloy", 2);	// Vibrant alloy
		}
		Object oreEmeraldOrReinforcedIridiumPlate = "gemEmerald";
		if (OreDictionary.doesOreNameExist("plateAlloyIridium") && !OreDictionary.getOres("plateAlloyIridium").isEmpty()) {// IndustrialCraft2 and Gregtech
			oreEmeraldOrReinforcedIridiumPlate = "plateAlloyIridium";
		} else if (WarpDriveConfig.isEnderIOLoaded) {// EnderIO
			oreEmeraldOrReinforcedIridiumPlate = WarpDriveConfig.getModItemStack("EnderIO", "itemFrankenSkull", 2);
		} else if (WarpDriveConfig.isThermalExpansionLoaded) {
			oreEmeraldOrReinforcedIridiumPlate = "ingotLumium";
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockCloakingCoil), false, "iti", "cmc", "iti",
				't', oreEmeraldOrTitaniumPlate,
				'i', oreEmeraldOrReinforcedIridiumPlate,
				'c', itemStackGoldIngotOrCoil,
				'm', itemStackMachineCasings[3] ));
		
		// Enantiomorphic reactor core is 1 EV Machine casing, 4 Capacitive crystal, 1 Computer interface, 1 Power interface, 2 Lenses
		if (!WarpDriveConfig.ACCELERATOR_ENABLE) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnanReactorCore), false, "CpC", "lml", "CcC",
			                                           'm', ItemComponent.getItemStack(EnumComponentType.REACTOR_CORE),
			                                           'l', ItemComponent.getItemStack(EnumComponentType.LENS),
			                                           'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			                                           'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
			                                           'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL)));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnanReactorCore), false, " p ", "lCl", "cpm",
			                                           'C', ItemComponent.getItemStack(EnumComponentType.REACTOR_CORE),
			                                           'l', ItemComponent.getItemStack(EnumComponentType.LENS),
			                                           'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			                                           'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
			                                           'm', itemStackMachineCasings[2]));
		}
		
		// Enantiomorphic reactor stabilization laser is 1 HV Machine casing, 2 Advanced hull, 1 Computer interface, 1 Power interface, 1 Lense, 1 Redstone, 2 glass pane
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnanReactorLaser), false, "ghr", "ldm", "ghc",
				'd', ItemComponent.getItemStack(EnumComponentType.DIFFRACTION_GRATING),
				'l', ItemComponent.getItemStack(EnumComponentType.LENS),
				'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
				'm', itemStackMachineCasings[1],
				'r', Items.redstone,
				'g', "paneGlassColorless",
				'h', "blockHull2_plain"));
		
		// Basic Energy bank is 1 Capacitive crystal + 1 Power interface + 3 paper + 4 iron bars 
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnergyBank, 1, 1), false, "iPi", "pcp", "ipi",
		                                          'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
		                                          'i', ironBars,
												  'p', Items.paper,
		                                          'P', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE) ));
		
		// Advanced Energy bank is 4 Basic energy bank + 1 Power interface 
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnergyBank, 1, 2), false, " c ", "cpc", " c ",
		                                          'c', new ItemStack(WarpDrive.blockEnergyBank, 1, 1),
		                                          'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE) ));
		// or 4 Capacitive crystal + 1 Gold ingot + 4 Power interface
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnergyBank, 1, 2), false, "pcp", "cgc", "pcp",
		                                          'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
												  'g', "ingotGold",
		                                          'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE) ));
		
		// Superior Energy bank is 4 Advanced energy bank + 1 Ender tuned crystal + 4 Iron ingot 
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnergyBank, 1, 3), false, "ici", "cec", "ici",
		                                          'c', new ItemStack(WarpDrive.blockEnergyBank, 1, 2),
		                                          'i', "ingotIron",
		                                          'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL) ));
		// or 4 Capacitive crystal block + 1 Superconductor + 4 Iron ingot
		/*
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnergyBank, 1, 3), false, "ici", "csc", "ici",
		                                          'c', @TODO MC1.10 Capacitive crystal block,
		                                          'i', "ingotIron",
		                                          's', ItemComponent.getItemStack(EnumComponentType.SUPERCONDUCTOR) ));
		/**/
		
		// Force field projector is 1 or 2 Electromagnetic Projector + 1 LV/MV/HV Machine casing + 1 Ender crystal + 1 Redstone
		for (int tier = 1; tier <= 3; tier++) {
			int index = tier - 1;
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockForceFieldProjectors[index], 1, 0), false, " e ", "pm ", " r ",
			                                          'p', ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR),
			                                          'm', itemStackMachineCasings[index],
			                                          'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                          'r', Items.redstone));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockForceFieldProjectors[index], 1, 0), false, " e ", " mp", " r ",
			                                          'p', ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR),
			                                          'm', itemStackMachineCasings[index],
			                                          'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                          'r', Items.redstone));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockForceFieldProjectors[index], 1, 1), false, " e ", "pmp", " r ",
			                                          'p', ItemComponent.getItemStack(EnumComponentType.ELECTROMAGNETIC_PROJECTOR),
			                                          'm', itemStackMachineCasings[index],
			                                          'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                          'r', Items.redstone));
		}
		
		// Force field relay is 2 Coil crystals + 1 LV/MV/HV Machine casing + 1 Ender crystal + 1 Redstone
		for (int tier = 1; tier <= 3; tier++) {
			int index = tier - 1;
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockForceFieldRelays[index]), false, " e ", "CmC", " r ",
			                                          'C', ItemComponent.getItemStack(EnumComponentType.COIL_CRYSTAL),
			                                          'm', itemStackMachineCasings[index],
			                                          'e', ItemComponent.getItemStack(EnumComponentType.ENDER_CRYSTAL),
			                                          'r', Items.redstone));
		}
		
		// Decorative blocks
		OreDictionary.registerOre("warpDecorative", BlockDecorative.getItemStack(EnumDecorativeType.PLAIN));
		OreDictionary.registerOre("warpDecorative", BlockDecorative.getItemStack(EnumDecorativeType.ENERGIZED));
		OreDictionary.registerOre("warpDecorative", BlockDecorative.getItemStack(EnumDecorativeType.NETWORK));
		GameRegistry.addRecipe(new ShapedOreRecipe(BlockDecorative.getItemStackNoCache(EnumDecorativeType.PLAIN, 8), false, "sss", "scs", "sss",
				's', Blocks.stone,
				'c', Items.paper));
		GameRegistry.addRecipe(new ShapedOreRecipe(BlockDecorative.getItemStackNoCache(EnumDecorativeType.PLAIN, 8), false, "sss", "scs", "sss",
				's', "warpDecorative",
				'c', "dyeWhite"));
		GameRegistry.addRecipe(new ShapedOreRecipe(BlockDecorative.getItemStackNoCache(EnumDecorativeType.ENERGIZED, 8), false, "sss", "scs", "sss",
				's', "warpDecorative",
				'c', "dyeRed"));
		GameRegistry.addRecipe(new ShapedOreRecipe(BlockDecorative.getItemStackNoCache(EnumDecorativeType.NETWORK, 8), false, "sss", "scs", "sss",
				's', "warpDecorative",
				'c', "dyeBlue"));
		
		// Warp helmet
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemWarpArmor[0]), false, "ggg", "gig", "wcw",
				'i', Items.iron_helmet,
				'w', Blocks.wool,
				'g', "blockGlass",
				'c', ItemComponent.getItemStack(EnumComponentType.AIR_CANISTER)));
		
		// Warp chestplate
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemWarpArmor[1]), false, "gcg", "wiw", "GmG",
				'i', Items.iron_chestplate,
				'w', Blocks.wool,
				'g', "blockHull3_glass",
				'm', ItemComponent.getItemStack(EnumComponentType.MOTOR),
				'G', Items.gold_ingot,
				'c', ItemComponent.getItemStack(EnumComponentType.AIR_CANISTER)));
		
		// Warp Leggings
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemWarpArmor[2]), false, "gig", "m m", "w w",
				'i', Items.iron_leggings,
				'm', ItemComponent.getItemStack(EnumComponentType.MOTOR),
				'w', Blocks.wool,
				'g', "blockHull2_glass"));
		
		// Warp boots
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemWarpArmor[3]), false, "wiw", "r r", "   ",
				'i', Items.iron_boots,
				'w', Blocks.wool,
				'r', rubberOrLeather));
		
		// Tuning fork ore dictionary
		for (int dyeColor = 0; dyeColor < 16; dyeColor++) {
			OreDictionary.registerOre("itemTuningFork", new ItemStack(WarpDrive.itemTuningFork, 1, dyeColor));
		}
		
		// Tuning fork variations
		for (int dyeColor = 0; dyeColor < 16; dyeColor++) {
			
			// crafting tuning fork
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemTuningFork, 1, dyeColor), false, "  q", "iX ", " i ",
					'q', "gemQuartz",
					'i', "ingotIron",
					'X', oreDyes[dyeColor] ));
			
			// changing colors
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.itemTuningFork, 1, dyeColor),
					oreDyes[dyeColor],
					"itemTuningFork"));
		}
		
		// Tuning driver crafting
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemTuningDriver, 1, ItemTuningDriver.MODE_VIDEO_CHANNEL), false, "  q", "pm ", "d  ",
				'q', "gemQuartz",
				'p', Blocks.heavy_weighted_pressure_plate,
				'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
				'm', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL) ));
		
		// Tuning driver configuration
		GameRegistry.addRecipe(new RecipeTuningDriver(new ItemStack(WarpDrive.itemTuningDriver, 1, ItemTuningDriver.MODE_VIDEO_CHANNEL),
				new ItemStack(Items.redstone), 7));
		GameRegistry.addRecipe(new RecipeTuningDriver(new ItemStack(WarpDrive.itemTuningDriver, 1, ItemTuningDriver.MODE_BEAM_FREQUENCY),
				new ItemStack(Items.redstone), 4));
		GameRegistry.addRecipe(new RecipeTuningDriver(new ItemStack(WarpDrive.itemTuningDriver, 1, ItemTuningDriver.MODE_CONTROL_CHANNEL),
				new ItemStack(Items.redstone), 7));
		
		// HULL blocks and variations
		initDynamicHull();
		
		// Sirens
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockSiren, 1, BlockSiren.METADATA_TYPE_INDUSTRIAL), "ICI", "ICI", "NRN",
				'I', "plankWood",
				'C', "ingotIron",
				'N', new ItemStack(Blocks.noteblock, 1),
				'R', "dustRedstone"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockSiren, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_BASIC), " I ", "ISI", " I ",
				'I', "ingotIron",
				'S', new ItemStack(WarpDrive.blockSiren, 1, BlockSiren.METADATA_TYPE_INDUSTRIAL)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockSiren, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_ADVANCED), " I ", "ISI", " I ",
				'I', "ingotGold",
				'S', new ItemStack(WarpDrive.blockSiren, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_BASIC)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockSiren, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_SUPERIOR), " I ", "ISI", " I ",
				'I', "gemDiamond",
				'S', new ItemStack(WarpDrive.blockSiren, 1, BlockSiren.METADATA_TYPE_RAID + BlockSiren.METADATA_RANGE_ADVANCED)));
		
		// Accelerator related ore dictionary
		if (WarpDriveConfig.ACCELERATOR_ENABLE) {
			OreDictionary.registerOre("blockVoidShell", new ItemStack(WarpDrive.blockVoidShellPlain, 1));
			OreDictionary.registerOre("blockVoidShell", new ItemStack(WarpDrive.blockVoidShellGlass, 1));
			for (int tier = 1; tier <= 3; tier++) {
				int index = tier - 1;
				OreDictionary.registerOre("blockElectromagnet" + tier, new ItemStack(WarpDrive.blockElectromagnetPlain[index], 1));
				OreDictionary.registerOre("blockElectromagnet" + tier, new ItemStack(WarpDrive.blockElectromagnetGlass[index], 1));
			}
		}
		
		// iron or steel
		Object ingotIronOrSteel = "ingotIron";
		if (OreDictionary.doesOreNameExist("ingotSteel") && !OreDictionary.getOres("ingotSteel").isEmpty()) {
			ingotIronOrSteel = "ingotSteel";
		}
		
		if (WarpDriveConfig.ACCELERATOR_ENABLE) {
			
			// Void shells is hull + power interface + steel or iron
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockVoidShellPlain, 6), "psh", "s s", "hsp",
			        'h', "blockHull1_plain",
			        'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			        's', ingotIronOrSteel));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockVoidShellGlass, 6), "psh", "s s", "hsp",
			        'h', "blockHull1_glass",
			        'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			        's', ingotIronOrSteel));
			
			// Electromagnetic cell
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemElectromagneticCell, 2), "iri", "i i", "ici",
			        'i', ironBars,
			        'c', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL),
			        'r', Items.redstone));
			
			// Accelerator control point
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAcceleratorControlPoint), "hd ", "vc ", "he ",
			        'h', Blocks.hopper,
			        'd', ItemComponent.getItemStack(EnumComponentType.DIAMOND_CRYSTAL),
			        'e', ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL),
			        'c', ItemComponent.getItemStack(EnumComponentType.COMPUTER_INTERFACE),
			        'v', "blockVoidShell"));
			
			// Particles injector
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockParticlesInjector), "mm ", "vvp", "mmc",
			        'p', Blocks.piston,
			        'm', "blockElectromagnet1",
			        'c', WarpDrive.blockAcceleratorControlPoint,
			        'v', "blockVoidShell"));
			
			// Accelerator controller
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAcceleratorController), "MmM", "mcm", "MmM",
			        'M', ItemComponent.getItemStack(EnumComponentType.MEMORY_CRYSTAL),
			        'm', "blockElectromagnet1",
			        'c', WarpDrive.blockAcceleratorControlPoint));
			
			// Particles collider
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockParticlesCollider), "hoh", "odo", "hoh",
			        'h', "blockHull1_plain",
			        'o', Blocks.obsidian,
			        'd', Items.diamond));
			
			// Chillers
			Object snowOrIce = Blocks.snow;
			if (OreDictionary.doesOreNameExist("dustCryotheum") && !OreDictionary.getOres("dustCryotheum").isEmpty()) {
				snowOrIce = Blocks.ice;
			}
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockChillers[0]), "wgw", "sms", "bMb",
					'w', snowOrIce,
					'g', Items.ghast_tear,
					's', ingotIronOrSteel,
					'm', itemStackMotorLV,
					'b', ironBars,
					'M', "blockElectromagnet1"));
			
			Object nitrogen = Blocks.ice;
			if (OreDictionary.doesOreNameExist("dustCryotheum") && !OreDictionary.getOres("dustCryotheum").isEmpty()) {
				nitrogen = Blocks.packed_ice;
			}
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockChillers[1]), "ngn", "dmd", "bMb",
			        'n', nitrogen,
			        'g', Items.ghast_tear,
			        'd', Items.diamond,
			        'm', itemStackMotorMV,
			        'b', ironBars,
			        'M', "blockElectromagnet2"));
			
			Object helium = Blocks.packed_ice;
			if (OreDictionary.doesOreNameExist("dustCryotheum") && !OreDictionary.getOres("dustCryotheum").isEmpty()) {
				helium = "dustCryotheum";
			}
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockChillers[2]), "hgh", "eme", "bMb",
			                                          'h', helium,
			                                          'g', Items.ghast_tear,
			                                          'e', Items.emerald,
			                                          'm', itemStackMotorHV,
			                                          'b', ironBars,
			                                          'M', "blockElectromagnet3"));
			
			// Lower tier coil is iron, copper or coil
			Object ironIngotOrCopperIngotOrCoil = new ItemStack(Items.iron_ingot);
			if (WarpDriveConfig.isGregTech6Loaded) {
				ironIngotOrCopperIngotOrCoil = itemStackGoldIngotOrCoil;   // @TODO revise GT recipes
			} else if (WarpDriveConfig.isGregTech5Loaded) {
				ironIngotOrCopperIngotOrCoil = itemStackGoldIngotOrCoil;   // @TODO revise GT recipes
			} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
				ironIngotOrCopperIngotOrCoil = WarpDriveConfig.getModItemStack("IC2", "itemRecipePart", 0,           // IC2 Experimental Coil
				                                                               "IC2", "item.reactorPlatingHeat", 0); // IC2 Classic Heat-capacity reactor plating
			} else if (WarpDriveConfig.isThermalExpansionLoaded) {
				ironIngotOrCopperIngotOrCoil = WarpDriveConfig.getModItemStack("ThermalExpansion", "material", 1);    // Redstone reception coil
			} else if (WarpDriveConfig.isImmersiveEngineeringLoaded) {
				ironIngotOrCopperIngotOrCoil = WarpDriveConfig.getModItemStack("ImmersiveEngineering", "coil", 2);    // HV wire coil
			} else if (WarpDriveConfig.isEnderIOLoaded) {
				ironIngotOrCopperIngotOrCoil = WarpDriveConfig.getModItemStack("EnderIO", "itemPowerConduit", 1);    // Enhanced energy conduit
			} else if (OreDictionary.doesOreNameExist("ingotCopper") && !OreDictionary.getOres("ingotCopper").isEmpty()) {
				ironIngotOrCopperIngotOrCoil = "ingotCopper";
			}
			
			// Normal electromagnets
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockElectromagnetPlain[0], 4), "   ", "ccc", "Cmt",
			        'c', ironIngotOrCopperIngotOrCoil,
			        't', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
			        'm', itemStackMotorLV,
			        'C', ItemComponent.getItemStack(EnumComponentType.CAPACITIVE_CRYSTAL)));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockElectromagnetGlass[0], 4), "mgm", "g g", "mgm",
			        'g', Blocks.glass,
			        'm', WarpDrive.blockElectromagnetPlain[0]));
			
			// Advanced electromagnets
			GameRegistry.addRecipe(new RecipeParticleShapedOre(new ItemStack(WarpDrive.blockElectromagnetPlain[1], 6), "mpm", "pip", "mpm",
			        'i', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.ION, 200),
			        'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			        'm', WarpDrive.blockElectromagnetPlain[0]));
			GameRegistry.addRecipe(new RecipeParticleShapedOre(new ItemStack(WarpDrive.blockElectromagnetGlass[1], 6), "mpm", "pip", "mpm",
			        'i', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.ION, 200),
			        'p', ItemComponent.getItemStack(EnumComponentType.POWER_INTERFACE),
			        'm', WarpDrive.blockElectromagnetGlass[0]));
			
			// Superior electromagnets
			GameRegistry.addRecipe(new RecipeParticleShapedOre(new ItemStack(WarpDrive.blockElectromagnetPlain[2], 6), "mtm", "sps", "mMm",
			        't', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
			        's', ItemComponent.getItemStack(EnumComponentType.SUPERCONDUCTOR),
			        'p', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.PROTON, 24),
			        'M', itemStackMotorHV,
			        'm', WarpDrive.blockElectromagnetPlain[1]));
			GameRegistry.addRecipe(new RecipeParticleShapedOre(new ItemStack(WarpDrive.blockElectromagnetGlass[2], 6), "mtm", "sps", "mMm",
			        't', ItemComponent.getItemStack(EnumComponentType.GLASS_TANK),
			        's', ItemComponent.getItemStack(EnumComponentType.SUPERCONDUCTOR),
			        'p', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.PROTON, 24),
			        'M', itemStackMotorHV,
			        'm', WarpDrive.blockElectromagnetGlass[1]));
			
			// Defense tech
			if (WarpDriveConfig.isDefenseTechLoaded) {
				// antimatter
				removeRecipe(WarpDriveConfig.getModItemStack("DefenseTech", "explosives", 21));
				GameRegistry.addRecipe(new RecipeParticleShapedOre(WarpDriveConfig.getModItemStack("DefenseTech", "explosives", 21), "aaa", "ana", "aaa",
				        'a', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.ANTIMATTER, 1000),
				        'n', WarpDriveConfig.getModItemStack("DefenseTech", "explosives", 15)));
				
				// red matter
				removeRecipe(WarpDriveConfig.getModItemStack("DefenseTech", "explosives", 22));
				GameRegistry.addRecipe(new RecipeParticleShapedOre(WarpDriveConfig.getModItemStack("DefenseTech", "explosives", 22), "sss", "sas", "sss",
						's', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.STRANGE_MATTER, 1000),
						'a', WarpDriveConfig.getModItemStack("DefenseTech", "explosives", 21)));
			}
			
			// ICBM
			if (WarpDriveConfig.isICBMLoaded) {
				// @TODO
			}
			
			// ICBM classic
			if (WarpDriveConfig.isICBMClassicLoaded) {
				// antimatter
				removeRecipe(WarpDriveConfig.getModItemStack("icbmclassic", "icbmCExplosive", 22));
				GameRegistry.addRecipe(new RecipeParticleShapedOre(WarpDriveConfig.getModItemStack("icbmclassic", "icbmCExplosive", 22), "aaa", "ana", "aaa",
				        'a', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.ANTIMATTER, 1000),
				        'n', WarpDriveConfig.getModItemStack("icbmclassic", "icbmCExplosive", 15)));
				
				// red matter
				removeRecipe(WarpDriveConfig.getModItemStack("icbmclassic", "icbmCExplosive", 23));
				GameRegistry.addRecipe(new RecipeParticleShapedOre(WarpDriveConfig.getModItemStack("icbmclassic", "icbmCExplosive", 23), "sss", "sas", "sss",
						's', ItemElectromagneticCell.getItemStackNoCache(ParticleRegistry.STRANGE_MATTER, 1000),
						'a', WarpDriveConfig.getModItemStack("icbmclassic", "icbmCExplosive", 22)));
			}
		}
	}
	
	private static void initDynamicHull() {
		// Hull ore dictionary
		for (int tier = 1; tier <= 3; tier++) {
			int index = tier - 1;
			for (int woolColor = 0; woolColor < 16; woolColor++) {
				OreDictionary.registerOre("blockHull" + tier + "_plain", new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, woolColor));
				OreDictionary.registerOre("blockHull" + tier + "_glass", new ItemStack(WarpDrive.blockHulls_glass[index], 1, woolColor));
				OreDictionary.registerOre("blockHull" + tier + "_stairs", new ItemStack(WarpDrive.blockHulls_stairs[index][woolColor], 1));
				OreDictionary.registerOre("blockHull" + tier + "_tiled", new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, woolColor));
				OreDictionary.registerOre("blockHull" + tier + "_slab", new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 0));
				OreDictionary.registerOre("blockHull" + tier + "_slab", new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 2));
				OreDictionary.registerOre("blockHull" + tier + "_slab", new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 6));
				OreDictionary.registerOre("blockHull" + tier + "_slab", new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 8));
				OreDictionary.registerOre("blockHull" + tier + "_omnipanel", new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor));
			}
		}
		
		// Hull blocks plain
		// (BlockColored.func_150031_c is converting wool metadata into dye metadata)
		// Tier 1 = 5 reinforced stone + 4 obsidian gives 10
		// Tier 1 = 5 stone + 4 steel ingots gives 10
		// Tier 1 = 5 stone + 4 iron ingots gives 10
		// Tier 1 = 5 stone + 4 bronze ingots gives 5
		// Tier 1 = 5 stone + 4 aluminium ingots gives 3
		if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			ItemStack reinforcedStone = WarpDriveConfig.getModItemStack("IC2", "blockAlloy", 0,       // IC2 Experimental reinforced stone
			                                                            "IC2", "reinforcedStone", 0); // IC2 Classic reinforced stone
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[0][0], 10, 0), false, "cbc", "bcb", "cbc",
					'b', Blocks.obsidian,
					'c', reinforcedStone ));
		} else if (OreDictionary.doesOreNameExist("ingotSteel") && !OreDictionary.getOres("ingotSteel").isEmpty()) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[0][0], 10, 0), false, "cbc", "bcb", "cbc",
					'b', "ingotSteel",
					'c', "stone" ));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[0][0], 10, 0), false, "cbc", "bcb", "cbc",
					'b', "ingotIron",
					'c', "stone" ));
		}
		if (OreDictionary.doesOreNameExist("ingotBronze") && !OreDictionary.getOres("ingotBronze").isEmpty()) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[0][0], 5, 0), false, "cbc", "bcb", "cbc",
					'b', "ingotBronze",
					'c', "stone" ));
		}
		if (OreDictionary.doesOreNameExist("ingotAluminium") && !OreDictionary.getOres("ingotAluminium").isEmpty()) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[0][0], 3, 0), false, "cbc", "bcb", "cbc",
					'b', "ingotAluminium",
					'c', "stone" ));
		} else if (OreDictionary.doesOreNameExist("ingotAluminum") && !OreDictionary.getOres("ingotAluminum").isEmpty()) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[0][0], 3, 0), false, "cbc", "bcb", "cbc",
					'b', "ingotAluminum",
					'c', "stone" ));
		}
		// Tier 2 = 4 Tier 1 + 1 Gregtech 5 Tungstensteel reinforced block gives 4
		// Tier 2 = 4 Tier 1 + 4 carbon plates gives 4
		// Tier 2 = 4 Tier 1 + 4 dark steel ingot gives 4
		// Tier 2 = 4 Tier 1 + 4 obsidian gives 4
		for (int woolColor = 0; woolColor < 16; woolColor++) {
			if (WarpDriveConfig.isGregTech6Loaded) {
				// ore:casingMachineQuadrupleTungstenSteel or gregtech:gt.meta.machine.quadruple:8635
				ItemStack tungstensteelReinforcedBlock = WarpDriveConfig.getModItemStack("gregtech", "gt.meta.machine.quadruple", 8635);
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, " b ", "bcb", " b ",
				                                           'b', new ItemStack(WarpDrive.blockHulls_plain[0][0], 4, BlockColored.func_150031_c(woolColor)),
				                                           'c', tungstensteelReinforcedBlock ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, "Xb ", "bcb", " b ",
				                                           'b', "blockHull1_plain",
				                                           'c', tungstensteelReinforcedBlock,
				                                           'X', oreDyes[woolColor] ));
			} else if (WarpDriveConfig.isGregTech5Loaded) {
				ItemStack tungstensteelReinforcedBlock = WarpDriveConfig.getModItemStack("gregtech", "gt.blockreinforced", 3);
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, " b ", "bcb", " b ",
						'b', new ItemStack(WarpDrive.blockHulls_plain[0][0], 4, BlockColored.func_150031_c(woolColor)),
						'c', tungstensteelReinforcedBlock ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, "Xb ", "bcb", " b ",
						'b', "blockHull1_plain",
						'c', tungstensteelReinforcedBlock,
						'X', oreDyes[woolColor] ));
			} else if (WarpDriveConfig.isIndustrialCraft2Loaded) {
				ItemStack carbonPlate = WarpDriveConfig.getModItemStack("IC2", "itemPartCarbonPlate", 0,       // IC2 Experimental carbon plate
				                                                        "IC2", "item.itemPartCarbonPlate", 0); // IC2 Classic carbon plate
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "b b", "cbc",
						'b', new ItemStack(WarpDrive.blockHulls_plain[0][0], 4, BlockColored.func_150031_c(woolColor)),
						'c', carbonPlate ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "bXb", "cbc",
						'b', "blockHull1_plain",
						'c', carbonPlate,
						'X', oreDyes[woolColor] ));
			} else if (OreDictionary.doesOreNameExist("ingotDarkSteel") && !OreDictionary.getOres("ingotDarkSteel").isEmpty()) {// EnderIO
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "b b", "cbc",
						'b', new ItemStack(WarpDrive.blockHulls_plain[0][0], 4, BlockColored.func_150031_c(woolColor)),
						'c', "ingotDarkSteel" ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "bXb", "cbc",
						'b', "blockHull1_plain",
						'c', "ingotDarkSteel",
						'X', oreDyes[woolColor] ));
			} else {
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "b b", "cbc",
						'b', new ItemStack(WarpDrive.blockHulls_plain[0][0], 4, BlockColored.func_150031_c(woolColor)),
						'c', Blocks.obsidian ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "bXb", "cbc",
						'b', "blockHull1_plain",
						'c', Blocks.obsidian,
						'X', oreDyes[woolColor] ));
			}
		}
		// Tier 3 = 4 Tier 2 + 1 naquadah plate gives 4
		// Tier 3 = 4 Tier 2 + 4 iridium plate gives 4
		// Tier 3 = 4 Tier 2 + 4 pulsating crystal gives 4
		// Tier 3 = 4 Tier 2 + 4 diamond gives 4
		for (int woolColor = 0; woolColor < 16; woolColor++) {
			if (OreDictionary.doesOreNameExist("plateNaquadah") && !OreDictionary.getOres("plateNaquadah").isEmpty()) {
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[2][0], 4, BlockColored.func_150031_c(woolColor)), false, " b ", "bcb", " b ",
						'b', new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)),
						'c', "plateNaquadah" ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[2][0], 4, BlockColored.func_150031_c(woolColor)), false, "Xb ", "bcb", " b ",
						'b', "blockHull2_plain",
						'c', "plateNaquadah",
						'X', oreDyes[woolColor] ));
			} else if (OreDictionary.doesOreNameExist("plateAlloyIridium") && !OreDictionary.getOres("plateAlloyIridium").isEmpty()) {
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[2][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "b b", "cbc",
						'b', new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)),
						'c', "plateAlloyIridium" ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[2][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "bXb", "cbc",
						'b', "blockHull2_plain",
						'c', "plateAlloyIridium",
						'X', oreDyes[woolColor] ));
			} else if (OreDictionary.doesOreNameExist("itemPulsatingCrystal") && !OreDictionary.getOres("itemPulsatingCrystal").isEmpty()) {// EnderIO
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[2][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "b b", "cbc",
						'b', new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)),
						'c', "itemPulsatingCrystal" ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[2][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "bXb", "cbc",
						'b', "blockHull2_plain",
						'c', "itemPulsatingCrystal",
						'X', oreDyes[woolColor] ));
			} else {
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[2][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "b b", "cbc",
						'b', new ItemStack(WarpDrive.blockHulls_plain[1][0], 4, BlockColored.func_150031_c(woolColor)),
						'c', "gemDiamond" ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[2][0], 4, BlockColored.func_150031_c(woolColor)), false, "cbc", "bXb", "cbc",
						'b', "blockHull2_plain",
						'c', "gemDiamond",
						'X', oreDyes[woolColor] ));
			}
		}
		
		// Hull blocks variation
		for (int tier = 1; tier <= 3; tier++) {
			int index = tier - 1;
			for (int woolColor = 0; woolColor < 16; woolColor++) {
				
				// crafting glass
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_glass[index], 4, woolColor), false, "gpg", "pFp", "gpg",
						'g', "blockGlass",
						'p', new ItemStack(WarpDrive.blockHulls_plain[index][0], 8, woolColor),
						'F', "dustGlowstone" ));
				
				// crafting stairs
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_stairs[index][woolColor], 4), false, "p  ", "pp ", "ppp",
				        'p', new ItemStack(WarpDrive.blockHulls_plain[index][0], 8, woolColor) ));
				
				// uncrafting stairs
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[index][0], 6, woolColor),
                        WarpDrive.blockHulls_stairs[index][woolColor],
                        WarpDrive.blockHulls_stairs[index][woolColor],
                        WarpDrive.blockHulls_stairs[index][woolColor],
                        WarpDrive.blockHulls_stairs[index][woolColor] ));
				
				// smelting tiled
				GameRegistry.addSmelting(
						new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, woolColor),
						new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, woolColor),
						0);
				
				// uncrafting tiled
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, woolColor),
				        new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, woolColor)));
				
				// crafting omnipanel
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_omnipanel[index], 16, woolColor), false, "ggg", "ggg",
						'g', new ItemStack(WarpDrive.blockHulls_glass[index], 1, woolColor)));
				
				// uncrafting omnipanel
				GameRegistry.addRecipe(new ShapelessOreRecipe( new ItemStack(WarpDrive.blockHulls_glass[index], 3, woolColor),
				        new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor),
				        new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor),
				        new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor),
				        new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor),
				        new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor),
				        new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor),
				        new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor),
				        new ItemStack(WarpDrive.blockHulls_omnipanel[index], 1, woolColor) ));
				
				// crafting slab
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 6, 0), false, "bbb",
						'b', new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, woolColor)));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 6, 2), false, "b", "b", "b",
						'b', new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, woolColor)));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 6, 6), false, "bbb",
						'b', new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, woolColor)));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 6, 8), false, "b", "b", "b",
				        'b', new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, woolColor)));
				
				// uncrafting slab
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, woolColor), false, "s", "s",
						's', new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 0)));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, woolColor), false, "ss",
				        's', new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 2)));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, woolColor), false, "s", "s",
				        's', new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 6)));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[index][1], 1, woolColor), false, "ss",
				        's', new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 8)));
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 2, 0),
				        new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 12)));
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 2, 6),
				        new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 13)));
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 2, 8),
				        new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 14)));
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 2, 8),
				        new ItemStack(WarpDrive.blockHulls_slab[index][woolColor], 1, 15)));
				
				// changing colors
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[index][0], 1, BlockColored.func_150031_c(woolColor)),
						oreDyes[woolColor],
						"blockHull" + tier + "_plain"));
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockHulls_glass[index], 1, BlockColored.func_150031_c(woolColor)),
						oreDyes[woolColor],
						"blockHull" + tier + "_glass"));
				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockHulls_stairs[index][BlockColored.func_150031_c(woolColor)], 1),
						oreDyes[woolColor],
						"blockHull" + tier + "_stairs"));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_plain[index][0], 8, BlockColored.func_150031_c(woolColor)), false, "###", "#X#", "###",
						'#', "blockHull" + tier + "_plain",
						'X', oreDyes[woolColor] ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_glass[index], 8, BlockColored.func_150031_c(woolColor)), false, "###", "#X#", "###",
						'#', "blockHull" + tier + "_glass",
						'X', oreDyes[woolColor] ));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockHulls_stairs[index][BlockColored.func_150031_c(woolColor)], 8), false, "###", "#X#", "###",
						'#', "blockHull" + tier + "_stairs",
						'X', oreDyes[woolColor] ));
			}
		}
	}
	
	private static void removeRecipe(final ItemStack itemStackOutputOfRecipeToRemove) {
		IRecipe recipeToRemove = null;
		for (final Object object : CraftingManager.getInstance().getRecipeList()) {
			if (object instanceof IRecipe) {
				final IRecipe recipe = (IRecipe) object;
				final ItemStack itemStackRecipeOutput = recipe.getRecipeOutput();
				if (itemStackRecipeOutput != null && itemStackRecipeOutput.isItemEqual(itemStackOutputOfRecipeToRemove)) {
					recipeToRemove = recipe;
					break;
				}
			} else {
				WarpDrive.logger.error(String.format("Invalid recipe %s", object));
			}
		}
		if (recipeToRemove == null) {
			WarpDrive.logger.error(String.format("Unable to find any recipe to remove with output %s", itemStackOutputOfRecipeToRemove));
		} else {
			WarpDrive.logger.info(String.format("Removing recipe %s with output %s", recipeToRemove, itemStackOutputOfRecipeToRemove));
			CraftingManager.getInstance().getRecipeList().remove(recipeToRemove);
		}
	}
	
	public static void patchOredictionary() {
		// patching for https://github.com/VoltzEngine-Project/Engine/issues/78
		for (final String nameOre : new String[] { "ingotDiamond" }) {
			if (OreDictionary.doesOreNameExist(nameOre) && OreDictionary.getOres(nameOre).size() == 0) {
				WarpDrive.logger.error(String.format("Invalid OreDictionary entry for %s! Adding a placeholder to prevent crashes.", nameOre));
				final ItemStack itemStack = new ItemStack(Blocks.fire);
				// itemStack.setStackDisplayName(String.format("Invalid %s", nameOre));
				OreDictionary.registerOre(nameOre, itemStack);
			}
		}
	}
}
