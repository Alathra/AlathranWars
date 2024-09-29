package com.github.alathra.alathranwars.meta;

import com.github.alathra.alathranwars.utility.Utils;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.metadata.LocationDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class ControlPoint {
    @SuppressWarnings("unused")
    private final static LocationDataField controlPoint = new LocationDataField(TownyMetaHandler.META_NAMESPACE + "capturepoint", null, "Capture Point");

    @Nullable
    public static Location get(Town town) {
        LocationDataField ldf = (LocationDataField) controlPoint.clone();

        if (town.hasMeta() && MetaDataUtil.hasMeta(town, ldf))
            return MetaDataUtil.getLocation(town, ldf);

        return null;
    }

    public static void set(Town town, Location location) {
        MetaDataUtil.setLocation(town, controlPoint, location, false);
    }

    public static Location getSafe(Town town) {
        Location loc = get(town);

        if (loc == null) {
            TownBlock townBlock = town.getHomeBlockOrNull();
            if (townBlock == null) {
                loc = town.getSpawnOrNull();
                set(town, loc);
            } else {
                loc = Utils.getTownBlockCenter(townBlock).toHighestLocation().toCenterLocation();
                set(town, loc);
            }
        }

        return loc;
    }
}
