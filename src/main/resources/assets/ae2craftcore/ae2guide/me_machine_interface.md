---
navigation:
  parent: index.md
  title: "ME Machine Interface"
  icon: ae2craftcore:me_machine_interface
item_ids:
  - ae2craftcore:me_machine_interface
---

# ME Machine Interface

<Row>
  <BlockImage id="ae2craftcore:me_machine_interface" scale="4" />
</Row>

The <ItemLink id="ae2craftcore:me_machine_interface" /> is a special block that acts as a "smart" bridge between the ME
auto-crafting network and external machinery.

### Crafting Recipe for the Interface:

To craft this machine, you will need fluix crystals, fluix pearls, an ME Pattern Provider, and a Quantum Processor Tier
III:
<RecipeFor id="ae2craftcore:me_machine_interface" />

---

### ⚙ Integration and Configuration Principles

1. **Block Mounting:** Install the Interface adjacent to any receiving inventory of a machine (from any technical mod).
2. **Virtual Recipes:** This block does not have physical slots for classic patterns. It reads all available recipes
   from <ItemLink id="ae2craftcore:recipe_storage_cell_1k" /> installed in drives, provided the recipe group name
   matches the assigned name of the interface.
3. **Blocking Mode:** Prevents overflowing the connected machine's inventory. The ME network will not send the next
   batch of crafting resources until the machine has finished processing the previous one.
4. **Locking Modes:** Allow pausing the craft depending on the supplied redstone signal.