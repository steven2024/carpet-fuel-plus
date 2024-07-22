package net.steve2024.carpetfuelplus;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class FuelListener implements Listener {
    private final Map<String, Integer> carpetBurnTimes;

    public FuelListener(Map<String, Integer> carpetBurnTimes) {
        this.carpetBurnTimes = carpetBurnTimes;
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        ItemStack fuel = event.getFuel();
        if (fuel != null && isCarpet(fuel.getType())) {
            String color = fuel.getType().name().replace("_CARPET", "").toLowerCase();
            int burnTime = carpetBurnTimes.getOrDefault(color, carpetBurnTimes.get("default"));
            event.setBurnTime(burnTime); // Set the burn time from the config
        }
    }

    private boolean isCarpet(Material material) {
        return material.name().endsWith("_CARPET");
    }
}
