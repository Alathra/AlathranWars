package io.github.alathra.alathranwars.conflict.war.side;

import io.github.alathra.alathranwars.conflict.IAssociatedWar;
import io.github.alathra.alathranwars.conflict.IUnique;
import io.github.alathra.alathranwars.conflict.IUpdateable;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.spawn.RallyPoint;
import io.github.alathra.alathranwars.conflict.war.side.spawn.SpawnCache;
import io.github.alathra.alathranwars.enums.battle.BattleSide;
import io.github.alathra.alathranwars.enums.battle.BattleTeam;
import io.github.alathra.alathranwars.hook.NameColorHandler;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Side extends AbstractSideTeamManager implements IUnique<Side>, IAssociatedWar, IUpdateable {
    private static final Duration SIEGE_COOLDOWN = Duration.ofMinutes(15);
    private static final Duration RAID_COOLDOWN = Duration.ofMinutes(15);
    private final UUID warUUID;
    private final UUID uuid;
    private final BattleSide side;
    private final BattleTeam team;
    private final String name; // A town name

    private final Town town; // The initiating town or capital of the nation
    private Instant siegeGrace = Instant.now().minus(SIEGE_COOLDOWN); // The time after which this side can be besieged
    private Instant raidGrace = Instant.now().minus(RAID_COOLDOWN); // The time after which this side can be raided

    private int score = 0;
    private final SpawnCache spawnCache;

    /**
     * Instantiates a existing Side.
     * @throws SideCreationException exception
     */
    @ApiStatus.Internal
    Side(
        UUID warUUID,
        UUID uuid,
        Government government,
        BattleSide side,
        BattleTeam team,
        String name,
        Set<Nation> nations,
        Set<Town> towns,
        Set<OfflinePlayer> players,
        Set<Nation> nationsSurrendered,
        Set<Town> townsSurrendered,
        Set<OfflinePlayer> playersSurrendered,
        Instant siegeGrace,
        Instant raidGrace,
        List<RallyPoint> spawns
    ) throws SideCreationException {
        super(nations, towns, players, nationsSurrendered, townsSurrendered, playersSurrendered);
        this.warUUID = warUUID;
        this.uuid = uuid;
        this.side = side;
        this.team = team;
        this.name = name;
        this.siegeGrace = siegeGrace;
        this.raidGrace = raidGrace;

        if (government instanceof Nation nation) {
            this.town = nation.getCapital();
        } else if (government instanceof Town town2) {
            this.town = town2;
        } else {
            throw new SideCreationException("No town or nation specified!");
        }

        this.spawnCache = new SpawnCache(this, spawns);
    }

    /**
     * Instantiates a new Side.
     * @throws SideCreationException exception
     */
    @ApiStatus.Internal
    Side(
        UUID warUUID,
        UUID uuid,
        Government government,
        BattleSide side,
        BattleTeam team
    ) throws SideCreationException {
        super();
        this.warUUID = warUUID;
        this.uuid = uuid;
        this.side = side;
        this.team = team;

        if (government instanceof Nation nation) {
            this.town = nation.getCapital();
            add(nation);
        } else if (government instanceof Town town2) {
            this.town = town2;
            add(town2);
        } else {
            throw new SideCreationException("No town or nation specified!");
        }

        this.name = this.town.getName();
        this.spawnCache = new SpawnCache(this);
    }

    public BattleSide getSide() {
        return side;
    }

    public BattleTeam getTeam() {
        return team;
    }

    public String getName() {
        return name;
    }

    // Participant management

    public void applyNameTags() {
        getPlayersOnlineAll().forEach(
            player -> NameColorHandler.getInstance().calculatePlayerColors(player)
        );
    }

    // Graces

    public Instant getSiegeGrace() {
        return siegeGrace;
    }

    public void setSiegeGrace() {
        siegeGrace = Instant.now().plus(SIEGE_COOLDOWN);
    }

    public boolean isSiegeGraceActive() {
        return siegeGrace.isAfter(Instant.now());
    }

    public Duration getSiegeGraceCooldown() {
        return Duration.between(Instant.now(), siegeGrace);
    }

    public Instant getRaidGrace() {
        return raidGrace;
    }

    public void setRaidGrace() {
        raidGrace = Instant.now().plus(RAID_COOLDOWN);
    }

    public boolean isRaidGraceActive() {
        return raidGrace.isAfter(Instant.now());
    }

    public Duration getRaidGraceCooldown() {
        return Duration.between(Instant.now(), this.raidGrace);
    }

    // Score

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int add) {
        this.score += add;
    }

    // Comparators & UUID

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(UUID uuid) {
        return getUUID().equals(uuid);
    }

    public boolean equals(Side side) {
        return getUUID().equals(side.getUUID());
    }

    public boolean equals(String sideName) {
        return this.name.equals(sideName);
    }

    // Associated war

    @Override
    public boolean equals(War war) {
        return this.warUUID.equals(war.getUUID());
    }

    @Override
    public War getWar() {
        return WarController.getInstance().getWar(warUUID);
    }

    public void processSurrenders() {
        if (shouldSurrender())
            surrenderWar();
    }

    private void surrenderWar() {
        War war = getWar();
        if (war != null)
            getWar().defeat(this);
    }

    public Town getTown() {
        return town;
    }

    // Player management

    @Override
    public void add(OfflinePlayer p) {
        super.add(p);

        // Check if war exists yet, (it does not during war creation)
        War war = getWar();
        if (war == null)
            return;

        // Add player to battles
        /*war.getSieges().forEach(siege -> {
            if (siege.getAttackerSide().equals(this)) {
                siege.addPlayer(p, BattleSide.ATTACKER);
            } else if (siege.getDefenderSide().equals(this)) {
                siege.addPlayer(p, BattleSide.DEFENDER);
            }
        });*/
        // TODO Raids
        /*war.getRaids().forEach(raid -> {
            if (raid.getAttackerSide().equals(this)) {
                raid.addPlayer(p, BattleSide.ATTACKER);
            } else if (raid.getDefenderSide().equals(this)) {
                raid.addPlayer(p, BattleSide.DEFENDER);
            }
        });*/
    }

    @Override
    public void remove(OfflinePlayer p) {
        super.remove(p);

        // Check if war exists yet, (it does not during war creation)
        War war = getWar();
        if (war == null)
            return;

        // Remove player from battles
        /*war.getSieges().forEach(siege -> {
            if (siege.getAttackerSide().equals(this)) {
                siege.removePlayer(p, BattleSide.ATTACKER);
            } else if (siege.getDefenderSide().equals(this)) {
                siege.removePlayer(p, BattleSide.DEFENDER);
            }
        });*/
        // TODO Raids
        /*war.getRaids().forEach(raid -> {
            if (raid.getAttackerSide().equals(this)) {
                raid.removePlayer(p, BattleSide.ATTACKER);
            } else if (raid.getDefenderSide().equals(this)) {
                raid.removePlayer(p, BattleSide.DEFENDER);
            }
        });*/
    }

    // Spawning

    public SpawnCache getSpawnManager() {
        return spawnCache;
    }

    // Misc

    @Override
    public void update() {
        getSpawnManager().update();
    }

    public boolean isAttacker() {
        if (getWar() == null)
            return false;
        return getWar().getAttacker().equals(this);
    }

    public boolean isDefender() {
        if (getWar() == null)
            return false;
        return getWar().getDefender().equals(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private @Nullable UUID warUUID;
        private @Nullable UUID uuid;
        private @Nullable BattleSide side;
        private @Nullable BattleTeam team;
        private @Nullable String name;

        private @Nullable Government government;

        private @Nullable Set<Nation> nations;
        private @Nullable Set<Town> towns;
        private @Nullable Set<OfflinePlayer> players;

        private @Nullable Set<Nation> nationsSurrendered;
        private @Nullable Set<Town> townsSurrendered;
        private @Nullable Set<OfflinePlayer> playersSurrendered;

        private @Nullable Instant siegeGrace;
        private @Nullable Instant raidGrace;

        private @Nullable List<RallyPoint> rallyPoints;

        private Builder() {
        }

        /**
         * Build a new side from save data
         * @return a new side
         * @throws SideCreationException exception
         */
        public Side rebuild() throws SideCreationException {
            if (warUUID == null)
                throw new SideCreationException("Missing state warUUID required to create Side!");

            if (uuid == null)
                throw new SideCreationException("Missing state uuid required to create Side!");

            if (government == null)
                throw new SideCreationException("Missing state government required to create Side!");

            if (side == null)
                throw new SideCreationException("Missing state side required to create Side!");

            if (team == null)
                throw new SideCreationException("Missing state team required to create Side!");

            if (name == null)
                throw new SideCreationException("Missing state name required to create Side!");

            if (towns == null)
                throw new SideCreationException("Missing state towns required to create Side!");

            if (nations == null)
                throw new SideCreationException("Missing state nations required to create Side!");

            if (players == null)
                throw new SideCreationException("Missing state players required to create Side!");

            if (townsSurrendered == null)
                throw new SideCreationException("Missing state townsSurrendered required to create Side!");

            if (nationsSurrendered == null)
                throw new SideCreationException("Missing state nationsSurrendered required to create Side!");

            if (playersSurrendered == null)
                throw new SideCreationException("Missing state playersSurrendered required to create Side!");

            if (siegeGrace == null)
                throw new SideCreationException("Missing state siegeGrace required to create Side!");

            if (raidGrace == null)
                throw new SideCreationException("Missing state raidGrace required to create Side!");

            if (rallyPoints == null)
                throw new SideCreationException("Missing state spawns required to create Side!");

            return new Side(
                warUUID,
                uuid,
                government,
                side,
                team,
                name,
                nations,
                towns,
                players,
                nationsSurrendered,
                townsSurrendered,
                playersSurrendered,
                siegeGrace,
                raidGrace,
                rallyPoints
            );
        }

        /**
         * Build a new Side
         * @return a new side
         * @throws SideCreationException exception
         */
        public Side build() throws SideCreationException {
            if (warUUID == null)
                throw new SideCreationException("Missing state warUUID required to create Side!");

            if (uuid == null)
                throw new SideCreationException("Missing state uuid required to create Side!");

            if (government == null)
                throw new SideCreationException("Missing state government required to create Side!");

            if (side == null)
                throw new SideCreationException("Missing state side required to create Side!");

            if (team == null)
                throw new SideCreationException("Missing state team required to create Side!");

            return new Side(
                warUUID,
                uuid,
                government,
                side,
                team
            );
        }

        public Builder setWarUUID(UUID warUUID) {
            this.warUUID = warUUID;
            return this;
        }

        public Builder setUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder setSide(BattleSide side) {
            this.side = side;
            return this;
        }

        public Builder setTeam(BattleTeam team) {
            this.team = team;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setLeader(Government target) {
            this.government = target;
            return this;
        }

        public Builder setNations(Set<Nation> nations) {
            this.nations = nations;
            return this;
        }

        public Builder setTowns(Set<Town> towns) {
            this.towns = towns;
            return this;
        }

        public Builder setPlayers(@NotNull Set<UUID> players) {
            this.players = players.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
            return this;
        }

        public Builder setNationsSurrendered(Set<Nation> nationsSurrendered) {
            this.nationsSurrendered = nationsSurrendered;
            return this;
        }

        public Builder setTownsSurrendered(Set<Town> townsSurrendered) {
            this.townsSurrendered = townsSurrendered;
            return this;
        }

        public Builder setPlayersSurrendered(@NotNull Set<UUID> playersSurrendered) {
            this.playersSurrendered = playersSurrendered.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
            return this;
        }

        public Builder setSiegeGrace(Instant siegeGrace) {
            this.siegeGrace = siegeGrace;
            return this;
        }

        public Builder setRaidGrace(Instant raidGrace) {
            this.raidGrace = raidGrace;
            return this;
        }

        public Builder setRallies(@Nullable List<RallyPoint> spawns) {
            this.rallyPoints = spawns;
            return this;
        }
    }
}
