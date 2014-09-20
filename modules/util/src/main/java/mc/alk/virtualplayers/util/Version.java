package mc.alk.virtualplayers.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * The Version object: Capable of asking the important questions:. <br/><br/>
 * 
 * Is the version that's currently installed on the server compatible/supported with a specified version ? <br/><br/>
 * 
 * isCompatible(): Is the installed version greater than or equal to the minimum required version ? <br/><br/>
 * 
 * isSupported(): Is the installed version less than or equal to the maximum required version ? <br/><br/>
 * 
 * @author Europia79, BigTeddy98, Tux2, DSH105
 */
public class Version implements Comparable<Version> {
    
    final Plugin plugin;
    final String version;
    String separator = "[_.-]";

    /**
     * Factory methods getPluginVersion(), getServerVersion(), getNmsVersion() available for convenience. <br/>
     */
    public Version(String version) {
        this.version = version;
        this.plugin = null;
    }
    
    public Version(Server server) {
        this.plugin = null;
        this.version = server.getBukkitVersion();
    }
    
    public Version(Plugin plugin) {
        this.plugin = (plugin == null) ? null : plugin;
        this.version = (plugin == null) ? null : plugin.getDescription().getVersion();
    }
    
    /**
     * Factory method used when you want to construct a Version object via a Plugin object. <br/>
     */
    public static Version getPluginVersion(Plugin plugin) {
        return new Version(plugin);
    }
    
    /**
     * Factory method used when you want to construct a Version object via pluginName. <br/>
     */
    public static Version getPluginVersion(String pluginName) {
        return new Version(Bukkit.getPluginManager().getPlugin(pluginName));
    }
    
    /**
     * Factory method to conveniently construct a Version object of the server. <br/>
     */
    public static Version getServerVersion() {
        return new Version(Bukkit.getServer());
    }
    
    /**
     * Factory method to conveniently construct a Version object of net.minecraft.server.v1_X_RY package. <br/>
     */
    public static Version getNmsVersion() {
        String NMS = null;
            try {
                NMS = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            } catch (ArrayIndexOutOfBoundsException ex) {
                NMS = "vpre";
            }
        return new Version(NMS);
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
     * @return Return true, if the currently running/installed version is greater than or equal to minVersion.
     */
    public boolean isCompatible(String minVersion) {
        if (!this.isEnabled()) return false;
        int x = compareTo(new Version(minVersion));
        if (x >= 0) {
            return true;
        } 
        return false;
    }
    
    /**
     * @param maxVersion - The absolute maximum version that's supported.
     * @return Return true, if the currently running/installed version is less than or equal to maxVersion.
     */
    public boolean isSupported(String maxVersion) {
        if (!this.isEnabled()) return false;
        int x = compareTo(new Version(maxVersion));
        if (x <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Version whichVersion) {
        int[] currentVersion = parseVersion(this.version);
        int[] otherVersion = parseVersion(whichVersion.toString());
        int length = (currentVersion.length >= otherVersion.length) ? currentVersion.length : otherVersion.length;
        for (int index = 0; index <= (length - 1); index = index + 1) {
            try {
                if (currentVersion[index] != otherVersion[index]) {
                    return currentVersion[index] - otherVersion[index];
                }
            } catch (IndexOutOfBoundsException ex) {
                if (currentVersion.length > otherVersion.length) {
                    return currentVersion[index] - 0;
                } else if (currentVersion.length < otherVersion.length) {
                    return 0 - otherVersion[index];
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
        String dev = this.version;
        if (matcher.find()) {
            dev = matcher.group();
        }
        return new Version(dev);
    }
    
    @Override
    public String toString() {
        String v = (this.version == null) ? "" : this.version;
        return v;
    }
}
