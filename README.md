# EcoTown

EcoTown 是一個為 Paper / Folia 伺服器設計的生態城鎮插件。它將傳統領地系統與 [EcoChain](https://github.com/TLeung094/EcoChain) 的 Flora、Fauna、Aqua 生態數值結合，讓城鎮的環境狀況直接決定特產、名聲及跨鎮經濟。

> 你的環境行為決定城鎮特產；城鎮特產與交易則塑造城鎮名聲。

## 功能特色

### 城鎮與領地

- 建立、解散城鎮及邀請鎮民
- 依 EcoChain 生態評分限制領地擴張
- 防止非鎮民破壞、放置方塊及進行 PVP
- 設定城鎮中心並支援 Folia 非同步傳送
- SQLite 持久化城鎮、成員、領地及傳送點

### 角色化 GUI

輸入 `/town` 即可打開整合主選單。

**鎮長控制台**提供：

- 生態報告與當前特產
- 城鎮名聲及全服排名
- 跨鎮交易
- 領地擴張與解除
- 設定城鎮中心
- 邀請鎮民與解散城鎮

**鎮民中心**只顯示日常功能：

- 生態報告
- 城鎮名聲
- 跨鎮交易
- 返回城鎮中心

鎮民不會看到鎮長專屬的領地和管理操作。

### 生態特產

EcoTown 會計算城鎮已載入領地區塊的平均生態值。每個生態類別只啟用目前符合門檻的最高一級特產；跌穿門檻後會立即停止或降級。

| 條件 | 特產 | 每次產量 |
|---|---|---:|
| Flora >= 60 | 黃金蘿蔔、西瓜片、南瓜、可可豆 | 3-5 |
| Flora >= 40 | 小麥、薯仔、紅蘿蔔、甜菜根 | 5-8 |
| Fauna >= 60 | 皮革、羽毛、兔腳、羊肉、命名牌 | 2-4 |
| Fauna >= 40 | 生牛肉、生豬肉、生雞肉、骨粉 | 4-6 |
| Aqua >= 60 | 熱帶魚、海晶碎片、鸚鵡螺殼、海洋之心 | 2-4 |
| Aqua >= 40 | 生鱈魚、生三文魚、墨囊、海帶 | 4-6 |

- 預設每 5 分鐘生成一次
- 生成位置為城鎮中心，鎮長必須先使用 `/town setspawn`
- 高階特產每日消耗對應生態值 1 點
- 基礎特產隔日消耗 1 點，平均每日 0.5 點
- 名聲等級會提高特產產量

> 生態平均值只計算 EcoChain 當前已載入的城鎮區塊。查看報告前，請確保相關領地附近有玩家並已載入。

### 城鎮名聲

| 名聲值 | 等級 | 特產產量加成 |
|---:|---|---:|
| 0-99 | 萌芽小鎮 | 0% |
| 100-249 | 生機聚落 | 5% |
| 250-499 | 繁榮城鎮 | 10% |
| 500-999 | 輝煌都市 | 15% |
| 1000+ | 傳奇王國 | 20% |

名聲來源包括：

- 每日依城鎮生態平均總分結算
- 所有在線鎮民每小時各貢獻名聲
- 跨鎮交易成功後，買賣雙方城鎮都會獲得名聲
- 交易名聲預設每鎮每日最多 10 點
- 任一生態值跌至 0 以下會受到名聲懲罰

### 跨鎮實物交易

EcoTown 內建不依賴經濟插件的實物交換市場。

1. 輸入 `/town trade` 打開 27 格掛單編輯器。
2. 在第 11 格放入出售品。
3. 在第 15 格放入要求交換的物品及數量。
4. 點擊確認後，出售品會進入託管；交換品範本會退回背包。
5. 從交易市場瀏覽其他城鎮掛單並完成交換。
6. 賣方可在市場領取交易所得。

交易保障：

- 只允許不同城鎮之間成交
- 每名玩家最多建立 9 個掛單
- 掛單及待領物資保存於 SQLite，重啟不會消失
- 取消掛單或解散城鎮時會退還託管物品
- 背包已滿時，退還物品會掉落在玩家附近
- 暫不支援帶名稱、附魔或其他自訂資料的物品

## 指令

| 指令 | 功能 |
|---|---|
| `/town` | 打開角色化城鎮主選單 |
| `/town create <名稱>` | 建立城鎮 |
| `/town claim` | 佔領腳下區塊，限鎮長 |
| `/town unclaim` | 解除腳下領地，限鎮長 |
| `/town invite <玩家>` | 邀請在線玩家，限鎮長 |
| `/town accept` | 接受城鎮邀請 |
| `/town setspawn` | 設定城鎮中心，限鎮長 |
| `/town spawn` | 返回城鎮中心 |
| `/town disband` | 解散城鎮，限鎮長 |
| `/town eco` | 打開城鎮生態報告 GUI |
| `/town eco map` | 列出已載入領地區塊的生態分佈 |
| `/town eco stats` | 顯示詳細生態資料 |
| `/town specialties` | 打開目前特產報告 |
| `/town reputation` | 查看城鎮名聲與等級 |
| `/town reputation top` | 查看全服名聲前十名 |
| `/town trade` | 打開跨鎮交易掛單 GUI |

## 權限

| 權限 | 預設 | 說明 |
|---|---|---|
| `town.eco.stats` | OP | 查看詳細城鎮生態統計 |

其他玩家指令目前透過城鎮角色與程式內權限檢查控制。

## PlaceholderAPI

安裝 PlaceholderAPI 後可使用：

| Placeholder | 輸出 |
|---|---|
| `%ecotown_name%` | 玩家所屬城鎮名稱 |
| `%ecotown_name_formatted%` | 格式化城鎮標籤 |
| `%ecotown_role%` | 鎮長、鎮民或流浪者 |

## 設定

首次啟動會產生 `plugins/EcoTown/config.yml`：

```yaml
global:
  time-zone: "Asia/Hong_Kong"
  spawn-interval-seconds: 300
  daily-cost-hour: 0
  global-spawn-multiplier: 1.0

thresholds:
  high: 60
  low: 40

reputation:
  online-per-hour: 1
  trade-per-transaction: 1
  trade-daily-limit: 10
  eco-total-per: 10
  eco-below-zero-penalty: 5
```

| 設定 | 說明 |
|---|---|
| `global.time-zone` | 每日結算使用的 IANA 時區 |
| `global.spawn-interval-seconds` | 特產生成間隔，最低 60 秒 |
| `global.daily-cost-hour` | 每日生態消耗結算小時，0-23 |
| `global.global-spawn-multiplier` | 全服特產產量倍率 |
| `thresholds.high` | 高階特產門檻 |
| `thresholds.low` | 基礎特產門檻 |
| `reputation.online-per-hour` | 每名在線鎮民每小時貢獻名聲 |
| `reputation.trade-per-transaction` | 每次成功交易給予雙方城鎮的名聲 |
| `reputation.trade-daily-limit` | 每鎮每日可從交易獲得的名聲上限 |
| `reputation.eco-total-per` | 每多少點生態總分換算 1 名聲 |
| `reputation.eco-below-zero-penalty` | 任一生態值低於 0 時的每日名聲懲罰 |

修改設定後需重新啟動伺服器。

## 安裝需求

- Paper / Folia，API 版本 26.2 或相容版本
- Java 21 或以上
- [EcoChain](https://github.com/TLeung094/EcoChain)，必要依賴
- PlaceholderAPI，可選依賴

安裝步驟：

1. 將 EcoChain 與 EcoTown JAR 放入伺服器的 `plugins/`。
2. 確認伺服器使用 Java 21 或以上。
3. 啟動伺服器，等待插件建立 SQLite 資料庫及設定檔。
4. 玩家使用 `/town` 開始建立及管理城鎮。

## 從原始碼建置

EcoTown 使用 Maven。EcoChain 必須先存在於本機 Maven repository：

```bash
mvn install:install-file \
  -Dfile=EcoChain-1.0-SNAPSHOT.jar \
  -DgroupId=me.tleung \
  -DartifactId=EcoChain \
  -Dversion=1.0-SNAPSHOT \
  -Dpackaging=jar
```

目前 Shade Plugin 需以 Java 21 目標位元碼打包：

```bash
mvn clean package -Djava.version=21
```

輸出檔案：

```text
target/EcoTown-1.0-SNAPSHOT.jar
```

## 資料儲存

EcoTown 使用 `plugins/EcoTown/ecotown.db` 保存：

- 城鎮、鎮長與成員
- 領地與城鎮中心
- 名聲值及每日交易名聲狀態
- 跨鎮交易掛單
- 交易待領物資

EcoChain 負責保存各區塊的 Flora、Fauna 與 Aqua 數值。

## 專案

- EcoTown: <https://github.com/TLeung094/EcoTown>
- EcoChain: <https://github.com/TLeung094/EcoChain>
- 作者：TLeung
