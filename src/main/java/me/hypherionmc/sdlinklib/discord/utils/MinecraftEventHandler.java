package me.hypherionmc.sdlinklib.discord.utils;

import java.util.List;
import java.util.UUID;

public interface MinecraftEventHandler {

    public void discordMessageReceived(String username, String message);
    public boolean whiteListingEnabled();
    public String whitelistPlayer(String username, UUID uuid);
    public String unWhitelistPlayer(String username, UUID uuid);
    public List<String> getWhitelistedPlayers();
    public int getPlayerCount();
    public int getMaxPlayerCount();
    public List<String> getOnlinePlayers();

}
