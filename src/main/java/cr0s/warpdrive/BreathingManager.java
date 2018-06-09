package cr0s.warpdrive;

import cr0s.warpdrive.api.IAirContainerItem;
import cr0s.warpdrive.api.IBreathingHelmet;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.StateAir;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.event.ChunkHandler;
import cr0s.warpdrive.item.ItemEnergyWrapper;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

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
	
	public static boolean hasAirBlock(final EntityLivingBase entityLivingBase, final int x, final int y, final int z) {
		for (final VectorI vOffset : vAirOffsets) {
			final VectorI vPosition = new VectorI(x + vOffset.x, y + vOffset.y, z + vOffset.z);
			final Block block = vPosition.getBlockState(entityLivingBase.world).getBlock();
			if (isAirBlock(block)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isAirBlock(final Block block) {
		return block == WarpDrive.blockAir || block == WarpDrive.blockAirSource || block == WarpDrive.blockAirFlow;
	}
	
	public static boolean onLivingJoinEvent(final EntityLivingBase entityLivingBase, final int x, final int y, final int z) {
		// skip living entities who don't need air
		final String idEntity = EntityList.getEntityString(entityLivingBase);
		if (Dictionary.ENTITIES_LIVING_WITHOUT_AIR.contains(idEntity)) {
			return true;
		}
		
		if (hasAirBlock(entityLivingBase, x, y, z)) {
			return true;
		}
		if (hasValidSetup(entityLivingBase)) {
			return true;
		}
		
		if (WarpDriveConfig.LOGGING_BREATHING) {
			WarpDrive.logger.warn(String.format("Entity spawn denied @ %s (%d %d %d) entityId '%s'",
			                                    entityLivingBase.world.provider.getSaveFolder(),
			                                    x, y, z, idEntity));
		}
		return false;
	}
	
	public static void onLivingUpdateEvent(final EntityLivingBase entityLivingBase, final int x, final int y, final int z) {
		// skip living entities who don't need air
		final String idEntity = EntityList.getEntityString(entityLivingBase);
		if (Dictionary.ENTITIES_LIVING_WITHOUT_AIR.contains(idEntity)) {
			return;
		}
		
		// find an air block
		final UUID uuidEntity = entityLivingBase.getUniqueID();
		VectorI vAirBlock = null;
		IBlockState blockState = null;
		Block block = null;
		for (final VectorI vOffset : vAirOffsets) {
			final VectorI vPosition = new VectorI(x + vOffset.x, y + vOffset.y, z + vOffset.z);
			blockState = vPosition.getBlockState(entityLivingBase.world);
			block = blockState.getBlock();
			if (block == WarpDrive.blockAir || block == WarpDrive.blockAirSource || block == WarpDrive.blockAirFlow) {
				vAirBlock = vPosition;
				break;
			} else if (block != Blocks.AIR) {
				final StateAir stateAir = ChunkHandler.getStateAir(entityLivingBase.world, vPosition.x, vPosition.y, vPosition.z);
				if ( stateAir == null
				  || stateAir.concentration > 0 ) {
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
					final int metadata = blockState.getBlock().getMetaFromState(blockState);
					if (metadata > 0 && metadata < 15) {
						entityLivingBase.world.setBlockState(vAirBlock.getBlockPos(), WarpDrive.blockAir.getStateFromMeta(metadata - 1), 2);
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
			final boolean hasValidSetup = hasValidSetup(entityLivingBase);
			if (entityLivingBase instanceof EntityPlayerMP) {
				final EntityPlayerMP player = (EntityPlayerMP) entityLivingBase;
				air = player_airTank.get(uuidEntity);
				
				boolean hasHelmet = hasValidSetup;
				if (hasValidSetup) {
					if (air == null) {// new player in space => grace period
						player_airTank.put(uuidEntity, AIR_FIRST_BREATH_TICKS);
					} else if (air <= 1) {
						final int ticksAir = consumeAir(player);
						if (ticksAir > 0) {
							player_airTank.put(uuidEntity, ticksAir);
						} else {
							hasHelmet = false;
						}
					} else {
						player_airTank.put(uuidEntity, air - 1);
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
				if (hasValidSetup) {
					// let it live for now, checking periodically if helmet gets broken in combat
					entity_airBlock.put(uuidEntity, AIR_FIRST_BREATH_TICKS);
				} else {
					entity_airBlock.put(uuidEntity, 0);
					entityLivingBase.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
				}
			}
		}
	}
	
	private static int consumeAir(final EntityLivingBase entityLivingBase) {
		if (WarpDriveConfig.LOGGING_BREATHING) {
			WarpDrive.logger.info("Checking inventory for air reserves...");
		}
		if (!(entityLivingBase instanceof EntityPlayerMP)) {
			return 0;
		}
		
		final EntityPlayerMP entityPlayer = (EntityPlayerMP) entityLivingBase;
		final NonNullList<ItemStack> playerInventory = entityPlayer.inventory.mainInventory;
		int slotAirCanisterFound = -1;
		float fillingRatioAirCanisterFound = 0.0F;
		
		// find most consumed air canister with smallest stack
		for (int slotIndex = 0; slotIndex < playerInventory.size(); slotIndex++) {
			final ItemStack itemStack = playerInventory.get(slotIndex);
			if ( itemStack != ItemStack.EMPTY
			  && itemStack.getCount() > 0
			  && itemStack.getItem() instanceof IAirContainerItem) {
				final IAirContainerItem airContainerItem = (IAirContainerItem) itemStack.getItem();
				final int airAvailable = airContainerItem.getCurrentAirStorage(itemStack);
				if (airAvailable > 0) {
					float fillingRatio = airAvailable / (float) airContainerItem.getMaxAirStorage(itemStack);
					fillingRatio -= itemStack.getCount() / 1000.0F;
					if (fillingRatioAirCanisterFound <= 0.0F || fillingRatio < fillingRatioAirCanisterFound) {
						slotAirCanisterFound = slotIndex;
						fillingRatioAirCanisterFound = fillingRatio;
					}
				}
			}
		}
		// consume air on the selected Air canister
		if (slotAirCanisterFound >= 0) {
			final ItemStack itemStack = playerInventory.get(slotAirCanisterFound);
			if ( !itemStack.isEmpty()
			  && itemStack.getItem() instanceof IAirContainerItem ) {
				final IAirContainerItem airContainerItem = (IAirContainerItem) itemStack.getItem();
				final int airAvailable = airContainerItem.getCurrentAirStorage(itemStack);
				if (airAvailable > 0) {
					if (itemStack.getCount() > 1) {// unstack
						itemStack.shrink(1);
						ItemStack itemStackToAdd = itemStack.copy();
						itemStackToAdd.setCount(1);
						itemStackToAdd = airContainerItem.consumeAir(itemStackToAdd);
						if (!entityPlayer.inventory.addItemStackToInventory(itemStackToAdd)) {
							final EntityItem entityItem = new EntityItem(entityPlayer.world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, itemStackToAdd);
							entityPlayer.world.spawnEntity(entityItem);
						}
						entityPlayer.sendContainerToPlayer(entityPlayer.inventoryContainer);
					} else {
						final ItemStack itemStackNew = airContainerItem.consumeAir(itemStack);
						if (itemStack != itemStackNew) {
							playerInventory.set(slotAirCanisterFound, itemStackNew);
						}
					}
					return airContainerItem.getAirTicksPerConsumption(itemStack);
				}
			}
		}
		
		// (no air canister or all empty)
		// check IC2 compressed air cells
		if (WarpDriveConfig.IC2_compressedAir != null) {
			for (int slotIndex = 0; slotIndex < playerInventory.size(); ++slotIndex) {
				final ItemStack itemStack = playerInventory.get(slotIndex);
				if ( !itemStack.isEmpty()
				  && itemStack.isItemEqual(WarpDriveConfig.IC2_compressedAir) ) {
					itemStack.shrink(1);
					playerInventory.set(slotIndex, itemStack);
					
					if (WarpDriveConfig.IC2_emptyCell != null) {
						final ItemStack emptyCell = new ItemStack(WarpDriveConfig.IC2_emptyCell.getItem(), 1, 0);
						if (!entityPlayer.inventory.addItemStackToInventory(emptyCell)) {
							final World world = entityPlayer.world;
							final EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, emptyCell);
							entityPlayer.world.spawnEntity(entityItem);
						}
						entityPlayer.sendContainerToPlayer(entityPlayer.inventoryContainer);
					}
					return AIR_IC2_COMPRESSED_AIR_TICKS;
				}
			}
		}
		
		// all air containers are empty
		final ItemStack itemStackChestplate = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (!itemStackChestplate.isEmpty()) {
			final Item itemChestplate = itemStackChestplate.getItem();
			if (itemChestplate == WarpDrive.itemWarpArmor[2]) {
				return electrolyseIceToAir(entityLivingBase);
			}
		}
		return 0;
	}
	
	public static boolean hasValidSetup(final EntityLivingBase entityLivingBase) {
		final ItemStack itemStackHelmet = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if (entityLivingBase instanceof EntityPlayer) {
			final ItemStack itemStackChestplate = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			final ItemStack itemStackLeggings = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
			final ItemStack itemStackBoots = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.FEET);
			// need full armor set to breath
			if ( !itemStackHelmet.isEmpty()
			  && !itemStackChestplate.isEmpty()
			  && !itemStackLeggings.isEmpty()
			  && !itemStackBoots.isEmpty() ) {
				// need a working breathing helmet to breath
				final Item itemHelmet = itemStackHelmet.getItem();
				return (itemHelmet instanceof IBreathingHelmet && ((IBreathingHelmet) itemHelmet).canBreath(entityLivingBase))
				    || Dictionary.ITEMS_BREATHING_HELMET.contains(itemHelmet);
			}
			
		} else {
			// need just a working breathing helmet to breath
			if (!itemStackHelmet.isEmpty()) {
				final Item itemHelmet = itemStackHelmet.getItem();
				return (itemHelmet instanceof IBreathingHelmet && ((IBreathingHelmet) itemHelmet).canBreath(entityLivingBase))
				    || Dictionary.ITEMS_BREATHING_HELMET.contains(itemHelmet);
			}
		}
		return false;
	}
	
	public static float getAirReserveRatio(final EntityPlayer entityPlayer) {
		final NonNullList<ItemStack> playerInventory = entityPlayer.inventory.mainInventory;
		
		// check electrolysing
		boolean canElectrolyse = false;
		final ItemStack itemStackChestplate = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (!itemStackChestplate.isEmpty()) {
			final Item itemChestplate = itemStackChestplate.getItem();
			if (itemChestplate == WarpDrive.itemWarpArmor[2]) {
				canElectrolyse = true;
			}
		}
		
		// check all inventory slots for air containers, etc.
		final Item itemIce = Item.getItemFromBlock(Blocks.ICE);
		int sumAirCapacityTicks = 0;
		int sumAirStoredTicks = 0;
		int countAirContainer = 0;
		int countIce = 0;
		int countEnergy = 0;
		ItemStack itemStackAirContainer = null;
		for (final ItemStack itemStack : playerInventory) {
			if (!itemStack.isEmpty()) {
				if (itemStack.getItem() instanceof IAirContainerItem) {
					countAirContainer++;
					itemStackAirContainer = itemStack;
					final IAirContainerItem airContainerItem = (IAirContainerItem) itemStack.getItem();
					final int airAvailable = airContainerItem.getCurrentAirStorage(itemStack);
					if (airAvailable > 0) {
						sumAirStoredTicks += airAvailable * airContainerItem.getAirTicksPerConsumption(itemStack);
					}
					final int airCapacity = airContainerItem.getMaxAirStorage(itemStack);
					sumAirCapacityTicks += airCapacity * airContainerItem.getAirTicksPerConsumption(itemStack);
					
				} else if (WarpDriveConfig.IC2_compressedAir != null && itemStack.isItemEqual(WarpDriveConfig.IC2_compressedAir)) {
					sumAirStoredTicks += AIR_IC2_COMPRESSED_AIR_TICKS;
					sumAirCapacityTicks += AIR_IC2_COMPRESSED_AIR_TICKS;
					
				} else if (WarpDriveConfig.IC2_emptyCell != null && itemStack.isItemEqual(WarpDriveConfig.IC2_emptyCell)) {
					sumAirCapacityTicks += AIR_IC2_COMPRESSED_AIR_TICKS;
					
				} else if (canElectrolyse) {
					if (itemStack.getItem() == itemIce) {
						countIce += itemStack.getCount();
					} else if ( ItemEnergyWrapper.isEnergyContainer(itemStack)
					         && ItemEnergyWrapper.canOutput(itemStack)
					         && ItemEnergyWrapper.getEnergyStored(itemStack) >= AIR_ENERGY_FOR_ELECTROLYSE ) {
						countEnergy += Math.floor(ItemEnergyWrapper.getEnergyStored(itemStack) / AIR_ENERGY_FOR_ELECTROLYSE);
					}
				}
			}
		}
		
		// add electrolyse bonus
		if (countAirContainer >= 1 && countIce > 0 && countEnergy > 0 && itemStackAirContainer.getItem() instanceof IAirContainerItem) {
			final IAirContainerItem airContainerItem = (IAirContainerItem) itemStackAirContainer.getItem();
			final int sumElectrolyseTicks =
					  Math.min(2, countAirContainer)       // up to 2 containers refilled
			        * Math.min(countIce, countEnergy)      // requiring both ice and energy
			        * airContainerItem.getMaxAirStorage(itemStackAirContainer)
			        * airContainerItem.getAirTicksPerConsumption(itemStackAirContainer);
			sumAirStoredTicks += sumElectrolyseTicks;
			sumAirCapacityTicks += sumElectrolyseTicks;
		}
		
		return sumAirCapacityTicks > 0 ? sumAirStoredTicks / (float) sumAirCapacityTicks : 0.0F;
	}
	
	private static int electrolyseIceToAir(final Entity entity) {
		if (WarpDriveConfig.LOGGING_BREATHING) {
			WarpDrive.logger.info("Checking inventory for ice electrolysing...");
		}
		if (!(entity instanceof EntityPlayerMP)) {
			return 0;
		}
		final EntityPlayerMP entityPlayer = (EntityPlayerMP) entity;
		final NonNullList<ItemStack> playerInventory = entityPlayer.inventory.mainInventory;
		int slotIceFound = -1;
		int slotFirstEmptyAirCanisterFound = -1;
		int slotSecondEmptyAirCanisterFound = -1;
		int slotEnergyContainer = -1;
		
		// find 1 ice, 1 energy and 2 empty air containers
		final Item itemIce = Item.getItemFromBlock(Blocks.ICE);
		for (int slotIndex = 0; slotIndex < playerInventory.size(); slotIndex++) {
			final ItemStack itemStack = playerInventory.get(slotIndex);
			if (itemStack.isEmpty()) {
				// skip
			} else if (itemStack.getItem() == itemIce) {
				slotIceFound = slotIndex;
			} else if (itemStack.getCount() == 1 && itemStack.getItem() instanceof IAirContainerItem) {
				final IAirContainerItem airCanister = (IAirContainerItem) itemStack.getItem();
				if (airCanister.canContainAir(itemStack) && airCanister.getCurrentAirStorage(itemStack) >= 0) {
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
			ItemStack itemStackEnergyContainer = playerInventory.get(slotEnergyContainer);
			itemStackEnergyContainer = ItemEnergyWrapper.consume(itemStackEnergyContainer, AIR_ENERGY_FOR_ELECTROLYSE, false);
			if (itemStackEnergyContainer != null) {
				if (playerInventory.get(slotEnergyContainer).getCount() <= 1) {
					playerInventory.set(slotEnergyContainer, itemStackEnergyContainer);
				} else {
					playerInventory.get(slotEnergyContainer).shrink(1);
					if (!entityPlayer.inventory.addItemStackToInventory(itemStackEnergyContainer)) {
						final World world = entityPlayer.world;
						final EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, itemStackEnergyContainer);
						entityPlayer.world.spawnEntity(entityItem);
					}
				}
				
				// consume ice
				final ItemStack itemStackIce = playerInventory.get(slotIceFound);
				itemStackIce.shrink(1);
				playerInventory.set(slotIceFound, itemStackIce);
				
				// fill air canister(s)
				ItemStack itemStackAirCanister = playerInventory.get(slotFirstEmptyAirCanisterFound);
				IAirContainerItem airCanister = (IAirContainerItem) itemStackAirCanister.getItem();
				playerInventory.set(slotFirstEmptyAirCanisterFound, airCanister.getFullAirContainer(itemStackAirCanister));
				
				if (slotSecondEmptyAirCanisterFound >= 0) {
					itemStackAirCanister = playerInventory.get(slotSecondEmptyAirCanisterFound);
					airCanister = (IAirContainerItem) itemStackAirCanister.getItem();
					playerInventory.set(slotSecondEmptyAirCanisterFound, airCanister.getFullAirContainer(itemStackAirCanister));
				}
				entityPlayer.sendContainerToPlayer(entityPlayer.inventoryContainer);
				
				// first air breath is free
				return airCanister.getAirTicksPerConsumption(itemStackAirCanister);
			}
		}
		
		return 0;
	}
}
