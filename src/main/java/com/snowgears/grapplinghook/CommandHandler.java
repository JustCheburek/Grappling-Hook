package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.utils.HookSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler extends BukkitCommand {

    private GrapplingHook plugin;

    public CommandHandler(GrapplingHook instance, String permission, String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.setPermission(permission);
        plugin = instance;
        try {
            register();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                //these are commands only operators have access to
                if (player.hasPermission("grapplinghook.operator") || player.isOp()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("command", this.getName());
                    player.sendMessage(plugin.getMessageManager().getCommandMessage("help_give_self", placeholders));
                    player.sendMessage(plugin.getMessageManager().getCommandMessage("help_give_other", placeholders));
                    player.sendMessage(plugin.getMessageManager().getCommandMessage("help_info", placeholders));
                    return true;
                } else {
                    // Показываем команду info всем игрокам
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("command", this.getName());
                    player.sendMessage(plugin.getMessageManager().getCommandMessage("help_info", placeholders));
                    return true;
                }
            }
            //these are commands that can be executed from the console
            else{
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("command", this.getName());
                sender.sendMessage(plugin.getMessageManager().getCommandMessage("help_give_console", placeholders));
                sender.sendMessage(plugin.getMessageManager().getCommandMessage("help_info", placeholders));
                return true;
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("grapplinghook.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        player.sendMessage(plugin.getMessageManager().getCommandMessage("not_authorized"));
                        return true;
                    }
                    plugin.reload();
                    player.sendMessage(plugin.getMessageManager().getCommandMessage("reload_success"));
                } else {
                    plugin.reload();
                    sender.sendMessage(plugin.getMessageManager().getCommandMessage("reload_console"));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("info")) {
                // Команда для отображения информации о ремонте крюков
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.sendMessage(plugin.getMessageManager().getMessage("anvil.repair_info"));
                } else {
                    sender.sendMessage(plugin.getMessageManager().getMessage("anvil.repair_info"));
                }
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("grapplinghook.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        player.sendMessage(plugin.getMessageManager().getCommandMessage("not_authorized"));
                        return true;
                    }

                    String hookID = args[1];
                    HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(hookID);
                    if(hookSettings == null){
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("id", hookID);
                        player.sendMessage(plugin.getMessageManager().getCommandMessage("hook_not_found", placeholders));
                        return true;
                    }
                    player.getInventory().addItem(hookSettings.getHookItem());
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("id", hookID);
                    placeholders.put("player", player.getName());
                    player.sendMessage(plugin.getMessageManager().getCommandMessage("give_success", placeholders));
                    return true;
                }
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("grapplinghook.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        player.sendMessage(plugin.getMessageManager().getCommandMessage("not_authorized"));
                        return true;
                    }

                    String hookID = args[1];
                    HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(hookID);
                    if(hookSettings == null){
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("id", hookID);
                        player.sendMessage(plugin.getMessageManager().getCommandMessage("hook_not_found", placeholders));
                        return true;
                    }

                    Player playerToGive = plugin.getServer().getPlayer(args[2]);
                    if(playerToGive == null){
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", args[2]);
                        player.sendMessage(plugin.getMessageManager().getCommandMessage("player_not_found", placeholders));
                        return true;
                    }
                    playerToGive.getInventory().addItem(hookSettings.getHookItem());
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("id", hookID);
                    placeholders.put("player", args[2]);
                    player.sendMessage(plugin.getMessageManager().getCommandMessage("give_success", placeholders));
                    
                    Map<String, String> receiverPlaceholders = new HashMap<>();
                    receiverPlaceholders.put("id", hookID);
                    receiverPlaceholders.put("player", player.getName());
                    playerToGive.sendMessage(plugin.getMessageManager().getCommandMessage("received_hook", receiverPlaceholders));
                    return true;
                }
                else {
                    String hookID = args[1];
                    HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(hookID);
                    if(hookSettings == null){
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("id", hookID);
                        sender.sendMessage(plugin.getMessageManager().getCommandMessage("hook_not_found", placeholders));
                        return true;
                    }

                    Player playerToGive = plugin.getServer().getPlayer(args[2]);
                    if(playerToGive == null){
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", args[2]);
                        sender.sendMessage(plugin.getMessageManager().getCommandMessage("player_not_found", placeholders));
                        return true;
                    }
                    playerToGive.getInventory().addItem(hookSettings.getHookItem());
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("id", hookID);
                    placeholders.put("player", args[2]);
                    sender.sendMessage(plugin.getMessageManager().getCommandMessage("give_success", placeholders));
                    
                    Map<String, String> receiverPlaceholders = new HashMap<>();
                    receiverPlaceholders.put("id", hookID);
                    playerToGive.sendMessage(plugin.getMessageManager().getCommandMessage("received_hook_console", receiverPlaceholders));
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> results = new ArrayList<>();
        if (args.length == 0) {
            results.add(this.getName());
        }
        else if (args.length == 1) {

            boolean showOperatorCommands = false;
            if(sender instanceof Player){
                Player player = (Player)sender;

                if(player.hasPermission("grapplinghook.operator") || player.isOp()) {
                    showOperatorCommands = true;
                }
            }
            else{
                showOperatorCommands = true;
            }

            if(showOperatorCommands){
                results.add("give");
                results.add("reload");
                results.add("info");
            }
            return sortedResults(args[0], results);
        }
        else if (args.length == 2) {

            boolean showOperatorCommands = false;
            if(sender instanceof Player){
                Player player = (Player)sender;

                if(player.hasPermission("grapplinghook.operator") || player.isOp()) {
                    showOperatorCommands = true;
                }
            }
            else{
                showOperatorCommands = true;
            }

            if(showOperatorCommands && args[0].equalsIgnoreCase("give")){
                for(String hookID : plugin.getGrapplingListener().getHookIDs()) {
                    results.add(hookID);
                }
            }
            return sortedResults(args[1], results);
        }
        else if (args.length == 3) {

            boolean showOperatorCommands = false;
            if(sender instanceof Player){
                Player player = (Player)sender;

                if(player.hasPermission("grapplinghook.operator") || player.isOp()) {
                    showOperatorCommands = true;
                }
            }
            else{
                showOperatorCommands = true;
            }

            HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(args[1]);

            if(showOperatorCommands && hookSettings != null){
                results.add("<player name>");
            }
            return sortedResults(args[1], results);
        }
        return results;
    }

    private void register()
            throws ReflectiveOperationException {
        final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        bukkitCommandMap.setAccessible(true);

        CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        commandMap.register(this.getName(), this);
    }

    // Sorts possible results to provide true tab auto complete based off of what is already typed.
    public List <String> sortedResults(String arg, List<String> results) {
        final List <String> completions = new ArrayList < > ();
        StringUtil.copyPartialMatches(arg, results, completions);
        Collections.sort(completions);
        results.clear();
        for (String s: completions) {
            results.add(s);
        }
        return results;
    }
}
