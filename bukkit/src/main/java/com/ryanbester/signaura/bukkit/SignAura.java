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

import com.ryanbester.signaura.bukkit.compat.CommandManager;
import com.ryanbester.signaura.bukkit.config.MainConfig;
import com.ryanbester.signaura.bukkit.config.Messages;
import com.ryanbester.signaura.bukkit.database.AbstractDatabase;
import com.ryanbester.signaura.bukkit.database.FlatfileDatabase;
import com.ryanbester.signaura.bukkit.database.SqliteDatabase;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Locale;

public class SignAura extends JavaPlugin {

    public static boolean commandAPIInstalled = true;
    public static boolean floodgateInstalled = false;
    public static AbstractDatabase database;

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        Plugin commandAPIPlugin = getServer().getPluginManager().getPlugin("CommandAPI");
        if (commandAPIPlugin == null) {
            getLogger().info("CommandAPI is recommended when using this plugin.");
            getLogger().warning("CommandAPI must be installed to use the /signaura edit command.");
            commandAPIInstalled = false;
        }

        if (getServer().getPluginManager().getPlugin("floodgate") != null) {
            floodgateInstalled = true;
        }

        if (!loadConfig()) {
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSessionListener(this), this);

        registerCommands();
    }

    public boolean loadConfig() {
        try {
            MainConfig.getInstance().loadConfig(this, true);
            Messages.getInstance().loadConfig(this, true);
        } catch (IOException | InvalidConfigurationException e) {
            if (MainConfig.logErrors) e.printStackTrace();
            return false;
        }

        try {
            // Init database
            switch (MainConfig.DatabaseConfig.type.toLowerCase(Locale.ENGLISH)) {
                case "flatfile":
                    database = new FlatfileDatabase(this);
                    break;
                case "sqlite":
                    database = new SqliteDatabase(this);
                    break;
                default:
                    throw new Exception("Unknown database type.");
            }
            database.initDatabase();
        } catch (Exception e) {
            if (MainConfig.logErrors) e.printStackTrace();
            return false;
        }
        return true;
    }

    public void registerCommands() {
        SignAuraCommand.plugin = this;

        if (commandAPIInstalled) {
            CommandTree cmd = new CommandTree("signaura")
                    .withPermission("signaura")
                    .withHelp("SignAura", "SignAura related commands")
                    .then(new LiteralArgument("edit")
                            .withPermission("signaura.edit")
                            .then(new LocationArgument("position", LocationType.BLOCK_POSITION)
                                    .then(new TextArgument("line1")
                                            .then(new TextArgument("line2")
                                                    .then(new TextArgument("line3")
                                                            .then(new TextArgument("line4")
                                                                    .executes(SignAuraCommand::executeEdit)))))))
                    .then(new LiteralArgument("getline")
                            .withPermission("signaura.getline")
                            .then(new LocationArgument("position", LocationType.BLOCK_POSITION)
                                    .then(new IntegerArgument("line", 1, 4)
                                            .executes(SignAuraCommand::executeGetLine))))
                    .then(new LiteralArgument("getowner")
                            .withPermission("signaura.getowner")
                            .then(new LocationArgument("position", LocationType.BLOCK_POSITION)
                                    .executes(SignAuraCommand::executeGetOwner)))
                    .then(new LiteralArgument("help")
                            .withPermission("signaura.help")
                            .executes(SignAuraCommand::executeHelp))
                    .then(new LiteralArgument("reload")
                            .withPermission("signaura.reload")
                            .executes(SignAuraCommand::executeReload))
                    .then(new LiteralArgument("takeownership")
                            .withPermission("signaura.takeownership")
                            .then(new LocationArgument("position", LocationType.BLOCK_POSITION)
                                    .executes(SignAuraCommand::executeTakeOwnership)));
            cmd.register();
        } else {
            CommandManager commandManager = CompatManager.getCommandManager();
            if (commandManager == null) {
                getLogger().warning("Failed to register command.");
                return;
            }

            commandManager.addCommand("signaura", new SignAuraCommand());
        }
    }


    @Override
    public void onDisable() {
        try {
            if (database != null) database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
