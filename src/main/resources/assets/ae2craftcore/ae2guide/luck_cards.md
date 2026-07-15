---
navigation:
  parent: index.md
  title: "Error Correction Cards"
  icon: ae2craftcore:luck_card_1
item_ids:
  - ae2craftcore:luck_card_1
  - ae2craftcore:luck_card_2
  - ae2craftcore:luck_card_3
  - ae2craftcore:luck_card_4
---

# Error Correction (Luck) Cards

<Row>
  <ItemImage id="ae2craftcore:luck_card_1" scale="4" />
  <ItemImage id="ae2craftcore:luck_card_2" scale="4" />
  <ItemImage id="ae2craftcore:luck_card_3" scale="4" />
  <ItemImage id="ae2craftcore:luck_card_4" scale="4" />
</Row>

To prevent automation in the Logic Assembler from turning into endless resource loss, **Error Correction Cards** (also
known as Luck Cards) are used. They are installed in the expansion slots of the Logic Assembler and increase the success
rate of crafting processors.

Each card is created without a grid (shapeless craft) by combining an upgraded AE2 card and a quantum processor of the
corresponding tier:

<Row>
  <RecipeFor id="ae2craftcore:luck_card_1" />
  <RecipeFor id="ae2craftcore:luck_card_2" />
  <RecipeFor id="ae2craftcore:luck_card_3" />
  <RecipeFor id="ae2craftcore:luck_card_4" />
</Row>

---

### 📈 Impact of Correction Cards on Success Rate

Each installed card adds a fixed bonus to the success probability, depending on the tier of the processor being crafted.
Note that high-level recipes do not support basic improvement cards.

|     Output Processor      | Tier I Card Bonus | Tier II Card Bonus | Tier III Card Bonus |    Tier IV Card Bonus    |
|:-------------------------:|:-----------------:|:------------------:|:-------------------:|:------------------------:|
| **Tier I** *(from scrap)* |     **+10%**      |      **+30%**      |      **+60%**       | **+90%** *(Total: 100%)* |
|        **Tier II**        |     **+20%**      |      **+30%**      |      **+40%**       | **+50%** *(Total: 80%)*  |
|       **Tier III**        |     **+10%**      |      **+20%**      |      **+30%**       | **+40%** *(Total: 60%)*  |
|        **Tier IV**        |  *Not supported*  |      **+10%**      |      **+20%**       | **+30%** *(Total: 40%)*  |
|        **Tier V**         |  *Not supported*  |  *Not supported*   |      **+10%**       | **+20%** *(Total: 30%)*  |
|        **Tier VI**        |  *Not supported*  |  *Not supported*   |   *Not supported*   | **+10%** *(Total: 15%)*  |

---

### ⚠ Speed Card Penalties

You can speed up processor assembly using a <ItemLink id="ae2:speed_card" />, but each installed Speed Card imposes a *
*penalty of -1% to the final success rate**.