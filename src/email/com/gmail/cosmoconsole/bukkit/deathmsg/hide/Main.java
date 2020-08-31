package email.com.gmail.cosmoconsole.bukkit.deathmsg.hide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import email.com.gmail.cosmoconsole.bukkit.deathmsg.DMPReloadEvent;
import email.com.gmail.cosmoconsole.bukkit.deathmsg.DeathMessagePreparedEvent;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {
	public static final int CONFIG_VERSION = 2;
	public static String str_noSuchPlayer = "§cCannot find player, or invalid UUID";
	public static String str_hideOk = "§aDeath messages for given player will now be hidden for you";
	public static String str_showOk = "§aDeath messages for given player will now be shown again";
	public static FileConfiguration data = new YamlConfiguration();
    public void onEnable() {
    	getServer().getPluginManager().getPlugin("DeathMessagesPrime");
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.saveDefaultConfig();
        loadFromConfig();
    }
    public void onDisable() {
		save();
    }
	@EventHandler
    public void onDMPReload(DMPReloadEvent event) {
		save();
        loadFromConfig();
    }
	public void loadFromConfig() {
	    this.reloadConfig();
		if (getConfig().getInt("config-version", 1) < CONFIG_VERSION) {
			getLogger().warning("Your configuration is outdated. To migrate, rename the old config.yml file, restart the server and copy the saves: section from the old config.yml into the new one.");
		}
		str_noSuchPlayer = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message-no-such-player", str_noSuchPlayer));
		str_hideOk = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message-hide-ok", str_hideOk));
		str_showOk = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message-show-ok", str_showOk));
	    try {
			data.load(new File(getDataFolder(), "data.yml"));
		} catch (IOException | InvalidConfigurationException e) {
			this.saveResource("data.yml", false);
			try {
				data.load(new File(getDataFolder(), "data.yml"));
			} catch (IOException | InvalidConfigurationException e1) {
				e1.printStackTrace();
			}
		}
		if (getConfig().contains("saved")) {
			data.set("saved", getConfig().getConfigurationSection("saved"));
			save();
			getConfig().set("saved", null);
			try {
				getConfig().save(new File(getDataFolder(), "config.yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void save() {
		try {
			data.save(new File(getDataFolder(), "data.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EventHandler
	public void onDMPPrepare(DeathMessagePreparedEvent e) {
		ConfigurationSection sv = data.getConfigurationSection("saved");
		if (sv == null) 
			return;
		String k = e.getPlayer().getUniqueId().toString();
        if (sv.contains(k)) {
        	for (String s: sv.getStringList(k)) {
        		try {
        			e.addAlwaysHide(UUID.fromString(s));
        		} catch (Exception ex) { }
        	}
        }
	}
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("dmhlist")) {
        	if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
        	}
            if (!sender.hasPermission("dmphidemessages.hide")) {
                sender.sendMessage("§cYou have no permission to run this command.");
                return true;
            }
            ConfigurationSection sv = data.getConfigurationSection("saved");
            if (sv != null) {
	            String v = ((Player)sender).getUniqueId().toString();
	            List<UUID> j = new ArrayList<UUID>();
	            for (String k: sv.getKeys(false)) {
	            	if (sv.getStringList(k).contains(v)) {
	            		try {
	            			j.add(UUID.fromString(k));
	            		} catch (IllegalArgumentException e) {}
	            	}
	            }
	            sender.sendMessage("(" + j.size() + ")");
	            StringBuilder sb = new StringBuilder();
	            // build list of players
	            if (j.size() > 0) {
	            	OfflinePlayer p = null;
	            	for (UUID u: j) {
	            		sb.append(", ");
	            		p = getServer().getOfflinePlayer(u);
	            		if (p != null && p.hasPlayedBefore()) {
	            			sb.append(p.getName());
	            		} else {
	            			sb.append("???(" + u.toString() + ")");
	            		}
	            	}
	            	sender.sendMessage(sb.toString().substring(2));
	            }
	        } else {
	        	sender.sendMessage("(0)");
	        }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("dmshow")) {
        	if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
        	}
            if (!sender.hasPermission("dmphidemessages.hide")) {
                sender.sendMessage("§cYou have no permission to run this command.");
                return true;
            }
            if (args.length < 1) {
            	return false;
            }
            String s = args[0];
            UUID result = null;
            try {
            	result = UUID.fromString(s);
            } catch (IllegalArgumentException ex) {}
            if (result == null) {
            	Player p = getServer().getPlayer(s);
            	if (p == null) {
                    sender.sendMessage(str_noSuchPlayer);
                    return true;
            	}
            	result = p.getUniqueId();
            }
            String k = result.toString();
            List<String> zf;
            ConfigurationSection sv = data.getConfigurationSection("saved");
            if (sv != null) {
	            if (sv.contains(k)) {
	            	zf = sv.getStringList(k);
	            } else {
	            	zf = new ArrayList<String>();
	            }
	            String v = ((Player)sender).getUniqueId().toString();
	            zf.remove(v);
	            if (zf.isEmpty())
	            	sv.set(k, null);
	            else
	            	sv.set(k, zf);
            }
            sender.sendMessage(str_showOk);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("dmhide")) {
        	if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
        	}
            if (!sender.hasPermission("dmphidemessages.hide")) {
                sender.sendMessage("§cYou have no permission to run this command.");
                return true;
            }
            if (args.length < 1) {
            	return false;
            }
            String s = args[0];
            UUID result = null;
            try {
            	result = UUID.fromString(s);
            } catch (IllegalArgumentException ex) {}
            if (result == null) {
            	Player p = getServer().getPlayer(s);
            	if (p == null) {
                    sender.sendMessage(str_noSuchPlayer);
                    return true;
            	}
            	result = p.getUniqueId();
            }
            String k = result.toString();
            List<String> zf;
            ConfigurationSection sv = data.getConfigurationSection("saved");
            boolean tmpval = false;
            if (sv == null) {
            	data.set("saved.tmpval", 1);
            	save();
            	sv = data.getConfigurationSection("saved");
            	tmpval = true;
            }
            if (sv.contains(k)) {
            	zf = sv.getStringList(k);
            } else {
            	zf = new ArrayList<String>();
            }
            String v = ((Player)sender).getUniqueId().toString();
            if (!zf.contains(v))
            	zf.add(v);
            sv.set(k, zf);
            if (tmpval) {
            	data.set("saved.tmpval", null);
            	save();
            }
            sender.sendMessage(str_hideOk);
            return true;
        }
        return false;
    }
}
