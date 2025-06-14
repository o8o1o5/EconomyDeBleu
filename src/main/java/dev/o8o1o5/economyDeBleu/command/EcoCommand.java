package dev.o8o1o5.economyDeBleu.command;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor; // ChatColor 임포트
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EcoCommand implements CommandExecutor {

    private final Economy economy;

    public EcoCommand(Economy economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 권한 확인 (예시: op만 사용 가능)
        if (!sender.isOp()) { // 실제 사용 시 economydebleu.admin 권한 등으로 변경
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }

        if (args.length < 2) { // delete 명령어 고려하여 2로 변경
            sender.sendMessage(ChatColor.AQUA + "사용법: " + ChatColor.WHITE + "/eco <set|give|take> <플레이어> <금액>");
            sender.sendMessage(ChatColor.AQUA + "사용법: " + ChatColor.WHITE + "/eco delete <플레이어>");
            return true;
        }

        String action = args[0].toLowerCase();
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);

        // 계정 존재 여부 확인 (옵션: 모든 명령에서 확인할지, 특정 명령에서만 확인할지)
        if (!economy.hasAccount(targetPlayer) && !(action.equals("create") || action.equals("set"))) { // create나 set은 계정 없으면 생성 가능
            sender.sendMessage(ChatColor.RED + "존재하지 않는 플레이어 계정입니다: " + ChatColor.WHITE + targetPlayer.getName());
            return true;
        }


        if (action.equals("set") || action.equals("give") || action.equals("take")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.AQUA + "사용법: " + ChatColor.WHITE + "/eco <set|give|take> <플레이어> <금액>");
                return true;
            }
            double amount;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "유효하지 않은 금액입니다.");
                return true;
            }

            if (amount < 0) {
                sender.sendMessage(ChatColor.RED + "금액은 음수일 수 없습니다.");
                return true;
            }

            EconomyResponse response;

            switch (action) {
                case "set":
                    economy.withdrawPlayer(targetPlayer, economy.getBalance(targetPlayer));
                    response = economy.depositPlayer(targetPlayer, amount);
                    sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "님의 잔액을 " + ChatColor.WHITE + economy.format(response.balance) + ChatColor.GREEN + "으로 설정했습니다.");
                    break;
                case "give":
                    response = economy.depositPlayer(targetPlayer, amount);
                    if (response.transactionSuccess()) {
                        sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "님에게 " + ChatColor.WHITE + economy.format(response.amount) + ChatColor.GREEN + "을 지급했습니다. 새 잔액: " + ChatColor.WHITE + economy.format(response.balance));
                    } else {
                        sender.sendMessage(ChatColor.RED + "돈을 지급하는 데 실패했습니다: " + response.errorMessage);
                    }
                    break;
                case "take":
                    response = economy.withdrawPlayer(targetPlayer, amount);
                    if (response.transactionSuccess()) {
                        sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "님으로부터 " + ChatColor.WHITE + economy.format(response.amount) + ChatColor.GREEN + "을 회수했습니다. 새 잔액: " + ChatColor.WHITE + economy.format(response.balance));
                    } else {
                        sender.sendMessage(ChatColor.RED + "돈을 회수하는 데 실패했습니다: " + response.errorMessage);
                    }
                    break;
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다. 사용법: " + ChatColor.WHITE + "/eco <set|give|take|delete> <플레이어> [금액]");
        }
        return true;
    }
}