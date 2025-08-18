package net.fuzzykiwi3.paleontologyunearthed.item.custom;

import net.fuzzykiwi3.paleontologyunearthed.util.BlockChiselProgressMixinInterface;
import net.fuzzykiwi3.paleontologyunearthed.util.ModTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;

public class ChiselItem extends ToolItem {
    private static final int MAX_CHISEL_TIME = 200;
    private ToolMaterial CHISEL_MATERIAL;
    private float CHISEL_PROGRESS = 0;

    // Help from KaupenJoe's 1.21 Fabric Modding tutorial

    private static final Map<Block, Block> CHISEL_MAP = Map.of(
            // Make sure to add blocks to the tag
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

        CHISEL_MATERIAL = toolMaterial;
    }


    private float getChiselProgressPerTick(Block block) {
        return (CHISEL_MATERIAL.getMiningSpeedMultiplier() / block.getHardness()) / 30;
    }


    private boolean chiselingIsComplete(BlockChiselProgressMixinInterface block) {
        return block.getChiselProgress() >= 1;
    }


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity != null && this.getHitResult(playerEntity).getType() == HitResult.Type.BLOCK) {
            playerEntity.setCurrentHand(context.getHand());
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
        if (remainingUseTicks >= 0 && user instanceof PlayerEntity playerEntity) {
            HitResult hitResult = this.getHitResult(playerEntity);
            if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK) {
                int i = this.getMaxUseTime(stack, user) - remainingUseTicks + 1;
                boolean bl = i % 10 == 5;
                if (bl) {
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    BlockState blockState = world.getBlockState(blockPos);
                    Block block = blockState.getBlock();
                    Arm arm = user.getActiveHand() == Hand.MAIN_HAND ? playerEntity.getMainArm() : playerEntity.getMainArm().getOpposite();
                    if (blockState.hasBlockBreakParticles() && blockState.getRenderType() != BlockRenderType.INVISIBLE) {
                        this.addDustParticles(world, blockHitResult, blockState, user.getRotationVec(0.0F), arm);
                    }
                    
                    SoundEvent soundEvent;
                    if (blockState.getBlock().getDefaultState().isIn(ModTags.Blocks.IS_CHISELABLE)) {
                        soundEvent = SoundEvents.ITEM_AXE_SCRAPE;
                    } else {
                        soundEvent = SoundEvents.BLOCK_STONE_HIT;
                    }
                    world.playSound(playerEntity, blockPos, soundEvent, SoundCategory.BLOCKS);
                    if (CHISEL_MAP.containsKey(block) && block.getDefaultState().isIn(ModTags.Blocks.IS_CHISELABLE)) {

                        BlockEntity blockEntity = world.getBlockEntity(blockPos);
                        if (blockEntity instanceof BlockChiselProgressMixinInterface blockWithChiselProgress) {
                            blockWithChiselProgress.increaseChiselProgress(getChiselProgressPerTick(block));

                            boolean bl2 = chiselingIsComplete(blockWithChiselProgress);
                            if (bl2) {
                                world.setBlockState(blockPos, CHISEL_MAP.get(block).getDefaultState());
                                blockWithChiselProgress.setChiselProgress(0);
                                EquipmentSlot equipmentSlot = stack.equals(playerEntity.getEquippedStack(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                                stack.damage(1, user, equipmentSlot);
                            }
                        }
                    }
                }
            } else {
                user.stopUsingItem();
            }
        } else {
            user.stopUsingItem();
        }
    }

    private HitResult getHitResult(PlayerEntity user) {
        return ProjectileUtil.getCollision(user, entity -> !entity.isSpectator() && entity.canHit(), user.getBlockInteractionRange());
    }

    private void addDustParticles(World world, BlockHitResult hitResult, BlockState state, Vec3d userRotation, Arm arm) {
        double d = 3.0;
        int i = arm == Arm.RIGHT ? 1 : -1;
        int j = world.getRandom().nextBetweenExclusive(7, 12);
        BlockStateParticleEffect blockStateParticleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
        Direction direction = hitResult.getSide();
        DustParticlesOffset dustParticlesOffset = DustParticlesOffset.fromSide(userRotation, direction);
        Vec3d vec3d = hitResult.getPos();

        for (int k = 0; k < j; k++) {
            world.addParticle(
                    blockStateParticleEffect,
                    vec3d.x - (direction == Direction.WEST ? 1.0E-6F : 0.0F),
                    vec3d.y,
                    vec3d.z - (direction == Direction.NORTH ? 1.0E-6F : 0.0F),
                    dustParticlesOffset.xd() * i * 3.0 * world.getRandom().nextDouble(),
                    0.0,
                    dustParticlesOffset.zd() * i * 3.0 * world.getRandom().nextDouble()
            );
        }
    }

    record DustParticlesOffset(double xd, double yd, double zd) {
        private static final double field_42685 = 1.0;
        private static final double field_42686 = 0.1;

        public static DustParticlesOffset fromSide(Vec3d userRotation, Direction side) {
            double d = 0.0;

            return switch (side) {
                case DOWN, UP -> new DustParticlesOffset(userRotation.getZ(), 0.0, -userRotation.getX());
                case NORTH -> new DustParticlesOffset(1.0, 0.0, -0.1);
                case SOUTH -> new DustParticlesOffset(-1.0, 0.0, 0.1);
                case WEST -> new DustParticlesOffset(-0.1, 0.0, -1.0);
                case EAST -> new DustParticlesOffset(0.1, 0.0, 1.0);
            };
        }
    }
}

