package io.github.alathra.alathranwars.deathspectate;

import io.github.milkdrinkers.colorparser.paper.ColorParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DeathConfig {
    private static final Duration respawnTime = Duration.ofSeconds(15);
    private static final Set<World> whitelistedWorlds = new HashSet<>();
    private static final Set<String> whitelistedCommands = new HashSet<>();
    private static final Set<EntityDamageEvent.DamageCause> blacklistedDamageCauses = new HashSet<>();
    private static final boolean usePermissionForSpectating = false;
    private static final boolean respawnWithServerDefaultGamemode = true;

    private static final String DEATH_COMMAND_DENIED = "";
    private static final String DEATH_MESSAGE = "";

    static {
        for (String worldName : List.of("World-O", "World-O_nether", "World-O_the_end")) {
            final World world = Bukkit.getServer().getWorld(worldName);
            if (world != null)
                whitelistedWorlds.add(world);
        }
    }

    public static String getDeathTitle() {
        return "<red>Wasted";
    }

    public static String getDeathSubTitle() {
        return "<gray>You died <time>";
    }

    public static Duration getRespawnTime() {
        return respawnTime;
    }

    public static boolean isWhitelistedCommand(String command) {
        return whitelistedCommands.contains(command);
    }

    public static boolean isAllowedToUseAnyCommand(Player player) {
        return player.hasPermission("core.death.commands");
    }

    public static String getCommandDeniedMessage() {
        return DEATH_COMMAND_DENIED;
    }

    public static String getYouDiedMessage() {
        return DEATH_MESSAGE;
    }

    public static boolean canSpectate(Player player, EntityDamageEvent.DamageCause damageCause) {
        return damageCause != null && isWhitelistedWorld(player.getWorld()) && !blacklistedDamageCauses.contains(damageCause) && hasPermissionToSpectate(player);
    }

    public static Component formatter(String stringToFormat, Object... formats) {
        return formatter(MessageFormat.format(stringToFormat, formats));
    }

    public static Component formatter(String stringToFormat) {
        return ColorParser.of(stringToFormat).legacy().build();
    }

    public static GameMode gameModeToRespawnWith() {
        if (respawnWithServerDefaultGamemode)
            return Bukkit.getServer().getDefaultGameMode();
        return null;
    }

    private static boolean isWhitelistedWorld(World world) {
        return whitelistedWorlds.isEmpty() || whitelistedWorlds.contains(world);
    }

    private static boolean hasPermissionToSpectate(Player player) {
        return !usePermissionForSpectating || player.hasPermission("alathranwars.death.spectate");
    }
}
