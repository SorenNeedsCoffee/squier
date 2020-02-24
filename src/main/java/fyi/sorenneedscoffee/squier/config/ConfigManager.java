package fyi.sorenneedscoffee.squier.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {
    private static final Yaml yaml = new Yaml();
    private static final Logger log = LoggerFactory.getLogger("Config");

    public static Config load() {
        Config config = null;
        try {
            InputStream in = Files.newInputStream(Paths.get("config.yml"));
            config = yaml.loadAs(in, Config.class);
        } catch (IOException e) {
            log.error("Config file not found. Expected: config.yml. Is the file named correctly?");
            System.exit(1);
        }

        return config;
    }
}
