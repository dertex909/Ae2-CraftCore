package org.ae2craftcore.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LogicAssemblerRecipe implements Recipe<RecipeInput> {
    private final Ingredient top;
    private final Ingredient bottom;
    private final ItemStack result;
    private final float chance;
    private final List<RecipeUpgrade> upgrades;
    private final NonNullList<Ingredient> ingredients;

    public LogicAssemblerRecipe(Ingredient top, Ingredient bottom, ItemStack result, float chance, List<RecipeUpgrade> upgrades) {
        this.top = top;
        this.bottom = bottom;
        this.result = result;
        this.chance = chance;
        this.upgrades = upgrades;
        this.ingredients = NonNullList.create();
        this.ingredients.add(top);
        this.ingredients.add(bottom);
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public Ingredient getTop() {
        return top;
    }

    public Ingredient getBottom() {
        return bottom;
    }

    public float getChance() {
        return chance;
    }

    public List<RecipeUpgrade> getUpgrades() {
        return upgrades;
    }

    @Override
    public boolean matches(RecipeInput input, @NotNull Level level) {
        if (input.size() < 2) return false;
        var topStack = input.getItem(0);
        var bottomStack = input.getItem(1);
        return this.top.test(topStack) && this.bottom.test(bottomStack);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput input, HolderLookup.@NotNull Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return this.result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public record RecipeUpgrade(Item card, float chanceBonus) {
        public static final Codec<RecipeUpgrade> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(BuiltInRegistries.ITEM.byNameCodec().fieldOf("card").forGetter(RecipeUpgrade::card),
                        Codec.FLOAT.fieldOf("chance_bonus").forGetter(RecipeUpgrade::chanceBonus)).apply(inst, RecipeUpgrade::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RecipeUpgrade> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(BuiltInRegistries.ITEM.byNameCodec()), RecipeUpgrade::card,
                ByteBufCodecs.FLOAT, RecipeUpgrade::chanceBonus, RecipeUpgrade::new
        );
    }

    public record LogicAssemblerInput(ItemStack top, ItemStack bottom) implements RecipeInput {
        @Override
        public @NotNull ItemStack getItem(int index) {
            return index == 0 ? top : bottom;
        }

        @Override
        public int size() {
            return 2;
        }
    }

    public static class Type implements RecipeType<LogicAssemblerRecipe> {
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer implements RecipeSerializer<LogicAssemblerRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<LogicAssemblerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("top").forGetter(LogicAssemblerRecipe::getTop),
                Ingredient.CODEC.fieldOf("bottom").forGetter(LogicAssemblerRecipe::getBottom),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.FLOAT.fieldOf("chance").forGetter(LogicAssemblerRecipe::getChance),
                Codec.list(RecipeUpgrade.CODEC).optionalFieldOf("upgrades", List.of()).forGetter(LogicAssemblerRecipe::getUpgrades)
        ).apply(inst, LogicAssemblerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, LogicAssemblerRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, LogicAssemblerRecipe::getTop,
                Ingredient.CONTENTS_STREAM_CODEC, LogicAssemblerRecipe::getBottom,
                ItemStack.STREAM_CODEC, r -> r.result,
                ByteBufCodecs.FLOAT, LogicAssemblerRecipe::getChance,
                RecipeUpgrade.STREAM_CODEC.apply(ByteBufCodecs.list()), LogicAssemblerRecipe::getUpgrades,
                LogicAssemblerRecipe::new
        );

        @Override
        public @NotNull MapCodec<LogicAssemblerRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, LogicAssemblerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}