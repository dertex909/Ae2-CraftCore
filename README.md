# ☭ AE2 Craft Core — Break the Chains of Pattern Encoding!

**SAY NO TO PATTERN ENCODING! SAVE YOUR TIME AND RESOURCES!**

Are you tired of spending countless hours of your life manually encoding hundreds of blank patterns in the terminal? Fed up with building massive walls of Pattern Providers, messing up P2P sub-networks, and fighting cable spaghetti just to automate complex expert modpacks?

**AE2 Craft Core** completely redefines the rules of automation by turning physical patterns into **digital data**. Welcome to the era of virtualized autocrafting!

---

### ⚙️ How It Works

1. **Insert the Storage Cell:** First, insert a **Recipe Storage Cell** (tiers available from 1k to 256k) into the dedicated slot of the new **ME Recipe Terminal**. This is your digital recipe drive.
2. **Configure and Encode:** Set up your recipe in the terminal as usual, type the target Machine Group name (e.g. `Industrial Crusher`) in the input box, and click the **Save** button. The recipe is written directly as digital data onto the cell inside the terminal.
3. **Place the Cell in your Network:** Take the loaded Recipe Storage Cell out of the terminal and place it into any standard **ME Drive** or **ME Chest** connected to your network. The entire grid instantly reads and registers every recipe stored on that cell.
4. **Name Your Interface:** Place an **ME Machine Interface** adjacent to your machine (furnace, crusher, custom workbench, etc.). Give it a matching name in the GUI (e.g., `Industrial Crusher`).
5. **Done!** The interface will automatically fetch all recipes assigned to the `Industrial Crusher` group from the network, and dynamically push ingredients into the machine when a craft is requested. **There is physically not a single pattern item inside the interface itself!**

---

### 🌟 Key Features

* **Recipe Virtualization:** Say goodbye to physical blank patterns. Store hundreds of recipes digitally on compact storage cells inside your ME Drives.
* **No Cable Spaghetti:** You no longer need to place dozens of pattern providers for a setup of identical machines. A single ME Machine Interface can broadcast an unlimited number of recipes to a group of machines.
* **Highly Optimized (TPS-Friendly):** The smart background grid cache (`RecipeCacheService`) only recalculates network topology when storage cells are inserted or extracted, guaranteeing zero server lag during heavy autocrafting.
* **Seamless JEI / EMI / REI Integration:** Native out-of-the-box support for recipe transfer (the "+" button) across all popular mod managers.
* **Universal Machine Compatibility:** Works flawlessly with standard inventories (`IItemHandler` / `IFluidHandler`) as well as custom AE2-compatible crafting machines.

---

**Stop hoarding chests full of patterns. Upgrade to software-defined autocrafting today!**