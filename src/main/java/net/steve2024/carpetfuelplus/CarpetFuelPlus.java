package net.steve2024.carpetfuelplus;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CarpetFuelPlus extends JavaPlugin implements CommandExecutor, TabCompleter {
    private Map<String, Integer> carpetBurnTimes;
    private boolean useSameBurnTimeForAll;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Save the default config if it does not exist
        saveDefaultConfig();
        loadConfig();

        // Register the event listener
        getServer().getPluginManager().registerEvents(new FuelListener(carpetBurnTimes, useSameBurnTimeForAll), this);

        // Register commands and tab completer
        this.getCommand("setburntime").setExecutor(this);
        this.getCommand("setburntime").setTabCompleter(this);
        this.getCommand("checkburntime").setExecutor(this);
        this.getCommand("checkburntime").setTabCompleter(this);
        this.getCommand("togglecarpetburntime").setExecutor(this);
        this.getCommand("resetburntime").setExecutor(this);
        this.getCommand("defaultburntime").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfig() {
        config = getConfig();
        if (!config.contains("default-burn-time-seconds")) {
            config.set("default-burn-time-seconds", 20); // Default to 20 seconds if not set
            saveConfig();
        }
        useSameBurnTimeForAll = config.getBoolean("use-same-burn-time-for-all", true);
        carpetBurnTimes = new HashMap<>();
        Map<String, Object> tempMap = config.getConfigurationSection("carpet-burn-times").getValues(false);
        for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
            carpetBurnTimes.put(entry.getKey(), (Integer) entry.getValue());
        }
        for (String key : carpetBurnTimes.keySet()) {
            carpetBurnTimes.put(key, carpetBurnTimes.get(key) * 20); // Convert seconds to ticks
        }
    }

    private void updateFurnaces() {
        // This method will force update all existing furnaces with new burn times.
        for (World world : getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState blockState : chunk.getTileEntities()) {
                    if (blockState instanceof Furnace) {
                        Furnace furnace = (Furnace) blockState;
                        ItemStack fuel = furnace.getInventory().getFuel();
                        if (fuel != null && isCarpet(fuel.getType())) {
                            int burnTime = getBurnTimeForCarpet(fuel.getType().name());
                            furnace.setBurnTime((short) burnTime);
                        }
                    }
                }
            }
        }
    }

    private int getBurnTimeForCarpet(String carpetType) {
        if (useSameBurnTimeForAll) {
            return carpetBurnTimes.get("default");
        } else {
            String color = carpetType.replace("_CARPET", "").toLowerCase();
            return carpetBurnTimes.getOrDefault(color, carpetBurnTimes.get("default"));
        }
    }

    private boolean isCarpet(Material material) {
        return material.name().endsWith("_CARPET");
    }

    private String getMessage(String key) {
        return config.getString("messages." + key);
    }

    private String formatMessage(String message, String color, int time) {
        return message.replace("{color}", color).replace("{time}", String.valueOf(time));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setburntime")) {
            if (sender.hasPermission("carpetfuelplus.setburntime")) {
                if (args.length < 1 || args.length > 2) {
                    sender.sendMessage("Usage: /setburntime <time_in_seconds> [color]");
                    return false;
                }

                try {
                    int burnTimeInSeconds = Integer.parseInt(args[0]);
                    int burnTimeInTicks = burnTimeInSeconds * 20;
                    String color = args.length == 2 ? args[1].toLowerCase() : "all";

                    if (color.equals("all")) {
                        for (String key : carpetBurnTimes.keySet()) {
                            carpetBurnTimes.put(key, burnTimeInTicks);
                        }
                        config.set("carpet-burn-times", carpetBurnTimes);
                        saveConfig();
                        updateFurnaces();
                        sender.sendMessage(formatMessage(getMessage("setburntime-success"), "all", burnTimeInSeconds));
                    } else if (carpetBurnTimes.containsKey(color)) {
                        carpetBurnTimes.put(color, burnTimeInTicks);
                        config.set("carpet-burn-times." + color, burnTimeInSeconds);
                        saveConfig();
                        updateFurnaces();
                        sender.sendMessage(formatMessage(getMessage("setburntime-success"), color, burnTimeInSeconds));
                    } else {
                        sender.sendMessage("Invalid carpet color.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(getMessage("setburntime-error"));
                }
                return true;
            } else {
                sender.sendMessage(getMessage("no-permission"));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("checkburntime")) {
            if (sender.hasPermission("carpetfuelplus.checkburntime")) {
                String color = args.length == 1 ? args[0].toLowerCase() : "all";

                if (color.equals("all")) {
                    sender.sendMessage("Current burn times for all carpets:");
                    for (String key : carpetBurnTimes.keySet()) {
                        sender.sendMessage(formatMessage(getMessage("checkburntime"), key, carpetBurnTimes.get(key) / 20));
                    }
                } else if (carpetBurnTimes.containsKey(color)) {
                    int burnTimeInSeconds = carpetBurnTimes.get(color) / 20;
                    sender.sendMessage(formatMessage(getMessage("checkburntime"), color, burnTimeInSeconds));
                } else {
                    sender.sendMessage("Invalid carpet color.");
                }
                return true;
            } else {
                sender.sendMessage(getMessage("no-permission"));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("togglecarpetburntime")) {
            if (sender.hasPermission("carpetfuelplus.toggleburntime")) {
                useSameBurnTimeForAll = !useSameBurnTimeForAll;
                config.set("use-same-burn-time-for-all", useSameBurnTimeForAll);
                saveConfig();
                updateFurnaces();
                sender.sendMessage(formatMessage(getMessage("toggleburntime-success"), useSameBurnTimeForAll ? "enabled" : "disabled", 0));
                return true;
            } else {
                sender.sendMessage(getMessage("no-permission"));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("defaultburntime")) {
            if (sender.hasPermission("carpetfuelplus.defaultburntime")) {
                int defaultBurnTimeInSeconds = 3; // Default burn time in vanilla Minecraft
                for (String key : carpetBurnTimes.keySet()) {
                    carpetBurnTimes.put(key, defaultBurnTimeInSeconds * 20);
                }
                config.set("carpet-burn-times", carpetBurnTimes);
                saveConfig();
                updateFurnaces();
                sender.sendMessage(formatMessage(getMessage("defaultburntime-success"), "all", defaultBurnTimeInSeconds));
                return true;
            } else {
                sender.sendMessage(getMessage("no-permission"));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("resetburntime")) {
            if (sender.hasPermission("carpetfuelplus.resetburntime")) {
                loadConfig();
                updateFurnaces();
                sender.sendMessage(getMessage("resetburntime-success"));
                return true;
            } else {
                sender.sendMessage(getMessage("no-permission"));
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("setburntime") || command.getName().equalsIgnoreCase("checkburntime")) {
            if (args.length == 2) {
                List<String> completions = new ArrayList<>(carpetBurnTimes.keySet());
                completions.add("all");
                return completions;
            }
        }
        return Collections.emptyList();
    }
}
