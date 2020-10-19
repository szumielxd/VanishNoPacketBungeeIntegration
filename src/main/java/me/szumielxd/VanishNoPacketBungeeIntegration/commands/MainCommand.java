package me.szumielxd.VanishNoPacketBungeeIntegration.commands;

import java.util.ArrayList;

import me.szumielxd.VanishNoPacketBungeeIntegration.VanishNoPacketBungeeIntegration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MainCommand extends Command implements TabExecutor {
	
	
	
	private final VanishNoPacketBungeeIntegration plugin;
	
	
	
	public MainCommand(VanishNoPacketBungeeIntegration plugin) {
		super("vnpb", "vnpb.command");
		this.plugin = plugin;
	}
	
	
	
	@Override
	public void execute(CommandSender s, String[] args) {
		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("list") && s.hasPermission("vnpb.command.list")) {
				TextComponent text = new TextComponent(TextComponent.fromLegacyText("§3Vanished: "));
				if(this.plugin.getVanishedPlayers().isEmpty()) {
					text.addExtra(new TextComponent(TextComponent.fromLegacyText("§7EMPTY")));
				} else {
					for(ProxiedPlayer p : this.plugin.getVanishedPlayers()) {
						TextComponent part = new TextComponent(p.getDisplayName());
						part.setColor(ChatColor.AQUA);
						Text hover = new Text("§a" + p.getName() + "\n"
								+ "§7Server: §b" + p.getServer().getInfo().getName() + "\n"
								+ "§7Groups: §b" + String.join("§7, §b", p.getGroups()) + "\n"
								+ "§7Version: §b" + p.getPendingConnection().getVersion() + "\n"
								+ "\n"
								+ "§3Click to switch server\n"
								+ "§3Shift + Click to insert nickname");
						part.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
						part.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/server " + p.getServer().getInfo().getName()));
						part.setInsertion(p.getName());
						text.addExtra(part);
						text.addExtra(new TextComponent(TextComponent.fromLegacyText("§7, ")));
					}
					text.getExtra().remove(text.getExtra().size()-1);
				}
				s.sendMessage(text);
				return;
			}
		}
		s.sendMessage(TextComponent.fromLegacyText("§cUsage: /vnpb list"));
	}



	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if(args.length == 1) {
			ArrayList<String> list = new ArrayList<>();
			if("list".startsWith(args[0].toLowerCase())) list.add("list");
			return list;
		}
		return null;
	}
	
}
