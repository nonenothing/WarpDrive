package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;

public class CommandHome extends CommandBase {
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public String getCommandName() {
		return "whome";
	}
	
	@Override
	public void processCommand(ICommandSender commandSender, String[] params) {
		if (commandSender == null) { return; }
		
		// parse arguments
		//noinspection StatementWithEmptyBody
		EntityPlayerMP[] entityPlayerMPs = null;
		if (params.length == 0) {
			if (commandSender instanceof EntityPlayerMP) {
				entityPlayerMPs = new EntityPlayerMP[1];
				entityPlayerMPs[0] = (EntityPlayerMP) commandSender;
			} else {
				Commons.addChatMessage(commandSender, String.format("§c/%s: use as a player or provide a player name",
				                                                    getCommandName()));
				return;
			}
			
		} else if (params.length == 1) {
			if ( params[0].equalsIgnoreCase("help")
			  || params[0].equalsIgnoreCase("?") ) {
				Commons.addChatMessage(commandSender, getCommandUsage(commandSender));
				return;
			}
			if ( commandSender instanceof EntityPlayerMP
			  && !((EntityPlayerMP) commandSender).capabilities.isCreativeMode ) {
				Commons.addChatMessage(commandSender, getCommandUsage(commandSender));
				return;
			}
			
			EntityPlayerMP[] entityPlayerMPs_found = Commons.getOnlinePlayerByNameOrSelector(commandSender, params[0]);
			if (entityPlayerMPs_found != null) {
				entityPlayerMPs = entityPlayerMPs_found;
			} else if (commandSender instanceof EntityPlayerMP) {
				entityPlayerMPs = new EntityPlayerMP[1];
				entityPlayerMPs[0] = (EntityPlayerMP) commandSender;
			} else {
				Commons.addChatMessage(commandSender, String.format("§c/%s: player not found '%s'",
				                                                    getCommandName(), params[0]));
				return;
			}
		}
		
		assert (entityPlayerMPs != null);
		for (final EntityPlayerMP entityPlayerMP : entityPlayerMPs) {
			ChunkCoordinates bedLocation = entityPlayerMP.getBedLocation(entityPlayerMP.worldObj.provider.dimensionId);
			if (bedLocation != null) {
				entityPlayerMP.setPositionAndUpdate(bedLocation.posX + 0.5D, bedLocation.posY + 0.5D, bedLocation.posZ + 0.5D);
				
				Commons.addChatMessage(entityPlayerMP, String.format("Teleporting to (%d %d %d)",
				                                                     bedLocation.posX, bedLocation.posY, bedLocation.posZ));
				if (params.length != 0) {
					Commons.addChatMessage(commandSender, String.format("Teleporting player %s to DIM%d @ (%d %d %d)",
					                                                    entityPlayerMP.getCommandSenderName(),
					                                                    entityPlayerMP.worldObj.provider.dimensionId,
					                                                    bedLocation.posX, bedLocation.posY, bedLocation.posZ));
				}
			} else {
				Commons.addChatMessage(entityPlayerMP, String.format("&cTeleportation failed, no bed location in DIM%d",
				                                                     entityPlayerMP.worldObj.provider.dimensionId));
				if (params.length != 0) {
					Commons.addChatMessage(commandSender, String.format("&cTeleportation failed, no bed location for player %s in DIM%d",
					                                                    entityPlayerMP.getCommandSenderName(),
					                                                    entityPlayerMP.worldObj.provider.dimensionId));
				}
			}
		}
	}
	
	@Override
	public String getCommandUsage(final ICommandSender commandSender) {
		return getCommandName() + " (<playerName>)"
		       + "\nplayerName: name of the player home to find. Exact casing is required.";
	}
}
