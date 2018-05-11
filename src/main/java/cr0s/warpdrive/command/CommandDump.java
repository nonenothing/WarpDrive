package cr0s.warpdrive.command;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class CommandDump extends CommandBase {
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Nonnull
	@Override
	public String getCommandName() {
		return "wdump";
	}
	
	@Override
	public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender commandSender, @Nonnull final String[] args) throws CommandException {
		if (commandSender == null) { return; }
		final World world = commandSender.getEntityWorld();
		final BlockPos coordinates = commandSender.getPosition();
		
		if (world == null || coordinates == null) {
			Commons.addChatMessage(commandSender, new TextComponentString("* wdump: unknown world or coordinates, probably an invalid command sender in action here."));
			return;
		}
		int x = coordinates.getX();
		int y = coordinates.getY();
		int z = coordinates.getZ();
		
		// parse arguments
		if (args.length != 0) {
			Commons.addChatMessage(commandSender, new TextComponentString(getCommandUsage(commandSender)));
			return;
		}
		
		// validate
		IInventory inventory = null;
		for (final EnumFacing direction : EnumFacing.values()) {
			inventory = getInventory(world, x + direction.getFrontOffsetX(), y + direction.getFrontOffsetY(), z + direction.getFrontOffsetZ());
			if (inventory != null) {
				x += direction.getFrontOffsetX();
				y += direction.getFrontOffsetY();
				z += direction.getFrontOffsetZ();
				break;
			}
		}
		if (inventory == null) {
			Commons.addChatMessage(commandSender, new TextComponentString("§c/wdump: no container found around player"));
			return;
		}
		
		// actually dump
		WarpDrive.logger.info(String.format("Dumping content from container @ %s (%d %d %d):",
		                                    world.provider.getDimensionType().getName(),
		                                    x, y, z));
		for (int indexSlot = 0; indexSlot < inventory.getSizeInventory(); indexSlot++) {
			final ItemStack itemStack = inventory.getStackInSlot(indexSlot);
			if (itemStack != null) {
				final ResourceLocation uniqueIdentifier = itemStack.getItem().getRegistryName();
				final String stringDamage = itemStack.getItemDamage() == 0 ? "" : String.format(" damage=\"%d\"", itemStack.getItemDamage());
				final String stringNBT = !itemStack.hasTagCompound() ? "" : String.format(" nbt=\"%s\"", itemStack.getTagCompound());
				WarpDrive.logger.info(String.format("Slot %3d is <loot item=\"%s:%s\"%s minQuantity=\"%d\" minQuantity=\"%d\"%s weight=\"1\" /><!-- %s -->",
				                                    indexSlot,
				                                    uniqueIdentifier.getResourceDomain(), uniqueIdentifier.getResourcePath(),
				                                    stringDamage,
				                                    itemStack.stackSize, itemStack.stackSize,
				                                    stringNBT,
				                                    itemStack.getDisplayName()));
			}
		}
	}
	
	@Nonnull
	@Override
	public String getCommandUsage(@Nonnull final ICommandSender icommandsender) {
		return "/wdump: write loot table in console for item container below or next to player";
	}
	
	private IInventory getInventory(final World world, final int x, final int y, final int z) {
		final BlockPos blockPos = new BlockPos(x, y, z);
		final IBlockState blockState = world.getBlockState(blockPos);
		if (blockState.getBlock() instanceof ITileEntityProvider) {
			if (blockState.getBlock().hasTileEntity(blockState)) {
				final TileEntity tileEntity = world.getTileEntity(blockPos);
				if (tileEntity instanceof IInventory) {
					if (((IInventory) tileEntity).getSizeInventory() > 0) {
						return (IInventory) tileEntity;
					}
				}
			}
		}
		return null;
	}
}
