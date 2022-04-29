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

import com.ryanbester.signaura.bukkit.compat.PacketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

public class PacketHandler_1_16_R3 implements PacketHandler {

    @Override
    public void playerJoin(Player p, Plugin plugin, boolean parseFormatting) {
        ChannelDuplexHandler duplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
                if (packet instanceof PacketPlayInUpdateSign
                && !p.getName().startsWith("*")) {
                    PacketPlayInUpdateSign inUpdateSign = (PacketPlayInUpdateSign) packet;
                    BlockPosition blockPos = inUpdateSign.b();
                    String[] lines = inUpdateSign.c();

                    WorldServer world = ((CraftPlayer) p).getHandle().getWorldServer();

                    Location signLoc = new Location(p.getWorld(), blockPos.getX(), blockPos.getY(),
                        blockPos.getZ());

                    // Get already existing sign
                    TileEntitySign sign = SignTileEntityStore.tileEntities.get(signLoc);
                    if (sign != null) {
                        SignTileEntityStore.tileEntities.remove(signLoc);

                        CraftPlayer player = (CraftPlayer) p.getPlayer();
                        if (player == null) {
                            return;
                        }

                        SignChangeEvent event = new SignChangeEvent(
                            player.getWorld().getBlockAt(signLoc), p, lines);

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.getServer().getPluginManager().callEvent(event);

                            if (!event.isCancelled()) {
                                sign.b(((CraftPlayer) p.getPlayer()).getHandle());
                                sign.isEditable = true;
                                System
                                    .arraycopy(CraftSign.sanitizeLines(lines), 0, sign.lines, 0, 4);
                            }

                            IBlockData blockData = world.getType(blockPos);
                            world.notify(blockPos, blockData, blockData, 3);

                            ((CraftPlayer) p).getHandle().playerConnection
                                .sendPacket(sign.getUpdatePacket());
                        });

                        return;
                    }

                    // Otherwise a new sign is created
                }

                super.channelRead(ctx, packet);
            }
        };
        final ChannelPipeline pipeline = ((CraftPlayer) p)
            .getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", p.getName(), duplexHandler);

    }

    @Override
    public void playerLeave(Player p) {
        final Channel channel = ((CraftPlayer) p)
            .getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> channel.pipeline().remove(p.getName()));
    }
}
