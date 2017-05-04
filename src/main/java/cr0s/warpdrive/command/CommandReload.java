package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.config.WarpDriveConfig;

import javax.annotation.Nonnull;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandReload extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Nonnull
	@Override
	public String getCommandName() {
		return "wreload";
	}
	
	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender commandSender, @Nonnull String[] params) throws CommandException {
		WarpDriveConfig.reload();
		Commons.addChatMessage(commandSender, new TextComponentString("WarpDrive configuration has been reloaded. Use at your own risk!"));
	}
	
	@Nonnull
	@Override
	public String getCommandUsage(@Nonnull ICommandSender commandSender) {
		return "/wreload";
	}
}
