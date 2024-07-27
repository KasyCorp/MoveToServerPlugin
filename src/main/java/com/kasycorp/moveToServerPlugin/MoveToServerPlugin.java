package com.kasycorp.moveToServerPlugin;


import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class MoveToServerPlugin extends JavaPlugin implements Listener {
    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        createConfig();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            String targetServer = config.getString("targetServer", "defaultServer");
            sendToServer(event.getPlayer(), targetServer);
        }
    }

    private void sendToServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    private void createConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("reloadconfig")) {
            if (sender.hasPermission("movetoserver.reloadconfig")) {
                createConfig();
                sender.sendMessage("Configuration reloaded.");
                return true;
            } else {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("settargetserver")) {
            if (sender.hasPermission("movetoserver.settargetserver")) {
                if (args.length != 1) {
                    sender.sendMessage("Usage: /settargetserver <serverName>");
                    return true;
                }
                String targetServer = args[0];
                config.set("targetServer", targetServer);
                try {
                    config.save(configFile);
                    sender.sendMessage("Target server set to " + targetServer);
                } catch (IOException e) {
                    sender.sendMessage("An error occurred while saving the configuration.");
                    e.printStackTrace();
                }
                return true;
            } else {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }
        }

        return false;
    }
}
