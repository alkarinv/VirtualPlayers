package mc.alk.virtualplayers.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * The Version object: Capable of asking the important question: <br/><br/>
 *
 * Is the version that's currently installed & running on the server compatible with a specified version ? <br/><br/>
 *
 * isCompatible(): Is the installed version greater than or equal to the minimum required version ? <br/><br/>
 *
 * isSupported(): Is the installed version less than or equal to the maximum required version ? <br/><br/>
 *
 * @author Europia79, BigTeddy98, Tux2
 */
public class Version implements Comparable<String> {

    Plugin plugin;
    String version;
    String separator = "[.-]";

    private Version(Version v) {
        this.plugin = v.getPlugin();
        this.version = v.toString();
    }

    public Version(String pluginName) {
        this.plugin = null;
        if (pluginName.equalsIgnoreCase("net.minecraft.server")) {
            try {
                this.version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            } catch (ArrayIndexOutOfBoundsException ex) {
                this.version = "vpre";
            }
            return;
        }
        this.plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
        this.version = (plugin == null) ? null : plugin.getDescription().getVersion();
    }

    public Version(Server server) {
        this.plugin = null;
        this.version = server.getBukkitVersion();
    }

    public Version(Plugin plugin) {
        this.plugin = (plugin == null) ? null : plugin;
        this.version = (plugin == null) ? null : plugin.getDescription().getVersion();
    }

    public static Version getVersion(Plugin plugin) {
        return new Version(plugin);
    }

    public static Version getVersion(String pluginName) {
        return new Version(pluginName);
    }

    public static Version getServerVersion() {
        return new Version(Bukkit.getServer());
    }

    public static Version getNmsVersion() {
        return new Version("net.minecraft.server");
    }

    public Plugin getPlugin() {
        return (plugin == null) ? null : plugin;
    }

    public boolean isEnabled() {
        if (this.plugin != null) {
            return this.plugin.isEnabled();
        } else if (this.version != null) { // No plugin, but object is a version checker for the server
            return true;
        }
        return false; // Plugin mis-spelled or not installed on the server.
    }

    /**
     * @param minVersion - The absolute minimum version that's required to achieve compatibility.
     * @return Greater than or equal to will return true. Otherwise, false.
     */
    public boolean isCompatible(String minVersion) {
        if (!this.isEnabled()) return false;
        int x = compareTo(minVersion);
        if (x >= 0) {
            return true;
        }
        return false;
    }

    /**
     * @param maxVersion - The absolute maximum version that's supported.
     * @return Less than or equal to will return true. Otherwise, false.
     */
    public boolean isSupported(String maxVersion) {
        if (!this.isEnabled()) return false;
        int x = compareTo(maxVersion);
        if (x <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(String whichVersion) {
        int[] currentVersion = parseVersion(this.version);
        int[] otherVersion = parseVersion(whichVersion);
        int length = (currentVersion.length >= otherVersion.length) ? currentVersion.length : otherVersion.length;
        for (int index = 0; index <= (length - 1); index = index + 1) {
            try {
                if (currentVersion[index] != otherVersion[index]) {
                    if (currentVersion[index] > otherVersion[index]) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                if (currentVersion.length > otherVersion.length) {
                    return 1;
                } else if (currentVersion.length < otherVersion.length) {
                    return -1;
                }
            }
        }
        return 0;
    }

    /**
     * A typical version of 1.2.3.4-b567 will be broken down into an array. <br/><br/>
     *
     * [1] [2] [3] [4] [567]
     */
    private int[] parseVersion(String version) {
        String[] stringArray = version.split(separator);
        int[] temp = new int[stringArray.length];
        for (int index = 0; index <= (stringArray.length - 1); index = index + 1) {
            String t = stringArray[index].replaceAll("\\D", "");
            try {
                temp[index] = Integer.valueOf(t);
            } catch(NumberFormatException ex) {
                temp[index] = 0;
            }
        }
        return temp;
    }

    public Version setSeparator(String regex) {
        this.separator = regex;
        return this;
    }

    public int length() {
        return (version == null) ? 0 : version.length();
    }

    /**
     * equalsIgnoreCase().
     */
    public boolean equals(String s) {
        String v = (version == null) ? "" : version;
        return s.equalsIgnoreCase(v);
    }

    public boolean contains(CharSequence s) {
        return (version == null) ? false : version.contains(s);
    }

    /**
     * search() for possible Development builds.
     */
    public boolean search(String regex) {
        if (version == null) return false;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.version);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public Version getSubVersion(String regex) {
        if (version == null) return this;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.version);
        if (matcher.find()) {
            String dev = matcher.group();
            return new Version(this).setVersion(dev);
        }
        return this;
    }

    private Version setVersion(String subVersion) {
        this.version = subVersion;
        return this;
    }

    @Override
    public String toString() {
        String v = (this.version == null) ? "" : this.version;
        return v;
    }
}
