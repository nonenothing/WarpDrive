package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.WarpDrive;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSiren extends Block implements ITileEntityProvider {
    private boolean isRaidSiren;
    private float range;

    public BlockSiren(String name, boolean isRaidSiren, float range) {
        super(Material.iron);

        this.setBlockName(name);
        this.isRaidSiren = isRaidSiren;
        this.range = range;

        this.setCreativeTab(WarpDrive.creativeTabWarpDrive);
        this.setBlockTextureName("warpdrive:detection/" + name);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntitySiren(this.unlocalizedName, isRaidSiren, range);
    }

    //Silences the siren if the block is destroyed.
    //If this fails, the siren will still be stopped when it's invalidated.
    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
        if (!world.isRemote) {
            super.onBlockPreDestroy(world, x, y, z, meta);
            return;
        }

        TileEntity te = world.getTileEntity(x, y, z);

        if(te != null && te instanceof TileEntitySiren) {
            TileEntitySiren siren = (TileEntitySiren) te;

            if (siren.isPlaying()) {
                siren.stopSound();
            }
        }
    }
}
