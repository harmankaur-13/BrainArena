# BrainArena

BrainArena is an interactive command-line quiz game written in pure Java (no build tools required). Play solo or compete in **real-time fastest-finger LAN battles** across multiple PCs over the same Wi-Fi network!

---

## ⚡ Zero-Install Wi-Fi Battle (via Netcat / Telnet)

Your friends **do not need any Java code, do not need to install anything, and do not need any database credentials**. Every Mac, Windows, and Linux computer already has `nc` (Netcat) or `telnet` pre-installed!

---

### 🎮 How to Play

#### 🖥️ Step 1: On YOUR Computer (Host)
Run in your terminal:
```bash
./run.sh
```
1. Select **`2. Host Live Battle (Fastest Finger LAN)`**.
2. Choose your topic, number of questions, and round timer (e.g. 10s).
3. The lobby screen will display:
   ```text
   ============================================================
            ⚡ LIVE FASTEST-FINGER MULTIPLAYER BATTLE ⚡       
   ============================================================
     YOUR Wi-Fi IP:      192.168.168.66
     Battle Port:        5555
   ------------------------------------------------------------
     TELL YOUR FRIENDS ON WI-FI TO TYPE THIS IN THEIR TERMINAL:
       nc 192.168.168.66 5555
       (or: telnet 192.168.168.66 5555)
   ============================================================
   ```
4. As your friends connect, you will see them appear live:
   `>> Alex joined the battle room! (Total players: 2)`
5. When everyone is in, press **[ENTER]** in your terminal to start the battle!

---

#### 💻 Step 2: On YOUR FRIENDS' Computers (No Code Needed!)
Your friends open their regular terminal (Terminal on Mac/Linux, PowerShell / Command Prompt on Windows) and run:
```bash
nc 192.168.168.66 5555
```
*(or `telnet 192.168.168.66 5555`)*

1. Their screen immediately clears and displays:
   ```text
   ==================================================
            🧠 WELCOME TO BRAINARENA BATTLE 🧠       
   ==================================================
   Enter your player name: 
   ```
2. They enter their name and join your lobby!
3. When you start, questions appear on all screens simultaneously with a live countdown timer.
4. They simply type `A`, `B`, `C`, or `D` and hit Enter to buzz in.
5. Fastest buzz-in gets the **+5 Speed Bonus**!
