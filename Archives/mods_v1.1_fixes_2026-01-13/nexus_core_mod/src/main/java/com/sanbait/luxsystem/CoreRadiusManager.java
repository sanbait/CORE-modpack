package com.sanbait.luxsystem;

import com.sanbait.nexuscore.NexusCoreEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class CoreRadiusManager {
    private static final Set<NexusCoreEntity> activeCores = new HashSet<>();

    public static void addCore(NexusCoreEntity core) {
        activeCores.add(core);
    }

    public static void removeCore(NexusCoreEntity core) {
        activeCores.remove(core);
    }

    public static boolean isInRadius(Level level, BlockPos pos) {
        if (level.isClientSide)
            return false;

        // Cleanup Logic: Remove dead/unloaded cores to prevent memory leaks and lag
        activeCores.removeIf(core -> core.isRemoved() || !core.isAlive());

        for (NexusCoreEntity core : activeCores) {
            if (core.level() == level) {
                // Calculate radius based on Entity level
                // Base: 10 + (Level * 2) from Config default which seems to coincide with GDD
                // Or call config directly?
                // Let's use a safe assumption for now to fix the core loop.
                double radius = 10.0 + (core.getCurrentLevel() * 2.0);

                if (core.blockPosition().distSqr(pos) <= (radius * radius)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean hasCore(Level level) {
        activeCores.removeIf(core -> core.isRemoved());
        for (NexusCoreEntity core : activeCores) {
            if (core.level() == level) return true;
        }
        return false;
    }
}
