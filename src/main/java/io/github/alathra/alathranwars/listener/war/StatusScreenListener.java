package io.github.alathra.alathranwars.listener.war;

import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.ResidentStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import io.github.alathra.alathranwars.api.AlathranWarsAPI;
import io.github.alathra.alathranwars.utility.TimeTagResolvers;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

@SuppressWarnings("unused")
public class StatusScreenListener implements Listener {
    @EventHandler
    public void onStatusScreen(ResidentStatusScreenEvent e) {
        final boolean hasAttackCooldown = AlathranWarsAPI.getInstance().hasAttackCooldown(e.getResident());
        final boolean hasDefenseCooldown = AlathranWarsAPI.getInstance().hasDefenseCooldown(e.getResident());
        final Duration attackCooldown = AlathranWarsAPI.getInstance().getAttackCooldown(e.getResident());
        final Duration defenseCooldown = AlathranWarsAPI.getInstance().getDefenseCooldown(e.getResident());

        final Component inactive = Translation.as("status-screen.cooldown-inactive");

        e.getStatusScreen().addComponentOf("cooldown",
            (com.palmergames.adventure.text.Component) ColorParser.of(Translation.of("status-screen.cooldown"))
                .with("cooldown_attack", hasAttackCooldown ? ColorParser.of(Translation.of("cooldown-active-format")).tag(TimeTagResolvers.tag(attackCooldown)).build() : inactive)
                .with("cooldown_defense", hasDefenseCooldown ? ColorParser.of(Translation.of("cooldown-active-format")).tag(TimeTagResolvers.tag(defenseCooldown)).build() : inactive)
                .build()
        );
    }

    @EventHandler
    public void onStatusScreen(TownStatusScreenEvent e) {
        final boolean hasAttackCooldown = AlathranWarsAPI.getInstance().hasAttackCooldown(e.getTown());
        final boolean hasDefenseCooldown = AlathranWarsAPI.getInstance().hasDefenseCooldown(e.getTown());
        final Duration attackCooldown = AlathranWarsAPI.getInstance().getAttackCooldown(e.getTown());
        final Duration defenseCooldown = AlathranWarsAPI.getInstance().getDefenseCooldown(e.getTown());

        final Component inactive = Translation.as("status-screen.cooldown-inactive");

        e.getStatusScreen().addComponentOf("cooldown",
            (com.palmergames.adventure.text.Component) ColorParser.of(Translation.of("status-screen.cooldown"))
                .with("cooldown_attack", hasAttackCooldown ? ColorParser.of(Translation.of("cooldown-active-format")).tag(TimeTagResolvers.tag(attackCooldown)).build() : inactive)
                .with("cooldown_defense", hasDefenseCooldown ? ColorParser.of(Translation.of("cooldown-active-format")).tag(TimeTagResolvers.tag(defenseCooldown)).build() : inactive)
                .build()
        );
    }

    @EventHandler
    public void onStatusScreen(NationStatusScreenEvent e) {
        final boolean hasAttackCooldown = AlathranWarsAPI.getInstance().hasAttackCooldown(e.getNation());
        final boolean hasDefenseCooldown = AlathranWarsAPI.getInstance().hasDefenseCooldown(e.getNation());
        final Duration attackCooldown = AlathranWarsAPI.getInstance().getAttackCooldown(e.getNation());
        final Duration defenseCooldown = AlathranWarsAPI.getInstance().getDefenseCooldown(e.getNation());

        final Component inactive = Translation.as("status-screen.cooldown-inactive");

        e.getStatusScreen().addComponentOf("cooldown",
            (com.palmergames.adventure.text.Component) ColorParser.of(Translation.of("status-screen.cooldown"))
                .with("cooldown_attack", hasAttackCooldown ? ColorParser.of(Translation.of("cooldown-active-format")).tag(TimeTagResolvers.tag(attackCooldown)).build() : inactive)
                .with("cooldown_defense", hasDefenseCooldown ? ColorParser.of(Translation.of("cooldown-active-format")).tag(TimeTagResolvers.tag(defenseCooldown)).build() : inactive)
                .build()
        );
    }
}
