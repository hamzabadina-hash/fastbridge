package com.hamzabadina.fastbridge;

import net.fabricmc.api.ModInitializer;

public class FastBridgeMod implements ModInitializer {
    public static final String MOD_ID = "fastbridge";

    @Override
    public void onInitialize() {
        FastBridgeConfig.load();
    }
}
