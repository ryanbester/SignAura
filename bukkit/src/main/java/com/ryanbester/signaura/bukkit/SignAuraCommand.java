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

import com.ryanbester.signaura.bukkit.compat.SignEditor;
import com.ryanbester.signaura.bukkit.config.MainConfig;
import com.ryanbester.signaura.bukkit.config.Messages;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class SignAuraCommand extends BukkitCommand {
    public static Plugin plugin;

    protected SignAuraCommand() {
        super("signaura");
        setPermission("signaura");
    }

    public static int executeHelp(CommandSender sender, Object args[]) {
        sender.sendMessage("SignAura Help");
        sender.sendMessage(ChatColor.GOLD + "/signaura edit: " + ChatColor.RESET + "Edits the text of a sign");
        sender.sendMessage(ChatColor.GOLD + "/signaura getline: " + ChatColor.RESET + "Gets a specific line of text from a sign");
        sender.sendMessage(ChatColor.GOLD + "/signaura getowner: " + ChatColor.RESET + "Gets the owner of a sign");
        sender.sendMessage(ChatColor.GOLD + "/signaura help: " + ChatColor.RESET + "Shows this help message");
        sender.sendMessage(ChatColor.GOLD + "/signaura reload: " + ChatColor.RESET + "Reloads the SignAura configuration");
        sender.sendMessage(ChatColor.GOLD + "/signaura takeownership: " + ChatColor.RESET + "Takes ownership of the specified sign");

        return 1;
    }

    public static int executeReload(CommandSender sender, Object args[]) {
        // Close database
        try {
            SignAura.database.close();
        } catch (Exception e) {
            if (MainConfig.logErrors) e.printStackTrace();
            sender.sendMessage(Messages.getMessage("command-reload-failed"));
            return 0;
        }

        // Load new configuration
        if (!((SignAura) plugin).loadConfig()) {
            sender.sendMessage(Messages.getMessage("command-reload-failed"));
            return 0;
        }

        sender.sendMessage(Messages.getMessage("command-reload-success"));
        return 1;
    }

    public static int executeEdit(CommandSender sender, Object args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.getMessage("only-in-game"));
            return 0;
        }

        Player p = (Player) sender;

        try {
            Location loc = (Location) args[0];
            Block block = ((Player) sender).getWorld().getBlockAt(loc);

            if (!block.getType().toString().endsWith("_SIGN")) {
                sender.sendMessage(Messages.getMessage("command-edit-not-sign"));
                return 0;
            }

            UUID owner = SignAura.database.getSignOwner(loc);
            if (MainConfig.onlyOwnersCanEdit && owner != null && !owner.equals(p.getUniqueId())) {
                if (!p.hasPermission("signaura.edit.all")) {
                    sender.sendMessage(Messages.getMessage("sign-wrong-owner"));
                    return 0;
                }
            }

            SignEditor signEditor = CompatManager.getSignEditor();
            if (signEditor == null) {
                sender.sendMessage(Messages.getMessage("command-edit-failed"));
                return 0;
            }

            for (int i = 1; i < 5; i++) {
                if (args[i].toString().length() > 15) {
                    sender.sendMessage(Messages.getMessage("command-edit-too-long"));
                    return 0;
                }
            }

            signEditor.trackSignLocation(p, plugin, loc);
            signEditor.updateSignContent(p, plugin, loc,
                    new String[]{args[1].toString(), args[2].toString(), args[3].toString(), args[4].toString()},
                    MainConfig.parseFormattingCodes);
        } catch (Exception ex) {
            if (MainConfig.logErrors) ex.printStackTrace();
            sender.sendMessage(Messages.getMessage("command-edit-failed"));
            return 0;
        }

        sender.sendMessage(Messages.getMessage("command-edit-success"));
        return 1;
    }

    public static int executeTakeOwnership(CommandSender sender, Object args[]) {
        return 1;
    }

    public static int executeGetOwner(CommandSender sender, Object args[]) {
        Location loc = (Location) args[0];
        Block block = ((Player) sender).getWorld().getBlockAt(loc);

        if (!block.getType().toString().endsWith("_SIGN")) {
            sender.sendMessage(ChatColor.RED + "Block is not a sign");
            return 0;
        }

        try {
            UUID uuid = SignAura.database.getSignOwner(loc);
            plugin.getLogger().info(uuid.toString());
            String playerName = PlayerUtil.getUsernameOnlineCheck(plugin, uuid);
            // TODO: Support bedrock players

            TextComponent message = new TextComponent(playerName);
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new Entity("player", uuid.toString(), new TextComponent(playerName))));

            sender.spigot().sendMessage(new ComponentBuilder("Sign is owned by: ").append(message).create());
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Error getting sign owner");
            ex.printStackTrace();
        }

        return 1;
    }

    public static int executeGetLine(CommandSender sender, Object args[]) {
        return 1;
    }


    @Override
    public boolean execute(CommandSender sender, String name, String[] args) {
        if (!sender.hasPermission("signaura")) {
            sender.sendMessage(Messages.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            executeHelp(sender, args);
            return true;
        }

        switch (args[0]) {
            case "edit":
                if (!sender.hasPermission("signaura.edit")) {
                    sender.sendMessage(Messages.getMessage("no-permission"));
                    return true;
                }

                executeEdit(sender, args);
                return true;
            case "help":
                if (!sender.hasPermission("signaura.help")) {
                    sender.sendMessage(Messages.getMessage("no-permission"));
                    return true;
                }

                executeHelp(sender, args);
                return true;
            case "reload":
                if (!sender.hasPermission("signaura.reload")) {
                    sender.sendMessage(Messages.getMessage("no-permission"));
                    return true;
                }

                executeReload(sender, args);
                return true;
            case "takeownership":
                if (!sender.hasPermission("signaura.takeownership")) {
                    sender.sendMessage(Messages.getMessage("no-permission"));
                    return true;
                }

                executeTakeOwnership(sender, args);
                return true;
        }

        return true;
    }

}
