package Neptuner.Bank;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.block.Block;

public class BankBlockListener implements Listener {
	Plugin plugin;
	Config config;
	SlotLoader loader;
	BankMap bm;
	Logger log;

	public ArrayList<String> signLines;
	public ArrayList<BlockFace> blockFaces;

	public BankBlockListener(Plugin instance){
		plugin = instance;
		config = plugin.config;
		loader = plugin.loader;
		bm = plugin.bm;
		log = plugin.log;
		//Setup the sign lines
		signLines = new ArrayList<String>();
		signLines.add("[Bank Open]");
		signLines.add("[Bank Buy]");
		signLines.add("[Bank Price]");
		signLines.add("[Bank List]");
		signLines.add("[Bank Delete]");
		//Setup possible blockfaces
		blockFaces = new ArrayList<BlockFace>();
		blockFaces.add(BlockFace.SELF);
		blockFaces.add(BlockFace.UP);
		blockFaces.add(BlockFace.EAST);
		blockFaces.add(BlockFace.NORTH);
		blockFaces.add(BlockFace.SOUTH);
		blockFaces.add(BlockFace.WEST);
	}

	boolean isSignLine(String s) {
		// Needed, for case-unsensitiveness
		for(String sl : signLines)
			if(sl.equalsIgnoreCase(s)) return true;
		return false;
	}
	boolean isSign(Block b) {
		return isSign(b.getType());
	}
	boolean isSign(Material m) {
		return m == Material.SIGN_POST || m == Material.WALL_SIGN;
	}
	boolean checkFace(BlockFace f, Block b) { //Relative face, the block (faced)
		if(blockFaces.contains(f) && isSign(b))
			try {
				switch(f) {
				case SELF:
					return isSign(b);
				case UP:
					return b.getType() == Material.SIGN_POST;
				case EAST: case NORTH: case SOUTH: case WEST:
					return (b.getType() == Material.WALL_SIGN) && (((org.bukkit.material.Sign)b.getState().getData()).getFacing() == f);
				}
			} catch (Exception e) { return false; } //This happens if it's not a sign, normally.
		return false;
	}
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		if(bm.getSlot(player.getName())!=null){
			String bankslot = bm.getSlot(player.getName());
			try {
				if(bankslot.contains("/")) {
					loader.saveSlot(event.getInventory(), bankslot.split("/")[0], bankslot.split("/")[1]);
				} else {
					loader.saveSlot(event.getInventory(), player.getName(), bankslot);
				}
				bm.setSlot(player.getName(), null);
				bm.setCurrentSlotContents(player.getName(), null);
			} catch (Exception e) {
				player.sendMessage(Messages.internalError);
				plugin.printException(e);
			}
		}
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		
		//Fixes closing the client unexpectedly :)
		player.closeInventory();
		
		//Clear out the values
		bm.setSlot(player.getName(), null);
		bm.setCurrentSlotContents(player.getName(), null);
		bm.setSign(player.getName(), null);
	} 
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if(player != null && bm.getSlot(player.getName()) != null && event.getInventory() instanceof Player)
			bm.setCurrentSlotContents(player.getName(), event.getInventory());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onSignClick(PlayerInteractEvent event){
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Player player = event.getPlayer();
			Block block = event.getClickedBlock();
			try {
				if(isSign(block)) {
					Sign sign = (Sign) block.getState();
					if(isSignLine(sign.getLine(0))) {
						event.setCancelled(true);
						
						if(sign.getLine(0).equals(signLines.get(0)) && player.hasPermission("bank.sign.open")) { //[Bank Open]
							player.sendMessage(Messages.enterSlotNameOpen);
							bm.setSign(player.getName(), "open");
						} else if(sign.getLine(0).equals(signLines.get(1)) && player.hasPermission("bank.sign.buy")) { //[Bank Buy]
							player.sendMessage(Messages.enterSlotNameBuy);
							bm.setSign(player.getName(), "buy");
						} else if(sign.getLine(0).equals(signLines.get(2)) && player.hasPermission("bank.sign.price")) { //[Bank Price]
							loader.cmdNewSlotPrice(player);
						} else if(sign.getLine(0).equals(signLines.get(3)) && player.hasPermission("bank.sign.list")) { //[Bank List]
							loader.printAllSlots(player);
						} else if(sign.getLine(0).equals(signLines.get(4)) && player.hasPermission("bank.sign.delete")) { //[Bank Delete]
							player.sendMessage(Messages.enterSlotNameDelete);
							bm.setSign(player.getName(), "delete");
						} else {
							event.setCancelled(false); //This is needed
						}
					}
				}
			}catch (Exception e){
				player.sendMessage(Messages.internalError);
				plugin.printException(e);
			}
		}

	}
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event){
		Player player = event.getPlayer();
		if(isSignLine(event.getLine(0)))
			for(String s : signLines)
				if(event.getLine(0).equalsIgnoreCase(s)) {
					if(player.hasPermission("bank.sign.place")) {
						event.setLine(0, s);
						player.sendMessage(Messages.signRegistered);
					} else event.setLine(0, ChatColor.RED + s + ChatColor.RED); //The last + ChatColor.RED is to center it properly ;p
					break;
				}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onSignBreak(BlockBreakEvent event){
		Block b = event.getBlock();
		Player p = event.getPlayer();
		Sign s;
		for(BlockFace f : blockFaces) {
			if(checkFace(f, b.getRelative(f))) {
				s = (Sign) b.getRelative(f).getState();
				if(isSignLine(s.getLine(0))) {
					if(p.hasPermission("bank.sign.break")){
						p.sendMessage(Messages.signRemoved);
					} else {
						event.setCancelled(true);
						p.sendMessage(Messages.noBreak);
						s.update();
					}
					break;
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent event){
		if(!event.getTo().getBlock().equals(event.getFrom().getBlock())) {
			Player player = event.getPlayer();
			if(bm.getSign(player.getName()) != null){
				player.sendMessage(Messages.bankingCancelled);
				bm.setSign(player.getName(), null);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onChat(PlayerChatEvent event){
		Player p = event.getPlayer();
		String s = event.getMessage();
		String active = bm.getSign(p.getName());

		if(active != null) {
			event.setCancelled(true); //Dont show the message.
			bm.setSign(p.getName(), null);
			if(active.equalsIgnoreCase("delete")) {
				loader.deleteSlot(p, s);
			} else if(active.equalsIgnoreCase("open")) {
				loader.openSlot(p, s);
			} else if(active.equalsIgnoreCase("buy")) {
				loader.buySlot(p, s);
			}
		}
	}
}