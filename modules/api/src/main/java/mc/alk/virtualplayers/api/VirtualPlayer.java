package mc.alk.virtualplayers.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 
 * @author Nikolai
 */
public interface VirtualPlayer extends Player {
    
    public void setOnline(boolean b);
    
    public void respawn(Location loc);
    public void moveTo(Location loc);
    public boolean teleport(Location location, boolean respawn);
    
    public Player getInformed();
    public void setShowMessages(boolean visibility);

}
