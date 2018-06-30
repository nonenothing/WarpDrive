package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class CommandBed extends CommandBase {
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Nonnull
	@Override
	public String getName() {
		return "wbed";
	}
	
	@Override
	public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) {
		if (commandSender == null) { return; }
		
		// parse arguments
		//noinspection StatementWithEmptyBody
		EntityPlayerMP[] entityPlayerMPs = null;
		if (args.length == 0) {
			if (commandSender instanceof EntityPlayerMP) {
				entityPlayerMPs = new EntityPlayerMP[1];
				entityPlayerMPs[0] = (EntityPlayerMP) commandSender;
			} else {
				Commons.addChatMessage(commandSender, new TextComponentString(String.format("§c/%s: use as a player or provide a player name",
				                                                    getName())));
				return;
			}
			
		} else if (args.length == 1) {
			if ( args[0].equalsIgnoreCase("help")
			  || args[0].equalsIgnoreCase("?") ) {
				Commons.addChatMessage(commandSender, new TextComponentString(getUsage(commandSender)));
				return;
			}
			if ( commandSender instanceof EntityPlayerMP
			  && !((EntityPlayerMP) commandSender).capabilities.isCreativeMode ) {
				Commons.addChatMessage(commandSender, new TextComponentString(getUsage(commandSender)));
				return;
			}
			
			final EntityPlayerMP[] entityPlayerMPs_found = Commons.getOnlinePlayerByNameOrSelector(commandSender, args[0]);
			if (entityPlayerMPs_found != null) {
				entityPlayerMPs = entityPlayerMPs_found;
			} else if (commandSender instanceof EntityPlayerMP) {
				entityPlayerMPs = new EntityPlayerMP[1];
				entityPlayerMPs[0] = (EntityPlayerMP) commandSender;
			} else {
				Commons.addChatMessage(commandSender, new TextComponentString(String.format("§c/%s: player not found '%s'",
				                                                    getName(), args[0])));
				return;
			}
		}
		
		assert entityPlayerMPs != null;
		for (final EntityPlayerMP entityPlayerMP : entityPlayerMPs) {
			final BlockPos bedLocation = entityPlayerMP.getBedLocation(entityPlayerMP.world.provider.getDimension());
			if (bedLocation == null) {
				Commons.addChatMessage(entityPlayerMP, new TextComponentString(String.format("§cTeleportation failed:\nyou need to set your bed location in %s",
				                                                                             Commons.format(entityPlayerMP.world))));
				if (args.length != 0) {
					Commons.addChatMessage(commandSender, new TextComponentString(String.format("§cTeleportation failed for player %s:\nplayer needs to set his/her bed location in %s",
					                                                                            entityPlayerMP.getName(),
					                                                                            Commons.format(entityPlayerMP.world))));
				}
				continue;
			}
			
			final Block block = entityPlayerMP.world.getBlockState(bedLocation).getBlock();
			if (!(block instanceof BlockBed)) {
				Commons.addChatMessage(entityPlayerMP, new TextComponentString(String.format("§cTeleportation failed:\nyour bed is no longer there in %s",
				                                                                             Commons.format(entityPlayerMP.world))));
				if (args.length != 0) {
					Commons.addChatMessage(commandSender, new TextComponentString(String.format("§cTeleportation failed for player %s:\nbed is no longer there in %s",
					                                                                            entityPlayerMP.getName(),
					                                                                            Commons.format(entityPlayerMP.world))));
				}
				continue;
			}
			
			entityPlayerMP.setPositionAndUpdate(bedLocation.getX() + 0.5D, bedLocation.getY() + 0.5D, bedLocation.getZ() + 0.5D);
			
			Commons.addChatMessage(entityPlayerMP, new TextComponentString(String.format("Teleporting to %s",
			                                                                             Commons.format(entityPlayerMP.world, bedLocation))));
			if (args.length != 0) {
				Commons.addChatMessage(commandSender, new TextComponentString(String.format("Teleporting player %s to %s",
				                                                                            entityPlayerMP.getName(),
				                                                                            Commons.format(entityPlayerMP.world, bedLocation))));
			}
		}
	}
	
	@Nonnull
	@Override
	public String getUsage(@Nonnull final ICommandSender commandSender) {
		return getName() + " (<playerName>)"
		       + "\nplayerName: name of the player home to find. Exact casing is required.";
	}
}
