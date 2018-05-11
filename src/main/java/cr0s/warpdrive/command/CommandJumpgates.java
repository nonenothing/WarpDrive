package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import mcp.MethodsReturnNonnullByDefault;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

@MethodsReturnNonnullByDefault
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
	public void execute(final MinecraftServer server, final ICommandSender commandSender, final String[] args) throws CommandException {
		final EntityPlayerMP player = (EntityPlayerMP) commandSender;
		Commons.addChatMessage(player, new TextComponentString("Jumpgates: " + WarpDrive.jumpgates.commaList()));
	}
}
