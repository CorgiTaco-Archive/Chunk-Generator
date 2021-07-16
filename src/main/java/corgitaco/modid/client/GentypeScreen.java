package corgitaco.modid.client;

import corgitaco.modid.EpicChunkGenerator;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;

public class GentypeScreen {
    public static final BiomeGeneratorTypeScreens EPIC_CHUNK_GENERATOR = new BiomeGeneratorTypeScreens(new TranslationTextComponent("yeet")) {
        protected ChunkGenerator generator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> settings, long seed) {
            return new EpicChunkGenerator(new SingleBiomeProvider(biomeRegistry.getOrThrow(Biomes.PLAINS)), seed, settings.getOrThrow(DimensionSettings.OVERWORLD).structureSettings());
        }
    };
}
