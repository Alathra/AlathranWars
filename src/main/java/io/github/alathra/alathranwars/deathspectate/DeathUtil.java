package io.github.alathra.alathranwars.deathspectate;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.deathspectate.event.DeathSpectatingEvent;
import io.github.alathra.alathranwars.deathspectate.task.SpectateTask;
import io.github.alathra.alathranwars.utility.Logger;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DeathUtil {
    public final static String META_DEAD = "DEAD";
    public final static String META_KEY_GAMEMODE = "DEAD_PREVIOUS_GAMEMODE";
    public final static String META_KEY_FLYSPEED = "DEAD_PREVIOUS_FLY_SPEED";
    public final static String META_AWAITING_RESPAWN = "DEAD_RESPAWN";
    public final static String META_TELEPORTING = "DEAD_TP";

    private final static String PDC_PREFIX = "alathran_wars_death";
    public final static NamespacedKey PDC_IS_DEAD = NamespacedKey.fromString(PDC_PREFIX + "_is_dead");
    public final static NamespacedKey PDC_RESPAWN_TIMER = NamespacedKey.fromString(PDC_PREFIX + "_respawn_timer");

    public static boolean isSpectating(Player p) {
        return p.getGameMode().equals(GameMode.SPECTATOR) && p.hasMetadata(META_DEAD);
    }

    private static void addSpectator(Player p, GameMode gameMode, Duration respawnTime) throws StartSpectateException {
        p.setGameMode(GameMode.SPECTATOR);
        if (p.getGameMode() != GameMode.SPECTATOR) {
            Logger.get().warn(ColorParser.of("Another plugin prevented the player from entering the spectator gamemode!").build());
            throw new StartSpectateException();
        }

        final AlathranWars instance = AlathranWars.getInstance();
        p.setMetadata(META_DEAD, new FixedMetadataValue(instance, gameMode));
        p.setMetadata(META_KEY_GAMEMODE, new FixedMetadataValue(instance, gameMode));
        p.setMetadata(META_KEY_FLYSPEED, new FixedMetadataValue(instance, p.getFlySpeed()));
        p.setFlySpeed(0.0f);

        try {
            final PersistentDataContainer pdc = p.getPersistentDataContainer();

            assert PDC_RESPAWN_TIMER != null;
            assert PDC_IS_DEAD != null;

            pdc.set(PDC_IS_DEAD, PersistentDataType.BOOLEAN, true);
            pdc.set(PDC_RESPAWN_TIMER, PersistentDataType.LONG, respawnTime.getSeconds());
        } catch (Exception ignored) {
        }
    }

    public static void removeSpectator(Player p, final @Nullable GameMode defaultGameMode) {
        final AlathranWars instance = AlathranWars.getInstance();

        final GameMode gameMode = Objects.requireNonNullElseGet(defaultGameMode, () -> instance.getServer().getDefaultGameMode());
        try {
            // Set game mode from meta
            if (defaultGameMode == null && p.hasMetadata(META_DEAD)) {
                p.setGameMode((GameMode) Objects.requireNonNull(p.getMetadata(META_DEAD).get(0).value()));
                return;
            }

            p.setGameMode(gameMode);
        } catch (Exception e) {
            p.setGameMode(gameMode);
        } finally {
            p.removeMetadata(META_KEY_GAMEMODE, instance);
        }

        try {
            final float previousFlySpeed = p.getMetadata(META_KEY_FLYSPEED).get(0).asFloat();
            p.setFlySpeed(previousFlySpeed);
        } catch (Exception e) {
            p.setFlySpeed(1.0f);
        } finally {
            p.removeMetadata(META_KEY_FLYSPEED, instance);
        }

        try {
            final PersistentDataContainer pdc = p.getPersistentDataContainer();

            assert PDC_RESPAWN_TIMER != null;
            assert PDC_IS_DEAD != null;

            pdc.remove(PDC_RESPAWN_TIMER);
            pdc.remove(PDC_IS_DEAD);
        } catch (Exception ignored) {
        }

        p.removeMetadata(META_DEAD, instance);
    }

    public static void respawnPlayer(Player p) {
        if (!isSpectating(p) || p.isDead())
            return;

        Location spawnLocation = p.getBedSpawnLocation();
        boolean isBedSpawn = true;
        if (spawnLocation == null) {
            spawnLocation = AlathranWars.getInstance().getServer().getWorlds().get(0).getSpawnLocation();
            isBedSpawn = false;
        }

        p.setMetadata(META_AWAITING_RESPAWN, new FixedMetadataValue(AlathranWars.getInstance(), true));

        final PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(p, spawnLocation, isBedSpawn, false, false, PlayerRespawnEvent.RespawnReason.DEATH);
        respawnEvent.callEvent();

        try {
            final AttributeInstance maxHealth = p.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null)
                p.setHealth(maxHealth.getValue());

            p.setFireTicks(0);
            p.setFallDistance(0);
            p.setFoodLevel(20);
            p.setSaturation(5f);
            p.setExhaustion(0);
            p.setTicksLived(1);
            p.setArrowsInBody(0, false);
            p.clearActivePotionEffects();
            p.closeInventory();
            p.setLastDamageCause(null);
        } catch (Throwable ignored) {
        }

        p.teleportAsync(respawnEvent.getRespawnLocation(), PlayerTeleportEvent.TeleportCause.UNKNOWN).thenAccept(success -> {
            removeSpectator(p, DeathConfig.gameModeToRespawnWith());
            p.removeMetadata(META_AWAITING_RESPAWN, AlathranWars.getInstance());
        });
    }

    public static boolean startDeathSpectating(Player player, PlayerDeathEvent deathEvent) {
        if (isSpectating(player))
            return false;

        final AlathranWars plugin = AlathranWars.getInstance();
        final Duration respawnTime = DeathConfig.getRespawnTime();

        try {
            final World world = player.getWorld();

            final boolean keepInventory = deathEvent.getKeepInventory() || player.getGameMode().equals(GameMode.SPECTATOR);
            final boolean showDeathMessages = Boolean.TRUE.equals(world.getGameRuleValue(GameRule.SHOW_DEATH_MESSAGES));

            try {
                addSpectator(player, player.getGameMode(), respawnTime);
            } catch (StartSpectateException e) {
                return false;
            }

            final Component deathMessage = deathEvent.deathMessage();
            if (deathMessage != null && !deathMessage.compact().equals(Component.empty().compact()) && showDeathMessages)
                plugin.getServer().broadcast(deathMessage);

            // Drop items
            if (!keepInventory) {
                player.getInventory().clear();
                for (ItemStack itemStack : deathEvent.getDrops()) {
                    if (itemStack != null && !itemStack.getType().equals(Material.AIR))
                        world.dropItemNaturally(player.getLocation(), itemStack);
                }

                if (!deathEvent.getKeepLevel()) {
                    player.setTotalExperience(deathEvent.getNewTotalExp());
                    player.setLevel(deathEvent.getNewLevel());
                    player.setExp(deathEvent.getNewExp());
                }
            }

            player.closeInventory();
            player.setSpectatorTarget(null);

            // Drop experience
            if (deathEvent.getDroppedExp() > 0)
                world.spawn(player.getLocation(), ExperienceOrb.class).setExperience(deathEvent.getDroppedExp());

            // Increment/reset death statistics
            player.incrementStatistic(Statistic.DEATHS);
            player.setStatistic(Statistic.TIME_SINCE_DEATH, 0);

            player.clearActivePotionEffects();

            // Play death sound
            final Set<Player> players = new HashSet<>(world.getNearbyPlayers(player.getLocation(), 50));
            players.remove(player);

            final net.kyori.adventure.sound.Sound deathSound = net.kyori.adventure.sound.Sound.sound()
                .type(Key.key("entity.player.death"))
                .source(net.kyori.adventure.sound.Sound.Source.PLAYER)
                .volume(1.0f)
                .pitch(1.0f)
                .build();
            Audience.audience(players).playSound(deathSound);

            // Determine killer of the player
            Entity killer = player.getKiller();

            if (player.getKiller() == null && player.getLastDamageCause() instanceof EntityDamageByEntityEvent entity) {
                killer = entity.getDamager();

                if ((killer instanceof Projectile projectile) && (projectile.getShooter() instanceof LivingEntity shooter)) {
                    killer = shooter;
                }
            }

            // Don't count suicide
            if (killer == player)
                killer = null;

            // Stats handling
            if (killer != null) {
                try {
                    player.incrementStatistic(Statistic.ENTITY_KILLED_BY, killer.getType());
                } catch (IllegalArgumentException | NullPointerException ignored) {
                }
            }

            if (killer != null && killer.getType().equals(EntityType.PLAYER)) {
                Player playerKiller = (Player) killer;
                playerKiller.incrementStatistic(Statistic.PLAYER_KILLS);
            }

            final SpectateTask task = new SpectateTask(player, respawnTime);
            new DeathSpectatingEvent(task).callEvent();
            task.runTaskTimer(plugin, 1L, 1L);

            if (!DeathConfig.getYouDiedMessage().isEmpty())
                player.sendMessage(DeathConfig.formatter(DeathConfig.getYouDiedMessage()));

            return true;
        } catch (Exception ignored) {
            removeSpectator(player, null);
            return false;
        }
    }
}
