package dev.o8o1o5.economyDeBleu.command;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor; // ChatColor 임포트
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final Economy economy;

    public BalanceCommand(Economy economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;
        sender.sendMessage(ChatColor.GREEN + "당신의 잔액: " + ChatColor.WHITE + economy.format(economy.getBalance(player)));
        return true;
    }
}