package me.szumielxd.VanishNoPacketBungeeIntegration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.szumielxd.VanishNoPacketBungeeIntegration.commands.MainCommand;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class VanishNoPacketBungeeIntegration extends Plugin implements Listener {
	
	
	public static final String VANISH_PLUGIN_CHANNEL = "vnp:state";
	private ArrayList<ProxiedPlayer> vanished = new ArrayList<>();
	private static VanishNoPacketBungeeIntegration instance = null;
	
	private Long lastCheck = null;
	private Map<ServerInfo, Integer> playerCount = new HashMap<>();
	
	public static VanishNoPacketBungeeIntegration getInst() {
		return instance;
	}
	
	public ArrayList<ProxiedPlayer> getVanishedPlayers() {
		return new ArrayList<>(this.vanished);
	}
	
	public boolean isVanished(ProxiedPlayer p) {
		return this.vanished.contains(p);
	}
	
	
	@Override
	public void onEnable() {
		instance = this;
		ProxyServer.getInstance().registerChannel(VANISH_PLUGIN_CHANNEL);
		if(!vanished.isEmpty()) for(ProxiedPlayer p : vanished.toArray(new ProxiedPlayer[0])) {
			vanished.remove(p);
			p.getServer().sendData(VANISH_PLUGIN_CHANNEL, "check".getBytes());
		}
		ProxyServer.getInstance().getPluginManager().registerListener(this, this);
		this.getProxy().getPluginManager().registerCommand(this, new MainCommand(this));
	}
	
	
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onChannelMessage(PluginMessageEvent e) {
		if(e.isCancelled()) return;
		if(e.getSender() instanceof Server) {
			if(e.getTag().equals("BungeeCord") || e.getTag().equals("bungeecord:main")) {
				ByteArrayDataInput data = ByteStreams.newDataInput(e.getData());
				String type = data.readUTF();
				if(type.equals("PlayerCount")) {
					String server = data.readUTF();
					if(!server.equals("ALL")) {
						e.setCancelled(true);
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("PlayerCount");
						out.writeUTF(server);
						int count = 0;
						ServerInfo info = ProxyServer.getInstance().getServerInfo(server);
						if(info != null && !info.getPlayers().isEmpty()) for(ProxiedPlayer p : info.getPlayers()) {
							if(!vanished.contains(p)) count++;
						}
						out.writeInt(count);
						((Server) e.getSender()).sendData(e.getTag(), out.toByteArray());
					}
				}
			} else if(e.getTag().equals(VANISH_PLUGIN_CHANNEL) || e.getTag().equals("vanishnopacket:state")) {
				ProxiedPlayer p = (ProxiedPlayer) e.getReceiver();
				e.setCancelled(true);
				if(e.getData().length == 1) {
					if(e.getData()[0] == 0x00) {
						vanished.remove(p);
					} else if(e.getData()[0] == 0x01) {
						if(!vanished.contains(p)) vanished.add(p);
					}
					//this.getProxy().broadcast(new TextComponent(TextComponent.fromLegacyText("§x§6§5§4§3§2§1"+e.getTag()+": "+e.getData()[0])));
				}
			}
		}
	}
	
	
	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent e) {
		this.vanished.remove(e.getPlayer());
	}
	
	
	@EventHandler
	public void onConnect(ServerConnectedEvent e) {
		this.getProxy().getScheduler().schedule(this, () -> {
			this.vanished.remove(e.getPlayer());
			e.getPlayer().getServer().sendData(VANISH_PLUGIN_CHANNEL, "check".getBytes());
		}, 50, TimeUnit.MILLISECONDS);
	}
	
	
	public void refreshPlayerCount() {
		HashMap<ServerInfo, Integer> map = new HashMap<>();
		if(this.getProxy().getServersCopy().size() > 0) for (ServerInfo srv : this.getProxy().getServersCopy().values()) {
			map.put(srv, (int)srv.getPlayers().stream().filter(this::isVanished).count());
		}
		this.lastCheck = System.currentTimeMillis();
		this.playerCount = map;
	}
	
	
	public Map<ServerInfo, Integer> getServerCount() {
		if (this.lastCheck == null ||System.currentTimeMillis() - this.lastCheck > 1000) this.refreshPlayerCount();
		return new HashMap<>(this.playerCount);
	}
	

}
