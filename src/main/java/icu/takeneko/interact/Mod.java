package icu.takeneko.interact;

import com.mojang.logging.LogUtils;
import icu.takeneko.interact.network.NekoService;
import icu.takeneko.interact.os.OperatingSystem;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Mod implements DedicatedServerModInitializer {

    public static Path privateDir = Path.of("./.nekoInteractApi");
    public static boolean hasMCDR = false;
    private final Logger logger = LogUtils.getLogger();

    @Override
    public void onInitializeServer() {
        logger.info("Hello World!");
        Config.INSTANCE.load();
        if (!privateDir.toFile().exists()){
            try {
                Files.createDirectory(privateDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        hasMCDR = OperatingSystem.Companion.getCurrent().walkProcessGroup().values().stream().anyMatch(it -> it.contains("python") || it.contains("mcdreforged"));
        if (!hasMCDR){
            logger.error("No MCDReforged configured in current environment, NekoInteractAPI will be unavailable!");
        }else {
            setUpServices();
        }
    }

    private void setUpServices(){
        NekoService.INSTANCE.start();
    }
}
