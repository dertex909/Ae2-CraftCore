/*
 * Ae2 CraftCore
 * Copyright (C) 2026 dertex909
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.ae2craftcore.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record LogicAssemblerRecipe(Ingredient top, Ingredient bottom, Identifier resultId, int resultCount,
                                   float chance,
                                   List<RecipeUpgrade> upgrades) implements Recipe<LogicAssemblerRecipe.LogicAssemblerInput> {

    @Override
    public boolean matches(LogicAssemblerInput input, @NotNull Level level) {
        return this.top.test(input.top()) && this.bottom.test(input.bottom());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull LogicAssemblerInput input) {
        return getResultItem();
    }

    public @NotNull ItemStack getResultItem() {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.tryParse(resultId.toString()));
        return new ItemStack(item, resultCount);
    }

    @Override
    public @NotNull RecipeSerializer<LogicAssemblerRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<LogicAssemblerRecipe> getType() {
        return Type.INSTANCE;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull String group() {
        return "";
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
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

    public static final MapCodec<LogicAssemblerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("top").forGetter(LogicAssemblerRecipe::top),
            Ingredient.CODEC.fieldOf("bottom").forGetter(LogicAssemblerRecipe::bottom),
            Identifier.CODEC.fieldOf("id").forGetter(LogicAssemblerRecipe::resultId),
            Codec.INT.optionalFieldOf("count", 1).forGetter(LogicAssemblerRecipe::resultCount),
            Codec.FLOAT.fieldOf("chance").forGetter(LogicAssemblerRecipe::chance),
            Codec.list(RecipeUpgrade.CODEC).optionalFieldOf("upgrades", List.of()).forGetter(LogicAssemblerRecipe::upgrades)
    ).apply(inst, LogicAssemblerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LogicAssemblerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, LogicAssemblerRecipe::top,
            Ingredient.CONTENTS_STREAM_CODEC, LogicAssemblerRecipe::bottom,
            Identifier.STREAM_CODEC, LogicAssemblerRecipe::resultId,
            ByteBufCodecs.VAR_INT, LogicAssemblerRecipe::resultCount,
            ByteBufCodecs.FLOAT, LogicAssemblerRecipe::chance,
            RecipeUpgrade.STREAM_CODEC.apply(ByteBufCodecs.list()), LogicAssemblerRecipe::upgrades,
            LogicAssemblerRecipe::new
    );

    public static final RecipeSerializer<LogicAssemblerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}