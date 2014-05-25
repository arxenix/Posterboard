package com.ksanur.posterboard;

import com.ksanur.posterboard.command.MainCommand;
import mondocommand.ChatMagic;
import mondocommand.FormatConfig;
import mondocommand.MondoCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * User: bobacadodl
 * Date: 12/29/13
 * Time: 11:25 AM
 */
public class Posterboard extends JavaPlugin implements Listener {
    public static Posterboard instance;
    public static Logger logger;

    public MondoCommand command;

    public HashMap<Short, PosterboardRenderer> posterboardRenderers = new HashMap<>();

    public static boolean DEBUG = false;
    public static int RENDER_DELAY = 40;
    public static int RENDER_QUEUE = 9;

    public void onEnable() {
        instance = this;
        logger = getLogger();

        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);

        ChatMagic.registerAlias("{MCMD}", ChatColor.YELLOW);
        ChatMagic.registerAlias("{USAGE}", ChatColor.GRAY);
        ChatMagic.registerAlias("{DESCRIPTION}", ChatColor.DARK_GRAY);

        FormatConfig fmt = new FormatConfig()
                .setUsageHeading("{GOLD}Posterboard Commands: ")
                .setPermissionWarning("{DARK_RED}You do not have permissions to perform this command!");

        command = new MondoCommand(fmt);
        command.autoRegisterFrom(new MainCommand());
        getCommand("posterboard").setExecutor(command);

        loadConfig();

        PosterboardRenderTask.init(this, RENDER_QUEUE, RENDER_DELAY);
    }

    public void loadConfig() {
        File posterboardFile = new File(this.getDataFolder(), "posterboards");
        if (posterboardFile.exists()) {
            File[] dirs = posterboardFile.listFiles();
            if (dirs != null) {
                for (File dir : dirs) {
                    if (dir.isDirectory()) {
                        if (DEBUG)
                            Bukkit.getLogger().info("[Posterboard] Loading posterboard " + dir.getName() + "...");
                        String name = dir.getName();
                        File[] pieces = dir.listFiles(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                return name.toLowerCase().endsWith(".png");
                            }
                        });

                        for (File piece : pieces) {
                            try {
                                String[] ididx = piece.getName().replace(".png", "").split("_");
                                int idx = Integer.parseInt(ididx[1]);
                                int id = Integer.parseInt(ididx[0]);
                                PosterboardRenderer renderer = new PosterboardRenderer(name, idx, ImageIO.read(piece));

                                MapView map = Bukkit.getMap((short) id);
                                for (MapRenderer mr : map.getRenderers()) {
                                    map.removeRenderer(mr);
                                }
                                map.addRenderer(renderer);

                                posterboardRenderers.put((short) id, renderer);

                                if (DEBUG)
                                    Bukkit.getLogger().info("[Posterboard] Loaded section " + idx + "...");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else {
            posterboardFile.mkdirs();
        }

        RENDER_DELAY = getConfig().getInt("render-delay", RENDER_DELAY);
        RENDER_QUEUE = getConfig().getInt("render-queue", RENDER_QUEUE);
        DEBUG = getConfig().getBoolean("debug", DEBUG);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                if (p != null)
                    PosterboardRenderTask.render(p);
            }
        }.runTaskLater(this, 25L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PosterboardRenderTask.renderQueue.remove(event.getPlayer().getUniqueId());
        for (PosterboardRenderer renderer : posterboardRenderers.values()) {
            renderer.renderedPlayers.remove(event.getPlayer().getUniqueId());
        }
    }

    public static Posterboard getInstance() {
        return instance;
    }
}
