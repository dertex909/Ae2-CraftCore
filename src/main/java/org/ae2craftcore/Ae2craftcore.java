package org.ae2craftcore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.ae2craftcore.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Ae2craftcore.MODID)
public class Ae2craftcore {
    public static final String MODID = "ae2craftcore";
    public static final String MOD_NAME = "Ae2 CraftCore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public Ae2craftcore(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("=== {} v{} ===", MOD_NAME, modContainer.getModInfo().getVersion());

        AutoBlockRegistry.register(modEventBus);
        AutoItemRegistry.register(modEventBus);
        AutoAttachmentRegistry.register(modEventBus);
        ModSounds.register(modEventBus);
        AutoCreativeTabsRegistry.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        AutoBlockEntityRegistry.register(modEventBus);
        modEventBus.addListener(AutoNetworkRegistry::registerPayloads);
        modEventBus.addListener(Ae2Setup::commonSetup);
    }
}