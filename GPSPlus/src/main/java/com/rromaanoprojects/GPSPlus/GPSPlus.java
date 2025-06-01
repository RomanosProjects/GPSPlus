package com.rromaanoprojects.GPSPlus;

import com.rromaanoprojects.GPSPlus.GPSPlusCommand;
import com.rromaanoprojects.GPSPlus.NavigationManager;
import com.rromaanoprojects.GPSPlus.NodeManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class GPSPlus extends JavaPlugin {
    private static GPSPlus instance;
    private Logger logger;
    private NodeManager nodeManager;
    private NavigationManager navigationManager;
    private File nodesFile;
    private FileConfiguration nodesConfig;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        // Create plugin directory if not exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        // Initialize config files
        nodesFile = new File(getDataFolder(), "nodes.yml");
        if (!nodesFile.exists()) {
            try {
                nodesFile.createNewFile();
            } catch (IOException e) {
                logger.severe("Errore nella creazione di nodes.yml: " + e.getMessage());
            }
        }
        nodesConfig = YamlConfiguration.loadConfiguration(nodesFile);

        // Initialize managers
        nodeManager = new NodeManager(this);
        navigationManager = new NavigationManager(this);

        // Register commands
        getCommand("gpsplus").setExecutor(new GPSPlusCommand(this));
        getCommand("gpsplus").setTabCompleter(new GPSPlusCommand(this));

        logger.info("GPSPlus abilitato!");
    }

    @Override
    public void onDisable() {
        // Save all data
        saveNodesConfig();
        logger.info("GPSPlus disabilitato!");
    }

    public void saveNodesConfig() {
        try {
            nodesConfig.save(nodesFile);
        } catch (IOException e) {
            logger.severe("Errore nel salvataggio di nodes.yml: " + e.getMessage());
        }
    }

    public static GPSPlus getInstance() {
        return instance;
    }

    public NodeManager getNodeManager() {
        return nodeManager;
    }

    public NavigationManager getNavigationManager() {
        return navigationManager;
    }

    public FileConfiguration getNodesConfig() {
        return nodesConfig;
    }
}