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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class PlayerInteractListener implements Listener {

    private final JavaPlugin plugin;

    public PlayerInteractListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null) {
                return;
            }


            if (isSign(clickedBlock.getType())) {
                try {
                    UUID owner = SignAura.database.getSignOwner(clickedBlock.getLocation());
                    if (MainConfig.onlyOwnersCanEdit && owner != null && !owner.equals(p.getUniqueId())) {
                        if (!p.hasPermission("signaura.edit.all")) {
                            CompatManager.sendActionBar(plugin, p, Messages.getMessage("sign-wrong-owner"));
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                ItemStack itemInHand = event.getItem();
                if (itemInHand != null) {
                    if (itemInHand.getType().toString().endsWith("_DYE")) {
                        return;
                    }

                    try {
                        MinecraftVersion mcVersion = new MinecraftVersion(Bukkit.getVersion());

                        if (mcVersion.atLeast("1.17") && itemInHand.getType() == Material.GLOW_INK_SAC) {
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!p.hasPermission("signaura.edit")) {
                    CompatManager.sendActionBar(plugin, p, Messages.getMessage("sign-no-permission"));
                    return;
                }

                // Bedrock support
                if (SignAura.floodgateInstalled && FloodgateApi.getInstance().isFloodgatePlayer(p.getUniqueId())) {
                    if (!MainConfig.allowBedrockEditing) {
                        CompatManager.sendActionBar(plugin, p, Messages.getMessage("sign-bedrock-unsupported"));
                        return;
                    }

                    BedrockSignEditor.openSignEditor(plugin, p, clickedBlock.getLocation());
                    return;
                }

                SignEditor signEditor = CompatManager.getSignEditor();
                if (signEditor != null) {
                    try {
                        signEditor.openSignEditor(p, this.plugin, clickedBlock.getLocation(), MainConfig.parseFormattingCodes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) throws Exception {
        if (isSign(event.getBlock().getType())) {
            SignAura.database.setSignOwner(event.getBlock().getLocation(), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) throws Exception {
        if (isSign(event.getBlock().getType())) {
            SignAura.database.removeSignOwner(event.getBlock().getLocation());
        }
    }

    private boolean isSign(Material mat) {
        return mat.equals(Material.OAK_SIGN)
                || mat.equals(Material.OAK_WALL_SIGN)
                || mat.equals(Material.DARK_OAK_SIGN)
                || mat.equals(Material.DARK_OAK_WALL_SIGN)
                || mat.equals(Material.BIRCH_SIGN)
                || mat.equals(Material.BIRCH_WALL_SIGN)
                || mat.equals(Material.ACACIA_SIGN)
                || mat.equals(Material.ACACIA_WALL_SIGN)
                || mat.equals(Material.SPRUCE_SIGN)
                || mat.equals(Material.SPRUCE_WALL_SIGN)
                || mat.equals(Material.JUNGLE_SIGN)
                || mat.equals(Material.JUNGLE_WALL_SIGN);
    }
}
