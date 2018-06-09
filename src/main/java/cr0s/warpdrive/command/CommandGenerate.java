package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.structures.AbstractStructure;
import cr0s.warpdrive.config.structures.StructureManager;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.world.JumpgateGenerator;
import cr0s.warpdrive.world.WorldGenSmallShip;
import cr0s.warpdrive.world.WorldGenStation;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.Nonnull;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

@MethodsReturnNonnullByDefault
public class CommandGenerate extends CommandBase {
	
	@Override
	public String getName() {
		return "generate";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public String getUsage(@Nonnull final ICommandSender commandSender) {
		return "/" + getName() + " <structure>\nPossible structures: moon, ship, asteroid, astfield, gascloud, star <class>, jumpgate <name>";
	}
	
	@Override
	public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) {
		final World world = commandSender.getEntityWorld();
		BlockPos blockPos = commandSender.getPosition();
		
		//noinspection ConstantConditions
		if (world == null || blockPos == null) {
			Commons.addChatMessage(commandSender, new TextComponentString("* generate: unknown world or coordinates, probably an invalid command sender in action here."));
			return;
		}
		
		if (args.length <= 0 || args.length == 3 || args.length > 5) {
			Commons.addChatMessage(commandSender, new TextComponentString(getUsage(commandSender)));
			return;
		}
		
		if (args.length > 3) {
			blockPos = new BlockPos(
			    AdjustAxis(blockPos.getX(), args[args.length - 3]),
			    AdjustAxis(blockPos.getY(), args[args.length - 2]),
			    AdjustAxis(blockPos.getZ(), args[args.length - 1]));
		}
		
		final String structure = args[0];
		
		// Reject command, if player is not in space
		if (!CelestialObjectManager.isInSpace(world, blockPos.getX(), blockPos.getZ()) && (!"ship".equals(structure))) {
			Commons.addChatMessage(commandSender, new TextComponentString("* generate: this structure is only allowed in space!"));
			return;
		}
		
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			final String name = (args.length > 1) ? args[1] : null;
			switch (structure) {
				case "ship":
					WarpDrive.logger.info("/generate: generating NPC ship at " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
					new WorldGenSmallShip(false, true).generate(world, world.rand, blockPos);
					break;
				case "station":
					WarpDrive.logger.info("/generate: generating station at " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
					new WorldGenStation(false).generate(world, world.rand, blockPos);
					break;
				case "asteroid":
					blockPos = new BlockPos(blockPos.getX(), blockPos.getY() - 10, blockPos.getZ());
					generateStructure(commandSender, StructureManager.GROUP_ASTEROIDS, name, world, blockPos);
					break;
				case "astfield":
					generateStructure(commandSender, StructureManager.GROUP_ASTEROIDS_FIELDS, name, world, blockPos);
					break;
				case "gascloud":
					generateStructure(commandSender, StructureManager.GROUP_GAS_CLOUDS, name, world, blockPos);
					break;
				case "moon":
					blockPos = new BlockPos(blockPos.getX(), blockPos.getY() - 16, blockPos.getZ());
					generateStructure(commandSender, StructureManager.GROUP_MOONS, name, world, blockPos);
					break;
				case "star":
					generateStructure(commandSender, StructureManager.GROUP_STARS, name, world, blockPos);
					break;
				case "jumpgate":
					if (args.length != 2) {
						Commons.addChatMessage(commandSender, new TextComponentString("Missing jumpgate name"));
					} else {
						WarpDrive.logger.info("/generate: creating jumpgate at " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
						blockPos = new BlockPos(blockPos.getX(), Math.min(blockPos.getY(), 255 - JumpgateGenerator.GATE_SIZE_HALF - 1), blockPos.getZ());
						
						if (WarpDrive.jumpgates.addGate(args[1], blockPos)) {
							JumpgateGenerator.generate(world, blockPos);
						} else {
							WarpDrive.logger.info("/generate: jumpgate '" + args[1] + "' already exists.");
						}
					}
					break;
				default:
					Commons.addChatMessage(commandSender, new TextComponentString(getUsage(commandSender)));
					break;
			}
		}
	}
	
	private int AdjustAxis(final int axis, final String param) {
		if (param.isEmpty() || param.equals("~")) {
			return axis;
		}
		
		if (param.charAt(0) == '~') {
			return axis + Integer.parseInt(param.substring(1));
		} else {
			return Integer.parseInt(param);
		}
	}
	
	private void generateStructure(final ICommandSender commandSender, final String group, final String name, final World world, final BlockPos blockPos) {
		final AbstractStructure structure = StructureManager.getStructure(world.rand, group, name);
		if (structure == null) {
			Commons.addChatMessage(commandSender, new TextComponentString("Invalid " + group + " '" + name + "', try one of the followings:\n" + StructureManager.getStructureNames(group)));
		} else {
			WarpDrive.logger.info("/generate: Generating " + group + ":" + structure.getName() + " at " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
			structure.generate(world, world.rand, blockPos);
			
			// do a weak attempt to extract player (ideally, it should be delayed after generation, but that's too complicated)
			if (commandSender instanceof EntityPlayerMP) {
				int newY = blockPos.getY() + 1;
				while ( newY < 256
				     && !world.isAirBlock(new BlockPos(blockPos.getX(), newY, blockPos.getZ())) ) {
					newY++;
				}
				final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) commandSender;
				entityPlayerMP.setPosition(entityPlayerMP.posX, newY, entityPlayerMP.posZ);
			}
		}
	}
}
