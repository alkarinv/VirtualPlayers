package com.alk.virtualPlayer;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerConfigurationManagerAbstract;
import net.minecraft.server.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class VirtualPlayer extends CraftPlayer {
	boolean online = true;
	ServerConfigurationManagerAbstract scm;
	int health = 20;
	public boolean isop = true;
	public boolean showMessages = true;
	public boolean showTeleports = true;
	Location loc;

	public VirtualPlayer(CraftServer cserver, MinecraftServer mcserver, World world, String s, ItemInWorldManager iiw) {
		super(cserver,new EntityPlayer(mcserver,world,s, iiw));
		this.scm = mcserver.getServerConfigurationManager();
		this.loc = this.getLocation();
	}

	public VirtualPlayer(CraftServer cserver, EntityPlayer ep) {
		super(cserver,ep);
		this.loc = this.getLocation();
	}

	@Override
	public void setHealth(int h){
		this.health = h;
		try {
			super.setHealth(h);
		} catch (Exception e){
		}
		this.health = h;
	}
	@Override
	public boolean isDead(){
		return health <= 0;
	}

	@Override
	public int getHealth(){
		return health;
	}

	@Override
	public void sendMessage(String s){
		if (showMessages) Util.sendMessage(this, (!isOnline() ? "&4(Offline)&b": "")+ getName() + " gettingMessage= "  +s);
	}

	public boolean teleport(Location l, boolean respawn){
		String fromWorld = "";
		String toWorld = "";
		boolean changedWorlds =!this.loc.getWorld().getName().equals(l.getWorld().getName());
		if (changedWorlds){
			fromWorld="&5"+ loc.getWorld().getName()+"&4,";
			toWorld = "&5"+ l.getWorld().getName()+"&4,";
		}
		final String teleporting = respawn? "respawning" : "teleporting";
		if (showTeleports) Util.sendMessage(this, getName() + "&e "+teleporting+" from &4"+fromWorld +Util.getLocString(loc)+
				" &e-> &4" +toWorld+Util.getLocString(l));
		this.loc = l.clone();
		if (changedWorlds){
			PlayerChangedWorldEvent pcwe = new PlayerChangedWorldEvent(this, l.getWorld());
			CraftServer cserver = (CraftServer) Bukkit.getServer();
			cserver.getPluginManager().callEvent(pcwe);
		}
		return true;
	}

	@Override
	public boolean teleport(Location l){
		teleport(l,false);
		super.teleport(l);
		return true;
	}

	public void respawn(Location loc){
		this.health = 20;
		boolean changedWorlds =!this.loc.getWorld().getName().equals(loc.getWorld().getName());
		CraftServer cserver = (CraftServer) Bukkit.getServer();
		PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(this, loc,false);
		cserver.getPluginManager().callEvent(respawnEvent);
		if (changedWorlds){
			PlayerChangedWorldEvent pcwe = new PlayerChangedWorldEvent(this, loc.getWorld());
			cserver.getPluginManager().callEvent(pcwe);			
		}
	}
	@Override
	public Location getLocation(){
		return loc;
	}
	@Override
	public boolean isOnline(){
		return online;
	}

	public void setOnline(boolean b) {
		Util.sendMessage(this, getName() + " is "  +(b? "connecting" : "disconnecting"));
		online = b;
	}

	@Override
	public boolean isOp(){
		return isop;
	}
	public void setOp(boolean b){
		isop= b;
	}
	public String toString(){
		String world = "&5"+this.loc.getWorld().getName()+",";
		return getName() + "&e h=&2" +getHealth() +"&e o=&5" + isOnline() +"&e d=&7" + isDead() +"&e loc=&4"+
		world+"&4"+Util.getLocString(loc);		
	}

	public void setLocation(Location l) {
		loc = l;
	}


}
