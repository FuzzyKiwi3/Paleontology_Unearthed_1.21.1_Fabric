package net.fuzzykiwi3.paleontologyunearthed;

import net.fabricmc.api.ModInitializer;

import net.fuzzykiwi3.paleontologyunearthed.block.ModBlocks;
import net.fuzzykiwi3.paleontologyunearthed.item.ModItemGroups;
import net.fuzzykiwi3.paleontologyunearthed.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaleontologyUnearthed implements ModInitializer {
	public static final String MOD_ID = "paleontologyunearthed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

        ModItemGroups.registerItemGroups();
        ModBlocks.registerModBlocks();
        ModItems.registerModItems();

	}
}