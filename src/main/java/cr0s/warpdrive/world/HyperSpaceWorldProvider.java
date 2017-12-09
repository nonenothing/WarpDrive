package cr0s.warpdrive.world;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.StarMapRegistry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HyperSpaceWorldProvider extends AbstractWorldProvider {
	
	public HyperSpaceWorldProvider() {
		worldChunkMgr = new WorldChunkManagerHell(WarpDrive.spaceBiome, 0.0F);
		hasNoSky = true;
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
	
	@Override
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return WarpDrive.spaceBiome;
	}
	
	@Override
	public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {
		super.setAllowedSpawnTypes(true, true);
	}
	
	@Override
	public float calculateCelestialAngle(long time, float partialTick) {
		return 0.5F;
	}
	
	@Override
	protected void generateLightBrightnessTable() {
		float f = 0.0F;
		
		for (int i = 0; i <= 15; ++i) {
			float f1 = 1.0F - i / 15.0F;
			lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean isSkyColored() {
		return true;
	}
	
	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		if (player == null || player.worldObj == null) {
			WarpDrive.logger.error("Invalid player passed to getRespawnDimension: " + player);
			return 0;
		}
		return StarMapRegistry.getHyperspaceDimensionId(player.worldObj, (int) player.posX, (int) player.posZ);
	}
	
	@Override
	public IChunkProvider createChunkGenerator() {
		return new HyperSpaceChunkProvider(worldObj, 46);
	}
	
	@Override
	public boolean canBlockFreeze(int x, int y, int z, boolean byWater) {
		return false;
	}
	
	@Override
	public boolean getWorldHasVoidParticles() {
		return false;
	}
	
	@Override
	public boolean isDaytime() {
		return false;
	}
	
	@Override
	public boolean canDoLightning(Chunk chunk) {
		return false;
	}
	
	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		return false;
	}
}