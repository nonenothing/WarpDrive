package cr0s.warpdrive;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.fml.common.Mod;
import cr0s.warpdrive.block.BlockChunkLoader;
import cr0s.warpdrive.block.BlockLaser;
import cr0s.warpdrive.block.BlockLaserMedium;
import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.block.TileEntityAbstractChunkLoading;
import cr0s.warpdrive.block.TileEntityChunkLoader;
import cr0s.warpdrive.block.TileEntityLaser;
import cr0s.warpdrive.block.TileEntityLaserMedium;
import cr0s.warpdrive.block.atomic.BlockAcceleratorControlPoint;
// import cr0s.warpdrive.block.atomic.BlockAcceleratorController;
import cr0s.warpdrive.block.atomic.BlockChiller;
import cr0s.warpdrive.block.atomic.BlockElectromagnetGlass;
import cr0s.warpdrive.block.atomic.BlockElectromagnetPlain;
import cr0s.warpdrive.block.atomic.BlockParticlesCollider;
import cr0s.warpdrive.block.atomic.BlockParticlesInjector;
import cr0s.warpdrive.block.atomic.BlockVoidShellGlass;
import cr0s.warpdrive.block.atomic.BlockVoidShellPlain;
import cr0s.warpdrive.block.atomic.TileEntityAcceleratorControlPoint;
// import cr0s.warpdrive.block.atomic.TileEntityAcceleratorController;
import cr0s.warpdrive.block.atomic.TileEntityParticlesInjector;
import cr0s.warpdrive.block.breathing.BlockAir;
import cr0s.warpdrive.block.breathing.BlockAirGenerator;
import cr0s.warpdrive.block.decoration.BlockDecorative;
import cr0s.warpdrive.block.decoration.BlockGas;
import cr0s.warpdrive.block.decoration.BlockLamp_bubble;
import cr0s.warpdrive.block.decoration.BlockLamp_flat;
import cr0s.warpdrive.block.decoration.BlockLamp_long;
import cr0s.warpdrive.block.detection.BlockCamera;
import cr0s.warpdrive.block.detection.BlockCloakingCoil;
import cr0s.warpdrive.block.detection.BlockCloakingCore;
import cr0s.warpdrive.block.detection.BlockMonitor;
import cr0s.warpdrive.block.detection.BlockRadar;
import cr0s.warpdrive.block.detection.BlockSiren;
import cr0s.warpdrive.block.detection.BlockWarpIsolation;
import cr0s.warpdrive.block.energy.BlockEnanReactorCore;
import cr0s.warpdrive.block.energy.BlockEnanReactorLaser;
import cr0s.warpdrive.block.energy.BlockEnergyBank;
import cr0s.warpdrive.block.energy.BlockIC2reactorLaserMonitor;
import cr0s.warpdrive.block.forcefield.BlockForceField;
import cr0s.warpdrive.block.forcefield.BlockForceFieldProjector;
import cr0s.warpdrive.block.forcefield.BlockForceFieldRelay;
import cr0s.warpdrive.block.hull.BlockHullStairs;
import cr0s.warpdrive.block.movement.BlockShipController;
import cr0s.warpdrive.block.movement.BlockShipCore;
import cr0s.warpdrive.block.passive.BlockHighlyAdvancedMachine;
import cr0s.warpdrive.block.passive.BlockIridium;
import cr0s.warpdrive.block.passive.BlockTransportBeacon;
import cr0s.warpdrive.config.Recipes;
import cr0s.warpdrive.item.ItemAirCanisterFull;
import cr0s.warpdrive.item.ItemComponent;
import cr0s.warpdrive.item.ItemCrystalToken;
import cr0s.warpdrive.item.ItemElectromagneticCell;
import cr0s.warpdrive.item.ItemForceFieldShape;
import cr0s.warpdrive.item.ItemForceFieldUpgrade;
import cr0s.warpdrive.item.ItemIC2reactorLaserFocus;
import cr0s.warpdrive.item.ItemTuningFork;
import cr0s.warpdrive.item.ItemUpgrade;
import cr0s.warpdrive.item.ItemWarpArmor;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import cr0s.warpdrive.api.IBlockBase;

import cr0s.warpdrive.block.building.BlockShipScanner;
import cr0s.warpdrive.block.collection.BlockLaserTreeFarm;
import cr0s.warpdrive.block.collection.BlockMiningLaser;
import cr0s.warpdrive.block.collection.TileEntityLaserTreeFarm;
import cr0s.warpdrive.block.collection.TileEntityMiningLaser;
import cr0s.warpdrive.block.detection.BlockCamera;
import cr0s.warpdrive.block.detection.BlockCloakingCoil;
import cr0s.warpdrive.block.detection.BlockCloakingCore;
import cr0s.warpdrive.block.detection.BlockMonitor;
import cr0s.warpdrive.block.detection.BlockRadar;
import cr0s.warpdrive.block.detection.BlockSiren;
import cr0s.warpdrive.block.detection.BlockWarpIsolation;
import cr0s.warpdrive.block.detection.TileEntityCamera;
import cr0s.warpdrive.block.detection.TileEntityCloakingCore;
import cr0s.warpdrive.block.detection.TileEntityMonitor;
import cr0s.warpdrive.block.detection.TileEntityRadar;
import cr0s.warpdrive.block.detection.TileEntitySiren;
import cr0s.warpdrive.block.energy.BlockEnanReactorCore;
import cr0s.warpdrive.block.energy.BlockEnanReactorLaser;
import cr0s.warpdrive.block.energy.BlockEnergyBank;
import cr0s.warpdrive.block.energy.BlockIC2reactorLaserMonitor;
import cr0s.warpdrive.block.energy.TileEntityEnanReactorCore;
import cr0s.warpdrive.block.energy.TileEntityEnanReactorLaser;
import cr0s.warpdrive.block.energy.TileEntityEnergyBank;
import cr0s.warpdrive.block.energy.TileEntityIC2reactorLaserMonitor;
import cr0s.warpdrive.block.forcefield.BlockForceField;
import cr0s.warpdrive.block.forcefield.BlockForceFieldProjector;
import cr0s.warpdrive.block.forcefield.BlockForceFieldRelay;
import cr0s.warpdrive.block.forcefield.ItemBlockForceFieldProjector;
import cr0s.warpdrive.block.forcefield.ItemBlockForceFieldRelay;
import cr0s.warpdrive.block.forcefield.TileEntityForceField;
import cr0s.warpdrive.block.forcefield.TileEntityForceFieldProjector;
import cr0s.warpdrive.block.forcefield.TileEntityForceFieldRelay;
import cr0s.warpdrive.block.hull.BlockHullGlass;
import cr0s.warpdrive.block.hull.BlockHullPlain;
import cr0s.warpdrive.block.hull.BlockHullStairs;
import cr0s.warpdrive.block.hull.ItemBlockHull;
import cr0s.warpdrive.block.movement.BlockLift;
import cr0s.warpdrive.block.movement.BlockTransporter;
import cr0s.warpdrive.block.weapon.BlockLaserCamera;
import cr0s.warpdrive.block.weapon.BlockWeaponController;
import cr0s.warpdrive.command.CommandDebug;
import cr0s.warpdrive.command.CommandEntity;
import cr0s.warpdrive.command.CommandGenerate;
import cr0s.warpdrive.command.CommandInvisible;
import cr0s.warpdrive.command.CommandJumpgates;
import cr0s.warpdrive.command.CommandSpace;
import cr0s.warpdrive.config.RecipeParticleShapedOre;
import cr0s.warpdrive.config.RecipeTuningDriver;
import cr0s.warpdrive.config.Recipes;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.damage.DamageAsphyxia;
import cr0s.warpdrive.damage.DamageCold;
import cr0s.warpdrive.damage.DamageIrradiation;
import cr0s.warpdrive.damage.DamageLaser;
import cr0s.warpdrive.damage.DamageShock;
import cr0s.warpdrive.damage.DamageTeleportation;
import cr0s.warpdrive.damage.DamageWarm;
import cr0s.warpdrive.data.CamerasRegistry;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.data.CloakManager;
import cr0s.warpdrive.data.JumpgatesRegistry;
import cr0s.warpdrive.data.StarMapRegistry;
import cr0s.warpdrive.event.ClientHandler;
import cr0s.warpdrive.event.LivingHandler;
import cr0s.warpdrive.event.WorldHandler;
import cr0s.warpdrive.item.ItemAirCanisterFull;
import cr0s.warpdrive.item.ItemComponent;
import cr0s.warpdrive.item.ItemCrystalToken;
import cr0s.warpdrive.item.ItemElectromagneticCell;
import cr0s.warpdrive.item.ItemForceFieldShape;
import cr0s.warpdrive.item.ItemForceFieldUpgrade;
import cr0s.warpdrive.item.ItemIC2reactorLaserFocus;
import cr0s.warpdrive.item.ItemTuningDriver;
import cr0s.warpdrive.item.ItemTuningFork;
import cr0s.warpdrive.item.ItemUpgrade;
import cr0s.warpdrive.item.ItemWarpArmor;
import cr0s.warpdrive.network.PacketHandler;
import cr0s.warpdrive.render.ClientCameraHandler;
import cr0s.warpdrive.render.RenderOverlayCamera;
import cr0s.warpdrive.world.BiomeSpace;
import cr0s.warpdrive.world.HyperSpaceWorldGenerator;
import cr0s.warpdrive.world.HyperSpaceWorldProvider;
import cr0s.warpdrive.world.SpaceWorldGenerator;
import cr0s.warpdrive.world.SpaceWorldProvider;

import net.minecraftforge.oredict.RecipeSorter;


import javax.annotation.Nullable;

@Mod(modid = WarpDrive.MODID, name = "WarpDrive", version = WarpDrive.VERSION, dependencies = "after:IC2;" + " after:CoFHCore;" + " after:ComputerCraft;"
		+ " after:OpenComputer;" + " after:CCTurtle;" + " after:gregtech;" + " after:AppliedEnergistics;" + " after:EnderIO;")
public class WarpDrive implements LoadingCallback {
	public static final String MODID = "warpdrive";
	public static final String VERSION = "@version@";
	public static final String PREFIX = MODID + ":";
	public static final boolean isDev = VERSION.equals("@" + "version" + "@") || VERSION.contains("-dev");
	public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("[WarpDrive]".getBytes()), "[WarpDrive]");
	
	public static Block blockShipCore;
	public static Block blockShipController;
	public static Block blockRadar;
	public static Block blockWarpIsolation;
	public static Block blockAirGenerator;
	public static Block blockLaser;
	public static Block blockLaserCamera;
	public static Block blockWeaponController;
	public static Block blockCamera;
	public static Block blockMonitor;
	public static Block blockLaserMedium;
	public static Block blockMiningLaser;
	public static Block blockLaserTreeFarm;
	public static Block blockLift;
	public static Block blockShipScanner;
	public static Block blockCloakingCore;
	public static Block blockCloakingCoil;
	public static Block blockTransporter;
	public static Block blockIC2reactorLaserMonitor;
	public static Block blockEnanReactorCore;
	public static Block blockEnanReactorLaser;
	public static Block blockEnergyBank;
	public static Block blockAir;
	public static Block blockGas;
	public static Block blockIridium;
	public static Block blockLamp_bubble;
	public static Block blockLamp_flat;
	public static Block blockLamp_long;
	public static Block blockHighlyAdvancedMachine;
	public static Block blockTransportBeacon;
	public static Block blockChunkLoader;
	public static Block[] blockForceFields;
	public static Block[] blockForceFieldProjectors;
	public static Block[] blockForceFieldRelays;
	public static Block blockAcceleratorController;
	public static Block blockAcceleratorControlPoint;
	public static Block blockParticlesCollider;
	public static Block blockParticlesInjector;
	public static Block blockVoidShellPlain;
	public static Block blockVoidShellGlass;
	public static Block[] blockElectromagnetPlain;
	public static Block[] blockElectromagnetGlass;
	public static Block[] blockChillers;
	public static Block blockDecorative;
	public static Block[] blockHulls_plain;
	public static Block[] blockHulls_glass;
	public static Block[][] blockHulls_stairs;
	public static Block[][] blockHulls_slab;
	public static Block blockSiren;
	
	public static Item itemIC2reactorLaserFocus;
	public static ItemComponent itemComponent;
	public static ItemCrystalToken itemCrystalToken;
	public static ItemUpgrade itemUpgrade;
	public static ItemTuningFork itemTuningFork;
	public static ItemTuningDriver itemTuningDriver;
	public static ItemForceFieldShape itemForceFieldShape;
	public static ItemForceFieldUpgrade itemForceFieldUpgrade;
	public static ItemElectromagneticCell itemElectromagneticCell;
	
	public static final ArmorMaterial armorMaterial = EnumHelper.addArmorMaterial("WARP", "warp", 18, new int[] { 2, 6, 5, 2 }, 9, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0.0F);
	public static ItemArmor[] itemWarpArmor;
	public static ItemAirCanisterFull itemAirCanisterFull;
	
	public static DamageAsphyxia damageAsphyxia;
	public static DamageCold damageCold;
	public static DamageIrradiation damageIrradiation;
	public static DamageLaser damageLaser;
	public static DamageShock damageShock;
	public static DamageTeleportation damageTeleportation;
	public static DamageWarm damageWarm;
	
	public static Biome spaceBiome;
	public static DimensionType dimensionTypeSpace;
	public static DimensionType dimensionTypeHyperSpace;
	@SuppressWarnings("FieldCanBeLocal")
	private SpaceWorldGenerator spaceWorldGenerator;
	@SuppressWarnings("FieldCanBeLocal")
	private HyperSpaceWorldGenerator hyperSpaceWorldGenerator;
	
	public static Field fieldBlockHardness = null;
	
	// Client settings
	public static final CreativeTabs creativeTabWarpDrive = new CreativeTabWarpDrive("WarpDrive", "WarpDrive").setBackgroundImageName("warpdrive:creativeTab");
	
	@Instance(WarpDrive.MODID)
	public static WarpDrive instance;
	@SidedProxy(clientSide = "cr0s.warpdrive.client.ClientProxy", serverSide = "cr0s.warpdrive.CommonProxy")
	public static CommonProxy proxy;
	
	public static StarMapRegistry starMap;
	public static JumpgatesRegistry jumpgates;
	public static CloakManager cloaks;
	public static CamerasRegistry cameras;
	
	@SuppressWarnings("FieldCanBeLocal")
	private static WarpDrivePeripheralHandler peripheralHandler = null;
	
	public static Logger logger;
	
	@EventHandler
	public void onFMLPreInitialization(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		
		WarpDriveConfig.onFMLpreInitialization(event.getModConfigurationDirectory().getAbsolutePath());
		
		RecipeSorter.register("warpdrive:particleShaped", RecipeParticleShapedOre.class, RecipeSorter.Category.SHAPED, "before:minecraft:shaped");
		RecipeSorter.register("warpdrive:tuningDriver", RecipeTuningDriver.class, RecipeSorter.Category.SHAPELESS, "before:minecraft:shapeless");
		
		// open access to Block.blockHardness
		fieldBlockHardness = Commons.getField(Block.class, "blockHardness", "field_149782_v");
		
		// building blocks
		blockShipScanner = new BlockShipScanner("blockShipScanner");
		
		// collection blocks
		blockMiningLaser = new BlockMiningLaser("blockMiningLaser");
		blockLaserTreeFarm = new BlockLaserTreeFarm("blockLaserTreeFarm");
		
		// detection blocks
		blockCamera = new BlockCamera("blockCamera");
		blockCloakingCore = new BlockCloakingCore("blockCloakingCore");
		blockCloakingCoil = new BlockCloakingCoil("blockCloakingCoil");
		blockMonitor = new BlockMonitor("blockMonitor");
		blockRadar = new BlockRadar("blockRadar");
		blockWarpIsolation = new BlockWarpIsolation("blockWarpIsolation");
		
		// energy blocks and items
		blockEnanReactorCore = new BlockEnanReactorCore("blockEnanReactorCore");
		blockEnanReactorLaser = new BlockEnanReactorLaser("blockEnanReactorLaser");
		blockEnergyBank = new BlockEnergyBank("blockEnergyBank");
		
		if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			blockIC2reactorLaserMonitor = new BlockIC2reactorLaserMonitor("blockIC2reactorLaserMonitor");
			itemIC2reactorLaserFocus = new ItemIC2reactorLaserFocus("itemIC2reactorLaserFocus");
		}
		
		// movement blocks
		blockLift = new BlockLift("blockLift");
		blockShipController = new BlockShipController("blockShipController");
		blockShipCore = new BlockShipCore("blockShipCore");
		blockTransporter = new BlockTransporter("blockTransporter");
		
		// passive blocks
		blockAir = new BlockAir("blockAir");
		blockHighlyAdvancedMachine = new BlockHighlyAdvancedMachine("blockHighlyAdvancedMachine");
		blockIridium = new BlockIridium("blockIridium");
		blockTransportBeacon = new BlockTransportBeacon("blockTransportBeacon");
		
		// weapon blocks
		blockLaserCamera = new BlockLaserCamera("blockLaserCamera");
		blockWeaponController = new BlockWeaponController("blockWeaponController");
		
		// common blocks
		blockAirGenerator = new BlockAirGenerator("blockAirGenerator");
		blockChunkLoader = new BlockChunkLoader("blockChunkLoader");
		blockLaser = new BlockLaser("blockLaser");
		blockLaserMedium = new BlockLaserMedium("blockLaserMedium");
		
		// force field blocks and items
		blockForceFields = new Block[3];
		blockForceFieldProjectors = new Block[3];
		blockForceFieldRelays = new Block[3];
		for(byte tier = 1; tier <= 3; tier++) {
			int index = tier - 1;
			blockForceFields[index] = new BlockForceField("blockForceField" + tier, tier);
			blockForceFieldProjectors[index] = new BlockForceFieldProjector("blockProjector" + tier, tier);
			blockForceFieldRelays[index] = new BlockForceFieldRelay("blockForceFieldRelay" + tier, tier);
		}
		/* @TODO security station
		blockSecurityStation = new BlockSecurityStation("blockSecurityStation");
		*/
		itemForceFieldShape = new ItemForceFieldShape("itemForceFieldShape");
		itemForceFieldUpgrade = new ItemForceFieldUpgrade("itemForceFieldUpgrade");
		
		/*
		// ACCELERATOR CONTROLLER
		blockAcceleratorController = new BlockAcceleratorController();
		GameRegistry.registerBlock(blockAcceleratorController, ItemBlockAbstractBase.class, "blockAcceleratorController");
		GameRegistry.registerTileEntity(TileEntityAcceleratorController.class, MODID + ":blockAcceleratorController");
		/**/
		// ACCELERATOR CONTROL POINT 
		blockAcceleratorControlPoint = new BlockAcceleratorControlPoint("blockAcceleratorControlPoint");
		
		// PARTICLES COLLIDER 
		blockParticlesCollider = new BlockParticlesCollider("blockParticlesCollider");
		
		// PARTICLES INJECTOR 
		blockParticlesInjector = new BlockParticlesInjector("blockParticlesInjector");
		
		// VOID SHELL PLAIN/GLASS 
		blockVoidShellPlain = new BlockVoidShellPlain("blockVoidShellPlain");
		blockVoidShellGlass = new BlockVoidShellGlass("blockVoidShellGlass");
		
		blockElectromagnetPlain = new Block[3];
		blockElectromagnetGlass = new Block[3];
		blockChillers = new Block[3];
		for(byte tier = 1; tier <= 3; tier++) {
			int index = tier - 1;
			// plain electromagnets 
			blockElectromagnetPlain[index] = new BlockElectromagnetPlain("blockElectromagnetPlain" + tier, tier);
			
			// glass electromagnets
			blockElectromagnetGlass[index] = new BlockElectromagnetGlass("blockElectromagnetGlass" + tier, tier);
			
			// chiller
			blockChillers[index] = new BlockChiller("blockChiller" + tier, tier);
		}
		
		// decorative
		blockDecorative = new BlockDecorative("blockDecorative");
		blockGas = new BlockGas("blockGas");
		blockLamp_bubble = new BlockLamp_bubble("blockLamp_bubble");
		blockLamp_flat = new BlockLamp_flat("blockLamp_flat");
		blockLamp_long = new BlockLamp_long("blockLamp_long");
		
		// hull blocks
		blockHulls_plain = new Block[3];
		blockHulls_glass = new Block[3];
		blockHulls_stairs = new Block[3][16];
		blockHulls_slab = new Block[3][16];
		
		for(byte tier = 1; tier <= 3; tier++) {
			int index = tier - 1;
			blockHulls_plain[index] = new BlockHullPlain("blockHull" + tier + "_plain", tier);
			blockHulls_glass[index] = new BlockHullGlass("blockHull" + tier + "_glass", tier);
			for (EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
				blockHulls_stairs[index][enumDyeColor.getMetadata()] = new BlockHullStairs("blockHull" + tier + "_stairs_" + enumDyeColor.getName(), blockHulls_plain[index].getStateFromMeta(enumDyeColor.getMetadata()), tier);
			}
		}
		
		// sirens
		blockSiren = new BlockSiren("blockSiren");
		
		// component items
		itemComponent = new ItemComponent("itemComponent");
		itemCrystalToken = new ItemCrystalToken("itemCrystalToken");
		
		// warp armor
		itemWarpArmor = new ItemArmor[4];
		itemWarpArmor[0] = new ItemWarpArmor("itemWarpArmor_" + ItemWarpArmor.suffixes[0], armorMaterial, 3, EntityEquipmentSlot.HEAD);
		itemWarpArmor[0] = new ItemWarpArmor("itemWarpArmor_" + ItemWarpArmor.suffixes[1], armorMaterial, 3, EntityEquipmentSlot.CHEST);
		itemWarpArmor[0] = new ItemWarpArmor("itemWarpArmor_" + ItemWarpArmor.suffixes[2], armorMaterial, 3, EntityEquipmentSlot.LEGS);
		itemWarpArmor[0] = new ItemWarpArmor("itemWarpArmor_" + ItemWarpArmor.suffixes[3], armorMaterial, 3, EntityEquipmentSlot.FEET);
		
		itemAirCanisterFull = new ItemAirCanisterFull("itemAirCanisterFull");
		
		if (WarpDriveConfig.RECIPES_ENABLE_VANILLA) {
			itemUpgrade = new ItemUpgrade("itemUpgrade");
		}
		
        // tool items
		itemTuningFork = new ItemTuningFork("itemTuningFork");
		itemTuningDriver = new ItemTuningDriver("itemTuningDriver");
		
		// electromagnetic cell
		itemElectromagneticCell = new ItemElectromagneticCell("itemElectromagneticCell");
		
		// damage sources
		damageAsphyxia = new DamageAsphyxia();
		damageCold = new DamageCold();
		damageIrradiation = new DamageIrradiation();
		damageLaser = new DamageLaser();
		damageShock = new DamageShock();
		damageTeleportation = new DamageTeleportation();
		damageWarm = new DamageWarm();
		
		// entities
		proxy.registerEntities();
		proxy.registerRendering();
		
		// chunk loading
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, instance);
		
		// world generation
		spaceWorldGenerator = new SpaceWorldGenerator();
		GameRegistry.registerWorldGenerator(spaceWorldGenerator, 0);
		hyperSpaceWorldGenerator = new HyperSpaceWorldGenerator();
		GameRegistry.registerWorldGenerator(hyperSpaceWorldGenerator, 0);
		
		Biome.BiomeProperties biomeProperties = new Biome.BiomeProperties("Space").setRainDisabled().setWaterColor(0);
		spaceBiome = (new BiomeSpace(biomeProperties));
		BiomeDictionary.registerBiomeType(spaceBiome, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.WASTELAND);
		dimensionTypeSpace = DimensionType.register("Space", "_space", WarpDriveConfig.G_SPACE_PROVIDER_ID, HyperSpaceWorldProvider.class, true);
		dimensionTypeHyperSpace = DimensionType.register("Hyperspace", "_hyperspace", WarpDriveConfig.G_HYPERSPACE_PROVIDER_ID, HyperSpaceWorldProvider.class, true);
		
		// only create dimensions if we own them
		for (CelestialObject celestialObject : WarpDriveConfig.celestialObjects) {
			if (celestialObject.isWarpDrive) {
				if (celestialObject.isSpace()) {
					DimensionManager.registerDimension(celestialObject.dimensionId, dimensionTypeSpace);
				} else if (celestialObject.isHyperspace()) {
					DimensionManager.registerDimension(celestialObject.dimensionId, dimensionTypeHyperSpace);
				} else {
					WarpDrive.logger.error(String.format("Only space and hyperspace dimensions can be provided by WarpDrive. Dimension %d is not what of those.",
						celestialObject.dimensionId));
				}
			}
		}
		
		proxy.onForgePreInitialisation();
		
		if (FMLCommonHandler.instance().getSide().isClient()) {
			creativeTabWarpDrive.setBackgroundImageName("items.png");
			
			MinecraftForge.EVENT_BUS.register(new RenderOverlayCamera(Minecraft.getMinecraft()));
			
			MinecraftForge.EVENT_BUS.register(new ClientCameraHandler());
			
			// @TODO MC1.10 force field rendering
			/*
			RenderBlockStandard.renderId = RenderingRegistry.getNextAvailableRenderId();
			RenderingRegistry.registerBlockHandler(RenderBlockStandard.instance);
			
			RenderBlockForceField.renderId = RenderingRegistry.getNextAvailableRenderId();
			RenderingRegistry.registerBlockHandler(RenderBlockForceField.instance);
			/**/
		}
	}
	
	@EventHandler
	public void onFMLInitialization(FMLInitializationEvent event) {
		PacketHandler.init();
		
		WarpDriveConfig.onFMLInitialization();
	}
	
	@EventHandler
	public void onFMLPostInitialization(FMLPostInitializationEvent event) {
		// load all owned dimensions at boot
		for (CelestialObject celestialObject : WarpDriveConfig.celestialObjects) {
			if (celestialObject.isWarpDrive) {
				DimensionManager.getWorld(celestialObject.dimensionId);
			}
		}
		
		WarpDriveConfig.onFMLPostInitialization();
		
		if (WarpDriveConfig.RECIPES_ENABLE_DYNAMIC) {
			Recipes.initDynamic();
		} else {
			if (WarpDriveConfig.isIndustrialCraft2Loaded && WarpDriveConfig.RECIPES_ENABLE_IC2) {
				Recipes.initIC2();
			}
			if (WarpDriveConfig.isIndustrialCraft2Loaded && WarpDriveConfig.RECIPES_ENABLE_HARD_IC2) {
				Recipes.initHardIC2();
			}
			if (WarpDriveConfig.RECIPES_ENABLE_VANILLA) {
				Recipes.initVanilla();
			}
		}
		
		// Registers
		starMap = new StarMapRegistry();
		jumpgates = new JumpgatesRegistry();
		cloaks = new CloakManager();
		cameras = new CamerasRegistry();
		
		// Event handlers
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		
		MinecraftForge.EVENT_BUS.register(new LivingHandler());
		
		if (WarpDriveConfig.isComputerCraftLoaded) {
			peripheralHandler = new WarpDrivePeripheralHandler();
			peripheralHandler.register();
		}
		
		WorldHandler worldHandler = new WorldHandler();
		MinecraftForge.EVENT_BUS.register(worldHandler);
	}
	
	@EventHandler
	public void onFMLServerStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandGenerate());
		event.registerServerCommand(new CommandSpace());
		event.registerServerCommand(new CommandInvisible());
		event.registerServerCommand(new CommandJumpgates());
		event.registerServerCommand(new CommandDebug());
		event.registerServerCommand(new CommandEntity());
	}
	
	private Ticket registerChunkLoadTE(TileEntityAbstractChunkLoading tileEntity, boolean refreshLoading) {
		World worldObj = tileEntity.getWorld();
		if (ForgeChunkManager.ticketCountAvailableFor(this, worldObj) > 0) {
			Ticket ticket = ForgeChunkManager.requestTicket(this, worldObj, Type.NORMAL);
			if (ticket != null) {
				tileEntity.giveTicket(ticket); // FIXME calling the caller is a bad idea
				if (refreshLoading)
					tileEntity.refreshLoading();
				return ticket;
			} else {
				WarpDrive.logger.error("Ticket not granted");
			}
		} else {
			WarpDrive.logger.error("No tickets left!");
		}
		return null;
	}
	
	public Ticket registerChunkLoadTE(TileEntityAbstractChunkLoading te) {
		return registerChunkLoadTE(te, true);
	}
	
	public Ticket getTicket(TileEntityAbstractChunkLoading te) {
		return registerChunkLoadTE(te, false);
	}
	
	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
		for (Ticket ticket : tickets) {
			NBTTagCompound data = ticket.getModData();
			if (data != null) {
				int w = data.getInteger("ticketWorldObj");
				int x = data.getInteger("ticketX");
				int y = data.getInteger("ticketY");
				int z = data.getInteger("ticketZ");
				if (w != 0 || x != 0 || y != 0 || z != 0) {
					WorldServer worldServer = DimensionManager.getWorld(w);
					if (worldServer != null) {// skip non-loaded worlds
						TileEntity tileEntity = worldServer.getTileEntity(new BlockPos(x, y, z));
						if (tileEntity != null && tileEntity instanceof TileEntityAbstractChunkLoading) {
							if (((TileEntityAbstractChunkLoading) tileEntity).shouldChunkLoad()) {
								WarpDrive.logger.info("ChunkLoadingTicket is loading " + tileEntity);
								((TileEntityAbstractChunkLoading) tileEntity).giveTicket(ticket);
								((TileEntityAbstractChunkLoading) tileEntity).refreshLoading(true);
								return;
							}
						}
					}
				}
			}
			
			ForgeChunkManager.releaseTicket(ticket);
		}
	}
	
	@SuppressWarnings("ConstantConditions")
	@Mod.EventHandler
	public void onFMLMissingMappings(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping: event.get()) {
			if (mapping.type == GameRegistry.Type.ITEM) {
				switch (mapping.name) {
					case "WarpDrive:airBlock":
						mapping.remap(Item.getItemFromBlock(blockAir));
						break;
					case "WarpDrive:airCanisterFull":
						mapping.remap(itemAirCanisterFull);
						break;
					case "WarpDrive:airgenBlock":
						mapping.remap(Item.getItemFromBlock(blockAirGenerator));
						break;
					case "WarpDrive:blockHAMachine":
						mapping.remap(Item.getItemFromBlock(blockHighlyAdvancedMachine));
						break;
					case "WarpDrive:boosterBlock":
						mapping.remap(Item.getItemFromBlock(blockLaserMedium));
						break;
					case "WarpDrive:cameraBlock":
						mapping.remap(Item.getItemFromBlock(blockCamera));
						break;
					case "WarpDrive:chunkLoader":
						mapping.remap(Item.getItemFromBlock(blockChunkLoader));
						break;
					case "WarpDrive:cloakBlock":
						mapping.remap(Item.getItemFromBlock(blockCloakingCore));
						break;
					case "WarpDrive:cloakCoilBlock":
						mapping.remap(Item.getItemFromBlock(blockCloakingCoil));
						break;
					case "WarpDrive:component":
						mapping.remap(itemComponent);
						break;
					case "WarpDrive:decorative":
						mapping.remap(Item.getItemFromBlock(blockDecorative));
						break;
					case "WarpDrive:gasBlock":
						mapping.remap(Item.getItemFromBlock(blockGas));
						break;
					case "WarpDrive:helmet":
					case "WarpDrive:itemHelmet":
						mapping.remap(itemWarpArmor[0]);
						break;
					case "WarpDrive:iridiumBlock":
						mapping.remap(Item.getItemFromBlock(blockIridium));
						break;
					case "WarpDrive:isolationBlock":
						mapping.remap(Item.getItemFromBlock(blockWarpIsolation));
						break;
					case "WarpDrive:laserBlock":
						mapping.remap(Item.getItemFromBlock(blockLaser));
						break;
					case "WarpDrive:laserCamBlock":
						mapping.remap(Item.getItemFromBlock(blockLaserCamera));
						break;
					case "WarpDrive:laserTreeFarmBlock":
						mapping.remap(Item.getItemFromBlock(blockLaserTreeFarm));
						break;
					case "WarpDrive:liftBlock":
						mapping.remap(Item.getItemFromBlock(blockLift));
						break;
					case "WarpDrive:miningLaserBlock":
						mapping.remap(Item.getItemFromBlock(blockMiningLaser));
						break;
					case "WarpDrive:monitorBlock":
						mapping.remap(Item.getItemFromBlock(blockMonitor));
						break;
					case "WarpDrive:powerLaser":
						mapping.remap(Item.getItemFromBlock(blockEnanReactorLaser));
						break;
					case "WarpDrive:powerReactor":
						mapping.remap(Item.getItemFromBlock(blockEnanReactorCore));
						break;
					case "WarpDrive:powerStore":
						mapping.remap(Item.getItemFromBlock(blockEnergyBank));
						break;
					case "WarpDrive:protocolBlock":
						mapping.remap(Item.getItemFromBlock(blockShipController));
						break;
					case "WarpDrive:radarBlock":
						mapping.remap(Item.getItemFromBlock(blockRadar));
						break;
					case "WarpDrive:reactorLaserFocus":
						mapping.remap(itemIC2reactorLaserFocus);
						break;
					case "WarpDrive:reactorMonitor":
						mapping.remap(Item.getItemFromBlock(blockIC2reactorLaserMonitor));
						break;
					case "WarpDrive:scannerBlock":
						mapping.remap(Item.getItemFromBlock(blockShipScanner));
						break;
					case "WarpDrive:transportBeacon":
						mapping.remap(Item.getItemFromBlock(blockTransportBeacon));
						break;
					case "WarpDrive:transporter":
						mapping.remap(Item.getItemFromBlock(blockTransporter));
						break;
					case "WarpDrive:upgrade":
						mapping.remap(itemUpgrade);
						break;
					case "WarpDrive:warpCore":
						mapping.remap(Item.getItemFromBlock(blockShipCore));
						break;
					case "WarpDrive:itemTuningRod":
						mapping.remap(itemTuningFork);
						break;
				}
				
			} else if (mapping.type == GameRegistry.Type.BLOCK) {
				switch (mapping.name) {
					case "WarpDrive:airBlock":
						mapping.remap(blockAir);
						break;
					case "WarpDrive:airgenBlock":
						mapping.remap(blockAirGenerator);
						break;
					case "WarpDrive:blockHAMachine":
						mapping.remap(blockHighlyAdvancedMachine);
						break;
					case "WarpDrive:boosterBlock":
						mapping.remap(blockLaserMedium);
						break;
					case "WarpDrive:cameraBlock":
						mapping.remap(blockCamera);
						break;
					case "WarpDrive:chunkLoader":
						mapping.remap(blockChunkLoader);
						break;
					case "WarpDrive:cloakBlock":
						mapping.remap(blockCloakingCore);
						break;
					case "WarpDrive:cloakCoilBlock":
						mapping.remap(blockCloakingCoil);
						break;
					case "WarpDrive:decorative":
						mapping.remap(blockDecorative);
						break;
					case "WarpDrive:gasBlock":
						mapping.remap(blockGas);
						break;
					case "WarpDrive:iridiumBlock":
						mapping.remap(blockIridium);
						break;
					case "WarpDrive:isolationBlock":
						mapping.remap(blockWarpIsolation);
						break;
					case "WarpDrive:laserBlock":
						mapping.remap(blockLaser);
						break;
					case "WarpDrive:laserCamBlock":
						mapping.remap(blockLaserCamera);
						break;
					case "WarpDrive:laserTreeFarmBlock":
						mapping.remap(blockLaserTreeFarm);
						break;
					case "WarpDrive:liftBlock":
						mapping.remap(blockLift);
						break;
					case "WarpDrive:miningLaserBlock":
						mapping.remap(blockMiningLaser);
						break;
					case "WarpDrive:monitorBlock":
						mapping.remap(blockMonitor);
						break;
					case "WarpDrive:powerLaser":
						mapping.remap(blockEnanReactorLaser);
						break;
					case "WarpDrive:powerReactor":
						mapping.remap(blockEnanReactorCore);
						break;
					case "WarpDrive:powerStore":
						mapping.remap(blockEnergyBank);
						break;
					case "WarpDrive:protocolBlock":
						mapping.remap(blockShipController);
						break;
					case "WarpDrive:radarBlock":
						mapping.remap(blockRadar);
						break;
					case "WarpDrive:reactorMonitor":
						mapping.remap(blockIC2reactorLaserMonitor);
						break;
					case "WarpDrive:scannerBlock":
						mapping.remap(blockShipScanner);
						break;
					case "WarpDrive:transportBeacon":
						mapping.remap(blockTransportBeacon);
						break;
					case "WarpDrive:transporter":
						mapping.remap(blockTransporter);
						break;
					case "WarpDrive:warpCore":
						mapping.remap(blockShipCore);
						break;
					case "siren":
						mapping.remap(blockSiren);
						break;
				}
			}
		}
	}
	
	/**
	 * Register a Block with the default ItemBlock class.
	 */
	public static <BLOCK extends Block> BLOCK register(final BLOCK block) {
		if (block instanceof IBlockBase) {
			return register(block, ((IBlockBase) block).createItemBlock());
		} else {
			return register(block, new ItemBlock(block));
		}
	}
	
	/**
	 * Register a Block with a custom ItemBlock class.
	 */
	public static <BLOCK extends Block> BLOCK register(final BLOCK block, @Nullable final ItemBlock itemBlock) {
		GameRegistry.register(block);
		
		if (itemBlock != null) {
			GameRegistry.register(itemBlock.setRegistryName(block.getRegistryName()));
		}
		
		// blocks.add(block);
		return block;
	}
}
