package your.plugin.package;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.UUID;

public class NetheriteLimiter extends JavaPlugin implements Listener {

    private static final HashSet<UUID> netheriteOwners = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("NetheriteLimiter has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("NetheriteLimiter has been disabled!");
    }

    @EventHandler
    public void onArmorEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        if (isNetheriteArmor(item.getType())) {
            // Check if someone already owns a full set
            if (!netheriteOwners.contains(player.getUniqueId()) && fullSetExists()) {
                event.setCancelled(true);
                player.sendMessage("§cA full Netherite set already exists. You must defeat its owner to claim it!");
            } else {
                netheriteOwners.add(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onCraftNetheriteArmor(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack craftedItem = event.getCurrentItem();
        if (craftedItem == null) return;

        if (isNetheriteArmor(craftedItem.getType())) {
            if (fullSetExists()) {
                event.setCancelled(true);
                dropItemWithEffect(player, craftedItem);
                player.sendMessage("§cA full Netherite armor set already exists. Defeat the current owner to claim it!");
            } else {
                netheriteOwners.add(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (netheriteOwners.contains(player.getUniqueId())) {
            netheriteOwners.remove(player.getUniqueId());
            Bukkit.broadcastMessage("§6The legendary Netherite armor is now up for grabs!");
        }
    }

    private boolean fullSetExists() {
        for (UUID uuid : netheriteOwners) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && hasFullNetheriteSet(player)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFullNetheriteSet(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        int count = 0;
        for (ItemStack piece : armor) {
            if (piece != null && isNetheriteArmor(piece.getType())) {
                count++;
            }
        }
        return count == 4;
    }

    private boolean isNetheriteArmor(Material material) {
        return material == Material.NETHERITE_HELMET ||
               material == Material.NETHERITE_CHESTPLATE ||
               material == Material.NETHERITE_LEGGINGS ||
               material == Material.NETHERITE_BOOTS;
    }

    private void dropItemWithEffect(Player player, ItemStack item) {
        Location loc = player.getLocation();
        player.getWorld().dropItemNaturally(loc, item);
        player.getWorld().playSound(loc, Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 30, 0.5, 0.5, 0.5, 0.1);
    }
}
