package red.jackf.chesttracker.impl.util;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class Constants {

    // TODO: Make user configurable
    public static final Path STORAGE_DIR = FMLPaths.GAMEDIR.get().resolve("chesttracker");
}
