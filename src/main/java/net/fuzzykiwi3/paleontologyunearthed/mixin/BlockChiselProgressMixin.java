package net.fuzzykiwi3.paleontologyunearthed.mixin;

import net.fuzzykiwi3.paleontologyunearthed.util.BlockChiselProgressMixinInterface;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public class BlockChiselProgressMixin implements BlockChiselProgressMixinInterface {

    private float CHISEL_PROGRESS = 0;

    @Override
    public void setChiselProgress(float chiselProgress) {
        CHISEL_PROGRESS = chiselProgress;
    }

    @Override
    public void increaseChiselProgress(float increase) {
        setChiselProgress(getChiselProgress() + increase);
    }

    @Override
    public float getChiselProgress() {
        return CHISEL_PROGRESS;
    }
}
