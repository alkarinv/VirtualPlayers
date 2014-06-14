package mc.alk.virtualPlayer;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class VirtualPlayers extends JavaPlugin implements Listener
{

    

    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this, this);
        // getCommand("vdc").setExecutor(new PlayerExecutor(this));
        // getCommand("virtualplayers").setExecutor(new VPExecutor(this));
    }

    @Override
    public void onDisable()
    {
        // VirtualPlayer.deleteVirtualPlayers();
    }
    
    public static void setPlayerMessages(boolean show){
        // VirtualPlayer.setGlobalMessages(show);
    }
    
    public static void setEventMessages(boolean show){
        // VPBaseExecutor.setShowEventMessages(show);
    }

    

    

    

   

    

    

   

    

    

    

    

    

   

    

    

}
