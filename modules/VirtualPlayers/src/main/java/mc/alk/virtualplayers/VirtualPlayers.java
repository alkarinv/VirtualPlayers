package mc.alk.virtualplayers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import mc.alk.virtualplayers.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public class VirtualPlayers extends JavaPlugin {
    
    private Version server;
    public static final String MAX = "1.7.10-R9.9-SNAPSHOT";
    public static final String MIN = "1.2.5";
    public static final String NMS = Version.getNmsVersion().toString();

    @Override
    public void onEnable() {
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

    public static Class<?> getNmsClass(String clazz) throws Exception {
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

    /**
     * Used by mc.alk.arena.util.ServerUtil.
     * Implementation moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer:
     */
    public static Player getPlayer(String pname)  {
        return (Player) invoke("getPlayer", pname);
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static Player getPlayer(UUID id)  {
        return (Player) invoke("getPlayer", id);
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static Player getPlayerExact(String pname)  {
        return (Player) invoke("getPlayerExact", pname);
    }

    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static Player getOrMakePlayer(String pname)  {
        return (Player) invoke("getOrMakePlayer", pname);
    }
    
    /**
     * Used by mc.alk.arena.util.ServerUtil.
     * Implementation moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer:getOnlinePlayers().
     */
    public static Player[] getOnlinePlayers()  {
        return (Player[]) invoke("getOnlinePlayers");
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static Player makeVirtualPlayer() throws Exception {
        return makeVirtualPlayer(null);
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static synchronized Player makeVirtualPlayer(String name) throws Exception {
        return (Player) invoke("makeVirtualPlayer", name);
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static Player deleteVirtualPlayer(Object vp)  {
        Player player = null;
        Method[] methods = null;
        try {
            methods = getNmsClass("VirtualPlayer").getDeclaredMethods();
        } catch (Exception ex) {
            Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Method m : methods) {
            Class<?>[] params = m.getParameterTypes();
            if (m.getName().equalsIgnoreCase("deleteVirtualPlayer")) {
                try {
                    player = (Player) m.invoke(null, vp);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return player;
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static void deleteVirtualPlayers() {
        invoke("deleteVirtualPlayers");
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static List getPlayerList() {
        return (List) invoke("getPlayerList");
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static Object getOrCreate(String name)  {
        return invoke("getOrCreate", name);
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static Map getVps()  {
        return (Map) invoke("getVps");
    }
    
    /**
     * Method moved to mc.alk.virtualplayers.nms.{version}.VirtualPlayer.
     */
    public static Map getNames()  {
        return (Map) invoke("getNames");
    }
    
    private static Object invoke(String methodName, Object... params) {
        Object object = null;
        Method method;
        try {
            if (params == null) {
                method = getNmsClass("VirtualPlayer").getDeclaredMethod(methodName);
            } else {
                Class[] classParams = new Class[params.length];
                for (int index = 0; index < params.length; index = index + 1) {
                    classParams[index] = params[index].getClass();
                }
                method = getNmsClass("VirtualPlayer").getDeclaredMethod(methodName, classParams);
            }
            object = method.invoke(null, params);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(mc.alk.virtualPlayer.VirtualPlayers.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

}
