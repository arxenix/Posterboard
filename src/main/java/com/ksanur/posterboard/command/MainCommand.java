package com.ksanur.posterboard.command;

import com.ksanur.posterboard.Posterboard;
import com.ksanur.posterboard.PosterboardRenderer;
import com.ksanur.posterboard.util.ImageUtil;
import com.ksanur.posterboard.util.ItemBuilder;
import mondocommand.CallInfo;
import mondocommand.MondoFailure;
import mondocommand.dynamic.Sub;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * User: bobacadodl
 * Date: 5/24/14
 * Time: 6:05 PM
 */
public class MainCommand {
    @Sub(permission = "posterboard.get", description = "Get maps for a posterboard", usage = "<name>", minArgs = 1, allowConsole = false)
    public void get(CallInfo call) {
        String name = call.getArg(0);
        File pbfile = new File(Posterboard.instance.getDataFolder(), "posterboards" + File.separator + name);
        if (pbfile.exists()) {
            if (Posterboard.DEBUG)
                Bukkit.getLogger().info("[Posterboard] Loading posterboard " + name + "...");
            File[] pieces = pbfile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".png");
                }
            });
            //load ids
            int[] ids = new int[pieces.length];
            for (File piece : pieces) {
                try {
                    String[] ididx = piece.getName().replace(".png", "").split("_");
                    int idx = Integer.parseInt(ididx[1]);
                    int id = Integer.parseInt(ididx[0]);
                    ids[idx] = id;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            //give to player
            for (int idx = 0; idx < ids.length; idx++) {
                int id = ids[idx];

                call.getPlayer().getInventory().addItem(new ItemBuilder(Material.MAP).withData((short) id).withLore(ChatColor.AQUA + "Piece " + idx).build());
                call.reply("{AQUA} Received piece " + idx);
                if (Posterboard.DEBUG)
                    Bukkit.getLogger().info("[Posterboard] Recieved section " + idx + "...");
            }
            call.getPlayer().updateInventory();
        } else {
            call.reply("{RED}That posterboard does not exist!");
        }
    }

    @Sub(permission = "posterboard.list", description = "List all posterboards", minArgs = 0, allowConsole = true)
    public void list(CallInfo call) throws MondoFailure {
        call.reply("{AQUA}List of Posterboards:");
        boolean listed = false;

        File posterboardFile = new File(Posterboard.instance.getDataFolder(), "posterboards");
        if (posterboardFile.exists()) {
            File[] dirs = posterboardFile.listFiles();
            if (dirs != null) {

                for (File dir : dirs) {
                    if (dir.isDirectory()) {
                        listed = true;
                        call.reply("{GRAY}- {YELLOW}" + dir.getName());
                    }
                }
            }
        }

        if (!listed) {
            call.reply("{RED}No Posterboards have been created yet!");
        }
    }

    @Sub(permission = "posterboard.create", description = "Create maps for a posterboard", usage = "<name> <height> <width> <url>", minArgs = 3, allowConsole = false)
    public void create(CallInfo call) {
        String name = call.getArg(0);
        int height = call.getIntArg(1);
        int width = call.getIntArg(2);

        String url = call.getArg(3);

        if (!new File(Posterboard.instance.getDataFolder(), "posterboards" + File.separator + name).exists()) {
            try {
                BufferedImage full = ImageIO.read(new URL(url));
                BufferedImage resized = ImageUtil.resizeImage(full, width * 128, height * 128);
                BufferedImage[] sections = ImageUtil.splitImage(resized, height, width);
                for (int i = 0; i < sections.length; i++) {
                    MapView view = Bukkit.getServer().createMap(call.getPlayer().getWorld());
                    for (MapRenderer renderer : view.getRenderers()) {
                        view.removeRenderer(renderer);
                    }

                    new File(Posterboard.instance.getDataFolder(), "posterboards" + File.separator + name).mkdirs();
                    ImageIO.write(sections[i], "png", new File(Posterboard.instance.getDataFolder(), "posterboards" + File.separator + name + File.separator + view.getId() + "_" + i + ".png"));
                    if (Posterboard.DEBUG)
                        Bukkit.getLogger().info("[Posterboard] Section " + i + " created...");

                    PosterboardRenderer pbrenderer = new PosterboardRenderer(name, i, sections[i]);
                    view.addRenderer(pbrenderer);
                    Posterboard.instance.posterboardRenderers.put(view.getId(), pbrenderer);

                    call.getPlayer().getInventory().addItem(new ItemBuilder(Material.MAP).withData(view.getId()).withLore(ChatColor.AQUA + "Piece " + i).build());
                    call.getPlayer().updateInventory();
                    call.reply("{AQUA} Received piece " + i);
                }
                if (Posterboard.DEBUG)
                    Bukkit.getLogger().info("[Posterboard] Finished creating maps");
                call.reply("{GREEN} Complete!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            call.reply("{RED}This posterboard already exists!");
        }
    }

    /*@Sub(permission = "posterboard.create", description = "Create a posterboard2", usage = "<name>", minArgs = 1, allowConsole = false)
    public void create2(CallInfo call) {
        if(call.getSender() instanceof Player) {
            Player p = call.getPlayer();
            String name = call.getArg(0);

            ItemFrame pointed = getTargetFrame(p);
            if(pointed!=null) {
                pointed.setItem(new ItemBuilder(Material.STONE).build());
                call.reply("{GREEN}Yay");
                call.reply(pointed.getFacing().toString());

                Set<ItemFrame> board = getBoard(pointed);
                if(board==null) {
                    call.reply("{RED}The board must be in a rectangular shape!");
                }
                else {
                    for(ItemFrame i:board) {
                        i.setItem(new ItemBuilder(Material.COBBLESTONE).build());
                    }
                }
            }
            else {
                call.reply("{RED}You are not looking at an item frame!");
            }
        }
        else {
            call.reply("{RED}You must be a player to execute this command!");
        }
    }*/

    private ItemFrame getTargetFrame(Player p) {
        BlockIterator itr = new BlockIterator(p, 8);
        while (itr.hasNext()) {
            Block b = itr.next();
            ItemFrame i = getClosestFrame(b.getLocation());
            if (i != null) return i;
        }
        return null;
    }

    private ItemFrame getClosestFrame(Location loc) {
        Location c = loc.clone().add(0.5, 0.5, 0.5);

        double closestDistance = Integer.MAX_VALUE;
        ItemFrame closest = null;
        for (Entity e : loc.getChunk().getEntities()) {
            if (e instanceof ItemFrame) {
                double distance = c.distanceSquared(e.getLocation());
                if (distance <= 0.9 && distance < closestDistance) {
                    closest = (ItemFrame) e;
                    closestDistance = distance;
                }
            }
        }
        return closest;
    }

    private ItemFrame getFrameAtLoc(Location loc, BlockFace facing) {
        Location c = loc.clone().add(getDirectionOffset(facing));

        double closestDistance = Integer.MAX_VALUE;
        ItemFrame closest = null;
        for (Entity e : loc.getChunk().getEntities()) {
            if (e instanceof ItemFrame) {
                ItemFrame i = (ItemFrame) e;
                if (i.getFacing() == facing) {
                    double distance = c.distanceSquared(e.getLocation().add(getDirectionOffset(i.getFacing())));
                    if (distance <= 0.2 && distance < closestDistance) {
                        closest = i;
                        closestDistance = distance;
                    }
                }
            }
        }
        return closest;
    }

    private Set<ItemFrame> getBoard(ItemFrame frame) {
        Set<ItemFrame> ret = new HashSet<>();

        int up = getFramesInDirection(new Vector(0, 1, 0), frame, new HashSet<ItemFrame>()).size();
        int down = getFramesInDirection(new Vector(0, -1, 0), frame, new HashSet<ItemFrame>()).size();

        if (frame.getFacing() == BlockFace.NORTH || frame.getFacing() == BlockFace.SOUTH) {
            int east = getFramesInDirection(new Vector(1, 0, 0), frame, new HashSet<ItemFrame>()).size();
            int west = getFramesInDirection(new Vector(-1, 0, 0), frame, new HashSet<ItemFrame>()).size();

            for (int dy = -down; dy <= up; dy++) {
                for (int dx = -west; dx <= east; dx++) {
                    ItemFrame i = getFrameAtLoc(frame.getLocation().add(dx, dy, 0), frame.getFacing());
                    if (i != null)
                        ret.add(i);
                    else
                        return null;
                }
            }
        } else if (frame.getFacing() == BlockFace.EAST || frame.getFacing() == BlockFace.WEST) {
            int south = getFramesInDirection(new Vector(0, 0, 1), frame, new HashSet<ItemFrame>()).size();
            int north = getFramesInDirection(new Vector(0, 0, -1), frame, new HashSet<ItemFrame>()).size();

            for (int dy = -down; dy <= up; dy++) {
                for (int dz = -north; dz <= south; dz++) {
                    ItemFrame i = getFrameAtLoc(frame.getLocation().add(0, dy, dz), frame.getFacing());
                    if (i != null)
                        ret.add(i);
                    else
                        return null;
                }
            }
        }
        return ret;
    }

    public Set<ItemFrame> getFramesInDirection(Vector dir, ItemFrame itemFrame, Set<ItemFrame> in) {
        ItemFrame up = getFrameAtLoc(itemFrame.getLocation().add(dir), itemFrame.getFacing());
        if (up != null) {
            in.add(up);
            return getFramesInDirection(dir, up, in);
        }
        return in;
    }

    public Vector getDirectionOffset(BlockFace face) {
        if (face == BlockFace.EAST) {
            return new Vector(0, 0.5, 0.5);
        } else if (face == BlockFace.WEST) {
            return new Vector(1, 0.5, 0.5);
        } else if (face == BlockFace.SOUTH) {
            return new Vector(0.5, 0.5, 0);
        } else if (face == BlockFace.NORTH) {
            return new Vector(0.5, 0.5, 1);
        } else {
            return new Vector(0, 0, 0);
        }
    }
}
