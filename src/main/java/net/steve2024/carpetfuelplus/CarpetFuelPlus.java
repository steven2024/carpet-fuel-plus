package net.steve2024.carpetfuelplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CarpetFuelPlus extends JavaPlugin implements CommandExecutor, TabCompleter {
    private Map<String, Integer> carpetBurnTimes;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Save the default config if it does not exist
        saveDefaultConfig();
        loadConfig();

        // Register the event listener
        getServer().getPluginManager().registerEvents(new FuelListener(carpetBurnTimes), this);

        // Register commands and tab completer
        this.getCommand("setburntime").setExecutor(this);
        this.getCommand("setburntime").setTabCompleter(this);
        this.getCommand("checkburntime").setExecutor(this);
        this.getCommand("checkburntime").setTabCompleter(this);
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
        carpetBurnTimes = (Map<String, Integer>) config.getConfigurationSection("carpet-burn-times").getValues(false);
        for (String key : carpetBurnTimes.keySet()) {
            carpetBurnTimes.put(key, carpetBurnTimes.get(key) * 20); // Convert seconds to ticks
        }
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
                        sender.sendMessage(formatMessage(getMessage("setburntime-success"), "all", burnTimeInSeconds));
                    } else if (carpetBurnTimes.containsKey(color)) {
                        carpetBurnTimes.put(color, burnTimeInTicks);
                        config.set("carpet-burn-times." + color, burnTimeInSeconds);
                        saveConfig();
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
