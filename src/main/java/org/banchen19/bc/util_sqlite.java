package org.banchen19.bc;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.banchen19.bc.Box_Item.locationToString;
import static org.bukkit.Bukkit.getLogger;

public class util_sqlite {
    public Connection connection;
    String bcPath = "./plugins/bc_box_rng/";
    String databasePath = bcPath + "sqlite.db";

    //创建链接数据表
    void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.databasePath);
            createTable();
            getLogger().info("数据库连接成功");
        } catch (SQLException var2) {
            getLogger().severe("数据库连接失败: " + var2.getMessage());
        }
    }

    //创建表
    void createTable() {
        try {
            Statement statement = connection.createStatement();
            try {
                //创建箱子位置表
                String sql = "CREATE TABLE IF NOT EXISTS chests (" +
                        "chest_name TEXT PRIMARY KEY," +
                        "lv TEXT," +
                        "timelong INTEGER," +
                        "refresh_duration INT," +
                        "location TEXT UNIQUE)";
                statement.executeUpdate(sql);
                //创建添加箱子可能性表
                String sql_box = "CREATE TABLE IF NOT EXISTS chests_box_rang (chest_name TEXT,item_data TEXT)";
                statement.executeUpdate(sql_box);
            } catch (Throwable var5) {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Throwable var4) {
                        var5.addSuppressed(var4);
                    }
                }

                throw var5;
            }
            statement.close();
        } catch (SQLException var6) {
            getLogger().severe("创建表失败: " + var6.getMessage());
        }

    }

    //断开数据库连接
    void disconnect() {
        try {
            if (this.connection != null) {
                this.connection.close();
                getLogger().info("数据库连接已关闭");
            }
        } catch (SQLException var2) {
            getLogger().severe("关闭数据库连接失败: " + var2.getMessage());
        }

    }
    //添加箱子位置
    public void insertData(Box_Item boxItem) {
        try {
            PreparedStatement statement = this.connection.prepareStatement("INSERT INTO chests (chest_name, lv, timelong, refresh_duration, location) VALUES (?, ?, ?, ?, ?)");

            try {
                statement.setString(1, boxItem.name);
                statement.setString(2, boxItem.lv);
                statement.setLong(3,boxItem.timestamp );
                statement.setInt(4, boxItem.refresh_duration);
                statement.setString(5, boxItem.locationstr);
                statement.executeUpdate();//更新数据
            } catch (Throwable var5) {
                statement.close();
            }
            statement.close();
        } catch (SQLException var6) {
//            getLogger().severe("插入数据失败: " + var6.getMessage());
        }
    }

    //获取箱子内数据
    public Box_Item getChestInfoByLocation(Location location) {
        try {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM chests WHERE location = ?");
            statement.setString(1, locationToString(location));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Box_Item boxItem = new Box_Item(
                        resultSet.getString("chest_name"),
                        resultSet.getString("lv"),
                        resultSet.getInt("refresh_duration"),
                        resultSet.getString("location"),
                        resultSet.getLong("timelong")
                );

                statement.close();
                return boxItem;
            } else {
                statement.close();
                return null;
            }
        } catch (SQLException var7) {
            getLogger().severe("查询数据失败: " + var7.getMessage());
            return null;
        }
    }

    //添加箱子可能性
    public void insertData_Box(Box_rng_lv boxRngLv) {
        try {
            PreparedStatement statement = this.connection.prepareStatement("INSERT INTO chests_box_rang (chest_name, item_data) VALUES (?, ?)");
            statement.setString(1, boxRngLv.chest_name);
            statement.setString(2, boxRngLv.item_data);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException var8) {
            getLogger().severe("插入数据失败: " + var8.getMessage());
        }
    }

    //更新指定位置箱子的时间
    void updateTimestampByLocation(String location_str) {
        try {
            PreparedStatement statement = this.connection.prepareStatement("UPDATE chests SET timelong = ? WHERE location = ?");

            try {
                statement.setLong(1, Date.from(Instant.now()).getTime());
                statement.setString(2, location_str);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    getLogger().info("成功更新了 " + rowsUpdated + " 行数据的 timestamp");
                } else {
                    getLogger().info("未找到符合条件的数据");
                }
            } catch (Throwable var6) {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (statement != null) {
                statement.close();
            }
        } catch (SQLException var7) {
            getLogger().severe("更新数据失败: " + var7.getMessage());
        }

    }

    //    删除指定等级下的所有可能性
    void deleteChestBy_box(CommandSender sender, String chestName) {
        try {
            PreparedStatement statement = this.connection.prepareStatement("DELETE FROM chests_box_rang WHERE chest_name = ?");

            statement.setString(1, chestName);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                sender.sendMessage("成功删除可能性 " + chestName);
            } else {
                sender.sendMessage("未找到可能性 " + chestName);
            }
            statement.close();
        } catch (SQLException var8) {
            sender.sendMessage("删除失败: " + var8.getMessage());
        }

    }

    //    删除指定位置的箱子
     void deleteChestByName(CommandSender sender, String chestName) {
        try {
            PreparedStatement statement = this.connection.prepareStatement("DELETE FROM chests WHERE chest_name = ?");

            statement.setString(1, chestName);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                sender.sendMessage("成功删除名称为 " + chestName);
            } else {
                sender.sendMessage("未找到箱子名称为 " + chestName);
            }
            statement.close();
        } catch (SQLException var8) {
            sender.sendMessage("删除记录失败: " + var8.getMessage());
        }

    }

    /**
     * @param chestName 箱子名字
     * @return 箱子内是数据
     */
    public String getRandomDataByChestName(String chestName) {
        try {
            String var5;
            PreparedStatement selectStatement = this.connection.prepareStatement("SELECT item_data FROM chests_box_rang WHERE chest_name = ? ORDER BY RANDOM() LIMIT 1");

            selectStatement.setString(1, chestName);
            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                String retrievedItemData = resultSet.getString("item_data");
                selectStatement.close();
                return retrievedItemData;
            } else {
                selectStatement.close();
                return null;
            }
        } catch (SQLException var8) {
            getLogger().severe("查询随机行失败: " + var8.getMessage());
            return null;
        }
    }

    //获取所有箱子名字以及所在位置
    public List<Box_Item> getChestInfoByName_String() {
        List<Box_Item> result = new ArrayList();
        try {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM chests");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Box_Item boxItem = new Box_Item(
                        resultSet.getString("chest_name"),
                        resultSet.getString("lv"),
                        resultSet.getInt("refresh_duration"),
                        resultSet.getString("location"),
                        resultSet.getLong("timelong")
                );
                result.add(boxItem);
            }
        } catch (SQLException var8) {
            getLogger().severe("查询数据失败: " + var8.getMessage());
        }

        return result;
    }


}
