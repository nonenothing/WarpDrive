package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import javax.annotation.Nonnull;

import mcp.MethodsReturnNonnullByDefault;

import net.minecraft.command.CommandBase;
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
	public String getName() {
		return "jumpgates";
	}

	@Override
	public String getUsage(@Nonnull final ICommandSender commandSender) {
		return "Lists jumpgates";
	}
	
	@Override
	public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) {
		final EntityPlayerMP player = (EntityPlayerMP) commandSender;
		Commons.addChatMessage(player, new TextComponentString("Jumpgates: " + WarpDrive.jumpgates.commaList()));
	}
}
