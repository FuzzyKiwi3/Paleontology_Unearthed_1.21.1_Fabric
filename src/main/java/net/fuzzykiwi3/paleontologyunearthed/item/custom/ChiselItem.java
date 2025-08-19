package net.fuzzykiwi3.paleontologyunearthed.item.custom;

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
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.Map;

public class ChiselItem extends ToolItem {
    private static final int MAX_CHISEL_TIME = 200;
    private static final int SOUND_INTERVAL = 15; // ticks between chisel sounds
    private static final int COOLDOWN_TICKS = 10;

    private final ToolMaterial material;
    private float progress = 0;
    private int cooldown = 0;
    private BlockPos lastBlockPos = null;
    private int soundTickCounter = 0;

    private static final Map<Block, Block> CHISEL_MAP = Map.of(
            Blocks.STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS,
            Blocks.COBBLED_DEEPSLATE, Blocks.CHISELED_DEEPSLATE,
            Blocks.TUFF, Blocks.CHISELED_TUFF,
            Blocks.TUFF_BRICKS, Blocks.CHISELED_TUFF_BRICKS,
            Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE,
            Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE,
            Blocks.NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS,
            Blocks.POLISHED_BLACKSTONE, Blocks.CHISELED_POLISHED_BLACKSTONE,
            Blocks.QUARTZ_BLOCK, Blocks.CHISELED_QUARTZ_BLOCK
    );

    public ChiselItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, settings);
        this.material = toolMaterial;
    }

    private float getProgressPerTick(Block block) {
        // Increase float to increase chisel time
        return block.getDefaultState().isIn(ModTags.Blocks.IS_CHISELABLE)
                ? (material.getMiningSpeedMultiplier() / block.getHardness()) / 90f
                : 0;
    }

    private boolean isComplete() {
        return progress >= 1;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && getHitResult(player).getType() == HitResult.Type.BLOCK) {
            player.setCurrentHand(context.getHand());
        }
        return ActionResult.CONSUME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BRUSH;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return MAX_CHISEL_TIME;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            resetChisel(user);
            return;
        }

        HitResult hitResult = getHitResult(player);
        if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK) {
            resetChisel(user);
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        boolean blockIsChiselable = state.isIn(ModTags.Blocks.IS_CHISELABLE) && CHISEL_MAP.containsKey(block);

        // Reset progress only if we moved to a new block
        if (lastBlockPos == null || !lastBlockPos.equals(pos)) {
            if (lastBlockPos != null
                && !world.getBlockState(lastBlockPos).isIn(ModTags.Blocks.IS_CHISELABLE)
                && blockIsChiselable) {
                soundTickCounter = 0;
            }
            progress = 0;
        }
        lastBlockPos = pos;

        if (blockIsChiselable && soundTickCounter == 0) {
            addDustParticles(world, blockHit, state, user.getRotationVec(0), player.getMainArm());
        }

        // Chiseling Progress & Completion
        if (blockIsChiselable && !world.isClient()) {
            progress += getProgressPerTick(block);

            if (progress < 1) {
                if (soundTickCounter == 0) {
                    if (user instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                                Registries.SOUND_EVENT.getEntry(ModSounds.CHISEL_USE),
                                SoundCategory.BLOCKS,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                1.0f, 1.0f, 0L));
                    }
                }
                soundTickCounter++;
                if (soundTickCounter >= SOUND_INTERVAL) {
                    soundTickCounter = 0;
                }
            }

            else if (progress >= 1) {
                // Play only the grindstone completion sound
                if (user instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                            Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_GRINDSTONE_USE),
                            SoundCategory.BLOCKS,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            1.0f, 1.0f, 0L));
                }

                world.setBlockState(pos, CHISEL_MAP.get(block).getDefaultState());
                stack.damage(1, (ServerWorld) world, (ServerPlayerEntity) user,
                        item -> user.sendEquipmentBreakStatus(item, EquipmentSlot.MAINHAND));

                resetChisel(user);
            }
        }

        // Decrease cooldown if active
        if (cooldown > 0) cooldown--;
    }

    /** Unified reset for progress, cooldown, and sound */
    private void resetChisel(LivingEntity user) {
        progress = 0;
        soundTickCounter = 0;
        lastBlockPos = null;
        cooldown = COOLDOWN_TICKS;
        user.stopUsingItem();
    }

    private HitResult getHitResult(PlayerEntity user) {
        return ProjectileUtil.getCollision(user,
                e -> !e.isSpectator() && e.canHit(),
                user.getBlockInteractionRange());
    }

    private void addDustParticles(World world, BlockHitResult hit, BlockState state, Vec3d rotation, Arm arm) {
        int directionMultiplier = arm == Arm.RIGHT ? 1 : -1;
        int count = world.getRandom().nextBetweenExclusive(7, 12); // number of particles per sound
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