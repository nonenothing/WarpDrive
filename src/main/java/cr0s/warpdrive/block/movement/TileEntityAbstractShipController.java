package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.WarpDriveText;
import cr0s.warpdrive.api.computer.IShipController;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumShipCommand;
import cr0s.warpdrive.data.EnumTier;
import cr0s.warpdrive.data.VectorI;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.common.Optional;

public abstract class TileEntityAbstractShipController extends TileEntityAbstractEnergy implements IShipController {
	
	// persistent properties
	public String shipName = "default";
	private int front, right, up;
	private int back, left, down;
	private boolean isResized = true;
	
	private int moveFront = 0;
	private int moveUp = 0;
	private int moveRight = 0;
	private byte rotationSteps = 0;
	protected EnumShipCommand command = EnumShipCommand.IDLE;
	protected boolean isEnabled = false;
	protected String nameTarget = "";
	
	public TileEntityAbstractShipController(final EnumTier enumTier) {
		super(enumTier);
		
		// (abstract) peripheralName = "xxx";
		addMethods(new String[] {
				"isAssemblyValid",
				"getOrientation",
				"isInSpace",
				"isInHyperspace",
				"shipName",
				"dim_positive",
				"dim_negative",
				"energy",
				"command",
				"enable",
				"getShipSize",
				"getMaxJumpDistance",
				"movement",
				"rotationSteps",
				"targetName",
				"getEnergyRequired",
				});
	}
	
	@Override
	public void update() {
		super.update();
		
		if (world.isRemote) {
			return;
		}
		
		// update bounding box
		if (isResized) {
			if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
				WarpDrive.logger.info(String.format("%s was resized, updating...", this));
			}
			updateAfterResize();
		}
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		
		shipName = tagCompound.getString("shipName");
		isEnabled = tagCompound.hasKey("isEnabled") && tagCompound.getBoolean("isEnabled");
		setCommand(tagCompound.getString("command"));
		
		setFront(tagCompound.getInteger("front"));
		setRight(tagCompound.getInteger("right"));
		setUp   (tagCompound.getInteger("up"));
		setBack (tagCompound.getInteger("back"));
		setLeft (tagCompound.getInteger("left"));
		setDown (tagCompound.getInteger("down"));
		
		setMovement(
				tagCompound.getInteger("moveFront"),
				tagCompound.getInteger("moveUp"),
				tagCompound.getInteger("moveRight") );
		setRotationSteps(tagCompound.getByte("rotationSteps"));
		nameTarget = tagCompound.getString("nameTarget");
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		
		tagCompound.setString("shipName", shipName);
		tagCompound.setBoolean("isEnabled", isEnabled);
		tagCompound.setString("command", command.name());
		
		tagCompound.setInteger("front", getFront());
		tagCompound.setInteger("right", getRight());
		tagCompound.setInteger("up", getUp());
		tagCompound.setInteger("back", getBack());
		tagCompound.setInteger("left", getLeft());
		tagCompound.setInteger("down", getDown());
		
		tagCompound.setInteger("moveFront", moveFront);
		tagCompound.setInteger("moveUp", moveUp);
		tagCompound.setInteger("moveRight", moveRight);
		tagCompound.setByte("rotationSteps", rotationSteps);
		tagCompound.setString("nameTarget", nameTarget);
		return tagCompound;
	}
	
	@Override
	public NBTTagCompound writeItemDropNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeItemDropNBT(tagCompound);
		
		tagCompound.removeTag("isEnabled");
		tagCompound.removeTag("command");
		
		tagCompound.removeTag("front");
		tagCompound.removeTag("right");
		tagCompound.removeTag("up");
		tagCompound.removeTag("back");
		tagCompound.removeTag("left");
		tagCompound.removeTag("down");
		
		tagCompound.removeTag("moveFront");
		tagCompound.removeTag("moveUp");
		tagCompound.removeTag("moveRight");
		tagCompound.removeTag("rotationSteps");
		tagCompound.removeTag("nameTarget");
		return tagCompound;
	}
	
	protected void cooldownDone() {
		sendEvent("shipCoreCooldownDone");
	}
	
	public EnumShipCommand getCommand() {
		return command;
	}
	
	protected void setCommand(final String command) {
		for (final EnumShipCommand enumShipCommand : EnumShipCommand.values()) {
			if (enumShipCommand.name().equalsIgnoreCase(command)) {
				this.command = enumShipCommand;
				markDirty();
				if (WarpDriveConfig.LOGGING_LUA && hasWorld()) {
					WarpDrive.logger.info(String.format("%s Command set to %s (%d)",
					                                    this, this.command, this.command.ordinal()));
				}
			}
		}
	}
	
	protected void commandDone(final boolean success, final WarpDriveText reason) {
		isEnabled = false;
		command = EnumShipCommand.IDLE;
	}
	
	protected int getFront() {
		return front;
	}
	
	private void setFront(final int front) {
		this.front = front;
		isResized = true;
	}
	
	protected int getRight() {
		return right;
	}
	
	private void setRight(final int right) {
		this.right = right;
		isResized = true;
	}
	
	protected int getUp() {
		return up;
	}
	
	private void setUp(final int up) {
		this.up = up;
		isResized = true;
	}
	
	protected int getBack() {
		return back;
	}
	
	private void setBack(final int back) {
		this.back = back;
		isResized = true;
	}
	
	protected int getLeft() {
		return left;
	}
	
	private void setLeft(final int left) {
		this.left = left;
		isResized = true;
	}
	
	protected int getDown() {
		return down;
	}
	
	private void setDown(final int down) {
		this.down = down;
		isResized = true;
	}
	
	protected void updateAfterResize() {
		isResized = false;
	}
	
	protected VectorI getMovement() {
		return new VectorI(moveFront, moveUp, moveRight);
	}
	
	protected void setMovement(final int moveFront, final int moveUp, final int moveRight) {
		this.moveFront = moveFront;
		this.moveUp = moveUp;
		this.moveRight = moveRight;
		markDirty();
	}
	
	protected byte getRotationSteps() {
		return rotationSteps;
	}
	
	private void setRotationSteps(final byte rotationSteps) {
		this.rotationSteps = (byte) ((rotationSteps + 4) % 4);
		markDirty();
	}
	
	protected void synchronizeFrom(@Nonnull final TileEntityAbstractShipController shipController) {
		shipName = shipController.shipName;
		front    = shipController.front;
		right    = shipController.right;
		up       = shipController.up;
		back     = shipController.back;
		left     = shipController.left;
		down     = shipController.down;
	}
	
	String getTargetName() {
		return nameTarget;
	}
	
	abstract public String getAllPlayersInArea();
	
	// Common OC/CC methods
	@Override
	abstract public Object[] isAssemblyValid();
	
	@Override
	abstract public Object[] getOrientation();
	
	@Override
	abstract public Object[] isInSpace();
	
	@Override
	abstract public Object[] isInHyperspace();
	
	@Override
	public Object[] shipName(final Object[] arguments) {
		if (arguments != null && arguments.length == 1 && arguments[0] != null) {
			final String shipNamePrevious = shipName;
			shipName = Commons.sanitizeFileName((String) arguments[0]);
			if (!shipName.equals(shipNamePrevious)) {
				WarpDrive.logger.info(String.format("Ship renamed from '%s' to '%s' with player(s) %s",
				                                    shipNamePrevious == null ? "-null-" : shipNamePrevious,
				                                    shipName,
				                                    getAllPlayersInArea()));
			}
		}
		return new Object[] { shipName };
	}
	
	@Override
	public Object[] dim_positive(final Object[] arguments) {
		try {
			if (arguments != null && arguments.length == 3) {
				final int argInt0 = Commons.clamp(0, WarpDriveConfig.SHIP_SIZE_MAX_PER_SIDE_BY_TIER[enumTier.getIndex()], Math.abs(Commons.toInt(arguments[0])));
				final int argInt1 = Commons.clamp(0, WarpDriveConfig.SHIP_SIZE_MAX_PER_SIDE_BY_TIER[enumTier.getIndex()], Math.abs(Commons.toInt(arguments[1])));
				final int argInt2 = Commons.clamp(0, WarpDriveConfig.SHIP_SIZE_MAX_PER_SIDE_BY_TIER[enumTier.getIndex()], Math.abs(Commons.toInt(arguments[2])));
				setFront(argInt0);
				setRight(argInt1);
				setUp(Math.min(255 - pos.getY(), argInt2));
			}
		} catch (final Exception exception) {
			if (WarpDriveConfig.LOGGING_LUA) {
				WarpDrive.logger.info(String.format("%s Invalid arguments to dim_positive(): %s",
				                                    this, Commons.format(arguments)));
			}
		}
		
		return new Integer[] { getFront(), getRight(), getUp() };
	}
	
	@Override
	public Object[] dim_negative(final Object[] arguments) {
		try {
			if (arguments != null && arguments.length == 3) {
				final int argInt0 = Commons.clamp(0, WarpDriveConfig.SHIP_SIZE_MAX_PER_SIDE_BY_TIER[enumTier.getIndex()], Math.abs(Commons.toInt(arguments[0])));
				final int argInt1 = Commons.clamp(0, WarpDriveConfig.SHIP_SIZE_MAX_PER_SIDE_BY_TIER[enumTier.getIndex()], Math.abs(Commons.toInt(arguments[1])));
				final int argInt2 = Commons.clamp(0, WarpDriveConfig.SHIP_SIZE_MAX_PER_SIDE_BY_TIER[enumTier.getIndex()], Math.abs(Commons.toInt(arguments[2])));
				setBack(argInt0);
				setLeft(argInt1);
				setDown(Math.min(pos.getY(), argInt2));
			}
		} catch (final Exception exception) {
			if (WarpDriveConfig.LOGGING_LUA) {
				WarpDrive.logger.info(String.format("%s Invalid arguments to dim_negative(): %s",
				                                    this, Commons.format(arguments)));
			}
		}
		
		return new Integer[] { getBack(), getLeft(), getDown() };
	}
	
	@Override
	public Object[] command(final Object[] arguments) {
		try {
			if (arguments.length == 1 && arguments[0] != null) {
				setCommand(arguments[0].toString());
			}
		} catch (final Exception exception) {
			return new Object[] { command.toString() };
		}
		
		return new Object[] { command.toString() };
	}
	
	@Override
	public Object[] enable(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			isEnabled = Commons.toBool(arguments[0]);
		}
		return new Object[] { isEnabled };
	}
	
	@Override
	abstract public Object[] getShipSize();
	
	@Override
	public Object[] movement(final Object[] arguments) {
		try {
			if (arguments.length == 3) {
				setMovement(Commons.toInt(arguments[0]), Commons.toInt(arguments[1]), Commons.toInt(arguments[2]));
			}
		} catch (final Exception exception) {
			return new Integer[] { moveFront, moveUp, moveRight };
		}
		
		return new Integer[] { moveFront, moveUp, moveRight };
	}
	
	@Override
	abstract public Object[] getMaxJumpDistance();
	
	@Override
	public Object[] rotationSteps(final Object[] arguments) {
		try {
			if (arguments.length == 1 && arguments[0] != null) {
				setRotationSteps((byte) Commons.toInt(arguments[0]));
			}
		} catch (final Exception exception) {
			return new Integer[] { (int) rotationSteps };
		}
		
		return new Integer[] { (int) rotationSteps };
	}
	
	@Override
	public Object[] targetName(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			this.nameTarget = (String) arguments[0];
		}
		return new Object[] { nameTarget };
	}
	
	@Override
	public abstract Object[] getEnergyRequired();
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] isAssemblyValid(final Context context, final Arguments arguments) {
		return isAssemblyValid();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getOrientation(final Context context, final Arguments arguments) {
		return getOrientation();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] isInSpace(final Context context, final Arguments arguments) {
		return isInSpace();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] isInHyperspace(final Context context, final Arguments arguments) {
		return isInHyperspace();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] shipName(final Context context, final Arguments arguments) {
		return shipName(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] dim_positive(final Context context, final Arguments arguments) {
		return dim_positive(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] dim_negative(final Context context, final Arguments arguments) {
		return dim_negative(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] energy(final Context context, final Arguments arguments) {
		return energy();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] command(final Context context, final Arguments arguments) {
		return command(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] enable(final Context context, final Arguments arguments) {
		return enable(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getShipSize(final Context context, final Arguments arguments) {
		return getShipSize();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getMaxJumpDistance(final Context context, final Arguments arguments) {
		return getMaxJumpDistance();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] movement(final Context context, final Arguments arguments) {
		return movement(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] rotationSteps(final Context context, final Arguments arguments) {
		return rotationSteps(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] targetName(final Context context, final Arguments arguments) {
		return targetName(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getEnergyRequired(final Context context, final Arguments arguments) {
		return getEnergyRequired();
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "computercraft")
	public Object[] callMethod(@Nonnull final IComputerAccess computer, @Nonnull final ILuaContext context, final int method, @Nonnull final Object[] arguments) {
		final String methodName = CC_getMethodNameAndLogCall(method, arguments);
		
		switch (methodName) {
		case "isAssemblyValid":
			return isAssemblyValid();
		
		case "getOrientation":
			return getOrientation();
		
		case "isInSpace":
			return isInSpace();
		
		case "isInHyperspace":
			return isInHyperspace();
		
		case "shipName":
			return shipName(arguments);
		
		case "dim_positive":
			return dim_positive(arguments);
		
		case "dim_negative":
			return dim_negative(arguments);
		
		case "energy":
			return energy();
		
		case "command":
			return command(arguments);
		
		case "enable":
			return enable(arguments);
		
		case "getShipSize":
			return getShipSize();
		
		case "getMaxJumpDistance":
			return getMaxJumpDistance();
		
		case "movement":
			return movement(arguments);
		
		case "rotationSteps":
			return rotationSteps(arguments);
		
		case "targetName":
			return targetName(arguments);
		
		case "getEnergyRequired":
			return getEnergyRequired();
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
}
