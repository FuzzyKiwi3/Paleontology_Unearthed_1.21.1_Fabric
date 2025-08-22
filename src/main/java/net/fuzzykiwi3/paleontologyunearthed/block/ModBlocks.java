package net.fuzzykiwi3.paleontologyunearthed.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fuzzykiwi3.paleontologyunearthed.PaleontologyUnearthed;
import net.fuzzykiwi3.paleontologyunearthed.block.custom.ModChiselTempBlockstate;
import net.fuzzykiwi3.paleontologyunearthed.block.custom.ModSuspiciousBlock;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    // Add blocks here:
    public static final Block STONE_BRICKS_TEMP = registerBlock("stone_bricks_temp", new ModChiselTempBlockstate(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS)));

    public static final Block SUSPICIOUS_STONE = registerBlock("suspicious_stone", new ModSuspiciousBlock(FabricBlockSettings.copyOf(Blocks.STONE)));
    public static final Block SUSPICIOUS_COBBLESTONE = registerBlock("suspicious_cobblestone", new ModSuspiciousBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE)));
    public static final Block SUSPICIOUS_SANDSTONE = registerBlock("suspicious_sandstone", new ModSuspiciousBlock(FabricBlockSettings.copyOf(Blocks.SANDSTONE)));

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
