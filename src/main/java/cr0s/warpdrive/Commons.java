package cr0s.warpdrive;

import cr0s.warpdrive.data.VectorI;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Common static methods
 */
public class Commons {
	
	public static void addChatMessage(final ICommandSender sender, final ITextComponent textComponent) {
		if (sender == null) {
			WarpDrive.logger.error("Unable to send message to NULL sender: " + textComponent.getFormattedText());
			return;
		}
		String[] lines = textComponent.getFormattedText().replace("§", "" + (char)167).replace("\\n", "\n").replaceAll("\u00A0", " ").split("\n");
		for (String line : lines) {
			sender.addChatMessage(new TextComponentString(line));
		}
		
		// logger.info(message);
	}
	
	// add tooltip information with text formatting and line splitting
	// will ensure it fits on minimum screen width
	public static void addTooltip(List<String> list, String tooltip) {
		final String charFormatting = "" + (char)167;
		tooltip = tooltip.replace("§", charFormatting).replace("\\n", "\n").replace("|", "\n");
		tooltip = tooltip.replace(charFormatting + "r", charFormatting + "7");
		
		String[] split = tooltip.split("\n");
		for (String line : split) {
			String lineRemaining = line;
			String formatNextLine = "";
			while (!lineRemaining.isEmpty()) {
				int indexToCut = formatNextLine.length();
				int displayLength = 0;
				int length = lineRemaining.length();
				while (indexToCut < length && displayLength <= 38) {
					if (lineRemaining.charAt(indexToCut) == (char)167 && indexToCut + 1 < length) {
						indexToCut++;
					} else {
						displayLength++;
					}
					indexToCut++;
				}
				if (indexToCut < length) {
					indexToCut = lineRemaining.substring(0, indexToCut).lastIndexOf(' ');
					if (indexToCut == -1 || indexToCut == 0) {// no space available, show the whole line 'as is'
						list.add(lineRemaining);
						lineRemaining = "";
					} else {// cut at last space
						list.add(lineRemaining.substring(0, indexToCut).replaceAll("\u00A0", " "));
						
						// compute remaining format
						int index = formatNextLine.length();
						while (index <= indexToCut) {
							if (lineRemaining.charAt(index) == (char)167 && index + 1 < indexToCut) {
								index++;
								formatNextLine += ("" + (char)167) + lineRemaining.charAt(index);
							}
							index++;
						}
						
						// cut for next line, recovering current format
						lineRemaining = formatNextLine + " " + lineRemaining.substring(indexToCut + 1);
					}
				} else {
					list.add(lineRemaining.replaceAll("\u00A0", " "));
					lineRemaining = "";
				}
			}
		}
	}
	
	public static Field getField(Class<?> clazz, String deobfuscatedName, String obfuscatedName) {
		Field fieldToReturn = null;
		
		try {
			fieldToReturn = clazz.getDeclaredField(deobfuscatedName);
		} catch (Exception exception1) {
			try {
				fieldToReturn = clazz.getDeclaredField(obfuscatedName);
			} catch (Exception exception2) {
				exception2.printStackTrace();
				String map = "";
				for(Field fieldDeclared : clazz.getDeclaredFields()) {
					if (!map.isEmpty()) {
						map += ", ";
					}
					map += fieldDeclared.getName();
				}
				WarpDrive.logger.error(String.format("Unable to find %1$s field in %2$s class. Available fields are: %3$s",
						deobfuscatedName, clazz.toString(), map));
			}
		}
		if (fieldToReturn != null) {
			fieldToReturn.setAccessible(true);
		}
		return fieldToReturn;
	}
	
	public static String format(final long value) {
		// alternate: BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_EVEN).toPlainString(),
		return String.format("%,d", Math.round(value));
	}
	
	public static ItemStack copyWithSize(ItemStack itemStack, int newSize) {
		ItemStack ret = itemStack.copy();
		ret.stackSize = newSize;
		return ret;
	}
	
	public static Collection<IInventory> getConnectedInventories(TileEntity tileEntityConnection) {
		Collection<IInventory> result = new ArrayList<>(6);
		
		for(EnumFacing side : EnumFacing.VALUES) {
			TileEntity tileEntity = tileEntityConnection.getWorld().getTileEntity(
			tileEntityConnection.getPos().offset(side));
			if (tileEntity != null && (tileEntity instanceof IInventory)) {
				result.add((IInventory) tileEntity);
				
				if (tileEntity instanceof TileEntityChest) {
					TileEntityChest tileEntityChest = (TileEntityChest) tileEntity;
					tileEntityChest.checkForAdjacentChests();
					if (tileEntityChest.adjacentChestXNeg != null) {
						result.add(tileEntityChest.adjacentChestXNeg);
					} else if (tileEntityChest.adjacentChestXPos != null) {
						result.add(tileEntityChest.adjacentChestXPos);
					} else if (tileEntityChest.adjacentChestZNeg != null) {
						result.add(tileEntityChest.adjacentChestZNeg);
					} else if (tileEntityChest.adjacentChestZPos != null) {
						result.add(tileEntityChest.adjacentChestZPos);
					}
				}
			}
		}
		
		return result;
	}
	
	
	// searching methods
	
	public static final EnumFacing[] UP_DIRECTIONS = { EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
	public static final EnumFacing[] HORIZONTAL_DIRECTIONS = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
	public static Set<BlockPos> getConnectedBlocks(World world, final BlockPos start, final EnumFacing[] directions, final Set<Block> whitelist, final int maxRange, final BlockPos... ignore) {
		return getConnectedBlocks(world, Collections.singletonList(start), directions, whitelist, maxRange, ignore);
	}
	
	public static Set<BlockPos> getConnectedBlocks(World world, final Collection<BlockPos> start, final EnumFacing[] directions, final Set<Block> whitelist, final int maxRange, final BlockPos... ignore) {
		Set<BlockPos> toIgnore = new HashSet<>();
		if (ignore != null) {
			toIgnore.addAll(Arrays.asList(ignore));
		}
		
		Set<BlockPos> toIterate = new HashSet<>();
		toIterate.addAll(start);
		
		Set<BlockPos> toIterateNext;
		
		Set<BlockPos> iterated = new HashSet<>();
		
		int range = 0;
		while(!toIterate.isEmpty() && range < maxRange) {
			toIterateNext = new HashSet<>();
			for (BlockPos current : toIterate) {
				if (whitelist.contains(new VectorI(current).getBlockState_noChunkLoading(world).getBlock())) {
					iterated.add(current);
				}
				
				for(EnumFacing direction : directions) {
					BlockPos next = current.offset(direction);
					if (!iterated.contains(next) && !toIgnore.contains(next) && !toIterate.contains(next) && !toIterateNext.contains(next)) {
						if (whitelist.contains(new VectorI(next).getBlockState_noChunkLoading(world).getBlock())) {
							toIterateNext.add(next);
						}
					}
				}
			}
			toIterate = toIterateNext;
			range++;
		}
		
		return iterated;
	}
	
	// data manipulation methods
	
	public static int toInt(double d) {
		return (int) Math.round(d);
	}
	
	public static int toInt(Object object) {
		return Commons.toInt(toDouble(object));
	}
	
	public static double toDouble(Object object) {
		assert(!(object instanceof Object[]));
		return Double.parseDouble(object.toString());
	}
	
	public static float toFloat(Object object) {
		assert(!(object instanceof Object[]));
		return Float.parseFloat(object.toString());
	}
	
	public static boolean toBool(Object object) {
		if (object == null) {
			 return false;
		}
		assert(!(object instanceof Object[]));
		if (object instanceof Boolean) {
			 return ((Boolean) object);
		}
		String string = object.toString();
		return string.equals("true") || string.equals("1.0") || string.equals("1") || string.equals("y") || string.equals("yes");
	}
	
	public static int clamp(final int min, final int max, final int value) {
		return Math.min(max, Math.max(value, min));
	}
	
	public static float clamp(final float min, final float max, final float value) {
		return Math.min(max, Math.max(value, min));
	}
	
	public static double clamp(final double min, final double max, final double value) {
		return Math.min(max, Math.max(value, min));
	}
	
	
	// configurable interpolation
	
	public static double interpolate(final double[] xValues, final double[] yValues, final double xInput) {
		if (WarpDrive.isDev) {
			assert(xValues.length == yValues.length);
			assert(xValues.length > 1);
		}
		
		// clamp to minimum
		if (xInput < xValues[0]) {
			return yValues[0];
		}
		
		for(int index = 0; index < xValues.length - 1; index++) {
			if (xInput < xValues[index + 1]) {
				return interpolate(xValues[index], yValues[index], xValues[index + 1], yValues[index + 1], xInput);
			}
		}
		
		// clamp to maximum
		return yValues[yValues.length - 1];
	}
	
	private static double interpolate(final double xMin, final double yMin, final double xMax, final double yMax, final double x) {
		return yMin + (x - xMin) * (yMax - yMin) / (xMax - xMin);
	}
}
