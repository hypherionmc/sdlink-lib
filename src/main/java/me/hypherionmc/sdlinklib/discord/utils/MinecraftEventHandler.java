package me.hypherionmc.sdlinklib.discord.utils;

import java.util.List;
import java.util.UUID;

public interface MinecraftEventHandler {

    public void discordMessageReceived(String username, String message);
    public int getPlayerCount();
    public int getMaxPlayerCount();
    public List<String> getOnlinePlayers();
    public long getServerUptime();
    public float getTPS();
    public String getServerVersion();
    public void sendStopCommand();
}
