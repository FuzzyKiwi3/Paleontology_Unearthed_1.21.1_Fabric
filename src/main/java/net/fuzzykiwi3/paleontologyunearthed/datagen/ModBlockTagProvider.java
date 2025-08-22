package net.fuzzykiwi3.paleontologyunearthed.datagen;

import net.fabricmc.fabric.api.block.v1.BlockFunctionalityTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fuzzykiwi3.paleontologyunearthed.block.ModBlocks;
import net.fuzzykiwi3.paleontologyunearthed.util.ModTags;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ModTags.Blocks.IS_CHISELABLE)
                .add(Blocks.STONE_BRICKS)
                .add(Blocks.COBBLED_DEEPSLATE)
                .add(Blocks.TUFF)
                .add(Blocks.TUFF_BRICKS)
                .add(Blocks.SANDSTONE)
                .add(Blocks.RED_SANDSTONE)
                .add(Blocks.NETHER_BRICKS)
                .add(Blocks.POLISHED_BLACKSTONE)
                .add(Blocks.QUARTZ_BLOCK)
                .add(ModBlocks.SUSPICIOUS_STONE)
                .add(ModBlocks.SUSPICIOUS_COBBLESTONE)
                .add(ModBlocks.SUSPICIOUS_SANDSTONE);

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.STONE_BRICKS_TEMP)

                .add(ModBlocks.SUSPICIOUS_STONE)
                .add(ModBlocks.SUSPICIOUS_COBBLESTONE)
                .add(ModBlocks.SUSPICIOUS_SANDSTONE);
    }
}
