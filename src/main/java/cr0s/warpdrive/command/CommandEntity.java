package cr0s.warpdrive.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import cr0s.warpdrive.WarpDrive;

/*
 *   /wentity <radius> <filter> <kill?>
 */

public class CommandEntity extends CommandBase {
	@Override
	public String getCommandName() {
		return "wentity";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender) {
		return "/" + getCommandName() + " <radius> <filter> <kill?>"
			+ "\nradius: - or <= 0 to check all loaded in current world, 1+ blocks around player"
			+ "\nfilter: * to get all, anything else is a case insensitive string"
			+ "\nkill: yes/y/1 to kill, anything else is ignored";
	}
	
	@Override
	public void processCommand(ICommandSender icommandsender, String[] params) {
		EntityPlayerMP player = (EntityPlayerMP) icommandsender;
		if (params.length > 3) {
			WarpDrive.addChatMessage(player, getCommandUsage(icommandsender));
			return;
		}
		
		int radius = 20;
		String filter = "";
		boolean kill = false;
		try {
			if (params.length > 0) {
				String par = params[0].toLowerCase();
				if (par.equals("-") || par.equals("world") || par.equals("global") || par.equals("*")) {
					radius = -1;
				} else {
					radius = Integer.parseInt(par);
				}
			}
			if (params.length > 1) {
				if (!params[1].equalsIgnoreCase("*")) {
					filter = params[1];
				}
			}
			if (params.length > 2) {
				String par = params[2].toLowerCase();
				kill = par.equals("y") || par.equals("yes") || par.equals("1");
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			WarpDrive.addChatMessage(player, getCommandUsage(icommandsender));
			return;
		}
		
		WarpDrive.logger.info("/" + getCommandName() + " " + radius + " '*" + filter + "*' " + kill);
		
		Collection<Object> entities;
		if (radius <= 0) {
			entities = new ArrayList<Object>();
			entities.addAll(player.worldObj.loadedEntityList);
		} else {
			entities = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(
					Math.floor(player.posX    ), Math.floor(player.posY    ), Math.floor(player.posZ    ),
					Math.floor(player.posX + 1), Math.floor(player.posY + 1), Math.floor(player.posZ + 1)).expand(radius, radius, radius));
		}
		HashMap<String, Integer> counts = new HashMap<String, Integer>(entities.size());
		for (Object object : entities) {
			if (object instanceof Entity) {
				String name = object.getClass().getTypeName();
				if (filter.isEmpty() || name.contains(filter)) {
					if (!counts.containsKey(name)) {
						counts.put(name, 1);
					} else {
						counts.put(name, counts.get(name) + 1);
					}
					if (kill && !((Entity) object).invulnerable) {
						((Entity) object).setDead();
					}
				}
			}
		}
		if (counts.isEmpty()) {
			WarpDrive.addChatMessage(player, "No matching entities found in range");
			return;
		}
		WarpDrive.addChatMessage(player, "&6Matching entities within range:");
		if (counts.size() < 10) {
			for (Entry<String, Integer> entry : counts.entrySet()) {
				WarpDrive.addChatMessage(player, entry.getValue() + " x " + entry.getKey());
			}
		} else {
			String message = "";
			for (Entry<String, Integer> entry : counts.entrySet()) {
				if (!message.isEmpty()) {
					message += ", ";
				}
				message += entry.getValue() + " x " + entry.getKey();
			}
			WarpDrive.addChatMessage(player, message);
		}
	}
}