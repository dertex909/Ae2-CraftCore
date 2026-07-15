---
navigation:
  parent: index.md
  title: "Ячейки Хранения Рецептов"
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

# Ячейки Хранения Рецептов и Компоненты

Ячейки хранения рецептов — это цифровые носители данных, предназначенные для хранения виртуальных шаблонов автоматизации
в МЭ-сети.

---

### 📦 1. Корпус ячейки

Основой любой ячейки является её физический корпус (<ItemLink id="ae2craftcore:recipe_cell_housing" />). Он собирается
на верстаке из кварцевого стекла, красной пыли и золотых слитков.
<RecipeFor id="ae2craftcore:recipe_cell_housing" />

---

### 💾 2. Компоненты памяти

Для увеличения емкости ячеек создаются специальные электронные платы памяти. Каждый последующий уровень требует плату
предыдущего тира и соответствующий **Квантовый процессор**.

* **1k компонент**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_1k" />
* **4k компонент**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_4k" />
* **16k компонент**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_16k" />
* **64k компонент**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_64k" />
* **256k компонент**:
  <RecipeFor id="ae2craftcore:recipe_cell_component_256k" />

---

### 🔋 3. Сборка и ТТХ готовых ячеек

Сборка ячейки осуществляется путем объединения корпуса и компонента на верстаке (доступен как простой бесформенный
крафт, так и полноценная сборка в сетке).

#### Спецификация и лимиты накопителей:

| Тип Ячейки                  | Лимит групп (машин) | Лимит рецептов |                            Пример сборки                             |
|:----------------------------|:-------------------:|:--------------:|:--------------------------------------------------------------------:|
| **Рецептурная ячейка 1k**   |        **2**        |     **8**      |  <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_1k" />  |
| **Рецептурная ячейка 4k**   |        **6**        |     **32**     |  <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_4k" />  |
| **Рецептурная ячейка 16k**  |       **16**        |    **128**     | <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_16k" />  |
| **Рецептурная ячейка 64k**  |       **32**        |    **256**     | <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_64k" />  |
| **Рецептурная ячейка 256k** |       **64**        |    **1024**    | <Recipe id="ae2craftcore:crafting_table/recipe_storage_cell_256k" /> |

*Примечание:* Поместите готовую ячейку в <ItemLink id="ae2:drive" /> или <ItemLink id="ae2:chest" /> вашего МЭ-хранилища
для активации.