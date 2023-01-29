package me.hypherionmc.sdlinklib.services.helpers;

import me.hypherionmc.sdlinklib.utils.MinecraftPlayer;

import java.util.List;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
public interface IMinecraftHelper {

    public void discordMessageEvent(String username, String message);

    public boolean isWhitelistingEnabled();

    public boolean isPlayerWhitelisted(MinecraftPlayer player);

    public boolean whitelistPlayer(MinecraftPlayer player);

    public boolean unWhitelistPlayer(MinecraftPlayer player);

    public List<String> getWhitelistedPlayers();

    public int getOnlinePlayerCount();

    public int getMaxPlayerCount();

    public List<String> getOnlinePlayerNames();

    public long getServerUptime();

    public String getServerVersion();

    public void executeMcCommand(String command, String args);

}
