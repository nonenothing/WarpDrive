package cr0s.warpdrive.command;

import cr0s.warpdrive.WarpDrive;

import javax.annotation.Nonnull;
import java.util.List;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

@MethodsReturnNonnullByDefault
public class CommandInvisible extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public String getName() {
		return "invisible";
	}
		
	@Override
	public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) {
		EntityPlayer player = commandSender instanceof EntityPlayer ? (EntityPlayer) commandSender : null;
		
		if (args.length >= 1) {
			WarpDrive.logger.info(String.format("/invisible: setting invisible to %s", args[0]));
			
			// get an online player by name
			final List<EntityPlayerMP> entityPlayers = server.getPlayerList().getPlayers();
			for (final EntityPlayerMP entityPlayer : entityPlayers) {
				if ( entityPlayer.getName().equalsIgnoreCase(args[0])
				  || entityPlayer.getDisplayNameString().equalsIgnoreCase(args[0]) ) {
					player = entityPlayer;
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
	public String getUsage(@Nonnull final ICommandSender commandSender) {
		return "/invisible [player]";
	}
}
