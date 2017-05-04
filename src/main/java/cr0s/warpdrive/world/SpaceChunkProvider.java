package cr0s.warpdrive.world;

import cr0s.warpdrive.WarpDrive;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;

public class SpaceChunkProvider extends ChunkProviderGenerate {
	
	private final World world;
	private final Random rand;
	
	public SpaceChunkProvider(World world, long seed) {
		super(world, seed, false);
		rand = new Random(seed);
		this.world = world;
	}
	
	@Override
	public Chunk provideChunk(int x, int z) {
		rand.setSeed(x * 341873128712L + z * 132897987541L);
		
		final Block[] chunkprimer = new Block[32768];
		final Chunk chunk = new Chunk(world, chunkprimer, x, z);
		
		final byte[] byteBiomes = chunk.getBiomeArray();
		for (int i = 0; i < byteBiomes.length; ++i) {
			byteBiomes[i] = (byte) WarpDrive.spaceBiome.biomeID;
		}
		
		chunk.generateSkylightMap();
		return chunk;
	}
	
	@Override
	public void populate(IChunkProvider var1, int var2, int var3) {
		// super.populate(var1, var2, var3);
		// Generate chunk population
		// GameRegistry.generateWorld(var2, var3, worldObj, var1, var1);
	}
	
	@Override
	public List getPossibleCreatures(EnumCreatureType var1, int var2, int var3, int var4) {
		return null;
	}
	
	@Override
	public ChunkPosition func_147416_a(World var1, String var2, int var3, int var4, int var5) {
		// no structure generation
		return null;
	}

	@Override
	public void recreateStructures(int var1, int var2) {
		// no structure generation
	}
}