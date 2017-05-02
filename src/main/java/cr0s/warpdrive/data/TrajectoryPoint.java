package cr0s.warpdrive.data;

import cr0s.warpdrive.block.atomic.BlockAcceleratorControlPoint;
import cr0s.warpdrive.block.atomic.BlockElectromagnetPlain;
import cr0s.warpdrive.block.atomic.BlockParticlesCollider;
import cr0s.warpdrive.block.atomic.BlockParticlesInjector;
import cr0s.warpdrive.block.atomic.BlockVoidShellPlain;
import cr0s.warpdrive.block.atomic.TileEntityAcceleratorControlPoint;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author LemADEC
 */
public class TrajectoryPoint extends VectorI {
	
	// 'type' is a bit-mask to cache surrounding blocks
	public static final int NO_TYPE                 = 0x00000000;
	public static final int TIER_NORMAL             = 0x00000001;
	public static final int TIER_ADVANCED           = 0x00000002;
	public static final int TIER_SUPERIOR           = 0x00000003;
	public static final int MASK_TIERS              = 0x00000003;
	public static final int MAGNETS_HORIZONTAL      = 0x00000004;
	public static final int MAGNETS_VERTICAL        = 0x00000008;
	public static final int MASK_MAGNETS_BOTH       = 0x0000000C;
	
	public static final int IS_INPUT_FORWARD        = 0x00000010;
	public static final int IS_INPUT_BACKWARD       = 0x00000020;
	public static final int MASK_IS_INPUT           = 0x00000030;
	public static final int IS_OUTPUT_FORWARD       = 0x00000040;
	public static final int IS_OUTPUT_BACKWARD      = 0x00000080;
	public static final int MASK_IS_OUTPUT          = 0x000000C0;
	
	public static final int IS_COLLIDER             = 0x00000100;
	public static final int IS_TRANSFER_PIPE        = 0x00000200;
	public static final int NEEDS_REEVALUATION      = 0x00000400;
	// reserved 0x00000800
	// reserved 0x00001000
	// reserved 0x00002000
	// reserved 0x00004000
	// reserved 0x00008000
	public static final int ERROR_NONE                   = 0x00000000;
	public static final int ERROR_DOUBLE_JUNCTION        = 0x00010000;   // invalid void shell (double junction)
	public static final int ERROR_VERTICAL_JUNCTION      = 0x00020000;   // invalid void shell (vertical movement)
	public static final int ERROR_MISSING_TURNING_MAGNET = 0x00040000;   // missing main magnets at turning point
	public static final int ERROR_MISSING_MAIN_MAGNET    = 0x00080000;   // missing main magnets at control point
	public static final int ERROR_MISSING_CORNER_MAGNET  = 0x00100000;   // missing corner magnets at control point
	public static final int ERROR_MISSING_COLLIDER       = 0x00200000;
	public static final int ERROR_MISSING_VOID_SHELL     = 0x00400000;
	public static final int ERROR_TOO_MANY_VOID_SHELLS   = 0x00800000;
	// public static final int ERROR_OUT_OF_RANGE        = 0x01000000;
	// public static final int ERROR_TBD2                = 0x02000000;
	// public static final int ERROR_TBD4                = 0x04000000;
	// public static final int ERROR_TBD8                = 0x08000000;
	// public static final int ERROR_TBD10               = 0x10000000;
	// public static final int ERROR_TBD20               = 0x20000000;
	// public static final int ERROR_TBD40               = 0x40000000;
	// public static final int ERROR_TBD80               = 0x80000000;
	public static final int MASK_ERRORS                  = 0xFFFF0000;
	
	public final int type;
	
	public final VectorI vControlPoint;
	public final int controlChannel;
	
	// next block direction in positive movement
	public final ForgeDirection directionForward;
	public final ForgeDirection directionBackward;
	public final VectorI vJunctionForward;
	public final VectorI vJunctionBackward;
	
	public TrajectoryPoint(World world, final VectorI vPosition, final ForgeDirection directionForward) {
		this(world, vPosition.x, vPosition.y, vPosition.z, directionForward);
	}
	
	public TrajectoryPoint(final int x, final int y, final int z,
	                       final int type,
	                       final VectorI vControlPoint,
	                       final int controlChannel,
	                       final ForgeDirection directionForward,
	                       final ForgeDirection directionBackward,
	                       final VectorI vJunctionForward,
	                       final VectorI vJunctionBackward) {
		super(x, y, z);
		this.type = type;
		this.vControlPoint = vControlPoint;
		this.controlChannel = controlChannel;
		this.directionForward = directionForward;
		this.directionBackward = directionBackward;
		this.vJunctionForward = vJunctionForward;
		this.vJunctionBackward = vJunctionBackward;
	}
	
	// get next point on an acceleration pipe
	private TrajectoryPoint(World world, final int x, final int y, final int z, final ForgeDirection directionMain) {
		super(x, y, z);
		int typeNew = NO_TYPE;
		
		// check the core
		Block blockCore = world.getBlock(x, y , z);
		if (!(blockCore instanceof BlockVoidShellPlain)) {
			typeNew |= ERROR_MISSING_VOID_SHELL;
		}
		
		// get main blocks
		ForgeDirection directionLeft  = directionMain.getRotation(ForgeDirection.UP);
		ForgeDirection directionRight = directionMain.getRotation(ForgeDirection.DOWN);
		Block blockForward   = world.getBlock(x + directionMain.offsetX, y, z + directionMain.offsetZ);
		Block blockUp        = world.getBlock(x, y + 1, z);
		Block blockDown      = world.getBlock(x, y - 1, z);
		Block blockLeft      = world.getBlock(x + directionLeft .offsetX, y, z + directionLeft .offsetZ);
		Block blockRight     = world.getBlock(x + directionRight.offsetX, y, z + directionRight.offsetZ);
		int tier = 0;
		
		// check main magnets
		if (blockUp instanceof BlockElectromagnetPlain && blockDown instanceof BlockElectromagnetPlain) {
			int tierUp = ((BlockElectromagnetPlain) blockUp).tier;
			if (tierUp == ((BlockElectromagnetPlain) blockDown).tier) {
				tier = tier == 0 || tier == tierUp ? tierUp : -1;
				typeNew |= MAGNETS_VERTICAL;
			}
		}
		if (blockLeft instanceof BlockElectromagnetPlain && blockRight instanceof BlockElectromagnetPlain) {
			int tierLeft = ((BlockElectromagnetPlain) blockLeft).tier;
			if (tierLeft == ((BlockElectromagnetPlain) blockRight).tier) {
				tier = tier == 0 || tier == tierLeft ? tierLeft : -1;
				typeNew |= MAGNETS_HORIZONTAL;
			}
		}
		
		// checking turning
		TurnEvaluator turnEvaluator = new TurnEvaluator(world, x, y, z, directionMain, typeNew,
		                                               directionLeft, directionRight,
		                                               blockForward, blockUp, blockDown, blockLeft, blockRight,
		                                               tier);
		typeNew = turnEvaluator.typeNew;
		final boolean isShellValid = turnEvaluator.isShellValid;
		final boolean isTurning = turnEvaluator.isTurning;
		final boolean isForward = turnEvaluator.isForward;
		directionForward = turnEvaluator.directionForward;
		directionBackward = turnEvaluator.directionBackward;
		
		// check control point
		// (requires a valid shell and at least one set of main magnets) 
		VectorI new_vControlPoint = null;
		int new_controlChannel = -1;
		if (isShellValid) {
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				final Block block = world.getBlock(
						x + 2 * direction.offsetX,
						y + 2 * direction.offsetY,
						z + 2 * direction.offsetZ);
				
				if ( block instanceof BlockAcceleratorControlPoint
				  && !(block instanceof BlockParticlesInjector) ) {
					final TileEntity tileEntity = world.getTileEntity(
							x + 2 * direction.offsetX,
							y + 2 * direction.offsetY,
							z + 2 * direction.offsetZ);
					
					if ( tileEntity instanceof TileEntityAcceleratorControlPoint
					  && ((TileEntityAcceleratorControlPoint) tileEntity).getIsEnabled()) {
						if ((typeNew & MASK_MAGNETS_BOTH) == 0) {
							typeNew |= ERROR_MISSING_MAIN_MAGNET;
						} else {
							new_vControlPoint = new VectorI(
							        x + 2 * direction.offsetX,
							        y + 2 * direction.offsetY,
							        z + 2 * direction.offsetZ);
							new_controlChannel = ((TileEntityAcceleratorControlPoint) new_vControlPoint.getTileEntity(world)).getControlChannel();
						}
					}
					break;
				}
			}
		}
		vControlPoint = new_vControlPoint;
		controlChannel = new_controlChannel;
		
		// evaluate 3x3x3 blocks centered here
		boolean isInput  = false;
		boolean isOutput = false;
		if (isShellValid) {
			NodeEvaluator nodeEvaluator = new NodeEvaluator(world, x, y, z, typeNew, tier, isTurning);
			// report errors found
			typeNew = nodeEvaluator.typeNew;
			
			// require control node for collider and output
			if (nodeEvaluator.isCollider && vControlPoint != null) {
				typeNew |= IS_COLLIDER;
			}
			
			// require control node for output
			isOutput = nodeEvaluator.isOutput && vControlPoint != null;
			
			// no control node required for input
			isInput = nodeEvaluator.isInput;
		}
		
		// compute junction vectors
		if (!isInput && !isOutput) {
			vJunctionBackward = null;
			vJunctionForward = null;
		} else {
			if (isTurning) {
				if (isForward) {
					typeNew |= isInput ? IS_INPUT_FORWARD : IS_OUTPUT_FORWARD;
					vJunctionBackward = null;
					vJunctionForward = new VectorI(directionMain);
				} else {
					typeNew |= isInput ? IS_INPUT_BACKWARD : IS_OUTPUT_BACKWARD;
					vJunctionForward = null;
					if (directionForward == directionRight) {
						vJunctionBackward = new VectorI(directionLeft);
					} else {
						vJunctionBackward = new VectorI(directionRight);
					}
				}
			} else {
				typeNew |= isInput ? IS_INPUT_FORWARD : IS_OUTPUT_FORWARD; // @TODO code review, probably inverted somewhere
				Block blockForwardLeft = world.getBlock(
					x + directionMain.offsetX + directionLeft.offsetX,
					y,
					z + directionMain.offsetZ + directionLeft.offsetZ);
				Block blockForwardRight = world.getBlock(
					x + directionMain.offsetX + directionRight.offsetX,
					y,
					z + directionMain.offsetZ + directionRight.offsetZ);
				Block blockBackwardLeft = world.getBlock(
					x - directionMain.offsetX + directionLeft.offsetX,
					y,
					z - directionMain.offsetZ + directionLeft.offsetZ);
				Block blockBackwardRight = world.getBlock(
					x - directionMain.offsetX + directionRight.offsetX,
					y,
					z - directionMain.offsetZ + directionRight.offsetZ);
				if (blockForwardLeft instanceof BlockVoidShellPlain) {
					typeNew |= isInput ? IS_INPUT_FORWARD : IS_OUTPUT_FORWARD;
					vJunctionForward = new VectorI(directionLeft).translate(directionMain);
					if (blockBackwardLeft instanceof BlockVoidShellPlain) {
						typeNew |= isInput ? IS_INPUT_BACKWARD : IS_OUTPUT_BACKWARD;
						vJunctionBackward = new VectorI(directionLeft).translate(directionMain, -1);
					} else {
						vJunctionBackward = null;
					}
				} else if (blockForwardRight instanceof BlockVoidShellPlain) {
					typeNew |= isInput ? IS_INPUT_FORWARD : IS_OUTPUT_FORWARD;
					vJunctionForward = new VectorI(directionRight).translate(directionMain);
					if (blockBackwardRight instanceof BlockVoidShellPlain) {
						typeNew |= isInput ? IS_INPUT_BACKWARD : IS_OUTPUT_BACKWARD;
						vJunctionBackward = new VectorI(directionRight).translate(directionMain, -1);
					} else {
						vJunctionBackward = null;
					}
				} else {
					vJunctionForward = null;
					if (blockBackwardLeft instanceof BlockVoidShellPlain) {
						typeNew |= isInput ? IS_INPUT_BACKWARD : IS_OUTPUT_BACKWARD;
						vJunctionBackward = new VectorI(directionLeft).translate(directionMain, -1);
					} else if (blockBackwardRight instanceof BlockVoidShellPlain) {
						typeNew |= isInput ? IS_INPUT_BACKWARD : IS_OUTPUT_BACKWARD;
						vJunctionBackward = new VectorI(directionRight).translate(directionMain, -1);
					} else {
						vJunctionBackward = null;
					}
				}
			}
		}
		
		
		// save the results
		if (tier == 1) {
			typeNew |= TIER_NORMAL;
		} else if (tier == 2) {
			typeNew |= TIER_ADVANCED;
		} else if (tier == 3) {
			typeNew |= TIER_SUPERIOR;
		}
		type = typeNew;
	}
	
	// get next point on a transfer pipe
	public TrajectoryPoint(World world, final TrajectoryPoint trajectoryPoint, final boolean isForward) {
		int typeNew = IS_TRANSFER_PIPE;
		// get first/next transfer pipe
		this.x = trajectoryPoint.x + (isForward ? trajectoryPoint.vJunctionForward.x : trajectoryPoint.vJunctionBackward.x);
		this.y = trajectoryPoint.y + (isForward ? trajectoryPoint.vJunctionForward.y : trajectoryPoint.vJunctionBackward.y);
		this.z = trajectoryPoint.z + (isForward ? trajectoryPoint.vJunctionForward.z : trajectoryPoint.vJunctionBackward.z);
		// adjust to forward orientation
		this.directionForward  = isForward ? trajectoryPoint.directionBackward.getOpposite() : trajectoryPoint.directionForward.getOpposite();
		this.directionBackward = directionForward.getOpposite();
		this.vJunctionForward  = isForward ? trajectoryPoint.vJunctionForward : trajectoryPoint.vJunctionBackward;
		this.vJunctionBackward = null;
		// get expected tier
		int tier = (trajectoryPoint.type & MASK_TIERS)
		         + (((trajectoryPoint.type & MASK_IS_INPUT ) != 0) ? -1 : 0)
		         + (((trajectoryPoint.type & MASK_IS_OUTPUT) != 0) ? 1 : 0);
		if (tier <= 0 || tier > 3) {
			tier = 0;
		}
		
		// check support shells
		boolean isStraightLine = (vJunctionForward.x == -directionBackward.offsetX) && (vJunctionForward.z == -directionBackward.offsetZ);
		Block blockForward   = world.getBlock(x + vJunctionForward.x, y, z + vJunctionForward.z);
		Block blockBack      = world.getBlock(x + directionBackward.offsetX, y, z + directionBackward.offsetZ);
		Block blockUp        = world.getBlock(x, y + 1, z);
		Block blockDown      = world.getBlock(x, y - 1, z);
		
		// check main magnets to trigger node evaluation
		// (up and down magnets should have same tier, but different from current one)
		boolean hasVerticalMagnets = false;
		if (blockUp instanceof BlockElectromagnetPlain && blockDown instanceof BlockElectromagnetPlain) {
			int tierUp = ((BlockElectromagnetPlain) blockUp).tier;
			if (tierUp == ((BlockElectromagnetPlain) blockDown).tier) {
				hasVerticalMagnets = tier == tierUp;
			}
		}
		
		// check forward movement
		boolean isForwardOk = blockForward instanceof BlockVoidShellPlain;
		boolean isBackOk    = blockBack    instanceof BlockVoidShellPlain;
		boolean isShellValid = !(blockUp   instanceof BlockVoidShellPlain)     // no vertical transfer
		                    && !(blockDown instanceof BlockVoidShellPlain)     // no vertical transfer
		                    && isForwardOk && isBackOk && tier != 0;
		if (blockUp instanceof BlockVoidShellPlain || blockDown instanceof BlockVoidShellPlain) {
			typeNew |= ERROR_VERTICAL_JUNCTION;
		}
		if (!isForwardOk || !isBackOk) {
			typeNew |= ERROR_MISSING_VOID_SHELL;
		}
		
		// assuming it's a transfer node, we need to check if the void shells are turning
		boolean isTurning = false;
		if (hasVerticalMagnets) {
			// when transfer line is at 45deg, we can't input/output in a turning corner, so we skip that case
			if (isStraightLine) {
				// we just do a basic check of void shells, the full validation of magnets is done in the node evaluator
				ForgeDirection directionMain  = directionBackward.getOpposite();
				ForgeDirection directionLeft  = directionMain.getRotation(ForgeDirection.UP);
				ForgeDirection directionRight = directionMain.getRotation(ForgeDirection.DOWN);
				Block blockLeft      = world.getBlock(x + directionLeft .offsetX, y, z + directionLeft .offsetZ);
				Block blockRight     = world.getBlock(x + directionRight.offsetX, y, z + directionRight.offsetZ);
				isTurning = blockLeft instanceof BlockVoidShellPlain || blockRight instanceof BlockVoidShellPlain;
			}
		}
		
		// ignore control point
		vControlPoint = null;
		controlChannel = -1;
		
		// look for an input or output node
		if (isShellValid && hasVerticalMagnets) {
			NodeEvaluator nodeEvaluator = new NodeEvaluator(world, x, y, z, typeNew, tier, isTurning);
			typeNew = nodeEvaluator.typeNew;
			if (nodeEvaluator.isInput || nodeEvaluator.isOutput) {
				// it's a transfer node, mark it for re-evaluation
				typeNew |= NEEDS_REEVALUATION;
			}
		}
		
		// save the results
		if (tier == 1) {
			typeNew |= TIER_NORMAL;
		} else if (tier == 2) {
			typeNew |= TIER_ADVANCED;
		} else if (tier == 3) {
			typeNew |= TIER_SUPERIOR;
		}
		type = typeNew;
	}
	
	public boolean needsReevaluation() { return (type & NEEDS_REEVALUATION) != 0; }
	
	public boolean hasNoMissingVoidShells() {
		return (type & ERROR_MISSING_VOID_SHELL) == 0;
	}
	
	public int getTier() {
		return getTier(type);
	}
	
	public static int getTier(final int type) {
		switch (type & MASK_TIERS) {
		case TIER_NORMAL: return 1;
		case TIER_ADVANCED: return 2;
		case TIER_SUPERIOR: return 3;
		default: return 0;
		}
	}
	
	public int getMagnetsCount() {
		switch (type & (MASK_MAGNETS_BOTH | MASK_ERRORS)) {
		case MAGNETS_HORIZONTAL: return 1;
		case MAGNETS_VERTICAL: return 1;
		case MASK_MAGNETS_BOTH: return 2;
		default: return 0;
		}
	}
	
	public boolean isTransferPipe() {
		return (type & IS_TRANSFER_PIPE) != 0;
	}
	
	public boolean isCollider() {
		return isCollider(type);
	}
	
	public static boolean isCollider(final int type) {
		return (type & IS_COLLIDER) != 0;
	}
	
	public static boolean isInput(final int type) {
		return (type & MASK_IS_INPUT) != 0;
	}
	
	public static boolean isOutput(final int type) {
		return (type & MASK_IS_OUTPUT) != 0;
	}
	
	public Vector3 getJunctionOut(final ForgeDirection directionCurrent) {
		// skip erroneous setup
		if ((type & MASK_ERRORS) != ERROR_NONE) {
			return null;
		}
		// output while moving forward (i.e. while coming opposite of backward)
		if (((type & IS_OUTPUT_FORWARD) != 0) && directionCurrent.getOpposite().equals(directionBackward)) {
			return new Vector3(vJunctionForward.x, vJunctionForward.y, vJunctionForward.z).normalize();
		}
		// output while moving backward (i.e. while coming opposite of forward)
		if (((type & IS_OUTPUT_BACKWARD) != 0) && directionCurrent.getOpposite().equals(directionForward)) {
			return new Vector3(vJunctionBackward.x, vJunctionBackward.y, vJunctionBackward.z).normalize();
		}
		return null;
	}
	
	public ForgeDirection getTurnedDirection(final ForgeDirection directionCurrent) {
		// skip erroneous setup
		if ((type & ERROR_MISSING_TURNING_MAGNET) != ERROR_NONE) {
			return null;
		}
		// skip straight line
		if (directionForward.getOpposite().equals(directionBackward)) {
			return null;
		}
		// turn forward
		if (directionCurrent.equals(directionForward.getOpposite())) {
			return directionBackward;
		}
		// turn backward
		if (directionCurrent.equals(directionBackward.getOpposite())) {
			return directionForward;
		}
		return null;
	}
	
	public ForgeDirection getJunctionIn(final Vector3 vectorCurrent) {
		// skip erroneous setup
		if ((type & MASK_ERRORS) != ERROR_NONE) {
			return null;
		}
		// skip non-junction
		if ((type & MASK_IS_INPUT) == 0) {
			return null;
		}
		VectorI vJunctionRequired = new VectorI(
			(int) -Math.signum(vectorCurrent.x),
		    (int) -Math.signum(vectorCurrent.y),
		    (int) -Math.signum(vectorCurrent.z));
		// input in forward motion
		if ( ((type & IS_INPUT_FORWARD) != 0) && vJunctionRequired.equals(vJunctionBackward) ) {
			return directionForward;
		}
		// input in backward motion
		if (((type & IS_INPUT_BACKWARD) != 0) && vJunctionRequired.equals(vJunctionBackward)) {
			return directionBackward;
		}
		return null;
	}
	
	@Override
	public TrajectoryPoint clone() {
		return new TrajectoryPoint(x, y, z, type, vControlPoint, controlChannel, directionForward, directionBackward, vJunctionForward, vJunctionBackward);
	}
	
	public VectorI getVectorI() {
		return new VectorI(x, y, z);
	}
	
	@Override
	public boolean equals(final Object object) {
		if (object instanceof VectorI) {
			VectorI vector = (VectorI) object;
			return (x == vector.x) && (y == vector.y) && (z == vector.z);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("TrajectoryPoint [%d %d %d] %8x %5s %5s %s %s %s tier%d %s",
			x, y, z,
			type,
			directionForward  == null ? "-null-" : directionForward.toString(),
			directionBackward == null ? "-null-" : directionBackward.toString(),
			vJunctionForward  == null ? "-null-" : vJunctionForward.toString(),
			vJunctionBackward == null ? "-null-" : vJunctionBackward.toString(),
			vControlPoint     == null ? "-null-" : vControlPoint.toString(),
			type & MASK_TIERS,
			hasNoMissingVoidShells());
	}
	
	// validate a accelerator node
	private class NodeEvaluator {
		
		public int typeNew;
		public boolean isCollider;
		public boolean isInput;
		public boolean isOutput;
		
		public NodeEvaluator(World world, final int x, final int y, final int z, final int typeOriginal, final int tierMain, final boolean isTurning) {
			typeNew = typeOriginal;
			isInput = false;
			isOutput = false;
			
			// all 8 corners are always present, but can be glass or plain, so we can't check strict block equality
			int countVoidShell = 0;
			int countMainMagnet = 0;
			int countLowerMagnet = 0;
			int countHigherMagnet = 0;
			int countCollider = 0;
			for (int offsetX = -1; offsetX < 2; offsetX++) {
				for (int offsetY = -1; offsetY < 2; offsetY++) {
					for (int offsetZ = -1; offsetZ < 2; offsetZ++) {
						Block blockCheck = world.getBlock(
							x + offsetX,
							y + offsetY,
							z + offsetZ);
						if (blockCheck instanceof BlockElectromagnetPlain) {
							int tierCheck = ((BlockElectromagnetPlain) blockCheck).tier;
							if (tierCheck == tierMain) {
								countMainMagnet++;
							} else if (tierCheck > tierMain) {
								countHigherMagnet++;
							} else {
								countLowerMagnet++;
							}
						} else if (blockCheck instanceof BlockVoidShellPlain) {
							countVoidShell++;
						} else if (blockCheck instanceof BlockParticlesCollider) {
							countCollider++;
						} else if (blockCheck instanceof BlockParticlesInjector) {
							int tierCheck = ((BlockParticlesInjector) blockCheck).tier;
							if (tierCheck == tierMain) {
								countMainMagnet++;
							}
						}
					}
				}
			}
			
			// check collider
			// (we can be in between 2 colliders, so we need at least 9 to report an error)
			if (countCollider > 8) {
				if (countCollider < 12) {// need at least 12 collider blocks
					typeNew |= ERROR_MISSING_COLLIDER;
				} else if (countVoidShell < 3) {// need exactly 3 void shells
					typeNew |= ERROR_MISSING_VOID_SHELL;
				} else if (countVoidShell > 3) {// need exactly 3 void shells
					typeNew |= ERROR_TOO_MANY_VOID_SHELLS;
				} else if (countMainMagnet < 12) {
					typeNew |= ERROR_MISSING_MAIN_MAGNET;
				} else {
					isCollider = true;
				}
			}
			if (countLowerMagnet > 8 || countHigherMagnet > 8) {
				if (countLowerMagnet < 12 && countHigherMagnet < 12) {// need at least 12 corner magnets
					typeNew |= ERROR_MISSING_CORNER_MAGNET;
				} else if (countLowerMagnet != 0 && countHigherMagnet != 0) {// only one type of corner magnets
					typeNew |= ERROR_MISSING_CORNER_MAGNET;
				} else if (isTurning && countVoidShell < 4) {// a turning junction requires 4 void shells
					typeNew |= ERROR_MISSING_VOID_SHELL;
				} else if (isTurning && countVoidShell > 4) {// a turning junction requires 4 void shells
					typeNew |= ERROR_TOO_MANY_VOID_SHELLS;
				} else if ((!isTurning) && countVoidShell < 5) {// a straight junction requires at least 5 void shells
					typeNew |= ERROR_MISSING_VOID_SHELL;
				} else if ((!isTurning) && countVoidShell > 6) {// a straight junction requires at most 6 void shells
					typeNew |= ERROR_TOO_MANY_VOID_SHELLS;
				} else if (countMainMagnet < 9) {// at 9 main magnets
					typeNew |= ERROR_MISSING_MAIN_MAGNET;
				} else if (countVoidShell + countMainMagnet != 15) {// exactly 15 void shells + main magnets
					typeNew |= ERROR_MISSING_MAIN_MAGNET;
				} else {
					isInput = countLowerMagnet > 0;
					isOutput = countHigherMagnet > 0;
					assert(isInput || isOutput);
				}
			}
		}
	}
	
	private class TurnEvaluator {
		
		public int typeNew;
		public boolean isForward;
		public boolean isShellValid;
		public boolean isTurning;
		public ForgeDirection directionForward;
		public ForgeDirection directionBackward;
		
		public TurnEvaluator(World world, final int x, final int y, final int z, final ForgeDirection directionMain, final int typeOriginal,
		                     final ForgeDirection directionLeft, final ForgeDirection directionRight,
		                     final Block blockForward, final Block blockUp, final Block blockDown, final Block blockLeft, final Block blockRight,
		                     final int tier) {
			this.typeNew = typeOriginal;
			
			// check turning magnets
			isForward = blockForward instanceof BlockVoidShellPlain;
			boolean isLeftTurn  = blockLeft    instanceof BlockVoidShellPlain;
			boolean isRightTurn = blockRight   instanceof BlockVoidShellPlain;
			// boolean isJunction  = (isForward && (isLeftTurn || isRightTurn)) || (isLeftTurn && isRightTurn);
			isShellValid = !(blockUp instanceof BlockVoidShellPlain)       // no vertical accelerators
			            && !(blockDown instanceof BlockVoidShellPlain)     // no vertical accelerators
			            && (!isForward || !isLeftTurn || !isRightTurn);
			// boolean isShellEnding = (!isForward && !isLeftTurn && !isRightTurn); // dead end
			if (blockUp instanceof BlockVoidShellPlain || blockDown instanceof BlockVoidShellPlain) {
				typeNew |= ERROR_VERTICAL_JUNCTION;
			}
			if (isForward && isLeftTurn && isRightTurn) {
				typeNew |= ERROR_DOUBLE_JUNCTION;
			}
			isTurning = false;
			if (isShellValid && (isLeftTurn || isRightTurn)) {
				// validate the turning magnets
				Block blockForwardLeft = world.getBlock(
					x + directionMain.offsetX + directionLeft.offsetX,
					y,
					z + directionMain.offsetZ + directionLeft.offsetZ);
				Block blockForwardRight = world.getBlock(
					x + directionMain.offsetX + directionRight.offsetX,
					y,
					z + directionMain.offsetZ + directionRight.offsetZ);
				Block blockBackwardLeft = world.getBlock(
					x - directionMain.offsetX + directionLeft.offsetX,
					y,
					z - directionMain.offsetZ + directionLeft.offsetZ);
				Block blockBackwardRight = world.getBlock(
					x - directionMain.offsetX + directionRight.offsetX,
					y,
					z - directionMain.offsetZ + directionRight.offsetZ);
				if ( tier > 0
				  && blockForwardLeft   instanceof BlockElectromagnetPlain && tier == ((BlockElectromagnetPlain) blockForwardLeft  ).tier
				  && blockForwardRight  instanceof BlockElectromagnetPlain && tier == ((BlockElectromagnetPlain) blockForwardRight ).tier
				  && blockBackwardLeft  instanceof BlockElectromagnetPlain && tier == ((BlockElectromagnetPlain) blockBackwardLeft ).tier
				  && blockBackwardRight instanceof BlockElectromagnetPlain && tier == ((BlockElectromagnetPlain) blockBackwardRight).tier
				  && ((typeNew & MAGNETS_VERTICAL) == MAGNETS_VERTICAL) ) {
					// also validate the sided magnet
					isTurning = (isForward   || blockForward instanceof BlockElectromagnetPlain || blockForward instanceof BlockParticlesInjector)
					         && (isLeftTurn  || blockLeft    instanceof BlockElectromagnetPlain || blockLeft    instanceof BlockParticlesInjector)
					         && (isRightTurn || blockRight   instanceof BlockElectromagnetPlain || blockRight   instanceof BlockParticlesInjector);
				}
				if (!isTurning) {
					// check if another void shell is present on the turn side, in which case we assume it's an input/output area
					// otherwise, it's a missing turning magnet
					if (!( isLeftTurn  && (blockBackwardLeft  instanceof BlockVoidShellPlain || blockForwardLeft  instanceof BlockVoidShellPlain)
					    || isRightTurn && (blockBackwardRight instanceof BlockVoidShellPlain || blockForwardRight instanceof BlockVoidShellPlain) ) ) {
						  typeNew |= ERROR_MISSING_TURNING_MAGNET;
					}
				} else {
					// it's turning with all magnets, we can use full acceleration
					typeNew |= MASK_MAGNETS_BOTH;
				}
			}
			// compute forward vector
			if (!isTurning && (typeNew & ERROR_MISSING_TURNING_MAGNET) == 0) {
				directionForward = directionMain;
			} else if (isLeftTurn && !isRightTurn) {
				directionForward = directionLeft;
			} else if (!isLeftTurn && isRightTurn) {
				directionForward = directionRight;
			} else {
				assert(isLeftTurn && isRightTurn);
				// it's probably an input/output, in that case, magnets are all around, just pick one side to detect the direction
				Block blockUpRight   = world.getBlock(x + directionRight.offsetX, y + 1, z + directionRight.offsetZ);
				if (blockUpRight instanceof BlockElectromagnetPlain && tier != ((BlockElectromagnetPlain) blockUpRight).tier) {
					directionForward = directionLeft;
				} else {
					directionForward = directionRight;
				}
			}
			// backward vector is where we came from
			directionBackward = directionMain.getOpposite();
		}
	}
}