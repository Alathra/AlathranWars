package io.github.alathra.alathranwars.conflict;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.database.Queries;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;

public class SaveHandler implements Reloadable {
    private ScheduledTask autoSaveTask = null;

    @Override
    public void onEnable(AlathranWars plugin) {
        autoSaveTask = plugin.getPaperLib().scheduling().asyncScheduler().runAtFixedRate(Queries::saveAll, Duration.ofMinutes(10), Duration.ofMinutes(10));
    }

    @Override
    public void onDisable(AlathranWars plugin) {
        if (autoSaveTask != null && !autoSaveTask.isCancelled())
            autoSaveTask.cancel();
        autoSaveTask = null;
        Queries.saveAll();
        plugin.getPaperLib().scheduling().cancelGlobalTasks();
    }
}
