package com.lenis0012.bukkit.pvp.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import com.lenis0012.bukkit.pvp.PvpLevels;
import com.lenis0012.bukkit.pvp.PvpPlayer;

public class EntityListener implements Listener {
	private PvpLevels plugin;
	private int[] attacker = new int[Short.MAX_VALUE];
	private Map<String, String> killer = new HashMap<String, String>();
	
	public EntityListener(PvpLevels plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.isCancelled())
			return;
		
		Entity a = event.getEntity();
		Entity b = event.getDamager();
		
		if(a instanceof Player && b instanceof Player) {
			Player defender = (Player) a;
			Player attacker = (Player) b;
			this.attacker[defender.getEntityId()] = attacker.getEntityId();
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		
		if(entity instanceof Player) {
			Player defender = (Player) entity;
			String dname = defender.getName();
			Player attacker = this.getPlayerByEntityId(this.attacker[defender.getEntityId()]);
			
			if(attacker != null && attacker.isOnline()) {
				String name = attacker.getName();
				PvpPlayer pp = new PvpPlayer(name);
				
				if(killer.containsKey(name)) {
					String value = killer.get(name);
					String[] data = value.split(";");
					int allowed = plugin.getConfig().getInt("settings.kill-session");
					
					String cname = data[0];
					int current = Integer.valueOf(data[1]);
					
					if(dname.equals(cname)) {
						if(current >= allowed)
							return;
						else
							killer.put(name, dname+';'+(current + 1));
					} else
						killer.put(name, dname+';'+1);
				} else {
					killer.put(name, dname+';'+1);
				}
				
				int kills = pp.get("kills");
				int lvl = pp.get("level");
				kills += 1;
				
				if(plugin.levelList.contains(kills)) {
					lvl += 1;
					pp.set("level", lvl);
					attacker.sendMessage(ChatColor.GREEN + "Level up!");
					pp.reward(attacker);
				}
				
				pp.set("kills", kills);
			}
		}
	}
	
	private Player getPlayerByEntityId(int entityId) {
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
			if(player.getEntityId() == entityId)
				return player;
		}
		
		return null;
	}
}