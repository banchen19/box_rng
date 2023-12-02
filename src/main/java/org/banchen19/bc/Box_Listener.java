package org.banchen19.bc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.Instant;
import java.util.Date;
import java.util.Iterator;

import static org.banchen19.bc.Box_Item.*;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.reloadCommandAliases;

public class Box_Listener implements Listener {
    util_sqlite utilSqlite;

    public Box_Listener(util_sqlite utilSqlite) {
        this.utilSqlite = utilSqlite;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        Box_Item boxItem =utilSqlite.getChestInfoByLocation(block.getLocation());
        if (boxItem != null) {
            /*
              查询是否冷却完成，
             */
            event.setCancelled(true);
            if (gettime_tf(boxItem.timestamp, boxItem.refresh_duration))//是否抵达冷却时间
            {
//                创建ui
                createUI(boxItem, player);
                utilSqlite.updateTimestampByLocation(boxItem.locationstr);
            } else {
                long now = Date.from(Instant.now()).getTime();
                long runtime = (now - boxItem.timestamp) / 1000;
                player.sendMessage("冷却时间剩余：" +(boxItem.refresh_duration - runtime));
            }

        }else {
            Box_Item boxItem1= Box_Oj.getInstance().getBoxItem();
            if (boxItem1!=null)
            {
                boxItem1.setLocationstr(locationToString((block.getLocation())));
                //添加
                utilSqlite.insertData(boxItem1);
                Box_Oj.getInstance().setBoxItem(null);
                //结束箱子写入
            }
        }
    }

    //给玩家一个页面
    private void createUI(Box_Item boxItem, Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, boxItem.name);
        //反序列化 item_str
        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray)parser.parse(utilSqlite.getRandomDataByChestName(boxItem.lv));
            for (Object obj : jsonArray) {
                ItemStack itemStack = itemFromString(obj.toString());
//                getLogger().info(itemStack.getI18NDisplayName());
                inventory.addItem(itemStack);
            }
        } catch (ParseException var9) {
            throw new RuntimeException(var9);
        }


        player.openInventory(inventory);

    }

    //    检查时差
    boolean gettime_tf(Long time, int s) {
        long now = Date.from(Instant.now()).getTime();
        long runtime = (now - time) / 1000;
        return runtime >= s;
    }
}


