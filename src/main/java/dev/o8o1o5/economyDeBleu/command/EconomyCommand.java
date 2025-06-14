package dev.o8o1o5.economyDeBleu.command;

import dev.o8o1o5.economyDeBleu.EconomyDeBleu;
import dev.o8o1o5.economyDeBleu.EconomyDeBleuEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyCommand implements CommandExecutor {

    private final EconomyDeBleu plugin;

    public EconomyCommand(EconomyDeBleu plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("economydebleu.command.economy")) {
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
                return handleSetCommand(sender, args);
            case "give":
                return handleGiveCommand(sender, args);
            case "take":
                return handleTakeCommand(sender, args);
            case "reset":
                return handleResetCommand(sender, args);
            case "unit": // 새로운 'unit' 하위 명령어 처리
                return handleUnitCommand(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다. '/" + label + " help'를 입력하여 도움말을 확인하세요.");
                return true;
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "------ /economy 커맨드 ------");
        sender.sendMessage(ChatColor.YELLOW + "/economy set <플레이어> <금액> " + ChatColor.GRAY + "- 플레이어 잔액을 설정합니다.");
        sender.sendMessage(ChatColor.YELLOW + "/economy give <플레이어> <금액> " + ChatColor.GRAY + "- 플레이어에게 금액을 지급합니다.");
        sender.sendMessage(ChatColor.YELLOW + "/economy take <플레이어> <금액> " + ChatColor.GRAY + "- 플레이어에게서 금액을 차감합니다.");
        sender.sendMessage(ChatColor.YELLOW + "/economy reset <플레이어> " + ChatColor.GRAY + "- 플레이어 잔액을 0으로 초기화합니다.");
        sender.sendMessage(ChatColor.YELLOW + "/economy unit <단수단위> <복수단위> " + ChatColor.GRAY + "- 통화 단위를 설정합니다.");
        sender.sendMessage(ChatColor.GOLD + "----------------------------");
    }

    private boolean handleSetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("economydebleu.command.economy.set")) {
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "사용법: /economy set <플레이어> <금액>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + args[1]);
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "유효한 금액을 입력해주세요.");
            return true;
        }
        EconomyDeBleuEconomy economy = plugin.getEconomyDeBleuEconomy();
        if (economy == null) {
            sender.sendMessage(ChatColor.RED + "경제 시스템이 초기화되지 않았습니다.");
            return true;
        }
        economy.setBalance(target.getUniqueId(), amount);
        sender.sendMessage(ChatColor.GREEN + target.getName() + "님의 잔액이 " + economy.format(amount) + "(으)로 설정되었습니다.");
        return true;
    }

    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("economydebleu.command.economy.give")) {
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "사용법: /economy give <플레이어> <금액>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + args[1]);
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "유효한 금액을 입력해주세요.");
            return true;
        }
        if (amount < 0) {
            sender.sendMessage(ChatColor.RED + "음수 금액을 지급할 수 없습니다. 'take' 명령어를 사용해주세요.");
            return true;
        }
        EconomyDeBleuEconomy economy = plugin.getEconomyDeBleuEconomy();
        if (economy == null) {
            sender.sendMessage(ChatColor.RED + "경제 시스템이 초기화되지 않았습니다.");
            return true;
        }
        economy.depositPlayer(target, amount);
        sender.sendMessage(ChatColor.GREEN + target.getName() + "님에게 " + economy.format(amount) + "을(를) 지급했습니다. 현재 잔액: " + economy.format(economy.getBalance(target)));
        return true;
    }

    private boolean handleTakeCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("economydebleu.command.economy.take")) {
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "사용법: /economy take <플레이어> <금액>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + args[1]);
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "유효한 금액을 입력해주세요.");
            return true;
        }
        if (amount < 0) {
            sender.sendMessage(ChatColor.RED + "음수 금액을 차감할 수 없습니다. 'give' 명령어를 사용해주세요.");
            return true;
        }
        EconomyDeBleuEconomy economy = plugin.getEconomyDeBleuEconomy();
        if (economy == null) {
            sender.sendMessage(ChatColor.RED + "경제 시스템이 초기화되지 않았습니다.");
            return true;
        }
        economy.withdrawPlayer(target, amount);
        sender.sendMessage(ChatColor.GREEN + target.getName() + "님에게서 " + economy.format(amount) + "을(를) 차감했습니다. 현재 잔액: " + economy.format(economy.getBalance(target)));
        return true;
    }

    private boolean handleResetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("economydebleu.command.economy.reset")) {
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "사용법: /economy reset <플레이어>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + args[1]);
            return true;
        }
        EconomyDeBleuEconomy economy = plugin.getEconomyDeBleuEconomy();
        if (economy == null) {
            sender.sendMessage(ChatColor.RED + "경제 시스템이 초기화되지 않았습니다.");
            return true;
        }
        economy.setBalance(target.getUniqueId(), 0.0);
        sender.sendMessage(ChatColor.GREEN + target.getName() + "님의 잔액이 초기화되었습니다.");
        return true;
    }

    // 새로운 'unit' 하위 명령어 핸들러
    private boolean handleUnitCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("economydebleu.command.economy.unit")) { // 세부 권한 부여
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }

        if (args.length == 1) { // /eco unit (인수 없음) - 현재 단위 표시
            sender.sendMessage(ChatColor.YELLOW + "현재 통화 단위: " +
                    ChatColor.AQUA + plugin.getCurrencySingular() + ChatColor.YELLOW + " (단수), " +
                    ChatColor.AQUA + plugin.getCurrencyPlural() + ChatColor.YELLOW + " (복수)");
            sender.sendMessage(ChatColor.YELLOW + "사용법: " + ChatColor.GREEN + "/economy unit <단수단위> <복수단위>");
            sender.sendMessage(ChatColor.YELLOW + "예시: " + ChatColor.GREEN + "/economy unit 원 원");
            return true;
        }

        if (args.length == 2) { // /eco unit <단수단위> (복수단위 없음)
            sender.sendMessage(ChatColor.RED + "복수 단위를 지정해야 합니다. 사용법: /economy unit <단수단위> <복수단위>");
            return true;
        }

        if (args.length >= 3) { // /eco unit <단수단위> <복수단위>
            String newSingular = args[1]; // args[0]은 "unit"
            String newPlural = args[2];

            plugin.setCurrencyUnits(newSingular, newPlural);
            sender.sendMessage(ChatColor.GREEN + "통화 단위가 성공적으로 변경되었습니다!");
            sender.sendMessage(ChatColor.YELLOW + "새로운 단위: " +
                    ChatColor.AQUA + plugin.getCurrencySingular() + ChatColor.YELLOW + " (단수), " +
                    ChatColor.AQUA + plugin.getCurrencyPlural() + ChatColor.YELLOW + " (복수)");
            return true;
        }

        return false;
    }
}