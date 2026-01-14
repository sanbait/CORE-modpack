package com.sanbait.shadowgrid.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ShadowGridCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shadowgrid")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("unlock")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int x = IntegerArgumentType.getInteger(context, "x");
                                            int z = IntegerArgumentType.getInteger(context, "z");

                                            GridSavedData data = GridSavedData.get(context.getSource().getLevel());
                                            data.unlockSector(x, z);

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Unlocked Sector: " + x + ":" + z), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("reloadbiomes")
                        .executes(context -> {
                            com.sanbait.shadowgrid.world.BiomeGridConfig.reloadConfig();
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Reloaded biome config from disk"), true);
                            return 1;
                        })));
    }
}
