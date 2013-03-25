package Neptuner.Bank;

import org.bukkit.ChatColor;

public class Messages {
	public static final String prefix = ChatColor.GOLD + "[Bank] ";
	public static final String errorPrefix = ChatColor.RED + "Error: ";

	public static final String noPerms = errorPrefix + "you don't have permissions to use this bank sign.";
	public static final String internalError = errorPrefix + "an internal error occurred. Please notify the administrators.";
	public static final String noEconomy = errorPrefix + "no economy detected!";
	public static final String invalidArgs = errorPrefix + "invalid arguments. Please type /bank help for more info.";
	public static final String invalidSlotName = errorPrefix + "invalid slot name, must be alphanumeric.";
	public static final String slotAlreadyOpen = errorPrefix + "slot is already open!";
	
	public static final String signRemoved = prefix + "Bank sign removed.";
	public static final String signRegistered = prefix + "Bank sign registered.";
	public static final String noBreak = prefix + "You cannot break bank signs!";
	public static final String enterSlotNameOpen = prefix + "Enter the name of the slot you want to open: ";
	public static final String enterSlotNameBuy = prefix + "Enter the name of the slot you want to buy: ";
	public static final String enterSlotNameDelete = prefix + "Enter the name of the slot you want to delete: ";
	public static final String slotPrice = prefix + "A new bank slot will cost you " + ChatColor.YELLOW + "%0$" + ChatColor.GOLD + ".";
	public static final String maxSlots = prefix + "You already own the maximum ammount of slots!";
	public static final String slotsOwned = prefix + "You currently own " + ChatColor.YELLOW + "%0" + ChatColor.GOLD + " slots:";
	public static final String slotsOwnedOutOf = prefix + "You currently own " + ChatColor.YELLOW + "%0/%1"+ ChatColor.GOLD +" slots:";
	public static final String bankingCancelled = prefix + "Banking cancelled.";
	public static final String notEnoughMoney = prefix + "You do not have enough money to buy a bank slot.";

	//ADMIN MESSAGES
	public static final String adminSlotsOwned = prefix + "User "+ ChatColor.YELLOW +"%0"+ ChatColor.GOLD +" currently owns "+ ChatColor.YELLOW +"%1"+ ChatColor.GOLD +" slots:";
	public static final String adminSlotsOwnedOutOf = prefix + "User "+ ChatColor.YELLOW +"%0"+ ChatColor.GOLD +" currently owns "+ ChatColor.YELLOW +"%1/%2"+ ChatColor.GOLD +" slots:";

	public static final String slotMessage = prefix + "Bank slot " + ChatColor.YELLOW + "%0" + ChatColor.GOLD + " ";
	public static final String noSlot = slotMessage + "does not exist.";
	public static final String slotExists = slotMessage + "already exists.";
	public static final String slotOpening = slotMessage + "opening.";
	public static final String slotCreated = slotMessage + "created.";
	public static final String slotDeleted = slotMessage + "deleted.";
	public static final String slotRenamed = slotMessage + "renamed to" + ChatColor.YELLOW + "%1" + ChatColor.GOLD + ".";

}
