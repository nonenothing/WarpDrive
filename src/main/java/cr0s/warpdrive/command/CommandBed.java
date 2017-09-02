package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
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
	public String getCommandName() {
		return "bed";
	}
	
	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender commandSender, @Nonnull String[] args) throws CommandException {
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
				                                                    getCommandName())));
				return;
			}
			
		} else if (args.length == 1) {
			if ( args[0].equalsIgnoreCase("help")
			  || args[0].equalsIgnoreCase("?") ) {
				Commons.addChatMessage(commandSender, new TextComponentString(getCommandUsage(commandSender)));
				return;
			}
			if ( commandSender instanceof EntityPlayerMP
			  && !((EntityPlayerMP) commandSender).capabilities.isCreativeMode ) {
				Commons.addChatMessage(commandSender, new TextComponentString(getCommandUsage(commandSender)));
				return;
			}
			
			EntityPlayerMP[] entityPlayerMPs_found = Commons.getOnlinePlayerByNameOrSelector(commandSender, args[0]);
			if (entityPlayerMPs_found != null) {
				entityPlayerMPs = entityPlayerMPs_found;
			} else if (commandSender instanceof EntityPlayerMP) {
				entityPlayerMPs = new EntityPlayerMP[1];
				entityPlayerMPs[0] = (EntityPlayerMP) commandSender;
			} else {
				Commons.addChatMessage(commandSender, new TextComponentString(String.format("§c/%s: player not found '%s'",
				                                                    getCommandName(), args[0])));
				return;
			}
		}
		
		assert (entityPlayerMPs != null);
		for (final EntityPlayerMP entityPlayerMP : entityPlayerMPs) {
			final BlockPos bedLocation = entityPlayerMP.getBedLocation(entityPlayerMP.worldObj.provider.getDimension());
			if (bedLocation == null) {
				Commons.addChatMessage(entityPlayerMP, new TextComponentString(String.format("§cTeleportation failed:\nyou need to set your bed location in %s",
				                                                     entityPlayerMP.worldObj.provider.getDimensionType().getName())));
				if (args.length != 0) {
					Commons.addChatMessage(commandSender, new TextComponentString(String.format("§cTeleportation failed for player %s:\nplayer needs to set his/her bed location in %s",
					                                                    entityPlayerMP.getName(),
					                                                    entityPlayerMP.worldObj.provider.getDimensionType().getName())));
				}
				continue;
			}
			
			final Block block = entityPlayerMP.worldObj.getBlockState(bedLocation).getBlock();
			if (!(block instanceof BlockBed)) {
				Commons.addChatMessage(entityPlayerMP, new TextComponentString(String.format("§cTeleportation failed:\nyour bed is no longer there in %s",
				                                                     entityPlayerMP.worldObj.provider.getSaveFolder())));
				if (args.length != 0) {
					Commons.addChatMessage(commandSender, new TextComponentString(String.format("§cTeleportation failed for player %s:\nbed is no longer there in %s",
					                                                    entityPlayerMP.getName(),
					                                                    entityPlayerMP.worldObj.provider.getSaveFolder())));
				}
				continue;
			}
			
			entityPlayerMP.setPositionAndUpdate(bedLocation.getX() + 0.5D, bedLocation.getY() + 0.5D, bedLocation.getZ() + 0.5D);
			
			Commons.addChatMessage(entityPlayerMP, new TextComponentString(String.format("Teleporting to (%d %d %d)",
			                                                     bedLocation.getX(), bedLocation.getY(), bedLocation.getZ())));
			if (args.length != 0) {
				Commons.addChatMessage(commandSender, new TextComponentString(String.format("Teleporting player %s to %s @ (%d %d %d)",
				                                                    entityPlayerMP.getName(),
				                                                    entityPlayerMP.worldObj.provider.getSaveFolder(),
				                                                    bedLocation.getX(), bedLocation.getY(), bedLocation.getZ())));
			}
		}
	}
	
	@Nonnull
	@Override
	public String getCommandUsage(@Nonnull final ICommandSender commandSender) {
		return getCommandName() + " (<playerName>)"
		       + "\nplayerName: name of the player home to find. Exact casing is required.";
	}
}
