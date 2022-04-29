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

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public abstract class Config {

    protected String filename;

    public Config(String filename) {
        this.filename = filename;
    }

    protected static FileConfiguration config;

    public void loadConfig(Plugin plugin, boolean copyFromResources)
            throws IOException, InvalidConfigurationException {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File configFile = new File(plugin.getDataFolder(), this.filename);
        if (!configFile.exists() && copyFromResources) {
            plugin.saveResource(this.filename, false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig(Plugin plugin)
            throws IOException, InvalidConfigurationException {

        File configFile = new File(plugin.getDataFolder(), this.filename);
        config.save(configFile);
    }
}
