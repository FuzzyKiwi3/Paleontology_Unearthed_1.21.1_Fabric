package net.fuzzykiwi3.paleontologyunearthed.block;

import net.fuzzykiwi3.paleontologyunearthed.PaleontologyUnearthed;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    // Add blocks here:


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(PaleontologyUnearthed.MOD_ID, name), block);
    }


    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(PaleontologyUnearthed.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }


    public static void registerModBlocks() {
        PaleontologyUnearthed.LOGGER.info("Registering Mod Blocks for " + PaleontologyUnearthed.MOD_ID);
    }
}
