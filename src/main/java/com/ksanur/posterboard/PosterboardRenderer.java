package com.ksanur.posterboard;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User: bobacadodl
 * Date: 12/31/13
 * Time: 10:53 AM
 */
public class PosterboardRenderer extends MapRenderer {
    List<UUID> renderedPlayers = new ArrayList<>();

    private boolean drawn = false;

    private BufferedImage image;
    private String name;
    private int idx;

    public PosterboardRenderer(String name, int idx, BufferedImage image) {
        this.image = image;
        this.idx = idx;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return idx;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (!drawn) {
            mapCanvas.drawImage(0, 0, image);
            drawn = true;
        }


        if (!renderedPlayers.contains(player.getUniqueId())) {
            if (PosterboardRenderTask.renderQueue.containsKey(player.getUniqueId())) {
                ArrayDeque<MapView> queue = PosterboardRenderTask.renderQueue.get(player.getUniqueId());
                if (!queue.contains(mapView)) {
                    queue.add(mapView);
                    PosterboardRenderTask.renderQueue.put(player.getUniqueId(), queue);
                    renderedPlayers.add(player.getUniqueId());
                }
            } else {
                ArrayDeque<MapView> queue = new ArrayDeque<>();
                queue.add(mapView);
                PosterboardRenderTask.renderQueue.put(player.getUniqueId(), queue);
                renderedPlayers.add(player.getUniqueId());
            }
        }
    }
}
