package com.sanbait.nexuscore;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(NexusCore.MODID)
public class NexusCore {
    public static final String MODID = "nexuscore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,
            MODID);

    public static final RegistryObject<EntityType<NexusCoreEntity>> NEXUS_CORE = ENTITIES.register("core",
            () -> EntityType.Builder.of(NexusCoreEntity::new, MobCategory.MISC)
                    .sized(1.5f, 3.0f) // Sized for a large crystal/core
                    .build("core"));

    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS = DeferredRegister
            .create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<net.minecraft.world.item.Item> CORE_ITEM = ITEMS.register("core_item",
            () -> new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties()) {
                @Override
                public net.minecraft.world.InteractionResult useOn(
                        net.minecraft.world.item.context.UseOnContext context) {
                    if (!context.getLevel().isClientSide) {
                        // SINGLETON RULE: Check if one already exists in loaded entities
                        // Manual AABB covering the world since getBounds is missing
                        java.util.List<NexusCoreEntity> existing = context.getLevel().getEntitiesOfClass(
                                NexusCoreEntity.class, new net.minecraft.world.phys.AABB(-30000000.0D, -64.0D,
                                        -30000000.0D, 30000000.0D, 500.0D, 30000000.0D));

                        if (!existing.isEmpty()) {
                            context.getPlayer().displayClientMessage(net.minecraft.network.chat.Component
                                    .translatable("message.nexuscore.core_exist_error"), true);
                            return net.minecraft.world.InteractionResult.FAIL;
                        }

                        NexusCoreEntity entity = NEXUS_CORE.get().create(context.getLevel());
                        if (entity != null) {
                            entity.setPos(context.getClickLocation());
                            context.getLevel().addFreshEntity(entity);
                            context.getItemInHand().shrink(1);
                            return net.minecraft.world.InteractionResult.CONSUME;
                        }
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            });

    public static final RegistryObject<net.minecraft.world.item.Item> CORE_KEY = ITEMS.register("core_key",
            () -> new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties()) {
                @Override
                public net.minecraft.world.InteractionResult interactLivingEntity(
                        net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.player.Player player,
                        net.minecraft.world.entity.LivingEntity target, net.minecraft.world.InteractionHand hand) {
                    if (target instanceof NexusCoreEntity core && player.isShiftKeyDown()) {
                        if (!player.level().isClientSide) {
                            core.discard(); // Remove
                                            // entity
                            // Drop item
                            core.spawnAtLocation(CORE_ITEM.get());
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component.translatable("message.nexuscore.core_removed"),
                                    true);
                        }
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                    return super.interactLivingEntity(stack, player, target, hand);
                }
            });

    // Valid CreativeModeTab for 1.20.1
    public static final DeferredRegister<net.minecraft.world.item.CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<net.minecraft.world.item.CreativeModeTab> NEXUS_TAB = CREATIVE_MODE_TABS
            .register("nexus_tab", () -> net.minecraft.world.item.CreativeModeTab.builder()
                    .icon(() -> CORE_ITEM.get().getDefaultInstance())
                    .title(net.minecraft.network.chat.Component.translatable("creativetab.nexus_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(CORE_ITEM.get());
                        output.accept(CORE_KEY.get());
                    }).build());

    public NexusCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Initialize GeckoLib
        GeckoLib.initialize();

        net.minecraftforge.fml.ModLoadingContext.get()
                .registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, NexusCoreConfig.SPEC);

        ENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register event handlers
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::entityAttributeCreation);
        modEventBus.addListener(this::clientSetup);
        // Duplicate removed

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void registerCapabilities(net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent event) {
        event.register(com.sanbait.luxsystem.capabilities.ILuxStorage.class);
    }

    private void entityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(NEXUS_CORE.get(), NexusCoreEntity.createAttributes().build());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        EntityRenderers.register(NEXUS_CORE.get(), NexusCoreRenderer::new);
    }

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = MODID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onEntityJoinLevel(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof net.minecraft.world.entity.monster.Monster monster) {
                // Prevent Duplicate Goals: Check if we already have a specialized
                // CoreAttackGoal
                boolean hasGoal = monster.targetSelector.getAvailableGoals().stream()
                        .anyMatch(goal -> goal.getGoal() instanceof CoreAttackGoal);

                if (!hasGoal) {
                    monster.targetSelector.addGoal(2, new CoreAttackGoal(monster, true));
                }
            }

            // State-Based Lighting: Ensure light exists when Core joins level
            if (event.getEntity() instanceof NexusCoreEntity core && !event.getLevel().isClientSide) {
                com.sanbait.nexuscore.util.ServerLightManager.forceCoreLight(core);
            }
        }

        public static class CoreAttackGoal
                extends net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<NexusCoreEntity> {
            public CoreAttackGoal(net.minecraft.world.entity.monster.Monster mob, boolean mustSee) {
                super(mob, NexusCoreEntity.class, mustSee);
            }
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onLivingChangeTarget(net.minecraftforge.event.entity.living.LivingChangeTargetEvent event) {
            if (event.getOriginalTarget() instanceof NexusCoreEntity core) {
                // If the mob was targeting Core, and tries to switch...
                if (event.getNewTarget() != core) {
                    // ALLOW switching to Player (Self defense)
                    if (event.getNewTarget() instanceof net.minecraft.world.entity.player.Player) {
                        return;
                    }

                    // Check distance (if still close enough, deny switch to random stuff like
                    // villagers)
                    if (event.getEntity().distanceToSqr(core) < 2500) { // 50 blocks sq
                        event.setCanceled(true); // Force keep target on Core
                    }
                }
            }
        }

        // Prevent throwing the core item
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onItemToss(net.minecraftforge.event.entity.item.ItemTossEvent event) {
            if (event.getEntity().getItem().getItem() == CORE_ITEM.get()) {
                event.setCanceled(true);
                if (event.getPlayer() != null) {
                    event.getPlayer().getInventory().add(event.getEntity().getItem());
                }
            }
        }

        // Remove item from drops on death
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onPlayerDrops(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
            if (event.getEntity() instanceof net.minecraft.world.entity.player.Player) {
                event.getDrops().removeIf(drop -> drop.getItem().getItem() == CORE_ITEM.get());
            }
        }

        // Restore item on Respawn
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                net.minecraft.world.entity.player.Player player = event.getEntity();
                boolean hasCore = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (player.getInventory().getItem(i).getItem() == CORE_ITEM.get()) {
                        hasCore = true;
                        break;
                    }
                }
                if (!hasCore) {
                    player.getInventory().add(new net.minecraft.world.item.ItemStack(CORE_ITEM.get()));
                }
            }
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.END && !event.player.level().isClientSide
                    && event.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                com.sanbait.nexuscore.util.ServerLightManager.tickPlayer(serverPlayer);
            }
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onPlayerLogout(
                net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                com.sanbait.nexuscore.util.ServerLightManager.onPlayerLoggedOut(serverPlayer);
            }
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onEquipmentChange(net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent event) {
            com.sanbait.nexuscore.util.ServerLightManager.onEquipmentChange(event);
        }

        // Universal Lux Capability Attachment
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void attachCapabilities(
                net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.world.item.ItemStack> event) {
            net.minecraft.world.item.ItemStack stack = event.getObject();
            // Check Tag: nexuscore:lux_receptive
            // Since we can't easily check tags on ItemStack during attachment (sometimes
            // tags aren't loaded yet on empty stacks),
            // checking the Item's registry tag or config whitelist is safer.
            // For now, let's look for our own items AND the tag.

            boolean isLuxItem = stack.getItem() instanceof com.sanbait.luxsystem.capabilities.ILuxStorage;
            // Note: If using pure capability, ILuxStorage on Item class is just a marker
            // interface now, or we remove it.
            // Let's support the legacy hardcoded items checking too.

            if (isLuxItem || isReceptiveItem(stack)) {
                com.sanbait.luxsystem.capabilities.LuxProvider provider = new com.sanbait.luxsystem.capabilities.LuxProvider();

                // Determine Capacity
                int capacity = 1000; // Default
                if (stack.getItem() instanceof com.sanbait.luxsystem.items.LuxArmorItem armor) {
                    capacity = armor.getMaxLuxStored();
                } else if (stack.getItem() instanceof com.sanbait.luxsystem.items.LuxPickaxeItem pick) {
                    capacity = pick.getMaxLuxStored();
                }

                // Config Override
                int configCap = getCapacityFromConfig(stack);
                if (configCap > 0) {
                    capacity = configCap;
                }

                com.sanbait.luxsystem.capabilities.LuxCapability cap = new com.sanbait.luxsystem.capabilities.LuxCapability(
                        capacity);
                provider.setBackend(cap);

                event.addCapability(new net.minecraft.resources.ResourceLocation(MODID, "lux_storage"), provider);
            }
        }

        private static int getCapacityFromConfig(net.minecraft.world.item.ItemStack stack) {
            java.util.List<? extends String> configs = NexusCoreConfig.ITEM_LUX_CAPACITIES.get();
            net.minecraft.resources.ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS
                    .getKey(stack.getItem());
            if (id == null)
                return -1;

            String key = id.toString();
            for (String entry : configs) {
                String[] parts = entry.split("\\|");
                if (parts.length == 2 && parts[0].equals(key)) {
                    try {
                        return Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return -1;
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onItemTooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
            event.getItemStack().getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
                    .ifPresent(cap -> {
                        // Visual Bar Logic
                        int current = cap.getLuxStored();
                        int max = cap.getMaxLuxStored();
                        float percent = (float) current / max;

                        // Bar Construction (10 segments)
                        int bars = (int) (percent * 10);
                        StringBuilder bar = new StringBuilder();
                        bar.append("§8["); // Dark Gray Bracket
                        for (int i = 0; i < 10; i++) {
                            if (i < bars) {
                                bar.append("§b❙"); // Aqua Filled (or █)
                            } else {
                                bar.append("§7|"); // Gray Empty
                            }
                        }
                        bar.append("§8]");

                        // Calculate Duration based on typical usage (Armor = 1/5s, Tool = 1/use)
                        // Let's assume constant usage for estimation (Armor drain)
                        // 1 lux per 100 ticks (5 sec).
                        // Minutes = (Lux * 5) / 60
                        int minutesLeft = (current * 5) / 60;

                        // Bar Line: [|||||-----] 70%
                        event.getToolTip().add(net.minecraft.network.chat.Component.literal(
                                bar.toString() + " §f" + (int) (percent * 100) + "%"));

                        // Duration Line
                        if (current > 0) {
                            event.getToolTip().add(net.minecraft.network.chat.Component.translatable(
                                    "tooltip.nexuscore.duration", minutesLeft));
                        }

                        // Info
                        if (percent > 0) {
                            event.getToolTip().add(
                                    net.minecraft.network.chat.Component.translatable("tooltip.nexuscore.in_light"));
                        } else {
                            event.getToolTip().add(
                                    net.minecraft.network.chat.Component.translatable("tooltip.nexuscore.outside"));
                        }
                    });
        }

        private static boolean isReceptiveItem(net.minecraft.world.item.ItemStack stack) {
            // Check if item has tag 'nexuscore:lux_receptive'
            // Note: Tags is correct way.
            return stack.is(net.minecraft.tags.ItemTags
                    .create(new net.minecraft.resources.ResourceLocation(MODID, "lux_receptive")));
        }
    }

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = MODID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class ClientModEvents {

        public static final net.minecraft.client.KeyMapping TOGGLE_PARTICLES = new net.minecraft.client.KeyMapping(
                "key.nexuscore.toggle_particles",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                org.lwjgl.glfw.GLFW.GLFW_KEY_P,
                "key.category.nexuscore");

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onKeyRegister(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
            event.register(TOGGLE_PARTICLES);
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void registerOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("nexus_overlay", NexusCoreOverlay.INSTANCE);
        }
    }

    // Global static to track state
    public static boolean RENDER_PARTICLES = true;

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = MODID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class ClientForgeEvents {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onKeyInput(net.minecraftforge.client.event.InputEvent.Key event) {
            if (ClientModEvents.TOGGLE_PARTICLES.consumeClick()) {
                RENDER_PARTICLES = !RENDER_PARTICLES;
                net.minecraft.client.Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component
                                .translatable(RENDER_PARTICLES ? "message.nexuscore.particles.on"
                                        : "message.nexuscore.particles.off"),
                        true);
            }
        }
    }
}
