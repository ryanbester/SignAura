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

package com.ryanbester.signaura.bukkit.database;

import com.ryanbester.signaura.bukkit.config.MainConfig;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class FlatfileDatabase implements AbstractDatabase {

    private final Plugin plugin;

    private static final HashMap<Location, UUID> signs = new HashMap<>();

    public FlatfileDatabase(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initDatabase() throws Exception {
        File file = openFile();

        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] kv = line.split(":");
            if (kv.length < 2) {
                continue;
            }
            String[] loc = kv[0].split(",");
            if (loc.length < 4) {
                continue;
            }

            signs.put(new Location(plugin.getServer().getWorld(loc[0]), Float.parseFloat(loc[1]), Float.parseFloat(loc[2]), Float.parseFloat(loc[3])), UUID.fromString(kv[1]));
        }

        scanner.close();
    }

    @Override
    public UUID getSignOwner(Location loc) throws Exception {
        return signs.get(loc);
    }

    @Override
    public void setSignOwner(Location loc, UUID player) throws Exception {
        signs.put(loc, player);

        saveToFile();
    }

    @Override
    public void removeSignOwner(Location loc) throws Exception {
        signs.remove(loc);

        saveToFile();
    }

    private File openFile() throws Exception {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                throw new Exception("Failed to create data directory");
            }
        }

        File file = new File(plugin.getDataFolder(), MainConfig.DatabaseConfig.filename);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new Exception("Failed to create sign owners file");
            }
        }

        return file;
    }

    private void saveToFile() throws Exception {
        File file = openFile();

        FileWriter fw = new FileWriter(file);
        for (Map.Entry<Location, UUID> sign : signs.entrySet()) {
            Location loc = sign.getKey();
            UUID uuid = sign.getValue();

            Objects.requireNonNull(loc.getWorld());

            fw.write(String.format("%s,%f,%f,%f:%s%s",
                    loc.getWorld().getName(),
                    loc.getX(), loc.getY(), loc.getZ(),
                    uuid.toString(),
                    System.lineSeparator()));
        }
        fw.close();
    }

    @Override
    public void close() throws Exception {

    }
}
