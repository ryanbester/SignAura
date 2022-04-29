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


import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Credit: https://github.com/dmulloy2/ProtocolLib/blob/master/src/main/java/com/comphenix/protocol/utility/MinecraftVersion.java
public class MinecraftVersion implements Comparable<MinecraftVersion> {
    private static final Pattern VERSION_PATTERN = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-.]+).*");

    private final int major;
    private final int minor;
    private final int build;

    public MinecraftVersion(int major, int minor, int build) {
        this.major = major;
        this.minor = minor;
        this.build = build;
    }

    public MinecraftVersion(String text) throws Exception {
        Matcher version = VERSION_PATTERN.matcher(text);

        if (version.matches() && version.group(1) != null) {
            MinecraftVersion parsedVersion = fromVersionString(version.group(1));

            this.major = parsedVersion.major;
            this.minor = parsedVersion.minor;
            this.build = parsedVersion.build;
        } else {
            throw new IllegalStateException("Invalid version string");
        }

    }

    private static MinecraftVersion fromVersionString(String version) {
        String[] ele = version.split("\\.");

        if (ele.length < 1) {
            throw new IllegalStateException("Invalid Minecraft version");
        }

        int[] nums = new int[3];
        for (int i = 0; i < 3; i++) {
            nums[i] = Integer.parseInt(ele.length > i ? ele[i] : "0");
        }

        return new MinecraftVersion(nums[0], nums[1], nums[2]);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getBuild() {
        return build;
    }

    public boolean atLeast(String version) throws Exception {
        MinecraftVersion toCompare = fromVersionString(version);

        return this.compareTo(toCompare) >= 0;
    }

    @Override
    public int compareTo(MinecraftVersion o) {
        if (o == null) {
            return 1;
        }

        int a = Integer.compare(this.major, o.major);
        if (a == 0) {
            a = Integer.compare(this.minor, o.minor);
            if (a == 0) {
                return Integer.compare(this.build, o.build);
            }
        }

        return a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftVersion that = (MinecraftVersion) o;
        return major == that.major && minor == that.minor && build == that.build;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, build);
    }

    @Override
    public String toString() {
        return "MinecraftVersion{" +
                "major=" + major +
                ", minor=" + minor +
                ", build=" + build +
                '}';
    }
}
