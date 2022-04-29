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

import com.ryanbester.signaura.bukkit.compat.PacketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PacketHandler_1_18_R1 implements PacketHandler {

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

                    Location signLoc = new Location(p.getWorld(), blockPos.u(), blockPos.v(),
                            blockPos.w());

                    if ((new SignEditor_1_18_R1()).updateSignContent(p, plugin, signLoc, lines, parseFormatting)) {
                        return;
                    }
                }

                super.channelRead(ctx, packet);
            }
        };
        final ChannelPipeline pipeline = ((CraftPlayer) p)
                .getHandle().b.a.k.pipeline();
        pipeline.addBefore("packet_handler", p.getName(), duplexHandler);

    }

    @Override
    public void playerLeave(Player p) {
        final Channel channel = ((CraftPlayer) p).getHandle().b.a.k;
        channel.eventLoop().submit(() -> channel.pipeline().remove(p.getName()));
    }
}
