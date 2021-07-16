package corgitaco.modid;

import corgitaco.modid.client.GentypeScreen;
import corgitaco.modid.mixin.access.BiomeGeneratorTypeScreenAccess;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "modid";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Path CONFIG_PATH = new File(String.valueOf(FMLPaths.CONFIGDIR.get().resolve(MOD_ID))).toPath();

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(MOD_ID, "chunkgen"), EpicChunkGenerator.CODEC);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        BiomeGeneratorTypeScreenAccess.getPRESETS().add(GentypeScreen.EPIC_CHUNK_GENERATOR);
    }
}
