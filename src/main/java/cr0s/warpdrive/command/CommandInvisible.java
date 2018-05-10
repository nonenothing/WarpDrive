package cr0s.warpdrive.command;

import cr0s.warpdrive.WarpDrive;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandInvisible extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public String getCommandName() {
		return "invisible";
	}

	@Override
	public void processCommand(final ICommandSender commandSender, final String[] args) {
		EntityPlayer player = (EntityPlayer) commandSender;

		if (args.length >= 1) {
			WarpDrive.logger.info("/invisible: setting invisible to " + args[0]);
			
			// get an online player by name
			final List<EntityPlayer> onlinePlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			for (final EntityPlayer onlinePlayer : onlinePlayers) {
				if (onlinePlayer.getCommandSenderName().equalsIgnoreCase(args[0])) {
					player = onlinePlayer;
				}
			}
		}

		if (player == null) {
			return;
		}

		// Toggle invisibility
		player.setInvisible(!player.isInvisible());
	}

	@Override
	public String getCommandUsage(final ICommandSender commandSender) {
		return "/invisible [player]";
	}
}
