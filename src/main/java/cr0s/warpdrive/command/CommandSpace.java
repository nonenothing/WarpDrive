package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.data.StarMapRegistry;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.world.SpaceTeleporter;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

import mcp.MethodsReturnNonnullByDefault;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;

@MethodsReturnNonnullByDefault
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
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender commandSender, @Nonnull String[] params) throws CommandException {
		if (commandSender == null) { return; } 
		
		// set defaults
		int targetDimensionId = Integer.MAX_VALUE;
		
		List<EntityPlayerMP> entityPlayerMPs = null;
		if (commandSender instanceof EntityPlayerMP) {
			entityPlayerMPs = new ArrayList<>(1);
			entityPlayerMPs.add((EntityPlayerMP) commandSender);
		}
		
		// parse arguments
		//noinspection StatementWithEmptyBody
		if (params.length == 0) {
			// nop
		} else if (params.length == 1) {
			if (params[0].equalsIgnoreCase("help") || params[0].equalsIgnoreCase("?")) {
				Commons.addChatMessage(commandSender,  new TextComponentString(getCommandUsage(commandSender)));
				return;
			}
			List<EntityPlayerMP> entityPlayerMPs_found = getOnlinePlayerByNameOrSelector(server, commandSender, params[0]);
			if (!entityPlayerMPs_found.isEmpty()) {
				entityPlayerMPs = entityPlayerMPs_found;
			} else if (commandSender instanceof EntityPlayer) {
				targetDimensionId = StarMapRegistry.getDimensionId(params[0], (EntityPlayer) commandSender);
			} else {
				Commons.addChatMessage(commandSender, new TextComponentString("/space: player not found '" + params[0] + "'"));
				return;
			}
			
		} else if (params.length == 2) {
			List<EntityPlayerMP> entityPlayerMPs_found = getOnlinePlayerByNameOrSelector(server, commandSender, params[0]);
			if (!entityPlayerMPs_found.isEmpty()) {
				entityPlayerMPs = entityPlayerMPs_found;
			} else {
				Commons.addChatMessage(commandSender, new TextComponentString("/space: player not found '" + params[0] + "'"));
				return;
			}
			targetDimensionId = StarMapRegistry.getDimensionId(params[1], entityPlayerMPs.get(0));
			
		} else {
			Commons.addChatMessage(commandSender, new TextComponentString("/space: too many arguments " + params.length));
			return;
		}
		
		// check player
		if (entityPlayerMPs == null || entityPlayerMPs.isEmpty()) {
			Commons.addChatMessage(commandSender, new TextComponentString("/space: undefined player"));
			return;
		}
		
		for (EntityPlayerMP entityPlayerMP : entityPlayerMPs) {
			// toggle between overworld and space if no dimension was provided
			int newX = MathHelper.floor_double(entityPlayerMP.posX);
			int newY = Math.min(255, Math.max(0, MathHelper.floor_double(entityPlayerMP.posY)));
			int newZ = MathHelper.floor_double(entityPlayerMP.posZ);
			if (targetDimensionId == Integer.MAX_VALUE) {
				CelestialObject celestialObject = StarMapRegistry.getCelestialObject(entityPlayerMP.worldObj.provider.getDimension(), (int) entityPlayerMP.posX, (int) entityPlayerMP.posZ);
				if (celestialObject.isSpace() || celestialObject.isHyperspace()) {
					// in space or hyperspace => move to closest child
					celestialObject = StarMapRegistry.getClosestChildCelestialObject(entityPlayerMP.worldObj.provider.getDimension(), (int) entityPlayerMP.posX, (int) entityPlayerMP.posZ);
					if (celestialObject == null) {
						targetDimensionId = 0;
					} else {
						targetDimensionId = celestialObject.dimensionId;
						VectorI vEntry = celestialObject.getEntryOffset();
						newX += vEntry.x;
						newY += vEntry.y;
						newZ += vEntry.z;
					}
				} else {
					// on a planet => move to space
					celestialObject = StarMapRegistry.getClosestParentCelestialObject(entityPlayerMP.worldObj.provider.getDimension(), (int) entityPlayerMP.posX, (int) entityPlayerMP.posZ);
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
				Commons.addChatMessage(commandSender, new TextComponentString("/space: undefined dimension '" + targetDimensionId + "'"));
				return;
			}
			
			// inform player
			String message = "Teleporting player " + entityPlayerMP.getName() + " to dimension " + targetDimensionId + "..."; // + ":" + targetWorld.getWorldInfo().getWorldName();
			Commons.addChatMessage(commandSender, new TextComponentString(message));
			WarpDrive.logger.info(message);
			if (commandSender != entityPlayerMP) {
				Commons.addChatMessage(entityPlayerMP, new TextComponentString(commandSender.getName() + " is teleporting you to dimension " + targetDimensionId)); // + ":" + targetWorld.getWorldInfo().getWorldName());
			}
			
			// find a good spot
			
			if ( (targetWorld.isAirBlock(new BlockPos(newX, newY - 1, newZ)) && !entityPlayerMP.capabilities.allowFlying)
			  || !targetWorld.isAirBlock(new BlockPos(newX, newY    , newZ))
			  || !targetWorld.isAirBlock(new BlockPos(newX, newY + 1, newZ)) ) {// non solid ground and can't fly, or inside blocks
				newY = targetWorld.getTopSolidOrLiquidBlock(new BlockPos(newX, newY, newZ)).getY() + 1;
				if (newY == 0) {
					newY = 128;
				} else {
					for (int safeY = newY - 3; safeY > Math.max(1, newY - 20); safeY--) {
						if (!targetWorld.isAirBlock(new BlockPos(newX, safeY - 1, newZ))
						  && targetWorld.isAirBlock(new BlockPos(newX, safeY    , newZ))
						  && targetWorld.isAirBlock(new BlockPos(newX, safeY + 1, newZ))) {
							newY = safeY;
							break;
						}
					}
				}
			}
			
			// actual teleportation
			SpaceTeleporter teleporter = new SpaceTeleporter(targetWorld, 0, newX, newY, newZ);
			server.getPlayerList().transferPlayerToDimension(entityPlayerMP, targetDimensionId, teleporter);
			entityPlayerMP.setPositionAndUpdate(newX + 0.5D, newY + 0.2D, newZ + 0.5D);
			entityPlayerMP.sendPlayerAbilities();
		}
	}
	
	@Override
	public String getCommandUsage(@Nonnull ICommandSender commandSender) {
		return "/space (<playerName>) ([overworld|nether|end|theend|space|hyper|hyperspace|<dimensionId>])";
	}
	
	private List<EntityPlayerMP> getOnlinePlayerByNameOrSelector(MinecraftServer server, ICommandSender sender, final String playerNameOrSelector) {
		List<EntityPlayerMP> result = new ArrayList<>();
		List<EntityPlayerMP> onlinePlayers = server.getPlayerList().getPlayerList();
		for (EntityPlayerMP onlinePlayer : onlinePlayers) {
			if (onlinePlayer.getName().equalsIgnoreCase(playerNameOrSelector)) {
				result.add(onlinePlayer);
				return result;
			}
		}
		
		List<EntityPlayerMP> entityPlayerMPs_found = EntitySelector.matchEntities(sender, playerNameOrSelector, EntityPlayerMP.class);
		if (!entityPlayerMPs_found.isEmpty()) {
			result.addAll(entityPlayerMPs_found);
		}
		
		return result;
	}
}
