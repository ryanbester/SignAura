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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.HashSet;
import java.util.List;

public class BedrockSignEditor {
    public static void openSignEditor(Plugin plugin, Player p, Location signLoc) {
        SignEditor signEditor = CompatManager.getSignEditor();
        if (signEditor == null) {
            return;
        }

        List<String> lines = null;
        try {
            lines = signEditor.getSignText(p, plugin, signLoc, MainConfig.parseFormattingCodes);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        showSignEditorDialog(plugin, p, signEditor, signLoc, lines, new HashSet<>());
    }

    private static void showSignEditorDialog(Plugin plugin, Player p, SignEditor signEditor, Location signLoc, List<String> lines, HashSet<Integer> invalidLines) {
        FloodgatePlayer player = FloodgateApi.getInstance().getPlayer(p.getUniqueId());

        CustomForm.Builder builder = CustomForm.builder()
                .title("Edit Sign Text");

        for (int i = 0; i < 4; i++) {
            builder = builder.input("Line " + (i + 1), "Line " + (i + 1), lines.get(i));
            if (invalidLines.contains(i)) {
                builder = builder.label((char) 167 + "4Must be less than 15 characters");
            }
        }

        builder.responseHandler((customForm, s) -> {
            CustomFormResponse res = customForm.parseResponse(s);
            if (res.isClosed()) {
                return;
            }

            lines.set(0, res.getInput(0) != null ? res.getInput(0) : "");
            lines.set(1, res.getInput(1) != null ? res.getInput(1) : "");
            lines.set(2, res.getInput(2) != null ? res.getInput(2) : "");
            lines.set(3, res.getInput(3) != null ? res.getInput(3) : "");

            HashSet<Integer> errors = new HashSet<>();
            for (int i = 0; i < 4; i++) {
                if (lines.get(i).length() > 15) {
                    errors.add(i);
                }
            }
            if (errors.size() > 0) {
                showSignEditorDialog(plugin, p, signEditor, signLoc, lines, errors);
                return;
            }

            try {
                signEditor.trackSignLocation(p, plugin, signLoc);
                signEditor.updateSignContent(p, plugin, signLoc,
                        lines.toArray(new String[0]),
                        MainConfig.parseFormattingCodes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        player.sendForm(builder);
    }
}
