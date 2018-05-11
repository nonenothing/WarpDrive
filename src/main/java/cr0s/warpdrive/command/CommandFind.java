package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class CommandFind extends CommandBase {
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Nonnull
	@Override
	public String getCommandName() {
		return "wfind";
	}
	
	@Override
	public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) throws CommandException {
		if (commandSender == null) { return; }
		
		// parse arguments
		//noinspection StatementWithEmptyBody
		String nameToken = "";
		if (args.length == 0) {
			Commons.addChatMessage(commandSender, new TextComponentString(getCommandUsage(commandSender)));
			return;
		} else if (args.length == 1) {
			if ( args[0].equalsIgnoreCase("help")
			  || args[0].equalsIgnoreCase("?") ) {
				Commons.addChatMessage(commandSender, new TextComponentString(getCommandUsage(commandSender)));
				return;
			}
			nameToken = args[0];
		} else {
			final StringBuilder nameBuilder = new StringBuilder();
			for (final String param : args) {
				if (nameBuilder.length() > 0) {
					nameBuilder.append(" ");
				}
				nameBuilder.append(param);
			}
			nameToken = nameBuilder.toString();
		}
		
		final String result = WarpDrive.starMap.find(nameToken);
		Commons.addChatMessage(commandSender, new TextComponentString(result));
	}
	
	@Nonnull
	@Override
	public String getCommandUsage(@Nonnull final ICommandSender commandSender) {
		return getCommandName() + " (<shipName>)"
		       + "\nshipName: name of the ship to find. Exact casing is preferred.";
	}
}
