package io.github.alathra.alathranwars.data;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import io.github.alathra.alathranwars.data.type.InstantDataField;
import io.github.alathra.alathranwars.mercenary.MercenaryMeta;
import io.github.alathra.alathranwars.utility.Cfg;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class CooldownMeta {
    private static final InstantDataField LAST_OFFENSIVE_WAR_COOLDOWN = new InstantDataField("alathranwars_government_last_attacktime");
    private static final InstantDataField LAST_DEFENSIVE_WAR_COOLDOWN = new InstantDataField("alathranwars_government_last_defensetime");

    private final Duration OFFENSIVE_WAR_COOLDOWN_DURATION;
    private final Duration OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION;
    private final Duration DEFENSIVE_WAR_COOLDOWN_DURATION;

    private static CooldownMeta INSTANCE = null;

    private CooldownMeta() {
        OFFENSIVE_WAR_COOLDOWN_DURATION = Duration.ofDays(Cfg.get().getOrDefault("cooldowns.war.government.offensive.default", 14L));
        OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION = Duration.ofDays(Cfg.get().getOrDefault("cooldowns.war.government.offensive.mercenary", 30L));
        DEFENSIVE_WAR_COOLDOWN_DURATION = Duration.ofDays(Cfg.get().getOrDefault("cooldowns.war.government.defensive.default", 7L));
    }

    public static CooldownMeta getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CooldownMeta();
        return INSTANCE;
    }

    public CooldownType getCooldown(Government government) {
        if (hasAttackCooldown(government)) {
            return CooldownType.OFFENSIVE;
        } else if (hasDefenseCooldown(government)) {
            return CooldownType.DEFENSIVE;
        } else {
            return CooldownType.NONE;
        }
    }

    public boolean hasCooldown(Government government) {
        return switch (getCooldown(government)) {
            case OFFENSIVE, DEFENSIVE -> true;
            case NONE -> false;
        };
    }

    public boolean hasAttackCooldown(Government government) {
        final Instant now = Instant.now();

        final Instant cdStartTime = getOffensiveCooldown(government);
        if (MercenaryMeta.isMercenary(government)) {
            return cdStartTime.plus(OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION).isAfter(now);
        } else {
            return cdStartTime.plus(OFFENSIVE_WAR_COOLDOWN_DURATION).isAfter(now);
        }
    }

    public boolean hasDefenseCooldown(Government government) {
        final Instant now = Instant.now();

        final Instant cdStartTime = getDefensiveCooldown(government);
        return cdStartTime.plus(DEFENSIVE_WAR_COOLDOWN_DURATION).isAfter(now);
    }

    public Instant getOffensiveCooldown(Government government) {
        return Optional.ofNullable(government.getMetadata(getLastOffensiveWarCooldown().getKey(), InstantDataField.class)).map(CustomDataField::getValue).orElse(Instant.EPOCH);
    }

    public Instant getDefensiveCooldown(Government government) {
        return Optional.ofNullable(government.getMetadata(getLastDefensiveWarCooldown().getKey(), InstantDataField.class)).map(CustomDataField::getValue).orElse(Instant.EPOCH);
    }

    public void setOffensiveCooldown(Government government) {
        if (government.hasMeta(getLastOffensiveWarCooldown().getKey())) {
            Objects.requireNonNull(government.getMetadata(getLastOffensiveWarCooldown().getKey(), InstantDataField.class)).setValue(Instant.now());
        } else {
            government.addMetaData(new InstantDataField(getLastOffensiveWarCooldown().getKey()), true);
        }
    }

    public void setDefensiveCooldown(Government government) {
        if (government.hasMeta(getLastDefensiveWarCooldown().getKey())) {
            Objects.requireNonNull(government.getMetadata(getLastDefensiveWarCooldown().getKey(), InstantDataField.class)).setValue(Instant.now());
        } else {
            government.addMetaData(new InstantDataField(getLastDefensiveWarCooldown().getKey()), true);
        }
    }

    public InstantDataField getLastOffensiveWarCooldown() {
        return LAST_OFFENSIVE_WAR_COOLDOWN;
    }

    public InstantDataField getLastDefensiveWarCooldown() {
        return LAST_DEFENSIVE_WAR_COOLDOWN;
    }

    public enum CooldownType {
        OFFENSIVE,
        DEFENSIVE,
        NONE
    }
}
