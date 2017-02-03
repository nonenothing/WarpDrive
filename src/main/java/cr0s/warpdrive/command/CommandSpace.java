package cr0s.warpdrive.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.world.SpaceTeleporter;

import java.util.List;

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
	public void processCommand(ICommandSender sender, String[] params) {
		if (sender == null) { return; } 
		MinecraftServer server = MinecraftServer.getServer();
		
		// set defaults
		int targetDimensionId = Integer.MAX_VALUE;
		
		EntityPlayerMP[] entityPlayerMPs = null;
		if (sender instanceof EntityPlayerMP) {
			entityPlayerMPs = new EntityPlayerMP[1];
			entityPlayerMPs[0] = (EntityPlayerMP) sender;
		}
		
		// parse arguments
		//noinspection StatementWithEmptyBody
		if (params.length == 0) {
			// nop
		} else if (params.length == 1) {
			if (params[0].equalsIgnoreCase("help") || params[0].equalsIgnoreCase("?")) {
				WarpDrive.addChatMessage(sender, getCommandUsage(sender));
				return;
			}
			EntityPlayerMP[] entityPlayerMPs_found = getOnlinePlayerByNameOrSelector(sender, params[0]);
			if (entityPlayerMPs_found != null) {
				entityPlayerMPs = entityPlayerMPs_found;
			} else if (sender instanceof EntityPlayer) {
				targetDimensionId = getDimensionId(params[0]);
			} else {
				WarpDrive.addChatMessage(sender, "/space: player not found '" + params[0] + "'");
				return;
			}
			
		} else if (params.length == 2) {
			EntityPlayerMP[] entityPlayerMPs_found = getOnlinePlayerByNameOrSelector(sender, params[0]);
			if (entityPlayerMPs_found != null) {
				entityPlayerMPs = entityPlayerMPs_found;
			} else {
				WarpDrive.addChatMessage(sender, "/space: player not found '" + params[0] + "'");
				return;
			}
			targetDimensionId = getDimensionId(params[1]);
			
		} else {
			WarpDrive.addChatMessage(sender, "/space: too many arguments " + params.length);
			return;
		}
		
		// check player
		if (entityPlayerMPs == null || entityPlayerMPs.length <= 0) {
			WarpDrive.addChatMessage(sender, "/space: undefined player");
			return;
		}
		
		for (EntityPlayerMP entityPlayerMP : entityPlayerMPs) {
			// toggle between overworld and space if no dimension was provided
			if (targetDimensionId == Integer.MAX_VALUE) {
				if (WarpDrive.starMap.isInSpace(entityPlayerMP.worldObj)) {
					targetDimensionId = 0;
				} else {
					targetDimensionId = WarpDriveConfig.G_SPACE_DIMENSION_ID;
				}
			}
			
			// get target world
			WorldServer targetWorld = server.worldServerForDimension(targetDimensionId);
			if (targetWorld == null) {
				WarpDrive.addChatMessage(sender, "/space: undefined dimension '" + targetDimensionId + "'");
				return;
			}
			
			// inform player
			String message = "Teleporting player " + entityPlayerMP.getCommandSenderName() + " to dimension " + targetDimensionId + "..."; // + ":" + targetWorld.getWorldInfo().getWorldName();
			WarpDrive.addChatMessage(sender, message);
			WarpDrive.logger.info(message);
			if (sender != entityPlayerMP) {
				WarpDrive.addChatMessage(entityPlayerMP, sender.getCommandSenderName() + " is teleporting you to dimension " + targetDimensionId); // + ":" + targetWorld.getWorldInfo().getWorldName());
			}
			
			// find a good spot
			int newX = MathHelper.floor_double(entityPlayerMP.posX);
			int newY = Math.min(255, Math.max(0, MathHelper.floor_double(entityPlayerMP.posY)));
			int newZ = MathHelper.floor_double(entityPlayerMP.posZ);
			
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
	
	private int getDimensionId(String stringDimension) {
		if (stringDimension.equalsIgnoreCase("overworld")) {
			return 0;
		} else if (stringDimension.equalsIgnoreCase("nether")) {
			return -1;
		} else if (stringDimension.equalsIgnoreCase("end") || stringDimension.equalsIgnoreCase("theend")) {
			return 1;
		} else if (stringDimension.equalsIgnoreCase("space")) {
			return WarpDriveConfig.G_SPACE_DIMENSION_ID;
		} else if (stringDimension.equalsIgnoreCase("hyper") || stringDimension.equalsIgnoreCase("hyperspace")) {
			return WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID;
		}
		try {
			return Integer.parseInt(stringDimension);
		} catch(Exception exception) {
			// exception.printStackTrace();
			WarpDrive.logger.info("/space: invalid dimension '" + stringDimension + "', expecting integer or overworld/nether/end/theend/space/hyper/hyperspace");
		}
		return 0;
	}
}
