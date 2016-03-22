package com.darkania.npc;
 
import java.io.IOException;
import java.net.MalformedURLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;
 
public class Main extends JavaPlugin implements Listener{
 
 
        @Override
        public void onEnable() {
                Bukkit.getPluginManager().registerEvents(this, this);
        }
       
       public static NPC npc = null;
       
        @EventHandler
        public void onSneak(PlayerToggleSneakEvent event) throws MalformedURLException, IOException, ParseException{
                Player player = event.getPlayer();
               
                
                if(event.isSneaking()&&player.isOp()){
                        NPC npc = new NPC(event.getPlayer().getName(), player.getLocation());
                        npc.spawn();
                }
        }
       
}