package Neptuner.Bank;

import java.util.HashMap;

import org.bukkit.inventory.Inventory;

public class BankMap {
	HashMap<String, String> curSlots;
	HashMap<String, Inventory> curContents;
	HashMap<String, String> sign;

	public BankMap() {
		curSlots = new HashMap<String, String>();
		curContents = new HashMap<String, Inventory>();
		sign = new HashMap<String, String>();
	}

	public void setSlot(String player, String slotname){
		if(slotname == null) curSlots.remove(player);
		else curSlots.put(player, slotname);
	}
	public String getSlot(String player){
		return curSlots.get(player);
	}

	public void setCurrentSlotContents(String player, Inventory inv) {
		if(inv == null) curContents.remove(player);
		else curContents.put(player, inv);
	}
	public Inventory getCurrentSlotContents(String player) {
		return curContents.get(player);
	}

	public void setSign(String player, String using){
		if(using == null) sign.remove(player);
		else sign.put(player, using);
	}
	public String getSign(String player){
		return sign.get(player);
	}
}
