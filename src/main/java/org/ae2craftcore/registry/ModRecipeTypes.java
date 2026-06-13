package org.ae2craftcore.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.recipe.LogicAssemblerRecipe;

@SuppressWarnings("unused")
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Ae2craftcore.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Ae2craftcore.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<LogicAssemblerRecipe>> LOGIC_ASSEMBLING_TYPE = RECIPE_TYPES.register(
            "logic_assembling", () -> LogicAssemblerRecipe.Type.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<LogicAssemblerRecipe>> LOGIC_ASSEMBLING_SERIALIZER = RECIPE_SERIALIZERS.register(
            "logic_assembling", () -> LogicAssemblerRecipe.Serializer.INSTANCE);

    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }
}