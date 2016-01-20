package cr0s.warpdrive.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.config.structures.AbstractStructure;
import cr0s.warpdrive.config.structures.StructureManager;
import cr0s.warpdrive.world.JumpgateGenerator;
import cr0s.warpdrive.world.WorldGenSmallShip;
import cr0s.warpdrive.world.WorldGenStation;

/*
 *   /generate <structure>
 *   Possible structures:
 *   moon, ship, asteroid, astfield, gascloud, star
 */

public class CommandGenerate extends CommandBase {
	@Override
	public String getCommandName() {
		return "generate";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender) {
		return "/" + getCommandName() + " <structure>\nPossible structures: moon, ship, asteroid, astfield, gascloud, star <class>, jumpgate <name>";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] params) {
		EntityPlayerMP player = (EntityPlayerMP) icommandsender;
		if (params.length > 0) {
			String struct = params[0];

			// Reject command, if player is not in space
			if (player.dimension != WarpDriveConfig.G_SPACE_DIMENSION_ID && (!"ship".equals(struct))) {
				WarpDrive.addChatMessage(player, "* generate: this structure is only allowed in space!");
				return;
			}

			int x = MathHelper.floor_double(player.posX);
			int y = MathHelper.floor_double(player.posY);
			int z = MathHelper.floor_double(player.posZ);

			if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
				if (struct.equals("ship")) {
					WarpDrive.logger.info("/generate: generating NPC ship at " + x + ", " + y + ", " + z);
					new WorldGenSmallShip(false).generate(player.worldObj, player.worldObj.rand, x, y, z);
					
				} else if (struct.equals("station")) {
					WarpDrive.logger.info("/generate: generating station at " + x + ", " + y + ", " + z);
					new WorldGenStation(false).generate(player.worldObj, player.worldObj.rand, x, y, z);
					
				} else if (struct.equals("asteroid")) {
					String name = (params.length > 1) ? params[1] : null;
					generateStructure(player, StructureManager.GROUP_ASTEROIDS, name, x, y - 10, z);
					
				} else if (struct.equals("astfield")) {
					WarpDrive.logger.info("/generate: generating asteroid field at " + x + ", " + y + ", " + z);
					WarpDrive.instance.spaceWorldGenerator.generateAsteroidField(player.worldObj, x, y, z);
					
				} else if (struct.equals("gascloud")) {
					String name = (params.length > 1) ? params[1] : null;
					generateStructure(player, StructureManager.GROUP_GASCLOUDS, name, x, y, z);
					
				} else if (struct.equals("moon")) {
					String name = (params.length > 1) ? params[1] : null;
					generateStructure(player, StructureManager.GROUP_MOONS, name, x, y - 16, z);
					
				} else if (struct.equals("star")) {
					String name = (params.length > 1) ? params[1] : null;
					generateStructure(player, StructureManager.GROUP_STARS, name, x, y, z);
					
				} else if (struct.equals("jumpgate")) {
					if (params.length != 2) {
						WarpDrive.addChatMessage(player, "Missing jumpgate name");
					} else {
						WarpDrive.logger.info("/generate: creating jumpgate at " + x + ", " + y + ", " + z);
						
						if (WarpDrive.jumpgates.addGate(params[1], x, y, z)) {
							JumpgateGenerator.generate(player.worldObj, x, Math.min(y, 255 - JumpgateGenerator.GATE_SIZE_HALF - 1), z);
						} else {
							WarpDrive.logger.info("/generate: jumpgate '" + params[1] + "' already exists.");
						}
					}
				} else {
					WarpDrive.addChatMessage(player, getCommandUsage(icommandsender));
				}
			}
		} else {
			WarpDrive.addChatMessage(player, getCommandUsage(icommandsender));
		}
	}
	
	private void generateStructure(EntityPlayerMP player, final String group, final String name, final int x, final int y, final int z) {
		AbstractStructure structure = StructureManager.getStructure(player.worldObj.rand, group, name);
		if (structure == null) {
			WarpDrive.addChatMessage(player, "Invalid " + group + " '" + name + "', try one of the followings:\n" + StructureManager.getStructureNames(group));
		} else {
			WarpDrive.logger.info("/generate: Generating " + group + ":" + structure.getName() + " at " + x + " " + y + " " + z);
			structure.generate(player.worldObj, player.worldObj.rand, x, y, z);
			
			// do a weak attempt to extract player (ideally, it should be delayed after generation, but that's too complicated)
			int newY = y + 1;
			while (newY < 256 && !player.worldObj.isAirBlock(x, newY, z)) {
				newY++;
			}
			player.setPosition(player.posX, newY, player.posZ);
		}
	}
}
