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

import java.nio.file.Paths;
import java.sql.*;
import java.util.UUID;

public class SqliteDatabase implements AbstractDatabase {

    private final Plugin plugin;
    private Connection conn;

    public SqliteDatabase(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initDatabase() throws Exception {
        String filepath = Paths.get(plugin.getDataFolder().toString(), MainConfig.DatabaseConfig.filename).toString();

        String url = "jdbc:sqlite:" + filepath;
        conn = DriverManager.getConnection(url);

        String sql = "CREATE TABLE IF NOT EXISTS sign_owners (" +
                "world TEXT NOT NULL," +
                "x REAL NOT NULL," +
                "y REAL NOT NULL," +
                "z REAL NOT NULL," +
                "uuid TEXT NOT NULL," +
                "PRIMARY KEY(world, x, y, z)" +
                ");";
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }

    @Override
    public UUID getSignOwner(Location loc) throws Exception {
        String sql = "SELECT uuid FROM sign_owners WHERE " +
                "world = ? AND x = ? AND y = ? AND z = ?;";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, loc.getWorld().getName());
        stmt.setDouble(2, loc.getX());
        stmt.setDouble(3, loc.getY());
        stmt.setDouble(4, loc.getZ());

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            return UUID.fromString(rs.getString("uuid"));
        }
        return null;
    }

    @Override
    public void setSignOwner(Location loc, UUID player) throws Exception {
        String sql = "INSERT INTO sign_owners VALUES(?, ?, ?, ?, ?);";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, loc.getWorld().getName());
        stmt.setDouble(2, loc.getX());
        stmt.setDouble(3, loc.getY());
        stmt.setDouble(4, loc.getZ());
        stmt.setString(5, player.toString());

        stmt.executeUpdate();
    }

    @Override
    public void removeSignOwner(Location loc) throws Exception {
        String sql = "DELETE FROM sign_owners WHERE " +
                "world = ? AND x = ? AND y = ? AND z = ?;";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, loc.getWorld().getName());
        stmt.setDouble(2, loc.getX());
        stmt.setDouble(3, loc.getY());
        stmt.setDouble(4, loc.getZ());

        stmt.executeUpdate();
    }

    @Override
    public void close() throws Exception {
        conn.close();
    }
}
