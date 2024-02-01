package com.github.alathra.AlathranWars.conflict;

import com.github.alathra.AlathranWars.conflict.battle.raid.Raid;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.db.DatabaseQueries;
import com.github.alathra.AlathranWars.enums.ConflictType;
import com.github.alathra.AlathranWars.enums.WarDeleteReason;
import com.github.alathra.AlathranWars.enums.battle.BattleSide;
import com.github.alathra.AlathranWars.enums.battle.BattleTeam;
import com.github.alathra.AlathranWars.enums.battle.BattleVictoryReason;
import com.github.alathra.AlathranWars.events.PreWarCreateEvent;
import com.github.alathra.AlathranWars.events.PreWarDeleteEvent;
import com.github.alathra.AlathranWars.events.WarCreateEvent;
import com.github.alathra.AlathranWars.events.WarDeleteEvent;
import com.github.alathra.AlathranWars.hooks.NameColorHandler;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type New war.
 */
public class War extends Conflict {
    private final UUID uuid;
    private final String name; // Used in commands and such, like "TidalHaven.vs.Meme"
    private final String label;
    private final ConflictType conflictType = ConflictType.WAR;
    private boolean event = false;

    private @NotNull Side side1;
    private Side side2;
    private final Side attacker; // Reference variable to side1 or side2
    private final Side defender; // Reference variable to side1 or side2

    private Set<Siege> sieges = new HashSet<>();
    private Set<Raid> raids = new HashSet<>();

    public War(
        UUID uuid,
        String name,
        String label,
        @NotNull Side side1,
        Side side2,
        Set<Siege> sieges,
        Set<Raid> raids,
        boolean event
    ) {
        this.uuid = uuid;
        this.name = name;
        this.label = label;

        this.side1 = side1;
        this.side2 = side2;

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

        this.sieges = sieges;
        this.raids = raids;
        this.event = event;

        resume();
    }

    public War(
        UUID uuid,
        String label,
        Government aggressor,
        Government victim,
        boolean event
    ) throws SideCreationException, WrapperCommandSyntaxException {
        this.uuid = uuid;
        this.label = label;

        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

//        final @NotNull UUID side1UUID = UUIDUtil.generateSideUUID(null);
//        final @NotNull UUID side2UUID = UUIDUtil.generateSideUUID(side1UUID);

        // TODO Side builder that uses aggressorIsNation &
//        if (!aggressorIsNation && !victimIsNation) {
//            this.side1 = new Side(this.uuid, side1UUID, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
//            this.side2 = new Side(this.uuid, side2UUID, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);
//        } else if (!aggressorIsNation && victimIsNation) {
//            this.side1 = new Side(this.uuid, side1UUID, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
//            this.side2 = new Side(this.uuid, side2UUID, victim.getNationOrNull(), BattleSide.DEFENDER, BattleTeam.SIDE_2);
//        } else if (aggressorIsNation && !victimIsNation) {
//            this.side1 = new Side(this.uuid, side1UUID, aggressor.getNationOrNull(), BattleSide.ATTACKER, BattleTeam.SIDE_1);
//            this.side2 = new Side(this.uuid, side2UUID, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);
//        } else if (aggressorIsNation && victimIsNation) {
//            this.side1 = new Side(this.uuid, side1UUID, aggressor.getNationOrNull(), BattleSide.ATTACKER, BattleTeam.SIDE_1);
//            this.side2 = new Side(this.uuid, side2UUID, victim.getNationOrNull(), BattleSide.DEFENDER, BattleTeam.SIDE_2);
//        }

        this.side1 = new SideBuilder()
            .setWarUUID(this.uuid)
            .setUuid(UUID.randomUUID())
            .setLeader(aggressor)
            .setSide(BattleSide.ATTACKER)
            .setTeam(BattleTeam.SIDE_1)
            .buildNew();
        this.side2 = new SideBuilder()
            .setWarUUID(this.uuid)
            .setUuid(UUID.randomUUID())
            .setLeader(victim)
            .setSide(BattleSide.DEFENDER)
            .setTeam(BattleTeam.SIDE_2)
            .buildNew();

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;
        this.event = event;

        if (!new PreWarCreateEvent(this).callEvent())
            return;

        // TODO All logic below this point should be separated out
        WarManager.getInstance().addWar(this);

        start();

        new WarCreateEvent(this).callEvent();
    }

    /**
     * Instantiates a new New war.
     *
     * @param label     the name
     * @param aggressor the side 1
     * @param victim    the side 2
     */
    /*public War(
        UUID uuid,
        String label,
        Town aggressor,
        Town victim,
        boolean aggressorIsNation,
        boolean victimIsNation,
        boolean event
    ) throws WrapperCommandSyntaxException {
        this.uuid = uuid;
        this.label = label;

        final String aggressorName = aggressorIsNation ? Objects.requireNonNull(aggressor.getNationOrNull()).getName() : aggressor.getName();
        final String victimName = victimIsNation ? Objects.requireNonNull(victim.getNationOrNull()).getName() : victim.getName();
        this.name = "%s.vs.%s".formatted(aggressorName, victimName);
        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        final @NotNull UUID side1UUID = UUIDUtil.generateSideUUID(null);
        final @NotNull UUID side2UUID = UUIDUtil.generateSideUUID(side1UUID);

        // TODO Side builder that uses aggressorIsNation &
        if (!aggressorIsNation && !victimIsNation) {
            this.side1 = new Side(this.uuid, side1UUID, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
            this.side2 = new Side(this.uuid, side2UUID, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);
        } else if (!aggressorIsNation && victimIsNation) {
            this.side1 = new Side(this.uuid, side1UUID, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
            this.side2 = new Side(this.uuid, side2UUID, victim.getNationOrNull(), BattleSide.DEFENDER, BattleTeam.SIDE_2);
        } else if (aggressorIsNation && !victimIsNation) {
            this.side1 = new Side(this.uuid, side1UUID, aggressor.getNationOrNull(), BattleSide.ATTACKER, BattleTeam.SIDE_1);
            this.side2 = new Side(this.uuid, side2UUID, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);
        } else if (aggressorIsNation && victimIsNation) {
            this.side1 = new Side(this.uuid, side1UUID, aggressor.getNationOrNull(), BattleSide.ATTACKER, BattleTeam.SIDE_1);
            this.side2 = new Side(this.uuid, side2UUID, victim.getNationOrNull(), BattleSide.DEFENDER, BattleTeam.SIDE_2);
        }

        this.side1 = new SideBuilder()
            .setWarUUID(this.uuid)
            .setUuid(side1UUID)
            .setLeader(aggressorIsNation ? aggressor.getNationOrNull() : aggressor)
            .setSide(BattleSide.ATTACKER)
            .setTeam(BattleTeam.SIDE_1)
            .buildNew();

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;
        this.event = event;

        if (!new PreWarCreateEvent(this).callEvent())
            return;

        WarManager.getInstance().addWar(this);

        start();

        new WarCreateEvent(this).callEvent();
    }*/

    /**
     * Instantiates a new New war.
     *
     * @param label     the name
     * @param aggressor the side 1
     * @param victim    the side 2
     */
    /*public War(final String label, @NotNull Town aggressor, @NotNull Town victim, boolean event) throws WrapperCommandSyntaxException {
        this.uuid = UUIDUtil.generateWarUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        final @NotNull UUID side1UUID = UUIDUtil.generateSideUUID(null);
        final @NotNull UUID side2UUID = UUIDUtil.generateSideUUID(side1UUID);
        this.side1 = new Side(this.uuid, side1UUID, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, side2UUID, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;
        this.event = event;

        if (!new PreWarCreateEvent(this).callEvent())
            return;

        WarManager.getInstance().addWar(this);

        start();

        new WarCreateEvent(this).callEvent();
    }

    public War(final String label, @NotNull Nation aggressor, @NotNull Nation victim, boolean event) throws WrapperCommandSyntaxException {
        this.uuid = UUIDUtil.generateWarUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        final @NotNull UUID side1UUID = UUIDUtil.generateSideUUID(null);
        final @NotNull UUID side2UUID = UUIDUtil.generateSideUUID(side1UUID);
        this.side1 = new Side(this.uuid, side1UUID, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, side2UUID, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;
        this.event = event;

        if (!new PreWarCreateEvent(this).callEvent())
            return;

        WarManager.getInstance().addWar(this);

        start();

        new WarCreateEvent(this).callEvent();
    }

    public War(final String label, @NotNull Town aggressor, @NotNull Nation victim, boolean event) throws WrapperCommandSyntaxException {
        this.uuid = UUIDUtil.generateWarUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        final @NotNull UUID side1UUID = UUIDUtil.generateSideUUID(null);
        final @NotNull UUID side2UUID = UUIDUtil.generateSideUUID(side1UUID);
        this.side1 = new Side(this.uuid, side1UUID, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, side2UUID, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;
        this.event = event;

        if (!new PreWarCreateEvent(this).callEvent())
            return;

        WarManager.getInstance().addWar(this);

        start();

        new WarCreateEvent(this).callEvent();
    }

    public War(final String label, @NotNull Nation aggressor, @NotNull Town victim, boolean event) throws WrapperCommandSyntaxException {
        this.uuid = UUIDUtil.generateWarUUID();
        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        this.label = label;

        if (WarManager.getInstance().getWar(this.name) != null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>A war already exists with that name!").build());

        final @NotNull UUID side1UUID = UUIDUtil.generateSideUUID(null);
        final @NotNull UUID side2UUID = UUIDUtil.generateSideUUID(side1UUID);
        this.side1 = new Side(this.uuid, side1UUID, aggressor, BattleSide.ATTACKER, BattleTeam.SIDE_1);
        this.side2 = new Side(this.uuid, side2UUID, victim, BattleSide.DEFENDER, BattleTeam.SIDE_2);

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;
        this.event = event;

        if (!new PreWarCreateEvent(this).callEvent())
            return;

        WarManager.getInstance().addWar(this);

        start();

        new WarCreateEvent(this).callEvent();
    }
*/
    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    /**
     * Equals boolean.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean equals(UUID uuid) {
        return this.uuid.equals(uuid);
    }

    /**
     * Equals boolean.
     *
     * @param war the war
     * @return the boolean
     */
    public boolean equals(@NotNull War war) {
        return this.uuid.equals(war.getUUID());
    }

    /**
     * Equals boolean.
     *
     * @param warName the war name
     * @return the boolean
     */
    public boolean equals(String warName) {
        return this.name.equals(warName);
    }

    /**
     * Gets side 1.
     *
     * @return the side 1
     */
    @NotNull
    public Side getSide1() {
        return side1;
    }

    /**
     * Gets side 2.
     *
     * @return the side 2
     */
    @NotNull
    public Side getSide2() {
        return side2;
    }

    @NotNull
    public Set<Side> getSides() {
        return Set.of(
            getSide1(),
            getSide2()
        );
    }

    /**
     * Gets attacker.
     *
     * @return the attacker
     */
    @NotNull
    public Side getAttacker() {
        return attacker;
    }

    /**
     * Gets defender.
     *
     * @return the defender
     */
    @NotNull
    public Side getDefender() {
        return defender;
    }

    @Nullable
    public Side getSide(String name) {
        if (getSide1().equals(name))
            return getSide1();

        if (getSide2().equals(name))
            return getSide2();

        return null;
    }

    @Nullable
    public Side getSide(UUID uuid) {
        if (getSide1().equals(uuid))
            return getSide1();

        if (getSide2().equals(uuid))
            return getSide2();

        return null;
    }

    /**
     * Gets sieges.
     *
     * @return the sieges
     */
    @NotNull
    public Set<Siege> getSieges() {
        return sieges;
    }

    public void setSieges(Set<Siege> sieges) {
        this.sieges = sieges;
        this.sieges.forEach(Siege::resume);
    }

    @Nullable
    public Siege getSiege(UUID uuid) {
        for (@NotNull Siege siege : sieges) {
            if (siege.equals(uuid))
                return siege;
        }

        return null;
    }

    /**
     * Add siege.
     *
     * @param siege the siege
     */
    public void addSiege(Siege siege) {
        sieges.add(siege);
    }

    public void removeSiege(Siege siege) {
        sieges.remove(siege);
    }

    /**
     * Gets raids.
     *
     * @return the raids
     */
    @NotNull
    public Set<Raid> getRaids() {
        return raids;
    }

    /**
     * Add raid.
     *
     * @param raid the raid
     */
    public void addRaid(Raid raid) {
        raids.add(raid);
    }

    public void removeRaid(Raid raid) {
        raids.remove(raid);
    }

    @NotNull
    public boolean isTownUnderSiege(Town town) {
        return sieges.stream().anyMatch(siege -> siege.getTown().equals(town));
    }

    @NotNull
    public boolean isTownUnderRaid(Town town) {
        return raids.stream().anyMatch(raid -> raid.getTown().equals(town));
    }

    @NotNull
    public Set<Player> getPlayers() {
        return Stream.concat(
                getSide1().getPlayers().stream(),
                getSide2().getPlayers().stream()
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<OfflinePlayer> getPlayersIncludingOffline() {
        return Stream.concat(
                getSide1().getPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer),
                getSide2().getPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer)
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<OfflinePlayer> getSurrenderedPlayers() {
        return Stream.concat(
                getSide1().getSurrenderedPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer),
                getSide2().getSurrenderedPlayersIncludingOffline().stream().map(Bukkit::getOfflinePlayer)
            )
            .collect(Collectors.toSet());
    }

    public boolean isPlayerInWar(@NotNull Player p) {
        return side1.isPlayerOnSide(p) || side2.isPlayerOnSide(p);
    }

    public boolean isPlayerInWar(UUID uuid) {
        return side1.isPlayerOnSide(uuid) || side2.isPlayerOnSide(uuid);
    }

    @Nullable
    public Side getPlayerSide(@NotNull Player p) {
        if (side1.isPlayerOnSide(p))
            return getSide1();

        if (side2.isPlayerOnSide(p))
            return getSide2();

        return null;
    }

    @Nullable
    public Side getPlayerSide(UUID uuid) {
        if (side1.isPlayerOnSide(uuid))
            return getSide1();

        if (side2.isPlayerOnSide(uuid))
            return getSide2();

        return null;
    }

    public boolean isTownInWar(Town town) {
        return side1.isTownOnSide(town) || side2.isTownOnSide(town);
    }

    @Nullable
    public Side getTownSide(Town town) {
        if (side1.isTownOnSide(town))
            return getSide1();

        if (side2.isTownOnSide(town))
            return getSide2();

        return null;
    }

    public boolean isNationInWar(Nation nation) {
        return side1.isNationOnSide(nation) || side2.isNationOnSide(nation);
    }

    @Nullable
    public Side getNationSide(Nation nation) {
        if (side1.isNationOnSide(nation))
            return getSide1();

        if (side2.isNationOnSide(nation))
            return getSide2();

        return null;
    }

    public boolean isSideValid(String sideName) {
        return getSide1().equals(sideName) || getSide2().equals(sideName);
    }

    public void cancelSieges() {
        getTowns().forEach(this::cancelSieges);
    }

    public void cancelSieges(Town town) {
        if (this != null) {
            Side townSide = getTownSide(town);

            if (townSide == null) return;

            getSieges().forEach(siege -> { // TODO INFINITE LOOP, runs surrenderTown
                if (siege.getTown().equals(town)) {
                    if (siege.getAttackerSide().equals(townSide)) {
                        siege.defendersWin(BattleVictoryReason.ADMIN_CANCEL);
                    } else {
                        siege.attackersWin(BattleVictoryReason.ADMIN_CANCEL);
                    }
                }
            });
        }
    }

    public void surrenderNation(Nation nation) {
        Side nationSide = getNationSide(nation);
        if (nationSide == null) return;

        final @Nullable Nation occupier = nationSide.equals(getSide1()) ? getSide2().getTown().getNationOrNull() : getSide1().getTown().getNationOrNull();
        Occupation.setOccupied(nation, occupier);

        nationSide.surrenderNation(nation);
        // TODO Cancel in progress sieges for towns?
        nationSide.processSurrenders();
    }

    public void surrenderTown(Town town) {
        Side townSide = getTownSide(town);
        if (townSide == null) return;

        final @Nullable Nation townNation = town.getNationOrNull();
        final @Nullable Nation occupier = townSide.equals(getSide1()) ? getSide2().getTown().getNationOrNull() : getSide1().getTown().getNationOrNull();
        Occupation.setOccupied(town, occupier);
        townSide.surrenderTown(town);

        if (townNation != null) {
            if (townSide.shouldNationSurrender(townNation)) {
                surrenderNation(townNation);
            }
        }

        // TODO Cancel in progress sieges for towns?
        townSide.processSurrenders();
    }

    public void unsurrenderNation(Nation nation) {
        Side nationSide = getNationSide(nation);
        if (nationSide == null) return;

        nationSide.unsurrenderNation(nation);
        nation.getTowns().forEach(this::unsurrenderTown);
    }

    public void unsurrenderTown(Town town) {
        Side townSide = getTownSide(town);
        if (townSide == null) return;

        Occupation.removeOccupied(town);

        townSide.unsurrenderTown(town);
    }

    @NotNull
    public Set<Town> getTowns() {
        return Stream.concat(
                getSide1().getTowns().stream(),
                getSide2().getTowns().stream()
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Town> getAllTowns() {
        return Stream.concat(
                getSide1().getTowns().stream(),
                Stream.concat(
                    getSide2().getTowns().stream(),
                    getSurrenderedTowns().stream()
                )
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Nation> getNations() {
        return Stream.concat(
                getSide1().getNations().stream(),
                getSide2().getNations().stream()
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Nation> getAllNations() {
        return Stream.concat(
                getSide1().getNations().stream(),
                Stream.concat(
                    getSide2().getNations().stream(),
                    getSurrenderedNations().stream()
                )
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Town> getSurrenderedTowns() {
        return Stream.concat(
                getSide1().getSurrenderedTowns().stream(),
                getSide2().getSurrenderedTowns().stream()
            )
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Nation> getSurrenderedNations() {
        return Stream.concat(
                getSide1().getSurrenderedNations().stream(),
                getSide2().getSurrenderedNations().stream()
            )
            .collect(Collectors.toSet());
    }

    public void start() {
        Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The war of <war> has started between <attacker> and <defender>.")
            .parseMinimessagePlaceholder("war", getLabel())
            .parseMinimessagePlaceholder("attacker", getAttacker().getName())
            .parseMinimessagePlaceholder("defender", getDefender().getName())
            .build()
        );

        final Title warTitle = Title.title(
            ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War")
                .build(),
            ColorParser.of("<gray><i>The war of <war> has begun!")
                .parseMinimessagePlaceholder("war", getLabel())
                .build(),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
        );
        final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

        side1.getPlayers().forEach(player -> {
            player.showTitle(warTitle);
            player.playSound(warSound);
        });
        side2.getPlayers().forEach(player -> {
            player.showTitle(warTitle);
            player.playSound(warSound);
        });

        side1.applyNameTags();
        side2.applyNameTags();
    }

    public void resume() {
        side1.applyNameTags();
        side2.applyNameTags();
    }

    public void end(WarDeleteReason reason) {
        if (!new PreWarDeleteEvent(this, reason).callEvent())
            return;

        getSieges().forEach(Siege::stop);
        getAllTowns().forEach(Occupation::removeOccupied);

        DatabaseQueries.deleteWar(this);
        WarManager.getInstance().removeWar(this);

        // Run check after war is removed to cleanup player tags
        getPlayers().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));

        new WarDeleteEvent(this, reason).callEvent();
    }

    public void defeat(@NotNull Side loserSide) {
        @NotNull Side loser = loserSide.equals(getSide1()) ? getSide1() : getSide2();
        @NotNull Side winner = loserSide.equals(getSide1()) ? getSide2() : getSide1();

        Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The war of <war> has ended. <red><winner> <reset>has triumphed against <red><loser><reset>.")
            .parseMinimessagePlaceholder("war", getLabel())
            .parseMinimessagePlaceholder("winner", winner.getName())
            .parseMinimessagePlaceholder("loser", loser.getName())
            .build()
        );

        end(WarDeleteReason.DEFEAT);
    }

    // White defeat/force end war
    public void draw() {
        Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The war of <war> has ended with a white peace.")
            .parseMinimessagePlaceholder("war", getLabel())
            .build()
        );

        end(WarDeleteReason.DRAW);
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public boolean isEvent() {
        return event;
    }
}
