package com.supermartijn642.itemcollectors;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.itemcollectors.screen.AdvancedCollectorContainer;
import com.supermartijn642.itemcollectors.screen.BasicCollectorScreen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created 7/15/2020 by SuperMartijn642
 */
public class CollectorBlock extends BaseBlock {

    public static final EnumProperty<Direction> DIRECTION = BlockStateProperties.FACING;
    private static final BlockShape SHAPE = BlockShape.or(
        BlockShape.createBlockShape(3, 0, 3, 13, 1, 13),
        BlockShape.createBlockShape(5, 1, 5, 11, 2, 11),
        BlockShape.createBlockShape(6, 2, 6, 10, 5, 10),
        BlockShape.createBlockShape(5, 5, 5, 11, 6, 11),
        BlockShape.createBlockShape(5, 6, 5, 6, 11, 6),
        BlockShape.createBlockShape(5, 6, 10, 6, 11, 11),
        BlockShape.createBlockShape(10, 6, 5, 11, 11, 6),
        BlockShape.createBlockShape(10, 6, 10, 11, 11, 11),
        BlockShape.createBlockShape(6, 10, 5, 11, 11, 6),
        BlockShape.createBlockShape(6, 10, 10, 10, 11, 11),
        BlockShape.createBlockShape(5, 10, 6, 6, 11, 10),
        BlockShape.createBlockShape(10, 10, 6, 11, 11, 10),
        BlockShape.createBlockShape(6, 6, 6, 10, 10, 10));
    private static final BlockShape[] SHAPES = new BlockShape[6];

    static{
        SHAPES[Direction.DOWN.getIndex()] = SHAPE;
        SHAPES[Direction.UP.getIndex()] = SHAPE.rotate(Direction.Axis.X).rotate(Direction.Axis.X);
        SHAPES[Direction.NORTH.getIndex()] = SHAPE.rotate(Direction.Axis.X).rotate(Direction.Axis.Y).rotate(Direction.Axis.Y);
        SHAPES[Direction.EAST.getIndex()] = SHAPE.rotate(Direction.Axis.X).rotate(Direction.Axis.Y).rotate(Direction.Axis.Y).rotate(Direction.Axis.Y);
        SHAPES[Direction.SOUTH.getIndex()] = SHAPE.rotate(Direction.Axis.X);
        SHAPES[Direction.WEST.getIndex()] = SHAPE.rotate(Direction.Axis.X).rotate(Direction.Axis.Y);
    }

    private final Supplier<CollectorTile> tileSupplier;
    private final Supplier<Integer> maxRange;
    private final Supplier<Boolean> hasFilter;

    public CollectorBlock(String registryName, Supplier<CollectorTile> tileSupplier, Supplier<Integer> maxRange, Supplier<Boolean> hasFilter){
        super(registryName, false, Properties.create(Material.ROCK, MaterialColor.BLACK).harvestTool(ToolType.PICKAXE).harvestLevel(1).hardnessAndResistance(5, 1200));
        this.tileSupplier = tileSupplier;
        this.maxRange = maxRange;
        this.hasFilter = hasFilter;
        this.setDefaultState(this.getDefaultState().with(DIRECTION, Direction.DOWN));
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_){
        if(worldIn.isRemote && !this.hasFilter.get())
            ClientUtils.displayScreen(new BasicCollectorScreen(pos));
        else if(!worldIn.isRemote && this.hasFilter.get())
            NetworkHooks.openGui((ServerPlayerEntity)player, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName(){
                    return new StringTextComponent("");
                }

                @Nullable
                @Override
                public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player){
                    return new AdvancedCollectorContainer(id, player, pos);
                }
            }, pos);
        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return this.tileSupplier.get();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
        return SHAPES[state.get(DIRECTION).getIndex()].getUnderlying();
    }

    @Override
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        tooltip.add(new TranslationTextComponent("itemcollectors." + (this.hasFilter.get() ? "advanced" : "basic") + "_collector.info", this.maxRange.get()).applyTextStyle(TextFormatting.AQUA));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(DIRECTION);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context){
        return this.getDefaultState().with(DIRECTION, context.getFace().getOpposite());
    }
}
