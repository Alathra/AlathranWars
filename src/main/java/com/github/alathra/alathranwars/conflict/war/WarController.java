package com.github.alathra.alathranwars.conflict.war;

import com.github.alathra.alathranwars.conflict.battle.raid.Raid;
import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.db.DatabaseQueries;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The singleton War controller.
 */
@Singleton
public class WarController {
    private static WarController instance;
    private @NotNull Set<War> wars = new HashSet<>();

    private WarController() {
        if (instance != null)
            Bukkit.getServer().getLogger().warning("Tried to re-initialize singleton");
    }

    /**
     * Gets or creates an instance of the war controller.
     *
     * @return the instance
     */
    @NotNull
    public static WarController getInstance() {
        if (instance == null)
            instance = new WarController();

        return instance;
    }

    /**
     * Load all wars into memory from database.
     */
    @ApiStatus.Internal
    public void loadAll() {
        wars = DatabaseQueries.loadAll();
    }

    /**
     * Gets all wars.
     *
     * @return the wars
     */
    @NotNull
    public Set<War> getWars() {
        return wars;
    }

    /**
     * Gets war by its name or returns null.
     *
     * @param warName the war name
     * @return the war
     */
    @Nullable
    public War getWar(String warName) {
        return getWars().stream()
            .filter(war -> war.equals(warName))
            .findAny()
            .orElse(null);
    }

    /**
     * Gets war by its UUID or returns null.
     *
     * @param uuid the uuid
     * @return the war
     */
    @Nullable
    public War getWar(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.equals(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Gets side by the side UUID.
     *
     * @param uuid the uuid
     * @return the side
     */
    @Nullable
    public Side getSide(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.getSide(uuid) != null)
            .map(war -> war.getSide(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Gets siege by the siege UUID.
     *
     * @param uuid the uuid
     * @return the siege
     */
    @Nullable
    public Siege getSiege(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.getSiege(uuid) != null)
            .map(war -> war.getSiege(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Add a war to the war controller.
     *
     * @param war the war
     */
    @ApiStatus.Internal
    public void addWar(War war) {
        getWars().add(war);
    }

    /**
     * Remove a war from the war controller.
     *
     * @param war the war
     */
    @ApiStatus.Internal
    public void removeWar(War war) {
        getWars().remove(war);
    }

    /**
     * Is player in any wars. Includes if surrendered.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean isInAnyWars(UUID uuid) {
        return getWars().stream()
            .anyMatch(war -> war.isInWar(uuid));
    }

    /**
     * Is player in any wars. Includes if surrendered.
     *
     * @param p the p
     * @return the boolean
     */
    public boolean isInAnyWars(Player p) {
        return isInAnyWars(p.getUniqueId());
    }

    /**
     * Is nation/town in any wars. Includes surrendered nations/towns.
     *
     * @param government the nation/town
     * @return the boolean
     */
    public boolean isInAnyWars(Government government) {
        if (government instanceof Nation nation) {
            return getWars().stream()
                .anyMatch(war -> war.isInWar(nation));
        } else if (government instanceof Town town) {
            return getWars().stream()
                .anyMatch(war -> war.isInWar(town));
        }
        return false;
    }

    /**
     * Gets list of player wars. Includes if surrendered.
     *
     * @param uuid the uuid
     * @return the player wars
     */
    public @NotNull Set<War> getWars(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.isInWar(uuid))
            .collect(Collectors.toSet());
    }

    /**
     * Gets list of player wars. Includes if surrendered.
     *
     * @param p the p
     * @return the player wars
     */
    public @NotNull Set<War> getWars(Player p) {
        return getWars(p.getUniqueId());
    }

    /**
     * Gets nation/town wars. Includes surrendered nations/towns.
     *
     * @param government the nation/town
     * @return the town wars
     */
    public @NotNull Set<War> getWars(Government government) {
        if (government instanceof Nation nation) {
            return getWars().stream()
                .filter(war -> war.isInWar(nation))
                .collect(Collectors.toSet());
        } else if (government instanceof Town town) {
            return getWars().stream()
                .filter(war -> war.isInWar(town))
                .collect(Collectors.toSet());
        }
        return Set.of();
    }

    /**
     * Is player in any siege. Returns true for offline players.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean isInAnySiege(UUID uuid) {
        return getSieges().stream()
            .anyMatch(siege -> siege.isPlayerInSiege(uuid));
    }

    /**
     * Is player in any siege.
     *
     * @param p the p
     * @return the boolean
     */
    public boolean isInAnySiege(Player p) {
        return isInAnySiege(p.getUniqueId());
    }

    /**
     * Is nation/town in any sieges. Includes surrendered nations/towns.
     *
     * @param government the nation/town
     * @return the boolean
     */
    public boolean isInAnySieges(Government government) {
        if (government instanceof Nation nation) {
            return nation.getTowns().stream()
                .anyMatch(this::isInAnySieges);
        } else if (government instanceof Town town) {
            return getWars().stream()
                .anyMatch(war -> war.isInWar(town) && war.isTownUnderSiege(town));
        }
        return false;
    }

    /**
     * Is nation/town in any raids. Includes surrendered nations/towns.
     *
     * @param government the nation/town
     * @return the boolean
     */
    public boolean isInAnyRaids(Government government) {
        if (government instanceof Nation nation) {
            return nation.getTowns().stream()
                .anyMatch(this::isInAnyRaids);
        } else if (government instanceof Town town) {
            return getWars().stream()
                .anyMatch(war -> war.isInWar(town) && war.isTownUnderRaid(town));
        }
        return false;
    }

    /**
     * Gets a list of all active sieges.
     *
     * @return the sieges
     */
    @NotNull
    public Set<Siege> getSieges() {
        return getWars().stream()
            .map(War::getSieges)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Gets player sieges. Returns true for offline players.
     *
     * @param uuid the uuid
     * @return the player sieges
     */
    public @NotNull Set<Siege> getSieges(UUID uuid) {
        return getSieges().stream()
            .filter(siege -> siege.isPlayerInSiege(uuid))
            .collect(Collectors.toSet());
    }

    /**
     * Gets player sieges.
     *
     * @param p the p
     * @return the player sieges
     */
    public @NotNull Set<Siege> getSieges(Player p) {
        return getSieges(p.getUniqueId());
    }

    /**
     * Gets nation/town sieges. Includes surrendered nations/towns.
     *
     * @param government the nation/town
     * @return the nation/town sieges
     */
    public @NotNull Set<Siege> getSieges(Government government) {
        if (government instanceof Nation nation) {
            return getSieges().stream()
                .filter(siege -> siege.getAttackerSide().isOnSide(nation) || siege.getDefenderSide().isOnSide(nation))
                .collect(Collectors.toSet());
        } else if (government instanceof Town town) {
            return getSieges().stream()
                .filter(siege -> siege.getAttackerSide().isOnSide(town) || siege.getDefenderSide().isOnSide(town))
                .collect(Collectors.toSet());
        }
        return Set.of();
    }

    /**
     * Gets a list of all active raids.
     *
     * @return the raids
     */
    @NotNull
    public Set<Raid> getRaids() {
        return getWars().stream()
            .map(War::getRaids)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Gets nation/town raids. Includes surrendered nations/towns.
     *
     * @param government the nation/town
     * @return the nation/town raids
     */
    public @NotNull Set<Raid> getRaids(Government government) {
        if (government instanceof Nation nation) {
            return getRaids().stream()
                .filter(raid -> raid.getAttackerSide().isOnSide(nation) || raid.getDefenderSide().isOnSide(nation))
                .collect(Collectors.toSet());
        } else if (government instanceof Town town) {
            return getRaids().stream()
                .filter(raid -> raid.getAttackerSide().isOnSide(town) || raid.getDefenderSide().isOnSide(town))
                .collect(Collectors.toSet());
        }
        return Set.of();
    }

    /**
     * Gets a list of war names.
     *
     * @return the war names
     */
    @NotNull
    public List<String> getWarNames() {
        return getWars().stream()
            .map(War::getName)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    /**
     * Gets a list of war labels.
     *
     * @return the war labels
     */
    @NotNull
    public List<String> getWarLabels() {
        return getWars().stream()
            .map(War::getLabel)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    /**
     * Gets nations at war. Does not include surrendered nations.
     *
     * @return the nations at war
     */
    public @NotNull Set<Nation> getNationsAtWar() {
        return getWars().stream()
            .map(War::getNations)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Gets towns at war. Does not include surrendered towns.
     *
     * @return the towns at war
     */
    public @NotNull Set<Town> getTownsAtWar() {
        return getWars().stream()
            .map(War::getTowns)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }
}
