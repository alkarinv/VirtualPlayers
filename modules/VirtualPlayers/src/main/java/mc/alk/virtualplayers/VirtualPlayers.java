package mc.alk.virtualplayers;

import java.util.logging.Level;
import java.util.logging.Logger;
import mc.alk.virtualplayers.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public class VirtualPlayers extends JavaPlugin implements Listener
{
    Version server;
    public static final String MAX = "1.7.10-R9.9-SNAPSHOT";
    public static final String MIN = "1.2.5";
    String NMS;

    @Override
    public void onEnable() {
        NMS = Version.getNmsVersion().toString();
        server = Version.getServerVersion();
        if (!server.isSupported(MAX) || !server.isCompatible(MIN)) {
            getLogger().info("VirtualPlayers is not compatible with your server.");
            getLogger().info("The maximum supported version is " + MAX);
            getLogger().info("The minimum capatible version is " + MIN);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Bukkit.getPluginManager().registerEvents(this, this);
        // getCommand("vdc").setExecutor(new PlayerExecutor(this));
        // getCommand("virtualplayers").setExecutor(new VPExecutor(this));
        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable()
    {
        // mc.alk.virtualplayers.nms.{version}.VirtualPlayer.deleteVirtualPlayers();
        try {
            getNmsClass("VirtualPlayer").getDeclaredMethod("deleteVirtualPlayers", new Class[]{}).invoke(null);
        } catch (Exception ex) {
            Logger.getLogger(VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setPlayerMessages(boolean show){
        // mc.alk.virtualplayers.nms.{version}.VirtualPlayer.setGlobalMessages(show);
        try {
            getNmsClass("VirtualPlayer").getDeclaredMethod("setGlobalMessages", boolean.class).invoke(null, show);
        } catch (Exception ex) {
            Logger.getLogger(VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setEventMessages(boolean show){
        // mc.alk.virtualplayers.nms.{version}.VPBaseExecutor.setShowEventMessages(show);
        try {
            getNmsClass("VPBaseExecutor").getDeclaredMethod("setShowEventMessages", boolean.class).invoke(null, show);
        } catch (Exception ex) {
            Logger.getLogger(VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Class<?> getNmsClass(String clazz) throws Exception {
        return Class.forName("mc.alk.virtualplayers.nms." + NMS + "." + clazz);
    }

    private void registerListeners() {
        // mc.alk.virtualplayers.nms.{version}.PlayerListener
        try {
            getServer().getPluginManager().registerEvents(
                    (Listener) getNmsClass("PlayerListener").getConstructor().newInstance(), this);
        } catch (Exception ex) {
            Logger.getLogger(VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void registerCommands() {
        // getCommand("vdc").setExecutor(new PlayerExecutor(this));
        // getCommand("virtualplayers").setExecutor(new VPExecutor(this));
        try {
            getCommand("vdc").setExecutor(
                    (CommandExecutor) getNmsClass("PlayerExecutor")
                    .getConstructor(new Class[]{Plugin.class}).newInstance(this));
            getCommand("virtualplayers").setExecutor(
                    (CommandExecutor) getNmsClass("VPExecutor")
                    .getConstructor(new Class[]{Plugin.class}).newInstance(this));
        } catch (Exception ex) {
            Logger.getLogger(VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
