package io.github.alathra.alathranwars.api;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Resident;
import io.github.alathra.alathranwars.data.CooldownMeta;
import io.github.alathra.alathranwars.data.CooldownResidentMeta;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;

public interface WarCooldownAPI {
    default CooldownMeta.CooldownType getCooldown(Government government) {
        return CooldownMeta.getInstance().getCooldown(government);
    }

    default boolean hasCooldown(Government government) {
        return CooldownMeta.getInstance().hasCooldown(government);
    }

    default boolean hasAttackCooldown(Government government) {
        return CooldownMeta.getInstance().hasAttackCooldown(government);
    }

    default Duration getAttackCooldown(Government government) {
        return Duration.between(Instant.now(), CooldownMeta.getInstance().getOffensiveCooldown(government));
    }

    default void setAttackCooldown(Government government) {
        CooldownMeta.getInstance().setOffensiveCooldown(government);
    }

    default boolean hasDefenseCooldown(Government government) {
        return CooldownMeta.getInstance().hasDefenseCooldown(government);
    }

    default Duration getDefenseCooldown(Government government) {
        return Duration.between(Instant.now(), CooldownMeta.getInstance().getDefensiveCooldown(government));
    }

    default void setDefenseCooldown(Government government) {
        CooldownMeta.getInstance().setDefensiveCooldown(government);
    }

    default CooldownResidentMeta.CooldownType getCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return CooldownResidentMeta.CooldownType.NONE;
        return getCooldown(resident);
    }

    default CooldownResidentMeta.CooldownType getCooldown(Resident resident) {
        return CooldownResidentMeta.getInstance().getCooldown(resident);
    }

    default boolean hasCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return false;
        return hasCooldown(resident);
    }

    default boolean hasCooldown(Resident resident) {
        return CooldownResidentMeta.getInstance().hasCooldown(resident);
    }

    default boolean hasAttackCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return false;
        return hasAttackCooldown(resident);
    }
    default boolean hasAttackCooldown(Resident resident) {
        return CooldownResidentMeta.getInstance().hasAttackCooldown(resident);
    }

    default boolean hasDefenseCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return false;
        return hasDefenseCooldown(resident);
    }

    default boolean hasDefenseCooldown(Resident resident) {
        return CooldownResidentMeta.getInstance().hasDefenseCooldown(resident);
    }

    default Duration getAttackCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !hasAttackCooldown(resident))
            return Duration.ZERO;
        return getAttackCooldown(resident);
    }

    default Duration getAttackCooldown(Resident resident) {
        return Duration.between(Instant.now(), CooldownResidentMeta.getInstance().getOffensiveCooldown(resident));
    }

    default Duration getDefenseCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !hasDefenseCooldown(resident))
            return Duration.ZERO;
        return getDefenseCooldown(resident);
    }

    default Duration getDefenseCooldown(Resident resident) {
        return Duration.between(Instant.now(), CooldownResidentMeta.getInstance().getDefensiveCooldown(resident));
    }

    default void setAttackCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return;
        setAttackCooldown(resident);
    }

    default void setAttackCooldown(Resident resident) {
        CooldownResidentMeta.getInstance().setOffensiveCooldown(resident);
    }

    default void setDefenseCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return;
        setDefenseCooldown(resident);
    }

    default void setDefenseCooldown(Resident resident) {
        CooldownResidentMeta.getInstance().setDefensiveCooldown(resident);
    }
}
