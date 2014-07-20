package mc.alk.virtualplayers.nms.v1_7_R4;

import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * @author alkarin
 */
public class VPExecutor extends VPBaseExecutor {
    public VPExecutor(Plugin plugin) {
        super(Bukkit.getPluginManager().getPlugin("VirtualPlayers"));
        useAliasIndex(0);
    }

    @MCCommand(cmds = {"ap","addPlayers"}, op = true)
    public boolean addPlayer(CommandSender sender, Integer numPlayers) throws Exception
    {
        for (int i = 0; i < numPlayers; i++){
            final VirtualPlayer p1 = (VirtualPlayer) VirtualPlayer.makeVirtualPlayer();
            sendMessage(sender, "Added Player " + p1.getName());
        }
        return true;
    }

    @MCCommand(cmds = {"removeAll"}, op = true)
    public boolean removeAll(CommandSender sender) {
        VirtualPlayer.deleteVirtualPlayers();
        return sendMessage(sender, "&2Removed all VirtualPlayers");
    }

    @MCCommand(cmds = {"listPlayers"}, op = true)
    public void listVirtualPlayers(CommandSender sender) {
        List<VirtualPlayer> players = VirtualPlayer.getPlayerList();
        sender.sendMessage("VirtualPlayers count="+players.size());

        Collections.sort(players, new Comparator<VirtualPlayer>() {
            @Override
            public int compare(VirtualPlayer o1, VirtualPlayer o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (VirtualPlayer vp : players) {
            sendMessage(sender, vp.getName() + " : " + vp);
        }
    }

    @MCCommand(cmds = {"showMessages"}, op = true)
    public boolean showMessages(CommandSender sender) {
        return sendMessage(sender, "&2VirtualPlayer messages &eenabled");
    }


    @MCCommand(cmds = {"hideMessages"}, op = true)
    public boolean hideMessages(CommandSender sender) {
        return sendMessage(sender, "&2VirtualPlayer messages &cdisabled");
    }


    @MCCommand(cmds = {"setMessages"}, op = true)
    public static void setPlayerMessages(boolean show) {
        VirtualPlayer.setGlobalMessages(show);
    }

    @MCCommand(cmds = {"setEventMessages"}, op = true)
    public static void setEventMessages(boolean show) {
        showEventMessages = show;
    }


}
