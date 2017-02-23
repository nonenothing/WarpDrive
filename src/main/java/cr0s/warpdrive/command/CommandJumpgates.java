package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

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
	public String getCommandUsage(ICommandSender commandSender) {
		return "Lists jumpgates";
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] params) {
		EntityPlayerMP player = (EntityPlayerMP) commandSender;
		Commons.addChatMessage(player, "Jumpgates: " + WarpDrive.jumpgates.commaList());
	}
}
