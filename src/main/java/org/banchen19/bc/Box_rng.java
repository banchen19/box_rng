package org.banchen19.bc;

import com.google.gson.JsonArray;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.banchen19.bc.Box_Item.itemToString;


public final class Box_rng extends JavaPlugin implements @NotNull Listener {
    util_sqlite util_sqlite;

    @Override
    public void onEnable() {
        util_sqlite = new util_sqlite();
        try {
            File folder = new File(util_sqlite.bcPath);
            folder.mkdirs();
            File file_db = new File(util_sqlite.databasePath);
            if (!file_db.exists()) {
                file_db.createNewFile();
            }
            util_sqlite.connect();
            util_sqlite.createTable();
        } catch (Exception var3) {
            this.getLogger().severe("文件夹创建失败: " + var3.getMessage());
        }
        // Plugin startup logic
        Bukkit.getPluginCommand("rng_box").setExecutor(this);
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new Box_Listener(util_sqlite), this);
        this.getLogger().info("插件已启动！");
    }

    @Override
    public void onDisable() {
        util_sqlite.disconnect();
    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //add:1
        String text_usrg = "用法: " +
                "/rng_box add <箱子名> <箱子等级> <箱子刷新时间(秒)> \n " +
                "/rng_box remove <箱子名>\n" +
                "/rng_box all\n" +
                "/rng_box addlv <等级> 添加这个等级的随机箱可能性\n" +
                "/rng_box removelv <等级> 移除所有这个等级的随机箱可能性";
        Player player = (Player) sender;
        switch (args[0]) {
            case "all":
                List<Box_Item> boxItemList = util_sqlite.getChestInfoByName_String();
                for (Box_Item boxItem : boxItemList)
                {
                    player.sendMessage(
                            "箱子名: "+boxItem.name+
                            ",箱子等级: "+boxItem.lv+
                            ",冷却时间（秒）: "+boxItem.refresh_duration+
                            ",所在位置: "+boxItem.locationstr
                    );
                }
                break;
            case "add":
                if (args.length >= 4) {
                    Box_Item boxItem = new Box_Item(
                            args[1],
                            args[2],
                            Integer.parseInt(args[3]),
                            null,
                            Date.from(Instant.now()).getTime()
                    );
                    Box_Oj.getInstance().setBoxItem(boxItem);
                } else {
                    player.sendMessage(text_usrg);
                }
                break;
            case "addlv":
                Box_rng_lv boxRngLv=new Box_rng_lv();
                boxRngLv.chest_name =args[1];
                Inventory chestInventory = player.getInventory();
                JsonArray jsonArray = new JsonArray();
                ItemStack[] var10 = chestInventory.getContents();

                for (ItemStack item : var10) {
                    if (item != null) {
                        jsonArray.add(itemToString(item));
                    }
                }
                boxRngLv.item_data=String.valueOf(jsonArray);
                util_sqlite.insertData_Box(boxRngLv);
                break;

            case "remove":
               util_sqlite.deleteChestByName(sender, args[2]);
                break;
            case "removelv":
                util_sqlite.deleteChestBy_box(sender,args[2]);
                break;
        }
        return false;
    }

}
