---
navigation:
  parent: index.md
  title: "Logic Assembler"
  icon: ae2craftcore:logic_assembler
item_ids:
  - ae2craftcore:logic_assembler
  - ae2craftcore:quantum_processor_1
  - ae2craftcore:quantum_processor_2
  - ae2craftcore:quantum_processor_3
  - ae2craftcore:quantum_processor_4
  - ae2craftcore:quantum_processor_5
  - ae2craftcore:quantum_processor_6
  - ae2craftcore:quantum_scrap
---

# Logic Assembler and Quantum Processors

<Row>
  <BlockImage id="ae2craftcore:logic_assembler" scale="4" />
</Row>

The <ItemLink id="ae2craftcore:logic_assembler" /> is the central machine of the addon, designed for the step-by-step
synthesis of high-tech logic circuits: from Quantum Processor I to Quantum Processor VI.

### Crafting Recipe for the Assembler:

To assemble this machine, you will need copper and iron ingots, fluix pearls, singularity, and one Quantum Processor
Tier I:
<RecipeFor id="ae2craftcore:logic_assembler" />

---

### ⚙ Processor Crafting Mechanics

Crafting processors in the Logic Assembler happens in stages. Each processor tier has its own base success rate.

* **On success:** you receive the target processor of a higher tier.
* **On failure:** the source processors are destroyed, and the Assembler
  outputs <ItemLink id="ae2craftcore:quantum_scrap" />.
<ItemGrid>
  <ItemIcon id="ae2craftcore:quantum_scrap" />
</ItemGrid>

> 💡 *Don't throw away the scrap! Two pieces of Quantum Scrap can be used to attempt crafting a Quantum Processor Tier I
with a 10% base chance in the Logic Assembler.*

---

### 📊 Base Recipe and Assembly Chance Table

| Assembly Result           | Component (2 pieces)                               | Base Success Chance |
|:--------------------------|:---------------------------------------------------|:--------------------|
| **Quantum Processor I**   | <ItemLink id="ae2craftcore:quantum_scrap" />       | **10%** (0.10)      |
| **Quantum Processor II**  | <ItemLink id="ae2craftcore:quantum_processor_1" /> | **30%** (0.30)      |
| **Quantum Processor III** | <ItemLink id="ae2craftcore:quantum_processor_2" /> | **20%** (0.20)      |
| **Quantum Processor IV**  | <ItemLink id="ae2craftcore:quantum_processor_3" /> | **10%** (0.10)      |
| **Quantum Processor V**   | <ItemLink id="ae2craftcore:quantum_processor_4" /> | **10%** (0.10)      |
| **Quantum Processor VI**  | <ItemLink id="ae2craftcore:quantum_processor_5" /> | **5%** (0.05)       |

*Alternative method to obtain a Tier I processor:* you can craft Quantum Processor I with a 100% chance in a standard
Inscriber by combining three basic processors: Engineering, Logic, and Calculation.
<RecipeFor id="ae2craftcore:quantum_processor_1" />