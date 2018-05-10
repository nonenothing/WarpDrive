package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandReload extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public String getCommandName() {
		return "wreload";
	}
	
	@Override
	public void processCommand(final ICommandSender commandSender, final String[] args) {
		if (commandSender == null) { return; }
		
		WarpDriveConfig.reload();
		Commons.addChatMessage(commandSender, "§aWarpDrive configuration has been reloaded.\n§cUse at your own risk!");
	}
	
	@Override
	public String getCommandUsage(final ICommandSender commandSender) {
		return "/wreload";
	}
}
