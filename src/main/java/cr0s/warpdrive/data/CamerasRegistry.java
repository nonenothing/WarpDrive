package cr0s.warpdrive.data;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CamerasRegistry {
	private LinkedList<CameraRegistryItem> registry;
	
	public CamerasRegistry() {
		registry = new LinkedList<>();
	}
	
	public CameraRegistryItem getCameraByVideoChannel(final World world, final int videoChannel) {
		if (world == null) {
			return null;
		}
		CameraRegistryItem cam;
		for (final Iterator<CameraRegistryItem> it = registry.iterator(); it.hasNext();) {
			cam = it.next();
			if (cam.videoChannel == videoChannel && cam.dimensionId == world.provider.getDimension()) {
				if (isCamAlive(world, cam)) {
					return cam;
				} else {
					if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
						WarpDrive.logger.info("Removing 'dead' camera at "
							+ cam.position.getX() + ", " + cam.position.getY() + ", " + cam.position.getZ() + " (while searching)");
					}
					it.remove();
				}
			}
		}
		
		// not found => dump registry
		printRegistry(world);
		return null;
	}
	
	private CameraRegistryItem getCamByPosition(final World world, final BlockPos position) {
		CameraRegistryItem cam;
		for (final Iterator<CameraRegistryItem> it = registry.iterator(); it.hasNext();) {
			cam = it.next();
			if (cam.position.getX() == position.getX() && cam.position.getY() == position.getY() && cam.position.getZ() == position.getZ()
					&& cam.dimensionId == world.provider.getDimension()) {
				return cam;
			}
		}
		
		return null;
	}
	
	private static boolean isCamAlive(final World world, final CameraRegistryItem cam) {
		if (world.provider.getDimension() != cam.dimensionId) {
			WarpDrive.logger.error("Inconsistent worldObj with camera " + world.provider.getDimension() + " vs " + cam.dimensionId);
			return false;
		}
		
		if (!world.getChunkFromBlockCoords(cam.position).isLoaded()) {
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info("Reporting an 'unloaded' camera in dimension " + cam.dimensionId + " at "
						+ cam.position.getX() + ", " + cam.position.getY() + ", " + cam.position.getZ());
			}
			return false;
		}
		Block block = world.getBlockState(cam.position).getBlock();
		if ((block != WarpDrive.blockCamera) && (block != WarpDrive.blockLaserCamera)) {
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info("Reporting a 'dead' camera in dimension " + cam.dimensionId + " at "
						+ cam.position.getX() + ", " + cam.position.getY() + ", " + cam.position.getZ());
			}
			return false;
		}
		
		return true;
	}
	
	private void removeDeadCams(final World world) {
		// LocalProfiler.start("CamRegistry Removing dead cameras");
		
		CameraRegistryItem cam;
		for (final Iterator<CameraRegistryItem> it = registry.iterator(); it.hasNext();) {
			cam = it.next();
			if (!isCamAlive(world, cam)) {
				if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
					WarpDrive.logger.info("Removing 'dead' camera in dimension " + cam.dimensionId + " at "
							+ cam.position.getX() + ", " + cam.position.getY() + ", " + cam.position.getZ());
				}
				it.remove();
			}
		}
		
		// LocalProfiler.stop();
	}
	
	public void removeFromRegistry(final World world, final BlockPos position) {
		final CameraRegistryItem cam = getCamByPosition(world, position);
		if (cam != null) {
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info("Removing camera by request in dimension " + cam.dimensionId + " at "
						+ cam.position.getX() + ", " + cam.position.getY() + ", " + cam.position.getZ());
			}
			registry.remove(cam);
		}
	}
	
	public void updateInRegistry(final World world, final BlockPos position, final int videoChannel, final EnumCameraType enumCameraType) {
		final CameraRegistryItem cam = new CameraRegistryItem(world, position, videoChannel, enumCameraType);
		removeDeadCams(world);
		
		if (isCamAlive(world, cam)) {
			final CameraRegistryItem existingCam = getCamByPosition(world, cam.position);
			if (existingCam == null) {
				if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
					WarpDrive.logger.info("Adding 'live' camera at "
							+ cam.position.getX() + ", " + cam.position.getY() + ", " + cam.position.getZ()
							+ " with video channel '" + cam.videoChannel + "'");
				}
				registry.add(cam);
			} else if (existingCam.videoChannel != cam.videoChannel) {
				if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
					WarpDrive.logger.info("Updating 'live' camera at "
							+ cam.position.getX() + ", " + cam.position.getY() + ", " + cam.position.getZ()
							+ " from video channel '" + existingCam.videoChannel + "' to video channel '" + cam.videoChannel + "'");
				}
				existingCam.videoChannel = cam.videoChannel;
			}
		} else {
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info("Unable to update 'dead' camera at " + cam.position.getX() + ", " + cam.position.getY() + ", " + cam.position.getZ());
			}
		}
	}
	
	public void printRegistry(World world) {
		if (world == null) {
			return;
		}
		WarpDrive.logger.info("Cameras registry for dimension " + world.provider.getSaveFolder() + ":");
		
		for (final CameraRegistryItem cam : registry) {
			WarpDrive.logger.info("- " + cam.videoChannel + " (" + cam.position.getX() + ", " + cam.position.getY() + ", " + cam.position.getZ() + ")");
		}
	}
}
