package dev.o8o1o5.economyDeBleu;

import dev.o8o1o5.economyDeBleu.command.BalanceCommand;
import dev.o8o1o5.economyDeBleu.command.EconomyCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class EconomyDeBleu extends JavaPlugin {
    private EconomyDeBleuEconomy economyDeBleuEconomy;
    private File dataFile;
    private FileConfiguration dataConfig;

    private String currencySingular;
    private String currencyPlural;

    @Override
    public void onEnable() {
        // 1. config.yml 파일이 없으면 생성하고 기본값 로드 (가장 먼저 실행)
        saveDefaultConfig();

        // 2. 화폐 단위 로드 (config.yml에서 읽어와 currencySingular/Plural 초기화)
        //    이제 economyDeBleuEconomy 생성 시 null이 전달되지 않습니다.
        loadCurrencyUnits();

        // 3. balances.yml 파일 설정 및 로드 준비
        dataFile = new File(getDataFolder(), "balances.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                getLogger().info("balances.yml 파일을 생성했습니다."); // 파일 생성 로그 추가
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "balances.yml 파일을 생성할 수 없습니다: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // 4. EconomyDeBleuEconomy 인스턴스 초기화 (여기서 초기화된 화폐 단위 사용)
        economyDeBleuEconomy = new EconomyDeBleuEconomy(this, currencySingular, currencyPlural);

        // 5. 플레이어 데이터 로드 (economyDeBleuEconomy가 초기화된 후에 호출)
        loadPlayerData();

        // 6. Vault 서비스 등록
        getServer().getServicesManager().register(Economy.class, economyDeBleuEconomy, this, ServicePriority.Normal);

        // 7. 명령어 등록
        PluginCommand economyCommand = getCommand("economy");
        if (economyCommand != null) {
            economyCommand.setExecutor(new EconomyCommand(this)); // 'this'를 넘겨줘서 메인 클래스 접근 가능하게
        } else {
            getLogger().severe("명령어 'economy'를 plugin.yml에서 찾을 수 없습니다!");
        }

        PluginCommand balanceCommand = getCommand("balance");
        if (balanceCommand != null) {
            balanceCommand.setExecutor(new BalanceCommand(economyDeBleuEconomy));
        } else {
            getLogger().severe("명령어 'balance'를 plugin.yml에서 찾을 수 없습니다!");
        }

        getLogger().info("EconomyDeBleu가 활성화되었습니다.");
        getLogger().info("balances.yml에서 " + economyDeBleuEconomy.getBalances().size() + "개의 플레이어 잔액을 로드했습니다.");
    }

    @Override
    public void onDisable() {
        getLogger().info("EconomyDeBleu 플러그인이 비활성화되었습니다.");
        if (this.economyDeBleuEconomy != null) {
            savePlayerData();
            getServer().getServicesManager().unregister(Economy.class, this.economyDeBleuEconomy);
        }
    }

    private void loadCurrencyUnits() {
        FileConfiguration config = getConfig();
        // config.yml에서 화폐 단위를 로드, 없으면 기본값 "원" 사용
        this.currencySingular = config.getString("currency.singular", "원");
        // **수정됨:** currency.plural 값을 정확히 참조하도록 변경
        this.currencyPlural = config.getString("currency.plural", "원");
        getLogger().info("화폐 단위 로드: 단수='" + currencySingular + "', 복수='" + currencyPlural + "'");
    }

    public void setCurrencyUnits(String singular, String plural) {
        this.currencySingular = singular;
        this.currencyPlural = plural;
        FileConfiguration config = getConfig();
        config.set("currency.singular", singular);
        config.set("currency.plural", plural);
        saveConfig(); // 변경된 config.yml 저장

        if (economyDeBleuEconomy != null) {
            economyDeBleuEconomy.setcurrencyUnits(singular, plural);
        }
    }

    public String getCurrencySingular() {
        return currencySingular;
    }

    public String getCurrencyPlural() {
        return currencyPlural;
    }

    public FileConfiguration getDataConfig() { // 이 메서드는 외부 노출 주의 (캡슐화)
        return dataConfig;
    }

    public EconomyDeBleuEconomy getEconomyDeBleuEconomy() {
        return economyDeBleuEconomy;
    }

    public void loadPlayerData() {
        if (economyDeBleuEconomy == null) {
            // 이 경고는 이제 onEnable() 순서가 바뀌었으므로 발생하지 않아야 합니다.
            getLogger().warning("EconomyDeBleuEconomy가 아직 초기화되지 않아 플레이어 잔액을 로드할 수 없습니다.");
            return;
        }

        // dataConfig를 최신 상태로 다시 로드하여 외부 변경 사항 반영 (선택 사항이지만 안전)
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        int loadedCount = 0;
        for (String uuidStr : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                double balance = dataConfig.getDouble(uuidStr);
                economyDeBleuEconomy.setBalance(uuid, balance);
                loadedCount++;
            } catch (IllegalArgumentException e) {
                getLogger().warning("잘못된 UUID 형식: " + uuidStr + " - balances.yml에서 스킵합니다.");
            }
        }
        // 이 로그는 onEnable() 마지막 로그와 중복될 수 있으므로, 하나만 남기는 것을 고려
        // getLogger().info("balances.yml에서 " + loadedCount + "개의 플레이어 잔액을 로드했습니다.");
    }

    public void savePlayerData() {
        if (this.economyDeBleuEconomy == null) {
            getLogger().warning("EconomyDeBleuEconomy가 초기화되지 않아 잔액을 저장할 수 없습니다.");
            return;
        }
        for (Map.Entry<UUID, Double> entry : economyDeBleuEconomy.getBalances().entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
            // getLogger().info("플레이어 잔액을 balances.yml에 저장했습니다."); // 너무 자주 출력될 수 있으니 주석 처리
        } catch (IOException e) {
            getLogger().severe("balances.yml 파일을 저장할 수 없습니다: " + e.getMessage());
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        loadCurrencyUnits(); // config.yml 재로드 후 화폐 단위 다시 로드
    }
}