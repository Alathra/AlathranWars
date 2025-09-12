package io.github.alathra.alathranwars.database;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.battle.siege.SiegePhase;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.WarCreationException;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.conflict.war.side.SideCreationException;
import io.github.alathra.alathranwars.conflict.war.side.spawn.RallyPoint;
import io.github.alathra.alathranwars.conflict.war.side.spawn.SpawnType;
import io.github.alathra.alathranwars.cooldown.CooldownType;
import io.github.alathra.alathranwars.cooldown.Cooldowns;
import io.github.alathra.alathranwars.database.schema.tables.records.*;
import io.github.alathra.alathranwars.enums.battle.BattleSide;
import io.github.alathra.alathranwars.enums.battle.BattleTeam;
import io.github.alathra.alathranwars.utility.DB;
import io.github.alathra.alathranwars.utility.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.alathra.alathranwars.database.QueryUtils.UUIDUtil;
import static io.github.alathra.alathranwars.database.schema.Tables.*;

/**
 * A class providing access to all SQL queries.
 */
@SuppressWarnings({"LoggingSimilarMessage", "StringConcatenationArgumentToLogCall"})
public final class Queries {
    /**
     * Holds all queries related to using the database as a messaging service.
     */
    @ApiStatus.Internal
    public static final class Sync {
//        /**
//         * Fetch the latest (greatest) message ID from the database.
//         *
//         * @return the message id or empty if no messages are queued
//         */
//        public static Optional<Integer> fetchLatestMessageId() {
//            try (
//                Connection con = DB.getConnection()
//            ) {
//                DSLContext context = DB.getContext(con);
//
//                return context
//                    .select(max(MESSAGING.ID))
//                    .from(MESSAGING)
//                    .fetchOptional(0, Integer.class);
//            } catch (SQLException e) {
//                Logger.get().error("SQL Query threw an error!" + e);
//                return Optional.empty();
//            }
//        }
//
//        /**
//         * Adds a message to the database.
//         *
//         * @param message the outgoing message to send
//         * @return the new message id or empty if insert failed
//         */
//        public static <T> Optional<Integer> send(OutgoingMessage<T> message) {
//            try (
//                Connection con = DB.getConnection()
//            ) {
//                DSLContext context = DB.getContext(con);
//
//                return context
//                    .insertInto(MESSAGING, MESSAGING.TIMESTAMP, MESSAGING.MESSAGE)
//                    .values(
//                        currentLocalDateTime(),
//                        val(message.encode())
//                    )
//                    .returningResult(MESSAGING.ID)
//                    .fetchOptional(0, Integer.class);
//            } catch (SQLException e) {
//                Logger.get().error("SQL Query threw an error!" + e);
//                return Optional.empty();
//            }
//        }
//
//        /**
//         * Fetch all messages from the database.
//         *
//         * @param latestSyncId    the currently synced to message id
//         * @param cleanupInterval the configured cleanup interval
//         * @return the messages
//         */
//        public static Map<Integer, IncomingMessage<?, ?>> receive(int latestSyncId, long cleanupInterval) {
//            try (
//                Connection con = DB.getConnection()
//            ) {
//                DSLContext context = DB.getContext(con);
//
//                return context
//                    .selectFrom(MESSAGING)
//                    .where(MESSAGING.ID.greaterThan(latestSyncId)
//                        .and(MESSAGING.TIMESTAMP.greaterOrEqual(localDateTimeSub(currentLocalDateTime(), cleanupInterval / 1000, DatePart.SECOND))) // Checks TIMESTAMP >= now() - cleanupInterval
//                    )
//                    .orderBy(MESSAGING.ID.asc())
//                    .fetch()
//                    .intoMap(MESSAGING.ID, r -> BidirectionalMessage.from(r.getMessage()));
//            } catch (SQLException e) {
//                Logger.get().error("SQL Query threw an error!" + e);
//                return Map.of();
//            }
//        }
//
//        /**
//         * Deletes all outdate messages from the database.
//         *
//         * @param cleanupInterval the configured cleanup interval
//         */
//        public static void cleanup(long cleanupInterval) {
//            try (
//                Connection con = DB.getConnection()
//            ) {
//                DSLContext context = DB.getContext(con);
//
//                context
//                    .deleteFrom(MESSAGING)
//                    .where(MESSAGING.TIMESTAMP.lessThan(localDateTimeSub(currentLocalDateTime(), cleanupInterval / 1000, DatePart.SECOND))) // Checks TIMESTAMP < now() - cleanupInterval
//                    .execute();
//            } catch (SQLException e) {
//                Logger.get().error("SQL Query threw an error!" + e);
//            }
//        }
    }

    /**
     * Wrapper class to organize cooldown-related queries.
     */
    public static final class Cooldown {
        public static Map<CooldownType, Instant> load(OfflinePlayer player) {
            return load(player.getUniqueId());
        }

        public static Map<CooldownType, Instant> load(UUID uuid) {
            try (
                Connection con = DB.getConnection()
            ) {
                DSLContext context = DB.getContext(con);

                final Result<CooldownsRecord> cooldownsRecords = context
                    .selectFrom(COOLDOWNS)
                    .where(COOLDOWNS.UUID.eq(UUIDUtil.toBytes(uuid)))
                    .fetch();

                return cooldownsRecords.stream()
                    .collect(Collectors.toMap(
                        r -> CooldownType.valueOf(r.getCooldownType()),
                        r -> QueryUtils.InstantUtil.fromDateTime(r.getCooldownTime())
                    ));
            } catch (SQLException e) {
                Logger.get().error("SQL Query threw an error!", e);
            }
            return Collections.emptyMap();
        }

        public static void save(OfflinePlayer player) {
            save(player.getUniqueId());
        }

        public static void save(UUID uuid) {
            try (
                Connection con = DB.getConnection()
            ) {
                DSLContext context = DB.getContext(con);

                context.transaction(config -> {
                    DSLContext ctx = config.dsl();

                    // Delete old cooldowns
                    ctx.deleteFrom(COOLDOWNS)
                        .where(COOLDOWNS.UUID.eq(UUIDUtil.toBytes(uuid)))
                        .execute();

                    // Insert new cooldowns
                    final List<CooldownsRecord> cooldownsRecords = new ArrayList<>();

                    for (CooldownType cooldownType : CooldownType.values()) {
                        if (!Cooldowns.has(uuid, cooldownType))
                            continue;

                        cooldownsRecords.add(new CooldownsRecord(
                            UUIDUtil.toBytes(uuid),
                            cooldownType.name(),
                            QueryUtils.InstantUtil.toDateTime(Cooldowns.get(uuid, cooldownType))
                        ));
                    }

                    if (!cooldownsRecords.isEmpty())
                        ctx.batchInsert(cooldownsRecords).execute();
                });
            } catch (SQLException e) {
                Logger.get().error("SQL Query threw an error!", e);
            }
        }
    }

    public static void saveAll() {
        try (
            Connection con = DB.getConnection();
        ) {
            DSLContext context = DB.getContext(con);

            for (War war : WarController.getInstance().getWars()) {
                context
                    .insertInto(LIST, LIST.UUID, LIST.NAME, LIST.LABEL, LIST.SIDE1, LIST.SIDE2, LIST.EVENT)
                    .values(
                        QueryUtils.UUIDUtil.toBytes(war.getUUID()),
                        war.getName(),
                        war.getLabel(),
                        QueryUtils.UUIDUtil.toBytes(war.getSide1().getUUID()),
                        QueryUtils.UUIDUtil.toBytes(war.getSide2().getUUID()),
                        war.isEventWar() ? (byte) 1 : (byte) 0
                    )
                    .onDuplicateKeyUpdate()
                    .set(LIST.NAME, war.getName())
                    .set(LIST.LABEL, war.getLabel())
                    .set(LIST.SIDE1, QueryUtils.UUIDUtil.toBytes(war.getSide1().getUUID()))
                    .set(LIST.SIDE2, QueryUtils.UUIDUtil.toBytes(war.getSide2().getUUID()))
                    .execute();

                // Save sides
                for (Side side : war.getSides()) {
                    saveSide(con, side);
                }

                // Save sieges
                for (Siege siege : war.getSieges()) {
                    saveSiege(con, siege);
                }
            }
        } catch (SQLException e) {
            Logger.get().error("SQL Query failed to save data!", e);
        }
    }

    public static void saveSide(Connection con, Side side) {
        try {
            DSLContext context = DB.getContext(con);

            context
                .insertInto(SIDES, SIDES.WAR, SIDES.UUID, SIDES.SIDE, SIDES.TEAM, SIDES.NAME, SIDES.TOWN, SIDES.SIEGE_GRACE, SIDES.RAID_GRACE)
                .values(
                    QueryUtils.UUIDUtil.toBytes(side.getWar().getUUID()),
                    QueryUtils.UUIDUtil.toBytes(side.getUUID()),
                    side.getSide().toString(),
                    side.getTeam().toString(),
                    side.getName(),
                    QueryUtils.UUIDUtil.toBytes(side.getTown().getUUID()),
                    LocalDateTime.ofInstant(side.getSiegeGrace(), ZoneOffset.UTC),
                    LocalDateTime.ofInstant(side.getRaidGrace(), ZoneOffset.UTC)
                )
                .onDuplicateKeyUpdate()
                .set(SIDES.SIDE, side.getSide().toString())
                .set(SIDES.TEAM, side.getTeam().toString())
                .set(SIDES.NAME, side.getName())
                .set(SIDES.TOWN, QueryUtils.UUIDUtil.toBytes(side.getTown().getUUID()))
                .set(SIDES.SIEGE_GRACE, LocalDateTime.ofInstant(side.getSiegeGrace(), ZoneOffset.UTC))
                .set(SIDES.RAID_GRACE, LocalDateTime.ofInstant(side.getRaidGrace(), ZoneOffset.UTC))
                .execute();

            saveSideNations(con, side);
            saveSideTowns(con, side);
            saveSidePlayers(con, side);
            saveSideSpawns(con, side);
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query failed to save side!", e);
        }
    }

    public static void saveSideNations(Connection con, Side side) {
        try {
            DSLContext context = DB.getContext(con);

            context.transaction(config -> {
                DSLContext ctx = config.dsl();

                ctx
                    .deleteFrom(SIDES_NATIONS)
                    .where(SIDES_NATIONS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(side.getUUID())))
                    .execute();

                ctx.batchInsert(
                    side.getNations().stream().map(nation -> new SidesNationsRecord(
                        UUIDUtil.toBytes(side.getUUID()),
                        UUIDUtil.toBytes(nation.getUUID()),
                        (byte) 0
                    )).toList()
                ).execute();

                ctx.batchInsert(
                    side.getNationsSurrendered().stream().map(nation -> new SidesNationsRecord(
                        UUIDUtil.toBytes(side.getUUID()),
                        UUIDUtil.toBytes(nation.getUUID()),
                        (byte) 1
                    )).toList()
                ).execute();
            });
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query failed to save side nations!", e);
        }
    }

    public static void saveSideTowns(Connection con, Side side) {
        try {
            DSLContext context = DB.getContext(con);

            context.transaction(config -> {
                DSLContext ctx = config.dsl();

                ctx
                    .deleteFrom(SIDES_TOWNS)
                    .where(SIDES_TOWNS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(side.getUUID())))
                    .execute();

                ctx.batchInsert(
                    side.getTowns().stream().map(town -> new SidesTownsRecord(
                        UUIDUtil.toBytes(side.getUUID()),
                        UUIDUtil.toBytes(town.getUUID()),
                        (byte) 0
                    )).toList()
                ).execute();

                ctx.batchInsert(
                    side.getTownsSurrendered().stream().map(town -> new SidesTownsRecord(
                        UUIDUtil.toBytes(side.getUUID()),
                        UUIDUtil.toBytes(town.getUUID()),
                        (byte) 1
                    )).toList()
                ).execute();
            });
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query failed to save side towns!", e);
        }
    }

    public static void saveSidePlayers(Connection con, Side side) {
        try {
            DSLContext context = DB.getContext(con);

            context.transaction(config -> {
                DSLContext ctx = config.dsl();

                ctx
                    .deleteFrom(SIDES_PLAYERS)
                    .where(SIDES_PLAYERS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(side.getUUID())))
                    .execute();

                ctx.batchInsert(
                    side.getPlayersAll().stream().map(p ->
                        new SidesPlayersRecord(
                            QueryUtils.UUIDUtil.toBytes(side.getUUID()),
                            QueryUtils.UUIDUtil.toBytes(p.getUniqueId()),
                            (byte) 0
                        )
                    ).toList()
                ).execute();

                ctx.batchInsert(
                    side.getPlayersSurrendered().stream().map(p ->
                        new SidesPlayersRecord(
                            QueryUtils.UUIDUtil.toBytes(side.getUUID()),
                            QueryUtils.UUIDUtil.toBytes(p.getUniqueId()),
                            (byte) 1
                        )
                    ).toList()
                ).execute();
            });
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query failed to save side players!", e);
        }
    }

    public static void saveSideSpawns(Connection con, Side side) {
        try {
            DSLContext context = DB.getContext(con);

            context.transaction(config -> {
                DSLContext ctx = config.dsl();

                ctx
                    .deleteFrom(SIDES_SPAWNS)
                    .where(SIDES_SPAWNS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(side.getUUID())))
                    .execute();

                ctx.batchInsert(
                    side.getSpawnManager().getRallies()
                        .stream()
                        .map(RallyPoint::deserialize)
                        .collect(Collectors.toList())
                ).execute();
            });
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query failed to save side players!", e);
        }
    }

    private static void saveSiege(Connection con, Siege siege) {
        try {
            DSLContext context = DB.getContext(con);

            context.insertInto(SIEGES, SIEGES.WAR, SIEGES.UUID, SIEGES.TOWN, SIEGES.SIEGE_LEADER, SIEGES.END_TIME, SIEGES.LAST_TOUCHED, SIEGES.SIEGE_PROGRESS, SIEGES.PHASE_CURRENT, SIEGES.PHASE_PROGRESS, SIEGES.PHASE_START_TIME)
                .values(
                    QueryUtils.UUIDUtil.toBytes(siege.getWar().getUUID()),
                    QueryUtils.UUIDUtil.toBytes(siege.getUUID()),
                    QueryUtils.UUIDUtil.toBytes(siege.getTown().getUUID()),
                    QueryUtils.UUIDUtil.toBytes(siege.getSiegeLeader().getUniqueId()),
                    LocalDateTime.ofInstant(siege.getEndTime(), ZoneOffset.UTC),
                    LocalDateTime.ofInstant(siege.getLastTouched(), ZoneOffset.UTC),
                    siege.getProgressManager().get(),
                    siege.getPhaseManager().get().name(),
                    siege.getPhaseManager().getProgress(),
                    LocalDateTime.ofInstant(siege.getPhaseManager().getStartTime(), ZoneOffset.UTC)
                )
                .onDuplicateKeyUpdate()
                .set(SIEGES.WAR, QueryUtils.UUIDUtil.toBytes(siege.getWar().getUUID()))
                .set(SIEGES.TOWN, QueryUtils.UUIDUtil.toBytes(siege.getTown().getUUID()))
                .set(SIEGES.SIEGE_LEADER, QueryUtils.UUIDUtil.toBytes(siege.getSiegeLeader().getUniqueId()))
                .set(SIEGES.END_TIME, LocalDateTime.ofInstant(siege.getEndTime(), ZoneOffset.UTC))
                .set(SIEGES.LAST_TOUCHED, LocalDateTime.ofInstant(siege.getLastTouched(), ZoneOffset.UTC))
                .set(SIEGES.SIEGE_PROGRESS, siege.getProgressManager().get())
                .set(SIEGES.PHASE_CURRENT, siege.getPhaseManager().get().name())
                .set(SIEGES.PHASE_PROGRESS, siege.getPhaseManager().getProgress())
                .set(SIEGES.PHASE_START_TIME, LocalDateTime.ofInstant(siege.getPhaseManager().getStartTime(), ZoneOffset.UTC))
                .execute();

            saveSiegePlayers(con, siege);
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }

    public static void saveSiegePlayers(Connection con, Siege siege) { // TODO We no longer save players in battles
        try {
            DSLContext context = DB.getContext(con);

            context.transaction(config -> {
                DSLContext ctx = config.dsl();

                ctx
                    .deleteFrom(SIEGE_PLAYERS)
                    .where(SIEGE_PLAYERS.SIEGE.equal(QueryUtils.UUIDUtil.toBytes(siege.getUUID())))
                    .execute();

                ctx.batchInsert(
                    siege.getPlayersInBattle(BattleSide.ATTACKER).stream().map(p ->
                        new SiegePlayersRecord(
                            QueryUtils.UUIDUtil.toBytes(siege.getUUID()),
                            QueryUtils.UUIDUtil.toBytes(p.getUniqueId()),
                            (byte) 0
                        )
                    ).toList()
                ).execute();

                ctx.batchInsert(
                    siege.getPlayersInBattle(BattleSide.DEFENDER).stream().map(p ->
                        new SiegePlayersRecord(
                            QueryUtils.UUIDUtil.toBytes(siege.getUUID()),
                            QueryUtils.UUIDUtil.toBytes(p.getUniqueId()),
                            (byte) 1
                        )
                    ).toList()
                ).execute();
            });
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }

    // Getters
    public static @NotNull Set<War> loadAll() {
        Set<War> wars = new HashSet<>();

        try (
            Connection con = DB.getConnection();
        ) {
            DSLContext context = DB.getContext(con);

            Result<ListRecord> result = context
                .selectFrom(LIST)
                .fetch();

            for (ListRecord r : result) {
                UUID uuid = QueryUtils.UUIDUtil.fromBytes(r.getUuid());
                @Nullable Side side1 = loadSide(con, uuid, QueryUtils.UUIDUtil.fromBytes(r.getSide1()));
                @Nullable Side side2 = loadSide(con, uuid, QueryUtils.UUIDUtil.fromBytes(r.getSide2()));
                boolean event = QueryUtils.BooleanUtil.fromByte(r.getEvent());

                wars.add(
                    War.builder()
                        .setUuid(uuid)
                        .setName(r.getName())
                        .setLabel(r.getLabel())
                        .setSide1(side1)
                        .setSide2(side2)
                        .setSieges(new HashSet<>())
                        .setRaids(new HashSet<>())
                        .setEvent(event)
                        .setScheduledWarTime(QueryUtils.InstantUtil.fromDateTime(r.getWarTime()))
                        .resume()
                );
            }

            for (War war : wars) {
                @NotNull Set<Siege> sieges = loadSieges(con, war);
                war.setSieges(sieges);
                sieges.forEach(Siege::resume);
            }
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        } catch (WarCreationException e) {
            Logger.get().error("Failed to re-create war from database!", e);
        }

        return wars;
    }

    @Contract("_, _, _ -> new")
    public static @Nullable Side loadSide(Connection con, UUID warUUID, UUID uuid) {
        try {
            DSLContext context = DB.getContext(con);

            SidesRecord r = context
                .selectFrom(SIDES)
                .where(SIDES.WAR.equal(QueryUtils.UUIDUtil.toBytes(warUUID)))
                .and(SIDES.UUID.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetchOne();

            if (r == null)
                return null;

            // Side data
            BattleSide side = BattleSide.valueOf(r.getSide());
            BattleTeam team = BattleTeam.valueOf(r.getTeam());
            String name = r.getName();
            @Nullable Town town = TownyAPI.getInstance().getTown(QueryUtils.UUIDUtil.fromBytes(r.getTown()));
            Instant siegeGrace = r.getSiegeGrace().toInstant(ZoneOffset.UTC);
            Instant raidGrace = r.getRaidGrace().toInstant(ZoneOffset.UTC);

            // Load players
            Set<UUID> players = new HashSet<>();
            Set<UUID> playersSurrendered = new HashSet<>();
            Result<SidesPlayersRecord> resultPlayers = context.selectFrom(SIDES_PLAYERS)
                .where(SIDES_PLAYERS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetch();
            resultPlayers.forEach(
                record -> {
                    if (record.get(SIDES_PLAYERS.SURRENDERED).equals((byte) 0)) {
                        players.add(QueryUtils.UUIDUtil.fromBytes(record.getPlayer()));
                    } else {
                        playersSurrendered.add(QueryUtils.UUIDUtil.fromBytes(record.getPlayer()));
                    }
                }
            );

            // Load nations
            Set<Nation> nations = new HashSet<>();
            Set<Nation> nationsSurrendered = new HashSet<>();
            Result<SidesNationsRecord> resultNations = context.selectFrom(SIDES_NATIONS)
                .where(SIDES_NATIONS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetch();
            resultNations.forEach(
                record -> {
                    final UUID identifier = QueryUtils.UUIDUtil.fromBytes(record.getNation());
                    if (record.getSurrendered().equals((byte) 0)) {
                        @Nullable Nation nation = TownyAPI.getInstance().getNation(identifier);
                        if (nation != null)
                            nations.add(nation);
                    } else {
                        @Nullable Nation nation = TownyAPI.getInstance().getNation(identifier);
                        if (nation != null)
                            nationsSurrendered.add(nation);
                    }
                }
            );

            // Load towns
            Set<Town> towns = new HashSet<>();
            Set<Town> townsSurrendered = new HashSet<>();
            Result<SidesTownsRecord> resultTowns = context.selectFrom(SIDES_TOWNS)
                .where(SIDES_TOWNS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetch();
            resultTowns.forEach(
                record -> {
                    final UUID identifier = QueryUtils.UUIDUtil.fromBytes(record.getTown());
                    if (record.getSurrendered().equals((byte) 0)) {
                        @Nullable Town town2 = TownyAPI.getInstance().getTown(identifier);
                        if (town2 != null)
                            towns.add(town2);
                    } else {
                        @Nullable Town town2 = TownyAPI.getInstance().getTown(identifier);
                        if (town2 != null)
                            townsSurrendered.add(town2);
                    }
                }
            );

            // Load rallies
            List<RallyPoint> rallies = context.selectFrom(SIDES_SPAWNS)
                .where(SIDES_SPAWNS.SIDE.endsWith(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetch()
                .stream()
                .map(spawn -> {
                    final @Nullable World world = Bukkit.getWorld(UUIDUtil.fromBytes(spawn.getWorld()));
                    if (world == null)
                        return null;

                    final Location location = new Location(
                        world,
                        spawn.getX(),
                        spawn.getY(),
                        spawn.getZ(),
                        spawn.getYaw().floatValue(),
                        spawn.getPitch().floatValue()
                    );

                    final @Nullable World blockWorld = Bukkit.getWorld(UUIDUtil.fromBytes(spawn.getBlockWorld()));
                    if (blockWorld == null)
                        return null;

                    final Block block = blockWorld.getBlockAt(spawn.getX(), spawn.getY(), spawn.getZ());

                    return new RallyPoint(
                        spawn.get_Name(),
                        location,
                        SpawnType.RALLY,
                        block,
                        Bukkit.getOfflinePlayer(UUIDUtil.fromBytes(spawn.getCreator()))
                    );
                })
                .filter(Objects::nonNull)
                .toList();

            return Side.builder() // TODO IllegalStateException
                .setWarUUID(warUUID)
                .setUuid(uuid)
                .setLeader(town)
                .setSide(side)
                .setTeam(team)
                .setName(name)
                .setTowns(towns)
                .setNations(nations)
                .setPlayers(players)
                .setTownsSurrendered(townsSurrendered)
                .setNationsSurrendered(nationsSurrendered)
                .setPlayersSurrendered(playersSurrendered)
                .setSiegeGrace(siegeGrace)
                .setRaidGrace(raidGrace)
                .setRallies(rallies)
                .rebuild();
        } catch (SideCreationException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
        return null;
    }

    public static @NotNull Set<Siege> loadSieges(Connection con, War war) {
        Set<Siege> sieges = new HashSet<>();

        try {
            DSLContext context = DB.getContext(con);

            Result<Record> result = context.select()
                .from(SIEGES)
                .where(SIEGES.WAR.equal(QueryUtils.UUIDUtil.toBytes(war.getUUID())))
                .fetch();

            for (Record r : result) {
                UUID uuid = QueryUtils.UUIDUtil.fromBytes(r.get(SIEGES.UUID));
                @Nullable Town town = TownyAPI.getInstance().getTown(QueryUtils.UUIDUtil.fromBytes(r.get(SIEGES.TOWN)));
                OfflinePlayer siegeLeader = Bukkit.getOfflinePlayer(QueryUtils.UUIDUtil.fromBytes(r.get(SIEGES.SIEGE_LEADER)));
                Instant endTime = r.get(SIEGES.END_TIME).toInstant(ZoneOffset.UTC);
                Instant lastTouched = r.get(SIEGES.LAST_TOUCHED).toInstant(ZoneOffset.UTC);
                int siegeProgress = r.get(SIEGES.SIEGE_PROGRESS);
                Set<UUID> attackersIncludingOffline = new HashSet<>();
                Set<UUID> defendersIncludingOffline = new HashSet<>();

                Result<Record> resultPlayers = context.select()
                    .from(SIEGE_PLAYERS)
                    .where(SIEGE_PLAYERS.SIEGE.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                    .fetch();
                resultPlayers.forEach(
                    record -> {
                        if (record.get(SIEGE_PLAYERS.TEAM).equals((byte) 0)) {
                            attackersIncludingOffline.add(QueryUtils.UUIDUtil.fromBytes(record.get(SIEGE_PLAYERS.PLAYER)));
                        } else {
                            defendersIncludingOffline.add(QueryUtils.UUIDUtil.fromBytes(record.get(SIEGE_PLAYERS.PLAYER)));
                        }
                    }
                );

                SiegePhase phase;
                try {
                    phase = SiegePhase.valueOf(r.get(SIEGES.PHASE_CURRENT));
                } catch (IllegalArgumentException e) {
                    phase = SiegePhase.SIEGE;
                }
                int phaseProgress = r.get(SIEGES.PHASE_PROGRESS);
                Instant phaseStartTime = r.get(SIEGES.PHASE_START_TIME).toInstant(ZoneOffset.UTC);



                sieges.add(new Siege(
                    war,
                    uuid,
                    town,
                    siegeLeader,
                    endTime,
                    lastTouched,
                    siegeProgress,
                    attackersIncludingOffline,
                    defendersIncludingOffline,
                    phase,
                    phaseProgress,
                    phaseStartTime
                ));
            }
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }

        return sieges;
    }

    // Deleters

    public static void deleteWar(War war) {
        try (
            Connection con = DB.getConnection();
        ) {
            DSLContext context = DB.getContext(con);

            context
                .deleteFrom(LIST)
                .where(LIST.UUID.equal(QueryUtils.UUIDUtil.toBytes(war.getUUID())))
                .execute();
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }

    public static void deleteSiege(Siege siege) {
        try (
            Connection con = DB.getConnection();
        ) {
            DSLContext context = DB.getContext(con);

            context
                .deleteFrom(SIEGES)
                .where(SIEGES.UUID.equal(QueryUtils.UUIDUtil.toBytes(siege.getUUID())))
                .execute();
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }
}
