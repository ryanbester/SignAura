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

package com.ryanbester.signaura.bukkit.compat.v1_16_R3;

import com.ryanbester.signaura.bukkit.compat.SignEditor;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class SignEditor_1_16_R3 implements SignEditor {

    @Override
    public void trackSignLocation(Player p, Plugin plugin, Location location) throws Exception {

    }

    @Override
    public List<String> getSignText(Player p, Plugin plugin, Location location, boolean parseFormatting) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public void openSignEditor(Player p, Plugin plugin, Location location, boolean parseFormatting) throws Exception {
        BlockPosition blockPos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        CraftWorld world = (CraftWorld) location.getWorld();
        if (world == null) {
            return;
        }

        TileEntitySign sign = (TileEntitySign) world.getHandle().getTileEntity(blockPos);
        if (sign == null) {
            return;
        }
        SignTileEntityStore.tileEntities.put(location, sign);

        List<IChatBaseComponent> newLines = new ArrayList<>();

        for (int i = 0; i < sign.lines.length; i++) {
            IChatBaseComponent line = sign.lines[i];
            IChatBaseComponent newLine = new ChatComponentText("");

            for (IChatBaseComponent sibling : line.getSiblings()) {
                ChatModifier mod = sibling.getChatModifier();

                String formatCode = "";
                ChatHexColor color = mod.getColor();
                if (color != null) {
                    if (color.format != null) {
                        formatCode = color.format.toString().replace("§", "&");
                    }
                }
                if (mod.isRandom()) {
                    formatCode += "&k";
                }
                if (mod.isBold()) {
                    formatCode += "&l";
                }
                if (mod.isStrikethrough()) {
                    formatCode += "&m";
                }
                if (mod.isUnderlined()) {
                    formatCode += "&n";
                }
                if (mod.isItalic()) {
                    formatCode += "&o";
                }

                newLine.getSiblings().add(new ChatComponentText(formatCode + sibling.getString()));
            }

            newLines.add(newLine);
        }

        System.arraycopy(newLines.toArray(new IChatBaseComponent[0]), 0, sign.lines, 0, 4);

        PacketPlayOutOpenSignEditor packet = new PacketPlayOutOpenSignEditor(blockPos);

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(sign.getUpdatePacket());
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public boolean updateSignContent(Player p, Plugin plugin, Location signLoc, String[] lines, boolean parseFormatting) {
        return false;
    }
}
