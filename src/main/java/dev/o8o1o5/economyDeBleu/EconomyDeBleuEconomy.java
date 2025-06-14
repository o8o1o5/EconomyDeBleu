package dev.o8o1o5.economyDeBleu;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class EconomyDeBleuEconomy extends AbstractEconomy {
    private final EconomyDeBleu plugin;
    private final Map<UUID, Double> balances;
    private final double startingBalance;
    private final String currencySingular;
    private final String currencyPlural;

    public EconomyDeBleuEconomy(EconomyDeBleu plugin, FileConfiguration dataConfig, double startingBalance, String currencySingular, String currencyPlural) {
        this.plugin = plugin;
        this.balances = new HashMap<>();
        this.startingBalance = startingBalance;
        this.currencySingular = currencySingular;
        this.currencyPlural = currencyPlural;

        if (dataConfig != null) {
            for (String uuidString : dataConfig.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    double balance = dataConfig.getDouble(uuidString);
                    balances.put(uuid, balance);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("balances.yml에서 유효하지 않은 UUID를 발견했습니다: " + uuidString);
                }
            }
            plugin.getLogger().info("balances.yml에서 " + balances.size() + "개의 플레이어 잔액을 로드했습니다.");
        }
    }

    public Map<UUID, Double> getBalances() {
        return balances;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "EconomyDeBleu";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return String.format("%.2f", amount) + " " + (amount == 1.0 ? currencyNameSingular() : currencyNamePlural());
    }

    @Override
    public String currencyNamePlural() {
        return currencyPlural;
    }

    @Override
    public String currencyNameSingular() {
        return currencySingular;
    }

    @Override
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean has(String playerName, double amount) {
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "출금액은 음수일 수 없습니다.");
        }

        double currentBalance = getBalance(player);
        if (currentBalance < amount) {
            return new EconomyResponse(0, currentBalance, EconomyResponse.ResponseType.FAILURE, "잔액이 부족합니다.");
        }

        double newBalance = currentBalance - amount;
        balances.put(player.getUniqueId(), newBalance);
        plugin.savePlayerData();
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "예금액은 음수일 수 없습니다.");
        }
        double currentBalance = getBalance(player);
        double newBalance = currentBalance + amount;
        balances.put(player.getUniqueId(), newBalance);
        plugin.savePlayerData();
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    // --- 은행 관련 메서드 (hasBankSupport()가 false이므로 NOT_IMPLEMENTED 반환) ---
    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "은행 시스템은 구현되지 않았습니다.");
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    // --- 계정 생성/존재 확인 ---
    @Override
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (hasAccount(player)) {
            return false;
        }
        balances.put(player.getUniqueId(), startingBalance);
        plugin.savePlayerData();
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean hasAccount(String playerName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return balances.containsKey(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }
}