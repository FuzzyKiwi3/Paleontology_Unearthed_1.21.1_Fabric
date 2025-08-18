package net.fuzzykiwi3.paleontologyunearthed.item;

import net.fuzzykiwi3.paleontologyunearthed.PaleontologyUnearthed;
import net.fuzzykiwi3.paleontologyunearthed.item.custom.ChiselItem;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    // Add items here:
    public static final Item WOODEN_CHISEL = registerItem("wooden_chisel",
            new ChiselItem(ToolMaterials.WOOD, new Item.Settings().maxCount(1)));
    public static final Item STONE_CHISEL = registerItem("stone_chisel",
            new ChiselItem(ToolMaterials.STONE, new Item.Settings().maxCount(1)));
    public static final Item IRON_CHISEL = registerItem("iron_chisel",
            new ChiselItem(ToolMaterials.IRON, new Item.Settings().maxCount(1)));
    public static final Item GOLDEN_CHISEL = registerItem("golden_chisel",
            new ChiselItem(ToolMaterials.GOLD, new Item.Settings().maxCount(1)));
    public static final Item DIAMOND_CHISEL = registerItem("diamond_chisel",
            new ChiselItem(ToolMaterials.DIAMOND, new Item.Settings().maxCount(1)));
    public static final Item NETHERITE_CHISEL = registerItem("netherite_chisel",
            new ChiselItem(ToolMaterials.NETHERITE, new Item.Settings().maxCount(1)));


    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(PaleontologyUnearthed.MOD_ID, name), item);
    }


    public static void registerModItems() {
        PaleontologyUnearthed.LOGGER.info("Registering Mod Items for " + PaleontologyUnearthed.MOD_ID);
    }
}