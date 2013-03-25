package Neptuner.Bank;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;
import Neptuner.Bank.BankCommand;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin{
	BankBlockListener blocklistener;
	public Logger log;
	PluginDescriptionFile description;
	public Config config;
	public SlotLoader loader;
	public BankMap bm;
	public BankCommand cmd;

	public Vault vault;
	public Economy eco;
	public boolean economyEnabled;

	public void onEnable(){
		log = this.getLogger();
		config = new Config(this);
		config.load(); //load the config :)
		bm = new BankMap();
		loader = new SlotLoader(this);
		cmd = new BankCommand(this);

		PluginManager pm = getServer().getPluginManager();
		description = getDescription();

		new File(getDataFolder(), "Slots" + File.separator).mkdir();

		org.bukkit.plugin.Plugin v = null;
		try {
			v = getServer().getPluginManager().getPlugin("Vault");
		} catch (Exception e) {}
		
		if (v == null) {
			log.warning("Vault plugin not found. Skybank partially disabled.");
			economyEnabled = false;
		} else {
			vault = (Vault) v;
			try {
				eco = vault.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
			} catch (Exception e) {
				printException(e);
			}
			
			economyEnabled = eco != null;
			if(!economyEnabled)
				log.warning("Vault plugin found, but reported no economy system. Skybank partially disabled.");
			else
				log.info("Vault plugin found, using economy plugin: " + eco.getName());
		}

		pm.registerEvents(new BankBlockListener(this), this);
		getCommand("bank").setExecutor(cmd);

		if(new File("." + File.separator + "plugins" + File.separator + "Bank" + File.separator + "Slots" + File.separator).exists()) {
			if(new File("." + File.separator + "plugins" + File.separator + "Bank" + File.separator + "Slots" + File.separator).list().length > 0) {
				log.info("Found old slots, attempting to convert...");
				loader.convertAllSlots();
				log.info("Done converting slots.");
			}
		}

		log.info(description.getName() + " " + description.getVersion() + " is now enabled.");
	}

	public void onDisable(){
		log.info("Saving open slots...");
		for(Player p : getServer().getOnlinePlayers()){
			if(bm.getSlot(p.getName()) != null){
				try {
					loader.saveSlot(bm.getCurrentSlotContents(p.getName()), p.getName(), bm.getSlot(p.getName()));
				} catch (Exception e) {
					log.severe("ERROR: Failed saving slot " + p.getName() + "/" + bm.getSlot(p.getName()));
					printException(e);
				}
				bm.setCurrentSlotContents(p.getName(), null);
				bm.setSlot(p.getName(), null);
			}
		}
		log.info(description.getName() + " " + description.getVersion() + " has been disabled.");
	}

	public Inventory emptyInv() {
		return emptyInv(null, null);
	}
	public Inventory emptyInv(String name) {
		return emptyInv(name, null);
	}
	public Inventory emptyInv(String name, Player holder) {
		return getServer().createInventory(holder, 54, name);
	}

	//Economy
	public double getMoney(Player p) {
		if(!economyEnabled) return 0;
		return eco.getBalance(p.getName());
	}
	public void takeMoney(Player p, double amount) {
		if(economyEnabled) eco.withdrawPlayer(p.getName(), amount);
	}

	public void printException(Exception e) {
		log.severe("An exception occured. Please notify the plugin developpers via a BukkitDev ticket.");
		log.severe("Exception details: " + e.getMessage());
		final Writer w = new StringWriter();
		final PrintWriter pw = new PrintWriter(w);
		e.printStackTrace(pw);
		log.severe(w.toString());
		try {
			w.close();
			pw.close();
		} catch (Exception e1) {}
	}
}

