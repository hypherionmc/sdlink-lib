package me.hypherionmc.sdlinklib.services;

import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;

import java.util.ServiceLoader;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
public class PlatformServices {

    public static final IMinecraftHelper mc = load(IMinecraftHelper.class);

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz).iterator().next();
    }
}
