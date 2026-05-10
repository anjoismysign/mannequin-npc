package io.github.anjoismysign.mannequin.npc.manager;

import io.github.anjoismysign.mannequin.npc.MannequinNPC;
import io.github.anjoismysign.mannequin.npc.configuration.MannequinConfiguration;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ConfigurationManager {
    private final MannequinNPC plugin;
    private MannequinConfiguration configuration;

    public ConfigurationManager(MannequinNPC plugin){
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File pluginDataFolder = plugin.getDataFolder();
        plugin.saveResource("config.yml", false);
        File configurationFile = new File(pluginDataFolder, "config.yml");
        Constructor constructor = new Constructor(MannequinConfiguration.class, new LoaderOptions());
        Yaml yaml = new Yaml(constructor);
        try (FileInputStream inputStream = new FileInputStream(configurationFile)) {
            configuration = yaml.load(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }


}
