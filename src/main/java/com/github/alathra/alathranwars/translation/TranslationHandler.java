package com.github.alathra.alathranwars.translation;

import com.github.alathra.alathranwars.config.ConfigHandler;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.Reloadable;
import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.Translation;

import java.nio.file.Path;

/**
 * A class that generates/loads {@literal &} provides access to all translation files.
 */
public class TranslationHandler implements Reloadable {
    private final ConfigHandler configHandler;

    public TranslationHandler(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    /**
     * On plugin load.
     */
    @Override
    public void onLoad(AlathranWars plugin) {
    }

    /**
     * On plugin enable.
     */
    @Override
    public void onEnable(AlathranWars plugin) {
        Translation.initialize(TranslationConfig.builder() // Initialize word-weaver
            .translationDirectory(plugin.getDataPath().resolve("lang"))
            .resourcesDirectory(Path.of("lang"))
            .extractLanguages(true)
            .updateLanguages(true)
            .language(configHandler.getConfig().get("language", "en_US"))
            .defaultLanguage("en_US")
            .componentConverter(s -> ColorParser.of(s).legacy().papi().mini().build()) // Use color parser for components by default
            .build()
        );
    }

    /**
     * On plugin disable.
     */
    @Override
    public void onDisable(AlathranWars plugin) {
    }
}
