package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.StarMapRegistry;

import javax.annotation.Nonnull;

import mcp.MethodsReturnNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

@MethodsReturnNonnullByDefault
public class CommandDebug extends CommandBase {
	
	@Override
	public String getName()
	{
		return "wdebug";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public String getUsage(@Nonnull final ICommandSender commandSender)
	{
		return "/" + getName() + " <dimension> <x> <y> <z> <blockId> <Metadata> <action><action>...\n"
				+ "dimension: 0/world, 2/space, 3/hyperspace\n"
				+ "coordinates: x,y,z\n"
				+ "action: I(nvalidate), V(alidate), A(set air), R(emoveEntity), P(setBlock), S(etEntity)";
	}
	
	@Override
	public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) {
		if (args.length <= 6) {
			Commons.addChatMessage(commandSender,  new TextComponentString(getUsage(commandSender)));
			return;
		}
		final int dim, x, y, z, metadata;
		final int block;
		final String actions;
		try {
			dim = StarMapRegistry.getDimensionId(args[0], (EntityPlayer) commandSender);
			x = Integer.parseInt(args[1]);
			y = Integer.parseInt(args[2]);
			z = Integer.parseInt(args[3]);
			block = Integer.parseInt(args[4]);
			metadata = Integer.parseInt(args[5]);
			actions = args[6];
		} catch (final Exception exception) {
			exception.printStackTrace();
			Commons.addChatMessage(commandSender, new TextComponentString(getUsage(commandSender)));
			return;
		}

		WarpDrive.logger.info("/" + getName() + " " + dim + " " + x + "," + y + "," + z + " " + block + ":" + metadata + " " + actions);
		final World world = DimensionManager.getWorld(dim);
		final BlockPos blockPos = new BlockPos(x, y, z);
		final TileEntity tileEntity = world.getTileEntity(blockPos);
			WarpDrive.logger.info("[" + getName() + "] In dimension " + world.getProviderName() + " - " + world.getWorldInfo().getWorldName()
			                     + ", Current block is " + world.getBlockState(blockPos) + ", tile entity is " + ((tileEntity == null) ? "undefined" : "defined"));
		final String side = FMLCommonHandler.instance().getEffectiveSide().isClient() ? "Client" : "Server";

		// I(nvalidate), V(alidate), A(set air), R(emoveEntity), P(setBlock), S(etEntity)
		boolean bReturn;
		for (final char cAction : actions.toUpperCase().toCharArray()) {
			switch (cAction) {
			case 'I':
				WarpDrive.logger.info("[" + getName() + "] " + side + ": invalidating");
				if (tileEntity != null) {
					tileEntity.invalidate();
				}
				break;
			case 'V':
				WarpDrive.logger.info("[" + getName() + "] " + side + ": validating");
				if (tileEntity != null) {
					tileEntity.validate();
				}
				break;
			case 'A':
				WarpDrive.logger.info("[" + getName() + "] " + side + ": setting to Air");
				bReturn = world.setBlockToAir(blockPos);
				WarpDrive.logger.info("[" + getName() + "] " + side + ": returned " + bReturn);
				break;
			case 'R':
				WarpDrive.logger.info("[" + getName() + "] " + side + ": remove entity");
				world.removeTileEntity(blockPos);
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
				WarpDrive.logger.info("[" + getName() + "] " + side + ": set block " + x + ", " + y + ", " + z + " to " + block + ":" + metadata);
				bReturn = world.setBlockState(blockPos, Block.getBlockById(block).getStateFromMeta(metadata), cAction - '0');
				WarpDrive.logger.info("[" + getName() + "] " + side + ": returned " + bReturn);
				break;
			case 'P':
				WarpDrive.logger.info("[" + getName() + "] " + side + ": set block " + x + ", " + y + ", " + z + " to " + block + ":" + metadata);
				bReturn = world.setBlockState(blockPos, Block.getBlockById(block).getStateFromMeta(metadata), 2);
				WarpDrive.logger.info("[" + getName() + "] " + side + ": returned " + bReturn);
				break;
			case 'S':
				WarpDrive.logger.info("[" + getName() + "] " + side + ": set entity");
				world.setTileEntity(blockPos, tileEntity);
				break;
			case 'C':
				WarpDrive.logger.info("[" + getName() + "] " + side + ": update containing block info");
				if (tileEntity != null) {
					tileEntity.updateContainingBlockInfo();
				}
				break;
			default:
				WarpDrive.logger.info("[" + getName() + "] " + side + ": invalid step '" + cAction + "'");
				break;
			}
		}
	}

}