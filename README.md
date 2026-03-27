# Zyvera-TicTacToe v1.0

**TicTacToe Minigame Plugin für Minecraft Server**
*Autoren: Thomas U. & Zyvera-Systems*

---

## Kompatibilität

| Server-Software | Unterstützt |
|-----------------|-------------|
| Bukkit          | ✅          |
| Spigot          | ✅          |
| Paper           | ✅          |
| Purpur          | ✅          |
| Folia           | ✅ (Regionized Scheduler via Reflection) |

### Minecraft Versionen

| Version | Status |
|---------|--------|
| 1.13.x  | ✅ Volle Unterstützung (api-version Baseline) |
| 1.14.x  | ✅ Volle Unterstützung |
| 1.15.x  | ✅ Volle Unterstützung |
| 1.16.x  | ✅ Volle Unterstützung |
| 1.17.x  | ✅ Volle Unterstützung |
| 1.18.x  | ✅ Volle Unterstützung |
| 1.19.x  | ✅ Volle Unterstützung |
| 1.20.x  | ✅ Volle Unterstützung |
| 1.21.x  | ✅ Volle Unterstützung |

- **Java:** 8+ (kompiliert mit Java 8 Target)
- **Abhängigkeiten:** Keine (Standalone-Plugin)

**Hinweis:** Das Plugin verwendet Material-Fallbacks (z.B. `RED_CONCRETE` → `STAINED_CLAY`) für ältere Versionen, echte Spielerköpfe über `SkullMeta` mit Reflection-Fallback, und die Folia Scheduler-Abstraktion wird vollständig über Reflection gelöst — keine Compile-Time Abhängigkeit zu Folia.

---

## Features

### Spielmodi
- **Warteschlange (Ranked):** `/ttt play` — Automatisches Matchmaking, Stats werden gezählt
- **Herausforderung (Unranked):** `/ttt challenge <Spieler>` — Direkte Duelle, keine Stats
- **Werkbank-Bindung:** Rechtsklick auf gebundene Werkbank = Queue Toggle (mit 1s Cooldown)

### GUI-System
- **Hauptmenü:** Übersichtliches Menü mit Queue, Stats und Top-Spieler
- **Spiel-GUI:** Kompaktes 3x3 Board
- **End-Screen:** Gewinn-Linie wird grün hervorgehoben
- **Stats-GUI:** Siege, Niederlagen, Unentschieden, Winrate, Züge, Serien
- **Top-Spieler GUI:** Rangliste der besten 10 Spieler

### Timeout-System
- **Kein Zug nach 120s:** Spiel wird abgebrochen (keine Stats)
- **Ranked Queue:** Wenn ein Spieler 120s nicht zieht → der andere gewinnt automatisch
- **Challenge (Unranked):** Timeout hat keinen Einfluss

### Hologramme
- Gebundene Werkbänke zeigen ein schwebendes Hologramm:
  - Zeile 1: `§6§lTicTacToe`
  - Zeile 2: `§8[§aKlick Mich§8]`

### Werkbank-Bindung
- `/ttt bind` → Klick auf Werkbank = TicTacToe-Station
- Rechtsklick = **Toggle** (Queue beitreten/verlassen)
- 1 Sekunde Cooldown gegen Spam
- Persistent in `workbenches.yml`

---

## Befehle

| Befehl | Beschreibung | Permission |
|--------|-------------|------------|
| `/ttt` | Hauptmenü öffnen | `zyvera.ttt.use` |
| `/ttt play` | Warteschlange beitreten | `zyvera.ttt.use` |
| `/ttt leave` | Warteschlange verlassen | `zyvera.ttt.use` |
| `/ttt challenge <n>` | Spieler herausfordern | `zyvera.ttt.use` |
| `/ttt accept` | Herausforderung annehmen | `zyvera.ttt.use` |
| `/ttt deny` | Herausforderung ablehnen | `zyvera.ttt.use` |
| `/ttt stats [Name]` | Statistiken anzeigen (GUI) | `zyvera.ttt.use` |
| `/ttt top` | Top-Spieler Rangliste (GUI) | `zyvera.ttt.use` |
| `/ttt quit` | Aktives Spiel verlassen | `zyvera.ttt.use` |
| `/ttt bind` | Werkbank binden (Bind-Modus) | `zyvera.ttt.bind` |
| `/ttt unbind` | Werkbank-Bindung entfernen | `zyvera.ttt.bind` |
| `/ttt reload` | Config neuladen | `zyvera.ttt.admin` |
| `/ttt help` | Hilfe anzeigen | `zyvera.ttt.use` |

**Alias:** `/tictactoe`

---

## Permissions

| Permission | Beschreibung | Standard |
|-----------|-------------|----------|
| `zyvera.ttt.use` | Grundnutzung von /ttt | `true` |
| `zyvera.ttt.bind` | Werkbänke binden/entbinden | `op` |
| `zyvera.ttt.stats.others` | Stats anderer Spieler ansehen | `true` |
| `zyvera.ttt.admin` | Admin-Befehle (reload) | `op` |

---

## Installation

1. **Bauen:**
   ```bash
   mvn clean package
   ```
2. **JAR kopieren:**
   `target/Zyvera-TicTacToe-1.0.0.jar` → `plugins/`
3. **Server starten** — Config wird automatisch generiert
4. **Fertig!**

---


## Lizenz

© Thomas U. & Zyvera-Systems — Alle Rechte vorbehalten.
