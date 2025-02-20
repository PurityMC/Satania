package com.gtnewhorizons.angelica.loading;

import com.gtnewhorizons.angelica.config.AngelicaConfig;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.platform.MixinContainer;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.transformer.Config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gtnewhorizons.angelica.loading.AngelicaTweaker.LOGGER;

public class MixinCompatHackTweaker implements ITweaker {
    public static final boolean DISABLE_OPTIFINE_FASTCRAFT_BETTERFPS = true;
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        verifyDependencies();

        if(DISABLE_OPTIFINE_FASTCRAFT_BETTERFPS) {
            LOGGER.info("Disabling BetterFPS, and other incompatible mods (if present)");
            disableIncompatibleMods();
        }

        if (AngelicaConfig.enableHudCaching){
            disableXaerosMinimapWaypointTransformer();
        }
    }

    private void verifyDependencies() {
        if(MixinCompatHackTweaker.class.getResource("/it/unimi/dsi/fastutil/ints/Int2ObjectMap.class") == null) {
            throw new RuntimeException("Missing dependency: Angelica requires GTNHLib 0.2.1 or newer! Download: https://modrinth.com/mod/gtnhlib");
        }
    }

    private void disableXaerosMinimapWaypointTransformer(){
        try {
            final LaunchClassLoader lcl = Launch.classLoader;
            final Field xformersField = lcl.getClass().getDeclaredField("transformers");
            xformersField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<IClassTransformer> xformers = (List<IClassTransformer>) xformersField.get(lcl);
            for (int idx = xformers.size() - 1; idx >= 0; idx--) {
                final String name = xformers.get(idx).getClass().getName();
                if (name.startsWith("xaero.common.core.transformer.GuiIngameForgeTransformer")) {
                    LOGGER.info("Removing transformer " + name);
                    xformers.remove(idx);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void disableIncompatibleMods() {
        // Remove transformers, Mod Containers, and mixins for Optifine, BetterFPS and other incompatible mods
        try {
            final LaunchClassLoader lcl = Launch.classLoader;
            final Field xformersField = lcl.getClass().getDeclaredField("transformers");
            xformersField.setAccessible(true);
            final List<IClassTransformer> xformers = (List<IClassTransformer>) xformersField.get(lcl);
            for (int idx = xformers.size() - 1; idx >= 0; idx--) {
                final String name = xformers.get(idx).getClass().getName();
                if (name.startsWith("me.guichaguri.betterfps")) {
                    LOGGER.info("Removing transformer " + name);
                    xformers.remove(idx);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            final Field reparsedCoremodsField = CoreModManager.class.getDeclaredField("reparsedCoremods");
            final Field loadedCoremodsField = CoreModManager.class.getDeclaredField("loadedCoremods");
            reparsedCoremodsField.setAccessible(true);
            loadedCoremodsField.setAccessible(true);
            final ArrayList<String> reparsedCoremods = (ArrayList<String>) reparsedCoremodsField.get(CoreModManager.class);
            final ArrayList<String> loadedCoremods = (ArrayList<String>) loadedCoremodsField.get(CoreModManager.class);
            for (int idx = reparsedCoremods.size() - 1; idx >= 0; idx--) {
                final String coreMod = reparsedCoremods.get(idx);
                if (coreMod.startsWith("optimizationsandtweaks")) {
                    LOGGER.info("Removing reparsed coremod " + coreMod);
                    // Fool the CoreModManager into not checking for a mod container again later
                    loadedCoremods.add(reparsedCoremods.remove(idx));

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            final ArrayList<String> mixinConfigsDefault = (ArrayList<String>) Launch.blackboard.get("mixin.configs.default");
            if (mixinConfigsDefault != null) {
                for (int idx = mixinConfigsDefault.size() - 1; idx >= 0; idx--) {
                    final String name = mixinConfigsDefault.get(idx);
                    if (name != null && name.contains("optimizationsandtweaks")) {
                        LOGGER.info("Removing mixin config " + name);
                        mixinConfigsDefault.remove(idx);
                    }
                }
            }
            final Set<Config> mixinConfigs = (Set<Config>) Launch.blackboard.get("mixin.configs.queue");
            final Set<Config> toRemove = new HashSet<>();
            if (mixinConfigs != null) {
                for (Config config : mixinConfigs) {
                    final String name = config.getName();
                    if (name != null && name.contains("optimizationsandtweaks")) {
                        LOGGER.info("Removing queued mixin config " + config.getName());
                        toRemove.add(config);
                    }
                }
                mixinConfigs.removeAll(toRemove);
            }
            final MixinPlatformManager platformManager = (MixinPlatformManager) Launch.blackboard.get("mixin.platform");
            if (platformManager != null) {
                final Field containersField = platformManager.getClass().getDeclaredField("containers");
                containersField.setAccessible(true);
                final Map<IContainerHandle, MixinContainer> containers = (Map<IContainerHandle, MixinContainer>) containersField.get(platformManager);
                for (Map.Entry<IContainerHandle, MixinContainer> entry : containers.entrySet()) {
                    final String attribute = entry.getKey().getAttribute("MixinConfigs");
                    if(attribute != null && attribute.contains("optimizationsandtweaks")) {
                        LOGGER.info("Removing mixin container " + entry.getKey().toString());
                        containers.remove(entry.getKey());
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        // no-op
    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
