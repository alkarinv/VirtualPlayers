package mc.alk.virtualPlayer;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.MinecraftServer;
import net.minecraft.server.v1_7_R1.PlayerInteractManager;
import net.minecraft.server.v1_7_R1.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R1.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

public class VirtualPlayer extends CraftPlayer
{
	Player keepInformed; // / who to send the messages to
	boolean online = true;
	double health = 20;
	boolean isop = true;
	static boolean enableMessages = true;
	boolean showMessages = true;
	boolean showTeleports = true;
	GameMode gamemode = GameMode.SURVIVAL;

	Location loc;
    CraftScoreboard scoreboard;


	public VirtualPlayer(CraftServer cserver, MinecraftServer mcserver, WorldServer worldServer,
			GameProfile gameProfile, PlayerInteractManager pim)
	{
		/// mcserver, worldserver, GameProfile, PlayerInteractManager
		super(cserver, new EntityPlayer(mcserver, worldServer, gameProfile, pim));
		this.loc = this.getLocation();
	}

	public VirtualPlayer(CraftServer cserver, EntityPlayer ep)
	{
		super(cserver, ep);
		this.loc = this.getLocation();
	}

	@Override
	public InventoryView openInventory(Inventory inv){
		return null;
	}

	@Override
	public void removePotionEffect(PotionEffectType effect){
		/// do nothing
	}

	@Override
	public void closeInventory(){
		/// do nothing
	}

	@Override
	public void updateInventory()
	{
		/// Do nothing
	}

	@Override
	public void setGameMode(GameMode gamemode)
	{
		try{
			super.setGameMode(gamemode);
		} catch (Exception e){
            /* say nothing*/
		}
		this.gamemode = gamemode;
	}

	@Override
	public GameMode getGameMode()
	{
		return gamemode;
	}

    @Override
    public double getHealth() {
        return health;
    }

    @Override
	public void setHealth(double h)
	{
		if (h < 0) h = 0;
		this.health = h;
        try{super.setHealth(h);} catch (Exception e){}
		try{this.getHandle().setHealth((float)h);} catch (Exception e){e.printStackTrace();}
	}



	@Override
	public boolean isDead()
	{
		return super.isDead() || health <= 0;
	}

	@Override
	public void sendMessage(String s){
		if (showMessages && enableMessages)
			Util.sendMessage(this, (!isOnline() ? "&4(Offline)&b" : "")
					+ getName() + " gettingMessage= " + s);
	}

	public void moveTo(Location loc){
		entity.move(loc.getX(), loc.getY(), loc.getZ());
	}

	public boolean teleport(Location l, boolean respawn)
	{
		if (isDead()){
			return false;
		}
		try{
			boolean changedWorlds = !this.loc.getWorld().getName()
					.equals(l.getWorld().getName());
			final String teleporting = respawn ? "respawning" : "teleporting";
			if (showTeleports && enableMessages)
			{
				String fromWorld = "";
				String toWorld = "";
				if (changedWorlds)
				{
					fromWorld = "&5" + loc.getWorld().getName() + "&4,";
					toWorld = "&5" + l.getWorld().getName() + "&4,";
				}
				Util.sendMessage(this, getName() + "&e " + teleporting + " from &4"
						+ fromWorld + Util.getLocString(loc) + " &e-> &4" + toWorld
						+ Util.getLocString(l));
			}
			this.loc = l.clone();
			if (changedWorlds)
			{
				PlayerChangedWorldEvent pcwe = new PlayerChangedWorldEvent(this,
						l.getWorld());
				CraftServer cserver = (CraftServer) Bukkit.getServer();
				cserver.getPluginManager().callEvent(pcwe);
				/// For some reason, world doesnt get changed, so lets explicitly set it
				this.entity.world = ((CraftWorld) loc.getWorld()).getHandle();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}

    @Override
    public boolean teleport(Location location,  PlayerTeleportEvent.TeleportCause cause) {
        if (isDead())
            return false;
        super.teleport(location, cause);
        teleport(location, false);
        return true;
    }

	@Override
	public boolean teleport(Location l) {
        return teleport(l, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

	public void respawn(Location loc)
	{
		this.health = 20;
		boolean changedWorlds = !this.loc.getWorld().getName()
				.equals(loc.getWorld().getName());
		CraftServer cserver = (CraftServer) Bukkit.getServer();
		PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(this, loc,
				false);
		cserver.getPluginManager().callEvent(respawnEvent);
		if (changedWorlds)
		{
			PlayerChangedWorldEvent pcwe = new PlayerChangedWorldEvent(this,
					loc.getWorld());
			cserver.getPluginManager().callEvent(pcwe);
		}
	}

	@Override
	public Location getLocation()
	{
		return loc;
	}

	@Override
	public boolean isOnline()
	{
		return online;
	}

	public void setOnline(boolean b)
	{
		if (enableMessages)
			Util.sendMessage(this, getName() + " is "
					+ (b ? "connecting" : "disconnecting"));
		online = b;
	}

	@Override
	public boolean isOp()
	{
		return isop;
	}

	@Override
	public void setOp(boolean b)
	{
		isop = b;
	}

	@Override
	public String toString()
	{
		String world = "&5" + this.loc.getWorld().getName() + ",";
		return getName() + "&e h=&2" + getHealth() + "&e o=&5" + isOnline()
				+ "&e d=&7" + isDead() + "&e loc=&4" + world + "&4"
				+ Util.getLocString(loc) + " gm=" + getGameMode();
	}


	@Override
	public void setScoreboard(Scoreboard scoreboard){
		Object s = null;
        this.scoreboard = (CraftScoreboard)scoreboard;
        if (scoreboard != null){
			if (Bukkit.getScoreboardManager().getMainScoreboard() != null &&
					scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard())){
				s = "BukkitMainScoreboard";
			} else if(scoreboard.getObjective(DisplaySlot.SIDEBAR) != null){
				s = scoreboard.getObjective(DisplaySlot.SIDEBAR).getName();
			} else if(scoreboard.getObjective(DisplaySlot.PLAYER_LIST) != null){
				s = scoreboard.getObjective(DisplaySlot.PLAYER_LIST).getName();
			}
		}
		if (enableMessages)
			Util.sendMessage(this, getName() + " setting scoreboard "+ s);
	}

    @Override
    public CraftScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void setLocation(Location l){
		loc = l;
	}


	public Player getInformed(){
		return keepInformed;
	}

	public static void setGlobalMessages(boolean enable){
		VirtualPlayer.enableMessages = enable;
	}
}
