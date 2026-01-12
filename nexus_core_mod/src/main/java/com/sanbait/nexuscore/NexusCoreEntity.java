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
        implements GeoEntity, com.sanbait.luxsystem.capabilities.ILuxStorage, net.minecraft.world.MenuProvider {
    private static final EntityDataAccessor<Integer> CURRENT_LEVEL = SynchedEntityData.defineId(NexusCoreEntity.class,
            EntityDataSerializers.INT);
    // NEW: Sync Current Lux to Client for Rendering
    private static final EntityDataAccessor<Integer> CURRENT_LUX = SynchedEntityData.defineId(NexusCoreEntity.class,
            EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final net.minecraftforge.items.ItemStackHandler upgradeInventory = new net.minecraftforge.items.ItemStackHandler(
            1);

    public NexusCoreEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noCulling = true; // Disable culling standard way
        this.noCulling = true;
        this.setNoAi(true); // Disable AI for the Core itself to save performance
        this.setPersistenceRequired(); // Mark as persistent so it doesn't despawn
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Never despawn due to distance
    }

    @Override
    public void checkDespawn() {
        // Do nothing. Prevent vanilla despawn logic.
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

            if (this.tickCount % 5 == 0) {
                applyBuffs();
                chargeNearbyItems();
                checkUpgrade(); // Check for auto-upgrade items
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

    // Helper to get just the AMOUNT needed for upgrade
    public static int getUpgradeCostAmount(int currentLvl) {
        java.util.List<? extends String> costs = NexusCoreConfig.UPGRADE_COSTS.get();
        if (costs.isEmpty())
            return 999;

        int costIndex = Math.min(currentLvl - 1, costs.size() - 1);
        String costStr = costs.get(costIndex);
        String[] parts = costStr.split("\\|");
        if (parts.length != 2)
            return 999;

        try {
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 999;
        }
    }

    // Helper to get just the ITEM needed for upgrade
    public static net.minecraft.world.item.Item getUpgradeCostItem(int currentLvl) {
        java.util.List<? extends String> costs = NexusCoreConfig.UPGRADE_COSTS.get();
        if (costs.isEmpty())
            return net.minecraft.world.item.Items.AIR;

        int costIndex = Math.min(currentLvl - 1, costs.size() - 1);
        String costStr = costs.get(costIndex);
        String[] parts = costStr.split("\\|");
        net.minecraft.resources.ResourceLocation loc = new net.minecraft.resources.ResourceLocation(parts[0]);
        net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(loc);
        return item != null ? item : net.minecraft.world.item.Items.AIR;
    }

    public static String getNextUpgradeCost(int currentLvl) {
        net.minecraft.world.item.Item item = getUpgradeCostItem(currentLvl);
        int amount = getUpgradeCostAmount(currentLvl);
        if (item == null || item == net.minecraft.world.item.Items.AIR)
            return "Max Level / Error";
        return amount + "x " + item.getDescription().getString();
    }

    private void checkUpgrade() {
        if (this.level().isClientSide)
            return;

        int currentLvl = this.getCurrentLevel();
        if (currentLvl >= 10)
            return;

        net.minecraft.world.item.Item requiredItem = getUpgradeCostItem(currentLvl);
        int requiredAmount = getUpgradeCostAmount(currentLvl);

        net.minecraft.world.item.ItemStack inputStack = this.upgradeInventory.getStackInSlot(0);

        if (!inputStack.isEmpty() && inputStack.is(requiredItem) && inputStack.getCount() >= requiredAmount) {
            // Visualize upgrade
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F,
                    1.0F);
            // Spawn particles
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, this.getX(),
                        this.getY() + 1.5, this.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
            }

            // Actually consume item
            inputStack.shrink(requiredAmount);
            this.upgrade();
        }
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
                // Runs every 5 ticks (0.25 sec).
                // 20 Lux per pulse => 80 Lux/sec
                int transfer = 20;

                // Cap by Core storage
                int actualTransfer = Math.min(transfer, getCurrentLux());
                // Cap by Item space
                actualTransfer = Math.min(actualTransfer, max - current);

                if (actualTransfer > 0) {
                    cap.receiveLux(actualTransfer, false);
                    extractLux(actualTransfer, false); // Drain from core

                    // SYNC FIX: Mirror to NBT to force Client Sync
                    // Changing NBT makes the ItemStack "dirty", triggering a packet.
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putInt("LuxStored", cap.getLuxStored());
                    tag.putInt("LuxMax", max); // Sync Max as well for optimized rendering
                }
            } else {
                // Even if full, sync if tag is missing (e.g. freshly crafted or spawned item)
                if (!stack.hasTag() || !stack.getTag().contains("LuxMax")) {
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putInt("LuxStored", current);
                    tag.putInt("LuxMax", max);
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

        net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
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
        });
    }

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance pEffect) {
        if (pEffect.getEffect() == net.minecraft.world.effect.MobEffects.INVISIBILITY) {
            return false;
        }
        return super.canBeAffected(pEffect);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.getEntity() instanceof net.minecraft.world.entity.player.Player player && player.isCreative()) {
            this.setHealth(0);
            this.die(source);
            return true;
        }

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
        if (source.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            // Allow Creative players to break it
            return !player.isCreative();
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
    public boolean shouldRenderAtSqrDistance(double distance) {
        // Always render, no matter the distance.
        // The beacon beam needs to be visible from across the map.
        return true;
    }

    /**
     * Fixes "disappearing" when looking away.
     * Returns a LARGE but FINITE bounding box (64 blocks radius).
     * Infinite AABB can cause issues with Sodium/Embeddium culling calculations.
     */
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        // Return a very large finite bounding box. Infinite values can cause issues
        // with culling mods.
        return new net.minecraft.world.phys.AABB(
                this.getX() - 30000, this.getY() - 30000, this.getZ() - 30000,
                this.getX() + 30000, this.getY() + 30000, this.getZ() + 30000);
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
            // Open GUI - Use NetworkHooks to open screen on server side
            net.minecraft.server.level.ServerPlayer serverPlayer = (net.minecraft.server.level.ServerPlayer) player;
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer, this, buf -> buf.writeInt(this.getId()));
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return net.minecraft.world.InteractionResult.CONSUME;
    }

    public int getRadius() {
        return (int) (this.getCurrentLevel() * NexusCoreConfig.RADIUS_PER_LEVEL.get());
    }

    // MenuProvider implementation
    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("container.nexuscore.core");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId,
            net.minecraft.world.entity.player.Inventory playerInventory,
            net.minecraft.world.entity.player.Player player) {
        return new com.sanbait.nexuscore.gui.NexusCoreMenu(containerId, playerInventory, this);
    }

    public net.minecraftforge.items.ItemStackHandler getUpgradeInventory() {
        return this.upgradeInventory;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
