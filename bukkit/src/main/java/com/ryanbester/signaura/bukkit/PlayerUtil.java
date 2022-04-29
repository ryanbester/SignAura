/*
 * MIT License
 *
 * Copyright (c) 2022 Ryan Bester
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
 *
 * @author Ryan Bester
 * @link https://github.com/ryanbester/SignAura
 */

package com.ryanbester.signaura.bukkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class PlayerUtil {

    public static String getUUID(String username) throws Exception {
        JsonElement json = getJson("https://api.mojang.com/users/profiles/minecraft/" + username);

        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();

            JsonElement id = jsonObject.get("id");
            if (id.isJsonPrimitive()) {
                return id.getAsJsonPrimitive().getAsString();
            }
        }

        throw new Exception("Invalid UUID");
    }

    public static String getUsername(UUID uuid) throws Exception {
        JsonElement json = getJson("https://api.mojang.com/user/profiles/" + uuid.toString() + "/names");

        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();

            JsonElement nameObj = jsonArray.get(jsonArray.size() - 1);
            if (nameObj.isJsonObject()) {
                JsonElement name = nameObj.getAsJsonObject().get("name");
                if (name.isJsonPrimitive()) {
                    return name.getAsString();
                }
            }
        }

        return null;
    }

    public static String getUsernameOnlineCheck(Plugin plugin, UUID uuid) throws Exception {
        Player p = plugin.getServer().getPlayer(uuid);
        if (p != null) {
            return p.getName();
        } else {
            return getUsername(uuid);
        }
    }

    private static JsonElement getJson(String url) throws Exception {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new Exception("Response not 200");
        }

        return JsonParser.parseReader(new InputStreamReader(conn.getInputStream()));
    }
}
