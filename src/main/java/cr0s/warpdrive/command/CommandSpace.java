package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.data.StarMapRegistry;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.world.SpaceTeleporter;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;

public class CommandSpace extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public String getCommandName() {
		return "space";
	}
	
	@Override
	public void processCommand(ICommandSender commandSender, String[] params) {
		if (commandSender == null) { return; } 
		MinecraftServer server = MinecraftServer.getServer();
		
		// set defaults
		int targetDimensionId = Integer.MAX_VALUE;
		
		EntityPlayerMP[] entityPlayerMPs = null;
		if (commandSender instanceof EntityPlayerMP) {
			entityPlayerMPs = new EntityPlayerMP[1];
			entityPlayerMPs[0] = (EntityPlayerMP) commandSender;
		}
		
		// parse arguments
		//noinspection StatementWithEmptyBody
		if (params.length == 0) {
			// nop
		} else if (params.length == 1) {
			if (params[0].equalsIgnoreCase("help") || params[0].equalsIgnoreCase("?")) {
				Commons.addChatMessage(commandSender, getCommandUsage(commandSender));
				return;
			}
			EntityPlayerMP[] entityPlayerMPs_found = getOnlinePlayerByNameOrSelector(commandSender, params[0]);
			if (entityPlayerMPs_found != null) {
				entityPlayerMPs = entityPlayerMPs_found;
			} else if (commandSender instanceof EntityPlayer) {
				targetDimensionId = StarMapRegistry.getDimensionId(params[0], (EntityPlayer) commandSender);
			} else {
				Commons.addChatMessage(commandSender, "/space: player not found '" + params[0] + "'");
				return;
			}
			
		} else if (params.length == 2) {
			EntityPlayerMP[] entityPlayerMPs_found = getOnlinePlayerByNameOrSelector(commandSender, params[0]);
			if (entityPlayerMPs_found != null) {
				entityPlayerMPs = entityPlayerMPs_found;
			} else {
				Commons.addChatMessage(commandSender, "/space: player not found '" + params[0] + "'");
				return;
			}
			targetDimensionId = StarMapRegistry.getDimensionId(params[1], entityPlayerMPs[0]);
			
		} else {
			Commons.addChatMessage(commandSender, "/space: too many arguments " + params.length);
			return;
		}
		
		// check player
		if (entityPlayerMPs == null || entityPlayerMPs.length <= 0) {
			Commons.addChatMessage(commandSender, "/space: undefined player");
			return;
		}
		
		for (EntityPlayerMP entityPlayerMP : entityPlayerMPs) {
			// toggle between overworld and space if no dimension was provided
			int newX = MathHelper.floor_double(entityPlayerMP.posX);
			int newY = Math.min(255, Math.max(0, MathHelper.floor_double(entityPlayerMP.posY)));
			int newZ = MathHelper.floor_double(entityPlayerMP.posZ);
			if (targetDimensionId == Integer.MAX_VALUE) {
				CelestialObject celestialObject = StarMapRegistry.getCelestialObject(entityPlayerMP.worldObj.provider.dimensionId, (int) entityPlayerMP.posX, (int) entityPlayerMP.posZ);
				if (celestialObject == null) {
					Commons.addChatMessage(commandSender, 
						String.format("/space: player %s is in unknown dimension %d. Try specifying an explicit target dimension instead.",
							    entityPlayerMP.getCommandSenderName(), entityPlayerMP.worldObj.provider.dimensionId));
					return;
				}
				if (celestialObject.isSpace() || celestialObject.isHyperspace()) {
					// in space or hyperspace => move to closest child
					celestialObject = StarMapRegistry.getClosestChildCelestialObject(entityPlayerMP.worldObj.provider.dimensionId, (int) entityPlayerMP.posX, (int) entityPlayerMP.posZ);
					if (celestialObject == null) {
						targetDimensionId = 0;
					} else if (celestialObject.isVirtual) {
						Commons.addChatMessage(commandSender,
							String.format("/space: player %s closest celestial object is virtual (%s). Try specifying an explicit target dimension instead.",
								entityPlayerMP.getCommandSenderName(), celestialObject.getFullName()));
						return;
					} else {
						targetDimensionId = celestialObject.dimensionId;
						VectorI vEntry = celestialObject.getEntryOffset();
						newX += vEntry.x;
						newY += vEntry.y;
						newZ += vEntry.z;
					}
				} else {
					// on a planet => move to space
					celestialObject = StarMapRegistry.getClosestParentCelestialObject(entityPlayerMP.worldObj.provider.dimensionId, (int) entityPlayerMP.posX, (int) entityPlayerMP.posZ);
					if (celestialObject == null) {
						targetDimensionId = 0;
						
					} else {
						VectorI vEntry = celestialObject.getEntryOffset();
						newX -= vEntry.x;
						newY -= vEntry.y;
						newZ -= vEntry.z;
						if (celestialObject.isSpace()) {
							targetDimensionId = celestialObject.dimensionId;
						} else {
							targetDimensionId = celestialObject.parentDimensionId;
						}
					}
				}
			}
			
			// get target celestial object
			final CelestialObject celestialObject = StarMapRegistry.getCelestialObject(targetDimensionId, newX, newZ);
			
			// force to center if we're outside the border
			if ( celestialObject != null
			  && celestialObject.getSquareDistanceOutsideBorder(targetDimensionId, newX, newZ) > 0 ) {
				// outside 
				newX = celestialObject.dimensionCenterX;
				newZ = celestialObject.dimensionCenterZ;
			}
			
			// get target world
			WorldServer targetWorld = server.worldServerForDimension(targetDimensionId);
			if (targetWorld == null) {
				Commons.addChatMessage(commandSender, "/space: undefined dimension '" + targetDimensionId + "'");
				return;
			}
			
			// inform player
			String message = "Teleporting player " + entityPlayerMP.getCommandSenderName() + " to dimension " + targetDimensionId + "..."; // + ":" + targetWorld.getWorldInfo().getWorldName();
			Commons.addChatMessage(commandSender, message);
			WarpDrive.logger.info(message);
			if (commandSender != entityPlayerMP) {
				Commons.addChatMessage(entityPlayerMP, commandSender.getCommandSenderName() + " is teleporting you to dimension " + targetDimensionId); // + ":" + targetWorld.getWorldInfo().getWorldName());
			}
			
			// find a good spot
			
			if ( (targetWorld.isAirBlock(newX, newY - 1, newZ) && !entityPlayerMP.capabilities.allowFlying)
			  || !targetWorld.isAirBlock(newX, newY, newZ)
			  || !targetWorld.isAirBlock(newX, newY + 1, newZ) ) {// non solid ground and can't fly, or inside blocks
				newY = targetWorld.getTopSolidOrLiquidBlock(newX, newZ) + 1;
				if (newY == 0) {
					newY = 128;
				} else {
					for (int safeY = newY - 3; safeY > Math.max(1, newY - 20); safeY--) {
						if (!targetWorld.isAirBlock(newX, safeY - 1, newZ)
						  && targetWorld.isAirBlock(newX, safeY    , newZ)
						  && targetWorld.isAirBlock(newX, safeY + 1, newZ)) {
							newY = safeY;
							break;
						}
					}
				}
			}
			
			// actual teleportation
			SpaceTeleporter teleporter = new SpaceTeleporter(targetWorld, 0, newX, newY, newZ);
			server.getConfigurationManager().transferPlayerToDimension(entityPlayerMP, targetDimensionId, teleporter);
			entityPlayerMP.setPositionAndUpdate(newX + 0.5D, newY + 0.2D, newZ + 0.5D);
			entityPlayerMP.sendPlayerAbilities();
		}
	}
	
	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/space (<playerName>) ([overworld|nether|end|theend|space|hyper|hyperspace|<dimensionId>])";
	}
	
	private EntityPlayerMP[] getOnlinePlayerByNameOrSelector(ICommandSender sender, final String playerNameOrSelector) {
		@SuppressWarnings("unchecked")
		List<EntityPlayer> onlinePlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayer onlinePlayer : onlinePlayers) {
			if (onlinePlayer.getCommandSenderName().equalsIgnoreCase(playerNameOrSelector) && onlinePlayer instanceof EntityPlayerMP) {
				return new EntityPlayerMP[]{ (EntityPlayerMP)onlinePlayer };
			}
		}
		
		EntityPlayerMP[] entityPlayerMPs_found = PlayerSelector.matchPlayers(sender, playerNameOrSelector);
		if (entityPlayerMPs_found != null && entityPlayerMPs_found.length > 0) {
			return entityPlayerMPs_found.clone();
		}
		
		return null;
	}
}
