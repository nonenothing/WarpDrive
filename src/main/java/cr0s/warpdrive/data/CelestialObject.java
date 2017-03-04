package cr0s.warpdrive.data;

import cr0s.warpdrive.WarpDrive;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

/**
 * Celestial objects are area in space. They can be a planet or solar system (space dimension) or the all mighty hyperspace.
 *
 * @author LemADEC
 */
public class CelestialObject implements Cloneable {
	public int dimensionId;
	public int dimensionCenterX, dimensionCenterZ;
	public int borderRadiusX, borderRadiusZ;
	public int parentDimensionId;
	public int parentCenterX, parentCenterZ;
	public boolean isWarpDrive;
	public double gravity;
	public boolean isBreathable;
	
	// @TODO replace with RandomCollection once we switch to XML
	private final NavigableMap<Double, String> mapGenerationRatios = new TreeMap<>();
	private double totalRatio;
	
	public CelestialObject() {
		this(0, 0, 0, 5000, 5000, -2, 0, 0);
	}
	
	public CelestialObject(final int parDimensionId, final int parDimensionCenterX, final int parDimensionCenterZ,
	                       final int parBorderRadiusX, final int parBorderRadiusZ,
	                       final int parParentDimensionId, final int parParentCenterX, final int parParentCenterZ) {
		dimensionId = parDimensionId;
		dimensionCenterX = parDimensionCenterX;
		dimensionCenterZ = parDimensionCenterZ;
		borderRadiusX = parBorderRadiusX;
		borderRadiusZ = parBorderRadiusZ;
		parentDimensionId = parParentDimensionId;
		parentCenterX = parParentCenterX;
		parentCenterZ = parParentCenterZ;
	}
	
	public CelestialObject(NBTTagCompound nbt) {
		readFromNBT(nbt);
	}
	
	@SuppressWarnings("CloneDoesntCallSuperClone")
	@Override
	public CelestialObject clone() {
		return new CelestialObject(dimensionId, dimensionCenterX, dimensionCenterZ, borderRadiusX, borderRadiusZ, parentDimensionId, parentCenterX, parentCenterZ);
	}
	
	public void setSelfProvider(final boolean isWarpDrive) {
		this.isWarpDrive = isWarpDrive;
	}
	
	public void setGravity(final double gravity) {
		this.gravity = gravity;
	}
	
	public void setBreathable(final boolean isBreathable) {
		this.isBreathable = isBreathable;
	}
	
	public void addGenerationRatio(final double ratio, final String name) {
		if (ratio <= 0 || ratio >= 1.0) {
			WarpDrive.logger.warn("Ratio isn't in ]0, 1.0] bounds, skipping " + name + " with ratio " + ratio);
			return;
		}
		if (mapGenerationRatios.containsValue(name)) {
			WarpDrive.logger.warn("Object already has a ratio defined, skipping " + name + " with ratio " + ratio);
			return;
		}
		
		if (totalRatio + ratio > 1.0) {
			WarpDrive.logger.warn("Total ratio is greater than 1.0, skipping " + name + " with ratio " + ratio);
			return;
		}
		totalRatio += ratio;
		mapGenerationRatios.put(totalRatio, name);
	}
	
	public String getRandomGeneration(Random random) {
		double value = random.nextDouble();
		
		if (value >= totalRatio) {
			return null;
		}
		
		return mapGenerationRatios.ceilingEntry(value).getValue();
	}
	
	public AxisAlignedBB getWorldBorderArea() {
		return AxisAlignedBB.getBoundingBox(
		(dimensionCenterX - borderRadiusX),   0, (dimensionCenterZ - borderRadiusZ),
		(dimensionCenterX + borderRadiusX), 255, (dimensionCenterZ + borderRadiusZ) );
	}
	
	public AxisAlignedBB getAreaToReachParent() {
		return AxisAlignedBB.getBoundingBox(
			(dimensionCenterX - borderRadiusX), 250, (dimensionCenterZ - borderRadiusZ),
			(dimensionCenterX + borderRadiusX), 255, (dimensionCenterZ + borderRadiusZ) );
	}
	
	public AxisAlignedBB getAreaInParent() {
		return AxisAlignedBB.getBoundingBox(
			(parentCenterX - borderRadiusX), 0, (parentCenterZ - borderRadiusZ),
			(parentCenterX + borderRadiusX), 8, (parentCenterZ + borderRadiusZ) );
	}
	
	// offset vector when moving from parent to this dimension
	public VectorI getEntryOffset() {
		return new VectorI(dimensionCenterX - parentCenterX, 0, dimensionCenterZ - parentCenterZ);
	}
	
	public boolean isSpace() {
		if (isHyperspace()) {
			return false;
		}
		CelestialObject celestialObjectParent = StarMapRegistry.getCelestialObject(parentDimensionId, parentCenterX, parentCenterZ);
		return celestialObjectParent != null && celestialObjectParent.isHyperspace();
	}
	
	public boolean isHyperspace() {
		return parentDimensionId == dimensionId;
	}
	
	public boolean hasAtmosphere() {
		return isBreathable && !isHyperspace() && !isSpace();
	}
	
	/**
	 * Compute distance from border to further point in an area.
	 * It's up to caller to verify if this celestial object is matched.
	 *
	 * @param aabb bounding box that should fit within border
	 * @return distance to transition borders, 0 if take off is possible
	 */
	public double getSquareDistanceOutsideBorder(final int dimensionId, final AxisAlignedBB aabb) {
		if (dimensionId != this.dimensionId) {
			return Double.POSITIVE_INFINITY;
		}
		final double rangeX = Math.max(Math.abs(aabb.minX - dimensionCenterX), Math.abs(aabb.maxX - dimensionCenterX));
		final double rangeZ = Math.max(Math.abs(aabb.minZ - dimensionCenterZ), Math.abs(aabb.maxZ - dimensionCenterZ));
		final double dX = rangeX - borderRadiusX;
		final double dZ = rangeZ - borderRadiusZ;
		if ((rangeX <= borderRadiusX) && (rangeZ <= borderRadiusZ)) {
			return - (dX * dX + dZ * dZ);
		}
		return (dX * dX + dZ * dZ);
	}
	
	/**
	 * Compute distance to reach closest border, while inside the same dimension.
	 *
	 * @param dimensionId dimension id
	 * @param x coordinates inside the celestial object
	 * @param z coordinates inside the celestial object
	 * @return 'square' distance to the closest border,
	 *          <=0 if we're inside, > 0 if we're outside,
	 *          +INF if we're in the wrong dimension
	 */
	public double getSquareDistanceOutsideBorder(final int dimensionId, final double x, final double z) {
		if (dimensionId != this.dimensionId) {
			return Double.POSITIVE_INFINITY;
		}
		final double rangeX = Math.abs(x - dimensionCenterX);
		final double rangeZ = Math.abs(z - dimensionCenterZ);
		final double dX = rangeX - borderRadiusX;
		final double dZ = rangeZ - borderRadiusZ;
		if ( (rangeX <= borderRadiusX)
		  && (rangeZ <= borderRadiusZ) ) {
			// inside: both dX and dZ are negative, so the max is actually the closest to zero
			final double dMax = Math.max(dX, dZ);
			return - (dMax * dMax);
		} else if ( (rangeX > borderRadiusX)
		         && (rangeZ > borderRadiusZ) ) {
			// outside in a diagonal
			return (dX * dX + dZ * dZ);
		}
		// outside aligned: one is negative (inside), the other is positive (outside), so the max is the outside one
		final double dMax = Math.max(dX, dZ);
		return dMax * dMax;
	}
	
	/**
	 * Check if current space coordinates allow to enter this dimension atmosphere from space.
	 *
	 * @param dimensionId current position in parent dimension
	 * @param x current position in parent dimension
	 * @param z current position in parent dimension
	 * @return square distance to transition borders, 0 if we're in orbit of the object
	 */
	public double getSquareDistanceInParent(final int dimensionId, final double x, final double z) {
		// are in another dimension?
		if (dimensionId != parentDimensionId) {
			return Double.POSITIVE_INFINITY;
		}
		// are we in orbit?
		if ( (Math.abs(x - parentCenterX) <= borderRadiusX)
		  && (Math.abs(z - parentCenterZ) <= borderRadiusZ) ) {
			return 0.0D;
		}
		// do the maths
		final double dx = Math.max(0.0D, Math.abs(x - parentCenterX) - borderRadiusX);
		final double dz = Math.max(0.0D, Math.abs(z - parentCenterZ) - borderRadiusZ);
		return dx * dx + dz * dz;
	}
	
	public void readFromNBT(NBTTagCompound tag) {
		dimensionId = tag.getInteger("dimensionId");
		dimensionCenterX = tag.getInteger("dimensionCenterX");
		dimensionCenterZ = tag.getInteger("dimensionCenterZ");
		borderRadiusX = tag.getInteger("borderSizeX");
		borderRadiusZ = tag.getInteger("borderSizeZ");
		parentDimensionId = tag.getInteger("parentDimensionId");
		parentCenterX = tag.getInteger("parentCenterX");
		parentCenterZ = tag.getInteger("parentCenterZ");
		isWarpDrive = tag.getBoolean("isWarpDrive");
		gravity = tag.getDouble("gravity");
		isBreathable = tag.getBoolean("isBreathable");
		// @TODO: mapGenerationRatios
	}
	
	public void writeToNBT(NBTTagCompound tag) {
		tag.setInteger("dimensionId", dimensionId);
		tag.setInteger("dimensionCenterX", dimensionCenterX);
		tag.setInteger("dimensionCenterZ", dimensionCenterZ);
		tag.setInteger("borderRadiusX", borderRadiusX);
		tag.setInteger("borderRadiusZ", borderRadiusZ);
		tag.setInteger("parentDimensionId", parentDimensionId);
		tag.setInteger("parentCenterX", parentCenterX);
		tag.setInteger("parentCenterZ", parentCenterZ);
		tag.setBoolean("isWarpDrive", isWarpDrive);
		tag.setDouble("gravity", gravity);
		tag.setBoolean("isBreathable", isBreathable);
		// @TODO: mapGenerationRatios
	}
	
	@Override
	public int hashCode() {
		return dimensionId << 16 + (dimensionCenterX >> 10) << 8 + (dimensionCenterZ >> 10);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof CelestialObject) {
			CelestialObject celestialObject = (CelestialObject) object;
			return dimensionId == celestialObject.dimensionId
				&& dimensionCenterX == celestialObject.dimensionCenterX
				&& dimensionCenterZ == celestialObject.dimensionCenterZ
				&& borderRadiusX == celestialObject.borderRadiusX
				&& borderRadiusZ == celestialObject.borderRadiusZ
				&& parentDimensionId == celestialObject.parentDimensionId
				&& parentCenterX == celestialObject.parentCenterX
				&& parentCenterZ == celestialObject.parentCenterZ;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "CelestialObject [Dimension " + dimensionId + " @ (" + dimensionCenterX + " " + dimensionCenterZ + ")"
				+ " Border(" + borderRadiusX + " " + borderRadiusZ + ")"
				+ " Parent(" + parentDimensionId + " @ (" + parentCenterX + " " + parentCenterZ + "))]"
				+ " isWarpDrive + " + isWarpDrive + " gravity " + gravity + " isBreathable " + isBreathable;
	}
}