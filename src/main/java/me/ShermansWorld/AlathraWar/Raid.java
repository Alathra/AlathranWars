package me.ShermansWorld.AlathraWar;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import me.ShermansWorld.AlathraWar.commands.RaidCommands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

/**
 * This is based on the existing Siege Class
 */
public class Raid {

    private War war;
    private Town town;
    private String raiders;
    private String defenders;
    private boolean side1AreRaiders;
    private boolean side2AreRaiders;
    private int id;
    private int raiderPoints;
    private int defenderPoints;
    private int MAXRAIDTICKS;
    private Player owner;
    private int raidTicks;
    private TownBlock homeBlock;
    private Location townSpawn;
    int[] bukkitId;
    public ArrayList<String> raiderPlayers;
    public ArrayList<String> defenderPlayers;

    public Raid(final int id, final War war, final Town town, final String raiders, final String defenders,
                 final boolean side1AreRaiders, final boolean side2AreRaiders) {
        this.raidTicks = 0;
        this.bukkitId = new int[1];
        this.raiderPlayers = new ArrayList<String>();
        this.defenderPlayers = new ArrayList<String>();
        this.war = war;
        this.town = town;
        this.raiders = raiders;
        this.defenders = defenders;
        this.side1AreRaiders = side1AreRaiders;
        this.side2AreRaiders = side2AreRaiders;
        this.id = id;
    }

    public void start() {
        this.raiderPoints = Main.raidData.getConfig().getInt("Raids." + String.valueOf(this.id) + ".raiderpoints");
        this.defenderPoints = Main.raidData.getConfig().getInt("Raids." + String.valueOf(this.id) + ".defenderpoints");
        this.side1AreRaiders = Main.raidData.getConfig()
                .getBoolean("Raids." + String.valueOf(this.id) + ".side1areraiders");
        this.side2AreRaiders = Main.raidData.getConfig()
                .getBoolean("Raids." + String.valueOf(this.id) + ".side2areraiders");
        this.MAXRAIDTICKS = 108000;
        this.raidTicks = Main.raidData.getConfig().getInt("Raids." + String.valueOf(this.id) + ".raidticks");
        this.owner = Bukkit.getPlayer(Main.raidData.getConfig().getString("Raids." + String.valueOf(this.id) + ".owner"));
        if (this.side1AreRaiders) {
            this.raiderPlayers = this.war.getSide1Players();
            this.defenderPlayers = this.war.getSide2Players();
        } else {
            this.raiderPlayers = this.war.getSide2Players();
            this.defenderPlayers = this.war.getSide1Players();
        }
        try {
            homeBlock = town.getHomeBlock();
            townSpawn = town.getSpawn();
        } catch (TownyException e) {
            e.printStackTrace();
        }

        //Tick Loop sorta (runs every 10 seconds afaik)
        this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) Main.getInstance(),
                (Runnable) new Runnable() {
                    int homeBlockControl = 0;

                    @Override
                    public void run() {
                        if (homeBlock != null) {
                            town.setHomeBlock(homeBlock);
                            town.setSpawn(townSpawn);
                        }
                        if (Raid.this.side1AreRaiders) {
                            Raid.this.raiderPlayers = Raid.this.war.getSide1Players();
                            Raid.this.defenderPlayers = Raid.this.war.getSide2Players();
                        } else {
                            Raid.this.raiderPlayers = Raid.this.war.getSide2Players();
                            Raid.this.defenderPlayers = Raid.this.war.getSide1Players();
                        }
                        if (Raid.this.raidTicks >= Raid.this.MAXRAIDTICKS) {
                            Bukkit.getServer().getScheduler().cancelTask(Raid.this.bukkitId[0]);
                            if (Raid.this.raiderPoints > Raid.this.defenderPoints) {
                                Raid.this.raidersWin(Raid.this.owner);
                            } else {
                                Raid.this.defendersWin();
                            }
                        } else {
                            final Raid this$0 = Raid.this;
                            Raid.access$7(this$0, this$0.raidTicks + 200);
                            Main.raidData.getConfig().set("Raids." + String.valueOf(Raid.this.id) + ".raidticks",
                                    (Object) Raid.this.raidTicks);
                            Main.raidData.saveConfig();


                            if (Raid.this.raidTicks % 6000 == 0) {
                                Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "Report on the raid of "
                                        + Raid.this.town.getName() + ":");
                                Bukkit.broadcastMessage(
                                        "Raider Points - " + String.valueOf(Raid.this.raiderPoints));
                                Bukkit.broadcastMessage(
                                        "Defender Points - " + String.valueOf(Raid.this.defenderPoints));
                            }
                        }
                    }
                }, 0L, 200L);

    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(this.bukkitId[0]);
        RaidCommands.raids.remove(this);  //im not making this rn -Aubri
        Main.raidData.getConfig().set("Raids." + String.valueOf(this.id), (Object) null);
        Main.raidData.saveConfig();
    }


    /**
     * Need to add call to KillsListener (defined seperate from Siege)
     */
    public void raiderKilled() {

    }

    /**
     * Need to add call to KillsListener (defined seperate from Siege)
     */
    public void defenderKilled() {

    }

    public void raidersWin(final Player owner) {

    }

    public void defendersWin() {

    }

    public void addPointsToRaiders(final int points) {
        this.raiderPoints += points;
    }

    public void addPointsToDefenders(final int points) {
        this.defenderPoints += points;
    }

    public int getID() {
        return this.id;
    }

    public void setID(final int id) {
        this.id = id;
    }

    public War getWar() {
        return this.war;
    }

    public void setWar(final War war) {
        this.war = war;
    }

    public Town getTown() {
        return this.town;
    }

    public void setTown(final Town town) {
        this.town = town;
    }

    public String getRaiders() {
        return this.raiders;
    }

    public void setRaiders(final String raiders) {
        this.raiders = raiders;
    }

    public String getDefenders() {
        return this.defenders;
    }

    public void setDefenders(final String defenders) {
        this.raiders = defenders;
    }

    public int getRaiderPoints() {
        return this.raiderPoints;
    }

    public void raiderPoints(final int raiderPoints) {
        this.raiderPoints = raiderPoints;
    }

    public int getDefenderPoints() {
        return this.defenderPoints;
    }

    public void defenderPoints(final int defenderPoints) {
        this.defenderPoints = this.raiderPoints;
    }

    public boolean getSide1AreRaiders() {
        return this.side1AreRaiders;
    }

    public void setSide1AreRaiders(final boolean side1AreRaiders) {
        this.side1AreRaiders = side1AreRaiders;
    }

    public boolean getSide2AreRaiders() {
        return this.side2AreRaiders;
    }

    public void setSide2AreRaiders(final boolean side2AreRaiders) {
        this.side2AreRaiders = side2AreRaiders;
    }

    public ArrayList<String> getRaiderPlayers() {
        return this.raiderPlayers;
    }

    public void setRaiderPlayer(final ArrayList<String> raiderPlayers) {
        this.raiderPlayers = raiderPlayers;
    }

    public ArrayList<String> getDefenderPlayers() {
        return this.defenderPlayers;
    }

    public void setDefenderPlayer(final ArrayList<String> defenderPlayers) {
        this.defenderPlayers = defenderPlayers;
    }

    static /* synthetic */ void access$7(final Raid raid, final int raidTicks) {
        raid.raidTicks = raidTicks;
    }
}