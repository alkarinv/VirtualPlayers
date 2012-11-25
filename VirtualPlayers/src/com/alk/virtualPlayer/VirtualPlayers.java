package com.alk.virtualPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class VirtualPlayers extends JavaPlugin implements Listener{

	static int pcount = 0;

	static Map<String,VirtualPlayer> vps = new HashMap<String,VirtualPlayer>();

	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		for (VirtualPlayer vp: vps.values()){
			makeVirtualPlayer(vp.getName());
		}
	}

	@Override
	public void onDisable() {
		deleteVirtualPlayers();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player p = event.getPlayer();
		if (vps.containsKey(p.getName())){
			final Location l = event.getRespawnLocation();
			((VirtualPlayer)p).teleport(l, true);
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		final String commandStr = cmd.getName().toLowerCase();

		if(commandStr.equalsIgnoreCase("ap")){
			return addPlayer(sender,args);
		} else if(commandStr.equalsIgnoreCase("dc")){
			if (args.length < 2){
				System.out.println("you need more commands");
				return true;
			}
			VirtualPlayer vp = vps.get(args[0]);
			if (vp == null){
				vp = makeVirtualPlayer(args[0]);
				vps.put(vp.getName(), vp);
			}
			if (args[1].equalsIgnoreCase("disconnect") || args[1].equalsIgnoreCase("dc")
					|| args[1].equalsIgnoreCase("connect")){
				return playerConnection(vp,args[1].equalsIgnoreCase("connect") );
			} else if (args[1].equalsIgnoreCase("respawn")){
				return playerRespawn(sender,vp);
			} else if (args[1].equalsIgnoreCase("status")){
				sendMessage(sender, "&4"+vp);
				return true;
			} else if (args[1].equalsIgnoreCase("deop")){
				vp.setOp(false);
				sendMessage(sender, "&6"+vp.getName() +"&2 deopped!");
				return true;
			} else if (args[1].equalsIgnoreCase("op")){
				vp.setOp(true);
				sendMessage(sender, "&6"+vp.getName() +"&2 opped!");
				return true;
			} 
			if (!vp.isOnline()){
				sendMessage(sender, "&6"+vp.getName()+"&4 is offline!!");
				return true;
			}
			if (args[1].equalsIgnoreCase("kill")){
				return playerKill(vp);
			} else if (args[1].equalsIgnoreCase("inv")){
				return printInv(sender,vp);
			} else if (args[1].equalsIgnoreCase("giveinv")){
				return giveInv(vp,args[2]);
			} else if (args[1].equalsIgnoreCase("health")){
				int h = Integer.valueOf(args[2]);
				sendMessage(sender, "&6"+vp.getName() +"&e health now &4" + h);
				vp.setHealth(h);
				return true;
			} else if (args[1].equalsIgnoreCase("BlockPlaceEvent") || args[1].equalsIgnoreCase("bpe")){
				return blockPlaceEvent(sender, vp,args);
			} else if (args[1].equalsIgnoreCase("OpenInventoryEvent") || args[1].equalsIgnoreCase("oie")){
				return openInventoryEvent(sender, vp,args);
			} else if (args[1].equalsIgnoreCase("tp")){
				try {
					vp.teleport(getLocation(args[2]));
				} catch (Exception e) {
					sendMessage(sender,e.getMessage());
				}
				return true;
			} else if (args[1].equalsIgnoreCase("chat")){
				return chatEvent(sender, vp,args);
			} 

			StringBuilder sb = new StringBuilder();
			for (int i=1;i<args.length;i++){
				sb.append(args[i]);
				if (i < args.length-1){
					sb.append(" ");
				}
			}
			System.out.println("Executing '" + sb.toString() + "' for player '" +vp.getName()+"'");
			this.getServer().dispatchCommand(vp, sb.toString());
			return true;
		}
		return true;
	}

	private boolean chatEvent(CommandSender sender, final VirtualPlayer vp, String[] args) {
		StringBuilder sb = new StringBuilder();
		for (int i=2;i<args.length;i++){
			sb.append(args[i]);
		}
		final String msg = Util.colorChat(sb.toString());
		final HashSet<Player> players = new HashSet<Player>(Arrays.asList(Bukkit.getOnlinePlayers()));
		players.addAll(vps.values());
		Runnable r = new Runnable(){
			@Override
			public void run() {
				AsyncPlayerChatEvent apce = new AsyncPlayerChatEvent(true, vp,msg, players);
				Bukkit.getPluginManager().callEvent(apce);
			}
		};
		new Thread(r).start();

		sendMessage(sender, "&6"+vp.getName() +"&2 said " + msg);
		return true;
	}
	
	@EventHandler
	public void onAsyncChatEvent(AsyncPlayerChatEvent event){
		if (!vps.containsKey(event.getPlayer().getName())) /// don't need to handle it
			return;
		/// For some reason we do need to actually send the messages from virtualplayers ourself
        final String message = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
		for (Player p : event.getRecipients()){
			p.sendMessage(message);
		}
	}
	
	private boolean playerRespawn(CommandSender sender, VirtualPlayer vp) {
		World w = vp.getWorld();
		vp.respawn(w.getSpawnLocation());
		return true;
	}

	private boolean openInventoryEvent(CommandSender sender, VirtualPlayer vp, String[] args) {
		if (args.length < 7){
			sender.sendMessage(Util.colorChat("&6usage: /dc <player> oie <world> <x> <y> <z>"));
			return true;
		}
		return false;
	}

	private boolean blockPlaceEvent(CommandSender sender, VirtualPlayer vp, String[] args) {
		if (args.length < 7){
			sender.sendMessage(Util.colorChat("&6usage: /dc <player> bpe <world> <x> <y> <z>"));
			return true;
		}
		/// TODO complete 
		//		ItemStack is = InventoryUtil.getItemStack(args[1]);
		//		Integer x = Integer.parseInt(args[3]), y = Integer.parseInt(args[4]), z = Integer.parseInt(args[5]);
		//		World w = Bukkit.getWorld(args[2]);
		//		Block replaced = w.getBlockAt(x,y,z); q	
		//		Block b = w.getBlockAt(x,y,z); 
		//		BlockState bs = Mate
		//        BlockState blockState = CraftBlockState.getBlockState(w,x,y,z); // CraftBukkit

		//	    callBlockPlaceEvent(World world, EntityHuman who, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ) {
		//		CraftEventFactory.callBlockPlaceEvent(w, vp, BlockState., clickedX, clickedY, clickedZ);
		//		BlockPlaceEvent bpe =new BlockPlaceEvent(b, replaced, b, Material.AIR, (Player) vp, true);
		return false;
	}

	private boolean addPlayer(CommandSender sender,String[] args) {
		int n = 1;
		if (args.length > 0){
			try {n = Integer.valueOf(args[0]);}catch(Exception e){}
		}
		for (int i=0;i<n;i++){
			final VirtualPlayer p1 = makeVirtualPlayer("p" + (pcount++));
			vps.put(p1.getName(),p1);
			sendMessage(sender,"Added Player " + p1.getName());				
		}
		return true;
	}


	private void sendMessage(CommandSender sender, String string) {
		sender.sendMessage(Util.colorChat(string));
	}

	private boolean giveInv(VirtualPlayer vp, String string) {
		ItemStack is = InventoryUtil.getItemStack(string);
		int amount = is.getAmount() >0 ? is.getAmount() : 1;
		InventoryUtil.addItemToInventory(vp.getInventory(), is, amount);
		return true;
	}

	private boolean printInv(CommandSender sender, VirtualPlayer vp) {
		PlayerInventory inv = vp.getInventory();
		if (inv != null){
			sendMessage(sender, "&bHelm:&6" + InventoryUtil.getItemString(inv.getHelmet()));
			sendMessage(sender, "&bChest:&6" + InventoryUtil.getItemString(inv.getChestplate()));
			sendMessage(sender, "&bLegs:&6" + InventoryUtil.getItemString(inv.getLeggings()));
			sendMessage(sender, "&bBoot:&6" + InventoryUtil.getItemString(inv.getBoots()));
			sendMessage(sender, "&4InHand:&6" + InventoryUtil.getItemString(inv.getItemInHand()));
			ItemStack[] contents = inv.getContents();
			for (int i = 0;i<contents.length;i++){
				ItemStack is = contents[i];
				if (is != null && is.getType() != Material.AIR )
					sendMessage(sender, "&2"+i+":&e" +InventoryUtil.getItemString(is));
			}
		}
		return true;
	}

	private boolean playerKill(VirtualPlayer vp) {
		CraftServer cserver = (CraftServer) Bukkit.getServer();
		List<ItemStack> is = new LinkedList<ItemStack>();
		vp.setHealth(0);
		vp.damage(50);
		EntityDeathEvent ede = new EntityDeathEvent(vp,is);
		cserver.getPluginManager().callEvent(ede);						
		return true;
	}

	private boolean playerConnection(VirtualPlayer vp, boolean connecting) {
		vp.setOnline(connecting);
		CraftServer cserver = (CraftServer) Bukkit.getServer();
		if (connecting){
			PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(vp, "\u00A7e" + vp.getName()+ " joined the game.");
			cserver.getPluginManager().callEvent(playerJoinEvent);						
		} else {
			PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(vp, "\u00A7e" + vp.getName()+ " left the game.");
			cserver.getPluginManager().callEvent(playerQuitEvent);			
		}

		return true;
	}

	public static VirtualPlayer makeVirtualPlayer(String name){
		CraftServer cserver = (CraftServer) Bukkit.getServer();
		CraftWorld w = (CraftWorld) cserver.getWorld("World");
		Location location = new Location(w, 0,0,0);
		MinecraftServer mcserver = cserver.getServer();
		net.minecraft.server.World world = ((CraftWorld) location.getWorld()).getHandle();

		WorldServer worldserver = mcserver.getWorldServer(0);
		ItemInWorldManager iiw = new ItemInWorldManager(worldserver);
		VirtualPlayer vp = new VirtualPlayer(cserver,mcserver,world, name, iiw);
		vp.loc = location;
		vps.put(vp.getName(),vp);

		return vp;
	}	

	public static void deleteVirtualPlayers(){
		for (VirtualPlayer vp: vps.values()){
			net.minecraft.server.World world = ((CraftWorld) vp.getLocation().getWorld()).getHandle();
			world.removeEntity(vp.getHandle());
		}
	}
	public static VirtualPlayer deleteVirtualPlayer(VirtualPlayer vp){
		net.minecraft.server.World world = ((CraftWorld) vp.getLocation().getWorld()).getHandle();
		world.removeEntity(vp.getHandle());
		vps.remove(vp.getName());		
		return vp;
	}

	public static VirtualPlayer getPlayer(String pname){
		return vps.get(pname);
	}

	public static Player[] getOnlinePlayers(){
		Set<Player> players = new HashSet<Player>();
		for (Player p: vps.values()){
			if (p.isOnline()){
				players.add(p);
			}
		}
		return (Player[]) players.toArray(new Player[players.size()]);
	}

	static Location getLocation(String locstr) throws Exception {
		//		String loc = node.getString(nodestr,null);
		if (locstr == null)
			return null;
		String split[] = locstr.split(",");
		float x,y,z;
		String w = null;
		if (split.length == 4){
			w = split[0];
			x = Float.valueOf(split[1]);
			y = Float.valueOf(split[2]);
			z = Float.valueOf(split[3]);
		} else if (split.length == 3){
			//			w = Bukkit.getWorlds().get(0);
			x = Float.valueOf(split[0]);
			y = Float.valueOf(split[1]);
			z = Float.valueOf(split[2]);			
		} else {
			throw new Exception("You must specify a world and coords or just coords: Example world,5,6,7");
		}
		float yaw = 0, pitch = 0;
		if (split.length > 4){yaw = Float.valueOf(split[4]);}
		if (split.length > 5){pitch = Float.valueOf(split[5]);}
		World world = null;
		if (w != null)
			world = Bukkit.getWorld(w);
		if (world ==null || x == -1 || y==-1 || z==-1){
			return null;}
		return new Location(world,x,y,z,yaw,pitch);
	}

}
