package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandJumpgates extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public String getCommandName() {
		return "jumpgates";
	}

	@Override
	public String getCommandUsage(final ICommandSender commandSender) {
		return "Lists jumpgates";
	}

	@Override
	public void processCommand(final ICommandSender commandSender, final String[] args) {
		Commons.addChatMessage(commandSender, "Jumpgates: " + WarpDrive.jumpgates.commaList());
	}
}
