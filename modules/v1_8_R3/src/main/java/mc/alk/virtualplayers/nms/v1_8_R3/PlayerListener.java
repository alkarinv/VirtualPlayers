package mc.alk.virtualplayers.nms.v1_8_R3;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 *
 * @author Nikolai
 */
public class PlayerListener implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawnBegin(PlayerRespawnEvent event){
        Player p = event.getPlayer();
        if (VirtualPlayer.getVps().containsKey(p.getUniqueId()) && (p instanceof VirtualPlayer))
        {
            p.setHealth(20.0);
            p.setLastDamageCause(null);
            ((VirtualPlayer) p).setOnline(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawnEnd(PlayerRespawnEvent event)
    {
        Player p = event.getPlayer();
        if (VirtualPlayer.getVps().containsKey(p.getUniqueId()) && (p instanceof VirtualPlayer))
        {
            final Location l = event.getRespawnLocation();
            ((VirtualPlayer) p).teleport(l, true);
        }
    }
    
    @EventHandler
    public void onAsyncChatEvent(AsyncPlayerChatEvent event)
    {
        if (!VirtualPlayer.getVps().containsKey(event.getPlayer().getUniqueId())) // / don't need to
            // handle it
            return;
        // / For some reason we do need to actually send the messages from
        // virtualplayers ourself
        final String message = String.format(event.getFormat(), event
                .getPlayer().getDisplayName(), event.getMessage());
        for (Player p : event.getRecipients())
        {
            p.sendMessage(message);
        }
    }
    
}
