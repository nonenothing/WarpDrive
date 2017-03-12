package cr0s.warpdrive.event;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import cr0s.warpdrive.api.IAirCanister;
import cr0s.warpdrive.api.IBreathingHelmet;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.data.StarMapRegistry;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.item.ItemEnergyWrapper;
import cr0s.warpdrive.world.SpaceTeleporter;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LivingHandler {
	private HashMap<Integer, Integer> entity_airBlock;
	private HashMap<UUID, Integer> player_airTank;
	private HashMap<UUID, Integer> player_cloakTicks;
	
	private static final int CLOAK_CHECK_TIMEOUT_TICKS = 100;
	
	private static final int BORDER_WARNING_RANGE_BLOCKS_SQUARED = 20 * 20;
	private static final int BORDER_BYPASS_RANGE_BLOCKS_SQUARED = 32 * 32;
	private static final int BORDER_BYPASS_PULL_BACK_BLOCKS = 16;
	private static final int BORDER_BYPASS_DAMAGES_PER_TICK = 9000;
	
	private static final int AIR_BLOCK_TICKS = 20;
	private static final int AIR_TANK_TICKS = 300;
	private static final int AIR_DROWN_TICKS = 20;
	private static final VectorI[] vAirOffsets = { new VectorI(0, 0, 0), new VectorI(0, 1, 0),
		new VectorI(0, 1, 1), new VectorI(0, 1, -1), new VectorI(1, 1, 0), new VectorI(1, 1, 0),
		new VectorI(0, 0, 1), new VectorI(0, 0, -1), new VectorI(1, 0, 0), new VectorI(1, 0, 0) };
	private static final int AIR_ENERGY_FOR_ELECTROLYSE = 2000;
	
	public LivingHandler() {
		entity_airBlock = new HashMap<>();
		player_airTank = new HashMap<>();
		player_cloakTicks = new HashMap<>();
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.getEntityLiving() == null || event.getEntityLiving().worldObj.isRemote) {
			return;
		}
		
		EntityLivingBase entity = event.getEntityLiving();
		final int x = MathHelper.floor_double(entity.posX);
		final int y = MathHelper.floor_double(entity.posY);
		final int z = MathHelper.floor_double(entity.posZ);
		
		// Instant kill if entity exceeds world's limit
		CelestialObject celestialObject = StarMapRegistry.getCelestialObject(entity.worldObj, x, z);
		if (celestialObject == null) {
			// unregistered dimension => exit
			return;
		}
		final double distanceSquared = celestialObject.getSquareDistanceOutsideBorder(entity.worldObj.provider.getDimension(), x, z);
		if (distanceSquared <= 0.0D) {
			// are we close to the border?
			if ( Math.abs(distanceSquared) <= BORDER_WARNING_RANGE_BLOCKS_SQUARED
			  && entity instanceof EntityPlayer
			  && entity.ticksExisted % 40 == 0) {
				Commons.addChatMessage((EntityPlayer) entity,
				new TextComponentString(String.format("§cProximity alert: world border is only %d m away!",
						(int) Math.sqrt(Math.abs(distanceSquared)))));
			}
		} else {
			if (entity instanceof EntityPlayerMP) {
				if (((EntityPlayerMP) entity).capabilities.isCreativeMode) {
					if (entity.ticksExisted % 100 == 0) {
						Commons.addChatMessage((EntityPlayer) entity,
							new TextComponentString(String.format("§cYou're %d m outside the world border...",
								(int) Math.sqrt(Math.abs(distanceSquared)))));
					}
					return;
				}
			}
			
			// pull back the entity
			final double relativeX = entity.posX - celestialObject.dimensionCenterX;
			final double relativeZ = entity.posZ - celestialObject.dimensionCenterZ;
			final double newAbsoluteX = Math.min(Math.abs(relativeX), Math.max(0.0D, celestialObject.borderRadiusX - BORDER_BYPASS_PULL_BACK_BLOCKS));
			final double newAbsoluteZ = Math.min(Math.abs(relativeZ), Math.max(0.0D, celestialObject.borderRadiusZ - BORDER_BYPASS_PULL_BACK_BLOCKS));
			final double newEntityX = celestialObject.dimensionCenterX + Math.signum(relativeX) * newAbsoluteX;
			final double newEntityY = entity.posY + 0.1D;
			final double newEntityZ = celestialObject.dimensionCenterX + Math.signum(relativeZ) * newAbsoluteZ;
			// entity.isAirBorne = true;
			// @TODO: force client refresh of non-player entities
			if (entity instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) entity;
				player.setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
			} else {
				entity.setPosition(newEntityX, newEntityY, newEntityZ);
			}
			
			// spam chat if it's a player
			if (entity instanceof EntityPlayer && !entity.isDead && entity.deathTime <= 0) {
				Commons.addChatMessage(entity,
					new TextComponentString("§4You've reached the world border..."));
			}
			
			// delay damage for 'fast moving' players
			if (distanceSquared < BORDER_BYPASS_RANGE_BLOCKS_SQUARED) {
				// just set on fire
				entity.setFire(1);
			} else {
				// full damage
				entity.attackEntityFrom(DamageSource.outOfWorld, BORDER_BYPASS_DAMAGES_PER_TICK);
				return;
			}
		}
		if (entity instanceof EntityPlayerMP) {
			updatePlayerCloakState(entity);
			
			// skip players in creative
			if (((EntityPlayerMP) entity).capabilities.isCreativeMode) {
				return;
			}
		}
		
		// skip dead or invulnerable entities
		if (entity.isDead || entity.isEntityInvulnerable(WarpDrive.damageAsphyxia)) {
			return;
		}
		
		// If entity is in vacuum, check and start consuming air cells
		if (!celestialObject.hasAtmosphere()) {
			if (updateBreathing(entity, x, y, z)) return;
		}
		
		// If player falling down, teleport on earth
		if (entity.posY < -10.0D) {
			CelestialObject celestialObjectChild = StarMapRegistry.getClosestChildCelestialObject(
				entity.worldObj.provider.getDimension(), x, z);
			// are we actually in orbit?
			if ( celestialObjectChild != null
			  && !celestialObject.isHyperspace()
			  && celestialObjectChild.getSquareDistanceInParent(entity.worldObj.provider.getDimension(), x, z) <= 0.0D ) {
				
				WorldServer worldTarget = DimensionManager.getWorld(celestialObjectChild.dimensionId);
				if (worldTarget != null) {
					final int yTarget = worldTarget.getActualHeight() + 5;
					
					if (entity instanceof EntityPlayerMP) {
						EntityPlayerMP player = (EntityPlayerMP) entity;
						player.mcServer.getPlayerList().transferPlayerToDimension(player, celestialObjectChild.dimensionId,
							new SpaceTeleporter(worldTarget, 0, x, yTarget, z));
						if (celestialObjectChild.hasAtmosphere()) {
							player.setFire(30);
						}
						player.setPositionAndUpdate(entity.posX, yTarget, entity.posZ);
						player.sendPlayerAbilities();
					}
				}
				
			} else if (celestialObject.isHyperspace() || celestialObject.isSpace()) {
				// player is in space or hyperspace, let's roll around
				entity.setPositionAndUpdate(entity.posX, 260.0D, entity.posZ);
			}
		}
	}
	
	private boolean updateBreathing(EntityLivingBase entity, final int x, final int y, final int z) {
		// find an air block
		VectorI vAirBlock = null;
		IBlockState blockState = null;
		for (VectorI vOffset : vAirOffsets) {
			VectorI vPosition = new VectorI(x + vOffset.x, y + vOffset.y, z + vOffset.z);
			blockState = vPosition.getBlockState(entity.worldObj);
			if (blockState.getBlock().isAssociatedBlock(WarpDrive.blockAir)) {
				vAirBlock = vPosition;
				break;
			}
		}
		
		final boolean notInVacuum = vAirBlock != null;
		Integer air;
		if (notInVacuum) {// No atmosphere with air blocks
			air = entity_airBlock.get(entity.getEntityId());
			if (air == null) {
				entity_airBlock.put(entity.getEntityId(), AIR_BLOCK_TICKS);
			} else if (air <= 1) {// time elapsed => consume air block
				entity_airBlock.put(entity.getEntityId(), AIR_BLOCK_TICKS);
				
				int metadata = blockState.getBlock().getMetaFromState(blockState);
				if (metadata > 0 && metadata < 15) {
					entity.worldObj.setBlockState(vAirBlock.getBlockPos(), WarpDrive.blockAir.getStateFromMeta(metadata - 1), 2);
				}
			} else {
				entity_airBlock.put(entity.getEntityId(), air - 1);
			}
			
		} else {// No atmosphere without air blocks
			// Damage entity if in vacuum without protection
			if (entity instanceof EntityPlayerMP) {
				air = entity_airBlock.get(entity.getEntityId());
				if (air != null && air > 0) {
					entity_airBlock.put(entity.getEntityId(), air - 1);
					return true;
				}
				EntityPlayerMP player = (EntityPlayerMP) entity;
				UUID uuidPlayer = player.getUniqueID();
				air = player_airTank.get(uuidPlayer);
				
				boolean hasHelmet = false;
				ItemStack helmetStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				if (helmetStack != null) {
					Item itemHelmet = helmetStack.getItem();
					if (itemHelmet instanceof IBreathingHelmet) {
						IBreathingHelmet breathingHelmet = (IBreathingHelmet) itemHelmet;
						int airTicks = breathingHelmet.ticksPerCanDamage();
						if (breathingHelmet.canBreath(player)) {
							hasHelmet = true;
							if (air == null) {// new player in space => grace period
								player_airTank.put(uuidPlayer, airTicks);
							} else if (air <= 1) {
								if (breathingHelmet.removeAir(player) || consumeAirCanister(player)) {
									player_airTank.put(uuidPlayer, airTicks);
								} else {
									hasHelmet = false;
								}
							} else {
								player_airTank.put(uuidPlayer, air - 1);
							}
						}
					}
					if (Dictionary.ITEMS_BREATHING_HELMET.contains(itemHelmet)) {
						hasHelmet = true;
						if (air == null) {// new player in space => grace period
							player_airTank.put(uuidPlayer, AIR_TANK_TICKS);
						} else if (air <= 1) {
							if (consumeAirCanister(player)) {
								player_airTank.put(uuidPlayer, AIR_TANK_TICKS);
							} else {
								hasHelmet = false;
							}
						} else {
							player_airTank.put(uuidPlayer, air - 1);
						}
					}
				}
				
				if (!hasHelmet) {
					if (air == null) {// new player in space => grace period
						player_airTank.put(uuidPlayer, AIR_TANK_TICKS);
					} else if (air <= 1) {
						player_airTank.put(uuidPlayer, AIR_DROWN_TICKS);
						entity.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
					} else {
						player_airTank.put(uuidPlayer, air - 1);
					}
				}
				
			} else {// (in space, no air block and not a player)
				entity_airBlock.put(entity.getEntityId(), 0);
				entity.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
			}
		}
		return false;
	}
	
	private void updatePlayerCloakState(EntityLivingBase entity) {
		try {
			EntityPlayerMP player = (EntityPlayerMP) entity;
			Integer cloakTicks = player_cloakTicks.get(player.getUniqueID());
			
			if (cloakTicks == null) {
				player_cloakTicks.put(player.getUniqueID(), 0);
				return;
			}
			
			if (cloakTicks >= CLOAK_CHECK_TIMEOUT_TICKS) {
				player_cloakTicks.put(player.getUniqueID(), 0);
				
				WarpDrive.cloaks.updatePlayer(player);
			} else {
				player_cloakTicks.put(player.getUniqueID(), cloakTicks + 1);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	static public boolean consumeAirCanister(Entity entity) {
		if (WarpDriveConfig.LOGGING_BREATHING) {
			WarpDrive.logger.info("Checking inventory for air reserves...");
		}
		if (!(entity instanceof EntityPlayerMP)) {
			return false;
		}
		
		EntityPlayerMP entityPlayer = (EntityPlayerMP) entity;
		ItemStack[] playerInventory = entityPlayer.inventory.mainInventory;
		int slotAirCanisterFound = -1;
		float fillingRatioAirCanisterFound = 0.0F;
		
		// find most consumed air canister with smallest stack
		for (int slotIndex = 0; slotIndex < playerInventory.length; slotIndex++) {
			ItemStack itemStack = playerInventory[slotIndex];
			if (itemStack != null && itemStack.getItem() instanceof IAirCanister) {
				IAirCanister airCanister = (IAirCanister) itemStack.getItem();
				if (airCanister.containsAir(itemStack)) {
					float fillingRatio = 1.0F - itemStack.getItemDamage() / (float)itemStack.getMaxDamage();
					fillingRatio -= itemStack.stackSize / 1000;
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
			if (itemStack != null && itemStack.getItem() instanceof IAirCanister) {
				IAirCanister airCanister = (IAirCanister) itemStack.getItem();
				if (airCanister.containsAir(itemStack)) {
					if (itemStack.stackSize > 1) {// unstack
						itemStack.stackSize--;
						ItemStack toAdd = itemStack.copy();
						toAdd.stackSize = 1;
						toAdd.setItemDamage(itemStack.getItemDamage() + 1); // bypass unbreaking enchantment
						if (itemStack.getItemDamage() >= itemStack.getMaxDamage()) {
							toAdd = airCanister.emptyDrop(itemStack);
						}
						if (!entityPlayer.inventory.addItemStackToInventory(toAdd)) {
							EntityItem entityItem = new EntityItem(entityPlayer.worldObj, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, toAdd);
							entityPlayer.worldObj.spawnEntityInWorld(entityItem);
						}
						entityPlayer.sendContainerToPlayer(entityPlayer.inventoryContainer);
					} else {
						itemStack.setItemDamage(itemStack.getItemDamage() + 1); // bypass unbreaking enchantment
						if (itemStack.getItemDamage() >= itemStack.getMaxDamage()) {
							playerInventory[slotAirCanisterFound] = airCanister.emptyDrop(itemStack);
						}
					}
					return true;
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
					return true;
				}
			}
		}
		
		// all Air canisters empty
		ItemStack itemStackChestplate = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (itemStackChestplate != null) {
			Item itemChestplate = itemStackChestplate.getItem();
			if (itemChestplate == WarpDrive.itemWarpArmor[1]) {
				return electrolyseIceToAir(entity);
			}
		}
		return false;
	}
	
	static private boolean electrolyseIceToAir(Entity entity) {
		if (WarpDriveConfig.LOGGING_BREATHING) {
			WarpDrive.logger.info("Checking inventory for ice electrolysing...");
		}
		if (!(entity instanceof EntityPlayerMP)) {
			return false;
		}
		EntityPlayerMP entityPlayer = (EntityPlayerMP) entity;
		ItemStack[] playerInventory = entityPlayer.inventory.mainInventory;
		int slotIceFound = -1;
		int slotFirstEmptyAirCanisterFound = -1;
		int slotSecondEmptyAirCanisterFound = -1;
		int slotEnergyContainer = -1;
		
		// find most consumed air canister with smallest stack
		for (int slotIndex = 0; slotIndex < playerInventory.length; slotIndex++) {
			ItemStack itemStack = playerInventory[slotIndex];
			if (itemStack == null || itemStack.stackSize <= 0) {
				// skip
			} else if (itemStack.getItem() == Item.getItemFromBlock(Blocks.ICE)) {
				slotIceFound = slotIndex;
			} else if (itemStack.stackSize == 1 && itemStack.getItem() instanceof IAirCanister) {
				IAirCanister airCanister = (IAirCanister) itemStack.getItem();
				if (airCanister.canContainAir(itemStack) && !airCanister.containsAir(itemStack)) {
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
				IAirCanister airCanister = (IAirCanister) itemStackAirCanister.getItem();
				playerInventory[slotFirstEmptyAirCanisterFound] = airCanister.fullDrop(itemStackAirCanister);
				
				if (slotSecondEmptyAirCanisterFound >= 0) {
					itemStackAirCanister = playerInventory[slotSecondEmptyAirCanisterFound];
					airCanister = (IAirCanister) itemStackAirCanister.getItem();
					playerInventory[slotSecondEmptyAirCanisterFound] = airCanister.fullDrop(itemStackAirCanister);
				}
				entityPlayer.sendContainerToPlayer(entityPlayer.inventoryContainer);
				return true;
			}
		}
		
		return false;
	}
	
	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event) {
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		float distance = event.getDistance();
		
		if (entityLivingBase instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entityLivingBase;
			int check = MathHelper.ceiling_float_int(distance - 3.0F);
			
			if (check > 0) {
				for (ItemStack armor : player.getArmorInventoryList()) {
					if (armor != null) {
						if (Dictionary.ITEMS_NOFALLDAMAGE.contains(armor.getItem())) {
							event.setCanceled(true); // Don't damage player
						}
					}
				}
			}
		}
	}
}
