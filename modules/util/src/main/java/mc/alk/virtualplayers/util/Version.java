package mc.alk.virtualplayers.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The Version object: Capable of asking the important question: <br/><br/>
 * 
 * Is the version that's currently installed & running on the server compatible with a specified version ? <br/><br/>
 * 
 * isCompatible(): Is the installed version greater than or equal to the minimum required version ? <br/><br/>
 * 
 * isSupported(): Is the installed version less than or equal to the maximum required version ? <br/><br/>
 * 
 * @author Nikolai
 */
public class Version implements Comparable<String> {
    
    Plugin plugin;
    String version;
    boolean enabled;
    
    private Version() {
        this.plugin = null;
        this.version = Bukkit.getServer().getBukkitVersion();
        this.enabled = true;
    }
    
    public Version(String pluginName) {
        this.plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
        this.version = (plugin == null) ? null : plugin.getDescription().getVersion();
        this.enabled = (version == null) ? false : plugin.isEnabled();
    }
    
    public static Version getPlugin(String pluginName) {
        return new Version(pluginName);
    }
    
    public static Version getServer() {
        return new Version();
    }
    
    public JavaPlugin getJavaPlugin() {
        return (this.plugin == null) ? null : (JavaPlugin) this.plugin;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public boolean isNull() {
        return (this.plugin == null);
    }
    
    /**
     * @param minVersion - The absolute minimum version that's required to achieve compatibility.
     * @return Greater than or equal to will return true. Otherwise, false.
     */
    public boolean isCompatible(String minVersion) {
        if (this.version == null || !this.enabled) return false;
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
        if (this.version == null) return false;
        int x = compareTo(maxVersion);
        if (x <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(String whichVersion) {
        int[] current = parseVersion(this.version);
        int[] min = parseVersion(whichVersion);
        int length = (current.length >= min.length) ? current.length : min.length;
        for (int index = 0; index <= (length - 1); index = index + 1) {
            try {
                if (current[index] != min[index]) {
                    if (current[index] > min[index]) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                if (current.length > min.length) {
                    return 1;
                } else if (current.length < min.length) {
                    return -1;
                }
            }
        }
        return 0;
    }
    
    /**
     * A typical version of 1.2.3.4-b567 will be broken down into an array of length five. <br/><br/>
     * 
     * [1] [2] [3] [4] [567]
     */
    private int[] parseVersion(String version) {
        String[] stringArray = version.split("[.-]");
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
    
    @Override
    public String toString() {
        String v = (this.version == null) ? "" : this.version;
        return v;
    }
}
