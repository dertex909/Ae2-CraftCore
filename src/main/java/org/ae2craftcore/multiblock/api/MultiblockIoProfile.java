package org.ae2craftcore.multiblock.api;

public record MultiblockIoProfile(
        boolean fluidInsert,
        boolean fluidExtract,
        boolean itemInsert,
        boolean itemExtract,
        boolean energyInsert,
        boolean energyExtract,
        boolean dataInsert,
        boolean dataExtract
) {

    public static MultiblockIoProfile none() {
        return new MultiblockIoProfile(false, false, false, false, false, false, false, false);
    }

    public static MultiblockIoProfile fluidInput() {
        return new MultiblockIoProfile(true, false, false, false, false, false, false, false);
    }

    public static MultiblockIoProfile fluidOutput() {
        return new MultiblockIoProfile(false, true, false, false, false, false, false, false);
    }
}