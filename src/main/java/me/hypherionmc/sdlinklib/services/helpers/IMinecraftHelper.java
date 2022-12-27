package me.hypherionmc.sdlinklib.services.helpers;

import java.util.List;
import java.util.UUID;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
public interface IMinecraftHelper {

    public void discordMessageEvent(String username, String message);

    public boolean isWhitelistingEnabled();

    public boolean isPlayerWhitelisted(String name, UUID uuid);

    public boolean whitelistPlayer(String name, UUID uuid);

    public boolean unWhitelistPlayer(String name, UUID uuid);

    public List<String> getWhitelistedPlayers();

    public int getOnlinePlayerCount();

    public int getMaxPlayerCount();

    public List<String> getOnlinePlayerNames();

    public long getServerUptime();

    public String getServerVersion();

    public void executeMcCommand(String command, String args);

}
