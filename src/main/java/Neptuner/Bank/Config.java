package Neptuner.Bank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.HashSet;

import Neptuner.Bank.Plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	// Here, you add all the variables that contain your config.
	public double slotPrice;
	public int maxSlots;
	public double priceIncrease;

	private FileConfiguration config;
	private Plugin plugin; //this is the plugin
	public Config(Plugin p) {
		plugin = p;
	}

	public void load() {
		boolean exist = new File(plugin.getDataFolder(), "config.yml").exists();
		if(!exist) { //No config found
			plugin.log.info("No configuration detected, saving defaults");
			plugin.saveDefaultConfig();
		}
		config = plugin.getConfig(); //getConfig() >> this is the config file, located in the plugin folder
		//Get values from existing file
		slotPrice = config.getDouble("config.slot.price", 100.0D);
		maxSlots = config.getInt("config.slot.maximum", 20);
		if(maxSlots < 0) maxSlots = 0; //Fallback for negative values.
		priceIncrease = config.getDouble("config.slot.increase", 50.0D);

		if(exist) {
			//Load default configuration from JAR
			InputStream defConfigStream = plugin.getResource("config.yml"); 
			FileConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			//Initialize paths for check
			HashSet<String> paths = new HashSet<String>();
			for(String s : defConfig.getKeys(true))
				paths.add(s);
			//OUTDATED check >>
			boolean isOutdated = false;
			//Check for MISSING paths (NEW in jar/config.yml)
			for(String s : paths) {
				isOutdated = isOutdated || !config.contains(s);
				if(isOutdated) break;
			}
			//Check for NONEXISTING paths (DELETED in jar/config.yml)
			if(!isOutdated)
				for(String s : config.getKeys(true))
					if(!paths.contains(s)) {
						isOutdated = true;
						break;
					}
			//If outdated, update it.
			if(isOutdated) {
				plugin.log.info("Outdated configuration! Migrating configuration...");
				plugin.log.info("Taking a backup (config_backup.yml)...");
				//BACKUPS :D
				File theConfig = new File(plugin.getDataFolder(), "config.yml");
				File theConfigBackup = new File(plugin.getDataFolder(), "config_backup.yml");
				try {
					if(theConfigBackup.exists()) theConfigBackup.delete();
					theConfigBackup.createNewFile();

					FileChannel source = null;
					FileChannel destination = null;
					try {
						source = new FileInputStream(theConfig).getChannel();
						destination = new FileOutputStream(theConfigBackup).getChannel();

						long count = 0;
						while((count += destination.transferFrom(source, count, source.size() - count)) < source.size());
					} finally {
						if(source != null) source.close();
						if(destination != null) destination.close();
					}
				} catch (Exception e) {
					plugin.log.warning("Error while creating backup. Attempting to migrate anyway");
				}
				//Backup done, starting migration.
				FileConfiguration newConfig = new YamlConfiguration();
				//Set all values
				newConfig.set("config.slot.price", slotPrice);
				newConfig.set("config.slot.maximum", maxSlots);
				newConfig.set("config.slot.increase", priceIncrease);

				//Overwrite the original config
				try {
					newConfig.save(theConfig);
				} catch (IOException e) {
					plugin.log.warning("Failed to migrate configuration! Using defaults.");
					plugin.saveDefaultConfig(); return;
				}
				//Append comment information at the end
				try {
					FileWriter fstream = new FileWriter(theConfig, true);
					BufferedWriter fbw = new BufferedWriter(fstream);
					//Start writing
					fbw.newLine();
					fbw.write("#" + plugin.description.getName() + " version " + plugin.description.getVersion() + " configuration"); fbw.newLine();
					fbw.write("#Please refer to http://dev.bukkit.org/server-mods/skybank/pages/configuration/"); fbw.newLine();
					fbw.write("#for more information on the configuration.");
					//Close our writer
					fbw.close();
				} catch (Exception e) {}
				//Migrated :D
				plugin.log.info("Configuration migrated successfully!");
			}
		}	
	}
}