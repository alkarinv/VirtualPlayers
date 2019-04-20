package mc.alk.virtualplayers.listeners;

import mc.alk.virtualplayers.api.VirtualPlayer;
import mc.alk.virtualplayers.api.Vps;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Handles all Events related to VirtualPlayer instances.
 * 
 * @author alkarin
 */
public class VirtualPlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawnBegin(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        if (Vps.getApi().getVps().containsKey(p.getUniqueId()) && (p instanceof VirtualPlayer)) {
            p.setHealth(20);
            p.setLastDamageCause(null);
            ((VirtualPlayer) p).setOnline(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawnEnd(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        if (Vps.getApi().getVps().containsKey(p.getUniqueId()) && (p instanceof VirtualPlayer)) {
            final Location l = event.getRespawnLocation();
            ((VirtualPlayer) p).teleport(l, true);
        }
    }

    @EventHandler
    public void onAsyncChatEvent(AsyncPlayerChatEvent event) {
        if (!Vps.getApi().getVps().containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        // For some reason we do need to actually send the messages from
        // virtualplayers ourself
        final String message = String.format(
                event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage()
        );
        for (Player p : event.getRecipients()) {
            p.sendMessage(message);
        }
    }

}
