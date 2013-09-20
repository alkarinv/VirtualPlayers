package mc.alk.virtualPlayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.v1_6_R2.DamageSource;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.MinecraftServer;
import net.minecraft.server.v1_6_R2.PlayerInteractManager;
import net.minecraft.server.v1_6_R2.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_6_R2.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class VirtualPlayers extends JavaPlugin implements Listener
{

	static Map<String, VirtualPlayer> vps = new HashMap<String, VirtualPlayer>();
	static VirtualPlayers plugin;

	@Override
	public void onEnable()
	{
		plugin = this;
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		for (VirtualPlayer vp : vps.values())
		{
			try
			{
				makeVirtualPlayer(vp.getName());
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player p = event.getPlayer();
		if (vps.containsKey(p.getName()) && (p instanceof VirtualPlayer))
		{
			final Location l = event.getRespawnLocation();
			((VirtualPlayer) p).teleport(l, true);
			((VirtualPlayer) p).setOnline(true);
			((VirtualPlayer) p).setHealth(20);
			p.setLastDamageCause(null);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args)
	{
		if (!sender.isOp()) return true;
		final String commandStr = cmd.getName().toLowerCase();
		if (commandStr.equalsIgnoreCase("virtualplayers")){
			if (args.length < 1){
				return false;}
			if (args[0].equalsIgnoreCase("ap")){
				try{
					return addPlayer(sender, args);
				} catch (Exception e) {
					sendMessage(sender, e.getMessage());
					e.printStackTrace();
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				deleteVirtualPlayers();
				return sendMessage(sender, "&2Virtual players removed");
			} else if (args[0].equalsIgnoreCase("showMessages")) {
				VirtualPlayer.setGlobalMessages(true);
				return sendMessage(sender, "&2VirtualPlayer messages &eenabled");
			} else if (args[0].equalsIgnoreCase("hideMessages")) {
				VirtualPlayer.setGlobalMessages(false);
				return sendMessage(sender,
						"&2VirtualPlayer messages &cdisabled");
			}
			sendMessage(sender, "&cVirtualPlayer command &6" + args[0]
					+ "&c not found");
			return false;
		} else if(commandStr.equalsIgnoreCase("vdc")){
			if (args.length < 2) {
				System.out.println("you need more commands");
				return true;
			}
			List<VirtualPlayer> vps = getVps(sender, args[0]);
			for (VirtualPlayer vp: vps){
				onCommand(sender, args, vp);
			}
		}
		return true;
	}

	private List<VirtualPlayer> getVps(CommandSender sender, String names) {
		String name = names;
		List<VirtualPlayer> players = new ArrayList<VirtualPlayer>();
		if (names.contains("-")){
			String[] split = names.split("-");
			name = split[0];
			Integer start = Integer.valueOf(split[1]);
			Integer end = Integer.valueOf(split[2]);
			for (int i=start;i<=end;i++){
				players.add(getOrCreate(sender,name+i));
			}
		} else {
			players.add(getOrCreate(sender, name));
		}
		return players;
	}

	private VirtualPlayer getOrCreate(CommandSender sender, String name) {
		Player vp = vps.get(name);
		if (vp == null) {
			try{
				vp = makeVirtualPlayer(name);
			} catch (Exception e) {
				sendMessage(sender, e.getMessage());
				e.printStackTrace();
			}
			vps.put(vp.getName(), (VirtualPlayer) vp);
		}
		return (VirtualPlayer) vp;
	}

	private boolean onCommand(CommandSender sender, String[] args,VirtualPlayer vp) {
		if (args[1].equalsIgnoreCase("disconnect")
				|| args[1].equalsIgnoreCase("dc")
				|| args[1].equalsIgnoreCase("connect")){
			return playerConnection(sender, vp,
					args[1].equalsIgnoreCase("connect"));
		} else if (args[1].equalsIgnoreCase("respawn")) {
			return playerRespawn(sender, vp);
		} else if (args[1].equalsIgnoreCase("movetome")){
			return moveVirtualPlayerToMe(sender, vp);
		} else if (args[1].equalsIgnoreCase("status")){
			return sendMessage(sender, "&4" + vp);
		} else if (args[1].equalsIgnoreCase("deop")) {
			return opPlayer(sender, vp, false);
		} else if (args[1].equalsIgnoreCase("op")) {
			return opPlayer(sender, vp, true);
		} else if (args[1].equalsIgnoreCase("showMessages")) {
			vp.showMessages = !vp.showMessages;
			return sendMessage(sender, "&6" + vp.getName()
					+ "&2 showingMessages = &6" + vp.showMessages);
		} else if (args[1].equalsIgnoreCase("showMessages")) {
			vp.showTeleports = !vp.showTeleports;
			return sendMessage(sender, "&6" + vp.getName()
					+ "&2 showingTeleports = &6" + vp.showTeleports);
		}
		if (!vp.isOnline()){
			return sendMessage(sender, "&6" + vp.getName() + "&4 is offline!!");}
		if (args[1].equalsIgnoreCase("kill")){
			return playerKill(vp);}
		else if (args[1].equalsIgnoreCase("inv")){
			return printInv(sender, vp);
		} else if (args[1].equalsIgnoreCase("gamemode")
				|| args[1].equalsIgnoreCase("gm")){
			return setGameMode(sender, vp, args[2]);
		} else if (args[1].equalsIgnoreCase("givehelm")) {
			return giveHelm(vp, args[2]);
		} else if (args[1].equalsIgnoreCase("giveinv")) {
			return giveInv(vp, args[2]);
		} else if (args[1].equalsIgnoreCase("health")) {
			return health(sender, vp, args);
		} else if (args[1].equalsIgnoreCase("BlockPlaceEvent")
				|| args[1].equalsIgnoreCase("bpe")){
			return blockPlaceEvent(sender, vp, args);
		} else if (args[1].equalsIgnoreCase("BlockBreakEvent")
				|| args[1].equalsIgnoreCase("bbe")){
			return blockBreakEvent(sender, vp, args);
		} else if (args[1].equalsIgnoreCase("OpenInventoryEvent")
				|| args[1].equalsIgnoreCase("oie")){
			return openInventoryEvent(sender, vp, args);
		} else if (args[1].equalsIgnoreCase("tp")) {
			return teleportPlayer(sender, vp, args);
		} else if (args[1].equalsIgnoreCase("chat")) {
			return chatEvent(sender, vp, args);
		} else if (args[1].equalsIgnoreCase("hit")) {
			return damageEvent(sender, vp, args);
		} else if (args[1].equalsIgnoreCase("interact") || args[1].equalsIgnoreCase("click")) {
			return interactEvent(sender, vp, args);
		} else if (args[1].equalsIgnoreCase("examine")) {
			return examine(sender, vp, args);
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < args.length; i++){
			sb.append(args[i]);
			if (i < args.length - 1) {
				sb.append(" ");}
		}
		final String command = sb.toString();
		sendMessage(sender, "&2Executing '&6" + command
				+ "&2' for player '&6" + vp.getName() + "&2'");
		PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(
				vp, "/" + command);
		Bukkit.getPluginManager().callEvent(pcpe);
		if (pcpe.isCancelled()){
			return sendMessage(sender, "&cCommand cancelled : &6" + command);}
		this.getServer().dispatchCommand(vp, command);
		return true;
	}

	public static void setPlayerMessages(boolean show){
		VirtualPlayer.setGlobalMessages(show);
	}

	private boolean moveTurnPlayer(CommandSender sender, VirtualPlayer vp)
	{
		if (!(sender instanceof Player))
		{
			sendMessage(sender, "You must be a player to use this!");
			return false;
		}
		Player player = (Player) sender;
		sendMessage(player, "&6" + vp.getName() + " is now trying to come to you!");
		vp.moveTo(player.getLocation());
		return true;
	}


	private boolean moveVirtualPlayerToMe(CommandSender sender, VirtualPlayer vp)
	{
		if (!(sender instanceof Player))
		{
			sendMessage(sender, "You must be a player to use this!");
			return false;
		}
		Player player = (Player) sender;
		sendMessage(player, "&6" + vp.getName() + " is now trying to come to you!");
		vp.moveTo(player.getLocation());
		return true;
	}

	private boolean opPlayer(CommandSender sender, VirtualPlayer vp, boolean op)
	{
		vp.setOp(op);
		String opped = op ? "opped" : "deopped";
		return sendMessage(sender, "&6" + vp.getName() + "&2 " + opped + "!");
	}

	private boolean interactEvent(CommandSender sender, VirtualPlayer vp, String[] args){
		if (args.length < 4){
			return sendMessage(sender, "dc <player> interact <left|right> <location>");
		}
		// World w = vp.getWorld();
		Location l = parseLocation(sender, args[3]);
		if (l == null) return true;
		boolean left = args[2].equalsIgnoreCase("left");
		Action action;
		if (l.getBlock().getType() == Material.AIR){
			action = left ? Action.LEFT_CLICK_AIR : Action.RIGHT_CLICK_AIR;
		} else {
			action = left ? Action.LEFT_CLICK_BLOCK : Action.RIGHT_CLICK_BLOCK;
		}
		ItemStack inhand = vp.getItemInHand();
		// Block b = w.getBlockAt(l);
		Block b = l.getWorld().getBlockAt(l);
		// PlayerInteractEvent(final Player who, final Action action, final
		// ItemStack item,
		// final Block clickedBlock, final BlockFace clickedFace) {
		PlayerInteractEvent ede = new PlayerInteractEvent(vp, action, inhand, b, BlockFace.EAST);
		Bukkit.getPluginManager().callEvent(ede);
		sendMessage(sender, "&6" + vp.getName() + "&e " + action.name() + " &4"
				+ b.getType() + "&2  with a &a" + inhand.getType().name());
		if (b.getType() == Material.SIGN || b.getType() == Material.SIGN_POST)
		{
			Sign s = (Sign) b.getState();
			String[] lines = s.getLines();
			for (int i = 1; i <= lines.length; i++)
			{
				sendMessage(sender, "&6Line: " + i + "=&f'" + lines[i - 1] + "&f'");
			}
		}
		return true;
	}

	private boolean examine(CommandSender sender, VirtualPlayer vp, String[] args){
		if (args.length < 3){
			return sendMessage(sender, "dc <player> examine <location>");
		}
		Location l = parseLocation(sender, args[2]);
		if (l == null) return true;

		Block b = l.getWorld().getBlockAt(l);

		sendMessage(sender, "&6" + vp.getName() + "&e examining location "+args[2]+" &4"+ b.getType());
		if (b.getType() == Material.SIGN || b.getType() == Material.SIGN_POST)
		{
			Sign s = (Sign) b.getState();
			String[] lines = s.getLines();
			for (int i = 1; i <= lines.length; i++)
			{
				sendMessage(sender, "&6Line: " + i + "=&f'" + lines[i - 1] + "&f'");
			}
		}
		return true;
	}

	private boolean teleportPlayer(CommandSender sender, VirtualPlayer vp, String[] args)
	{
		Location l = parseLocation(sender, args[2]);
		if (l == null) return true;
		PlayerTeleportEvent pte = new PlayerTeleportEvent(vp, vp.getLocation(),
				l, TeleportCause.COMMAND);
		Bukkit.getPluginManager().callEvent(pte);
		if (pte.isCancelled()){
			sendMessage(sender, "&cTeleport of " + vp.getDisplayName()+ " was cancelled by some plugin");
		} else {
			vp.teleport(l);
		}
		return true;
	}


	private boolean setGameMode(CommandSender sender, VirtualPlayer vp, String string){
		GameMode gm = null;
		try
		{
			gm = GameMode.valueOf(string);
		} catch (Exception e)
		{
		}
		if (gm == null)
		{
			try
			{
				gm = GameMode.getByValue(Integer.valueOf(string));
			} catch (Exception e)
			{
			}
			if (gm == null)
			{
				sendMessage(sender, "&cGamemode " + string + " not found");
				return true;
			}
		}
		vp.setGameMode(gm);
		sendMessage(sender, "&6" + vp.getName() + "&2 gamemode &6" + string);
		return true;
	}

	private boolean chatEvent(CommandSender sender, final VirtualPlayer vp, String[] args){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i = 2; i < args.length; i++)
		{
			if (!first) sb.append(" ");
			sb.append(args[i]);
			first = false;
		}
		final String msg = Util.colorChat(sb.toString());
		final HashSet<Player> players = new HashSet<Player>(
				Arrays.asList(Bukkit.getOnlinePlayers()));
		players.addAll(vps.values());
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				AsyncPlayerChatEvent apce = new AsyncPlayerChatEvent(true, vp,
						msg, players);
				Bukkit.getPluginManager().callEvent(apce);
			}
		};
		new Thread(r).start();

		sendMessage(sender, "&6" + vp.getName() + "&2 said " + msg);
		return true;
	}

	@EventHandler
	public void onAsyncChatEvent(AsyncPlayerChatEvent event)
	{
		if (!vps.containsKey(event.getPlayer().getName())) // / don't need to
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

	private boolean playerRespawn(CommandSender sender, VirtualPlayer vp)
	{
		World w = vp.getWorld();
		vp.respawn(w.getSpawnLocation());
		return sendMessage(sender, "&6" + vp.getName() + "&2 respawned!");
	}

	private boolean openInventoryEvent(CommandSender sender, VirtualPlayer vp, String[] args)
	{
		if (args.length < 7)
		{
			return sendMessage(
					sender,
					Util.colorChat("&6usage: /dc <player> oie <world> <x> <y> <z>"));
		}
		return false;
	}

	private boolean blockPlaceEvent(CommandSender sender, VirtualPlayer vp, String[] args)
	{
		if (args.length < 4)
		{
			sender.sendMessage(Util
					.colorChat("&6usage: /dc <player> bpe <block> <location>"));
			return true;
		}
		ItemStack is = InventoryUtil.getItemStack(args[2]);
		if (is == null)
		{
			return sendMessage(sender, "&cCouldn't parse block &6" + args[2]);
		}
		Location loc = null;
		try
		{
			loc = getLocation(args[3]);
		} catch (Exception e)
		{
		}
		if (loc == null)
		{
			return sendMessage(sender, "&cCouldn't parse location &6" + args[3]);
		}

		Block replaced = loc.getBlock();
		Material old = replaced.getType();
		replaced.setType(is.getType());
		BlockPlaceEvent bpe = new BlockPlaceEvent(replaced,
				replaced.getState(), replaced.getRelative(BlockFace.NORTH),
				vp.getItemInHand(), vp, true);
		Bukkit.getPluginManager().callEvent(bpe);
		if (bpe.isCancelled())
		{
			replaced.setType(old);
			return sendMessage(sender, "&cBlockPlaceEvent was cancelled for &6"
					+ vp.getName());
		}
		else
		{
			return sendMessage(sender,
					"&6" + vp.getName() + "&e placed " + is.getType() + " on "
							+ old + "  at &4" + Util.getLocString(loc));
		}
	}

	private boolean blockBreakEvent(CommandSender sender, VirtualPlayer vp,
			String[] args)
	{
		if (args.length < 3)
		{
			sender.sendMessage(Util
					.colorChat("&6usage: /dc <player> bbe <location>"));
			return true;
		}
		Location loc = null;
		try
		{
			loc = getLocation(args[2]);
		} catch (Exception e)
		{
		}
		if (loc == null)
		{
			return sendMessage(sender, "&cCouldn't parse location &6" + args[2]);
		}

		Block replaced = loc.getBlock();
		Material old = replaced.getType();
		BlockBreakEvent bpe = new BlockBreakEvent(replaced, vp);
		Bukkit.getPluginManager().callEvent(bpe);
		if (bpe.isCancelled())
		{
			return sendMessage(sender, "&cBlockPlaceEvent was cancelled for &6"
					+ vp.getName());
		}
		else
		{
			replaced.setType(Material.AIR);
			return sendMessage(sender, "&6" + vp.getName() + "&e broke the "
					+ old + " at &4" + Util.getLocString(loc));
		}
	}

	private boolean addPlayer(CommandSender sender, String[] args)
			throws Exception
			{
		int n = 1;
		if (args.length > 1)
		{
			try
			{
				n = Integer.valueOf(args[1]);
			} catch (Exception e)
			{
			}
		}
		for (int i = 0; i < n; i++)
		{
			final VirtualPlayer p1 = (VirtualPlayer) makeVirtualPlayer("p"
					+ (vps.size() + 1));
			vps.put(p1.getName(), p1);
			sendMessage(sender, "Added Player " + p1.getName());
		}
		return true;
			}

	private static boolean sendMessage(CommandSender sender, String string)
	{
		if (string == null) return true;
		sender.sendMessage(Util.colorChat(string));
		return true;
	}

	private static boolean sendMessage(Player player, String string)
	{
		if (string == null) return true;
		player.sendMessage(Util.colorChat(string));
		return true;
	}

	private boolean giveInv(VirtualPlayer vp, String string)
	{
		ItemStack is = InventoryUtil.getItemStack(string);
		int amount = is.getAmount() > 0 ? is.getAmount() : 1;
		InventoryUtil.addItemToInventory(vp.getInventory(), is, amount);
		return true;
	}

	private boolean giveHelm(VirtualPlayer vp, String string)
	{
		ItemStack is = InventoryUtil.getItemStack(string);
		vp.getInventory().setHelmet(is);
		return true;
	}

	private boolean printInv(CommandSender sender, VirtualPlayer vp)
	{
		PlayerInventory inv = vp.getInventory();
		if (inv != null)
		{
			sendMessage(sender,
					"&bHelm:&6" + InventoryUtil.getItemString(inv.getHelmet()));
			sendMessage(
					sender,
					"&bChest:&6"
							+ InventoryUtil.getItemString(inv.getChestplate()));
			sendMessage(
					sender,
					"&bLegs:&6"
							+ InventoryUtil.getItemString(inv.getLeggings()));
			sendMessage(sender,
					"&bBoot:&6" + InventoryUtil.getItemString(inv.getBoots()));
			sendMessage(
					sender,
					"&4InHand:&6"
							+ InventoryUtil.getItemString(inv.getItemInHand()));
			ItemStack[] contents = inv.getContents();
			for (int i = 0; i < contents.length; i++)
			{
				ItemStack is = contents[i];
				if (is != null && is.getType() != Material.AIR)
					sendMessage(sender,
							"&2" + i + ":&e" + InventoryUtil.getItemString(is));
			}
		}
		return true;
	}

	private boolean health(CommandSender sender, VirtualPlayer damagee, String[] args) {
		if (args.length < 3){
			return sendMessage(sender,"&cusage: &6/dc <virtual player> health <int>");}
		Double h = Double.valueOf(args[2]);
		if (h == null){
			return sendMessage(sender, "&cHealth has to be an integer  " + args[2]);}

		EntityDamageEvent ede = CraftEventFactory.callEntityDamageEvent(
				null,
				((CraftPlayer) damagee).getHandle(),
				EntityDamageEvent.DamageCause.ENTITY_ATTACK, damagee.getHealth()-h);
		if (!ede.isCancelled()){
			damagee.setLastDamageCause(ede);
			EntityPlayer ep = ((CraftPlayer)damagee).getHandle();
			if (damagee instanceof VirtualPlayer){
				damagee.setHealth(damagee.getHealth()-ede.getDamage());
			} else {
				ep.damageEntity(DamageSource.GENERIC, (float)ede.getDamage());

			}
			sendMessage(sender, "&6" + damagee.getName() + "&e health now &4" + damagee.getHealth());
			return sendMessage(sender, "&6"+ damagee.getName() + "&2, life=&4"
					+ damagee.getHealth());

		} else {
			return sendMessage(sender, "&cHealth set was cancelled for &6"+ damagee.getName());
		}
	}

	private boolean damageEvent(CommandSender sender, VirtualPlayer vp, String[] args)
	{
		if (args.length < 2){
			return sendMessage(sender,
					"&cUsage: &6/dc <virtual player> hit <player2> [damage: default 5]");}
		Player damagee = getPlayer(args[2]);
		if (damagee == null){
			return sendMessage(sender, "Couldn't find player " + args[2]);}

		if (damagee.getHealth() <= 0){
			return sendMessage(sender, "&6" + damagee.getName()+ "&c is already dead!");}
		Integer damage = 5;
		if (args.length > 2){
			try{damage = Integer.valueOf(args[3]);} catch (Exception e){}}
		Player damager = getPlayer(vp.getName()); // / Try to get a real damager
		EntityDamageEvent ede = CraftEventFactory.callEntityDamageEvent(
				((CraftPlayer) damager).getHandle(),
				((CraftPlayer) damagee).getHandle(),
				EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage);
		if (!ede.isCancelled()){
			damagee.setLastDamageCause(ede);
			EntityPlayer ep = ((CraftPlayer)damagee).getHandle();
			if (damagee instanceof VirtualPlayer){
				((VirtualPlayer)damagee).setHealth(damagee.getHealth()-ede.getDamage());
			} else {
				ep.damageEntity(DamageSource.GENERIC, (float)ede.getDamage());
			}
			return sendMessage(sender, "&6" + vp.getName() + "&2 hit &6"
					+ damagee.getName() + "&2 for &6" + damage + "&2, life=&4"
					+ damagee.getHealth());
		} else {
			return sendMessage(sender, "&cDamage Event was cancelled for &6"
					+ vp.getName() + "&c hitting &6" + damagee.getName());
		}
	}

	private boolean playerKill(VirtualPlayer vp)
	{
		CraftServer cserver = (CraftServer) Bukkit.getServer();
		List<ItemStack> is = new LinkedList<ItemStack>();
		vp.setHealth(0);
		vp.damage(50);
		PlayerDeathEvent ede = new PlayerDeathEvent(vp, is, 0, "");
		cserver.getPluginManager().callEvent(ede);
		return true;
	}

	private boolean playerConnection(CommandSender sender, final VirtualPlayer vp, boolean connecting)
	{
		vp.setOnline(connecting);
		if (connecting)
		{
			// PreLogin Event has to be called from a thread other than the
			// main.
			final String playerName = vp.getName();
			Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					try{
						AsyncPlayerPreLoginEvent playerPreLoginEvent = new AsyncPlayerPreLoginEvent(
								playerName, InetAddress.getLocalHost());
						Bukkit.getPluginManager()
						.callEvent(playerPreLoginEvent);
					} catch (UnknownHostException ex){}
					/// After we are done Asynchronously handling the
					/// preloginevent
					/// Sync back up and call the Login and Join events
					Bukkit.getScheduler().scheduleSyncDelayedTask(
							VirtualPlayers.getSelf(), new Runnable(){
								@Override
								public void run()
								{
									// Then the Login Event.
									CraftServer cserver = (CraftServer) Bukkit.getServer();
									try
									{
										PlayerLoginEvent playerLoginEvent = new PlayerLoginEvent(
												vp, "localhost", InetAddress
												.getLocalHost());
										cserver.getPluginManager().callEvent(
												playerLoginEvent);
									} catch (UnknownHostException ex){}

									// Finally, the player join event.
									PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(
											vp, "\u00A7e" + vp.getName()
											+ " joined the game.");
									cserver.getPluginManager().callEvent( playerJoinEvent);
								}
							});

				}
			};
			new Thread(r).start();
		}
		else
		{ /// Disconnecting
			CraftServer cserver = (CraftServer) Bukkit.getServer();
			PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(vp, "\u00A7e"
					+ vp.getName() + " left the game.");
			cserver.getPluginManager().callEvent(playerQuitEvent);
		}
		sendMessage(sender, "&6" + vp.getName() + "&2 "+
				(connecting ? "connecting.  Details:&6"+vp : "&cdisconnecting"));
		return true;
	}

	public static Player makeVirtualPlayer() throws Exception
	{
		return makeVirtualPlayer("p" + (vps.size() + 1));
	}


	public static Player makeVirtualPlayer(String name) throws Exception
	{
		CraftServer cserver = (CraftServer) Bukkit.getServer();
		List<World> worlds = cserver.getWorlds();
		if (worlds == null || worlds.isEmpty())
			throw new Exception("There must be at least one world");
		CraftWorld w = (CraftWorld) worlds.get(0);
		Location location = new Location(w, 0, 0, 0);
		MinecraftServer mcserver = cserver.getServer();
		WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
		WorldServer worldserver = mcserver.getWorldServer(0);
		PlayerInteractManager pim = new PlayerInteractManager(worldserver);
		VirtualPlayer vp = new VirtualPlayer(cserver, mcserver, world, name,pim);
		vp.loc = location;
		vps.put(vp.getName(), vp);
		return vp;
	}

	public static void deleteVirtualPlayers()
	{
		for (VirtualPlayer vp : vps.values())
		{
			WorldServer world = ((CraftWorld) vp.getLocation().getWorld())
					.getHandle();
			world.removeEntity(vp.getHandle());
			vp.remove();
		}
		vps.clear();
	}

	public static VirtualPlayer deleteVirtualPlayer(VirtualPlayer vp)
	{
		WorldServer world = ((CraftWorld) vp.getLocation().getWorld())
				.getHandle();
		world.removeEntity(vp.getHandle());
		vps.remove(vp.getName());
		vp.remove();
		return vp;
	}

	public static Player getPlayer(String pname)
	{
		Player vp = Bukkit.getPlayer(pname);
		if (vp == null) vp = vps.get(pname);
		return vp;
	}

	public static Player getOrMakePlayer(String pname)
	{
		Player vp = Bukkit.getPlayer(pname);
		if (vp == null) vp = vps.get(pname);
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
		Set<Player> players = new HashSet<Player>();
		for (Player p : vps.values())
		{
			if (p.isOnline())
			{
				players.add(p);
			}
		}
		return players.toArray(new Player[players.size()]);
	}

	private static Location parseLocation(CommandSender sender, String strloc)
	{
		try{
			Location l = getLocation(strloc);
			if (l == null){
				sendMessage(sender, "Location " + strloc + " was not found");}
			return l;
		} catch (Exception e){
			sendMessage(sender, e.getMessage());
		}
		return null;

	}

	static Location getLocation(String locstr) throws Exception
	{
		// String loc = node.getString(nodestr,null);
		if (locstr == null) return null;
		String split[] = locstr.split(",");
		float x, y, z;
		float yaw = 0, pitch = 0;
		String w = null;
		if (split.length == 3)
		{
			// w = Bukkit.getWorlds().get(0);
			x = Float.valueOf(split[0]);
			y = Float.valueOf(split[1]);
			z = Float.valueOf(split[2]);
		}
		else if (split.length > 3 && split.length <= 6)
		{
			w = split[0];
			x = Float.valueOf(split[1]);
			y = Float.valueOf(split[2]);
			z = Float.valueOf(split[3]);
			if (split.length > 4)
			{
				yaw = Float.valueOf(split[4]);
			}
			if (split.length > 5)
			{
				pitch = Float.valueOf(split[5]);
			}
		}
		else
		{
			throw new Exception(
					"You must specify a world and coords or just coords: Example world,5,6,7");
		}
		World world = null;
		if (w != null) world = Bukkit.getWorld(w);
		if (world == null || x == -1 || y == -1 || z == -1)
		{
			return null;
		}
		return new Location(world, x, y, z, yaw, pitch);
	}

}
