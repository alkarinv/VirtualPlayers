package mc.alk.virtualplayers.nms.v1_4_6;

import java.util.Collection;

import mc.alk.virtualplayers.api.VirtualPlayer;
import mc.alk.virtualplayers.util.Util;

import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.MinecraftServer;
import net.minecraft.server.v1_4_6.PlayerInteractManager;
import net.minecraft.server.v1_4_6.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_6.CraftServer;
import org.bukkit.craftbukkit.v1_4_6.CraftWorld;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CraftVirtualPlayer extends CraftPlayer implements VirtualPlayer {

    Player keepInformed; // / who to send the messages to
    boolean online = true;
    int health = 20;
    boolean isop = true;
    boolean showMessages = true;
    boolean showTeleports = true;
    GameMode gamemode = GameMode.SURVIVAL;
    Location location;
    
    public CraftVirtualPlayer(CraftServer cserver, MinecraftServer mcserver, 
            WorldServer world, String s, PlayerInteractManager pim, Location loc) {
        super(cserver, new EntityPlayer(mcserver, world, s, pim));
        this.location = loc;
    }
    
    public CraftVirtualPlayer(CraftServer cserver, MinecraftServer mcserver, 
            WorldServer world, String s, PlayerInteractManager pim) {
        super(cserver, new EntityPlayer(mcserver, world, s, pim));
        this.location = this.getLocation();
    }

    public CraftVirtualPlayer(CraftServer cserver, EntityPlayer ep) {
        super(cserver, ep);
        this.location = this.getLocation();
    }

    @Override
    public InventoryView openInventory(Inventory inv) {
        return null;
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        return false;
    }
    
    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        return false;
    }
    
    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        return false;
    }

    @Override
    public void removePotionEffect(PotionEffectType effect) {
        /// do nothing
    }

    @Override
    public void closeInventory() {
        /// do nothing
    }

    @Override
    public void updateInventory() {
        /// Do nothing
    }

    @Override
    public void setGameMode(GameMode gamemode) {
        try {
            super.setGameMode(gamemode);
        } catch (Exception e) {
            /* say nothing*/
        }
        this.gamemode = gamemode;
    }

    @Override
    public GameMode getGameMode() {
        return gamemode;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void setHealth(int h) {
        if (h < 0) {
            h = 0;
        }
        this.health = h;
        try {
            super.setHealth(h);
        } catch (Exception e) {
        }
        try {
            this.getHandle().setHealth(h);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDead() {
        return super.isDead() || health <= 0;
    }

    @Override
    public void sendMessage(String s) {
        if (showMessages) {
            Util.sendMessage(this, (!isOnline() ? "&4(Offline)&b" : "")
                    + getName() + " gettingMessage= " + s);
        }
    }

    @Override
    public void moveTo(Location loc) {
        entity.move(loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public boolean teleport(Location l, boolean respawn) {
        if (isDead()) {
            return false;
        }
        try {
            boolean changedWorlds = !this.location.getWorld().getName()
                    .equals(l.getWorld().getName());
            final String teleporting = respawn ? "respawning" : "teleporting";
            if (showTeleports && showMessages) {
                String fromWorld = "";
                String toWorld = "";
                if (changedWorlds) {
                    fromWorld = "&5" + location.getWorld().getName() + "&4,";
                    toWorld = "&5" + l.getWorld().getName() + "&4,";
                }
                Util.sendMessage(this, getName() + "&e " + teleporting + " from &4"
                        + fromWorld + Util.getLocString(location) + " &e-> &4" + toWorld
                        + Util.getLocString(l));
            }
            this.location = l.clone();
            if (changedWorlds) {
                PlayerChangedWorldEvent pcwe = new PlayerChangedWorldEvent(this,
                        l.getWorld());
                CraftServer cserver = (CraftServer) Bukkit.getServer();
                cserver.getPluginManager().callEvent(pcwe);
                /// For some reason, world doesnt get changed, so lets explicitly set it
                this.entity.world = ((CraftWorld) location.getWorld()).getHandle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
        if (isDead()) {
            return false;
        }
        super.teleport(location, cause);
        teleport(location, false);
        return true;
    }

    @Override
    public boolean teleport(Location l) {
        return teleport(l, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    @Override
    public void respawn(Location loc) {
        this.health = 20;
        boolean changedWorlds = !this.location.getWorld().getName()
                .equals(loc.getWorld().getName());
        CraftServer cserver = (CraftServer) Bukkit.getServer();
        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(this, loc,
                false);
        cserver.getPluginManager().callEvent(respawnEvent);
        if (changedWorlds) {
            PlayerChangedWorldEvent pcwe = new PlayerChangedWorldEvent(this,
                    loc.getWorld());
            cserver.getPluginManager().callEvent(pcwe);
        }
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean b) {
        if (showMessages) {
            Util.sendMessage(this, getName() + " is "
                    + (b ? "connecting" : "disconnecting"));
        }
        online = b;
    }

    @Override
    public boolean isOp() {
        return isop;
    }

    @Override
    public void setOp(boolean b) {
        isop = b;
    }

    @Override
    public String toString() {
        String world = "&5" + this.location.getWorld().getName() + ",";
        return getName() + "&e h=&2" + getHealth() + "&e o=&5" + isOnline()
                + "&e d=&7" + isDead() + "&e loc=&4" + world + "&4"
                + Util.getLocString(location) + "&e gm=&8" + getGameMode();
    }

    public void setLocation(Location l) {
        location = l;
    }

    @Override
    public Player getInformed() {
        return keepInformed;
    }

    @Override
    public void setShowMessages(boolean visibility) {
        showMessages = visibility;
    }

}
