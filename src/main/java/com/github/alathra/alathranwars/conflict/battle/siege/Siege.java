package com.github.alathra.alathranwars.conflict.battle.siege;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.conflict.battle.AbstractBattleTeamManagement;
import com.github.alathra.alathranwars.conflict.battle.Battle;
import com.github.alathra.alathranwars.conflict.battle.BattleRunnableManager;
import com.github.alathra.alathranwars.conflict.battle.bossbar.BossBarManager;
import com.github.alathra.alathranwars.conflict.battle.phase.BattlePhaseManager;
import com.github.alathra.alathranwars.conflict.battle.progress.BattleProgressManager;
import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.database.DatabaseQueries;
import com.github.alathra.alathranwars.enums.battle.*;
import com.github.alathra.alathranwars.event.battle.*;
import com.github.alathra.alathranwars.packet.CustomLaser;
import com.github.alathra.alathranwars.conflict.battle.LaserManager;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author darksaid98
 */
public class Siege extends AbstractBattleTeamManagement implements Battle {
    public static final Duration SIEGE_DURATION = Duration.ofMinutes(60);
    public static final int MAX_SIEGE_PROGRESS_MINUTES = 8; // How many minutes attackers will need to be on point uncontested for to reach 100%
    public static final int MAX_SIEGE_PROGRESS = 60 * 10 * MAX_SIEGE_PROGRESS_MINUTES; // On reaching this, the attackers win. 10 points is added per second
    //    public static final Duration ATTACKERS_MUST_TOUCH_END = Duration.ofMinutes(40); // If point is not touched in this much time, defenders win
    public static final Duration ATTACKERS_MUST_TOUCH_REVERT = Duration.ofSeconds(60); // If point is not touched in this much time, siege progress begins reverting
    public static final int BATTLEFIELD_RANGE = 500;
    public static final int BATTLEFIELD_START_MAX_RANGE = BATTLEFIELD_RANGE * 2;
    public static final int BATTLEFIELD_START_MIN_RANGE = 75;
    public static final double SIEGE_VICTORY_MONEY = 2500.0;

    // Battle fields
    private final UUID uuid;
    private final static BattleType battleType = BattleType.SIEGE;
    private final War war; // War which siege belongs to

    // Misc fields
    private Instant endTime;
    private Instant lastTouched;
    private Town town; // Town of the siege
    private final boolean isSide1Attackers; // bool of if side1 ia attacker side in this battle (This is cached in its getter)
    private OfflinePlayer siegeLeader;
    private @Nullable Location townSpawn = null;
    private @Nullable Location controlPoint = null;
    private boolean stopped = false; // Used to track if the siege has been already deleted

    // Runnable manager
    private final BattleRunnableManager runnableController = new BattleRunnableManager();
    private final BossBarManager bossBarManager = new BossBarManager();
    private final LaserManager laserManager = new LaserManager();

    // Progress / Phases
    private final BattleProgressManager progressManager; // Represents the capture progress of the capture point in this case
    private final BattlePhaseManager<SiegePhase> phaseManager; // Controls the current stage of the siege

    /**
     * Instantiates a new Siege. Used when creating a new siege.
     *
     * @param war         the war
     * @param town        the town
     * @param siegeLeader the siege leader
     */
    public Siege(
        War war,
        Town town,
        Player siegeLeader
    ) {
        super(
            isSide1Attackers(war, town) ? // Pass attackers side
            war.getSide1() :
            war.getSide2(),
            isSide1Attackers(war, town) ? // Pass defenders side
            war.getSide2() :
            war.getSide1()
        );
        this.uuid = UUID.randomUUID();

        this.endTime = Instant.now().plus(SIEGE_DURATION);
        this.lastTouched = Instant.now();

        this.war = war;
        this.town = town;
        this.siegeLeader = siegeLeader;
        this.progressManager = new BattleProgressManager(0, MAX_SIEGE_PROGRESS);
        this.phaseManager = new BattlePhaseManager<>(SiegePhase.SIEGE, 0, Instant.now());

        this.isSide1Attackers = isSide1Attackers(getWar(), getTown());
    }

    /**
     * Instantiates a new Siege. Used when loading existing Siege from Database.
     *
     * @param war                             the war
     * @param uuid                            the uuid
     * @param town                            the town
     * @param siegeLeader                     the siege leader
     * @param endTime                         the end time
     * @param lastTouched                     the last touched
     * @param siegeProgress                   the siege progress
     * @param attackers the attacker players including offline
     * @param defenders the defender players including offline
     */
    public Siege(
        War war,
        UUID uuid,
        Town town,
        OfflinePlayer siegeLeader,
        Instant endTime,
        Instant lastTouched,
        int siegeProgress,
        Set<UUID> attackers,
        Set<UUID> defenders,
        SiegePhase phase,
        int phaseProgress,
        Instant phaseStartTime
    ) {
        super(
            isSide1Attackers(war, town) ? // Pass attackers side
                war.getSide1() :
                war.getSide2(),
            isSide1Attackers(war, town) ? // Pass defenders side
                war.getSide2() :
                war.getSide1()
        );
        this.war = war;
        this.uuid = uuid;
        this.town = town;
        this.siegeLeader = siegeLeader;
        this.endTime = endTime;
        this.lastTouched = lastTouched;
        this.progressManager = new BattleProgressManager(siegeProgress, MAX_SIEGE_PROGRESS);
        this.phaseManager = new BattlePhaseManager<>(phase, phaseProgress, phaseStartTime);

        this.isSide1Attackers = isSide1Attackers(getWar(), getTown());
    }

    @Override
    public War getWar() {
        return war;
    }

    /**
     * Starts the battle
     */
    @ApiStatus.Internal
    public void start() {
        if (!new PreBattleStartEvent(war, this, BattleType.SIEGE).callEvent()) return;

        runnableController.add(
            new SiegeRunnable(this),
            new SiegeTeamRunnable(this),
            AlathranWars.getPacketEventsHook().isHookLoaded() ? new SiegeParticleRunnable(this) : null
        );
        runnableController.start();
        stopped = false;
        if (getControlPoint() != null && AlathranWars.getPacketEventsHook().isHookLoaded()) // Add laser if dependencies are loaded
            laserManager.setLaser(CustomLaser.of(SiegeUtils.getLaserFromLocation(getControlPoint()), SiegeUtils.getLaserToLocation(getControlPoint())));
        laserManager.start();
        bossBarManager.start();

        if (!war.isEventWar())
            if (AlathranWars.getVaultHook().isEconomyLoaded())
                AlathranWars.getVaultHook().getEconomy().withdrawPlayer(siegeLeader, SIEGE_VICTORY_MONEY);

        new BattleStartEvent(war, this, BattleType.SIEGE).callEvent();
    }

    /**
     * Resumes the battle (after a server restart e.t.c.)
     */
    @ApiStatus.Internal
    public void resume() {
        if (!new PreBattleStartEvent(war, this, BattleType.SIEGE).callEvent()) return;

        runnableController.add(
            new SiegeRunnable(this, getProgressManager().get()),
            new SiegeTeamRunnable(this),
            AlathranWars.getPacketEventsHook().isHookLoaded() ? new SiegeParticleRunnable(this) : null
        );
        runnableController.start();
        stopped = false;
        if (getControlPoint() != null && AlathranWars.getPacketEventsHook().isHookLoaded()) // Add laser if dependencies are loaded
            laserManager.setLaser(CustomLaser.of(SiegeUtils.getLaserFromLocation(getControlPoint()), SiegeUtils.getLaserToLocation(getControlPoint())));
        laserManager.start();
        bossBarManager.start();

        new BattleStartEvent(war, this, BattleType.SIEGE).callEvent();
    }

    /**
     * Stops the battle
     * </p>
     * Internal stop method for battles which triggers cleanup methods
     */
    @ApiStatus.Internal
    public void stop() {
        if (stopped) return;
        stopped = true;
        runnableController.stop();
        laserManager.stop();
        bossBarManager.stop();
        DatabaseQueries.deleteSiege(this); // TODO Run as latent event?
        war.removeSiege(this); // TODO Run as latent event?
    }

    /**
     * Stop a battle in favor of the attackers
     *
     * @param reason what triggered the end
     */
    public void attackersWin(BattleVictoryReason reason) {
        if (!new PreBattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.ATTACKER, reason).callEvent()) return;

        if (!war.isEventWar()) {
            if (AlathranWars.getVaultHook().isEconomyLoaded())
                AlathranWars.getVaultHook().getEconomy().depositPlayer(siegeLeader, SIEGE_VICTORY_MONEY);
            double amt;

            if (town.getAccount().getHoldingBalance() > 10000.0) {
                amt = Math.floor(town.getAccount().getHoldingBalance()) / 4.0;
                town.getAccount().withdraw(amt, "Siege Defeat");
            } else {
                town.getAccount().withdraw(SIEGE_VICTORY_MONEY, "Siege Defeat");
                amt = SIEGE_VICTORY_MONEY;
            }

            if (AlathranWars.getVaultHook().isEconomyLoaded())
                AlathranWars.getVaultHook().getEconomy().depositPlayer(siegeLeader, amt);
        }

        new BattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.ATTACKER, reason).callEvent();

        stop();
    }

    /**
     * Stop a battle in favor of the defenders
     *
     * @param reason what triggered the end
     */
    public void defendersWin(BattleVictoryReason reason) {
        if (!new PreBattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.DEFENDER, reason).callEvent()) return;

        if (!war.isEventWar())
            town.getAccount().deposit(SIEGE_VICTORY_MONEY, "Siege Victory");

        new BattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.DEFENDER, reason).callEvent();

        stop();
    }

    /**
     * End a battle in favor of no one
     *
     * @param reason what triggered the end
     */
    public void equalWin(BattleVictoryReason reason) {
        if (!new PreBattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.DRAW, reason).callEvent()) return;

        new BattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.DRAW, reason).callEvent();

        stop();
    }

    @NotNull
    public Instant getLastTouched() {
        return lastTouched;
    }

    public void setLastTouched(Instant lastTouched) {
        this.lastTouched = lastTouched;
    }

    public BattleProgressManager getProgressManager() {
        return progressManager;
    }

    public float getSiegeProgressPercentage() {
        return (getProgressManager().get() * 1.0f) / MAX_SIEGE_PROGRESS;
    }

    @NotNull
    public Town getTown() {
        return town;
    }

    public void setTown(final Town town) {
        this.town = town;
    }

    /**
     * Gets attacker name string
     */
    public Side getAttackerSide() {
        return isSide1Attackers() ? war.getSide1() : war.getSide2();
    }

    /**
     * Gets defender name string
     */
    public Side getDefenderSide() {
        return isSide1Attackers() ? war.getSide2() : war.getSide1();
    }

    // SECTION Display Bar

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    // SECTION UUID

    @Override
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Equals boolean.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean equals(UUID uuid) {
        return getUUID().equals(uuid);
    }

    /**
     * Equals boolean.
     *
     * @param battle the battle
     * @return the boolean
     */
    @Override
    public boolean equals(Battle battle) {
        return getUUID().equals(battle.getUUID());
    }

    // SECTION BattleType

    @Override
    public BattleType getBattleType() {
        return battleType;
    }

    // SECTION Accessors & Getters

    @Nullable
    public Location getTownSpawn() {
        return townSpawn;
    }

    public void setTownSpawn(@Nullable Location townSpawn) {
        this.townSpawn = townSpawn;
    }

    public @Nullable Location getControlPoint() {
        return controlPoint;
    }

    public void setControlPoint(@Nullable Location controlPoint) {
        this.controlPoint = controlPoint;
    }

    public static boolean isSide1Attackers(War war, Town town) {
        if (war == null)
            return false;

        final Side side = war.getSide(town);
        if (side == null)
            return false;

        return side.getTeam().equals(BattleTeam.SIDE_2);
    }

    public boolean isSide1Attackers() {
        return isSide1Attackers;
    }

    public Set<Player> getPlayersOnBattlefield() {
        return Stream.concat(
            getPlayersInZone(BattleSide.SPECTATOR).stream(),
            Stream.concat(
                getPlayersInZone(BattleSide.DEFENDER).stream(),
                getPlayersInZone(BattleSide.ATTACKER).stream()
            )
        ).collect(Collectors.toUnmodifiableSet());
    }

    @NotNull
    public OfflinePlayer getSiegeLeader() {
        return siegeLeader;
    }

    public void setSiegeLeader(OfflinePlayer siegeLeader) {
        this.siegeLeader = siegeLeader;
    }

    @NotNull
    public String getName() { // TODO Make better siegenames
        return getTown().getName();
    }

    // SECTION Time management

    @NotNull
    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant time) {
        endTime = time;
    }

    // SECTION Phase Management

    public BattlePhaseManager<SiegePhase> getPhaseManager() {
        return phaseManager;
    }

    // Misc

    public LaserManager getLaserManager() {
        return laserManager;
    }
}