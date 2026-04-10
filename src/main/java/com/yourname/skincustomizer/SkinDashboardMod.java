package com.yourname.skincustomizer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkinDashboardMod implements ClientModInitializer {
    public static final String MOD_ID = "skincustomizer";
    
    @Override
    public void onInitializeClient() {
        // Client-side init - agar baad me config/events chahiye ho
    }
}
