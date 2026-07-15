---
navigation:
  parent: index.md
  title: "Recipe Storage Cells"
  icon: ae2craftcore:recipe_storage_cell_64k
item_ids:
  - ae2craftcore:recipe_cell_housing
  - ae2craftcore:recipe_cell_component_1k
  - ae2craftcore:recipe_cell_component_4k
  - ae2craftcore:recipe_cell_component_16k
  - ae2craftcore:recipe_cell_component_64k
  - ae2craftcore:recipe_cell_component_256k
  - ae2craftcore:recipe_storage_cell_1k
  - ae2craftcore:recipe_storage_cell_4k
  - ae2craftcore:recipe_storage_cell_16k
  - ae2craftcore:recipe_storage_cell_64k
  - ae2craftcore:recipe_storage_cell_256k
---

# Recipe Storage Cells and Components

Recipe storage cells are digital data carriers designed to store virtual automation patterns in the ME network.

---

### 📦 1. Cell Housing

The base of any cell is its physical housing (<ItemLink id="ae2craftcore:recipe_cell_housing" />). It is assembled on a
crafting table using quartz glass, redstone, and gold ingots.
<RecipeFor id="ae2craftcore:recipe_cell_housing" />

---

### 💾 2. Memory Components

To increase the capacity of the cells, special electronic memory boards are created. Each subsequent level requires a
component of the previous tier and the corresponding **Quantum Processor**.

* **1k component**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_1k" />
* **4k component**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_4k" />
* **16k component**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_16k" />
* **64k component**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_64k" />
* **256k component**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_256k" />

---

### 🔋 3. Assembly and Specifications of Finished Cells

Assembling the cell is done by combining the housing and the component on a crafting table (available as a simple
shapeless craft or a full crafting grid recipe).

#### Storage Specifications and Limits:

| Cell Type                    | Group Limit (Machines) | Recipe Limit |                           Assembly Example                           |
|:-----------------------------|:----------------------:|:------------:|:--------------------------------------------------------------------:|
| **1k Recipe Storage Cell**   |         **2**          |    **8**     |  <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_1k" />  |
| **4k Recipe Storage Cell**   |         **6**          |    **32**    |  <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_4k" />  |
| **16k Recipe Storage Cell**  |         **16**         |   **128**    | <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_16k" />  |
| **64k Recipe Storage Cell**  |         **32**         |   **256**    | <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_64k" />  |
| **256k Recipe Storage Cell** |         **64**         |   **1024**   | <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_256k" /> |

*Note:* Place the finished cell into an <ItemLink id="ae2:drive" /> of your ME storage to activate it.