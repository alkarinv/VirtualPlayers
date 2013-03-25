package mc.alk.virtualPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Util
{

	static public String getLocString(Location l)
	{
		if (l == null) return "null";
		StringBuilder sb = new StringBuilder();
		sb.append(((isInt(l.getX())) ? l.getBlockX() : l.getX()) + ",");
		sb.append(((isInt(l.getY())) ? l.getBlockY() : l.getY()) + ",");
		sb.append(((isInt(l.getZ())) ? l.getBlockZ() : l.getZ()));
		if (l.getYaw() != 0f) sb.append("," + l.getYaw());
		if (l.getPitch() != 0f) sb.append("," + l.getPitch());
		return sb.toString();
	}

	public static void sendMessage(VirtualPlayer p, String msg)
	{
		final String finalMsg = colorChat("&5[" + p.getDisplayName() + "] &b'"
				+ msg + "'");
		Bukkit.getConsoleSender().sendMessage(finalMsg);
		if (p.getInformed() != null) p.getInformed().sendMessage(finalMsg);
	}

	public static String colorChat(String msg)
	{
		return msg.replace('&', (char) 167);
	}

	public static boolean isInt(double f)
	{
		return Math.floor(f) == f;
	}

}
