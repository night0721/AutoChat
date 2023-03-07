package me.night0721.autochat;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigUtils {
    public static Configuration config;
    public final static String file = "config/AutoChat.cfg";

    public static void register() {
        init();
        reloadConfig();
    }


    public static void reloadConfig() {
        if (hasNoKey("main", "hub")) writeStringConfig("main", "hub", "FREE CARRIES AND FREQUENT GIVEAWAYS!!! /p me for invite");
        if (hasNoKey("main", "party")) writeStringConfig("main", "party", "Join and verify to get access! https://discord.gg/umuwc6fx");
    }

    public static void init() {
        config = new Configuration(new File(file));
        try {
            config.load();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static String getString(String category, String key) {
        category = category.toLowerCase();
        config = new Configuration(new File(file));
        try {
            config.load();
            if (config.getCategory(category).containsKey(key)) {
                return config.get(category, key, "").getString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return "";
    }

    public static void writeStringConfig(String category, String key, String value) {
        category = category.toLowerCase();
        config = new Configuration(new File(file));
        try {
            config.load();
//            String set = config.get(category, key, value).getString();
            config.getCategory(category).get(key).set(value);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }
    public static boolean hasNoKey(String category, String key) {
        category = category.toLowerCase();
        config = new Configuration(new File(file));
        try {
            config.load();
            if (!config.hasCategory(category)) return true;
            return !config.getCategory(category).containsKey(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return true;
    }

    public static void deleteCategory(String category) {
        category = category.toLowerCase();
        config = new Configuration(new File(file));
        try {
            config.load();
            if (config.hasCategory(category)) {
                config.removeCategory(new ConfigCategory(category));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }
}