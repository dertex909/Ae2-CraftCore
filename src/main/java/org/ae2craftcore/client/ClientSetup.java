package org.ae2craftcore.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.client.screen.LogicAssemblerScreen;
import org.ae2craftcore.registry.ModMenuTypes;

@EventBusSubscriber(modid = Ae2craftcore.MODID)
public class ClientSetup {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.LOGIC_ASSEMBLER.get(), LogicAssemblerScreen::new);
    }
}