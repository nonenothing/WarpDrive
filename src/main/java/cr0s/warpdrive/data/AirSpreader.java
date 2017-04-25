package cr0s.warpdrive.data;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.breathing.BlockAirGeneratorTiered;
import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public class AirSpreader {
	
	private static StateAir stateCenter = new StateAir(null);
	private static StateAir[] stateAround = {
		new StateAir(null), new StateAir(null), new StateAir(null),
		new StateAir(null), new StateAir(null), new StateAir(null) };
	private static StateAir stateAirParent = new StateAir(null);
	
	protected static void execute(final World world, final int x, final int y, final int z) {
		// note: compared to the pure block implementation, 0 really means no air, so we no longer offset by 1 on read/write
		
		// get central block state
		stateCenter.refresh(world, x, y, z);
		// force block refresh
		stateCenter.updateBlockCache(world);
		
		// skip non-air blocks
		if (!stateCenter.isAir()) {
			stateCenter.setConcentration(world, (byte) 0);
			return;
		}
		
		// identify leaking directions
		ForgeDirection[] directions = ForgeDirection.VALID_DIRECTIONS;
		if (stateCenter.isLeakingHorizontally()) {
			directions = Commons.HORIZONTAL_DIRECTIONS;
		} else if (stateCenter.isLeakingVertically()) {
			directions = Commons.VERTICAL_DIRECTIONS;
		}
		
		// collect air state in adjacent blocks
		// - biggest generator/void pressure around (excluding center block)
		int max_pressureGenerator = 0;
		ForgeDirection max_directionGenerator = null;
		int max_pressureVoid = 0;
		ForgeDirection max_directionVoid = null;
		// - accumulated air concentration including center block
		final int concentration = stateCenter.concentration;
		int sum_concentration = concentration;
		int max_concentration = concentration;
		int min_concentration = concentration;
		// - number of blocks to consider
		int air_count = 1;
		int empty_count = 0;
		
		for (ForgeDirection forgeDirection : directions) {
			StateAir stateAir = stateAround[forgeDirection.ordinal()];
			stateAir.refresh(world,
			                 x + forgeDirection.offsetX,
			                 y + forgeDirection.offsetY,
			                 z + forgeDirection.offsetZ);
			if (stateAir.isAir(forgeDirection)) {
				air_count++;
				if (stateAir.concentration > 0) {// (note1)
					sum_concentration += stateAir.concentration;
					max_concentration = Math.max(max_concentration, stateAir.concentration);
					min_concentration = Math.min(min_concentration, stateAir.concentration);
				} else {
					empty_count++;
				}
				// keep highest generator pressure that's going towards us
				if ( max_pressureGenerator < stateAir.pressureGenerator
				  && (forgeDirection.getOpposite() != stateAir.directionGenerator || stateAir.isAirSource())) {
					max_pressureGenerator = stateAir.pressureGenerator;
					max_directionGenerator = forgeDirection;
				}
				// keep highest void pressure that's going towards us
				if ( max_pressureVoid < stateAir.pressureVoid) {
					if (stateAir.isVoidSource()) {
						max_pressureVoid = stateAir.pressureVoid;
						max_directionVoid = forgeDirection;
					} else if (forgeDirection.getOpposite() != stateAir.directionVoid) {
						// confirm the source validity (i.e. detect local loops)
						stateAirParent.refresh(world, stateAir, stateAir.directionVoid);
						if (stateAirParent.pressureVoid == stateAir.pressureVoid + 1) {
							max_pressureVoid = stateAir.pressureVoid;
							max_directionVoid = forgeDirection;
						}
					}
				}
			}
		}
		
		// update volume detection, skipping the sources
		if (!stateCenter.isAirSource()) {
			// propagate if bigger pressure existing around, erase pressure otherwise
			if ( stateCenter.pressureGenerator <= max_pressureGenerator && max_pressureGenerator > 1) {
				final short new_pressureGenerator = (short) (max_pressureGenerator - 1);
				stateCenter.setGeneratorAndUpdateVoid(world, new_pressureGenerator, max_directionGenerator);
			} else {
				stateCenter.setGeneratorAndUpdateVoid(world, (short) 0, ForgeDirection.DOWN);
			}
		}
		
		if (!stateCenter.isVoidSource()) {
			// propagate if bigger pressure exists around, erase pressure otherwise
			if (stateCenter.pressureVoid <= max_pressureVoid && max_pressureVoid > 1) {
				final short new_voidPressure = (short) (max_pressureVoid - 1);
				stateCenter.setVoid(world, new_voidPressure, max_directionVoid);
			} else {
				stateCenter.setVoidAndCascade(world, (short) 0, ForgeDirection.DOWN);
			}
		}
		
		// air leaks means penalty plus some randomization for visual effects
		if (empty_count > 0 && max_pressureGenerator < 16) {
			if (concentration < 4) {
				sum_concentration -= empty_count + (world.rand.nextBoolean() ? 0 : empty_count);
			} else if (concentration < 8) {
				sum_concentration -= empty_count;
			} else if (concentration < 12) {
				sum_concentration -= air_count;
			}
		}
		if (sum_concentration < 0) sum_concentration = 0;
		
		// compute new concentration, buffing closed space
		int mid_concentration;
		int new_concentration;
		final boolean isGrowth = (max_concentration > 8 && (max_concentration - min_concentration < 70))
		                      || (max_concentration > 5 && (max_concentration - min_concentration < 4));
		if (isGrowth) {
			mid_concentration = Math.round(sum_concentration / (float) air_count) - 1;
			new_concentration = sum_concentration - mid_concentration * (air_count - 1);
			new_concentration = Math.max(Math.max(concentration + 1, max_concentration - 1), new_concentration - 20);
		} else {
			mid_concentration = (int) Math.floor(sum_concentration / (float) air_count);
			new_concentration = sum_concentration - mid_concentration * (air_count - 1);
			if (empty_count > 0) {
				new_concentration = Math.max(0, new_concentration - 5);
			}
		}
		// apply (de)pressurisation effects
		if (max_pressureVoid > 0) {
			if (max_pressureVoid < 260) {
				mid_concentration = Math.min(mid_concentration, 12);
				new_concentration = Math.min(new_concentration, 12);
			}
		} else if (max_pressureGenerator > 20 && new_concentration > 16) {
			mid_concentration += 2;
			new_concentration += 2;
		}
		
		// apply scale and clamp
		if (mid_concentration < 0) {
			mid_concentration = 0;
		} else if (mid_concentration > StateAir.CONCENTRATION_MAX) {
			mid_concentration = StateAir.CONCENTRATION_MAX;
		}
		if (new_concentration < 0) {
			new_concentration = 0;
		} else if (new_concentration > max_concentration - 1) {
			new_concentration = Math.max(0, max_concentration - 1);
		}
		if (WarpDrive.isDev) {
			assert (new_concentration < 0);
			assert (mid_concentration < 0);
			assert (new_concentration > StateAir.CONCENTRATION_MASK);
			assert (mid_concentration > StateAir.CONCENTRATION_MASK);
			if (WarpDriveConfig.LOGGING_BREATHING) {
				StringBuilder debugConcentrations = new StringBuilder();
				for (ForgeDirection forgeDirection : directions) {
					debugConcentrations.append(String.format(" %3d", stateAround[forgeDirection.ordinal()].concentration));
				}
				WarpDrive.logger.info(String.format("Updating air 0x%8x @ %6d %3d %6d %s from %3d near %s total %3d, empty %d/%d -> %3d + %d * %3d",
				                                    stateCenter.dataAir, x, y, z,
				                                    isGrowth ? "growing" : "stable ",
				                                    concentration,
				                                    debugConcentrations.toString(),
				                                    sum_concentration, empty_count, air_count,
				                                    new_concentration, air_count - 1, mid_concentration) );
			}
			
			// new_concentration = mid_concentration = 0;
		}
		
		// protect air generator
		if (concentration != new_concentration) {
			if (!stateCenter.isAirSource()) {
				stateCenter.setConcentration(world, (byte) new_concentration);
			} else {
				boolean hasGenerator = false;
				final int metadataSource = world.getBlockMetadata(x, y, z);
				ForgeDirection forgeDirection = ForgeDirection.getOrientation(metadataSource & 7).getOpposite();
				Block block = world.getBlock(x + forgeDirection.offsetX,
				                             y + forgeDirection.offsetY,
				                             z + forgeDirection.offsetZ);
				if (block instanceof BlockAirGeneratorTiered) {
					int metadataGenerator = world.getBlockMetadata(x + forgeDirection.offsetX,
					                                               y + forgeDirection.offsetY,
					                                               z + forgeDirection.offsetZ);
					if ((metadataGenerator & 8) != 0 && (metadataGenerator & 7) == (metadataSource & 7)) {
						// all good
						hasGenerator = true;
					}
				}
				if (!hasGenerator) {
					if (WarpDriveConfig.LOGGING_BREATHING) {
						WarpDrive.logger.info(String.format("AirGenerator not found, removing air block at (%d %d %d)",
						                                    x, y, z));
					}
					stateCenter.removeAirSource(world);
				}
			}
		} else if (stateCenter.isAirFlow() && new_concentration == 0) {
			WarpDrive.logger.warn(String.format("Recovering: airFlow removed by center tick of %s", stateCenter));
			stateCenter.setConcentration(world, (byte) mid_concentration);
		}
		
		// Check and update air to adjacent blocks
		// (do not overwrite source block, do not decrease neighbors if we're growing)
		for (ForgeDirection forgeDirection : directions) {
			StateAir stateAir = stateAround[forgeDirection.ordinal()];
			if (stateAir.isAirFlow()) {
				if ( stateAir.concentration != mid_concentration
				  && (!isGrowth || stateAir.concentration < mid_concentration)) {
					stateAir.setConcentration(world, (byte) mid_concentration);
				} else if (mid_concentration == 0 && stateAir.concentration == 0) {
					WarpDrive.logger.warn(String.format("Recovering: airFlow removed by connected tick of %s", stateAir));
					stateAir.setConcentration(world, (byte) mid_concentration);
				}
			} else if (stateAir.isAir(forgeDirection) && !stateAir.isAirSource()) {
				stateAir.setConcentration(world, (byte) mid_concentration);
			}
		}
	}
	
	public static void clearCache() {
		stateCenter.clearCache();
		for (final StateAir stateAir : stateAround) {
			stateAir.clearCache();
		}
		stateAirParent.clearCache();		
	}
}
