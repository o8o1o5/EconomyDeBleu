package dev.o8o1o5.economyDeBleu;

import dev.o8o1o5.economyDeBleu.command.BalanceCommand;
import dev.o8o1o5.economyDeBleu.command.EcoCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class EconomyDeBleu extends JavaPlugin {
    private EconomyDeBleuEconomy economyDeBleuEconomy;
    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        getLogger().info("EconomyDeBleu가 활성화되었습니다!");

        // config.yml 파일 저장 (기본값 포함)
        saveDefaultConfig();

        // Vault 플러그인 확인
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault 플러그인을 찾을 수 없습니다! EconomyDeBleu를 비활성화합니다.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 데이터 파일 로드
        loadPlayerData();

        // EconomyDeBleuEconomy 인스턴스 생성 및 Vault에 서비스 등록
        this.economyDeBleuEconomy = new EconomyDeBleuEconomy(this, dataConfig, getConfig().getDouble("starting-balance", 0.0), getConfig().getString("currency.singular", "파랑"), getConfig().getString("currency.plural", "파랑"));
        getServer().getServicesManager().register(Economy.class, this.economyDeBleuEconomy, this, ServicePriority.Normal);

        // 명령어 등록
        getCommand("bal").setExecutor(new BalanceCommand(this.economyDeBleuEconomy));
        getCommand("eco").setExecutor(new EcoCommand(this.economyDeBleuEconomy));

        getLogger().info("EconomyDeBleu가 성공적으로 로드되었습니다!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EconomyDeBleu가 비활성화되었습니다.");
        // 데이터 저장
        savePlayerData();
        // Vault 서비스 등록 해제 (선택 사항이지만 깔끔함)
        getServer().getServicesManager().unregister(Economy.class, this.economyDeBleuEconomy);
    }

    /**
     * balances.yml 파일에서 플레이어 데이터를 로드합니다.
     */
    private void loadPlayerData() {
        dataFile = new File(getDataFolder(), "balances.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile(); // 파일이 없으면 새로 생성
            } catch (IOException e) {
                getLogger().severe("balances.yml 파일을 생성할 수 없습니다: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * 현재 플레이어 잔액 데이터를 balances.yml 파일에 저장합니다.
     */
    public void savePlayerData() {
        // EconomyDeBleuEconomy가 가지고 있는 balances 맵을 dataConfig에 저장
        for (Map.Entry<UUID, Double> entry : economyDeBleuEconomy.getBalances().entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("balances.yml 파일을 저장할 수 없습니다: " + e.getMessage());
        }
    }

    // config.yml 기본값 설정
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        saveDefaultConfig(); // 기본 설정 파일이 없으면 생성
    }
}