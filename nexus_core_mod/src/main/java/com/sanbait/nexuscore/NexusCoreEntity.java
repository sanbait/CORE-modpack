package com.sanbait.nexuscore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NexusCoreEntity extends PathfinderMob
        implements GeoEntity, com.sanbait.luxsystem.capabilities.ILuxStorage {
    private static final EntityDataAccessor<Integer> CURRENT_LEVEL = SynchedEntityData.defineId(NexusCoreEntity.class,
            EntityDataSerializers.INT);
    // NEW: Sync Current Lux to Client for Rendering
    private static final EntityDataAccessor<Integer> CURRENT_LUX = SynchedEntityData.defineId(NexusCoreEntity.class,
            EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public NexusCoreEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.setNoAi(true); // Disable AI for the Core itself to save performance
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CURRENT_LEVEL, 1); // Start at Level 1
        this.entityData.define(CURRENT_LUX, 0); // Start with 0 Lux
    }

    public int getCurrentLevel() {
        return this.entityData.get(CURRENT_LEVEL);
    }

    // Lux getters/setters using EntityData
    public int getCurrentLux() {
        return this.entityData.get(CURRENT_LUX);
    }

    public void setCurrentLux(int lux) {
        this.entityData.set(CURRENT_LUX, lux);
    }

    public void setCurrentLevel(int level) {
        this.entityData.set(CURRENT_LEVEL, Mth.clamp(level, 1, 10)); // Max level 10
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(calculateMaxHealth());
        this.setHealth(this.getMaxHealth());
    }

    private double calculateMaxHealth() {
        return this.getCurrentLevel() * NexusCoreConfig.HP_PER_LEVEL.get();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (CURRENT_LEVEL.equals(key)) {
            this.refreshDimensions();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 5.0D);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(1.5f, 1.0f * this.getCurrentLevel());
    }

    private net.minecraft.core.BlockPos anchorPos = null;

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.level().isClientSide) {
            com.sanbait.nexuscore.util.ServerLightManager.onCoreRemoved(this);
            com.sanbait.luxsystem.CoreRadiusManager.removeCore(this);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // ANCHOR LOGIC: Force position to stay at anchor
        if (!this.level().isClientSide) {
            com.sanbait.luxsystem.CoreRadiusManager.addCore(this); // Ensure registered

            if (this.anchorPos == null) {
                this.anchorPos = this.blockPosition(); // Set anchor on first tick/spawn
            } else if (this.distanceToSqr(this.anchorPos.getX() + 0.5, this.anchorPos.getY(),
                    this.anchorPos.getZ() + 0.5) > 0.01) {
                this.setPos(this.anchorPos.getX() + 0.5, this.anchorPos.getY(), this.anchorPos.getZ() + 0.5);
                this.setDeltaMovement(0, 0, 0); // Kill momentum
            }

            // Server Side Logic - OPTIMIZED TICKS
            // Mob Attraction is now handled by Vanilla AI Goals (EntityJoinLevelEvent)
            // Apply buffs and charge items every 1 second (20 ticks)
            // User requested events, but for "Area of Effect" charging, a centralized
            // periodic check
            // from the Source (Core) is the most performant "Server Event" we can create.

            // Lux Regeneration Logic
            int genPerTick = this.getCurrentLevel() * NexusCoreConfig.CORE_LUX_GENERATION_PER_LEVEL.get();
            int maxLux = this.getCurrentLevel() * NexusCoreConfig.CORE_LUX_CAPACITY_PER_LEVEL.get();

            // Regenerate
            int current = getCurrentLux();
            if (current < maxLux) {
                int next = Math.min(current + genPerTick, maxLux);
                setCurrentLux(next);
            }

            if (this.tickCount % 20 == 0) {
                applyBuffs();
                chargeNearbyItems();
            }

            // Server Side Light Manager - Removed periodic tick as per user request (State
            // Based)
            // com.sanbait.nexuscore.util.ServerLightManager.tickCore(this);
        } else {
            // Client Side Logic (Visuals)
            spawnRadiusParticles();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("NexusLevel", this.getCurrentLevel());
        if (this.anchorPos != null) {
            compound.putInt("AnchorX", this.anchorPos.getX());
            compound.putInt("AnchorY", this.anchorPos.getY());
            compound.putInt("AnchorZ", this.anchorPos.getZ());
        }
        compound.putInt("NexusLux", this.getCurrentLux());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("NexusLevel")) {
            this.setCurrentLevel(compound.getInt("NexusLevel"));
        }
        if (compound.contains("NexusLux")) {
            this.setCurrentLux(compound.getInt("NexusLux"));
        }
        if (compound.contains("AnchorX")) {
            this.anchorPos = new net.minecraft.core.BlockPos(
                    compound.getInt("AnchorX"),
                    compound.getInt("AnchorY"),
                    compound.getInt("AnchorZ"));
        }
    }

    // Helper for HUD and Interaction
    public static String getNextUpgradeCost(int currentLvl) {
        java.util.List<? extends String> costs = NexusCoreConfig.UPGRADE_COSTS.get();
        if (costs.isEmpty())
            return "Unknown";

        int costIndex = Math.min(currentLvl - 1, costs.size() - 1);
        String costStr = costs.get(costIndex);
        String[] parts = costStr.split("\\|");
        if (parts.length != 2)
            return "Error";

        net.minecraft.resources.ResourceLocation loc = new net.minecraft.resources.ResourceLocation(parts[0]);
        net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(loc);
        int amount = 1;
        try {
            amount = Integer.parseInt(parts[1]);
        } catch (Exception e) {
        }

        if (item == null)
            return "Invalid Item";

        return amount + "x " + item.getDescription().getString();
    }

    private void chargeNearbyItems() {
        double radius = NexusCoreConfig.BASE_RADIUS.get()
                + (this.getCurrentLevel() * NexusCoreConfig.RADIUS_PER_LEVEL.get());
        AABB searchBox = this.getBoundingBox().inflate(radius);

        java.util.List<net.minecraft.world.entity.player.Player> players = this.level().getEntitiesOfClass(
                net.minecraft.world.entity.player.Player.class, searchBox);

        for (net.minecraft.world.entity.player.Player player : players) {
            // Scan inventory for ILuxStorage items
            // Main hand
            chargeItem(player.getMainHandItem());
            chargeItem(player.getOffhandItem());
            // Armor
            for (net.minecraft.world.item.ItemStack armor : player.getArmorSlots()) {
                chargeItem(armor);
            }
        }
    }

    private void chargeItem(net.minecraft.world.item.ItemStack stack) {
        int coreLux = getCurrentLux();
        if (coreLux <= 0)
            return; // No power in core

        stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP).ifPresent(cap -> {
            int max = cap.getMaxLuxStored();
            int current = cap.getLuxStored();
            if (current < max) {
                // Determine transfer rate (e.g. up to 10 per tick per item)
                // Since this runs every 20 ticks (1 sec), let's give a chunk, e.g. 20 (1 per
                // sec equivalent? no, faster)
                // Let's make it snappy: 50 lux per second (pulse)
                int transfer = 50;

                // Cap by Core storage
                int actualTransfer = Math.min(transfer, getCurrentLux());
                // Cap by Item space
                actualTransfer = Math.min(actualTransfer, max - current);

                if (actualTransfer > 0) {
                    cap.receiveLux(actualTransfer, false);
                    extractLux(actualTransfer, false); // Drain from core
                }
            }
        });
    }

    // ILuxStorage Implementation
    @Override
    public int getLuxStored() {
        return getCurrentLux();
    }

    @Override
    public int getMaxLuxStored() {
        return this.getCurrentLevel() * NexusCoreConfig.CORE_LUX_CAPACITY_PER_LEVEL.get();
    }

    @Override
    public int receiveLux(int maxReceive, boolean simulate) {
        int space = getMaxLuxStored() - getLuxStored();
        int accepted = Math.min(space, maxReceive);
        if (!simulate) {
            setCurrentLux(getLuxStored() + accepted);
        }
        return accepted;
    }

    @Override
    public int extractLux(int maxExtract, boolean simulate) {
        int current = getLuxStored();
        int extracted = Math.min(current, maxExtract);
        if (!simulate) {
            setCurrentLux(current - extracted);
        }
        return extracted;
    }

    private void applyBuffs() {
        double radius = NexusCoreConfig.BASE_RADIUS.get()
                + (this.getCurrentLevel() * NexusCoreConfig.RADIUS_PER_LEVEL.get());
        AABB searchBox = this.getBoundingBox().inflate(radius);

        // Give Regeneration and Resistance to Players
        this.level().getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, searchBox).forEach(player -> {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.REGENERATION, 100, 0, true, false));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 100, 0, true, false));
        });
    }

    private void spawnRadiusParticles() {
        if (!NexusCore.RENDER_PARTICLES || !this.isAlive())
            return; // Toggle check & Dead check

        // OPTIMIZATION: Distance Culling (Don't render if player is far away)
        net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null && this.distanceToSqr(player) > 64 * 64) {
            return;
        }

        double radius = NexusCoreConfig.BASE_RADIUS.get()
                + (this.getCurrentLevel() * NexusCoreConfig.RADIUS_PER_LEVEL.get());

        // Spawn particles in a circle
        // OPTIMIZATION: Reduced from 10 per tick (200/sec) to 2 per tick (40/sec) to
        // save FPS.
        for (int i = 0; i < 2; i++) {
            double angle = this.random.nextDouble() * 2 * Math.PI;
            double x = this.getX() + radius * Math.cos(angle);
            double z = this.getZ() + radius * Math.sin(angle);
            double y = this.getY() + 0.5D;

            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            // Visuals: Bleed particles (Redstone dust look-alike or purely red particles)
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1.0f, 0.0f, 0.0f),
                                1.0f),
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        20, 0.5, 0.5, 0.5, 0.1);
                this.playSound(net.minecraft.sounds.SoundEvents.IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        if (source.getEntity() instanceof net.minecraft.world.entity.player.Player) {
            return true;
        }
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            return true;
        }
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        if (!this.level().isClientSide) {
            int currentLvl = this.getCurrentLevel();
            if (currentLvl > 1) {
                // Level Down Logic
                this.setCurrentLevel(currentLvl - 1);
                this.setHealth(this.getMaxHealth());

                // Visuals
                this.level().levelEvent(2001, this.blockPosition(), net.minecraft.world.level.block.Block
                        .getId(net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState()));
                this.playSound(net.minecraft.sounds.SoundEvents.ANVIL_BREAK, 1.0F, 1.0F);

                return; // Prevent death
            }
        }
        super.die(source);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
        // Immobile
    }

    @Override
    public void setDeltaMovement(double x, double y, double z) {
        super.setDeltaMovement(0, 0, 0); // Immobile
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public boolean canBeLeashed(net.minecraft.world.entity.player.Player player) {
        return false;
    }

    // ... (upgrade logic follows)

    public void upgrade() {
        int current = this.getCurrentLevel();
        if (current < 10) {
            this.setCurrentLevel(current + 1);
            this.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
        }
    }

    @Override
    protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        if (!this.level().isClientSide && hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
            net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
            int lvl = this.getCurrentLevel();

            // Get requirement for *next* level (or same level for healing?).
            // Logic: To heal/upgrade Level X core, we often use Level X resource (or X->X+1
            // resource).
            // Let's use the resource defined for "Current Level -> Next Level".
            // If Max level, we use the last defined resource (index 9).

            java.util.List<? extends String> costs = NexusCoreConfig.UPGRADE_COSTS.get();
            int costIndex = Math.min(lvl - 1, costs.size() - 1); // Level 1 uses index 0 (Copper)

            // Safety check for empty config
            if (costs.isEmpty())
                return super.mobInteract(player, hand);

            String costStr = costs.get(costIndex);
            // Parse "modid:item|amount"
            String[] parts = costStr.split("\\|");
            if (parts.length != 2)
                return super.mobInteract(player, hand);

            net.minecraft.resources.ResourceLocation loc = new net.minecraft.resources.ResourceLocation(parts[0]);
            net.minecraft.world.item.Item neededItem = net.minecraftforge.registries.ForgeRegistries.ITEMS
                    .getValue(loc);
            int amountNeeded = 1;
            try {
                amountNeeded = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }

            if (neededItem == null || neededItem == net.minecraft.world.item.Items.AIR) {
                // Invalid config fallback
                return super.mobInteract(player, hand);
            }

            // CHECK: Is player holding the item?
            if (stack.getItem() == neededItem) {

                // HEALING LOGIC: If HP < MaxHP, heal instead of upgrade
                if (this.getHealth() < this.getMaxHealth()) {
                    float healAmount = 20.0f;
                    float missing = this.getMaxHealth() - this.getHealth();
                    if (healAmount > missing)
                        healAmount = missing; // Cap exactly to Max

                    stack.shrink(1);
                    this.heal(healAmount);
                    this.playSound(net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR, 1.0F, 1.0F);
                    if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, this.getX(),
                                this.getY() + 1.0, this.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
                    }
                    // side like this, need
                    // casting
                    // Just play sound. Particles for healing happen automatically often.
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }

                // UPGRADE LOGIC: Only if Full HP
                // Check if user has enough amount
                if (stack.getCount() >= amountNeeded) {
                    if (lvl < 10) {
                        stack.shrink(amountNeeded);
                        this.upgrade();
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.translatable("text.nexuscore.upgrade_success",
                                        (lvl + 1)),
                                true);
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    } else {
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.translatable("message.nexuscore.max_level"), true);
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                } else {
                    // Not enough items
                    player.displayClientMessage(net.minecraft.network.chat.Component
                            .translatable("text.nexuscore.upgrade_fail",
                                    amountNeeded + "x " + neededItem.getDescription().getString()),
                            true);
                    return net.minecraft.world.InteractionResult.FAIL;
                }
            } else if (!stack.isEmpty() && !(stack.getItem() instanceof net.minecraft.world.item.SwordItem)) {
                // Hint wrong item
                player.displayClientMessage(net.minecraft.network.chat.Component
                        .translatable("text.nexuscore.upgrade_fail",
                                amountNeeded + "x " + neededItem.getDescription().getString()),
                        true);
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
