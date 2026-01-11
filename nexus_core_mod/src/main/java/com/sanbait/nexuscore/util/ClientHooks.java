package com.sanbait.nexuscore.util;

import net.minecraftforge.fml.DistExecutor;

public class ClientHooks {

    public static boolean isShiftDown() {
        return DistExecutor.unsafeRunForDist(
                () -> () -> net.minecraft.client.gui.screens.Screen.hasShiftDown(),
                () -> () -> false);
    }
}
