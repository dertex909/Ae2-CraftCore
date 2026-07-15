---
navigation:
  parent: index.md
  title: "Сборщик Логики"
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

# Сборщик Логики и Квантовые Процессоры

<Row>
  <BlockImage id="ae2craftcore:logic_assembler" scale="4" />
</Row>

<ItemLink id="ae2craftcore:logic_assembler" /> — центральный механизм модификации, предназначенный для поэтапного синтеза высокотехнологичных логических схем: от Квантового процессора I до Квантового процессора VI.

### Рецепт создания Сборщика:
Для сборки этого прибора вам понадобятся медные и железные слитки, жемчуг флюикса, сингулярность, а также один Квантовый процессор I уровня:
<RecipeFor id="ae2craftcore:logic_assembler" />

---

### ⚙ Механика создания процессоров

Создание процессоров в Сборщике Логики происходит поэтапно. Каждому уровню процессора соответствует свой базовый шанс успеха.

* **В случае успеха:** вы получаете целевой процессор более высокого уровня.
* **В случае неудачи:** исходные процессоры разрушаются, а на выходе Сборщик выдаёт <ItemLink id="ae2craftcore:quantum_scrap" />.
<ItemGrid>
  <ItemIcon id="ae2craftcore:quantum_scrap" />
</ItemGrid>
> 💡 *Не выбрасывайте металлолом! Из двух единиц Квантового металлолома можно снова попытаться собрать Квантовый процессор I уровня с базовым шансом 10% в самом Сборщике.*

---

### 📊 Таблица базовых рецептов и шансов сборки

| Результат сборки            | Компонент (2 штуки)                                | Базовый шанс успеха |
|:----------------------------|:---------------------------------------------------|:--------------------|
| **Квантовый процессор I**   | <ItemLink id="ae2craftcore:quantum_scrap" />       | **10%** (0.10)      |
| **Квантовый процессор II**  | <ItemLink id="ae2craftcore:quantum_processor_1" /> | **30%** (0.30)      |
| **Квантовый процессор III** | <ItemLink id="ae2craftcore:quantum_processor_2" /> | **20%** (0.20)      |
| **Квантовый процессор IV**  | <ItemLink id="ae2craftcore:quantum_processor_3" /> | **10%** (0.10)      |
| **Квантовый процессор V**   | <ItemLink id="ae2craftcore:quantum_processor_4" /> | **10%** (0.10)      |
| **Квантовый процессор VI**  | <ItemLink id="ae2craftcore:quantum_processor_5" /> | **5%** (0.05)       |

*Альтернативный способ получения процессора I уровня:* вы можете изготовить Квантовый процессор I со 100% вероятностью в обычном высекателе, объединив три стандартных процессора: инженерный, логический и вычислительный.
<RecipeFor id="ae2craftcore:quantum_processor_1" />