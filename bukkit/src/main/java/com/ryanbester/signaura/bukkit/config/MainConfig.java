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
import org.bukkit.plugin.Plugin;

import java.io.IOException;

public class MainConfig extends Config {
    private static MainConfig instance;

    public static boolean onlyOwnersCanEdit;
    public static boolean parseFormattingCodes;
    public static boolean allowBedrockEditing;
    public static boolean logErrors;

    public static MainConfig getInstance() {
        if (instance == null) {
            instance = new MainConfig();
        }
        return instance;
    }

    public MainConfig() {
        super("config.yml");
    }

    @Override
    public void loadConfig(Plugin plugin, boolean copyFromResources) throws IOException, InvalidConfigurationException {
        super.loadConfig(plugin, copyFromResources);

        onlyOwnersCanEdit = config.getBoolean("only-owners-can-edit", true);
        parseFormattingCodes = config.getBoolean("parse-formatting-codes", false);
        allowBedrockEditing = config.getBoolean("allow-bedrock-editing", true);
        logErrors = config.getBoolean("log-errors", true);

        DatabaseConfig.type = config.getString("database.type", "flatfile");
        DatabaseConfig.filename = config.getString("database.filename", "signowners.txt");
    }

    public static class DatabaseConfig {
        public static String type;
        public static String filename;
    }
}
