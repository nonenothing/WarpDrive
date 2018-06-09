package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.config.WarpDriveConfig;

import javax.annotation.Nonnull;

import net.minecraft.command.CommandBase;
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
	public String getName() {
		return "wreload";
	}
	
	@Override
	public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] params) {
		WarpDriveConfig.reload(server);
		Commons.addChatMessage(commandSender, new TextComponentString("§aWarpDrive configuration has been reloaded.\n§cUse at your own risk!"));
	}
	
	@Nonnull
	@Override
	public String getUsage(@Nonnull final ICommandSender commandSender) {
		return "/wreload";
	}
}
