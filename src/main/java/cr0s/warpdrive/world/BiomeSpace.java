package cr0s.warpdrive.world;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeSpace extends BiomeGenBase
{
    public BiomeSpace(final int biomeId)
    {
        super(biomeId);
        this.theBiomeDecorator.treesPerChunk = 0;
        //this.temperature = 1F;
        this.theBiomeDecorator.flowersPerChunk = 0;
        this.theBiomeDecorator.grassPerChunk = 0;
        this.biomeName = "Space";
    }

    @Override
    public float getSpawningChance()
    {
        return 0;
    }

    @Override
    public boolean canSpawnLightningBolt()
    {
        return false;
    }

    @Override
    public boolean getEnableSnow()
    {
        return false;
    }
}
