package net.fuzzykiwi3.paleontologyunearthed.item.custom;

import com.google.common.collect.ImmutableMap.Builder;

import net.fuzzykiwi3.paleontologyunearthed.block.ModBlocks;
import net.fuzzykiwi3.paleontologyunearthed.block.custom.ModChiselTempBlockstate;
import net.fuzzykiwi3.paleontologyunearthed.sound.ModSounds;
import net.fuzzykiwi3.paleontologyunearthed.util.ModTags;
import net.minecraft.block.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.Map;

public class ChiselItem extends ToolItem {
    private static final int MAX_CHISEL_TIME = 200;
    private static final int SOUND_INTERVAL = 10;
    private static final int COOLDOWN_TICKS = 10;

    // Tweakable: ~40 ticks to finish on “typical” blocks; adjust to taste.
    private static final float TICKS_TO_FINISH_BASELINE = 90f;

    private final ToolMaterial material;
    private float progress = 0;
    private int cooldown = 0;
    private BlockPos lastBlockPos = null;
    private int soundTickCounter = 0;

    private static final Map<Block, Block> CHISEL_MAP = new Builder<Block, Block>()
            .put(ModBlocks.STONE_BRICKS_TEMP, Blocks.CHISELED_STONE_BRICKS)
            .put(Blocks.COBBLED_DEEPSLATE, Blocks.CHISELED_DEEPSLATE)
            .put(Blocks.TUFF, Blocks.CHISELED_TUFF)
            .put(Blocks.TUFF_BRICKS, Blocks.CHISELED_TUFF_BRICKS)
            .put(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE)
            .put(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE)
            .put(Blocks.NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS)
            .put(Blocks.POLISHED_BLACKSTONE, Blocks.CHISELED_POLISHED_BLACKSTONE)
            .put(Blocks.QUARTZ_BLOCK, Blocks.CHISELED_QUARTZ_BLOCK)
            .put(ModBlocks.SUSPICIOUS_STONE, ModBlocks.SUSPICIOUS_COBBLESTONE)
            .put(ModBlocks.SUSPICIOUS_COBBLESTONE, Blocks.SUSPICIOUS_GRAVEL)
            .put(ModBlocks.SUSPICIOUS_SANDSTONE, Blocks.SUSPICIOUS_SAND)
            .build();

    // original -> temp
    private static final Map<Block, Block> TEMP_MAP = new Builder<Block, Block>()
            .put(Blocks.STONE_BRICKS, ModBlocks.STONE_BRICKS_TEMP)
            .build();

    // NEW: temp -> original (for cancel revert)
    private static final Map<Block, Block> TEMP_TO_SOURCE = new Builder<Block, Block>()
            .put(ModBlocks.STONE_BRICKS_TEMP, Blocks.STONE_BRICKS)
            .build();

    public ChiselItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, settings);
        this.material = toolMaterial;
    }

    private float getProgressPerTick(Block currentBlock) {
        // If we’re on a temp block, measure hardness based on the original block it represents.
        Block hardnessBlock = TEMP_TO_SOURCE.getOrDefault(currentBlock, currentBlock);

        // Be defensive: avoid division by zero or negative hardness.
        float hardness = Math.max(0.1f, hardnessBlock.getHardness());
        float speed = Math.max(0.1f, material.getMiningSpeedMultiplier());

        // Calibrated so common blocks finish comfortably < MAX_CHISEL_TIME.
        return (speed / hardness) / TICKS_TO_FINISH_BASELINE;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && getHitResult(player).getType() == HitResult.Type.BLOCK) {
            BlockState state = context.getWorld().getBlockState(context.getBlockPos());
            boolean alreadyTempOrSpecial = CHISEL_MAP.containsKey(state.getBlock());
            if (state.isIn(ModTags.Blocks.IS_CHISELABLE) || alreadyTempOrSpecial) {
                World world = context.getWorld();
                if (!world.isClient() && !alreadyTempOrSpecial) {
                    Block temp = TEMP_MAP.get(state.getBlock());
                    if (temp != null) {
                        world.setBlockState(context.getBlockPos(), temp.getDefaultState());
                    }
                }
                player.setCurrentHand(context.getHand());
            }
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        resetChisel(user, true);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return cooldown == 0 ? UseAction.BRUSH : UseAction.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return MAX_CHISEL_TIME;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            resetChisel(user, true);
            return;
        }

        HitResult hitResult = getHitResult(player);
        if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK) {
            resetChisel(user, true);
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        boolean blockIsChiselable = CHISEL_MAP.containsKey(block);
        if (!blockIsChiselable) {
            resetChisel(user, true);
            return;
        }

        if (lastBlockPos == null || !lastBlockPos.equals(pos)) {
            if (lastBlockPos != null
                    && !(world.getBlockState(lastBlockPos).isIn(ModTags.Blocks.IS_CHISELABLE)
                    || CHISEL_MAP.containsKey(world.getBlockState(lastBlockPos).getBlock()))
                    && blockIsChiselable) {
                soundTickCounter = 0;
            }
            progress = 0;
        }
        lastBlockPos = pos;

        if (cooldown == 0) {
            if (soundTickCounter == 0) {
                addDustParticles(world, blockHit, state, user.getRotationVec(0), player.getMainArm());
            }

            if (!world.isClient()) {
                progress += getProgressPerTick(block);

                if (progress < 1f) {

                    if (progress < 0.33f) {
                        world.setBlockState(pos, state.with(ModChiselTempBlockstate.STAGE, 1), Block.NOTIFY_ALL);
                    } else if (progress < 0.66f) {
                        world.setBlockState(pos, state.with(ModChiselTempBlockstate.STAGE, 2), Block.NOTIFY_ALL);
                    } else {
                        world.setBlockState(pos, state.with(ModChiselTempBlockstate.STAGE, 3), Block.NOTIFY_ALL);
                    }

                    if (soundTickCounter == 0 && user instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                                Registries.SOUND_EVENT.getEntry(ModSounds.CHISEL_USE),
                                SoundCategory.BLOCKS,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                1.0f, 1.0f, 0L));
                    }
                    soundTickCounter++;
                    if (soundTickCounter >= SOUND_INTERVAL) soundTickCounter = 0;

                } else {
                    // Finish
                    if (user instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                                Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_GRINDSTONE_USE),
                                SoundCategory.BLOCKS,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                1.0f, 1.0f, 0L));
                    }

                    world.setBlockState(pos, CHISEL_MAP.get(block).getDefaultState(), Block.NOTIFY_ALL);
                    stack.damage(1, (ServerWorld) world, (ServerPlayerEntity) user,
                            item -> user.sendEquipmentBreakStatus(item, EquipmentSlot.MAINHAND));

                    resetChisel(user, false);
                }
            }
        }

        if (cooldown > 0) cooldown--;
    }

    /** Unified reset for progress, cooldown, and sound */
    private void resetChisel(LivingEntity user, boolean cancel) {
        progress = 0f;
        soundTickCounter = 0;

        if (cancel && lastBlockPos != null) {
            World world = user.getWorld();
            if (!world.isClient()) {
                BlockState current = world.getBlockState(lastBlockPos);
                Block currentBlock = current.getBlock();

                // Revert temp -> original if possible
                Block source = TEMP_TO_SOURCE.get(currentBlock);
                if (source != null) {
                    world.setBlockState(lastBlockPos, source.getDefaultState(), Block.NOTIFY_ALL);
                }
                // If it's a temp block but no source exists, reset its stage property to 0
                else if (currentBlock instanceof ModChiselTempBlockstate) {
                    BlockState reset = currentBlock.getDefaultState();
                    if (reset.contains(ModChiselTempBlockstate.STAGE)) {
                        reset = reset.with(ModChiselTempBlockstate.STAGE, 0);
                    }
                    world.setBlockState(lastBlockPos, reset, Block.NOTIFY_ALL);
                }
            }
        }

        lastBlockPos = null;
        cooldown = COOLDOWN_TICKS;

        // Safely clear item use
        if (user.isUsingItem() && user.getActiveItem().getItem() == this) {
            user.clearActiveItem();
        }
    }

    private HitResult getHitResult(PlayerEntity user) {
        return ProjectileUtil.getCollision(user,
                e -> !e.isSpectator() && e.canHit(),
                user.getBlockInteractionRange());
    }

    private void addDustParticles(World world, BlockHitResult hit, BlockState state, Vec3d rotation, Arm arm) {
        int directionMultiplier = arm == Arm.RIGHT ? 1 : -1;
        int count = world.getRandom().nextBetweenExclusive(7, 12);
        BlockStateParticleEffect effect = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
        Direction side = hit.getSide();
        DustParticlesOffset offset = DustParticlesOffset.fromSide(rotation, side);
        Vec3d pos = hit.getPos();

        for (int k = 0; k < count; k++) {
            world.addParticle(effect,
                    pos.x - (side == Direction.WEST ? 1e-6 : 0),
                    pos.y,
                    pos.z - (side == Direction.NORTH ? 1e-6 : 0),
                    offset.xd() * directionMultiplier * 3 * world.getRandom().nextDouble(),
                    0,
                    offset.zd() * directionMultiplier * 3 * world.getRandom().nextDouble());
        }
    }

    record DustParticlesOffset(double xd, double yd, double zd) {
        public static DustParticlesOffset fromSide(Vec3d rotation, Direction side) {
            return switch (side) {
                case DOWN, UP -> new DustParticlesOffset(rotation.getZ(), 0, -rotation.getX());
                case NORTH -> new DustParticlesOffset(1, 0, -0.1);
                case SOUTH -> new DustParticlesOffset(-1, 0, 0.1);
                case WEST -> new DustParticlesOffset(-0.1, 0, -1);
                case EAST -> new DustParticlesOffset(0.1, 0, 1);
            };
        }
    }
}
