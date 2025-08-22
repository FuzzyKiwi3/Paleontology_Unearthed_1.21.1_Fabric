package net.fuzzykiwi3.paleontologyunearthed.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fuzzykiwi3.paleontologyunearthed.block.ModBlocks;
import net.fuzzykiwi3.paleontologyunearthed.block.custom.ModChiselTempBlockstate;
import net.fuzzykiwi3.paleontologyunearthed.item.ModItems;
import net.minecraft.data.client.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        Identifier chiselState0Identifier = TexturedModel.CUBE_ALL.upload(ModBlocks.STONE_BRICKS_TEMP, blockStateModelGenerator.modelCollector);
        Identifier chiselState1Identifier = blockStateModelGenerator.createSubModel(ModBlocks.STONE_BRICKS_TEMP, "_1", Models.CUBE_ALL, TextureMap::all);
        Identifier chiselState2Identifier = blockStateModelGenerator.createSubModel(ModBlocks.STONE_BRICKS_TEMP, "_2", Models.CUBE_ALL, TextureMap::all);
        Identifier chiselState3Identifier = blockStateModelGenerator.createSubModel(ModBlocks.STONE_BRICKS_TEMP, "_3", Models.CUBE_ALL, TextureMap::all);

        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(ModBlocks.STONE_BRICKS_TEMP)
                        .coordinate(BlockStateVariantMap.create(ModChiselTempBlockstate.STAGE)
                                .register(0, BlockStateVariant.create().put(VariantSettings.MODEL, chiselState0Identifier))
                                .register(1, BlockStateVariant.create().put(VariantSettings.MODEL, chiselState1Identifier))
                                .register(2, BlockStateVariant.create().put(VariantSettings.MODEL, chiselState2Identifier))
                                .register(3, BlockStateVariant.create().put(VariantSettings.MODEL, chiselState3Identifier))
                        )
        );

        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.SUSPICIOUS_STONE);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.SUSPICIOUS_COBBLESTONE);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.SUSPICIOUS_SANDSTONE);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.WOODEN_CHISEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.STONE_CHISEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.IRON_CHISEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.GOLDEN_CHISEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.DIAMOND_CHISEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.NETHERITE_CHISEL, Models.HANDHELD);
    }
}
