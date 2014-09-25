package mc.alk.virtualPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * This class maps the old package to the new package. <br/><br/>
 * <pre>
 * old package: mc.alk.virtualPlayer;
 * new package: mc.alk.virtualplayers;
 * 
 * So that old plugins don't have to update to the new package structure.
 * 
 * The primary reason for the change is that several of these methods 
 * contained version dependent code. So they needed to be put into a 
 * versioned package structure.
 * </pre>
 * @author Nikolai
 */
public class VirtualPlayers {
    
    /**
     * Used by mc.alk.arena.util.ServerUtil.
     * @param pname
     * @return
     * @deprecated
     */
    @Deprecated
    public static Player getPlayer(String pname)
    {
        return mc.alk.virtualplayers.VirtualPlayers.getPlayer(pname);
    }
    
    @Deprecated
    public static Player getPlayer(UUID id)
    {
        return mc.alk.virtualplayers.VirtualPlayers.getPlayer(id);
    }

    @Deprecated
    public static Player getPlayerExact(String pname)
    {
        return mc.alk.virtualplayers.VirtualPlayers.getPlayerExact(pname);
    }

    @Deprecated
    public static Player getOrMakePlayer(String pname)
    {
        return mc.alk.virtualplayers.VirtualPlayers.getOrMakePlayer(pname);
    }

    @Deprecated
    public static Player[] getOnlinePlayers()
    {
        return mc.alk.virtualplayers.VirtualPlayers.getOnlinePlayers();
    }

    @Deprecated
    public static Player makeVirtualPlayer() throws Exception {
        return makeVirtualPlayer(null);
    }

    @Deprecated
    public static synchronized Player makeVirtualPlayer(String name) throws Exception
    {
        return mc.alk.virtualplayers.VirtualPlayers.makeVirtualPlayer(name);
    }

    /**
     * Object parameter needs to be changed to a VirtualPlayer interface.
     * @param vp Pass in an instance of VirtualPlayer.
     * @return 
     */
    @Deprecated
    public static Player deleteVirtualPlayer(Object vp)
    {
        return mc.alk.virtualplayers.VirtualPlayers.deleteVirtualPlayer(vp);
    }

    @Deprecated
    public static void deleteVirtualPlayers()
    {
        mc.alk.virtualplayers.VirtualPlayers.deleteVirtualPlayers();
    }

    /**
     * Moved to mc.alk.virtualplayers.VirtualPlayers:getPlayerList().
     * @return <pre> {@literal List<VirtualPlayer> } </pre>
     */
    @Deprecated
    public static List getPlayerList() {
        return (List) mc.alk.virtualplayers.VirtualPlayers.getPlayerList();
    }
    
    @Deprecated
    public static Object getOrCreate(String name) {
        return mc.alk.virtualplayers.VirtualPlayers.getOrCreate(name);
    }
    
    /**
     * Moved to mc.alk.virtualplayers.VirtualPlayers:getVps().
     * @return <pre> {@literal Map<UUID, VirtualPlayer> } </pre>
     * @deprecated
     */
    @Deprecated
    public static Map getVps() {
        return (Map) mc.alk.virtualplayers.VirtualPlayers.getVps();
    }
    
    /**
     * Moved to mc.alk.virtualplayers.VirtualPlayers:getNames().
     * @return <pre> {@literal Map<String, VirtualPlayer> } </pre>
     * @deprecated
     */
    @Deprecated
    public static Map getNames() {
        return (Map) mc.alk.virtualplayers.VirtualPlayers.getNames();
    }
}
