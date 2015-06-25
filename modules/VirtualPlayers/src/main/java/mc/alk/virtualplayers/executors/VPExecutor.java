package mc.alk.virtualplayers.executors;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mc.alk.virtualplayers.VirtualPlayers;
import mc.alk.virtualplayers.api.VirtualPlayer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * @author alkarin
 */
public class VPExecutor extends VPBaseExecutor {

    public VPExecutor() {
        super(Bukkit.getPluginManager().getPlugin("VirtualPlayers"));
        useAliasIndex(0);
    }

    public VPExecutor(Plugin plugin) {
        super(plugin);
        useAliasIndex(0);
    }

    @MCCommand(cmds = {"ap", "addPlayers"}, op = true)
    public boolean addPlayer(CommandSender sender, Integer numPlayers) throws Exception {
        for (int i = 0; i < numPlayers; i++) {
            final VirtualPlayer p1 = (VirtualPlayer) VirtualPlayers.makeVirtualPlayer();
            sendMessage(sender, "Added Player " + p1.getName());
        }
        return true;
    }

    @MCCommand(cmds = {"removeAll"}, op = true)
    public boolean removeAll(CommandSender sender) {
        VirtualPlayers.deleteVirtualPlayers();
        return sendMessage(sender, "&2Removed all VirtualPlayers");
    }

    @MCCommand(cmds = {"listPlayers"}, op = true)
    public void listVirtualPlayers(CommandSender sender) {
        List<VirtualPlayer> players = VirtualPlayers.getPlayerList();
        sender.sendMessage("VirtualPlayers count=" + players.size());

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
    public static void setPlayerMessages(boolean visibility) {
        VirtualPlayers.setGlobalMessages(visibility);
    }

    @MCCommand(cmds = {"setEventMessages"}, op = true)
    public static void setEventMessages(boolean visibility) {
        showEventMessages = visibility;
    }

}
