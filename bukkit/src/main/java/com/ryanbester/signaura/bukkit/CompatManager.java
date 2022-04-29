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

import com.ryanbester.signaura.bukkit.compat.ActionBar;
import com.ryanbester.signaura.bukkit.compat.CommandManager;
import com.ryanbester.signaura.bukkit.compat.PacketHandler;
import com.ryanbester.signaura.bukkit.compat.SignEditor;
import com.ryanbester.signaura.bukkit.compat.v1_16_R3.ActionBar_1_16_R3;
import com.ryanbester.signaura.bukkit.compat.v1_16_R3.PacketHandler_1_16_R3;
import com.ryanbester.signaura.bukkit.compat.v1_16_R3.SignEditor_1_16_R3;
import com.ryanbester.signaura.bukkit.compat.v1_18_R1.ActionBar_1_18_R1;
import com.ryanbester.signaura.bukkit.compat.v1_18_R1.CommandManager_1_18_R1;
import com.ryanbester.signaura.bukkit.compat.v1_18_R1.PacketHandler_1_18_R1;
import com.ryanbester.signaura.bukkit.compat.v1_18_R1.SignEditor_1_18_R1;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CompatManager {
    public static PacketHandler packetHandler = null;
    public static ActionBar actionBar = null;
    public static SignEditor signEditor = null;
    public static CommandManager commandManager = null;

    public static PacketHandler getPacketHandler() {
        if (packetHandler != null) {
            return packetHandler;
        }

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        if (version.equals("v1_16_R3")) {
            packetHandler = new PacketHandler_1_16_R3();
        }

        if (version.equals("v1_18_R1")) {
            packetHandler = new PacketHandler_1_18_R1();
        }

        return packetHandler;
    }


    public static ActionBar getActionBar() {
        if (actionBar != null) {
            return actionBar;
        }

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        if (version.equals("v1_16_R3")) {
            actionBar = new ActionBar_1_16_R3();
        }

        if (version.equals("v1_18_R1")) {
            actionBar = new ActionBar_1_18_R1();
        }

        return actionBar;
    }

    public static SignEditor getSignEditor() {
        if (signEditor != null) {
            return signEditor;
        }

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        if (version.equals("v1_16_R3")) {
            signEditor = new SignEditor_1_16_R3();
        }

        if (version.equals("v1_18_R1")) {
            signEditor = new SignEditor_1_18_R1();
        }

        return signEditor;
    }

    public static CommandManager getCommandManager() {
        if (commandManager != null) {
            return commandManager;
        }

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        if (version.equals("v1_18_R1")) {
            commandManager = new CommandManager_1_18_R1();
        }

        return commandManager;
    }

    public static void sendActionBar(Plugin plugin, Player player, String message) {
        ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            player.sendMessage(message);
            return;
        }

        actionBar.sendActionBar(player, message, plugin);
    }
}
