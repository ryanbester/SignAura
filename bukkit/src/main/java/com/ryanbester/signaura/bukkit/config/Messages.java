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

package com.ryanbester.signaura.bukkit.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Messages extends Config {

    private static Messages instance;

    private static final HashMap<String, String> messages = new HashMap<>();

    public static Messages getInstance() {
        if (instance == null) {
            instance = new Messages();
        }
        return instance;
    }

    public Messages() {
        super("messages.yml");
    }

    @Override
    public void loadConfig(Plugin plugin, boolean copyFromResources) throws IOException, InvalidConfigurationException {
        super.loadConfig(plugin, copyFromResources);

        messages.clear();

        for (Map.Entry<String, Object> message : config.getValues(false).entrySet()) {
            messages.put(message.getKey(), message.getValue().toString());
        }

        // Check for absent messages
        try (InputStream in = getClass().getResourceAsStream("/messages.yml")) {
            if (in != null) {
                FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                int count = 0;
                for (Map.Entry<String, Object> message : newConfig.getValues(false).entrySet()) {
                    if (messages.get(message.getKey()) == null) {
                        messages.put(message.getKey(), message.getValue().toString());
                        count++;
                    }
                }

                if (count > 0) {
                    plugin.getLogger().info("Loaded " + count + " new messages from plugin resources.");
                }
            }
        }
    }

    public static String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', messages.getOrDefault(key, key));
    }
}
