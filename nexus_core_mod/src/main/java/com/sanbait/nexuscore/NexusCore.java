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
                    .clientTrackingRange(64) // FIX FPS: Reduced from 256 to 64 to prevent performance issues on startup
                    .updateInterval(20) // Update every second (less spam)
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
                        // OPTIMIZATION: Use CoreRadiusManager instead of expensive AABB scan
                        if (com.sanbait.luxsystem.CoreRadiusManager.hasCore(context.getLevel())) {
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

    // Menu Types
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES = DeferredRegister
            .create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<net.minecraft.world.inventory.MenuType<com.sanbait.nexuscore.gui.NexusCoreMenu>> CORE_MENU = MENU_TYPES
            .register("core_menu", () -> net.minecraftforge.common.extensions.IForgeMenuType.create(
                    (windowId, inv, data) -> {
                        int entityId = data.readInt();
                        net.minecraft.world.entity.Entity entity = inv.player.level().getEntity(entityId);
                        if (entity instanceof NexusCoreEntity core) {
                            return new com.sanbait.nexuscore.gui.NexusCoreMenu(windowId, inv, core);
                        }
                        return null;
                    }));

    public NexusCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Initialize GeckoLib
        GeckoLib.initialize();

        net.minecraftforge.fml.ModLoadingContext.get()
                .registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, NexusCoreConfig.SPEC);

        ENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);

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

        // Register Screens - using enqueueWork for thread safety
        event.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(CORE_MENU.get(),
                    com.sanbait.nexuscore.gui.NexusCoreScreen::new);
        });
    }

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = MODID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onEntityJoinLevel(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
            // FIX FPS: Пропускаем обработку если чанк еще не загружен (при загрузке мира)
            if (!event.getLevel().isLoaded(event.getEntity().blockPosition())) {
                return; // Чанк еще не загружен, пропускаем
            }

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
                // Register Core for Radius Manager (Moved from Tick)
                com.sanbait.luxsystem.CoreRadiusManager.addCore(core);
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
        public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
            // FIX FPS: Вся логика ядер перенесена на события, а не на tick() каждой сущности
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                // Обрабатываем все ядра централизованно раз в секунду (каждые 20 тиков)
                if (event.getServer().getTickCount() % 20 == 0) {
                    for (net.minecraft.server.level.ServerLevel level : event.getServer().getAllLevels()) {
                        java.util.List<NexusCoreEntity> cores = level.getEntitiesOfClass(
                                NexusCoreEntity.class, net.minecraft.world.phys.AABB.ofSize(
                                        net.minecraft.world.phys.Vec3.ZERO, 30000000, 300, 30000000));
                        
                        for (NexusCoreEntity core : cores) {
                            if (core.isRemoved() || !core.isAlive()) continue;
                            
                            // Lux Regeneration (setCurrentLux уже синкает с клиентом)
                            int genPerTick = core.getCurrentLevel() * NexusCoreConfig.CORE_LUX_GENERATION_PER_LEVEL.get();
                            int maxLux = core.getCurrentLevel() * NexusCoreConfig.CORE_LUX_CAPACITY_PER_LEVEL.get();
                            int current = core.getCurrentLux();
                            if (current < maxLux) {
                                int next = Math.min(current + genPerTick, maxLux);
                                core.setCurrentLux(next); // setCurrentLux автоматически синкает с клиентом
                            }
                            
                            // Periodic operations (buffs, charging)
                            core.performPeriodicOperations();
                            
                            // Initialize blocks if needed (only once)
                            if (!core.blocksInitialized && core.anchorPos != null) {
                                if (level.isLoaded(core.anchorPos)) {
                                    core.updateCoreBlocks(0, core.getCurrentLevel());
                                    core.blocksInitialized = true;
                                }
                            }
                        }
                    }
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

        // --- CONSUMPTION LOGIC ---

        // --- CONSUMPTION LOGIC ---

        private static int getCostFromConfig(net.minecraft.world.item.ItemStack stack) {
            java.util.List<? extends String> configs = NexusCoreConfig.ITEM_LUX_COSTS.get();
            net.minecraft.resources.ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS
                    .getKey(stack.getItem());
            if (id != null) {
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
            }
            // Fallbacks
            if (stack.getItem() instanceof net.minecraft.world.item.SwordItem) {
                return NexusCoreConfig.DEFAULT_ATTACK_COST.get();
            }
            return NexusCoreConfig.DEFAULT_BLOCK_BREAK_COST.get();
        }

        private static boolean shouldSkipConsumption(net.minecraft.world.item.ItemStack stack) {
            // Check if usages is allowed without lux
            return NexusCoreConfig.ALLOW_USE_WITHOUT_LUX.get();
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onBlockBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
            net.minecraft.world.entity.player.Player player = event.getPlayer();
            if (player != null && !player.level().isClientSide) {
                net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
                stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP).ifPresent(cap -> {
                    int cost = getCostFromConfig(stack);
                    if (cap.getLuxStored() >= cost) {
                        cap.extractLux(cost, false);
                        // Sync
                        stack.getOrCreateTag().putInt("LuxStored", cap.getLuxStored());
                    } else if (!NexusCoreConfig.ALLOW_USE_WITHOUT_LUX.get()) {
                        // Prevent break if not allowed
                        event.setCanceled(true);
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.translatable("message.nexuscore.no_lux"), true);
                    }
                });
            }
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
            if (event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player
                    && !player.level().isClientSide) {
                net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
                stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP).ifPresent(cap -> {
                    int cost = getCostFromConfig(stack);
                    // For weapons, we use Attack Cost default if mapped
                    if (stack.getItem() instanceof net.minecraft.world.item.SwordItem) {
                        // Recalculate cost if generic fallback was used incorrectly?
                        // No, getCostFromConfig handles type check.
                    } else {
                        // If not a sword/tool, maybe we shouldn't consume?
                        // But we want to support any item with Lux cap.
                        // Let's rely on config.
                    }

                    if (cap.getLuxStored() >= cost) {
                        cap.extractLux(cost, false);
                        // Sync
                        stack.getOrCreateTag().putInt("LuxStored", cap.getLuxStored());
                    } else if (!NexusCoreConfig.ALLOW_USE_WITHOUT_LUX.get()) {
                        // Reduce damage if no lux
                        event.setAmount(event.getAmount() * 0.5f);
                        // Limit spam
                        if (player.tickCount % 20 == 0) {
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component.translatable("message.nexuscore.low_lux"),
                                    true);
                        }
                    }
                });
            }
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

            // Improved Checks using Class Inheritance (Works for modded items too)
            boolean isTool = stack.getItem() instanceof net.minecraft.world.item.DiggerItem; // Pickaxe, Axe, Shovel, Hoe
            boolean isWeapon = stack.getItem() instanceof net.minecraft.world.item.SwordItem;
            boolean isArmor = stack.getItem() instanceof net.minecraft.world.item.ArmorItem;
            boolean isRanged = stack.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem; // Bow, Crossbow
            boolean isShield = stack.getItem() instanceof net.minecraft.world.item.ShieldItem;
            boolean isTrident = stack.getItem() instanceof net.minecraft.world.item.TridentItem;

            if (isLuxItem || isReceptiveItem(stack) || isTool || isWeapon || isArmor || isRanged || isShield || isTrident) {
                com.sanbait.luxsystem.capabilities.LuxProvider provider = new com.sanbait.luxsystem.capabilities.LuxProvider();

                // Determine Capacity
                int capacity = 1000; // Default

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
                        // Global Shift Check
                        if (com.sanbait.nexuscore.util.ClientHooks.isShiftDown()) {
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
                                    bar.append("§b|"); // Aqua Filled
                                } else {
                                    bar.append("§7."); // Gray Empty
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
                                        net.minecraft.network.chat.Component
                                                .translatable("tooltip.nexuscore.in_light"));
                            } else {
                                event.getToolTip().add(
                                        net.minecraft.network.chat.Component
                                                .translatable("tooltip.nexuscore.outside"));
                            }
                        } else {
                            // If Shift NOT down, show hint (Only for non-custom items to avoid duplicates)
                            if (!(event.getItemStack()
                                    .getItem() instanceof com.sanbait.luxsystem.capabilities.ILuxStorage)) {
                                event.getToolTip().add(net.minecraft.network.chat.Component
                                        .translatable("tooltip.nexuscore.hold_shift")
                                        .withStyle(net.minecraft.ChatFormatting.GRAY));
                            }
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

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void registerItemDecorators(net.minecraftforge.client.event.RegisterItemDecorationsEvent event) {
            // Register decorator for ALL items - LuxItemDecorator is optimized (uses NBT cache)
            // Only renders if item has LuxStored NBT tag, avoiding expensive capability lookups
            for (net.minecraft.world.item.Item item :
                    net.minecraftforge.registries.ForgeRegistries.ITEMS) {
                event.register(item, com.sanbait.nexuscore.client.LuxItemDecorator.INSTANCE);
            }
        }


    }

    // Global static to track state
    public static boolean RENDER_PARTICLES = true;

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = MODID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class ClientForgeEvents {
        // FIX FPS: Частицы убраны почти в ноль - спавнятся очень редко (раз в 200 тиков = 10 секунд)
        private static int clientTickCounter = 0; // Счетчик для частиц
        
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.END && NexusCore.RENDER_PARTICLES) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.level != null && mc.player != null) {
                    clientTickCounter++;
                    
                    // FIX FPS: Increased frequency for visibility (was 200, now 5)
                    if (clientTickCounter % 5 == 0) {
                        java.util.List<NexusCoreEntity> cores = mc.level.getEntitiesOfClass(
                                NexusCoreEntity.class,
                                mc.player.getBoundingBox().inflate(64)); // Increased range slightly
                        
                        // Обрабатываем все ядра рядом
                        for (NexusCoreEntity core : cores) {
                            if (core.isRemoved() || !core.isAlive()) continue;
                            
                            // Spawn radius particles (multiple to make a ring)
                            core.spawnRadiusParticles();
                            
                            // Glow particles (less frequent)
                            if (clientTickCounter % 20 == 0) {
                                core.spawnGlowParticles();
                            }
                        }
                    }
                }
            }
        }

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
