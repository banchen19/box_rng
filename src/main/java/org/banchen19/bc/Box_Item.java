package org.banchen19.bc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class Box_Item {
    String name;//箱子名字
    public String lv;
    public int refresh_duration;//冷却时间-m
    String locationstr;//所在位置

    public String getLocationstr() {
        return locationstr;
    }

    public void setLocationstr(String locationstr) {
        this.locationstr = locationstr;
    }

    long timestamp;//时间

    public Box_Item(String name, String lv, int refresh_duration, String locations, long timestamp) {
        this.name = name;
        this.lv = lv;
        this.refresh_duration = refresh_duration;
        this.locationstr = locations;
        this.timestamp = timestamp;
    }

    //将所在位置转换为字符串
    public static String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
    //将物品转换为字符串
    public static String itemToString(ItemStack item) {
        Map<String, Object> serializedItem = item.serialize();
        Gson gson = new Gson();
        return gson.toJson(serializedItem);
    }

    public static ItemStack itemFromString(String jsonString) {
        Gson gson = new Gson();
        Map<String, Object> serializedItem = gson.fromJson(jsonString, new TypeToken<Map<String, Object>>(){}.getType());
        return ItemStack.deserialize(serializedItem);
    }
}
