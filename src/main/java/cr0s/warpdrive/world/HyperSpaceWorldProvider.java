package cr0s.warpdrive.world;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.StarMapRegistry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class HyperSpaceWorldProvider extends AbstractWorldProvider {
	
	public HyperSpaceWorldProvider() {
		biomeProvider  = new BiomeProviderSingle(WarpDrive.spaceBiome);
		hasNoSky = true;
	}
	
	@Nonnull
	@Override
	public DimensionType getDimensionType() {
		return WarpDrive.dimensionTypeHyperSpace;
	}
	
	@Override
	public boolean canRespawnHere() {
		return true;
	}
	
	@Override
	public boolean isSurfaceWorld() {
		return true;
	}
	
	@Override
	public int getAverageGroundLevel() {
		return 1;
	}
	
	@Override
	public double getHorizon() {
		return -256;
	}
	
	@Override
	public void updateWeather() {
		super.resetRainAndThunder();
	}
	
	@Nonnull
	@Override
	public Biome getBiomeForCoords(@Nonnull final BlockPos blockPos) {
		return WarpDrive.spaceBiome;
	}
	
	@Override
	public void setAllowedSpawnTypes(final boolean allowHostile, final boolean allowPeaceful) {
		super.setAllowedSpawnTypes(true, true);
	}
	
	@Override
	public float calculateCelestialAngle(final long time, final float partialTick) {
		return 0.5F;
	}
	
	@Override
	protected void generateLightBrightnessTable() {
		final float f = 0.0F;
		
		for (int i = 0; i <= 15; ++i) {
			final float f1 = 1.0F - i / 15.0F;
			lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean isSkyColored() {
		return true;
	}
		
	@Override
	public int getRespawnDimension(final EntityPlayerMP entityPlayerMP) {
		if ( entityPlayerMP == null
		  || entityPlayerMP.worldObj == null ) {
			WarpDrive.logger.error("Invalid player passed to getRespawnDimension: " + entityPlayerMP);
			return 0;
		}
		return StarMapRegistry.getHyperspaceDimensionId(entityPlayerMP.worldObj, (int) entityPlayerMP.posX, (int) entityPlayerMP.posZ);
	}
	
	@Nonnull
	@Override
	public IChunkGenerator createChunkGenerator() {
		return new HyperSpaceChunkProvider(worldObj, 46);
	}
	
	@Override
	public boolean canBlockFreeze(@Nonnull final BlockPos blockPos, final boolean byWater) {
		return false;
	}
	
	@Override
	public boolean isDaytime() {
		return false;
	}
	
	@Override
	public boolean canDoLightning(final Chunk chunk) {
		return false;
	}
	
	@Override
	public boolean canDoRainSnowIce(final Chunk chunk) {
		return false;
	}
}