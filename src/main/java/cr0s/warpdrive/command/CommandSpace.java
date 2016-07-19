package cr0s.warpdrive.command;

import java.util.List;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.world.SpaceTeleporter;

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
	public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException {
		if (commandSender == null) { return; } 
		
		// set defaults
		int targetDimensionId = Integer.MAX_VALUE;
		
		EntityPlayerMP player = null;
		if (commandSender instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) commandSender;
		}
		
		// parse arguments
		if (args.length == 0) {
			// nop
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
				WarpDrive.addChatMessage(commandSender, getCommandUsage(commandSender));
				return;
			}
			
			EntityPlayerMP playerFound = getOnlinePlayerByName(server, args[0]);
			if (playerFound != null) {
				player = playerFound;
			} else {
				targetDimensionId = getDimensionId(args[0]);
			}
			
		} else if (args.length == 2) {
			player = getOnlinePlayerByName(server, args[0]);
			targetDimensionId = getDimensionId(args[1]);
			
		} else {
			WarpDrive.addChatMessage(commandSender, "/space: too many arguments " + args.length);
			return;
		}
		
		// check player
		if (player == null) {
			WarpDrive.addChatMessage(commandSender, "/space: undefined player");
			return;
		}
		
		// toggle between overworld and space if no dimension was provided
		if (targetDimensionId == Integer.MAX_VALUE) {
			if (player.worldObj.provider.getDimension() == WarpDriveConfig.G_SPACE_DIMENSION_ID) {
				targetDimensionId = 0;
			} else {
				targetDimensionId = WarpDriveConfig.G_SPACE_DIMENSION_ID;
			}
		}
		
		// get target world
		WorldServer targetWorld = server.worldServerForDimension(targetDimensionId);
		if (targetWorld == null) {
			WarpDrive.addChatMessage(commandSender, "/space: undefined dimension " + targetDimensionId);
			return;
		}
		
		// inform player
		String message = "Teleporting player " + player.getDisplayNameString() + " to dimension " + targetDimensionId + "..."; // + ":" + targetWorld.getWorldInfo().getWorldName();
		WarpDrive.addChatMessage(commandSender, message);
		WarpDrive.logger.info(message);
		if (commandSender != player) {
			WarpDrive.addChatMessage(player, commandSender.getDisplayName().getFormattedText() + " is teleporting you to dimension " + targetDimensionId); // + ":" + targetWorld.getWorldInfo().getWorldName());
		}
		
		// find a good spot
		int newX = MathHelper.floor_double(player.posX);
		int newY = Math.min(255, Math.max(0, MathHelper.floor_double(player.posY)));
		int newZ = MathHelper.floor_double(player.posZ);
		
		if ( (targetWorld.isAirBlock(new BlockPos(newX, newY - 1, newZ)) && !player.capabilities.allowFlying)
		  || !targetWorld.isAirBlock(new BlockPos(newX, newY, newZ))
		  || !targetWorld.isAirBlock(new BlockPos(newX, newY + 1, newZ))) {// non solid ground and can't fly, or inside blocks
			newY = targetWorld.getTopSolidOrLiquidBlock(new BlockPos(newX, newY, newZ)).getY() + 1;
			if (newY == 0) {
				newY = 128;
			}
		}
		
		SpaceTeleporter teleporter = new SpaceTeleporter(targetWorld, 0, newX, newY, newZ);
		server.getPlayerList().transferPlayerToDimension(player, targetDimensionId, teleporter);
		player.setPositionAndUpdate(newX + 0.5D, newY + 0.05D, newZ + 0.5D);
		player.sendPlayerAbilities();
	}
	
	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "/space (<playerName>) ([overworld|nether|end|theend|space|hyper|hyperspace|<dimensionId>])";
	}
	
	private EntityPlayerMP getOnlinePlayerByName(MinecraftServer server, final String playerName) {
		List<EntityPlayerMP> entityPlayers = server.getPlayerList().getPlayerList();
		for (EntityPlayerMP entityPlayer : entityPlayers) {
			if (entityPlayer.getDisplayNameString().equalsIgnoreCase(playerName)) {
				return entityPlayer;
			}
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
