package net.fuzzykiwi3.paleontologyunearthed.util;

import net.fuzzykiwi3.paleontologyunearthed.PaleontologyUnearthed;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> IS_CHISELABLE = createTag("is_chiselable");

        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(PaleontologyUnearthed.MOD_ID, name));
        }
    }

    public static class Items {

        // Add tags here:


        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(PaleontologyUnearthed.MOD_ID, name));
        }
    }
}
