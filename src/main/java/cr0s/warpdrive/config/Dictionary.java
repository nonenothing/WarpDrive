package cr0s.warpdrive.config;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.block.hull.BlockHullGlass;
import cr0s.warpdrive.block.hull.BlockHullSlab;
import cr0s.warpdrive.block.hull.BlockHullStairs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

public class Dictionary {
	private static final boolean adjustResistance = false;
	
	// Tagged blocks and entities (loaded from configuration file at PreInit, parsed at PostInit)
	private static HashMap<String, String> taggedBlocks = null;
	private static HashMap<String, String> taggedEntities = null;
	private static HashMap<String, String> taggedItems = null;
	
	// Blocks dictionary
	public static HashSet<Block> BLOCKS_ORES = null;
	public static HashSet<Block> BLOCKS_SOILS = null;
	public static HashSet<Block> BLOCKS_LOGS = null;
	public static HashSet<Block> BLOCKS_LEAVES = null;
	public static HashSet<Block> BLOCKS_ANCHOR = null;
	public static HashSet<Block> BLOCKS_NOMASS = null;
	public static HashSet<Block> BLOCKS_LEFTBEHIND = null;
	public static HashSet<Block> BLOCKS_EXPANDABLE = null;
	public static HashSet<Block> BLOCKS_MINING = null;
	public static HashSet<Block> BLOCKS_SKIPMINING = null;
	public static HashSet<Block> BLOCKS_STOPMINING = null;
	public static HashMap<Block, Integer> BLOCKS_PLACE = null;
	public static HashSet<Block> BLOCKS_NOCAMOUFLAGE = null;
	public static HashSet<Block> BLOCKS_NOBLINK = null;
	
	// Entities dictionary
	public static HashSet<String> ENTITIES_ANCHOR = null;
	public static HashSet<String> ENTITIES_NOMASS = null;
	public static HashSet<String> ENTITIES_LEFTBEHIND = null;
	public static HashSet<String> ENTITIES_NONLIVINGTARGET = null;
	public static HashSet<String> ENTITIES_LIVING_WITHOUT_AIR = null;
	
	// Items dictionary
	public static HashSet<Item> ITEMS_FLYINSPACE = null;
	public static HashSet<Item> ITEMS_NOFALLDAMAGE = null;
	public static HashSet<Item> ITEMS_BREATHING_HELMET = null;
	
	public static void loadConfig(final Configuration config) {
		
		// Block dictionary
		{
			config.addCustomCategoryComment("block_tags",
					  "Use this section to enable special behavior on blocks using tags.\n"
					+ "Most blocks are already supported automatically. Only modify this section when something doesn't work!\n" + "\n"
					+ "Tags shall be separated by at least one space, comma or tabulation.\n"
					+ "Invalid tags will be ignored silently. Tags and block names are case sensitive.\n"
					+ "In case of conflicts, the latest tag overwrite the previous ones.\n"
					+ "- Soil: this block is a soil for plants (default: dirt, farmland, grass, sand & soul sand).\n"
					+ "- Log: this block is harvestable as a wood log (default: all 'log*', '*log' & '*logs' blocks from the ore dictionary).\n"
					+ "- Leaf: this block is harvestable as a leaf (default: all 'leave*', '*leave' & '*leaves' blocks from the ore dictionary).\n"
					+ "- Anchor: ship can't move with this block aboard (default: bedrock and assimilated).\n"
					+ "- NoMass: this block doesn't count when calculating ship volume/mass (default: leaves, all 'air' blocks).\n"
					+ "- LeftBehind: this block won't move with your ship (default: RailCraft heat, WarpDrive gases).\n"
					+ "- Expandable: this block will be squished/ignored in case of collision.\n"
					+ "- Mining: this block is mineable (default: all 'ore' blocks from the ore dictionary).\n"
					+ "- SkipMining: this block is ignored from mining (default: bedrock).\n"
					+ "- StopMining: this block will prevent mining through it (default: command/creative, bedrock, force fields).\n"
					+ "- PlaceEarliest: this block will be removed last and placed first (default: ship hull and projectors).\n"
					+ "- PlaceEarlier: this block will be placed fairly soon (default: forcefield blocks).\n"
					+ "- PlaceNormal: this block will be removed and placed with non-tile entities.\n"
					+ "- PlaceLater: this block will be placed fairly late (default: IC2 Reactor core).\n"
					+ "- PlaceLatest: this block will be removed first and placed last (default: IC2 Reactor chamber).\n"
					+ "- NoCamouflage: this block isn't valid for camouflage.\n"
					+ "- NoBlink: this block will prevent teleportation through it (default: bedrock, force fields)");
			
			final ConfigCategory categoryBlockTags = config.getCategory("block_tags");
			String[] taggedBlocksName = categoryBlockTags.getValues().keySet().toArray(new String[0]);
			if (taggedBlocksName.length == 0) {
				// farming
				config.get("block_tags", "minecraft:dirt"                                  , "Soil").getString();
				config.get("block_tags", "minecraft:farmland"                              , "Soil").getString();
				config.get("block_tags", "minecraft:grass"                                 , "Soil").getString();
				config.get("block_tags", "minecraft:mycelium"                              , "Soil").getString();
				config.get("block_tags", "minecraft:sand"                                  , "Soil").getString();
				config.get("block_tags", "minecraft:soul_sand"                             , "Soil").getString();
				config.get("block_tags", "ic2:rubber_wood"                                 , "Log").getString();
				config.get("block_tags", "tconstruct:slime_congealed"                      , "Log").getString();
				config.get("block_tags", "tconstruct:slime_leaves"                         , "Leaf").getString();
				
				// anchors
				config.get("block_tags", "minecraft:barrier"                               , "Anchor SkipMining").getString();
				config.get("block_tags", "minecraft:bedrock"                               , "Anchor SkipMining NoBlink").getString();
				config.get("block_tags", "minecraft:chain_command_block"                   , "Anchor StopMining").getString();
				config.get("block_tags", "minecraft:command_block"                         , "Anchor StopMining").getString();
				config.get("block_tags", "minecraft:end_gateway"                           , "Anchor StopMining").getString();
				config.get("block_tags", "minecraft:end_portal_frame"                      , "Anchor StopMining").getString();
				config.get("block_tags", "minecraft:end_portal"                            , "Anchor StopMining").getString();
				config.get("block_tags", "minecraft:portal"                                , "Anchor StopMining").getString();
				config.get("block_tags", "minecraft:repeating_command_block"               , "Anchor StopMining").getString();
				config.get("block_tags", "minecraft:structure_block"                       , "Anchor StopMining").getString();
				// config.get("block_tags", "Artifacts:invisible_bedrock"                     , "Anchor StopMining NoBlink").getString();
				// config.get("block_tags", "Artifacts:anti_anti_builder_stone"               , "Anchor StopMining").getString();
				// config.get("block_tags", "Artifacts:anti_builder"                          , "Anchor StopMining").getString();
				config.get("block_tags", "computercraft:command_computer"                  , "Anchor SkipMining").getString();
				// @TODO MC1.12 config.get("block_tags", "ic2:blockPersonal"                               , "Anchor SkipMining").getString(); // IC2 personal chest, need property filtering
				// @TODO MC1.12 config.get("block_tags", "malisisdoors:null"                               , "Anchor").getString(); // improper registration of block causing NPE
				config.get("block_tags", "malisisdoors:rustyhatch"                         , "Anchor").getString();
				config.get("block_tags", "warpdrive:block_bedrock_glass"                   , "Anchor StopMining NoBlink").getString();
				
				// placement priorities
				config.get("block_tags", "minecraft:lever"                                 , "PlaceLatest").getString();
				config.get("block_tags", "warpdrive:block_hull1_plain"                     , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "warpdrive:block_hull2_plain"                     , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "warpdrive:block_hull3_plain"                     , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "warpdrive:block_hull1_glass"                     , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "warpdrive:block_hull2_glass"                     , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "warpdrive:block_hull3_glass"                     , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "warpdrive:block_lamp_bubble"                     , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "warpdrive:block_lamp_flat"                       , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "warpdrive:block_lamp_long"                       , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "warpdrive:block_force_field1"                    , "PlaceLatest StopMining NoMass").getString();
				config.get("block_tags", "warpdrive:block_force_field2"                    , "PlaceLatest StopMining NoMass").getString();
				config.get("block_tags", "warpdrive:block_force_field3"                    , "PlaceLatest StopMining NoMass").getString();
				config.get("block_tags", "ic2:foam"                                        , "PlaceEarliest StopMining").getString();
				// @TODO MC1.12 config.get("block_tags", "ic2:blockAlloy"                                  , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "ic2:glass"                                       , "PlaceEarliest StopMining").getString();
				config.get("block_tags", "minecraft:obsidian"                              , "PlaceEarliest Mining").getString();
				// config.get("block_tags", "AdvancedRepulsionSystems:field"                  , "PlaceEarlier StopMining NoBlink").getString();
				// config.get("block_tags", "MFFS:field"                                                   , "PlaceEarlier StopMining NoBlink").getString();
				// @TODO MC1.12 config.get("block_tags", "ic2:blockGenerator"                              , "PlaceLater").getString(); // nuclear reactor type:nuclear_reactor
				// @TODO MC1.12 config.get("block_tags", "ic2:blockReactorChamber"                         , "PlaceLatest").getString(); // reactor chamber type:reactor_chamber
				config.get("block_tags", "immersiveengineering:metalDevice"                , "PlaceLatest").getString();	// FIXME: need to fine tune at metadata level
				// config.get("block_tags", "CarpentersBlocks:blockCarpentersDaylightSensor"  , "PlaceLatest").getString();
				// config.get("block_tags", "CarpentersBlocks:blockCarpentersDoor"            , "PlaceLatest").getString();
				// config.get("block_tags", "CarpentersBlocks:blockCarpentersGarageDoor"      , "PlaceLatest").getString();
				// config.get("block_tags", "CarpentersBlocks:blockCarpentersHatch"           , "PlaceLatest").getString();
				// config.get("block_tags", "CarpentersBlocks:blockCarpentersLadder"          , "PlaceLatest").getString();
				// config.get("block_tags", "CarpentersBlocks:blockCarpentersLever"           , "PlaceLatest").getString();
				// config.get("block_tags", "CarpentersBlocks:blockCarpentersPressurePlate"   , "PlaceLatest").getString();
				// config.get("block_tags", "CarpentersBlocks:blockCarpentersTorch"           , "PlaceLatest").getString();
				// config.get("block_tags", "sgcraft:stargateBase"                                         , "PlaceEarliest").getString();
				// config.get("block_tags", "sgcraft:stargateBase"                                         , "PlaceEarliest").getString();
				// config.get("block_tags", "sgcraft:stargateRing"                                         , "PlaceEarlier").getString();
				// config.get("block_tags", "sgcraft:stargateController"                                   , "PlaceLatest").getString();
				config.get("block_tags", "opencomputers:case1"                             , "PlaceLatest").getString();
				config.get("block_tags", "opencomputers:case2"                             , "PlaceLatest").getString();
				config.get("block_tags", "opencomputers:case3"                             , "PlaceLatest").getString();
				config.get("block_tags", "opencomputers:casecreative"                      , "PlaceLatest").getString();
				config.get("block_tags", "opencomputers:keyboard"                          , "PlaceLatest").getString();
				config.get("block_tags", "pneumaticcraft:pressure_chamber_valve"           , "PlaceEarlier").getString();
				// config.get("block_tags", "StargateTech2:block.shieldEmitter"               , "PlaceLater StopMining NoBlink").getString();
				// config.get("block_tags", "StargateTech2:block.shieldController"            , "PlaceNormal StopMining NoBlink").getString();
				// config.get("block_tags", "StargateTech2:block.shield"                      , "PlaceNormal StopMining NoBlink").getString();
				// config.get("block_tags", "StargateTech2:block.busAdapter"                  , "PlaceLatest StopMining").getString();
				// config.get("block_tags", "StargateTech2:block.busCable"                    , "PlaceNormal StopMining").getString();
				
				// expendables, a.k.a. "don't blow my ship with this..."
				config.get("block_tags", "chisel:cloud"                                    , "LeftBehind Expandable").getString();
				config.get("block_tags", "railcraft:residual.heat"                         , "LeftBehind Expandable").getString();
				config.get("block_tags", "warpdrive:block_gas"                             , "LeftBehind Expandable").getString();
				// config.get("block_tags", "InvisibLights:blockLightSource"                  , "NoMass Expandable").getString();
				config.get("block_tags", "warpdrive:block_air"                             , "NoMass Expandable PlaceLatest").getString();
				config.get("block_tags", "warpdrive:block_air_flow"                        , "NoMass Expandable PlaceLatest").getString();
				config.get("block_tags", "warpdrive:block_air_source"                      , "NoMass Expandable PlaceLatest").getString();
				
				// mining a mineshaft...
				config.get("block_tags", "minecraft:web"                                   , "Mining").getString();
				config.get("block_tags", "minecraft:fence"                                 , "Mining").getString();
				config.get("block_tags", "minecraft:torch"                                 , "Mining").getString();
				config.get("block_tags", "minecraft:glowstone"                             , "Mining").getString();
				config.get("block_tags", "minecraft:redstone_block"                        , "Mining").getString();
				
				// mining an 'end' moon
				config.get("block_tags", "warpdrive:block_iridium"                         , "Mining").getString();	// stronger than obsidian but can still be mined (see ender moon)
				
				// force field camouflage blacklisting
				config.get("block_tags", "deepresonance:energy_collector"                   , "NoCamouflage").getString();
				config.get("block_tags", "deepresonance:resonating_crystal"                 , "NoCamouflage").getString();
				config.get("block_tags", "evilcraft:blood_infuser"                          , "NoCamouflage").getString();
				config.get("block_tags", "evilcraft:dark_ore"                               , "NoCamouflage").getString();
				config.get("block_tags", "evilcraft:sanguinary_environmental_accumulator"   , "NoCamouflage").getString();
				config.get("block_tags", "evilcraft:spirit_reanimator"                      , "NoCamouflage").getString();
				config.get("block_tags", "openmodularturrets:turret_base"                   , "NoCamouflage").getString(); // Turret tiers are stored in meta data
				config.get("block_tags", "thaumcraft:blockCustomPlant"                      , "NoCamouflage").getString(); // To be tested
				config.get("block_tags", "thermalexpansion:cache"                           , "NoCamouflage").getString();
				config.get("block_tags", "thermalexpansion:device"                          , "NoCamouflage").getString();
				config.get("block_tags", "thermalexpansion:machine"                         , "NoCamouflage").getString();
				// config.get("block_tags", "thermalexpansion:Sponge"                          , "NoCamouflage").getString(); // not found
				// config.get("block_tags", "witchery:leechchest"                              , "NoCamouflage").getString();
				
				taggedBlocksName = categoryBlockTags.getValues().keySet().toArray(new String[0]);
			}
			taggedBlocks = new HashMap<>(taggedBlocksName.length);
			for (final String name : taggedBlocksName) {
				final String tags = config.get("block_tags", name, "").getString();
				taggedBlocks.put(name, tags);
			}
		}
		
		// Entity dictionary
		{
			config.addCustomCategoryComment("entity_tags", 
					  "Use this section to enable special behavior on entities using tags.\n"
					+ "Most entities are already supported automatically. Only modify this section when something doesn't work!\n" + "\n"
					+ "Tags shall be separated by at least one space, comma or tabulation.\n" + "Invalid tags will be ignored silently. Tags and block names are case sensitive.\n"
					+ "In case of conflicts, the latest tag overwrite the previous ones.\n" + "- Anchor: ship can't move with this entity aboard (default: none).\n"
					+ "- NoMass: this entity doesn't count when calculating ship volume/mass (default: Galacticraft air bubble).\n"
					+ "- LeftBehind: this entity won't move with your ship nor transporter (default: Galacticraft air bubble).\n"
//					+ "- NoTransport: this entity is ignored by the transporter (default: -none-).\n"
					+ "- NonLivingTarget: this non-living entity can be targeted/removed by weapons (default: ItemFrame, Painting).\n"
					+ "- LivingWithoutAir: this living entity doesn't need air to live (default: vanilla zombies and skeletons).");
			
			final ConfigCategory categoryEntityTags = config.getCategory("entity_tags");
			String[] taggedEntitiesName = categoryEntityTags.getValues().keySet().toArray(new String[0]);
			if (taggedEntitiesName.length == 0) {
				config.get("entity_tags", "GalacticraftCore.OxygenBubble"          , "NoMass LeftBehind").getString(); // Still needs fixing
				config.get("entity_tags", "minecraft:item_frame"                   , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:painting"                     , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:leash_knot"                   , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:boat"                         , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:minecart"                     , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:chest_minecart"               , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:furnace_minecart"             , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:tnt_minecart"                 , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:hopper_minecart"              , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:spawner_minecart"             , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:ender_crystal"                , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "minecraft:arrow"                        , "NoMass NonLivingTarget").getString();
				
				config.get("entity_tags", "ic2:carbon_boat"                        , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "ic2:rubber_boat"                        , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "ic2:electric_boat"                      , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "ic2:nuke"                               , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "ic2:itnt"                               , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "ic2:sticky_dynamite"                    , "NoMass NonLivingTarget").getString();
				config.get("entity_tags", "ic2:dynamite"                           , "NoMass NonLivingTarget").getString();
				
				config.get("entity_tags", "minecraft:creeper"                      , "LivingWithoutAir").getString();
				config.get("entity_tags", "minecraft:skeleton"                     , "LivingWithoutAir").getString();
				config.get("entity_tags", "minecraft:zombie"                       , "LivingWithoutAir").getString();
				config.get("entity_tags", "testdummy.Dummy"                        , "LivingWithoutAir").getString(); // need review
				
				taggedEntitiesName = categoryEntityTags.getValues().keySet().toArray(new String[0]);
			}
			taggedEntities = new HashMap<>(taggedEntitiesName.length);
			for (final String name : taggedEntitiesName) {
				final String tags = config.get("entity_tags", name, "").getString();
				taggedEntities.put(name, tags);
			}
		}
		
		// Item dictionary
		{
			config.addCustomCategoryComment("item_tags", "Use this section to enable special behavior on items using tags.\n"
					+ "Most items are already supported automatically. Only modify this section when something doesn't work!\n" + "\n"
					+ "Tags shall be separated by at least one space, comma or tabulation.\n" + "Invalid tags will be ignored silently. Tags and block names are case sensitive.\n"
					+ "In case of conflicts, the latest tag overwrite the previous ones.\n" + "- FlyInSpace: player can move without gravity effect while wearing this item (default: jetpacks).\n"
					+ "- NoFallDamage: player doesn't take fall damage while wearing this armor item (default: IC2 rubber boots).\n"
					+ "- BreathingHelmet: player can breath from WarpDrive air canister or IC2 compressed air while wearing this armor item (default: IC2 nano helmet and Cie).\n");
			
			final ConfigCategory categoryItemTags = config.getCategory("item_tags");
			String[] taggedItemsName = categoryItemTags.getValues().keySet().toArray(new String[0]);
			if (taggedItemsName.length == 0) {
				config.get("item_tags", "bloodmagic:living_armour_helmet"              , "BreathingHelmet").getString();
				config.get("item_tags", "bloodmagic:sentient_armour_helmet"            , "BreathingHelmet").getString();
				config.get("item_tags", "advanced_solar_panels:advancedsolarhelmet"    , "BreathingHelmet").getString();
				config.get("item_tags", "advanced_solar_panels:hybridsolarhelmet"      , "BreathingHelmet").getString();
				config.get("item_tags", "advanced_solar_panels:ultimatesolarhelmet"    , "BreathingHelmet").getString();
				config.get("item_tags", "botania:elementiumhelm"                       , "BreathingHelmet").getString();
				config.get("item_tags", "botania:elementiumhelmreveal"                 , "BreathingHelmet").getString();
				config.get("item_tags", "botania:terrasteelhelm"                       , "BreathingHelmet").getString();
				config.get("item_tags", "botania:terrasteelhelmreveal"                 , "BreathingHelmet").getString();
				config.get("item_tags", "enderio:item_dark_steel_helmet"               , "BreathingHelmet").getString();
				config.get("item_tags", "ic2:hazmat_helmet"                            , "BreathingHelmet").getString();
				config.get("item_tags", "ic2:solar_helmet"                             , "BreathingHelmet").getString();
				config.get("item_tags", "ic2:nano_helmet"                              , "BreathingHelmet").getString();
				config.get("item_tags", "ic2:quantum_helmet"                           , "BreathingHelmet").getString();
				config.get("item_tags", "pneumaticcraft:pneumatic_helmet"              , "BreathingHelmet").getString();
				config.get("item_tags", "RedstoneArsenal:armor.helmetFlux"             , "BreathingHelmet").getString();
				config.get("item_tags", "techguns:t3_exo_helmet"                       , "BreathingHelmet").getString();
				config.get("item_tags", "techguns:t3_miner_helmet"                     , "BreathingHelmet").getString();
				config.get("item_tags", "techguns:t3_power_helmet"                     , "BreathingHelmet").getString();
				config.get("item_tags", "techguns:steam_helmet"                        , "BreathingHelmet").getString();
				config.get("item_tags", "techguns:tacticalmask"                        , "BreathingHelmet").getString();
				
				config.get("item_tags", "ic2:jetpack"                                  , "FlyInSpace NoFallDamage").getString();
				config.get("item_tags", "ic2:jetpack_electric"                         , "FlyInSpace NoFallDamage").getString();
				config.get("item_tags", "ic2:quantum_chestplate"                       , "FlyInSpace NoFallDamage").getString();
				config.get("item_tags", "GraviSuite:advJetpack"                        , "FlyInSpace NoFallDamage").getString(); // not ported
				config.get("item_tags", "GraviSuite:advNanoChestPlate"                 , "FlyInSpace NoFallDamage").getString(); // not ported
				config.get("item_tags", "GraviSuite:graviChestPlate"                   , "FlyInSpace NoFallDamage").getString(); // not ported
				
				config.get("item_tags", "ic2:rubber_boots"                             , "NoFallDamage").getString();
				config.get("item_tags", "ic2:quantum_boots"                            , "NoFallDamage").getString();
				config.get("item_tags", "warpdrive:item_warp_armor_leggings"           , "NoFallDamage").getString();
				config.get("item_tags", "warpdrive:item_warp_armor_boots"              , "NoFallDamage").getString();
				taggedItemsName = categoryItemTags.getValues().keySet().toArray(new String[0]);
			}
			taggedItems = new HashMap<>(taggedItemsName.length);
			for (final String name : taggedItemsName) {
				final String tags = config.get("item_tags", name, "").getString();
				taggedItems.put(name, tags);
			}
		}
		
	}
	
	public static void apply() {
		// get default settings from parsing ore dictionary
		BLOCKS_ORES = new HashSet<>();
		BLOCKS_LOGS = new HashSet<>();
		BLOCKS_LEAVES = new HashSet<>();
		final String[] oreNames = OreDictionary.getOreNames();
		for (final String oreName : oreNames) {
			final String lowerOreName = oreName.toLowerCase();
			if (oreName.length() > 4 && oreName.substring(0, 3).equals("ore")) {
				final List<ItemStack> itemStacks = OreDictionary.getOres(oreName);
				for (final ItemStack itemStack : itemStacks) {
					BLOCKS_ORES.add(Block.getBlockFromItem(itemStack.getItem()));
					// WarpDrive.logger.info("- added " + oreName + " to ores as " + itemStack);
				}
			}
			if (lowerOreName.startsWith("log") || lowerOreName.endsWith("log") || lowerOreName.endsWith("logs")) {
				final List<ItemStack> itemStacks = OreDictionary.getOres(oreName);
				for (final ItemStack itemStack : itemStacks) {
					BLOCKS_LOGS.add(Block.getBlockFromItem(itemStack.getItem()));
					// WarpDrive.logger.info("- added " + oreName + " to logs as " + itemStack);
				}
			}
			if (lowerOreName.startsWith("leave") || lowerOreName.endsWith("leave") || lowerOreName.endsWith("leaves")) {
				final List<ItemStack> itemStacks = OreDictionary.getOres(oreName);
				for (final ItemStack itemStack : itemStacks) {
					BLOCKS_LEAVES.add(Block.getBlockFromItem(itemStack.getItem()));
					// WarpDrive.logger.info("- added " + oreName + " to leaves as " + itemStack);
				}
			}
		}
		
		// translate tagged blocks
		BLOCKS_SOILS = new HashSet<>(taggedBlocks.size());
		BLOCKS_ANCHOR = new HashSet<>(taggedBlocks.size());
		BLOCKS_NOMASS = new HashSet<>(taggedBlocks.size() + BLOCKS_LEAVES.size());
		BLOCKS_NOMASS.addAll(BLOCKS_LEAVES);
		BLOCKS_LEFTBEHIND = new HashSet<>(taggedBlocks.size());
		BLOCKS_EXPANDABLE = new HashSet<>(taggedBlocks.size() + BLOCKS_LEAVES.size());
		BLOCKS_EXPANDABLE.addAll(BLOCKS_LEAVES);
		BLOCKS_MINING = new HashSet<>(taggedBlocks.size());
		BLOCKS_SKIPMINING = new HashSet<>(taggedBlocks.size());
		BLOCKS_STOPMINING = new HashSet<>(taggedBlocks.size());
		BLOCKS_PLACE = new HashMap<>(taggedBlocks.size());
		BLOCKS_NOCAMOUFLAGE = new HashSet<>(taggedBlocks.size());
		BLOCKS_NOBLINK = new HashSet<>(taggedBlocks.size());
		for (final Entry<String, String> taggedBlock : taggedBlocks.entrySet()) {
			final Block block = Block.getBlockFromName(taggedBlock.getKey());
			if (block == null) {
				WarpDrive.logger.info("Ignoring missing block " + taggedBlock.getKey());
				continue;
			}
			for (final String tag : taggedBlock.getValue().replace("\t", " ").replace(",", " ").replace("  ", " ").split(" ")) {
				switch (tag) {
				case "Soil"         : BLOCKS_SOILS.add(block); break;
				case "Log"          : BLOCKS_LOGS.add(block); break;
				case "Leaf"         : BLOCKS_LEAVES.add(block); break;
				case "Anchor"       : BLOCKS_ANCHOR.add(block); break;
				case "NoMass"       : BLOCKS_NOMASS.add(block); break;
				case "LeftBehind"   : BLOCKS_LEFTBEHIND.add(block); break;
				case "Expandable"   : BLOCKS_EXPANDABLE.add(block); break;
				case "Mining"       : BLOCKS_MINING.add(block); break;
				case "SkipMining"   : BLOCKS_SKIPMINING.add(block); break;
				case "StopMining"   : BLOCKS_STOPMINING.add(block); break;
				case "PlaceEarliest": BLOCKS_PLACE.put(block, 0); break;
				case "PlaceEarlier" : BLOCKS_PLACE.put(block, 1); break;
				case "PlaceNormal"  : BLOCKS_PLACE.put(block, 2); break;
				case "PlaceLater"   : BLOCKS_PLACE.put(block, 3); break;
				case "PlaceLatest"  : BLOCKS_PLACE.put(block, 4); break;
				case "NoCamouflage" : BLOCKS_NOCAMOUFLAGE.add(block); break;
				case "NoBlink"      : BLOCKS_NOBLINK.add(block); break;
				default:
					WarpDrive.logger.error("Unsupported tag '" + tag + "' for block " + block);
					break;
				}
			}
		}
		
		// translate tagged entities
		ENTITIES_ANCHOR = new HashSet<>(taggedEntities.size());
		ENTITIES_NOMASS = new HashSet<>(taggedEntities.size());
		ENTITIES_LEFTBEHIND = new HashSet<>(taggedEntities.size());
		ENTITIES_NONLIVINGTARGET = new HashSet<>(taggedEntities.size());
		ENTITIES_LIVING_WITHOUT_AIR = new HashSet<>(taggedEntities.size());
		for (final Entry<String, String> taggedEntity : taggedEntities.entrySet()) {
			final String entityId = taggedEntity.getKey();
			/* we can't detect missing entities, since some of them are 'hacked' in
			if (!EntityList.stringToIDMapping.containsKey(entityId)) {
				WarpDrive.logger.info("Ignoring missing entity " + entityId);
				continue;
			}
			/**/
			for (final String tag : taggedEntity.getValue().replace("\t", " ").replace(",", " ").replace("  ", " ").split(" ")) {
				switch (tag) {
				case "Anchor"          : ENTITIES_ANCHOR.add(entityId); break;
				case "NoMass"          : ENTITIES_NOMASS.add(entityId); break;
				case "LeftBehind"      : ENTITIES_LEFTBEHIND.add(entityId); break;
				case "NonLivingTarget" : ENTITIES_NONLIVINGTARGET.add(entityId); break;
				case "LivingWithoutAir": ENTITIES_LIVING_WITHOUT_AIR.add(entityId); break;
				default:
					WarpDrive.logger.error("Unsupported tag '" + tag + "' for entity " + entityId);
					break;
				}
			}
		}
		
		// translate tagged items
		ITEMS_FLYINSPACE = new HashSet<>(taggedItems.size());
		ITEMS_NOFALLDAMAGE = new HashSet<>(taggedItems.size());
		ITEMS_BREATHING_HELMET = new HashSet<>(taggedItems.size());
		for (final Entry<String, String> taggedItem : taggedItems.entrySet()) {
			final String itemId = taggedItem.getKey();
			final Item item = Item.REGISTRY.getObject(new ResourceLocation(itemId));
			if (item == null) {
				WarpDrive.logger.info("Ignoring missing item " + itemId);
				continue;
			}
			for (final String tag : taggedItem.getValue().replace("\t", " ").replace(",", " ").replace("  ", " ").split(" ")) {
				switch (tag) {
				case "FlyInSpace"     : ITEMS_FLYINSPACE.add(item); break;
				case "NoFallDamage"   : ITEMS_NOFALLDAMAGE.add(item); break;
				case "BreathingHelmet": ITEMS_BREATHING_HELMET.add(item); break;
				default:
					WarpDrive.logger.error("Unsupported tag '" + tag + "' for item " + item);
					break;
				}
			}
		}
		
		adjustHardnessAndResistance();
		print();
	}
	
	private static void print() {
		// translate tagged blocks
		WarpDrive.logger.info("Active blocks dictionary:");
		WarpDrive.logger.info("- " + BLOCKS_ORES.size() + " ores: " + getHashMessage(BLOCKS_ORES));
		WarpDrive.logger.info("- " + BLOCKS_SOILS.size() + " soils: " + getHashMessage(BLOCKS_SOILS));
		WarpDrive.logger.info("- " + BLOCKS_LOGS.size() + " logs: " + getHashMessage(BLOCKS_LOGS));
		WarpDrive.logger.info("- " + BLOCKS_LEAVES.size() + " leaves: " + getHashMessage(BLOCKS_LEAVES));
		WarpDrive.logger.info("- " + BLOCKS_ANCHOR.size() + " anchors: " + getHashMessage(BLOCKS_ANCHOR));
		WarpDrive.logger.info("- " + BLOCKS_NOMASS.size() + " with NoMass tag: " + getHashMessage(BLOCKS_NOMASS));
		WarpDrive.logger.info("- " + BLOCKS_LEFTBEHIND.size() + " with LeftBehind tag: " + getHashMessage(BLOCKS_LEFTBEHIND));
		WarpDrive.logger.info("- " + BLOCKS_EXPANDABLE.size() + " expandable: " + getHashMessage(BLOCKS_EXPANDABLE));
		WarpDrive.logger.info("- " + BLOCKS_MINING.size() + " with Mining tag: " + getHashMessage(BLOCKS_MINING));
		WarpDrive.logger.info("- " + BLOCKS_SKIPMINING.size() + " with SkipMining tag: " + getHashMessage(BLOCKS_SKIPMINING));
		WarpDrive.logger.info("- " + BLOCKS_STOPMINING.size() + " with StopMining tag: " + getHashMessage(BLOCKS_STOPMINING));
		WarpDrive.logger.info("- " + BLOCKS_PLACE.size() + " with Placement priority: " + getHashMessage(BLOCKS_PLACE));
		
		// translate tagged entities
		WarpDrive.logger.info("Active entities dictionary:");
		WarpDrive.logger.info("- " + ENTITIES_ANCHOR.size() + " anchors: " + getHashMessage(ENTITIES_ANCHOR));
		WarpDrive.logger.info("- " + ENTITIES_NOMASS.size() + " with NoMass tag: " + getHashMessage(ENTITIES_NOMASS));
		WarpDrive.logger.info("- " + ENTITIES_LEFTBEHIND.size() + " with LeftBehind tag: " + getHashMessage(ENTITIES_LEFTBEHIND));
		WarpDrive.logger.info("- " + ENTITIES_NONLIVINGTARGET.size() + " with NonLivingTarget tag: " + getHashMessage(ENTITIES_NONLIVINGTARGET));
		WarpDrive.logger.info("- " + ENTITIES_LIVING_WITHOUT_AIR.size() + " with LivingWithoutAir tag: " + getHashMessage(ENTITIES_LIVING_WITHOUT_AIR));
		
		// translate tagged items
		WarpDrive.logger.info("Active items dictionary:");
		WarpDrive.logger.info("- " + ITEMS_FLYINSPACE.size() + " allowing fly in space: " + getHashMessage(ITEMS_FLYINSPACE));
		WarpDrive.logger.info("- " + ITEMS_NOFALLDAMAGE.size() + " absorbing fall damages: " + getHashMessage(ITEMS_NOFALLDAMAGE));
		WarpDrive.logger.info("- " + ITEMS_BREATHING_HELMET.size() + " allowing breathing air: " + getHashMessage(ITEMS_BREATHING_HELMET));
	}
	
	private static void adjustHardnessAndResistance() {
		// Apply explosion resistance adjustments
		Blocks.OBSIDIAN.setResistance(60.0F);
		Blocks.ENCHANTING_TABLE.setResistance(60.0F);
		Blocks.ENDER_CHEST.setResistance(60.0F);
		Blocks.ANVIL.setResistance(60.0F);
		Blocks.WATER.setResistance(30.0F);
		Blocks.FLOWING_WATER.setResistance(30.0F);
		Blocks.LAVA.setResistance(30.0F);
		Blocks.FLOWING_LAVA.setResistance(30.0F);
		
		// keep IC2 Reinforced stone stats 'as is'
		/*
		if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			Block blockReinforcedStone = (Block) Block.blockRegistry.getObject("IC2:blockAlloy");
		}
		/**/
		
		// scan blocks registry
		for (final ResourceLocation resourceLocation : Block.REGISTRY.getKeys()) {
			final Block block = Block.REGISTRY.getObject(resourceLocation);
			WarpDrive.logger.debug("Checking block registry for '" + resourceLocation + "': " + block);
			
			// get hardness and blast resistance
			float hardness = -2.0F;
			if (WarpDrive.fieldBlockHardness != null) {
				// WarpDrive.fieldBlockHardness.setAccessible(true);
				try {
					hardness = (float) WarpDrive.fieldBlockHardness.get(block);
				} catch (final IllegalArgumentException | IllegalAccessException exception) {
					exception.printStackTrace();
					WarpDrive.logger.error("Unable to access block hardness value '" + resourceLocation + "' " + block);
				}
			}
			
			final float blastResistance = block.getExplosionResistance(null);
			
			// check actual values
			if (hardness != -2.0F) {
				if (hardness < 0 && !(BLOCKS_ANCHOR.contains(block))) {// unbreakable block
					WarpDrive.logger.warn("Warning: non-anchor block with unbreakable hardness '" + resourceLocation + "' " + block + " (" + hardness + ")");
				} else if ( hardness > WarpDriveConfig.HULL_HARDNESS[0]
				         && !( block instanceof BlockAbstractBase
				            || block instanceof BlockAbstractContainer
				            || block instanceof BlockHullGlass
				            || block instanceof BlockHullSlab
				            || block instanceof BlockHullStairs
				            || BLOCKS_ANCHOR.contains(block) ) ) {
					WarpDrive.logger.warn("Warning: non-hull block with high hardness '" + resourceLocation + "' " + block + " (" + hardness + ")");
				}
			}
			if ( blastResistance > WarpDriveConfig.HULL_BLAST_RESISTANCE[0]
			   && !( block instanceof BlockAbstractBase
			      || block instanceof BlockAbstractContainer
			      || block instanceof BlockHullGlass
			      || block instanceof BlockHullSlab
			      || block instanceof BlockHullStairs
			      || BLOCKS_ANCHOR.contains(block) ) ) {
				block.setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[0]);
				WarpDrive.logger.warn("Warning: non-anchor block with high blast resistance '" + resourceLocation + "' " + block + " (" + hardness + ")");
				if (adjustResistance) {// TODO: not implemented
					WarpDrive.logger.warn("Adjusting blast resistance of '" + resourceLocation + "' " + block + " from " + blastResistance + " to " + block.getExplosionResistance(null));
					if (block.getExplosionResistance(null) > WarpDriveConfig.HULL_BLAST_RESISTANCE[0]) {
						WarpDrive.logger.error("Blacklisting block with high blast resistance '" + resourceLocation + "' " + block + " (" + blastResistance + ")");
						BLOCKS_ANCHOR.add(block);
						BLOCKS_STOPMINING.add(block);
					}
				}
			}
			
			if (WarpDriveConfig.LOGGING_DICTIONARY) {
				WarpDrive.logger.info("Block registry for '" + resourceLocation + "': Block " + block
					+ " with hardness " + (WarpDrive.fieldBlockHardness != null ? hardness : "-") + " resistance " + block.getExplosionResistance(null));
			}
		}
	}
	
	private static String getHashMessage(final HashSet hashSet) {
		final StringBuilder message = new StringBuilder();
		for (final Object object : hashSet) {
			if (message.length() > 0) {
				message.append(", ");
			}
			if (object instanceof Block) {
				message.append(((Block) object).getRegistryName());
			} else if (object instanceof String) {
				message.append((String) object);
			} else {
				message.append(object);
			}
		}
		return message.toString();
	}
	
	private static String getHashMessage(final HashMap<Block, Integer> hashMap) {
		final StringBuilder message = new StringBuilder();
		for (final Entry<Block, Integer> entry : hashMap.entrySet()) {
			if (message.length() > 0) {
				message.append(", ");
			}
			message.append(entry.getKey().getRegistryName()).append("=").append(entry.getValue());
		}
		return message.toString();
	}
	
	public static NBTBase writeItemsToNBT(final HashSet<Item> hashSetItem) {
		final NBTTagList nbtTagList = new NBTTagList();
		assert(hashSetItem != null);
		for (final Item item : hashSetItem) {
			final String registryName = item.getRegistryName().toString();
			nbtTagList.appendTag(new NBTTagString(registryName));
		}
		return nbtTagList;
	}
	
	public static HashSet<Item> readItemsFromNBT(final NBTTagList nbtTagList) {
		assert(nbtTagList != null);
		final int size = nbtTagList.tagCount();
		final HashSet<Item> hashSetItem = new HashSet<>(Math.max(8, size));
		
		if (size > 0) {
			for (int index = 0; index < nbtTagList.tagCount(); index++) {
				final String registryName = nbtTagList.getStringTagAt(index);
				final Item item = Item.REGISTRY.getObject(new ResourceLocation(registryName));
				if (item != null) {
					hashSetItem.add(item);
				} else {
					WarpDrive.logger.warn(String.format("Ignoring unknown item %s", registryName));
				}
			}
		}
		return hashSetItem;
	}
}
