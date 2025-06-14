package dev.o8o1o5.economyDeBleu.listener;

import dev.o8o1o5.economyDeBleu.EconomyDeBleu;
import dev.o8o1o5.economyDeBleu.EconomyDeBleuEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EconomyPlayerJoinEvent implements Listener {
    private final EconomyDeBleu plugin;
    private final EconomyDeBleuEconomy economy;

    public EconomyPlayerJoinEvent(EconomyDeBleu plugin, EconomyDeBleuEconomy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (economy.hasAccount(event.getPlayer())) {
            return;
        }
        economy.createPlayerAccount(event.getPlayer());
        plugin.getLogger().info(event.getPlayer().getName() + "님의 계좌가 생성되었습니다.");
    }
}
