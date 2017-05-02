package cr0s.warpdrive.event;

import cr0s.warpdrive.BreathingManager;
import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.data.StarMapRegistry;
import cr0s.warpdrive.data.StateAir;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.world.SpaceTeleporter;

import java.util.HashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class LivingHandler {
	private final HashMap<String, Integer> player_cloakTicks;
	
	private static final int CLOAK_CHECK_TIMEOUT_TICKS = 100;
	
	private static final int BORDER_WARNING_RANGE_BLOCKS_SQUARED = 20 * 20;
	private static final int BORDER_BYPASS_RANGE_BLOCKS_SQUARED = 32 * 32;
	private static final int BORDER_BYPASS_PULL_BACK_BLOCKS = 16;
	private static final int BORDER_BYPASS_DAMAGES_PER_TICK = 9000;
	
	public LivingHandler() {
		player_cloakTicks = new HashMap<>();
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.entityLiving == null || event.entityLiving.worldObj.isRemote) {
			return;
		}
		
		final EntityLivingBase entity = event.entityLiving;
		final int x = MathHelper.floor_double(entity.posX);
		final int y = MathHelper.floor_double(entity.posY);
		final int z = MathHelper.floor_double(entity.posZ);
		
		// *** world border handling
		// Instant kill if entity exceeds world's limit
		final CelestialObject celestialObject = StarMapRegistry.getCelestialObject(entity.worldObj, x, z);
		if (celestialObject == null) {
			// unregistered dimension => exit
			return;
		}
		final double distanceSquared = celestialObject.getSquareDistanceOutsideBorder(entity.worldObj.provider.dimensionId, x, z);
		if (distanceSquared <= 0.0D) {
			// are we close to the border?
			if ( Math.abs(distanceSquared) <= BORDER_WARNING_RANGE_BLOCKS_SQUARED
			  && entity instanceof EntityPlayer
			  && entity.ticksExisted % 40 == 0) {
				Commons.addChatMessage((EntityPlayer) entity,
					String.format("§cProximity alert: world border is only %d m away!",
						(int) Math.sqrt(Math.abs(distanceSquared))));
			}
		} else {
			if (entity instanceof EntityPlayerMP) {
				if (((EntityPlayerMP) entity).capabilities.isCreativeMode) {
					if (entity.ticksExisted % 100 == 0) {
						Commons.addChatMessage((EntityPlayer) entity,
						String.format("§cYou're %d m outside the world border...",
							(int) Math.sqrt(Math.abs(distanceSquared))));
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
				Commons.addChatMessage((EntityPlayer) entity,
					String.format("§4You've reached the world border..."));
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
			// *** cloak handling
			updatePlayerCloakState(entity);
			
			// *** air handling
			if ( WarpDriveConfig.BREATHING_AIR_AT_ENTITY_DEBUG
			  && entity.worldObj.getWorldTime() % 20 == 0) {
				StateAir.dumpAroundEntity((EntityPlayer) entity);
			}
		}
		
		// skip dead or invulnerable entities
		if (entity.isDead || entity.isEntityInvulnerable()) {
			return;
		}
		
		// If entity is in vacuum, check and start consuming air cells
		if (!celestialObject.hasAtmosphere()) {
			// skip players in creative
			if ( !(entity instanceof EntityPlayerMP)
			  || !((EntityPlayerMP) entity).capabilities.isCreativeMode ) {
				BreathingManager.onLivingUpdateEvent(entity, x, y, z);
			}
		}
		
		
		// *** world transition handling
		// If player falling down, teleport to child celestial object
		if (entity.posY < -10.0D) {
			final CelestialObject celestialObjectChild = StarMapRegistry.getClosestChildCelestialObject(
					entity.worldObj.provider.dimensionId, x, z);
			// are we actually in orbit?
			if ( celestialObjectChild != null
			  && !celestialObject.isHyperspace()
			  && celestialObjectChild.getSquareDistanceInParent(entity.worldObj.provider.dimensionId, x, z) <= 0.0D ) {
				
				final WorldServer worldTarget = DimensionManager.getWorld(celestialObjectChild.dimensionId);
				if (worldTarget != null) {
					final VectorI vEntry = celestialObjectChild.getEntryOffset();
					final int xTarget = x + vEntry.x;
					final int yTarget = worldTarget.getActualHeight() + 5;
					final int zTarget = z + vEntry.z;
					
					if (entity instanceof EntityPlayerMP) {
						final EntityPlayerMP player = (EntityPlayerMP) entity;
						
						// add tolerance to fall distance
						player.fallDistance = -5.0F;
						
						// transfer player to new dimension
						player.mcServer.getConfigurationManager().transferPlayerToDimension(
								player,
								celestialObjectChild.dimensionId,
								new SpaceTeleporter(worldTarget, 0, xTarget, yTarget, zTarget) );
						
						// add fire if we're entering an atmosphere
						if (!celestialObject.hasAtmosphere() && celestialObjectChild.hasAtmosphere()) {
							player.setFire(30);
						}
						
						// close player transfer 
						player.setPositionAndUpdate(xTarget + 0.5D, yTarget, zTarget + 0.5D);
						player.sendPlayerAbilities();
					}
				}
				
			} else if (celestialObject.isHyperspace() || celestialObject.isSpace()) {
				// player is in space or hyperspace, let's roll around
				entity.setPositionAndUpdate(entity.posX, 260.0D, entity.posZ);
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
	
	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event) {
		// player in the overworld falling from
		// 3 blocks high is motionY = -0,6517088xxx
		// 4 blocks high is motionY = -0,717
		// 5 blocks high is motionY = -0.844
		// slime in the overworld falling from
		// 2.177279 blocks high is motionY -0.6517000126838683
		// spider in the overworld falling from
		// 3.346 blocks high is motionY -0.717
		
		// cancel in case of very low speed
		final EntityLivingBase entity = event.entityLiving;
		if (entity.motionY > -0.65170D) {
			event.setCanceled(true); // Don't damage entity
			return;
		}
		
		// get vanilla check for fall distance, as found in EntityLivingBase.fall()
		// we're ignoring the jump potion effect bonus
		final float distance = event.distance;
		final int check = MathHelper.ceiling_float_int(distance - 3.0F);
		// ignore small jumps
		if (check <= 0) {
			event.setCanceled(true); // Don't damage entity
			return;
		}
		
		if (WarpDrive.isDev) {
			WarpDrive.logger.warn(String.format("Entity fall damage at motionY %.3f from distance %.3f of %s", entity.motionY, distance, entity));
		}
		
		// check for equipment with NOFALLDAMAGE tag
		for (int i = 1; i < 5; i++) {
			final ItemStack armor = entity.getEquipmentInSlot(i);
			if (armor != null) {
				if (Dictionary.ITEMS_NOFALLDAMAGE.contains(armor.getItem())) {
					event.setCanceled(true); // Don't damage entity
				}
			}
		}
		
		// (entity has significant speed, above minimum distance and there's no absorption)
		// Let it fall
	}
}
