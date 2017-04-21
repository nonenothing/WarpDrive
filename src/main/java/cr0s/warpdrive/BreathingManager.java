package cr0s.warpdrive;

import cr0s.warpdrive.api.IAirContainerItem;
import cr0s.warpdrive.api.IBreathingHelmet;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.StateAir;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.event.ChunkHandler;
import cr0s.warpdrive.item.ItemEnergyWrapper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class BreathingManager {
	
	private static final int AIR_BLOCK_TICKS = 20;
	private static final int AIR_DROWN_TICKS = 20;
	private static final int AIR_FIRST_BREATH_TICKS = 300;
	
	private static final int AIR_IC2_COMPRESSED_AIR_TICKS = 300;
	
	private static final int AIR_ENERGY_FOR_ELECTROLYSE = 2000;
	
	private static final VectorI[] vAirOffsets = { new VectorI(0, 0, 0), new VectorI(0, 1, 0),
		new VectorI(0, 1, 1), new VectorI(0, 1, -1), new VectorI(1, 1, 0), new VectorI(1, 1, 0),
		new VectorI(0, 0, 1), new VectorI(0, 0, -1), new VectorI(1, 0, 0), new VectorI(1, 0, 0) };
	
	private static final HashMap<UUID, Integer> entity_airBlock = new HashMap<>();
	private static final HashMap<UUID, Integer> player_airTank = new HashMap<>();
	
	public static boolean hasAirBlock(EntityLivingBase entityLivingBase, final int x, final int y, final int z) {
		Block block;
		for (VectorI vOffset : vAirOffsets) {
			VectorI vPosition = new VectorI(x + vOffset.x, y + vOffset.y, z + vOffset.z);
			block = entityLivingBase.worldObj.getBlock(vPosition.x, vPosition.y, vPosition.z);
			if (block == WarpDrive.blockAir || block == WarpDrive.blockAirSource || block == WarpDrive.blockAirFlow) {
				return true;
			}
		}
		return false;
	}
	
	public static void onLivingUpdateEvent(EntityLivingBase entityLivingBase, final int x, final int y, final int z) {
		// skip living entities who don't need air
		String idEntity = EntityList.getEntityString(entityLivingBase);
		if (Dictionary.ENTITIES_LIVING_WITHOUT_AIR.contains(idEntity)) {
			return;
		}
		
		// find an air block
		UUID uuidEntity = entityLivingBase.getUniqueID();
		VectorI vAirBlock = null;
		Block block = null;
		for (VectorI vOffset : vAirOffsets) {
			VectorI vPosition = new VectorI(x + vOffset.x, y + vOffset.y, z + vOffset.z);
			block = entityLivingBase.worldObj.getBlock(vPosition.x, vPosition.y, vPosition.z);
			if (block == WarpDrive.blockAir || block == WarpDrive.blockAirSource || block == WarpDrive.blockAirFlow) {
				vAirBlock = vPosition;
				break;
			} else if (block != Blocks.air) {
				StateAir stateAir = ChunkHandler.getStateAir(entityLivingBase.worldObj, vPosition.x, vPosition.y, vPosition.z);
				if (stateAir.concentration > 0) {
					vAirBlock = vPosition;
					break;
				}
			}
		}
		
		final boolean notInVacuum = vAirBlock != null;
		Integer air;
		if (notInVacuum) {// no atmosphere with air blocks
			air = entity_airBlock.get(uuidEntity);
			if (air == null) {
				entity_airBlock.put(uuidEntity, AIR_BLOCK_TICKS);
			} else if (air <= 1) {// time elapsed => consume air block
				entity_airBlock.put(uuidEntity, AIR_BLOCK_TICKS);
				
				if (block == WarpDrive.blockAir) {
					final int metadata = entityLivingBase.worldObj.getBlockMetadata(vAirBlock.x, vAirBlock.y, vAirBlock.z);
					if (metadata > 0 && metadata < 15) {
						entityLivingBase.worldObj.setBlockMetadataWithNotify(vAirBlock.x, vAirBlock.y, vAirBlock.z, metadata - 1, 2);
					}
				}
			} else {
				entity_airBlock.put(uuidEntity, air - 1);
			}
			
		} else {// no atmosphere without air blocks
			// finish air from blocks
			air = entity_airBlock.get(uuidEntity);
			if (air != null && air > 0) {
				entity_airBlock.put(uuidEntity, air - 1);
				return;
			} else if (air == null) {
				entity_airBlock.put(uuidEntity, AIR_FIRST_BREATH_TICKS);
				return;
			}
			
			// damage entity if in vacuum without protection
			final ItemStack itemStackHelmet     = entityLivingBase.getEquipmentInSlot(4);
			final ItemStack itemStackChestplate = entityLivingBase.getEquipmentInSlot(3);
			final ItemStack itemStackLeggings   = entityLivingBase.getEquipmentInSlot(2);
			final ItemStack itemStackBoots      = entityLivingBase.getEquipmentInSlot(1);
			if (entityLivingBase instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) entityLivingBase;
				air = player_airTank.get(uuidEntity);
				
				boolean hasHelmet = false;
				// need full armor set to breath
				if ( itemStackHelmet     != null
				  && itemStackChestplate != null
				  && itemStackLeggings   != null
				  && itemStackBoots      != null ) {
					// need a working breathing helmet to breath
					Item itemHelmet = itemStackHelmet.getItem();
					if ( (itemHelmet instanceof IBreathingHelmet && ((IBreathingHelmet) itemHelmet).canBreath(entityLivingBase))
					  || Dictionary.ITEMS_BREATHING_HELMET.contains(itemHelmet) ) {
						hasHelmet = true;
						if (air == null) {// new player in space => grace period
							player_airTank.put(uuidEntity, AIR_FIRST_BREATH_TICKS);
						} else if (air <= 1) {
							int ticksAir = consumeAir(player);
							if (ticksAir > 0) {
								player_airTank.put(uuidEntity, ticksAir);
							} else {
								hasHelmet = false;
							}
						} else {
							player_airTank.put(uuidEntity, air - 1);
						}
					}
				}
				
				if (!hasHelmet) {
					if (air == null) {// new player in space => grace period
						player_airTank.put(uuidEntity, AIR_FIRST_BREATH_TICKS);
					} else if (air <= 1) {
						player_airTank.put(uuidEntity, AIR_DROWN_TICKS);
						entityLivingBase.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
					} else {
						player_airTank.put(uuidEntity, air - 1);
					}
				}
				
			} else {// (in space, no air block and not a player)
				// need just a working breathing helmet to breath
				if (itemStackHelmet != null) {
					final Item itemHelmet = itemStackHelmet.getItem();
					if ( (itemHelmet instanceof IBreathingHelmet && ((IBreathingHelmet) itemHelmet).canBreath(entityLivingBase))
					  || Dictionary.ITEMS_BREATHING_HELMET.contains(itemHelmet) ) {
						// let it live for now, checking periodically if helmet gets broken in combat
						entity_airBlock.put(uuidEntity, AIR_FIRST_BREATH_TICKS);
					} else {
						entity_airBlock.put(uuidEntity, 0);
						entityLivingBase.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
					}
					
				} else {
					entity_airBlock.put(uuidEntity, 0);
					entityLivingBase.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
				}
			}
		}
	}
	
	static private int consumeAir(Entity entity) {
		if (WarpDriveConfig.LOGGING_BREATHING) {
			WarpDrive.logger.info("Checking inventory for air reserves...");
		}
		if (!(entity instanceof EntityPlayerMP)) {
			return 0;
		}
		
		EntityPlayerMP entityPlayer = (EntityPlayerMP) entity;
		ItemStack[] playerInventory = entityPlayer.inventory.mainInventory;
		int slotAirCanisterFound = -1;
		float fillingRatioAirCanisterFound = 0.0F;
		
		// find most consumed air canister with smallest stack
		for (int slotIndex = 0; slotIndex < playerInventory.length; slotIndex++) {
			ItemStack itemStack = playerInventory[slotIndex];
			if (itemStack != null && itemStack.getItem() instanceof IAirContainerItem) {
				IAirContainerItem airContainerItem = (IAirContainerItem) itemStack.getItem();
				final int airAvailable = airContainerItem.getCurrentAirStorage(itemStack);
				if (airAvailable > 0) {
					float fillingRatio = airAvailable / (float) airContainerItem.getMaxAirStorage(itemStack);
					fillingRatio -= itemStack.stackSize / 1000.0F;
					if (fillingRatioAirCanisterFound <= 0.0F || fillingRatio < fillingRatioAirCanisterFound) {
						slotAirCanisterFound = slotIndex;
						fillingRatioAirCanisterFound = fillingRatio;
					}
				}
			}
		}
		// consume air on the selected Air canister
		if (slotAirCanisterFound >= 0) {
			ItemStack itemStack = playerInventory[slotAirCanisterFound];
			if (itemStack != null && itemStack.getItem() instanceof IAirContainerItem) {
				IAirContainerItem airContainerItem = (IAirContainerItem) itemStack.getItem();
				final int airAvailable = airContainerItem.getCurrentAirStorage(itemStack);
				if (airAvailable > 0) {
					if (itemStack.stackSize > 1) {// unstack
						itemStack.stackSize--;
						ItemStack itemStackToAdd = itemStack.copy();
						itemStackToAdd.stackSize = 1;
						itemStackToAdd = airContainerItem.consumeAir(itemStackToAdd);
						if (!entityPlayer.inventory.addItemStackToInventory(itemStackToAdd)) {
							EntityItem entityItem = new EntityItem(entityPlayer.worldObj, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, itemStackToAdd);
							entityPlayer.worldObj.spawnEntityInWorld(entityItem);
						}
						entityPlayer.sendContainerToPlayer(entityPlayer.inventoryContainer);
					} else {
						ItemStack itemStackNew = airContainerItem.consumeAir(itemStack);
						if (itemStack != itemStackNew) {
							playerInventory[slotAirCanisterFound] = itemStack;
						}
					}
					return airContainerItem.airTicksPerConsumption(itemStack);
				}
			}
		}
		
		// (no air canister or all empty)
		// check IC2 compressed air cells
		if (WarpDriveConfig.IC2_compressedAir != null) {
			for (int j = 0; j < playerInventory.length; ++j) {
				if (playerInventory[j] != null && playerInventory[j].isItemEqual(WarpDriveConfig.IC2_compressedAir)) {
					playerInventory[j].stackSize--;
					if (playerInventory[j].stackSize <= 0) {
						playerInventory[j] = null;
					}
					
					if (WarpDriveConfig.IC2_emptyCell != null) {
						ItemStack emptyCell = new ItemStack(WarpDriveConfig.IC2_emptyCell.getItem(), 1, 0);
						if (!entityPlayer.inventory.addItemStackToInventory(emptyCell)) {
							World world = entityPlayer.worldObj;
							EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, emptyCell);
							entityPlayer.worldObj.spawnEntityInWorld(entityItem);
						}
						entityPlayer.sendContainerToPlayer(entityPlayer.inventoryContainer);
					}
					return AIR_IC2_COMPRESSED_AIR_TICKS;
				}
			}
		}
		
		// all Air canisters empty
		ItemStack itemStackChestplate = entityPlayer.getCurrentArmor(2);
		if (itemStackChestplate != null) {
			Item itemChestplate = itemStackChestplate.getItem();
			if (itemChestplate == WarpDrive.itemWarpArmor[1]) {
				return electrolyseIceToAir(entity);
			}
		}
		return 0;
	}
	
	static private int electrolyseIceToAir(Entity entity) {
		if (WarpDriveConfig.LOGGING_BREATHING) {
			WarpDrive.logger.info("Checking inventory for ice electrolysing...");
		}
		if (!(entity instanceof EntityPlayerMP)) {
			return 0;
		}
		EntityPlayerMP entityPlayer = (EntityPlayerMP) entity;
		ItemStack[] playerInventory = entityPlayer.inventory.mainInventory;
		int slotIceFound = -1;
		int slotFirstEmptyAirCanisterFound = -1;
		int slotSecondEmptyAirCanisterFound = -1;
		int slotEnergyContainer = -1;
		
		// find 1 ice, 1 energy and 2 empty air containers
		for (int slotIndex = 0; slotIndex < playerInventory.length; slotIndex++) {
			final ItemStack itemStack = playerInventory[slotIndex];
			if (itemStack == null || itemStack.stackSize <= 0) {
				// skip
			} else if (itemStack.getItem() == Item.getItemFromBlock(Blocks.ice)) {
				slotIceFound = slotIndex;
			} else if (itemStack.stackSize == 1 && itemStack.getItem() instanceof IAirContainerItem) {
				IAirContainerItem airCanister = (IAirContainerItem) itemStack.getItem();
				if (airCanister.canContainAir(itemStack) && airCanister.getCurrentAirStorage(itemStack) > 0) {
					if (slotFirstEmptyAirCanisterFound < 0) {
						slotFirstEmptyAirCanisterFound = slotIndex;
					} else if (slotSecondEmptyAirCanisterFound < 0) {
						slotSecondEmptyAirCanisterFound = slotIndex;
						if (slotIceFound >= 0 && slotEnergyContainer >= 0) {
							break;
						}
					}
				}
			} else if ( slotEnergyContainer < 0
				&& ItemEnergyWrapper.isEnergyContainer(itemStack)
				&& ItemEnergyWrapper.canOutput(itemStack)
				&& ItemEnergyWrapper.getEnergyStored(itemStack) >= AIR_ENERGY_FOR_ELECTROLYSE ) {
				slotEnergyContainer = slotIndex;
			}
		}
		
		if (slotEnergyContainer >= 0 && slotIceFound >= 0 && slotFirstEmptyAirCanisterFound >= 0) {
			// consume energy
			ItemStack itemStackEnergyContainer = playerInventory[slotEnergyContainer];
			itemStackEnergyContainer = ItemEnergyWrapper.consume(itemStackEnergyContainer, AIR_ENERGY_FOR_ELECTROLYSE, false);
			if (itemStackEnergyContainer != null) {
				if (playerInventory[slotEnergyContainer].stackSize <= 1) {
					playerInventory[slotEnergyContainer] = itemStackEnergyContainer;
				} else {
					playerInventory[slotEnergyContainer].stackSize--;
					if (!entityPlayer.inventory.addItemStackToInventory(itemStackEnergyContainer)) {
						World world = entityPlayer.worldObj;
						EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, itemStackEnergyContainer);
						entityPlayer.worldObj.spawnEntityInWorld(entityItem);
					}
				}
				
				// consume ice
				ItemStack itemStackIce = playerInventory[slotIceFound];
				if (itemStackIce.stackSize > 1) {
					itemStackIce.stackSize--;
					playerInventory[slotIceFound] = itemStackIce;
				} else {
					playerInventory[slotIceFound] = null;
				}
				
				// fill air canister(s)
				ItemStack itemStackAirCanister = playerInventory[slotFirstEmptyAirCanisterFound];
				IAirContainerItem airCanister = (IAirContainerItem) itemStackAirCanister.getItem();
				playerInventory[slotFirstEmptyAirCanisterFound] = airCanister.getFullAirContainer(itemStackAirCanister);
				
				if (slotSecondEmptyAirCanisterFound >= 0) {
					itemStackAirCanister = playerInventory[slotSecondEmptyAirCanisterFound];
					airCanister = (IAirContainerItem) itemStackAirCanister.getItem();
					playerInventory[slotSecondEmptyAirCanisterFound] = airCanister.getFullAirContainer(itemStackAirCanister);
				}
				entityPlayer.sendContainerToPlayer(entityPlayer.inventoryContainer);
				
				// first air breath is free
				return airCanister.airTicksPerConsumption(itemStackAirCanister);
			}
		}
		
		return 0;
	}
}
