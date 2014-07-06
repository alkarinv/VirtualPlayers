package mc.alk.virtualplayers.nms.v1_4_R1;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Util
{
    public interface VPMessageListener{
        public void gettingMessage(Player player, String msg);
    }

    static final List<VPMessageListener> listeners = new ArrayList<VPMessageListener>();

    static public String getLocString(Location l)
	{
		if (l == null) return "null";
		StringBuilder sb = new StringBuilder();
		sb.append((isInt(l.getX())) ? l.getBlockX() : l.getX()).append(",");
		sb.append((isInt(l.getY())) ? l.getBlockY() : l.getY()).append(",");
		sb.append(((isInt(l.getZ())) ? l.getBlockZ() : l.getZ()));
		if (l.getYaw() != 0f) sb.append(",").append(l.getYaw());
		if (l.getPitch() != 0f) sb.append(",").append(l.getPitch());
		return sb.toString();
	}

	public static void sendMessage(VirtualPlayer p, String msg)
	{
		final String finalMsg = colorChat("&5[" + p.getDisplayName() + "] &b'"+ msg + "'");
		Bukkit.getConsoleSender().sendMessage(finalMsg);
        if (!listeners.isEmpty()){
            for (VPMessageListener l: listeners) {
                l.gettingMessage(p, msg);}
        }
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

    public static boolean addListener(VPMessageListener listener) {
        return listeners.add(listener);
    }

    public static boolean removeListener(VPMessageListener listener) {
        return listeners.remove(listener);
    }
}
