package mc.alk.virtualPlayer;

import mc.alk.virtualPlayer.Executors.CustomCommandExecutor;
import mc.alk.virtualPlayer.Executors.PlayerExecutor;
import mc.alk.virtualPlayer.Executors.VPBaseExecutor;
import mc.alk.virtualPlayer.Executors.VPExecutor;
import net.minecraft.server.v1_7_R2.MinecraftServer;
import net.minecraft.server.v1_7_R2.PlayerInteractManager;
import net.minecraft.server.v1_7_R2.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R2.CraftServer;
import org.bukkit.craftbukkit.v1_7_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VirtualPlayers extends JavaPlugin implements Listener
{

    static final Map<UUID, VirtualPlayer> vps = new HashMap<UUID, VirtualPlayer>();
    static final Map<String, VirtualPlayer> names = new HashMap<String, VirtualPlayer>();
    static VirtualPlayers plugin;

    @Override
    public void onEnable()
    {
        plugin = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("vdc").setExecutor(new PlayerExecutor(this));
        getCommand("virtualplayers").setExecutor(new VPExecutor(this));
    }

    @Override
    public void onDisable()
    {
        deleteVirtualPlayers();
    }

    public static VirtualPlayers getSelf()
    {
        return plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawnBegin(PlayerRespawnEvent event){
        Player p = event.getPlayer();
        if (vps.containsKey(p.getUniqueId()) && (p instanceof VirtualPlayer))
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
        if (vps.containsKey(p.getUniqueId()) && (p instanceof VirtualPlayer))
        {
            final Location l = event.getRespawnLocation();
            ((VirtualPlayer) p).teleport(l, true);
        }
    }

    public static void setPlayerMessages(boolean show){
        VirtualPlayer.setGlobalMessages(show);
    }

    public static void setEventMessages(boolean show){
        VPBaseExecutor.setShowEventMessages(show);
    }

    @EventHandler
    public void onAsyncChatEvent(AsyncPlayerChatEvent event)
    {
        if (!vps.containsKey(event.getPlayer().getUniqueId())) // / don't need to
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

    public static Player getPlayer(String pname)
    {
        Player vp = Bukkit.getPlayer(pname);
        if (vp == null) vp = names.get(pname);
        return vp;
    }

    public static Player getPlayer(UUID id) {
        Player vp = Bukkit.getPlayer(id);
        if (vp == null) vp = vps.get(id);
        return vp;
    }

    public static Player getPlayerExact(String pname)
    {
        Player vp = Bukkit.getPlayerExact(pname);
        if (vp == null) vp = names.get(pname);
        return vp;
    }

    public static Player getOrMakePlayer(String pname)
    {
        Player vp = Bukkit.getPlayer(pname);
        if (vp == null) vp = names.get(pname);
        if (vp == null){
            try {
                return makeVirtualPlayer(pname);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return vp;
    }

    public static Player[] getOnlinePlayers()
    {
        List<Player> players = new ArrayList<Player>();
        for (Player p : vps.values()){
            if (p.isOnline()){
                players.add(p);}
        }
        Player[] ps = players.toArray(new Player[players.size()]);
        Player[] bps = Bukkit.getOnlinePlayers();
        return (Player[]) ArrayUtils.addAll(ps, bps);
    }

    public static Player makeVirtualPlayer() throws Exception {
        return makeVirtualPlayer(null);
    }

    public static synchronized Player makeVirtualPlayer(String name) throws Exception
    {
        CraftServer cserver = (CraftServer) Bukkit.getServer();
        List<World> worlds = cserver.getWorlds();
        if (worlds == null || worlds.isEmpty())
            throw new Exception("There must be at least one world");
        CraftWorld w = (CraftWorld) worlds.get(0);
        Location location = new Location(w, 0, 0, 0);
        MinecraftServer mcserver = cserver.getServer();
        WorldServer worldServer = mcserver.getWorldServer(0);
        PlayerInteractManager pim = new PlayerInteractManager(worldServer);
        if (name == null) {
            name = "p" + (vps.size() + 1);}
        GameProfile gameProfile = new GameProfile(UUID.randomUUID().toString(), CustomCommandExecutor.colorChat(name));
        VirtualPlayer vp = new VirtualPlayer(cserver, mcserver, worldServer, gameProfile, pim);
        vps.put(vp.getUniqueId(), vp);
        names.put(vp.getName(), vp);
        vp.loc = location;
        return vp;
    }

    public static Player deleteVirtualPlayer(VirtualPlayer vp)
    {
        WorldServer world = ((CraftWorld) vp.getLocation().getWorld()).getHandle();
        world.removeEntity(vp.getHandle());
        vp.remove();
        vps.remove(vp.getUniqueId());
        return vp;
    }

    public static void deleteVirtualPlayers()
    {
        for (VirtualPlayer vp : vps.values())
        {
            WorldServer world = ((CraftWorld) vp.getLocation().getWorld()).getHandle();
            world.removeEntity(vp.getHandle());
            vp.remove();
        }
        vps.clear();
    }

    public List<VirtualPlayer> getPlayerList() {
        synchronized (vps) {
            return new ArrayList<VirtualPlayer>(vps.values());
        }
    }

    public static VirtualPlayer getOrCreate(String name) {
        Player vp = names.get(name);
        if (vp == null) {
            try{
                vp = makeVirtualPlayer(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (VirtualPlayer) vp;
    }

}
