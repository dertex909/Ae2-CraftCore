AE2 CraftCore 1.2.3 resolves JEI recipe transfer conflicts with AE2WTLib wireless terminals and introduces a dedicated
transfer handler for pattern encoding. Players can now write crafting and processing recipes directly from JEI into
pattern terminals without missing buttons or mod incompatibilities.

---

### 🔥 What's New for Players & Automation Engineers

* **Dedicated JEI Recipe Transfer Handler**: Implemented a native JEI transfer handler (`PatternRecipeTransferHandler`)
  to reliably encode patterns directly from recipe views.
    * **Crafting & Processing Modes**: Seamlessly transfers standard 3x3 crafting recipes as well as custom processing
      recipes into the pattern encoding interface.
    * **Item & Fluid Ingredients**: Properly parses both standard items (`VanillaTypes.ITEM_STACK`) and NeoForge fluid
      ingredients (`NeoForgeTypes.FLUID_STACK`) when sending recipes to terminal slots.

---

### ⚡ Performance & Stability

* **AE2WTLib Terminal Compatibility (`MixinJeiTransfer`)**: Resolved an issue where the JEI `+` button disappeared
  inside AE2WTLib Wireless Pattern Encoding Terminals (and third-party terminal addons). The container check is now
  strictly bound to `PatternEncodingTermMenu.TYPE`, preventing CraftCore from overwriting transfer handlers registered
  by other mods (fixes #1).
* **AE2 Dependency Alignment**: Updated internal AE2 integrations to match the latest API targets, preventing JEI recipe
  category registration halts on client boot.