/*
 * This file is part of sdlink-lib, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 - 2023 HypherionSA and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.hypherionmc.sdlinklib.utils;

import me.hypherionmc.sdlinklib.database.UserTable;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public class MinecraftPlayer {

    private final String username;
    private final UUID uuid;
    private final boolean isOffline;
    private final boolean isValid;

    private MinecraftPlayer(String username, UUID uuid, boolean isOffline, boolean isValid) {
        this.username = username;
        this.uuid = uuid;
        this.isOffline = isOffline;
        this.isValid = isValid;
    }

    public static MinecraftPlayer standard(String username) {
        Pair<String, String> player = fetchPlayer(username);
        return new MinecraftPlayer(player.getLeft(), player.getRight().isEmpty() ? null : mojangIdToUUID(player.getRight()), false, !player.getRight().isEmpty());
    }

    public static MinecraftPlayer offline(String username) {
        Pair<String, String> player = offlineUUID(username);
        return new MinecraftPlayer(player.getLeft(), mojangIdToUUID(player.getRight()), true, true);
    }

    public String getUsername() {
        return username;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public boolean isValid() {
        return isValid;
    }

    public UUID getUuid() {
        return uuid;
    }


    public Result linkAccount(String nickname, Member member) {
        UserTable userTable = new UserTable();

        userTable.username = this.username;
        userTable.UUID = this.uuid.toString();
        userTable.discordID = member.getIdLong();

        List<UserTable> tables = userTable.fetchAll("discordID = '" + member.getIdLong() + "'");
        if (tables.isEmpty()) {
            userTable.insert();
        } else {
            tables.forEach(UserTable::update);
        }

        String nickName = nickname;
        nickName = nickName + " [MC: " + this.username + "]";

        try {
            member.modifyNickname(nickName).queue();
        } catch (Exception e) {
            if (modConfig.generalConfig.debugging) {
                e.printStackTrace();
            }
        }

        return Result.success("Your Discord and MC accounts have been linked");
    }

    // Helper Methods
    private static Pair<String, String> fetchPlayer(String name) {
        try {
            BufferedReader read = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
            JSONObject obj = new JSONObject(new JSONTokener(read));
            String uuid = "";
            String returnname = name;

            if (!obj.getString("name").isEmpty()) {
                returnname = obj.getString("name");
            }
            if (!obj.getString("id").isEmpty()) {
                uuid = obj.getString("id");
            }

            read.close();
            return Pair.of(returnname, uuid);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return Pair.of("", "");
    }

    private static UUID mojangIdToUUID(String id) {
        final List<String> strings = new ArrayList<>();
        strings.add(id.substring(0, 8));
        strings.add(id.substring(8, 12));
        strings.add(id.substring(12, 16));
        strings.add(id.substring(16, 20));
        strings.add(id.substring(20, 32));

        return UUID.fromString(String.join("-", strings));
    }

    private static UUID offlineNameToUUID(String offlineName) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + offlineName).getBytes(StandardCharsets.UTF_8));
    }

    private static Pair<String, String> offlineUUID(String offlineName) {
        return Pair.of(offlineName, UUID.nameUUIDFromBytes(("OfflinePlayer:" + offlineName).getBytes(StandardCharsets.UTF_8)).toString().replace("-", ""));
    }
}
