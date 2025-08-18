package net.fuzzykiwi3.paleontologyunearthed.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fuzzykiwi3.paleontologyunearthed.PaleontologyUnearthed;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup PALEONTOLOGY_UNEARTHED_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(PaleontologyUnearthed.MOD_ID, "paleontology_unearthed_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(Items.BRUSH))
                    .displayName(Text.translatable("itemgroup.paleontologyunearthed.paleontology_unearthed_items"))
                    .entries((displayContext, entries) -> {

                        // Add entries here:
                        entries.add(ModItems.WOODEN_CHISEL);
                        entries.add(ModItems.STONE_CHISEL);
                        entries.add(ModItems.IRON_CHISEL);
                        entries.add(ModItems.GOLDEN_CHISEL);
                        entries.add(ModItems.DIAMOND_CHISEL);
                        entries.add(ModItems.NETHERITE_CHISEL);

                    }).build());


    public static void registerItemGroups() {
        PaleontologyUnearthed.LOGGER.info("Registering Item Groups for " + PaleontologyUnearthed.MOD_ID);
    }
}
