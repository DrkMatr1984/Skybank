package Neptuner.Bank;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SlotLoader {
	Plugin plugin;
	Config config;
	BankMap bm;
	
	public SlotLoader(Plugin p) {
		this.plugin = p;
		this.config = p.config;
		this.bm = p.bm;
	}
	
	public void renameSlot(Player player, String slot, String newName) {
		if (isValidSlotName(slot) && isValidSlotName(newName)) {
			if(!bm.curSlots.containsValue(slot)) {
				try {
					File f = slotFile(player.getName(), slot);
					File d = slotFile(player.getName(), newName);
					if (f.exists()) {
						if (!d.exists()) {
							f.renameTo(d);
							player.sendMessage(Messages.slotRenamed.replace("%0", slot).replace("%1", newName));
						} else player.sendMessage(Messages.slotExists.replace("%0", newName));
					} else player.sendMessage(Messages.noSlot.replace("%0", slot));
				} catch (Exception e) {
					player.sendMessage(Messages.internalError);
					plugin.printException(e);
				}
			} else player.sendMessage(Messages.slotAlreadyOpen);
		} else player.sendMessage(Messages.invalidSlotName);
	}
	
	public void deleteSlot(String player, String slot, Player admin) {
		Player dest = (admin == null ? Bukkit.getPlayer(player) : admin);
		String display = (admin == null ? slot : player + "/" + slot);
		
		if (isValidSlotName(slot)) {
			if(!bm.curSlots.containsValue(slot)) {
				File f = slotFile(player, slot);
				if (f.exists()) {
					if (f.delete()) dest.sendMessage(Messages.slotDeleted.replace("%0", display));
					else dest.sendMessage(Messages.internalError);
				} else dest.sendMessage(Messages.noSlot.replace("%0", display));
			}else dest.sendMessage(Messages.slotAlreadyOpen);
		} else dest.sendMessage(Messages.invalidSlotName);
	}
	
	public void deleteSlot(Player player, String slot) {
		deleteSlot(player.getName(), slot, null);
	}
	
	public void cmdNewSlotPrice(Player player) {
		if (getAllSlotFiles(player.getName()).size() < config.maxSlots) player.sendMessage(Messages.slotPrice.replace("%0", "" + getNewSlotPrice(player.getName())));
		else player.sendMessage(Messages.maxSlots);
	}
	
	public void printAllSlots(Player player) {
		printAllSlots(player, player.getName());
	}
	
	public void printAllSlots(Player player, String listPlayer) {
		if (listPlayer == player.getName()) if (config.maxSlots == 0) player.sendMessage(Messages.slotsOwned.replace("%0", "" + getAllSlotFiles(player.getName()).size()));
		else player.sendMessage(Messages.slotsOwnedOutOf.replace("%0", "" + getAllSlotFiles(player.getName()).size()).replace("%1", "" + config.maxSlots));
		else if (config.maxSlots == 0) player.sendMessage(Messages.adminSlotsOwned.replace("%0", listPlayer).replace("%1", "" + getAllSlotFiles(listPlayer).size()));
		else player.sendMessage(Messages.adminSlotsOwnedOutOf.replace("%0", listPlayer).replace("%1", "" + getAllSlotFiles(listPlayer).size()).replace("%2", "" + config.maxSlots));
		
		String list = "";
		for (File f : getAllSlotFiles(listPlayer))
			list += f.getName().replace(".slot", "") + ", ";
		if (list.length() > 0) player.sendMessage(ChatColor.WHITE + list.substring(0, list.length() - 2)); // -2 for ", "
	}
	
	public void openSlot(Player player, String name) {
		openSlot(player, name, "");
	}
	
	public void openSlot(Player player, String name, String target) {
		String fullName = (target == "" ? "" : target + "/") + name;
		String playerName = (target == "" ? player.getName() : target);
		
		if (isValidSlotName(name)) {
			if(!bm.curSlots.containsValue(fullName)) {
				
				File f = slotFile(playerName, name);
				if (f.exists()) {
					try {
						Inventory inv = loadSlot(playerName, name);
						
						bm.setSlot(player.getName(), fullName);
						
						player.sendMessage(Messages.slotOpening.replace("%0", fullName));
						player.openInventory(inv);
					} catch (Exception e) {
						player.sendMessage(Messages.internalError);
						plugin.printException(e);
					}
				} else player.sendMessage(Messages.noSlot.replace("%0", fullName));
			} else player.sendMessage(Messages.slotAlreadyOpen);
		} else player.sendMessage(Messages.invalidSlotName);
	}
	
	public void buySlot(Player player, String name) {
		if (plugin.economyEnabled) {
			if (isValidSlotName(name)) {
				if(!bm.curSlots.containsValue(name)) {
					File f = slotFile(player.getName(), name);
					if (!f.exists()) {
						if (getAllSlotFiles(player.getName()).size() < config.maxSlots) {
							double playermoney = plugin.getMoney(player);
							try {
								double price = getNewSlotPrice(player.getName());
								
								if (playermoney >= price) {
									createSlot(player.getName(), name);
									plugin.takeMoney(player, price);
									player.sendMessage(Messages.slotCreated.replace("%0", name));
								} else player.sendMessage(Messages.notEnoughMoney);
							} catch (Exception e) {
								player.sendMessage(Messages.internalError);
								plugin.printException(e);
							}
						} else player.sendMessage(Messages.maxSlots);
					} else player.sendMessage(Messages.slotExists.replace("%0", name));
				} else player.sendMessage(Messages.slotAlreadyOpen);
			} else player.sendMessage(Messages.invalidSlotName);
		} else player.sendMessage(Messages.noEconomy);
	}
	
	// IN CODE, NOT COMMANDS!!
	public void createSlot(String player, String name) throws Exception {
		saveSlot(plugin.emptyInv(), player, name);
	}
	
	public void saveSlot(Inventory inv, String player, String name) throws Exception {
		// Delete previous file, create new empty
		File f = slotFile(player, name);
		if (f.exists()) f.delete();
		f.createNewFile();
		
		// Start streams
		FileOutputStream fos = new FileOutputStream(f);
		GZIPOutputStream gz = new GZIPOutputStream(fos);
		DataOutputStream s = new DataOutputStream(gz);
		// Write header
		s.write("SKYBANK".getBytes("US-ASCII"));
		// Init array
		ItemStack[] invContents = new ItemStack[54];
		// Empty item stack
		ItemStack empty = new ItemStack(0);
		empty.setAmount(0);
		empty.setDurability((short)0);
		empty.getData().setData((byte)0);
		for (int i = 0; i < 54; i++)
			invContents[i] = (inv.getContents()[i] == null ? empty : inv.getContents()[i]);
		// Write items
		for (int i = 0; i < 54; i++) {
			if (invContents[i] == null) invContents[i] = empty; // Fallback
			s.writeInt(invContents[i].getTypeId());
			s.writeInt(invContents[i].getAmount());
			s.writeShort(invContents[i].getDurability());
			s.write(invContents[i].getData().getData());
			// Enchantments
			s.writeByte(invContents[i].getEnchantments().size());
			for (Entry<Enchantment, Integer> enc : invContents[i].getEnchantments().entrySet()) {
				s.writeInt(enc.getKey().getId());
				s.writeInt(enc.getValue());
			}
		}
		s.flush();
		s.close();
		gz.close();
		fos.close();
	}
	
	public Inventory loadSlot(String player, String name) throws Exception {
		FileInputStream fis = new FileInputStream(slotFile(player, name));
		GZIPInputStream gz = new GZIPInputStream(fis);
		DataInputStream s = new DataInputStream(gz);
		// Attempt to find header
		byte[] b = new byte[7];
		try {
			s.read(b, 0, 7);
		} catch (java.io.EOFException e) { plugin.log.warning("Slot " + player + "/" + name + " could possibly be corrupt!"); }
		if (!Arrays.equals(b, "SKYBANK".getBytes("US-ASCII"))) throw new Exception("SKYBANK header not found!");
		// Header found, start reading
		ItemStack[] invContents = new ItemStack[54];
		byte encCount;
		int enc, lvl;
		
		try {
			for (int i = 0; i < 54; i++) {
				invContents[i] = new ItemStack(s.readInt());
				invContents[i].setAmount(s.readInt());
				invContents[i].setDurability(s.readShort());
				invContents[i].getData().setData(s.readByte());
				// Enchantments
				encCount = s.readByte();
				for (int x = 0; x < encCount; x++) {
					enc = s.readInt();
					lvl = s.readInt();
					if (Enchantment.getById(enc) != null) invContents[i].addUnsafeEnchantment(Enchantment.getById(enc), lvl);
				}
			}
		} catch (java.io.EOFException e) {
			//Ignore the error, see what happens, still send a message to console
			plugin.log.warning("Got unexpected EOFException while running SlotLoader.loadSlot(" + player + ", " + name + "), please notify plugin authors and send them the slot file " + player + "/" + name + "!");
		}
		
		s.close();
		gz.close();
		fis.close();
		// Done reading, now we need to convert to Inventory
		Inventory inv = plugin.emptyInv(name, Bukkit.getPlayerExact(player));
		inv.setContents(invContents);
		return inv;
	}
	
	public File slotFile(String player, String name) {
		slotFolder(player).mkdir();
		return new File(plugin.getDataFolder(), "Slots" + File.separator + player + File.separator + name + ".slot");
	}
	
	public File slotFolder(String player) {
		File f = new File(plugin.getDataFolder(), "Slots" + File.separator + player + File.separator);
		if (!f.exists()) f.mkdir();
		return f;
	}
	
	public List<File> getAllSlotFiles(String player) {
		List<File> files = new ArrayList<File>();
		for (File f : slotFolder(player).listFiles())
			if (f.getName().toLowerCase().endsWith(".slot")) files.add(f);
		return files;
	}
	
	public double getNewSlotPrice(String player) {
		if (getAllSlotFiles(player).size() >= config.maxSlots) return -1;
		return (double)(config.slotPrice + config.priceIncrease * getAllSlotFiles(player).size());
	}
	
	public boolean isValidSlotName(String slotName) {
		return slotName.length() > 0 && slotName.matches("\\w+");
	}
	
	public void convertAllSlots() {
		long start = System.currentTimeMillis();
		int n = 0;
		// id:amount|durability?metadata%enchantmentstring
		// ".\\plugins\\Bank\\Slots\\"+player.getName()+"\\"+slot+".yml"
		File mainFile = new File("." + File.separator + "plugins" + File.separator + "Bank" + File.separator + "Slots" + File.separator);
		// Recursive search begin
		// For each Directory in Bank\Slots
		for (File d : mainFile.listFiles()) {
			if (d.isDirectory()) {
				String playerName = d.getName();
				// For each YML file in Bank\Slots\playername
				for (File f : d.listFiles()) {
					if (f.isFile() && f.getAbsolutePath().toLowerCase().endsWith(".yml") && f.canRead()) {
						String name = f.getName();
						// Attempt to read it
						Inventory inv = plugin.emptyInv();
						ItemStack[] invContents = new ItemStack[54];
						try {
							BufferedReader in = new BufferedReader(new FileReader(f));
							String curLine;
							String[] values;
							for (int i = 0; i < 54; i++) {
								curLine = in.readLine();
								if (!curLine.contains(":") || !curLine.contains("|") || !curLine.contains("?") || !curLine.contains("%")) break;
								curLine = curLine.replace(":", "|").replace("?", "|").replace("%", "|");
								values = curLine.split("\\|");
								// {Enchantment[16, DAMAGE_ALL]=5, Enchantment[20, FIRE_ASPECT]=2}
								invContents[i] = new ItemStack(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Short.parseShort(values[2]), Byte.parseByte(values[3]));
								values[4] = values[4].replace("{", "").replace("}", "").replace(" ", "");
								int v = 0;
								int encId = 0;
								int encLevel = 0;
								if (values[4].contains("Enchantment")) {
									for (String s : values[4].split(",")) {
										v++;
										if (s.startsWith("Enchantment[")) {
											encId = Integer.parseInt(s.replace("Enchantment[", ""));
										} else {
											encLevel = Integer.parseInt(s.split("=")[1]);
											if (v == 2) {
												if (Enchantment.getById(encId) != null) invContents[i].addUnsafeEnchantment(Enchantment.getById(encId), encLevel);
												encId = 0;
												encLevel = 0;
												v = 0;
											}
										}
									}
								}
								
							}
							// Done parsing, convert to Inventory and save new slot
							inv.setContents(invContents);
							in.close();
							saveSlot(inv, playerName, name);
							plugin.log.info("Converted slot " + playerName + File.separator + name);
							f.delete();
							n++;
						} catch (Exception e) {
							plugin.log.severe("Failed to convert slot " + playerName + File.separator + name);
							plugin.printException(e);
							continue;
						}
					}
					if (d.list().length == 0) d.delete();
				}
			} // Skip non directories
		}
		if (mainFile.list().length == 0) mainFile.delete();
		if (new File("." + File.separator + "plugins" + File.separator + "Bank").list().length == 1) new File("." + File.separator + "plugins" + File.separator + "Bank").delete();
		plugin.log.info("Converted " + n + " files in " + (System.currentTimeMillis() - start) + "ms.");
	}
}
