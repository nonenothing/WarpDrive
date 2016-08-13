package cr0s.warpdrive.world;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import cr0s.warpdrive.WarpDrive;
import net.minecraft.world.gen.ChunkProviderOverworld;

import javax.annotation.Nullable;

public class SpaceChunkProvider extends ChunkProviderOverworld {
	private World world;
	private Random rand;
	private final Biome[] biomesForGeneration = new Biome[1];
	
	public SpaceChunkProvider(World world, long seed) {
		super(world, seed, false, "");
		rand = new Random(seed);
		this.world = world;
		biomesForGeneration[0] = WarpDrive.spaceBiome;
	}
	
	@Override
	public Chunk provideChunk(int x, int z) {
		rand.setSeed(x * 341873128712L + z * 132897987541L);
		ChunkPrimer chunkprimer = new ChunkPrimer();
		setBlocksInChunk(x, z, chunkprimer);
		// biomesForGeneration = world.getBiomeProvider().getBiomes(biomesForGeneration, x * 16, z * 16, 16, 16);
		// replaceBiomeBlocks(x, z, chunkprimer, biomesForGeneration);
		
		Chunk chunk = new Chunk(world, chunkprimer, x, z);
		byte[] byteBiomes = chunk.getBiomeArray();
		for (int i = 0; i < byteBiomes.length; ++i) {
			byteBiomes[i] = (byte)Biome.getIdForBiome(biomesForGeneration[i]);
		}
		
		chunk.generateSkylightMap();
		return chunk;
	}
	
	@Override
	public void populate(int x, int z) {
		// super.populate(x, z);
	}

	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}

	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		return null;
	}

	@Nullable
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
		return null;
	}
	
	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z) {
		// no structure generation
	}
}