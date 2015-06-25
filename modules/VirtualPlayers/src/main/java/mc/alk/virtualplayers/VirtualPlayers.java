package mc.alk.virtualplayers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mc.alk.virtualplayers.api.VirtualPlayer;
import mc.alk.virtualplayers.api.VirtualPlayerFactory;
import mc.alk.virtualplayers.executors.PlayerExecutor;
import mc.alk.virtualplayers.executors.VPBaseExecutor;
import mc.alk.virtualplayers.executors.VPExecutor;
import mc.alk.virtualplayers.listeners.VirtualPlayerListener;
import mc.alk.virtualplayers.version.Version;
import mc.alk.virtualplayers.version.VersionFactory;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class VirtualPlayers extends JavaPlugin {

    public static final String MAX = "1.8.7-R9.9-SNAPSHOT";
    public static final String MIN = "1.2.5";
    public static final Version<Server> server = VersionFactory.getServerVersion();
    public static final String NMS = VersionFactory.getNmsVersion().toString();

    static final VirtualPlayerFactory factory = VirtualPlayerFactory.newInstance();

    @Override
    public void onEnable() {
        if (!server.isSupported(MAX) || !server.isCompatible(MIN)) {
            getLogger().info("VirtualPlayers is not compatible with your server.");
            getLogger().info("The maximum supported version is " + MAX);
            getLogger().info("The minimum capatible version is " + MIN);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(new VirtualPlayerListener(), this);
        getCommand("vdc").setExecutor(new PlayerExecutor(this));
        getCommand("virtualplayers").setExecutor(new VPExecutor(this));
    }

    @Override
    public void onDisable() {
        deleteVirtualPlayers();
    }

    public void setPlayerMessages(boolean visibility) {
        for (VirtualPlayer vp : getVirtualPlayers()) {
            vp.setShowMessages(visibility);
        }
    }

    public void setEventMessages(boolean visibility) {
        VPBaseExecutor.setEventMessages(visibility);
    }

    public static Map<UUID, VirtualPlayer> getVps() {
        return VirtualPlayerFactory.getVps();
    }

    public static Map<String, VirtualPlayer> getNames() {
        return VirtualPlayerFactory.getNames();
    }

    public static Player getPlayer(String pname) {
        Player vp = Bukkit.getPlayer(pname);
        if (vp == null) {
            vp = getNames().get(pname);
        }
        return vp;
    }

    public static Player getPlayer(UUID id) {
        Player vp = Bukkit.getPlayer(id);
        if (vp == null) {
            vp = getVps().get(id);
        }
        return vp;
    }

    public static Player getPlayerExact(String pname) {
        Player vp = Bukkit.getPlayerExact(pname);
        if (vp == null) {
            vp = getNames().get(pname);
        }
        return vp;
    }

    /**
     * Calls getNewPlayerList() : i.e. Makes a copy.
     *
     * @return A new list, or copy.
     */
    public static List<VirtualPlayer> getPlayerList() {
        return getNewPlayerList();
    }

    /**
     * @since v2.0 : This is getPlayerList() renamed to reflect that it's a
     * copy.
     * @return A new list, or copy.
     */
    public static List<VirtualPlayer> getNewPlayerList() {
        return VirtualPlayerFactory.getNewPlayerList();
    }
    
    public static Collection<VirtualPlayer> getVirtualPlayers() {
        return VirtualPlayerFactory.getVirtualPlayers();
    }

    /**
     * This absolutely MUST be fixed:.
     * <pre>
     * Bukkit.getOnlinePlayers() won't work on older versions because of the return type.
     * </pre>
     *
     * @return
     */
    public static Player[] getOnlinePlayers() {
        generateErrorOnPurpose(); // Self reminder that can't be ignored.
        List<Player> players = new ArrayList<Player>();
        for (Player p : VirtualPlayerFactory.getVirtualPlayers()) {
            if (p.isOnline()) {
                players.add(p);
            }
        }
        Player[] ps = players.toArray(new Player[players.size()]);
        Player[] bps = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        return (Player[]) ArrayUtils.addAll(ps, bps);
    }

    public static Player getOrMakePlayer(String pname) {
        Player vp = Bukkit.getPlayer(pname);
        if (vp == null) {
            vp = getNames().get(pname);
        }
        if (vp == null) {
            try {
                return makeVirtualPlayer(pname);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return vp;
    }

    public static VirtualPlayer getOrCreate(String name) {
        Player vp = VirtualPlayerFactory.getNames().get(name);
        if (vp == null) {
            try {
                vp = makeVirtualPlayer(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (VirtualPlayer) vp;
    }

    public static Player makeVirtualPlayer() throws Exception {
        return makeVirtualPlayer(null);
    }

    public static synchronized Player makeVirtualPlayer(String name) throws Exception {
        return factory.makeVirtualPlayer(name);
    }

    /**
     * @since v2.0
     * @param name
     */
    public static void deleteVirtualPlayer(String name) {
        VirtualPlayer vp = getNames().get(name);
        deleteVirtualPlayer(vp);
    }

    /**
     * @since v2.0 : return value changed from Player to void.
     * @param vp
     */
    public static void deleteVirtualPlayer(VirtualPlayer vp) {
        factory.deleteVirtualPlayer(vp);
    }

    public static void deleteVirtualPlayers() {
        for (VirtualPlayer vp : getNewPlayerList()) {
            factory.deleteVirtualPlayer(vp);
        }
    }

    public static void setGlobalMessages(boolean visibility) {
        for (VirtualPlayer vp : VirtualPlayerFactory.getVirtualPlayers()) {
            vp.setShowMessages(visibility);
        }
    }

}
