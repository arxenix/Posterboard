package com.ksanur.posterboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.UUID;

/**
 * User: bobacadodl
 * Date: 3/13/14
 * Time: 11:40 AM
 */
public class PosterboardRenderTask extends BukkitRunnable {
    private static PosterboardRenderTask instance = null;

    static HashMap<UUID, ArrayDeque<MapView>> renderQueue = new HashMap<>();
    static int queueSize;

    private PosterboardRenderTask() {
    }

    public void run() {
        for (UUID uuid : renderQueue.keySet()) {
            render(Bukkit.getPlayer(uuid));
        }
    }

    public static void render(Player p) {
        if (renderQueue.containsKey(p.getUniqueId())) {
            ArrayDeque<MapView> queue = renderQueue.get(p.getUniqueId());
            for (int i = 0; i < queueSize; i++) {
                MapView view = queue.poll();
                if (view != null) {
                    p.sendMap(view);
                }
            }
        }
    }

    public static void init(JavaPlugin plugin, int queue, int delay) {
        if (instance == null) {
            instance = new PosterboardRenderTask();
            instance.runTaskTimer(plugin, delay, delay);
            queueSize = queue;
        }
    }
}
