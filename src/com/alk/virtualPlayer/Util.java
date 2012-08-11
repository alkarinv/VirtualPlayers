package com.alk.virtualPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class Util {

	static public String getLocString(Location l){
		if (l == null)
			return "null";
		StringBuilder sb = new StringBuilder();
		sb.append( ((isInt(l.getX())) ? l.getBlockX() : l.getX()) + ",") ;  
		sb.append( ((isInt(l.getY())) ? l.getBlockY() : l.getY()) + ",") ;  
		sb.append( ((isInt(l.getZ())) ? l.getBlockZ() : l.getZ())) ;  
		if (l.getYaw() != 0f) sb.append("," + l.getYaw());
		if (l.getPitch() != 0f) sb.append("," + l.getPitch());
		return sb.toString();
	}
	
	public static void sendMessage(Player p, String msg){
		ConsoleCommandSender sender = Bukkit.getConsoleSender();
		sender.sendMessage(colorChat("&5["+p.getDisplayName()+"] &b'" + msg+"'"));
	}
	
	public static String colorChat(String msg) {
		return msg.replaceAll("&", Character.toString((char) 167));
	}

	public static boolean isInt(double f) {
		return Math.floor(f) == f;
	}

}
