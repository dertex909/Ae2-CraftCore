package org.ae2craftcore.multiblock.definition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.multiblock.api.MultiblockDefinition;
import org.ae2craftcore.multiblock.api.MultiblockNodeSpec;
import org.ae2craftcore.multiblock.api.MultiblockPortSpec;

import java.util.List;
import java.util.function.Predicate;

public abstract class BaseMultiblockDefinition implements MultiblockDefinition {

    private final ResourceLocation id;
    private final List<MultiblockNodeSpec> nodes;
    private final List<MultiblockPortSpec> ports;
    private final Predicate<BlockState> controllerMatcher;
    private final Predicate<BlockState> partMatcher;

    protected BaseMultiblockDefinition(ResourceLocation id,
                                       List<MultiblockNodeSpec> nodes, List<MultiblockPortSpec> ports,
                                       Predicate<BlockState> controllerMatcher, Predicate<BlockState> partMatcher) {
        this.id = id;
        this.nodes = List.copyOf(nodes);
        this.ports = List.copyOf(ports);
        this.controllerMatcher = controllerMatcher;
        this.partMatcher = partMatcher;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public List<MultiblockNodeSpec> nodes() {
        return nodes;
    }

    @Override
    public List<MultiblockPortSpec> ports() {
        return ports;
    }

    @Override
    public boolean matchesController(BlockState state) {
        return controllerMatcher.test(state);
    }

    @Override
    public boolean matchesPart(BlockState state) {
        return partMatcher.test(state);
    }
}