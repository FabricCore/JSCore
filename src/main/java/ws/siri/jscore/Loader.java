package ws.siri.jscore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

public class Loader {
    public static void init() {
        Path initPath = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID).resolve("init.js");
        createFileIfNotExist(initPath);

        try {
            Core.eval(readFile(initPath), CatchMode.THROW, initPath.toString());
        } catch (Exception e) {
            Core.LOGGER.error("YOUR INIT.JS IS BOGUS");
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void createDirIfNotExist(Path path) {
        if(!Files.exists(path)) createDir(path);
    }

    public static void createDir(Path path) {
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void createFileIfNotExist(Path path) {
        if(!Files.exists(path.getParent())) createDir(path.getParent());
        if(!Files.exists(path)) createFile(path);
    }

    public static void createFile(Path path) {
        try {
            Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String readFile(Path path) throws IOException {
        return Files.readString(path);
    }
}
