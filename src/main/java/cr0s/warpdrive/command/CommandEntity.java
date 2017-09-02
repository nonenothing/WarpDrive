package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import mcp.MethodsReturnNonnullByDefault;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;

@MethodsReturnNonnullByDefault
public class CommandEntity extends CommandBase {
	
	private static final List<String> entitiesNoRemoval = Arrays.asList(
			"item.EntityItemFrame_" 
			);
	private static final List<String> entitiesNoCount = Arrays.asList(
			"item.EntityItemFrame_" 
			);
	
	@Override
	public String getCommandName() {
		return "wentity";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public String getCommandUsage(@Nonnull final ICommandSender commandSender) {
		return "/" + getCommandName() + " <radius> <filter> <kill?>"
			+ "\nradius: - or <= 0 to check all loaded in current world, 1+ blocks around player"
			+ "\nfilter: * to get all, anything else is a case insensitive string"
			+ "\nkill: yes/y/1 to kill, anything else is ignored";
	}
	
	@Override
	public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) throws CommandException {
		if (args.length > 3) {
			Commons.addChatMessage(commandSender, new TextComponentString(getCommandUsage(commandSender)));
			return;
		}
		
		int radius = 20;
		String filter = "";
		boolean kill = false;
		try {
			if (args.length > 0) {
				final String par = args[0].toLowerCase();
				if (par.equals("-") || par.equals("world") || par.equals("global") || par.equals("*")) {
					radius = -1;
				} else {
					radius = Integer.parseInt(par);
				}
			}
			if (args.length > 1) {
				if (!args[1].equalsIgnoreCase("*")) {
					filter = args[1];
				}
			}
			if (args.length > 2) {
				final String par = args[2].toLowerCase();
				kill = par.equals("y") || par.equals("yes") || par.equals("1");
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			Commons.addChatMessage(commandSender, new TextComponentString(getCommandUsage(commandSender)));
			return;
		}

		WarpDrive.logger.info("/" + getCommandName() + " " + radius + " '*" + filter + "*' " + kill);

		List<Entity> entities;
		if (radius <= 0) {
			World world;
			if (commandSender instanceof EntityPlayerMP) {
				world = ((EntityPlayerMP) commandSender).worldObj;
			} else if (radius <= 0) {
				world = DimensionManager.getWorld(0);
			} else {
				Commons.addChatMessage(commandSender, new TextComponentString(getCommandUsage(commandSender)));
				return;
			}
			entities = new ArrayList<>();
			entities.addAll(world.loadedEntityList);
		} else {
			if (!(commandSender instanceof EntityPlayerMP)) {
				Commons.addChatMessage(commandSender, new TextComponentString(getCommandUsage(commandSender)));
				return;
			}
			final EntityPlayerMP entityPlayer = (EntityPlayerMP) commandSender;
			entities = entityPlayer.worldObj.getEntitiesWithinAABBExcludingEntity(entityPlayer, new AxisAlignedBB(
					Math.floor(entityPlayer.posX    ), Math.floor(entityPlayer.posY    ), Math.floor(entityPlayer.posZ    ),
					Math.floor(entityPlayer.posX + 1), Math.floor(entityPlayer.posY + 1), Math.floor(entityPlayer.posZ + 1)).expand(radius, radius, radius));
		}
		final HashMap<String, Integer> counts = new HashMap<>(entities.size());
		int count = 0;
		for (Object object : entities) {
			if (object instanceof Entity) {
				String name = object.getClass().getCanonicalName();
				if (name == null) {
					name = "-null-";
				} else {
					name = name.replaceAll("net\\.minecraft\\.entity\\.", "") + "_";
				}
				if (filter.isEmpty() && !entitiesNoCount.isEmpty()) {
					boolean isCountable = true;
					for (final String entityNoCount : entitiesNoCount) {
						if (name.contains(entityNoCount)) {
							isCountable = false;
							break;
						}
					}
					if (!isCountable) {
						continue;
					}
				}
				if (filter.isEmpty() || name.contains(filter)) {
					// update statistics
					count++;
					if (!counts.containsKey(name)) {
						counts.put(name, 1);
					} else {
						counts.put(name, counts.get(name) + 1);
					}
					if (!filter.isEmpty()) {
						ITextComponent textComponent = new TextComponentString("Found " + object);
						textComponent.getStyle().setColor(TextFormatting.RED);
						Commons.addChatMessage(commandSender, textComponent);
					}
					// remove entity
					if (kill && !((Entity) object).isEntityInvulnerable(WarpDrive.damageAsphyxia)) {
						if (!entitiesNoRemoval.isEmpty()) {
							boolean isRemovable = true;
							for (final String entityNoRemoval : entitiesNoRemoval) {
								if (name.contains(entityNoRemoval)) {
									isRemovable = false;
									break;
								}
							}
							if (!isRemovable) {
								continue;
							}
						}
						((Entity) object).setDead();
					}
				}
			}
		}
		if (count == 0) {
			ITextComponent textComponent = new TextComponentString("No matching entities found within " + radius + " blocks");
			textComponent.getStyle().setColor(TextFormatting.RED);
			Commons.addChatMessage(commandSender, textComponent);
			return;
		}
		
		ITextComponent textComponent = new TextComponentString(count + " matching entities within " + radius + " blocks:");
		textComponent.getStyle().setColor(TextFormatting.GOLD);
		Commons.addChatMessage(commandSender, textComponent);
		if (counts.size() < 10) {
			for (final Entry<String, Integer> entry : counts.entrySet()) {
				textComponent = new TextComponentString(entry.getValue().toString() + "§8x§d" + entry.getKey());
				textComponent.getStyle().setColor(TextFormatting.WHITE);
				Commons.addChatMessage(commandSender, textComponent);
			}
		} else {
			String message = "";
			for (final Entry<String, Integer> entry : counts.entrySet()) {
				if (!message.isEmpty()) {
					message += "§8" + ", ";
				}
				message += "§f" + entry.getValue() + "§8x§d" + entry.getKey();
			}
			Commons.addChatMessage(commandSender, new TextComponentString(message));
		}
	}
}