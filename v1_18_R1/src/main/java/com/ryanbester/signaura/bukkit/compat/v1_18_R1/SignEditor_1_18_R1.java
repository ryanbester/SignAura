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

package com.ryanbester.signaura.bukkit.compat.v1_18_R1;

import com.ryanbester.signaura.bukkit.compat.SignEditor;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatHexColor;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignEditor_1_18_R1 implements SignEditor {

    private final String SECTION_SIGN = Character.toString((char) 167);

    private BlockPosition getBlockPos(Location location) {
        return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private CraftWorld getWorld(Location location) throws Exception {
        CraftWorld world = (CraftWorld) location.getWorld();
        if (world == null) {
            throw new Exception("World is null");
        }
        return world;
    }

    private TileEntitySign getSign(CraftWorld world, BlockPosition blockPosition) throws Exception {
        TileEntitySign sign = (TileEntitySign) world.getHandle().c_(blockPosition);
        if (sign == null) {
            throw new Exception("Sign is null");
        }
        return sign;
    }

    @Override
    public void trackSignLocation(Player p, Plugin plugin, Location location) throws Exception {
        BlockPosition blockPos = getBlockPos(location);
        CraftWorld world = getWorld(location);
        SignTileEntityStore.tileEntities.put(location, getSign(world, blockPos));
    }

    @Override
    public List<String> getSignText(Player p, Plugin plugin, Location location, boolean parseFormatting) throws Exception {
        TileEntitySign sign = getSign(getWorld(location), getBlockPos(location));

        List<String> lines = new ArrayList<>();

        for (int i = 0; i < TileEntitySign.a; i++) {
            boolean filterText = ((CraftPlayer) p).getHandle().U();
            IChatBaseComponent line = sign.a(i, filterText);

            if (parseFormatting) {
                lines.add(convertToFormattedString(line).getString());
            } else {
                lines.add(line.getString());
            }
        }
        return lines;
    }

    @Override
    public void openSignEditor(Player p, Plugin plugin, Location location, boolean parseFormatting) throws Exception {
        BlockPosition blockPos = getBlockPos(location);
        CraftWorld world = getWorld(location);
        TileEntitySign sign = getSign(world, blockPos);

        SignTileEntityStore.tileEntities.put(location, sign);

        if (parseFormatting) {
            for (int i = 0; i < TileEntitySign.a; i++) {
                boolean filterText = ((CraftPlayer) p).getHandle().U();
                IChatBaseComponent line = sign.a(i, filterText);

                sign.a(i, convertToFormattedString(line));
            }
        }

        PacketPlayOutTileEntityData dataPacket = PacketPlayOutTileEntityData.a(sign, TileEntity::o);
        PacketPlayOutOpenSignEditor packet = new PacketPlayOutOpenSignEditor(blockPos);

        ((CraftPlayer) p).getHandle().b.a(dataPacket);
        ((CraftPlayer) p).getHandle().b.a(packet);
    }

    @Override
    public boolean updateSignContent(Player p, Plugin plugin, Location signLoc, String[] lines, boolean parseFormatting) {
        WorldServer world = ((CraftPlayer) p).getHandle().x();

        // Get already existing sign
        TileEntitySign sign = SignTileEntityStore.tileEntities.get(signLoc);
        if (sign != null) {
            SignTileEntityStore.tileEntities.remove(signLoc);

            CraftPlayer player = (CraftPlayer) p.getPlayer();
            if (player == null) {
                return true;
            }

            SignChangeEvent event = new SignChangeEvent(
                    player.getWorld().getBlockAt(signLoc), p, lines);

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    sign.b(((CraftPlayer) p.getPlayer()).getHandle());
                    sign.f = true;

                    if (parseFormatting) {
                        System.arraycopy(
                                CraftSign.sanitizeLines(
                                        Arrays.stream(lines).map(line -> ChatColor.translateAlternateColorCodes('&', line)).toArray(String[]::new))
                                , 0, sign.d, 0, 4);
                    } else {
                        System.arraycopy(
                                CraftSign.sanitizeLines(lines)
                                , 0, sign.d, 0, 4);
                    }
                }

                BlockPosition blockPos = new BlockPosition(signLoc.getX(), signLoc.getY(), signLoc.getZ());
                IBlockData blockData = world.a_(blockPos);
                world.a(blockPos, blockData, blockData, 3);

                            ((CraftPlayer) p).getHandle().b
                                    .a(sign.c());
                ((CraftPlayer) p).getHandle().b.a(PacketPlayOutTileEntityData.a(sign, TileEntity::o));
            });

            return true;
        }

        // Otherwise a new sign is created
        return false;
    }

    private IChatBaseComponent convertToFormattedString(IChatBaseComponent component) {
        List<IChatBaseComponent> toAdd = new ArrayList<>();

        for (IChatBaseComponent sibling : component.b()) {
            toAdd.add(convertToFormattedString(sibling));
        }

        IChatBaseComponent base = new ChatComponentText(processFormatting(component));
        toAdd.forEach(sibling -> base.b().add(sibling));

        return base;
    }

    private String processFormatting(IChatBaseComponent component) {
        ChatModifier mod = component.c();

        String formatCode = "";
        ChatHexColor color = mod.a();
        if (color != null) {
            if (color.format != null) {
                formatCode = color.format.toString().replace(SECTION_SIGN, "&");
            }
        }
        formatCode += mod.f() ? "&k" : "";
        formatCode += mod.b() ? "&l" : "";
        formatCode += mod.d() ? "&m" : "";
        formatCode += mod.e() ? "&n" : "";
        formatCode += mod.c() ? "&o" : "";

        return formatCode + component.a();
    }
}
