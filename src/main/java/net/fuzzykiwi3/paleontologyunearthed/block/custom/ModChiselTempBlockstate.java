package net.fuzzykiwi3.paleontologyunearthed.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;

public class ModChiselTempBlockstate extends Block {
    public static final IntProperty STAGE = IntProperty.of("stage", 0, 3);

    public ModChiselTempBlockstate(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(STAGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }
}
