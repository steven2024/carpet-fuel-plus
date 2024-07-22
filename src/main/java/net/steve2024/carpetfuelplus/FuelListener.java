package net.steve2024.carpetfuelplus;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class FuelListener implements Listener {
    private final Map<String, Integer> carpetBurnTimes;
    private final boolean useSameBurnTimeForAll;

    public FuelListener(Map<String, Integer> carpetBurnTimes, boolean useSameBurnTimeForAll) {
        this.carpetBurnTimes = carpetBurnTimes;
        this.useSameBurnTimeForAll = useSameBurnTimeForAll;
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        ItemStack fuel = event.getFuel();
        if (fuel != null && isCarpet(fuel.getType())) {
            int burnTime = getBurnTimeForCarpet(fuel.getType().name());
            event.setBurnTime(burnTime); // Set the burn time from the config
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
}
