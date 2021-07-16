package corgitaco.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.modid.util.FastNoiseLite;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;

import java.util.stream.IntStream;

public class EpicChunkGenerator extends ChunkGenerator {
    public static final Codec<EpicChunkGenerator> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(BiomeProvider.CODEC.fieldOf("biome_source").forGetter((epicChunkGenerator) -> {
            return epicChunkGenerator.biomeSource;
        }), Codec.LONG.fieldOf("seed").stable().forGetter((epicChunkGenerator) -> {
            return epicChunkGenerator.seed;
        }), DimensionStructuresSettings.CODEC.fieldOf("settings").forGetter((epicChunkGenerator) -> {
            return epicChunkGenerator.getSettings();
        })).apply(builder, builder.stable(EpicChunkGenerator::new));
    });


    private final long seed;
    private final SharedSeedRandom random;
    private final INoiseGenerator surfaceNoise;
    private final BlockState defaultBlock = Blocks.STONE.defaultBlockState();
    private final BlockState defaultFluid = Blocks.WATER.defaultBlockState();
    private final FastNoiseLite mountainNoise;
    private final FastNoiseLite mountainSurfaceNoise;
    private final FastNoiseLite noise3;

    public EpicChunkGenerator(BiomeProvider biomeSource, long seed, DimensionStructuresSettings structuresSettings) {
        super(biomeSource, biomeSource, structuresSettings, seed);
        this.seed = seed;
        this.random = new SharedSeedRandom(seed);
        this.surfaceNoise = new PerlinNoiseGenerator(this.random, IntStream.rangeClosed(-3, 0));
        this.mountainNoise = createMountainNoise(seed);
        this.mountainSurfaceNoise = createSimplexNoise(seed);
        this.noise3 = createSurfaceNoise(seed);
    }

    private static FastNoiseLite createMountainNoise(long seed) {
        FastNoiseLite fastNoiseLite = new FastNoiseLite((int) seed);
        fastNoiseLite.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
        fastNoiseLite.SetFractalOctaves(3);
        fastNoiseLite.SetFrequency(0.005F);
        return fastNoiseLite;
    }

    private static FastNoiseLite createSimplexNoise(long seed) {
        FastNoiseLite fastNoiseLite = new FastNoiseLite((int) seed);
        fastNoiseLite.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
        fastNoiseLite.SetFractalType(FastNoiseLite.FractalType.PingPong);
        fastNoiseLite.SetFractalOctaves(3);
        fastNoiseLite.SetFrequency(0.005F);
        fastNoiseLite.SetFractalGain(0.05F);
        return fastNoiseLite;
    }

    private static FastNoiseLite createSurfaceNoise(long seed) {
        FastNoiseLite fastNoiseLite = new FastNoiseLite((int) seed);
        fastNoiseLite.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        fastNoiseLite.SetFractalType(FastNoiseLite.FractalType.FBm);
        fastNoiseLite.SetFractalOctaves(6);
        fastNoiseLite.SetFrequency(0.05F);
        fastNoiseLite.SetFractalGain(0.05F);
        fastNoiseLite.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        return fastNoiseLite;
    }

    @Override
    public void fillFromNoise(IWorld world, StructureManager structureManager, IChunk chunk) {
        ChunkPos pos = chunk.getPos();
        this.mountainNoise.SetFrequency(0.05F);
        int minChunkBlockX = pos.getMinBlockX();
        int minChunkBlockZ = pos.getMinBlockZ();

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int chunkX = 0; chunkX < 16; chunkX++) {
            for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                int worldX = minChunkBlockX + chunkX;
                int worldZ = minChunkBlockZ + chunkZ;
                float heightRaw =  this.mountainNoise.GetNoise(worldX, worldZ);

                float simplexHeight = Math.abs(this.mountainSurfaceNoise.GetNoise(worldX * 0.5F, worldZ * 0.5F)) * 100;


                float height = (getSeaLevel() + heightRaw * 50) + 50 + simplexHeight;


                for (int y = 0; y < Math.max(height, getSeaLevel()); y++) {
                    chunk.setBlockState(mutable.set(chunkX, y, chunkZ), Blocks.STONE.defaultBlockState(), false);
                }

            }
        }
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion world, IChunk chunk) {
        ChunkPos chunkpos = chunk.getPos();
        int i = chunkpos.x;
        int j = chunkpos.z;
        SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
        sharedseedrandom.setBaseChunkSeed(i, j);
        ChunkPos chunkpos1 = chunk.getPos();
        int k = chunkpos1.getMinBlockX();
        int l = chunkpos1.getMinBlockZ();
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int i1 = 0; i1 < 16; ++i1) {
            for (int j1 = 0; j1 < 16; ++j1) {
                int k1 = k + i1;
                int l1 = l + j1;
                int i2 = chunk.getHeight(Heightmap.Type.WORLD_SURFACE_WG, i1, j1) + 1;
                double d1 = this.surfaceNoise.getSurfaceNoiseValue((double) k1 * 0.0625D, (double) l1 * 0.0625D, 0.0625D, (double) i1 * 0.0625D) * 15.0D;
                world.getBiome(blockpos$mutable.set(k + i1, i2, l + j1)).buildSurfaceAt(sharedseedrandom, chunk, k1, l1, i2, d1, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), world.getSeed());
            }
        }

    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return new EpicChunkGenerator(this.biomeSource, seed, this.getSettings());
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Type type) {
        return 0;
    }

    @Override
    public IBlockReader getBaseColumn(int x, int z) {
        return null;
    }
}
