package me.hypherionmc.sdlinklib.utils;

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

public class PlayerUtils {

    public static Pair<String, String> fetchUUID(String name) {
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

    public static UUID mojangIdToUUID(String id) {
        final List<String> strings = new ArrayList<>();
        strings.add(id.substring(0, 8));
        strings.add(id.substring(8, 12));
        strings.add(id.substring(12, 16));
        strings.add(id.substring(16, 20));
        strings.add(id.substring(20, 32));

        return UUID.fromString(String.join("-", strings));
    }

    public static UUID offlineNameToUUID(String offlineName) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + offlineName).getBytes(StandardCharsets.UTF_8));
    }

    public static Pair<String, String> offlineUUID(String offlineName) {
        return Pair.of(offlineName, UUID.nameUUIDFromBytes(("OfflinePlayer:" + offlineName).getBytes(StandardCharsets.UTF_8)).toString().replace("-", ""));
    }
}
