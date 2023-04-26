package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import me.ShermansWorld.AlathraWar.*;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Random;

public class AdminCommands implements CommandExecutor {

    public AdminCommands(final Main plugin) {
        plugin.getCommand("alathrawaradmin").setExecutor(this);
        plugin.getCommand("awa").setExecutor(this);
    }

    /**
     * //done
     * //edit war/event in real time, side can be "both" to effect both
     * -modify raid score [war] [town] [value]
     * -modify raid townspawn [war] [town] (x) (Z)
     * -modify raid gather [war] [town] [town]
     * -modify raid phase [war] [town] [phase] //"next" to move to next phase
     * -modify raid loot [war] [town] (x) (z) [value,looted,ticks,reset] [amt] //no coords just does current chunk, reset deletes it from the list
     * -modify raid time [war] [town] [add/set] [value]
     * -modify raid owner [war] [town] [add/set] [value]
     * -modify raid move [war] [town] [newWar] //low priority, moves raid to other war
     * -modify raid clearActive [war] [town] //low priority
     * <p>
     * -modify siege score [war] [town] [side] [amt]
     * -modify siege homeblock [war] [town] (x) (Z)
     * -modify siege time [war] [town] [add/set/max] [value] //max modified the max length
     * -modify siege owner [war] [town] [add/set] [value]
     * -modify siege move [war] [town] [newWar] //low priority, moves siege to other war
     * <p>
     * -modify war score [war] [side] [amt]
     * -modify war side [war]  [side] [name]
     * -modify war name [war] [name]
     * -modify war add town [war] [town]
     * -modify war add nation [war] [nation]
     * -modify war surrender town [war] [town] //adds town to surrender list
     * -modify war surrender nation [war] [town] //adds all towns to surrender list
     * -modify war raidTime [add,set,reset] [war] [town] [amt] //set when last raid was
     * <p>
     * // low priority idea
     * -info war score [war]
     * -info war surrenderedTowns [war]
     * -info war raids [war]
     * -info war sieges [war]
     * -info war towns [war]
     * -info war lastRaidTime [war]
     * <p>
     * -info raid homeblock [war] [town]
     * -info raid lootedTownBlocks [war] [town]
     * -info raid timeSinceLastRaid [town]
     * <p>
     * -info siege homeblock [war] [town]
     * <p>
     * //done
     * //force make event or war, if owner isnt defined, idk havent decided
     * -create siege [war] [town] (owner)
     * -create raid [war] [raidTown] (gatherTown) (owner)
     * -create war [name] [side1] [side2]
     * <p>
     * //force end a war/event, can declare winner side, or no winner
     * -force end war [war] (side/victor)
     * -force end siege [war] [town] (side/victor)
     * -force end raid [war] [town] (side/victor)
     * <p>
     * //done
     * //force player into or out of a war/event
     * -force join war [player] [war] [side]
     * -force join siege [player] [war] [town] (side)
     * -force join raid [player] [war] [town] (side)
     * <p>
     * //done
     * -force leave raid [war] [player] (timeout) //kicks from raid party
     * <p>
     * // Ultra low priority idea
     * -rule raidersRespawnAtGatherTown [true/false]
     * -rule siegersRespawnAtTown [true/false]
     * -rule anyoneCanJoinRaid [true/false]
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return Valid command
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player p = (Player) sender;
        if (!p.hasPermission("AlathraWar.admin")) {
            return fail(p, args, "permissions");
        }

        if (args.length == 0) {
            return fail(p, args, "syntax");
        } else if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                return create(p, args);
            } else if (args[0].equalsIgnoreCase("force")) {
                return force(p, args);
            } else if (args[0].equalsIgnoreCase("help")) {
                return help(p, args);
            } else if (args[0].equalsIgnoreCase("info")) {
                return info(p, args);
            } else if (args[0].equalsIgnoreCase("modify")) {
                return modify(p, args);
            } else if (args[0].equalsIgnoreCase("awa")) {
                return awa(p, args);
            }
        }
        return true;
    }


    /**
     * //force make event or war, if owner isn't defined, idk haven't decided
     * -create siege [war] [town] (owner)
     * -create raid [war] [raidTown] [gatherTown] (owner)
     * -create war [name] [side1] [side2]
     */
    private static boolean create(Player p, String[] args) {
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("raid")) {
                if (args.length >= 4) {
                    //specific behavior exists if an admin ran this
                    //using same method so that this doesnt get fucked up if we go back and change original implementation
                    RaidCommands.startRaid(p, args, true);
                } else {
                    //defaultCode will bypass the custom gather town to force set owner
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin create raid [war] [raidTown] (gatherTown/\"defaultCode\") (owner)"));
                }
                return true;
            } else if (args[1].equalsIgnoreCase("siege")) {
                //this exists to force a seige with a new owner
                if (args.length >= 4) {
                    String[] adjusted = new String[]{
                            "start",
                            args[2],
                            args[3],
                            args[4]
                    };
                    SiegeCommands.siegeStart(p, adjusted, true);
                    p.sendMessage(Helper.Chatlabel() + "Try again later!");
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin create siege [war] [town] (owner)"));
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("war")) {
                //this command basically is a copy or /war create
                if (args.length == 5) {
                    //should purge the first argument;
                    String[] adjusted = new String[]{
                            args[1],
                            args[2],
                            args[3],
                            args[4]
                    };
                    WarCommands.warCreate(p, adjusted);
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin create war [name] [side1] [side2]"));
                    return true;
                }
            }
        } else {
            return fail(p, args, "syntax");
        }
        return fail(p, args, "syntax");
    }

    /**
     * //force end a war/event, can declare winner side, or no winner
     * -force end war [name] (side/victor)
     * -force end siege [war] [town] (side/victor)
     * -force end raid [war] [town] (side/victor)
     * <p>
     * //force player into or out of a war/event
     * -force join siege [player] [war] [town] (side)
     * -force join raid [player] [war] [town] (side)
     * -force leave raid [war] [player] (timeout) //kicks from raid party
     * -force surrender war [name] [town]
     *
     * @param p player
     * @param args args
     * @return result
     */
    private static boolean force(Player p, String[] args) {
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("end")) {
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("raid")) {
                        for (Raid r : RaidData.getRaids()) {
                            if (args.length >= 6) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    if (r.getSide1AreRaiders()) {
                                        if (args[5].equals(r.getWar().getSide1())) {
                                            r.raidersWin(r.getOwner(), r.getRaidScore());
                                            p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (raiders) declared as victor.");
                                            Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (raiders) declared as victor.");
                                            return true;
                                        } else if (args[5].equals(r.getWar().getSide2())) {
                                            r.defendersWin(r.getRaidScore());
                                            p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("&cSide not found!"));
                                            return true;
                                        }
                                    } else {
                                        if (args[5].equals(r.getWar().getSide1())) {
                                            r.defendersWin(r.getRaidScore());
                                            p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            return true;
                                        } else if (args[5].equals(r.getWar().getSide2())) {
                                            r.raidersWin(r.getOwner(), r.getRaidScore());
                                            p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (raiders) declared as victor.");
                                            Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (raiders) declared as victor.");
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("&cSide not found!"));
                                            return true;
                                        }
                                    }
                                }
                            } else if (args.length == 5) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    r.noWinner();
                                    p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with no victor.");
                                    Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with no victor.");
                                    return true;
                                }
                            } else {
                                p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force end raid [war] [town] (side)"));
                                return true;
                            }
                        }
                        p.sendMessage(Helper.color("&cRaid not found!"));
                        return true;

                    } else if (args[2].equalsIgnoreCase("siege")) {
                        for (Siege s : SiegeData.getSieges()) {
                            if (args.length >= 6) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    if (s.getSide1AreAttackers()) {
                                        if (args[5].equals(s.getWar().getSide1())) {
                                            s.attackersWin(s.getSiegeOwner());
                                            p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (attackers) declared as victor.");
                                            Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (attackers) declared as victor.");
                                            return true;
                                        } else if (args[5].equals(s.getWar().getSide2())) {
                                            s.defendersWin();
                                            p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("&cSide not found!"));
                                            return true;
                                        }
                                    } else {
                                        if (args[5].equals(s.getWar().getSide1())) {
                                            s.defendersWin();
                                            p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            return true;
                                        } else if (args[5].equals(s.getWar().getSide2())) {
                                            s.attackersWin(s.getSiegeOwner());
                                            p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (attackers) declared as victor.");
                                            Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (attackers) declared as victor.");
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("&cSide not found!"));
                                            return true;
                                        }
                                    }
                                }
                            } else if (args.length == 5) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    s.noWinner();
                                    p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with no victor.");
                                    Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with no victor.");
                                    return true;
                                }
                            } else {
                                p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force end siege [war] [town] (side)"));
                                return true;
                            }
                        }
                        p.sendMessage(Helper.color("&cSiege not found!"));
                        return true;
                    } else if (args[2].equalsIgnoreCase("war")) {
                        //TODO determine if needed
                        p.sendMessage(Helper.color("&cUnused! use /war delete"));
                        return true;
                    } else {
                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force end [raid/siege]"));
                        return true;
                    }
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force end [raid/siege]"));
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("join")) {
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("raid")) {
                        if (args.length >= 6) {
                            //fix args
                            String[] adjusted = new String[]{
                                    args[1], //join
                                    args[4], //war
                                    args[5], //town
                                    args[3], //player
                                    args.length >= 7 ? args[6] : null //side
                            };
                            RaidCommands.joinRaid(p, adjusted, true);
                            return true;
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force join raid [player] [war] [town] (side)"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("siege")) {
                        if (args.length >= 6) {
                            p.sendMessage(Helper.color("&cError! Unimplemented!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force join siege [player] [war] [town] (side)"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("war")) {
                        if (args.length >= 6) {
                            //fix args to match
                            String[] adjusted = new String[]{
                                    args[1],
                                    args[4],
                                    args[5],
                                    args[3]
                            };
                            if (Bukkit.getPlayer(args[3]) != null) {
                            } else {
                                p.sendMessage(Helper.color("c") + args[3] + " does not exist!");
                                p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force join war [player] [war] [side]"));
                                return true;
                            }
                            WarCommands.warJoin(p, adjusted, true);
                            p.sendMessage(Helper.color("&cForced " + args[3] + " to join the war " + args[4] + " on side " + args[5]));
                            Main.warLogger.log("Forced " + args[3] + " to join the war " + args[4] + " on side " + args[5]);
                            return true;
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force join war [player] [war] [side]"));
                            return true;
                        }
                    } else {
                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force join [raid/siege/war]"));
                        return true;
                    }
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force join [raid/siege/war]"));
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("leave")) {
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("raid")) {
                        if (args.length >= 6) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    if (Bukkit.getPlayer(args[5]) != null) {
                                        String[] fixed = new String[]{
                                                args[1],
                                                args[3],
                                                args[4],
                                                args[5]
                                        };
                                        RaidCommands.leaveRaid(p, fixed, true);
                                        p.sendMessage(Helper.Chatlabel() + "Forced player " + args[5] + " to leave raid on " + args[4] + " in war " + args[3]);
                                        Main.warLogger.log("Forced player " + args[5] + " to leave raid on " + args[4] + " in war " + args[3]);
                                        return finalizeRaid(r);
                                    } else {
                                        p.sendMessage(Helper.color("&cPlayer not found!"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("&cRaid not found!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force leave raid [war] [town] [player] (timeout)"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("siege")) {
                        //TODO determine if needed
                        p.sendMessage(Helper.color("&cUnused! use /siege abandon or /war surrender"));
                        return true;
                    } else if (args[2].equalsIgnoreCase("war")) {
                        //TODO determine if needed
                        p.sendMessage(Helper.color("&cUnused! use /war surrender"));
                        return true;
                    } else {
                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force leave [raid/siege/war]"));
                        return true;
                    }
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin force leave [raid/siege/war]"));
                    return true;
                }
            } else {
                return fail(p, args, "syntax");
            }
        }
        return fail(p, args, "syntax");
    }

    private static boolean help(Player p, String[] args) {
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin create");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin force");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin help");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin info");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin modify");
        return true;
    }

    /**
     * // low priority idea
     * -info war [war]
     * <p>
     * -info raid [war] [town]
     * <p>
     * -info siege [war] [town]
     *
     * @param p player
     * @param args args
     * @return result
     */
    private static boolean info(Player p, String[] args) {
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("war")) {
                if (args.length >= 3) {
                    for (War w : WarData.getWars()) {
                        if (w.getName().equals(args[2])) {
                            p.sendMessage(Helper.Chatlabel() + "Info dump for war: " + w.getName());
                            p.sendMessage(Helper.Chatlabel() + "oOo------------===------------oOo");
                            p.sendMessage(Helper.Chatlabel() + "Name: " + w.getName());
                            p.sendMessage(Helper.Chatlabel() + "Side 1: " + w.getSide1());
                            p.sendMessage(Helper.Chatlabel() + "Side 2: " + w.getSide2());
                            p.sendMessage(Helper.Chatlabel() + "Last Raid for Side 1: " + new Timestamp(((long) w.getLastRaidTimeSide1()) * 1000L));
                            p.sendMessage(Helper.Chatlabel() + "Last Raid for Side 2: " + new Timestamp(((long) w.getLastRaidTimeSide2()) * 1000L));
                            p.sendMessage(Helper.Chatlabel() + "oOo------------===------------oOo");
                            StringBuilder side1Towns = new StringBuilder();
                            StringBuilder side1Players = new StringBuilder();
                            for (String t : w.getSide1Towns()) {
                                side1Towns.append(t);
                                side1Towns.append(", ");
                            }
                            for (String pl : w.getSide1Players()) {
                                side1Players.append(pl);
                                side1Players.append(", ");
                            }
                            //cut off last two characters
                            if(side1Towns.length() > 2) side1Towns = new StringBuilder(side1Towns.substring(0, side1Towns.length() - 2));
                            if(side1Players.length() > 2) side1Players = new StringBuilder(side1Players.substring(0, side1Players.length() - 2));
                            p.sendMessage(Helper.Chatlabel() + w.getSide1() + " Towns: " + side1Towns);
                            p.sendMessage(Helper.Chatlabel() + w.getSide1() + " Players: " + side1Players);

                            p.sendMessage(Helper.Chatlabel() + "oOo------------===------------oOo");
                            StringBuilder side2Towns = new StringBuilder();
                            StringBuilder side2Players = new StringBuilder();
                            for (String t : w.getSide2Towns()) {
                                side2Towns.append(t);
                                side2Towns.append(", ");
                            }
                            for (String pl : w.getSide2Players()) {
                                side2Players.append(pl);
                                side2Players.append(", ");
                            }
                            //cut off last two characters
                            if(side2Towns.length() > 2) side2Towns = new StringBuilder(side2Towns.substring(0, side2Towns.length() - 2));
                            if(side2Players.length() > 2) side2Players = new StringBuilder(side2Players.substring(0, side2Players.length() - 2));
                            p.sendMessage(Helper.Chatlabel() + w.getSide2() + " Towns: " + side2Towns);
                            p.sendMessage(Helper.Chatlabel() + w.getSide2() + " Players: " + side2Players);

                            p.sendMessage(Helper.Chatlabel() + "oOo------------===------------oOo");
                            StringBuilder surrenderedTowns = new StringBuilder();
                            for (String t : w.getSurrenderedTowns()) {
                                surrenderedTowns.append(t);
                                surrenderedTowns.append(", ");
                            }
                            //cut off last two characters
                            if(surrenderedTowns.length() > 2) surrenderedTowns = new StringBuilder(surrenderedTowns.substring(0, surrenderedTowns.length() - 2));
                            p.sendMessage(Helper.Chatlabel() + "Surrendered Towns: " + surrenderedTowns);
                            return true;
                        }
                    }
                    p.sendMessage(Helper.color("&cError Raid not found!"));
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin info [raid/siege/war]"));
                }
                return true;
            } else if (args[1].equalsIgnoreCase("raid")) {
                if (args.length >= 4) {
                    for (Raid r : RaidData.getRaids()) {
                        if (r.getWar().getName().equals(args[2]) && r.getRaidedTown().getName().equals(args[3])) {
                            p.sendMessage(Helper.Chatlabel() + "Info dump for raid: " + r.getName());
                            p.sendMessage(Helper.Chatlabel() + "oOo------------===------------oOo");
                            p.sendMessage(Helper.Chatlabel() + "Name: " + r.getName());
                            p.sendMessage(Helper.Chatlabel() + "Raiders: " + r.getRaiders());
                            p.sendMessage(Helper.Chatlabel() + "Defenders: " + r.getDefenders());
                            p.sendMessage(Helper.Chatlabel() + "Raid Score: " + r.getRaidScore());
                            p.sendMessage(Helper.Chatlabel() + "War: " + r.getWar().getName());
                            p.sendMessage(Helper.Chatlabel() + "Raided Town: " + r.getRaidedTown().getName());
                            p.sendMessage(Helper.Chatlabel() + "Gather Town: " + r.getGatherTown().getName());
                            p.sendMessage(Helper.Chatlabel() + "Current Phase: " + r.getPhase().name());
                            p.sendMessage(Helper.Chatlabel() + "Tick progress: " + r.getRaidTicks());
                            p.sendMessage(Helper.Chatlabel() + "Owner: " + r.getOwner().getName());
                            p.sendMessage(Helper.Chatlabel() + "Gather Homeblock: " + r.getHomeBlockGather().toString());
                            p.sendMessage(Helper.Chatlabel() + "Raided Homeblock: " + r.getHomeBlockRaided().toString());
                            p.sendMessage(Helper.Chatlabel() + "oOo------------===------------oOo");
                            StringBuilder activeRaiders = new StringBuilder();
                            for (String pl : r.getActiveRaiders()) {
                                activeRaiders.append(pl);
                                activeRaiders.append(", ");
                            }
                            //cut off last two characters
                            if(activeRaiders.length() > 2) activeRaiders = new StringBuilder(activeRaiders.substring(0, activeRaiders.length() - 2));
                            p.sendMessage(Helper.Chatlabel() + "Raiding Players: " + activeRaiders);
                            return true;
                        }
                    }
                    p.sendMessage(Helper.color("&cError Raid not found!"));
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin info [raid/siege/war]"));
                }
                return true;
            } else if (args[1].equalsIgnoreCase("siege")) {
                if (args.length >= 4) {
                    for (Siege s : SiegeData.getSieges()) {
                        if (s.getWar().getName().equals(args[2]) && s.getTown().getName().equals(args[3])) {
                            p.sendMessage(Helper.Chatlabel() + "Info dump for siege: " + s.getName());
                            p.sendMessage(Helper.Chatlabel() + "oOo------------===------------oOo");
                            p.sendMessage(Helper.Chatlabel() + "Name: " + s.getName());
                            p.sendMessage(Helper.Chatlabel() + "Attackers: " + s.getAttackers());
                            p.sendMessage(Helper.Chatlabel() + "Defenders: " + s.getDefenders());
                            p.sendMessage(Helper.Chatlabel() + "Attacker points: " + s.getAttackerPoints());
                            p.sendMessage(Helper.Chatlabel() + "Defender points: " + s.getDefenderPoints());
                            p.sendMessage(Helper.Chatlabel() + "War: " + s.getWar().getName());
                            p.sendMessage(Helper.Chatlabel() + "Attacked Town: " + s.getTown().getName());
                            p.sendMessage(Helper.Chatlabel() + "Max Ticks: " + s.getMaxSiegeTicks());
                            p.sendMessage(Helper.Chatlabel() + "Tick progress: " + s.getSiegeTicks());
                            p.sendMessage(Helper.Chatlabel() + "Owner: " + s.getSiegeOwner());
                            p.sendMessage(Helper.Chatlabel() + "Homeblock: " + s.getHomeBlock().toString());
                            p.sendMessage(Helper.Chatlabel() + "oOo------------===------------oOo");
                            StringBuilder attackers = new StringBuilder();
                            for (String pl : s.getAttackerPlayers()) {
                                attackers.append(pl);
                                attackers.append(", ");
                            }
                            //cut off last two characters
                            if(attackers.length() > 2) attackers = new StringBuilder(attackers.substring(0, attackers.length() - 2));
                            p.sendMessage(Helper.Chatlabel() + "Attacking Players: " + attackers);
                            StringBuilder defenders = new StringBuilder();
                            for (String pl : s.getAttackerPlayers()) {
                                defenders.append(pl);
                                defenders.append(", ");
                            }
                            //cut off last two characters
                            if(defenders.length() > 2) defenders = new StringBuilder(defenders.substring(0, defenders.length() - 2));
                            p.sendMessage(Helper.Chatlabel() + "Defending Players: " + defenders);
                            return true;
                        }
                    }
                    p.sendMessage(Helper.color("&cError Siege not found!"));
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin info [raid/siege/war]"));
                }
                return true;
            } else {
                return fail(p, args, "syntax");
            }
        }
        return fail(p, args, "syntax");
    }

    /**
     * //edit war/event in real time, side can be "both" to effect both
     * -modify raid score [add/set] [war] [town] [value]
     * -modify raid townspawn [war] [town] (x) (y) (Z)
     * -modify raid gather [war] [town] [town]
     * -modify raid phase [war] [town] [phase] //"next" to move to next phase
     * -modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)  //no coords just does current chunk, reset deletes it from the list
     * -modify raid time [war] [town] [add/set] [value]
     * -modify raid owner [war] [town] [player]
     * -modify raid move [war] [town] [newWar] //low priority, moves raid to other war/ town
     * -modify raid clearActive [war] [town] //low priority
     * <p>
     * -modify siege score [war] [town] [side] [amt]
     * -modify siege townspawn [war] [town] (x) (Z)
     * -modify siege time [war] [town] [add/set/max] [value] //max modified the max length
     * -modify siege owner [war] [town] [add/set] [value]
     * -modify siege move [war] [town] [newWar] //low priority, moves siege to other war/town
     * <p>
     * -modify war score [war] [side] [amt]
     * -modify war side [war]  [side] [name]
     * -modify war name [war] [name]
     * -modify war add town [war] [town]
     * -modify war add nation [war] [nation]
     * -modify war surrender town [war] [town] //adds town to surrender list
     * -modify war surrender nation [war] [town] //adds all towns to surrender list
     * -modify war raidTime [add,set,reset] [war] [town] [amt] //set when last raid was
     *
     * @param p player
     * @param args args
     * @return result
     */
    private static boolean modify(Player p, String[] args) {
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("raid")) {
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("score")) {
                        if (args.length >= 7) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    if (args[5].equalsIgnoreCase("add")) {
                                        r.addPointsToRaidScore(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Added " + args[6] + " points to the raid score in the war " + args[3] + " on town " + args[4]);
                                        Main.warLogger.log("Added " + args[6] + " points to the raid score in the war " + args[3] + " on town " + args[4]);
                                        return finalizeRaid(r);
                                    } else if (args[5].equalsIgnoreCase("subtract")) {
                                        r.subtractPointsFromRaidScore(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Subtracted " + args[6] + " points to the raid score in the war " + args[3] + " on town " + args[4]);
                                        Main.warLogger.log("Subtracted " + args[6] + " points to the raid score in the war " + args[3] + " on town " + args[4]);
                                        return finalizeRaid(r);
                                    } else if (args[5].equalsIgnoreCase("set")) {
                                        r.setRaidScore(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set " + args[6] + " points as the raid score in the war " + args[3] + " on town " + args[4]);
                                        Main.warLogger.log("Set " + args[6] + " points as the raid score in the war " + args[3] + " on town " + args[4]);
                                        return finalizeRaid(r);
                                    } else {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid score [add/subtract/set] [war] [town] [value]"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cRaid cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid score [add/subtract/set] [war] [town] [value]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("townspawn")) {
                        if (args.length >= 5) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    if (args.length == 6 || args.length == 7) {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid townspawn [war] [town] (x) (Z)"));
                                        return true;
                                    }
                                    Town t = r.getRaidedTown();
                                    if (args.length >= 8) {
                                        if (p.getWorld() == t.getWorld()) {
                                            try {
                                                WorldCoord tb = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) Double.parseDouble(args[5]), (int) Double.parseDouble(args[7]));
                                                if (t.hasTownBlock(tb)) {
                                                    t.setHomeBlock(tb.getTownBlock());
                                                    t.setSpawn(new Location(p.getWorld(), Double.parseDouble(args[5]), Double.parseDouble(args[6]), Double.parseDouble(args[7])));
                                                    r.setHomeBlockRaided(tb.getTownBlock());
                                                    r.setTownSpawnRaided(t.getSpawn());
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for raided town " + args[4] + " in war " + args[3] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for raided town " + args[4] + " in war " + args[3] + " to [" + args[5] + "," + args[6] + "," + args[7] + "]");
                                                    return finalizeRaid(r);
                                                } else {
                                                    p.sendMessage(Helper.color("&cTown does not contain town block at [" + args[5] + "," + args[7] + "]"));
                                                    return true;
                                                }
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("&cError! Townblock does not exist!"));
                                                return true;
                                            } catch (TownyException e) {
                                                p.sendMessage(Helper.color("&cError!"));
                                                return true;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("&cError! Wrong world!"));
                                            return true;
                                        }
                                    } else {
                                        if (p.getWorld() == t.getWorld()) {
                                            try {
                                                WorldCoord tb = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) p.getLocation().getX(), (int) p.getLocation().getZ());
                                                if (t.hasTownBlock(tb)) {
                                                    t.setHomeBlock(tb.getTownBlock());
                                                    t.setSpawn(p.getLocation());
                                                    r.setHomeBlockRaided(tb.getTownBlock());
                                                    r.setTownSpawnRaided(t.getSpawn());
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for raided town " + args[4] + " in war " + args[3] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for raided town " + args[4] + " in war " + args[3] + " to " + p.getLocation().toString());
                                                    return finalizeRaid(r);
                                                } else {
                                                    p.sendMessage(Helper.color("&cTown does not contain town block at your location [" + (int) p.getLocation().getX() + "," + (int) p.getLocation().getZ() + "]"));
                                                    return true;
                                                }
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("&cError! Townblock does not exist!"));
                                                return true;
                                            } catch (TownyException e) {
                                                p.sendMessage(Helper.color("&cError!"));
                                                return true;
                                            }
                                        } else {
                                            return true;
                                        }
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cRaid cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid homeblock [war] [town] (x) (Z)"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("gather")) {
                        if (args.length >= 6) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    Town t = TownyAPI.getInstance().getTown(args[5]);
                                    if (t != null) {
                                        r.setRaidedTown(t);
                                        try {
                                            r.setTownSpawnGather(t.getSpawn());
                                            r.setHomeBlockGather(t.getHomeBlock());
                                        } catch (TownyException e) {
                                            p.sendMessage(Helper.Chatlabel() + "Error!");
                                            throw new RuntimeException(e);
                                        }
                                        p.sendMessage(Helper.Chatlabel() + "Set town new gather town " + t.getName() + " for raid against " + args[4] + " in war " + args[3]);
                                        Main.warLogger.log("Set town new gather town " + t.getName() + " for raid against " + args[4] + " in war " + args[3]);
                                        return finalizeRaid(r);
                                    } else {
                                        p.sendMessage(Helper.color("&cTown does not exists!"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cRaid cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid gather [war] [town] [town]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("phase")) {
                        if (args.length >= 6) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    //parse phase
                                    RaidPhase ph;
                                    if (args[5].equalsIgnoreCase("next")) {
                                        ph = RaidPhase.getNext(r.getPhase());
                                    } else {
                                        ph = RaidPhase.getByName(args[5]);
                                    }

                                    //if we found it
                                    if (ph != null) {
                                        r.setPhase(ph);
                                        r.setRaidTicks(ph.startTick);

                                        p.sendMessage(Helper.Chatlabel() + "Set phase for raid against " + args[4] + " in war " + args[3] + " to " + ph.name());
                                        Main.warLogger.log("Set phase for raid against " + args[4] + " in war " + args[3] + " to " + ph.name());
                                        return finalizeRaid(r);
                                    } else {
                                        p.sendMessage(Helper.color("&cPhase does not exist!"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cRaid cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid phase [war] [town] [phase]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("loot")) {
                        if (args.length >= 5) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    //parse phase
                                    if (!p.getWorld().equals(r.getRaidedTown().getWorld())) {
                                        p.sendMessage(Helper.Chatlabel() + "Error wrong world");
                                        return true;
                                    }
                                    if (args.length >= 9) {
                                        if (args[5].equalsIgnoreCase("value")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) Double.parseDouble(args[7]), (int) Double.parseDouble(args[8]));
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            if(lb == null) {
                                                r.addLootedChunk(wc);
                                                lb = r.getLootedChunks().get(wc);
                                                p.sendMessage(Helper.Chatlabel() + "Created empty loot chunk data to modify");
                                            }
                                            lb.value = Double.parseDouble(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set value for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set value for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return finalizeRaid(r);
                                        } else if (args[5].equalsIgnoreCase("looted")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) Double.parseDouble(args[7]), (int) Double.parseDouble(args[8]));
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            //The base value can be abjusted
                                            if(!lb.finished && Boolean.parseBoolean(args[6])) {
                                                lb.value = new Random().nextDouble() * 100;
                                                //score for looting
                                                r.addPointsToRaidScore(10);
                                                p.sendMessage(Helper.Chatlabel() + "Gave value and score because flag was false before.");
                                                Main.warLogger.log("Gave value and score because flag was false before.");
                                            }

                                            lb.finished = Boolean.parseBoolean(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set finished flag for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set finished flag for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return finalizeRaid(r);
                                        } else if (args[5].equalsIgnoreCase("ticks")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) Double.parseDouble(args[7]), (int) Double.parseDouble(args[8]));
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.ticks = Integer.parseInt(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set ticks for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set ticks for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return finalizeRaid(r);
                                        } else if (args[5].equalsIgnoreCase("reset")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) Double.parseDouble(args[6]), (int) Double.parseDouble(args[7]));
                                            r.getLootedChunks().remove(wc);
                                            p.sendMessage(Helper.Chatlabel() + "Reset loot for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3]);
                                            Main.warLogger.log("Reset loot for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3]);
                                            return finalizeRaid(r);
                                        } else {
                                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)"));
                                            return true;
                                        }
                                    } else if (args.length == 8) {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)"));
                                        return true;
                                    } else {
                                        if (args[5].equalsIgnoreCase("value")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.value = Integer.parseInt(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set value for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set value for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return finalizeRaid(r);
                                        } else if (args[5].equalsIgnoreCase("looted")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.finished = Boolean.parseBoolean(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set looted status for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set looted status for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return finalizeRaid(r);
                                        } else if (args[5].equalsIgnoreCase("ticks")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.ticks = Integer.parseInt(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set ticks for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set ticks for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return finalizeRaid(r);
                                        } else if (args[5].equalsIgnoreCase("reset")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            r.getLootedChunks().remove(wc);
                                            p.sendMessage(Helper.Chatlabel() + "Reset loot for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3]);
                                            Main.warLogger.log("Reset Loot for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3]);
                                            return finalizeRaid(r);
                                        } else {
                                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)"));
                                            return true;
                                        }
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cRaid cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("time")) {
                        if (args.length >= 7) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    //parse phase
                                    if (args[5].equalsIgnoreCase("add")) {
                                        r.setRaidTicks(r.getRaidTicks() + Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set time for raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        Main.warLogger.log("Set time for raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        return finalizeRaid(r);
                                    } else if (args[5].equalsIgnoreCase("set")) {
                                        int t = Integer.parseInt(args[6]);
                                        if (t >= r.getPhase().startTick) {
                                            r.setRaidTicks(t);
                                            p.sendMessage(Helper.Chatlabel() + "Set time for raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set time for raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return finalizeRaid(r);
                                        } else {
                                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cTime set before current phase, use \"/alathrawaradmin modify raid phase\" instead"));
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid time [war] [town] [add/set] [value]"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cRaid cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid time [war] [town] [add/set] [value]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("owner")) {
                        if (args.length >= 6) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    Player own = Bukkit.getPlayer(args[5]);
                                    if (own != null) {
                                        if(r.getActiveRaiders().contains(own.getName())) {
                                            r.setOwner(own);
                                            p.sendMessage(Helper.Chatlabel() + "Set owner of raid against " + args[4] + " in war " + args[3] + " to " + own.getName());
                                            Main.warLogger.log("Set owner of raid against " + args[4] + " in war " + args[3] + " to " + own.getName());
                                            return finalizeRaid(r);
                                        } else {
                                            p.sendMessage(Helper.Chatlabel() + "Player is not an active raider!");
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("&cPlayer not found!"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cRaid cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid owner [war] [town] [player]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("move")) {
                        //TODO later
                        p.sendMessage(Helper.color("&cError! Not implemented!"));
                        return true;
                    } else if (args[2].equalsIgnoreCase("clearActive")) {
                        //TODO later
                        p.sendMessage(Helper.color("&cError! Not implemented!"));
                        return true;
                    } else {
                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid [propery]"));
                        return true;
                    }
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify [raid/siege/war] [propery]"));
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("siege")) {
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("score")) {
                        if (args.length >= 8) {
                            for (Siege s : SiegeData.getSieges()) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    if (args[5].equalsIgnoreCase("add")) {
                                        if (s.getWar().getSide1().equals(args[6])) {
                                            if (s.getSide1AreAttackers()) {
                                                s.addPointsToAttackers(Integer.parseInt(args[7]));
                                            } else {
                                                s.addPointsToDefenders(Integer.parseInt(args[7]));
                                            }
                                            p.sendMessage(Helper.Chatlabel() + "Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                            Main.warLogger.log("Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                            return finalizeSiege(s);
                                        } else if (s.getWar().getSide2().equals(args[6])) {
                                            if (s.getSide1AreAttackers()) {
                                                s.addPointsToDefenders(Integer.parseInt(args[7]));
                                            } else {
                                                s.addPointsToAttackers(Integer.parseInt(args[7]));
                                            }
                                            p.sendMessage(Helper.Chatlabel() + "Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                            Main.warLogger.log("Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                            return finalizeSiege(s);
                                        } else {
                                            p.sendMessage(Helper.color("&cSide not found"));
                                            return true;
                                        }
                                    } else if (args[5].equalsIgnoreCase("set")) {
                                        if (s.getWar().getSide1().equals(args[6])) {
                                            if (s.getSide1AreAttackers()) {
                                                s.setAttackerPoints(Integer.parseInt(args[7]));
                                            } else {
                                                s.setDefenderPoints(Integer.parseInt(args[7]));
                                            }
                                            p.sendMessage(Helper.Chatlabel() + "Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                            Main.warLogger.log("Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                            return finalizeSiege(s);
                                        } else if (s.getWar().getSide2().equals(args[6])) {
                                            if (s.getSide1AreAttackers()) {
                                                s.setDefenderPoints(Integer.parseInt(args[7]));
                                            } else {
                                                s.setAttackerPoints(Integer.parseInt(args[7]));
                                            }
                                            p.sendMessage(Helper.Chatlabel() + "Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                            Main.warLogger.log("Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                            return finalizeSiege(s);
                                        } else {
                                            p.sendMessage(Helper.color("&cSide not found"));
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify siege score [war] [town] [add/set] [side] [amt]"));
                                        return true;
                                    }
                                } else {
                                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify siege score [war] [town] [add/set] [side] [amt]"));
                                    return true;
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cSiege cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify siege score [war] [town] [add/set] [side] [amt]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("townspawn")) {
                        if (args.length >= 5) {
                            for (Siege s : SiegeData.getSieges()) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    if (args.length == 6 || args.length == 7) {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify siege townspawn [war] [town] (x) (y) (Z)"));
                                        return true;
                                    }
                                    Town t = s.getTown();
                                    if (args.length >= 8) {
                                        if (p.getWorld() == t.getWorld()) {
                                            try {
                                                WorldCoord tb = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) Double.parseDouble(args[5]), (int) Double.parseDouble(args[7]));
                                                if (t.hasTownBlock(tb)) {
                                                    t.setHomeBlock(tb.getTownBlock());
                                                    t.setSpawn(new Location(p.getWorld(), (int) Double.parseDouble(args[5]), (int) Double.parseDouble(args[6]), (int) Double.parseDouble(args[7])));
                                                    s.setHomeBlock(tb.getTownBlock());
                                                    s.setTownSpawn(t.getSpawn());
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for sieged town " + args[4] + " in war " + args[3] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for sieged town " + args[4] + " in war " + args[3] + " to [" + args[5] + "," + args[6] + "," + args[7] + "]");
                                                    return finalizeSiege(s);
                                                } else {
                                                    p.sendMessage(Helper.color("&cTown does not contain town block at [" + args[5] + "," + args[7] + "]"));
                                                    return true;
                                                }
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("&cError! Townblock does not exist!"));
                                                return true;
                                            } catch (TownyException e) {
                                                p.sendMessage(Helper.color("&cError!"));
                                                return true;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("&cError! Wrong world!"));
                                            return true;
                                        }
                                    } else {
                                        if (p.getWorld() == t.getWorld()) {
                                            try {
                                                WorldCoord tb = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) p.getLocation().getX(), (int) p.getLocation().getZ());
                                                if (t.hasTownBlock(tb)) {
                                                    t.setHomeBlock(tb.getTownBlock());
                                                    t.setSpawn(p.getLocation());
                                                    s.setHomeBlock(tb.getTownBlock());
                                                    s.setTownSpawn(t.getSpawn());
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for sieged town " + args[4] + " in war " + args[3] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for sieged town " + args[4] + " in war " + args[3] + " to " + p.getLocation().toString());
                                                    return finalizeSiege(s);
                                                } else {
                                                    p.sendMessage(Helper.color("&cTown does not contain town block at your location [" + (int) p.getLocation().getX() + "," + (int) p.getLocation().getZ() + "]"));
                                                    return true;
                                                }
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("&cError! Townblock does not exist!"));
                                                return true;
                                            } catch (TownyException e) {
                                                p.sendMessage(Helper.color("&cError!"));
                                                return true;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify siege townspawn [war] [town] (x) (y) (Z)"));
                                            return true;
                                        }
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cSiege cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify siege townspawn [war] [town] (x) (y) (Z)"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("time")) {
                        if (args.length >= 7) {
                            for (Siege s : SiegeData.getSieges()) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    //parse phase
                                    if (args[5].equalsIgnoreCase("add")) {
                                        s.setSiegeTicks(s.getSiegeTicks() + Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set time for siege against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        Main.warLogger.log("Set time for siege against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        return finalizeSiege(s);
                                    } else if (args[5].equalsIgnoreCase("set")) {
                                        s.setSiegeTicks(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set time for siege against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        Main.warLogger.log("Set time for siege against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        return finalizeSiege(s);
                                    } else {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify raid time [war] [town] [add/set] [value]"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cSiege cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify siege time [war] [town] [add/set/max] [value]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("owner")) {
                        if (args.length >= 7) {
                            for (Siege s : SiegeData.getSieges()) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    Player own = Bukkit.getPlayer(args[5]);
                                    if (own != null) {
                                        if (s.attackerPlayers.contains(own.getName())) {
                                            s.setSiegeOwner(own);
                                            p.sendMessage(Helper.Chatlabel() + "Set owner of siege against " + args[4] + " in war " + args[3] + " to " + own.getName());
                                            Main.warLogger.log("Set owner of siege against " + args[4] + " in war " + args[3] + " to " + own.getName());
                                            return finalizeSiege(s);
                                        } else {
                                            p.sendMessage(Helper.color("&cPlayer not on attacking side!"));
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("&cPlayer not found!"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("&cSiege cannot be found."));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify siege owner [war] [town] [newOwner]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("move")) {
                        //TODO later
                        p.sendMessage(Helper.color("&cError!"));
                        return true;
                    } else {
                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify [raid/siege/war] [propery]"));
                        return true;
                    }
                } else {
                    p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify [raid/siege/war] [propery]"));
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("war")) {
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("score")) {
                        if (args.length >= 6) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    //TODO war score
                                    p.sendMessage(Helper.color("&cError! Feature unimplemented!"));
                                    return true;
                                }
                            }
                            p.sendMessage(Helper.color("&cError: War not found!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war score [war] [side] [add/set] [amt]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("side")) {
                        if (args.length >= 6) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    if (w.getSide1().equals(args[4])) {
                                        w.setSide1(args[5]);
                                        w.save();
                                        p.sendMessage(Helper.Chatlabel() + "Set side " + args[4] + " to " + args[5] + " in war " + args[3]);
                                        Main.warLogger.log(Helper.Chatlabel() + "Set side " + args[4] + " to " + args[5] + " in war " + args[3]);
                                        return finalizeWar(w);
                                    } else if (w.getSide2().equals(args[4])) {
                                        w.setSide2(args[5]);
                                        w.save();
                                        p.sendMessage(Helper.Chatlabel() + "Set side " + args[4] + " to " + args[5] + " in war " + args[3]);
                                        Main.warLogger.log(Helper.Chatlabel() + "Set side " + args[4] + " to " + args[5] + " in war " + args[3]);
                                        return finalizeWar(w);
                                    } else {
                                        p.sendMessage(Helper.color("&cError: Side not found!"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("&cError: War not found!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war side [war] [side] [name]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("name")) {
                        if (args.length >= 5) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    w.setName(args[4]);
                                    p.sendMessage(Helper.Chatlabel() + "Set name of war " + args[3] + " to " + args[4]);
                                    Main.warLogger.log(Helper.Chatlabel() + "Set name of war " + args[3] + " to " + args[4]);
                                    return finalizeWar(w);
                                }
                            }
                            p.sendMessage(Helper.color("&cError: War not found!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war name [war] [name]"));
                        }
                        return true;
                    }
                    //TODO If a town is surrendered, use last arg as flag to force unsurrender
                    else if (args[2].equalsIgnoreCase("add")) {
                        if (args.length >= 7) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    if (!w.getSide1().equals(args[4]) && !w.getSide2().equals(args[4])) {
                                        p.sendMessage(Helper.color("&cError: Side not found!"));
                                        return true;
                                    }
                                    if (args[5].equalsIgnoreCase("town")) {
                                        Town t = TownyAPI.getInstance().getTown(args[6]);
                                        if (t != null) {
                                            if(args.length >= 8) {
                                                //forceful add, ignore surrender and override it if the town is
                                                if(Boolean.parseBoolean(args[7])) {
                                                    //if we already have surrendered
                                                    if(w.getSurrenderedTowns().contains(args[6])) {
                                                        w.unSurrenderTown(args[6]);
                                                        p.sendMessage(Helper.Chatlabel() + "Un-surrendered town " + args[6]);
                                                        Main.warLogger.log(Helper.Chatlabel() + "Un-surrendered town " + args[6]);
                                                    }
                                                    if(w.getSide1().equals(args[4])) {
                                                        if(w.getSide2Towns().remove(args[6])) {
                                                            p.sendMessage(Helper.Chatlabel() + "Removed town " + args[6] + " from side " + w.getSide2());
                                                            Main.warLogger.log(Helper.Chatlabel() + "Un-surrendered town " + args[6]);
                                                        }
                                                    } else if(w.getSide2().equals(args[4])) {
                                                        if(w.getSide1Towns().remove(args[6])) {
                                                            p.sendMessage(Helper.Chatlabel() + "Removed town " + args[6] + " from side " + w.getSide2());
                                                            Main.warLogger.log(Helper.Chatlabel() + "Un-surrendered town " + args[6]);
                                                        }
                                                    }
                                                }
                                            }
                                            // Side checks
                                            int side = w.getSide(t.getName().toLowerCase());
                                            if (side == -1) {
                                                p.sendMessage(Helper.Chatlabel() + "Town already surrendered!");
                                                return true;
                                            } else if (side > 0) {
                                                p.sendMessage(Helper.Chatlabel() + "Town already in this war!");
                                                return true;
                                            }

                                            w.addTown(t, args[4]);
                                            p.sendMessage(Helper.Chatlabel() + "Forcefully added town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Forcefully added town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("&cError: Town not found!"));
                                        }
                                        return finalizeWar(w);
                                    } else if (args[5].equalsIgnoreCase("nation")) {
                                        Nation n = TownyAPI.getInstance().getNation(args[6]);
                                        if (n != null) {
                                            if(args.length >= 8) {
                                                //forceful add, ignore surrender and override it if the town is
                                                if(Boolean.parseBoolean(args[7])) {
                                                    //if we already have surrendered
                                                    for(Town t : n.getTowns()) {
                                                        if(w.getSurrenderedTowns().contains(t.getName())) {
                                                            w.unSurrenderTown(t.getName());
                                                            p.sendMessage(Helper.Chatlabel() + "Unsurrendered town " + t.getName());
                                                            Main.warLogger.log(Helper.Chatlabel() + "Unsurrendered town " + t.getName());
                                                        }
                                                        if(w.getSide1().equals(args[4])) {
                                                            if(w.getSide2Towns().remove(t.getName())) {
                                                                p.sendMessage(Helper.Chatlabel() + "Removed town " + t.getName() + " from side " + w.getSide2());
                                                                Main.warLogger.log(Helper.Chatlabel() + "Un-surrendered town " + t.getName());
                                                            }
                                                        } else if(w.getSide2().equals(args[4])) {
                                                            if(w.getSide1Towns().remove(t.getName())) {
                                                                p.sendMessage(Helper.Chatlabel() + "Removed town " + t.getName() + " from side " + w.getSide2());
                                                                Main.warLogger.log(Helper.Chatlabel() + "Un-surrendered town " + t.getName());
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            for(Town t : n.getTowns()) {
                                                // Side checks
                                                int side = w.getSide(t.getName().toLowerCase());
                                                if (side == -1) {
                                                    p.sendMessage(Helper.Chatlabel() + "Town " + t.getName() + " already surrendered!");
                                                    continue;
                                                } else if (side > 0) {
                                                    p.sendMessage(Helper.Chatlabel() + "Town " + t.getName() + " already in this war!");
                                                    continue;
                                                }

                                                w.addTown(t, args[4]);
                                            }
                                            p.sendMessage(Helper.Chatlabel() + "Forcefully added nation " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Forcefully added nation " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            return finalizeWar(w);
                                        } else {
                                            p.sendMessage(Helper.color("&cError: Nation not found!"));
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war add [war] [side] town/nation [town/nation] [force]"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("&cError: War not found!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war add [war] [side] town/nation [town/nation] [force] "));
                        }
                        return true;
                    }
                    //TODO do
                    else if (args[2].equalsIgnoreCase("unsurrender")) {
                        if (args.length >= 6) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    if (args[4].equalsIgnoreCase("town")) {
                                        Town t = TownyAPI.getInstance().getTown(args[5]);
                                        if (t != null) {
                                            if(w.getSurrenderedTowns().contains(args[5])) {
                                                w.unSurrenderTown(args[5]);
                                                p.sendMessage(Helper.Chatlabel() + "Un-surrendered town " + args[5]);
                                                Main.warLogger.log(Helper.Chatlabel() + "Un-surrendered town " + args[5]);
                                                p.sendMessage(Helper.color("&cTo re-add, use: /alathrawaradmin modify war add, or have the town loader join."));
                                                return true;
                                            }
                                            p.sendMessage(Helper.Chatlabel() + "Town " + args[5] + " not already surrendered, ignoring.");
                                            Main.warLogger.log(Helper.Chatlabel() + "Town " + args[5] + " not already surrendered, ignoring.");
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("&cError: Town not found!"));
                                        }
                                        return finalizeWar(w);
                                    } else if (args[4].equalsIgnoreCase("nation")) {
                                        Nation n = TownyAPI.getInstance().getNation(args[5]);
                                        if (n != null) {
                                            for(Town t : n.getTowns()) {
                                                if (w.getSurrenderedTowns().contains(t.getName())) {
                                                    w.unSurrenderTown(t.getName());
                                                    p.sendMessage(Helper.Chatlabel() + "Un-surrendered town " + t.getName());
                                                    Main.warLogger.log(Helper.Chatlabel() + "Un-surrendered town " + t.getName());
                                                }
                                            }

                                            p.sendMessage(Helper.Chatlabel() + "Un-surrendered nation " + args[5] + " in war " + args[3]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Un-surrendered nation " + args[5] + " in war " + args[3]);
                                            p.sendMessage(Helper.color("&cTo re-add, use: /alathrawaradmin modify war add, or have the nation loader join."));
                                            return finalizeWar(w);
                                        } else {
                                            p.sendMessage(Helper.color("&cError: Nation not found!"));
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war unsurrender [war] town/nation [town/nation]"));
                                        p.sendMessage(Helper.color("&cTo re-add, use: /alathrawaradmin modify war add, or have the town/nation loader join."));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("&cError: War not found!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war unsurrender [war] town/nation [town/nation]"));
                        }
                        return true;
                    }
                    //TODO IDK IF THIS WORKS
                    else if (args[2].equalsIgnoreCase("surrender")) {
                        if (args.length >= 7) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    if (!w.getSide1().equals(args[4]) && !w.getSide2().equals(args[4])) {
                                        p.sendMessage(Helper.color("&cError: Side not found!"));
                                        return true;
                                    }
                                    if (args[5].equalsIgnoreCase("town")) {
                                        Town t = TownyAPI.getInstance().getTown(args[6]);
                                        if (t != null) {
                                            if(w.getSide1Towns().contains(t.getName()) && w.getSide1().equals(args[4])) {
                                                w.surrenderTown(t.getName());
                                                p.sendMessage(Helper.Chatlabel() + "Forcefully surrendered town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                                Main.warLogger.log(Helper.Chatlabel() + "Forcefully surrendered town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                                return finalizeWar(w);
                                            } else if(w.getSide2Towns().contains(t.getName()) && w.getSide2().equals(args[4])) {
                                                w.surrenderTown(t.getName());
                                                p.sendMessage(Helper.Chatlabel() + "Forcefully surrendered town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                                Main.warLogger.log(Helper.Chatlabel() + "Forcefully surrendered town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                                return finalizeWar(w);
                                            } else {
                                                if (w.getSide1().equals(args[4]) || w.getSide2().equals(args[4])) {
                                                    p.sendMessage(Helper.Chatlabel() + "Town " + args[5] + " is not on either side of the war!");
                                                    Main.warLogger.log(Helper.Chatlabel() + "Town " + args[5] + " is not on either side of the war!");
                                                    return true;
                                                }
                                                p.sendMessage(Helper.Chatlabel() + "Town " + args[5] + " is on a side but side not correct! ERROR!");
                                                Main.warLogger.log(Helper.Chatlabel() + "Town " + args[5] + " is on a side but side not correct! ERROR!");
                                                return true;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("&cError: Town not found!"));
                                            return true;
                                        }
                                    } else if (args[5].equalsIgnoreCase("nation")) {
                                        Nation n = TownyAPI.getInstance().getNation(args[6]);
                                        if (n != null) {
                                            for (Town t : n.getTowns()) {
                                                if(w.getSide1Towns().contains(t.getName()) && w.getSide1().equals(args[4])) {
                                                    w.surrenderTown(t.getName());
                                                    p.sendMessage(Helper.Chatlabel() + "Forcefully surrendered town " + t.getName() + " war " + args[3] + " on side " + args[4]);
                                                    Main.warLogger.log(Helper.Chatlabel() + "Forcefully surrendered town " + t.getName() + " war " + args[3] + " on side " + args[4]);

                                                } else if(w.getSide2Towns().contains(t.getName()) && w.getSide2().equals(args[4])) {
                                                    w.surrenderTown(t.getName());
                                                    p.sendMessage(Helper.Chatlabel() + "Forcefully surrendered town " + t.getName() + " war " + args[3] + " on side " + args[4]);
                                                    Main.warLogger.log(Helper.Chatlabel() + "Forcefully surrendered town " + t.getName() + " war " + args[3] + " on side " + args[4]);

                                                } else {
                                                    if (w.getSide1().equals(args[4]) || w.getSide2().equals(args[4])) {
                                                        p.sendMessage(Helper.Chatlabel() + "Town " + t.getName() + " is not on either side of the war!");
                                                        Main.warLogger.log(Helper.Chatlabel() + "Town " + t.getName() + " is not on either side of the war!");
                                                        return true;
                                                    }
                                                    p.sendMessage(Helper.Chatlabel() + "Town " + t.getName() + " is on a side but side not correct! ERROR!");
                                                    Main.warLogger.log(Helper.Chatlabel() + "Town " + t.getName() + " is on a side but side not correct! ERROR!");
                                                    return true;
                                                }
                                            }
                                            p.sendMessage(Helper.Chatlabel() + "Surrendered nation " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Surrendered nation " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            return finalizeWar(w);
                                        } else {
                                            p.sendMessage(Helper.color("&cError: Nation not found!"));
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war surrender [war] [side] town/nation [town/nation]"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("&cError: War not found!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war surrender town [war] [town]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("raidTimeWar")) {
                        if (args.length >= 5) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[4])) {
                                    if (args[3].equalsIgnoreCase("add")) {
                                        if (args.length >= 7) {
                                            if (args[6].equals(w.getSide1())) {
                                                w.setLastRaidTimeSide1(w.getLastRaidTimeSide1() + Integer.parseInt(args[5]));
                                                p.sendMessage(Helper.Chatlabel() + "Added " + args[6] + " to last raid time in war " + args[4]);
                                                Main.warLogger.log(Helper.Chatlabel() + "Added " + args[6] + " to last raid time in war " + args[4]);
                                                return finalizeWar(w);
                                            } else if (args[6].equals(w.getSide2())) {
                                                w.setLastRaidTimeSide2(w.getLastRaidTimeSide2() + Integer.parseInt(args[5]));
                                                p.sendMessage(Helper.Chatlabel() + "Added " + args[6] + " to last raid time in war " + args[4]);
                                                Main.warLogger.log(Helper.Chatlabel() + "Added " + args[6] + " to last raid time in war " + args[4]);
                                                return finalizeWar(w);
                                            } else {
                                                p.sendMessage(Helper.color("&cSide not found!"));
                                                return true;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war raidTimeWar [add,set,reset] [town/war] [amt] [side]"));
                                            return true;
                                        }
                                    } else if (args[3].equalsIgnoreCase("set")) {
                                        if (args.length >= 7) {
                                            if (args[6].equals(w.getSide1())) {
                                                w.setLastRaidTimeSide1(Integer.parseInt(args[5]));
                                                p.sendMessage(Helper.Chatlabel() + "Set last raid time in war " + args[4] + " to " + args[6]);
                                                Main.warLogger.log(Helper.Chatlabel() + "Set last raid time in war " + args[4] + " to " + args[6]);
                                                return finalizeWar(w);
                                            } else if (args[6].equals(w.getSide2())) {
                                                w.setLastRaidTimeSide2(Integer.parseInt(args[5]));
                                                p.sendMessage(Helper.Chatlabel() + "Set last raid time in war " + args[4] + " to " + args[6]);
                                                Main.warLogger.log(Helper.Chatlabel() + "Set last raid time in war " + args[4] + " to " + args[6]);
                                                return finalizeWar(w);
                                            } else {
                                                p.sendMessage(Helper.color("&cSide not found!"));
                                                return true;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war raidTimeWar [add,set,reset] [town/war] [amt] [side]"));
                                            return true;
                                        }
                                    } else if (args[3].equalsIgnoreCase("reset")) {

                                        if (args.length >= 6) {
                                            if (args[5].equals(w.getSide1())) {
                                                w.setLastRaidTimeSide1(0);
                                                p.sendMessage(Helper.Chatlabel() + "Reset last raid time in war " + args[4]);
                                                Main.warLogger.log(Helper.Chatlabel() + "Reset last raid time in war " + args[4]);
                                                return finalizeWar(w);
                                            } else if (args[5].equals(w.getSide2())) {
                                                w.setLastRaidTimeSide2(0);
                                                p.sendMessage(Helper.Chatlabel() + "Reset last raid time in war " + args[4]);
                                                Main.warLogger.log(Helper.Chatlabel() + "Reset last raid time in war " + args[4]);
                                                return finalizeWar(w);
                                            } else {
                                                p.sendMessage(Helper.color("&cSide not found!"));
                                                return true;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war raidTimeWar [add,set,reset] [town/war] [amt] [side]"));
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war raidTimeWar [add,set,reset] [town/war] [amt] [side]"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("&cError: War not found!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war raidTimeWar [add,set,reset] [war] [amt] [side]"));
                        }
                        return true;
                    } else if (args[2].equalsIgnoreCase("raidTimeTown")) {
                        if (args.length >= 5) {
                            for (Town t : TownyAPI.getInstance().getTowns()) {
                                if (t.getName().equals(args[4])) {
                                    if (args[3].equalsIgnoreCase("add")) {
                                        if (args.length >= 6) {
                                            LongDataField last = (LongDataField) t.getMetadata("lastRaided");
                                            if (last != null) {
                                                t.addMetaData(new LongDataField("lastRaided", last.getValue() + Long.parseLong(args[6])));
                                            } else {
                                                t.addMetaData(new LongDataField("lastRaided", Long.parseLong(args[6])));
                                            }
                                            p.sendMessage(Helper.Chatlabel() + "Added " + args[6] + " to last raid time in war " + args[3]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Added " + args[6] + " to last raid time in war " + args[3]);
                                        } else {
                                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war raidTimeTown [add,set,reset] [town/war] [amt] [side]"));
                                        }
                                        return true;
                                    } else if (args[3].equalsIgnoreCase("set")) {
                                        if (args.length >= 6) {
                                            t.addMetaData(new LongDataField("lastRaided", Long.parseLong(args[6])));
                                            p.sendMessage(Helper.Chatlabel() + "Set last raid time in war " + args[4] + " to " + args[6]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Set last raid time in war " + args[4] + " to " + args[6]);
                                        } else {
                                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war raidTimeTown [add,set,reset] [town/war] [amt] [side]"));
                                        }
                                        return true;
                                    } else if (args[3].equalsIgnoreCase("reset")) {
                                        t.addMetaData(new LongDataField("lastRaided", 0L));
                                        p.sendMessage(Helper.Chatlabel() + "Reset last raid time in town " + args[4]);
                                        Main.warLogger.log(Helper.Chatlabel() + "Reset last raid time in war " + args[4]);
                                        return true;
                                    } else {
                                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify war raidTimeTown [add,set,reset] [town] [amt]"));
                                        return true;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("&cError: Town not found!"));
                        } else {
                            p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify [raid/siege/war] [propery]"));
                        }
                        return true;
                    } else {
                        p.sendMessage(Helper.color("&cUsage: /alathrawaradmin modify [raid/siege/war] [propery]"));
                        return true;
                    }
                } else {
                    return fail(p, args, "syntax");
                }
            } else {
                return fail(p, args, "syntax");
            }
        }
        return true;
    }

    private static boolean awa(Player sender, String[] args) {
        if (args.length > 1) {
            Player p = Bukkit.getPlayer(args[1]);
            if (p == null) {
                sender.sendMessage(Helper.Chatlabel() + Helper.color("&cPlayer not found!"));
                return true;
            }
            p.chat("awa awa! ^.^ UwU");
            return true;
        }
        sender.chat("awa awa! ^.^ UwU");
        return true;
    }

    private static boolean fail(Player p, String[] args, String type) {
        switch (type) {
            case "permissions" -> {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("&cYou do not have permission to do this."));
                return true;
            }
            case "syntax" -> {
                p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /alathrawaradmin help");
                return true;
            }
            default -> {
                p.sendMessage(Helper.Chatlabel() + "Something wrong. /alathrawaradmin help");
                return true;
            }
        }
    }

    private static boolean finalizeRaid(Raid r) {
        r.save();
        return true;
    }

    private static boolean finalizeSiege(Siege s) {
        s.save();
        return true;
    }

    private static boolean finalizeWar(War w) {
        w.save();
        return true;
    }
}
