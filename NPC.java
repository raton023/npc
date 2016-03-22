package com.darkania.npc;
 
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.util.CraftChatMessage;
import org.json.simple.parser.ParseException;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_9_R1.DataWatcher;
import net.minecraft.server.v1_9_R1.DataWatcherObject;
import net.minecraft.server.v1_9_R1.DataWatcherRegistry;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_9_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_9_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_9_R1.WorldSettings.EnumGamemode;
 
public class NPC extends Reflections {
 
       
        int entityID;
        Location location;
        GameProfile gameprofile;
       
public NPC(String name,Location location) throws MalformedURLException, IOException, ParseException{
entityID = (int)Math.ceil(Math.random() * 1000) + 2000;
gameprofile = new GameProfile(UUID.randomUUID(), name);
ChangeSkin(name);
this.location = location;
}
        
public String getUUID(String elname) throws IOException, ParseException{
URL url = new URL("https://us.mc-api.net/v3/uuid/" + elname);
URLConnection uc = url.openConnection();
uc.setUseCaches(false);
uc.setDefaultUseCaches(false);
uc.addRequestProperty("User-Agent", "Mozilla/5.0");
uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
uc.addRequestProperty("Pragma", "no-cache");
@SuppressWarnings("resource")
String json = new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A").next();
org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
Object obj = parser.parse(json);//json es la pagina ya con todos los valores
//si fuera archivo hiria new FileReader("c:\\test.json");
org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) obj;
String eluuid = (String) jsonObject.get("uuid").toString();
System.out.println("uuid "+eluuid);
return jsonObject.get("uuid").toString();
}
        
        
        
        
public void ChangeSkin(String nick) throws MalformedURLException, IOException, ParseException {
String value ="";
String firma="";
URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + getUUID(nick) + "?unsigned=false");
URLConnection uc = url.openConnection();
uc.setUseCaches(false);
uc.setDefaultUseCaches(false);
uc.addRequestProperty("User-Agent", "Mozilla/5.0");
uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
uc.addRequestProperty("Pragma", "no-cache");
//Parse it
@SuppressWarnings("resource")
String json = new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A").next();
org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
Object obj = parser.parse(json);
org.json.simple.JSONArray properties = (org.json.simple.JSONArray) ((org.json.simple.JSONObject) obj).get("properties");
for (int i = 0; i < properties.size(); i++) {
try {
org.json.simple.JSONObject property = (org.json.simple.JSONObject) properties.get(i);

value = (String) property.get("value");
firma = property.containsKey("signature") ? (String) property.get("signature") : null;
} catch (Exception e) {
Bukkit.getLogger().log(Level.WARNING, "Failed to apply auth property", e);
}
}    	
gameprofile.getProperties().put("textures", new Property("textures", value,firma));
}

public void spawn(){
PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
setValue(packet, "a", entityID);
setValue(packet, "b", gameprofile.getId());
setValue(packet, "c", location.getX());
setValue(packet, "d", location.getY());
setValue(packet, "e", location.getZ());
setValue(packet, "f", (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
setValue(packet, "g", (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));
DataWatcher watcher = new DataWatcher(null);
watcher.register(new DataWatcherObject<>(6, DataWatcherRegistry.c), 20F);
watcher.register(new DataWatcherObject<>(10, DataWatcherRegistry.a), (byte)127);
setValue(packet, "h", watcher);
addToTablist();
sendPacket(packet);
headRotation(location.getYaw(), location.getPitch());
}
       
public void destroy(){
PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] {entityID});
rmvFromTablist();
sendPacket(packet);
}
       
public void addToTablist(){
PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(gameprofile, 1, EnumGamemode.NOT_SET, CraftChatMessage.fromString(gameprofile.getName())[0]);
@SuppressWarnings("unchecked")
List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
players.add(data);
setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
setValue(packet, "b", players);
sendPacket(packet);
}
       
public void rmvFromTablist(){
PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(gameprofile, 1, EnumGamemode.NOT_SET, CraftChatMessage.fromString(gameprofile.getName())[0]);
@SuppressWarnings("unchecked")
List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
players.add(data);
setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
setValue(packet, "b", players);
sendPacket(packet);
}
       
public void headRotation(float yaw,float pitch){
PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook();
PacketPlayOutEntityHeadRotation packetHead = new PacketPlayOutEntityHeadRotation();
setValue(packetHead, "a", entityID);
setValue(packetHead, "b", yaw);
sendPacket(packet);
sendPacket(packetHead);
}
    
public int getEntityID() {
return entityID;
}
}