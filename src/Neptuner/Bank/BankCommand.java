package Neptuner.Bank;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankCommand implements CommandExecutor{
	Plugin plugin;
	Config config;
	SlotLoader loader;
	BankMap bm;
	Logger log;

	public BankCommand(Plugin p) {
		plugin = p;
		config = plugin.config;
		loader = plugin.loader;
		bm = plugin.bm;
		log = plugin.log;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,String[]args){
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;

			if(cmd.getName().equalsIgnoreCase("bank")){
				if(args.length > 0) {
					if(args[0].equalsIgnoreCase("help")){
						if(player.hasPermission("bank.help")) {
							player.sendMessage(ChatColor.GRAY + "[Skybank Commands]");
							player.sendMessage(ChatColor.GOLD + "/bank buy [slot]" + ChatColor.WHITE + " - buy a bank slot.");
							player.sendMessage(ChatColor.GOLD + "/bank open [slot]" + ChatColor.WHITE + " - open a bank slot.");
							player.sendMessage(ChatColor.GOLD + "/bank delete [slot]" + ChatColor.WHITE + " - delete a bank slot.");
							player.sendMessage(ChatColor.GOLD + "/bank rename [slot] [new name]" + ChatColor.WHITE + " - rename a bank slot.");
							player.sendMessage(ChatColor.GOLD + "/bank price" + ChatColor.WHITE + " - price for next bank slot.");
							player.sendMessage(ChatColor.GOLD + "/bank list" + ChatColor.WHITE + " - list your bank slots.");
							player.sendMessage(ChatColor.GRAY + "Skybank - Item banking made simple.");
							player.sendMessage(ChatColor.GRAY + "A plugin by Neptuner and lemon42.");
						} else player.sendMessage(Messages.noPerms);
					} else if(args[0].equalsIgnoreCase("rename")){
						if(player.hasPermission("bank.rename")) {
							if(args.length == 3)
								loader.renameSlot(player, args[1], args[2]);
							else player.sendMessage(Messages.invalidArgs);
						} else player.sendMessage(Messages.noPerms);
					} else if(args[0].equalsIgnoreCase("price")){
						if(player.hasPermission("bank.price")) {
							if(args.length == 1)
								loader.cmdNewSlotPrice(player);
							else player.sendMessage(Messages.invalidArgs);
						} else player.sendMessage(Messages.noPerms);
					} else if(args[0].equalsIgnoreCase("delete")){
						if(player.hasPermission("bank.delete")) {
							if(args.length == 2){
								if(args[1].contains("/")) {
									if(player.hasPermission("bank.delete.any")) {
										loader.deleteSlot(args[1].split("/")[0], args[1].split("/")[1], player);
									} else player.sendMessage(Messages.noPerms);
								} else {
									loader.deleteSlot(player, args[1]);
								}
							} else player.sendMessage(Messages.invalidArgs);
						} else player.sendMessage(Messages.noPerms);
					} else if(args[0].equalsIgnoreCase("list")){
						if(player.hasPermission("bank.list")) {
							if(args.length == 1) {
								loader.printAllSlots(player);
							} else if (args.length == 2) { //bank list [player]
								if(player.hasPermission("bank.list.any")) {
									loader.printAllSlots(player, args[1]);
								} else player.sendMessage(Messages.noPerms);
							} else player.sendMessage(Messages.invalidArgs);
						} else player.sendMessage(Messages.noPerms);
					} else if(args[0].equalsIgnoreCase("open")){
						if(player.hasPermission("bank.open")) {
							if(args.length == 2) {
								if(args[1].contains("/")) {
									if(player.hasPermission("bank.open.any")) {
										loader.openSlot(player, args[1].split("/")[1], args[1].split("/")[0]);
									} else player.sendMessage(Messages.noPerms);
								} else {
									loader.openSlot(player, args[1]);
								}
							} else player.sendMessage(Messages.invalidArgs);
						} else player.sendMessage(Messages.noPerms);
					} else if(args[0].equalsIgnoreCase("buy")){
						if(player.hasPermission("bank.buy")) {
							if(args.length == 2) {
								loader.buySlot(player, args[1]);
							} else player.sendMessage(Messages.invalidArgs);//ARGS CHECK
						} else player.sendMessage(Messages.noPerms); //PERMS CHECK
					} else player.sendMessage(Messages.invalidArgs); //END IF (args[0] check)
				} else player.performCommand("bank help"); //Perform the help command, display help :)
			}
		} else {
			if(cmd.getName().equalsIgnoreCase("bank")) {
				if(args.length > 0) {
					if(args[0].equalsIgnoreCase("help")){
						sender.sendMessage(ChatColor.GRAY + "[Skybank Console Commands]");
						sender.sendMessage(ChatColor.GOLD + "bank check" + ChatColor.WHITE + " - Check all slots.");
						sender.sendMessage(ChatColor.GOLD + "bank check [slot]" + ChatColor.WHITE + " - Check a specific slot.");
						sender.sendMessage(ChatColor.GRAY + "Skybank - Item banking made simple.");
						sender.sendMessage(ChatColor.GRAY + "A plugin by Neptuner and lemon42.");
					} else if(args[0].equalsIgnoreCase("check")) {
						
					} else sender.getServer().dispatchCommand(sender, "bank help"); //Perform the help command, display help :)
				} else sender.sendMessage(Messages.invalidArgs); //END IF (args[0] check)
			}
			
		}
		return true;
	}
}