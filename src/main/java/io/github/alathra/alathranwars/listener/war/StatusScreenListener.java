package io.github.alathra.alathranwars.listener.war;

import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.ResidentStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import io.github.alathra.alathranwars.api.AlathranWarsAPI;
import io.github.alathra.alathranwars.utility.TimeTagResolversTowny;
import io.github.milkdrinkers.wordweaver.Translation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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

        final Component inactive = MiniMessage.miniMessage().deserialize(Translation.of("status-screen.cooldown-inactive"));

        e.getStatusScreen().addComponentOf("cooldown",
            MiniMessage.miniMessage().deserialize(
                Translation.of("status-screen.cooldown"),
                TagResolver.resolver(
                    TagResolver.resolver("cooldown_attack", Tag.selfClosingInserting(
                        hasAttackCooldown ? getCooldownRemaining(attackCooldown) : inactive
                    )),
                    TagResolver.resolver("cooldown_defense", Tag.selfClosingInserting(
                        hasDefenseCooldown ? getCooldownRemaining(defenseCooldown) : inactive
                    ))
                )
            )
        );
    }

    @EventHandler
    public void onStatusScreen(TownStatusScreenEvent e) {
        final boolean hasAttackCooldown = AlathranWarsAPI.getInstance().hasAttackCooldown(e.getTown());
        final boolean hasDefenseCooldown = AlathranWarsAPI.getInstance().hasDefenseCooldown(e.getTown());
        final Duration attackCooldown = AlathranWarsAPI.getInstance().getAttackCooldown(e.getTown());
        final Duration defenseCooldown = AlathranWarsAPI.getInstance().getDefenseCooldown(e.getTown());

        final Component inactive = MiniMessage.miniMessage().deserialize(Translation.of("status-screen.cooldown-inactive"));

        e.getStatusScreen().addComponentOf("cooldown",
            MiniMessage.miniMessage().deserialize(
                Translation.of("status-screen.cooldown"),
                TagResolver.resolver(
                    TagResolver.resolver("cooldown_attack", Tag.selfClosingInserting(
                        hasAttackCooldown ? getCooldownRemaining(attackCooldown) : inactive
                    )),
                    TagResolver.resolver("cooldown_defense", Tag.selfClosingInserting(
                        hasDefenseCooldown ? getCooldownRemaining(defenseCooldown) : inactive
                    ))
                )
            )
        );

        if (AlathranWarsAPI.getInstance().isMercenary(e.getTown())) {
            e.getStatusScreen().addComponentOf("mercenary",
                MiniMessage.miniMessage().deserialize(Translation.of("status-screen.mercenary"))
            );
        }
    }

    @EventHandler
    public void onStatusScreen(NationStatusScreenEvent e) {
        final boolean hasAttackCooldown = AlathranWarsAPI.getInstance().hasAttackCooldown(e.getNation());
        final boolean hasDefenseCooldown = AlathranWarsAPI.getInstance().hasDefenseCooldown(e.getNation());
        final Duration attackCooldown = AlathranWarsAPI.getInstance().getAttackCooldown(e.getNation());
        final Duration defenseCooldown = AlathranWarsAPI.getInstance().getDefenseCooldown(e.getNation());

        final Component inactive = MiniMessage.miniMessage().deserialize(Translation.of("status-screen.cooldown-inactive"));

        e.getStatusScreen().addComponentOf("cooldown",
            MiniMessage.miniMessage().deserialize(
                Translation.of("status-screen.cooldown"),
                TagResolver.resolver(
                    TagResolver.resolver("cooldown_attack", Tag.selfClosingInserting(
                        hasAttackCooldown ? getCooldownRemaining(attackCooldown) : inactive
                    )),
                    TagResolver.resolver("cooldown_defense", Tag.selfClosingInserting(
                        hasDefenseCooldown ? getCooldownRemaining(defenseCooldown) : inactive
                    ))
                )
            )
        );
    }

    private Component getCooldownRemaining(Duration duration) {
        return MiniMessage.miniMessage().deserialize(Translation.of("status-screen.cooldown-active-format"), TimeTagResolversTowny.tag(duration));
    }
}
