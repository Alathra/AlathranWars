package com.github.alathra.alathranwars.conflict.war;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.Reloadable;
import com.github.alathra.alathranwars.conflict.war.side.spawn.Spawn;
import com.github.alathra.alathranwars.utility.SpawnUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import javax.inject.Singleton;

/**
 * The singleton Spawn controller.
 */
@Singleton
public class SpawnController implements Reloadable {
    private static SpawnController instance;

    private SpawnController() {
        if (instance != null)
            Bukkit.getServer().getLogger().warning("Tried to re-initialize singleton");
    }

    /**
     * Gets or creates an instance of the spawn controller.
     *
     * @return the instance
     */
    public static SpawnController getInstance() {
        if (instance == null)
            instance = new SpawnController();

        return instance;
    }

    /**
     * Load all data into memory from database.
     */
    @ApiStatus.Internal
    public void loadAll() {

    }

    private ScheduledTask task;

    @Override
    public void onLoad(AlathranWars plugin) {

    }

    @Override
    public void onEnable(AlathranWars plugin) {
        task = AlathranWars.getPaperLib().scheduling().globalRegionalScheduler().runAtFixedRate(() -> SpawnUtils.getSpawns().forEach(Spawn::update), 0L, 1);
    }

    @Override
    public void onDisable(AlathranWars plugin) {
        task.cancel();
    }
}
