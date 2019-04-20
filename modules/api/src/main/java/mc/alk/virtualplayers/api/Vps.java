package mc.alk.virtualplayers.api;

import org.bukkit.Bukkit;

/**
 *
 * @author Nikolai
 */
public abstract class Vps {
    
    public static VirtualPlayersAPI getApi() {
        return (VirtualPlayersAPI)Bukkit.getPluginManager().getPlugin("VirtualPlayers");
    }
    
}
