package cr0s.warpdrive.event;

import java.util.HashMap;

import cr0s.warpdrive.data.VectorI;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBreathingHelmet;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.world.SpaceTeleporter;

public class LivingHandler {
	private final HashMap<Integer, Integer> entity_airBlock;
	private final HashMap<String, Integer> player_airTank;
	private final HashMap<String, Integer> player_cloakTicks;
	
	private static final int CLOAK_CHECK_TIMEOUT_TICKS = 100;
	private static final int AIR_BLOCK_TICKS = 20;
	private static final int AIR_TANK_TICKS = 300;
	private static final int AIR_DROWN_TICKS = 20;
	private static final VectorI[] vAirOffsets = { new VectorI(0, 0, 0), new VectorI(0, 1, 0),
		new VectorI(0, 1, 1), new VectorI(0, 1, -1), new VectorI(1, 1, 0), new VectorI(1, 1, 0),
		new VectorI(0, 0, 1), new VectorI(0, 0, -1), new VectorI(1, 0, 0), new VectorI(1, 0, 0) };
	
	public LivingHandler() {
		entity_airBlock = new HashMap<>();
		player_airTank = new HashMap<>();
		player_cloakTicks = new HashMap<>();
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.entityLiving == null || event.entityLiving.worldObj.isRemote) {
			return;
		}
		
		EntityLivingBase entity = event.entityLiving;
		int x = MathHelper.floor_double(entity.posX);
		int y = MathHelper.floor_double(entity.posY);
		int z = MathHelper.floor_double(entity.posZ);
		
		// Instant kill if entity exceeds world's limit
		if (WarpDriveConfig.G_SPACE_WORLDBORDER_BLOCKS > 0
		  && (Math.abs(x) > WarpDriveConfig.G_SPACE_WORLDBORDER_BLOCKS || Math.abs(z) > WarpDriveConfig.G_SPACE_WORLDBORDER_BLOCKS)) {
			if (entity instanceof EntityPlayerMP) {
				if (((EntityPlayerMP) entity).capabilities.isCreativeMode) {
					return;
				}
			}
			
			entity.attackEntityFrom(DamageSource.outOfWorld, 9000);
			return;
		}
		if (entity instanceof EntityPlayerMP) {
			updatePlayerCloakState(entity);
			
			// skip players in creative
			if (((EntityPlayerMP) entity).capabilities.isCreativeMode) {
				return;
			}
		}
		
		// skip dead or invulnerable entities
		if (entity.isDead || entity.isEntityInvulnerable()) {
			return;
		}
		
		// If entity is in vacuum, check and start consuming air cells
		if ( entity.worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID
		  || entity.worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) {
			// find an air block
			VectorI vAirBlock = null;	
			Block block;
			for (VectorI vOffset : vAirOffsets) {
				VectorI vPosition = new VectorI(x + vOffset.x, y + vOffset.y, z + vOffset.z);
				block = entity.worldObj.getBlock(vPosition.x, vPosition.y, vPosition.z);
				if (block.isAssociatedBlock(WarpDrive.blockAir)) {
					vAirBlock = vPosition;
					break;
				}
			}
			
			boolean notInVacuum = vAirBlock != null;
			Integer air;
			if (notInVacuum) {// In space with air blocks
				air = entity_airBlock.get(entity.getEntityId());
				if (air == null) {
					entity_airBlock.put(entity.getEntityId(), AIR_BLOCK_TICKS);
				} else if (air <= 1) {// time elapsed => consume air block
					entity_airBlock.put(entity.getEntityId(), AIR_BLOCK_TICKS);
					
					int metadata = entity.worldObj.getBlockMetadata(vAirBlock.x, vAirBlock.y, vAirBlock.z);
					if (metadata > 0 && metadata < 15) {
						entity.worldObj.setBlockMetadataWithNotify(vAirBlock.x, vAirBlock.y, vAirBlock.z, metadata - 1, 2);
					}
				} else {
					entity_airBlock.put(entity.getEntityId(), air - 1);
				}
			} else {// In space without air blocks
				// Damage entity if in vacuum without protection
				if (entity instanceof EntityPlayerMP) {
					air = entity_airBlock.get(entity.getEntityId());
					if (air != null && air > 0) {
						entity_airBlock.put(entity.getEntityId(), air - 1);
						return;
					}
					EntityPlayerMP player = (EntityPlayerMP) entity;
					String playerName = player.getCommandSenderName();
					air = player_airTank.get(playerName);
					
					boolean hasHelmet = false;
					ItemStack helmetStack = player.getCurrentArmor(3);
					if (helmetStack != null) {
						Item helmet = helmetStack.getItem();
						if (helmet instanceof IBreathingHelmet) {
							IBreathingHelmet breathHelmet = (IBreathingHelmet) helmet;
							int airTicks = breathHelmet.ticksPerCanDamage();
							if (breathHelmet.canBreath(player)) {
								hasHelmet = true;
								if (air == null) {// new player in space => grace period
									player_airTank.put(playerName, airTicks);
								} else if (air <= 1) {
									if (breathHelmet.removeAir(player)) {
										player_airTank.put(playerName, airTicks);
									} else {
										player_airTank.put(playerName, AIR_DROWN_TICKS);
										player.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
									}
								} else {
									player_airTank.put(playerName, air - 1);
								}
							}
						} else if (Dictionary.ITEMS_BREATHINGIC2.contains(helmet)) {
							hasHelmet = true;
							if (air == null) {// new player in space => grace period
								player_airTank.put(playerName, AIR_TANK_TICKS);
							} else if (air <= 1) {
								if (consumeCompressedAir(player.inventory.mainInventory, player)) {
									player_airTank.put(playerName, AIR_TANK_TICKS);
								} else {
									player_airTank.put(playerName, AIR_DROWN_TICKS);
									entity.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
								}
							} else {
								player_airTank.put(playerName, air - 1);
							}
						}
					}
					
					if (!hasHelmet) {
						if (air == null) {// new player in space => grace period
							player_airTank.put(playerName, AIR_TANK_TICKS);
						} else if (air <= 1) {
							player_airTank.put(playerName, AIR_DROWN_TICKS);
							entity.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
						} else {
							player_airTank.put(playerName, air - 1);
						}
					}
					
					// If player falling down, teleport on earth
					if (entity.posY < -10.0D) {
						player.mcServer.getConfigurationManager().transferPlayerToDimension(player, 0,
								new SpaceTeleporter(DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID), 0, x, 250, z));
						player.setFire(30);
						player.setPositionAndUpdate(entity.posX, 250.0D, entity.posZ);
						player.sendPlayerAbilities();
					}
				} else {// (in space, no air block and not a player)
					entity_airBlock.put(entity.getEntityId(), 0);
					entity.attackEntityFrom(WarpDrive.damageAsphyxia, 2.0F);
				}
			}
		}
	}
	
	private void updatePlayerCloakState(EntityLivingBase entity) {
		try {
			EntityPlayerMP player = (EntityPlayerMP) entity;
			Integer cloakTicks = player_cloakTicks.get(player.getCommandSenderName());
			
			if (cloakTicks == null) {
				player_cloakTicks.put(player.getCommandSenderName(), 0);
				return;
			}
			
			if (cloakTicks >= CLOAK_CHECK_TIMEOUT_TICKS) {
				player_cloakTicks.put(player.getCommandSenderName(), 0);
				
				WarpDrive.cloaks.updatePlayer(player);
			} else {
				player_cloakTicks.put(player.getCommandSenderName(), cloakTicks + 1);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	private static boolean consumeCompressedAir(ItemStack[] inventory, EntityPlayerMP entityPlayer) {
		for (int j = 0; j < inventory.length; ++j) {
			if (inventory[j] != null && inventory[j].isItemEqual(WarpDriveConfig.IC2_compressedAir)) {
				inventory[j].stackSize--;
				if (inventory[j].stackSize <= 0) {
					inventory[j] = null;
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
		return false;
	}
	
	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event) {
		EntityLivingBase entity = event.entityLiving;
		float distance = event.distance;
		
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			int check = MathHelper.ceiling_float_int(distance - 3.0F);
			
			if (check > 0) {
				for (int i = 0; i < 4; i++) {
					ItemStack armor = player.getCurrentArmor(i);
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
