package com.gtnewhorizons.angelica.mixins;

import com.gtnewhorizons.angelica.AngelicaMod;
import com.gtnewhorizons.angelica.config.AngelicaConfig;
import com.gtnewhorizons.angelica.config.CompatConfig;
import com.gtnewhorizons.angelica.loading.AngelicaTweaker;
import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum Mixins {
    ANGELICA(new Builder("Angelica").addTargetedMod(TargetedMod.VANILLA).setSide(Side.CLIENT)
        .setPhase(Phase.EARLY).addMixinClasses(
             "angelica.MixinActiveRenderInfo"
            ,"angelica.MixinEntityRenderer"
            ,"angelica.MixinGameSettings"
            ,"angelica.MixinMinecraft"
            ,"angelica.MixinMinecraftServer"
            ,"angelica.MixinFMLClientHandler"
        )
    ),

    ANGELICA_ENABLE_DEBUG(new Builder("Angelica Debug").addTargetedMod(TargetedMod.VANILLA).setSide(Side.CLIENT)
        .setPhase(Phase.EARLY).setApplyIf(() -> AngelicaMod.lwjglDebug).addMixinClasses(
             "angelica.debug.MixinProfiler"
            ,"angelica.debug.MixinSplashProgress"
            ,"angelica.debug.MixinTextureManager"
        )
    ),

    ANGELICA_FIX_FLUID_RENDERER_CHECKING_BLOCK_AGAIN(
        new Builder("Fix RenderBlockFluid reading the block type from the world access multiple times")
            .setPhase(Phase.EARLY).addMixinClasses("angelica.bugfixes.MixinRenderBlockFluid").setSide(Side.BOTH)
            .setApplyIf(() -> AngelicaConfig.fixFluidRendererCheckingBlockAgain)
            .addTargetedMod(TargetedMod.VANILLA)),

    ANGELICA_LIMIT_DROPPED_ITEM_ENTITIES(new Builder("Dynamically modifies the render distance of dropped items entities to preserve performance")
        .setPhase(Phase.EARLY).addMixinClasses("angelica.optimizations.MixinRenderGlobal_ItemRenderDist").setSide(Side.CLIENT)
        .setApplyIf(() -> AngelicaConfig.dynamicItemRenderDistance)
        .addTargetedMod(TargetedMod.VANILLA)),

    ANGELICA_TEXTURE(new Builder("Textures").addTargetedMod(TargetedMod.VANILLA).setSide(Side.CLIENT)
        .setPhase(Phase.EARLY).setApplyIf(() -> AngelicaConfig.enableIris || AngelicaConfig.enableSodium).addMixinClasses(
             "angelica.textures.MixinTextureAtlasSprite"
            ,"angelica.textures.MixinTextureUtil"
        )),

    HUD_CACHING(new Builder("Renders the HUD elements 20 times per second maximum to improve performance")
        .addTargetedMod(TargetedMod.VANILLA).setSide(Side.CLIENT).setPhase(Phase.EARLY)
        .setApplyIf(() -> AngelicaConfig.enableHudCaching).addMixinClasses(
        	"angelica.hudcaching.MixinGuiIngame",
        	"angelica.hudcaching.MixinGuiIngameForge",
            "angelica.hudcaching.MixinRenderGameOverlayEvent",
            "angelica.hudcaching.MixinEntityRenderer_HUDCaching",
            "angelica.hudcaching.MixinFramebuffer_HUDCaching",
            "angelica.hudcaching.MixinGuiIngame_HUDCaching",
            "angelica.hudcaching.MixinGuiIngameForge_HUDCaching",
            "angelica.hudcaching.MixinRenderItem")

    ),

    OPTIMIZE_WORLD_UPDATE_LIGHT(new Builder("Optimize world updateLightByType method").setPhase(Phase.EARLY)
        .setSide(Side.BOTH).addTargetedMod(TargetedMod.VANILLA).addExcludedMod(TargetedMod.ARCHAICFIX).setApplyIf(() -> AngelicaConfig.optimizeWorldUpdateLight)
        .addMixinClasses("angelica.lighting.MixinWorld_FixLightUpdateLag")),

    SCALED_RESOUTION_UNICODE_FIX(new Builder("Removes unicode languages gui scaling being forced to even values").setPhase(Phase.EARLY)
        .setSide(Side.CLIENT).addTargetedMod(TargetedMod.VANILLA)
        .setApplyIf(() -> AngelicaConfig.removeUnicodeEvenScaling)
        .addMixinClasses("angelica.bugfixes.MixinScaledResolution_UnicodeFix")),

    EXTRA_UTILITIES_THREAD_SAFETY(new Builder("Enable thread safety fixes in Extra Utilities").setPhase(Phase.LATE)
        .addTargetedMod(TargetedMod.EXTRAUTILS).setSide(Side.CLIENT)
        .setApplyIf(() -> CompatConfig.fixExtraUtils)
        .addMixinClasses(
            "client.extrautils.MixinRenderBlockConnectedTextures",
            "client.extrautils.MixinRenderBlockConnectedTexturesEthereal",
            "client.extrautils.MixinIconConnectedTexture")),

    MFR_THREAD_SAFETY(new Builder("Enable thread safety fixes for MineFactory Reloaded").setPhase(Phase.LATE)
            .addTargetedMod(TargetedMod.MINEFACTORY_RELOADED).setSide(Side.CLIENT)
            .setApplyIf(() -> CompatConfig.fixMinefactoryReloaded)
            .addMixinClasses("client.minefactoryreloaded.MixinRedNetCableRenderer")),

    SPEEDUP_CAMPFIRE_BACKPORT_ANIMATIONS(new Builder("Add animation speedup support to Campfire Backport").setPhase(Phase.LATE)
            .addTargetedMod(TargetedMod.CAMPFIRE_BACKPORT).setSide(Side.CLIENT)
            .setApplyIf(() -> AngelicaConfig.speedupAnimations)
            .addMixinClasses("client.campfirebackport.MixinRenderBlockCampfire")),

    IC2_FLUID_RENDER_FIX(new Builder("IC2 Fluid Render Fix").setPhase(Phase.EARLY).setSide(Side.CLIENT)
        .addTargetedMod(TargetedMod.IC2).setApplyIf(() -> AngelicaConfig.speedupAnimations)
        .addMixinClasses("angelica.textures.ic2.MixinRenderLiquidCell")),

    OPTIMIZE_TEXTURE_LOADING(new Builder("Optimize Texture Loading").setPhase(Phase.EARLY)
        .addMixinClasses("angelica.textures.MixinTextureUtil_OptimizeMipmap").addTargetedMod(TargetedMod.VANILLA)
        .setApplyIf(() -> AngelicaConfig.optimizeTextureLoading).setSide(Side.CLIENT)),

    QPR(new Builder("Adds a QuadProvider field to blocks without populating it")
        .setSide(Side.CLIENT)
        .setPhase(Phase.EARLY)
        .setApplyIf(() -> true)
        .addTargetedMod(TargetedMod.VANILLA)
        .addMixinClasses(
            "angelica.models.MixinBlock",
            "angelica.models.MixinBlockOldLeaf")),

    ;

    private final List<String> mixinClasses;
    private final Supplier<Boolean> applyIf;
    private final Phase phase;
    private final Side side;
    private final List<TargetedMod> targetedMods;
    private final List<TargetedMod> excludedMods;

    Mixins(Builder builder) {
        this.mixinClasses = builder.mixinClasses;
        this.applyIf = builder.applyIf;
        this.side = builder.side;
        this.targetedMods = builder.targetedMods;
        this.excludedMods = builder.excludedMods;
        this.phase = builder.phase;
        if (this.targetedMods.isEmpty()) {
            throw new RuntimeException("No targeted mods specified for " + this.name());
        }
        if (this.applyIf == null) {
            throw new RuntimeException("No ApplyIf function specified for " + this.name());
        }
    }

    public static List<String> getEarlyMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        final List<String> notLoading = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Mixins.Phase.EARLY) {
                if (mixin.shouldLoad(loadedCoreMods, Collections.emptySet())) {
                    mixins.addAll(mixin.mixinClasses);
                } else {
                    notLoading.addAll(mixin.mixinClasses);
                }
            }
        }
        AngelicaTweaker.LOGGER.info("Not loading the following EARLY mixins: {}", notLoading);
        return mixins;
    }

    public static List<String> getLateMixins(Set<String> loadedMods) {
        final List<String> mixins = new ArrayList<>();
        final List<String> notLoading = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Mixins.Phase.LATE) {
                if (mixin.shouldLoad(Collections.emptySet(), loadedMods)) {
                    mixins.addAll(mixin.mixinClasses);
                } else {
                    notLoading.addAll(mixin.mixinClasses);
                }
            }
        }
        AngelicaTweaker.LOGGER.info("Not loading the following LATE mixins: {}", notLoading.toString());
        return mixins;
    }

    private boolean shouldLoadSide() {
        return side == Side.BOTH || (side == Side.SERVER && FMLLaunchHandler.side().isServer())
                || (side == Side.CLIENT && FMLLaunchHandler.side().isClient());
    }

    private boolean allModsLoaded(List<TargetedMod> targetedMods, Set<String> loadedCoreMods, Set<String> loadedMods) {
        if (targetedMods.isEmpty()) return false;

        for (TargetedMod target : targetedMods) {
            if (target == TargetedMod.VANILLA) continue;

            // Check coremod first
            if (!loadedCoreMods.isEmpty() && target.coreModClass != null
                    && !loadedCoreMods.contains(target.coreModClass))
                return false;
            else if (!loadedMods.isEmpty() && target.modId != null && !loadedMods.contains(target.modId)) return false;
        }

        return true;
    }

    private boolean noModsLoaded(List<TargetedMod> targetedMods, Set<String> loadedCoreMods, Set<String> loadedMods) {
        if (targetedMods.isEmpty()) return true;

        for (TargetedMod target : targetedMods) {
            if (target == TargetedMod.VANILLA) continue;

            // Check coremod first
            if (!loadedCoreMods.isEmpty() && target.coreModClass != null
                    && loadedCoreMods.contains(target.coreModClass))
                return false;
            else if (!loadedMods.isEmpty() && target.modId != null && loadedMods.contains(target.modId)) return false;
        }

        return true;
    }

    private boolean shouldLoad(Set<String> loadedCoreMods, Set<String> loadedMods) {
        return (shouldLoadSide() && applyIf.get()
                && allModsLoaded(targetedMods, loadedCoreMods, loadedMods)
                && noModsLoaded(excludedMods, loadedCoreMods, loadedMods));
    }

    private static class Builder {

        private final List<String> mixinClasses = new ArrayList<>();
        private Supplier<Boolean> applyIf = () -> true;
        private Side side = Side.BOTH;
        private Phase phase = Phase.LATE;
        private final List<TargetedMod> targetedMods = new ArrayList<>();
        private final List<TargetedMod> excludedMods = new ArrayList<>();

        public Builder(@SuppressWarnings("unused") String description) {}

        public Builder addMixinClasses(String... mixinClasses) {
            this.mixinClasses.addAll(Arrays.asList(mixinClasses));
            return this;
        }

        public Builder setPhase(Phase phase) {
            this.phase = phase;
            return this;
        }

        public Builder setSide(Side side) {
            this.side = side;
            return this;
        }

        public Builder setApplyIf(Supplier<Boolean> applyIf) {
            this.applyIf = applyIf;
            return this;
        }

        public Builder addTargetedMod(TargetedMod mod) {
            this.targetedMods.add(mod);
            return this;
        }

        public Builder addExcludedMod(TargetedMod mod) {
            this.excludedMods.add(mod);
            return this;
        }
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    private static String[] addPrefix(String prefix, String... values) {
        return Arrays.stream(values)
            .map(s -> prefix + s)
            .collect(Collectors.toList())
            .toArray(new String[values.length]);
    }

    private enum Side {
        BOTH,
        CLIENT,
        SERVER
    }

    private enum Phase {
        EARLY,
        LATE,
    }
}
