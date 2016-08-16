package cr0s.warpdrive.block.detection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.SirenSound;
import cr0s.warpdrive.WarpDrive;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class TileEntitySiren extends TileEntity {
    public enum SirenState {
        STARTING, STARTED, STOPPING, STOPPED;
    }

    private SirenState state = SirenState.STOPPED;
    private boolean isRaidSiren;
    private String name;
    private float range;
    private int timeToLastUpdate = 0;

    @SideOnly(Side.CLIENT)
    private SirenSound sound;

    public TileEntitySiren(String name, boolean isRaidSiren, float range) {
        super();

        this.name = name;
        this.range = range;
        this.isRaidSiren = isRaidSiren;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        /*Updating the sound to quickly breaks Minecraft's sounds handler.
        * Therefor, we only update our sound once every 0.5 seconds.
        * It's less responsive like this, but doesn't completely freak out when
        * spamming the redstone on and off.*/

        if (this.timeToLastUpdate <= 0) {
            this.timeToLastUpdate = 10;
        } else {
            this.timeToLastUpdate--;
            return;
        }

        if (!this.hasWorldObj() || !worldObj.isRemote) return;
        if (this.sound == null) this.setSound();

        //Siren sound logic.
        switch (this.state) {
            case STOPPED:
                if (this.isPlaying()) {
                    this.state = SirenState.STOPPING;
                }

                if (this.isPowered()) {
                    this.state = SirenState.STARTING;
                }

                break;
            case STARTING:
                if (this.startSound()) {
                    this.state = SirenState.STARTED;
                } else {
                    this.state = SirenState.STOPPING;
                }

                break;
            case STARTED:
                if (!this.isPowered()) {
                    this.state = SirenState.STOPPING;
                } else if (!this.isPlaying()) {
                    this.state = SirenState.STARTING;
                }

                break;
            case STOPPING:
                if (this.isPlaying()) {
                    this.stopSound();
                } else {
                    this.state = SirenState.STOPPED;
                }

                break;
            default:
                if (this.isPlaying()) {
                    this.state = SirenState.STOPPING;
                } else {
                    this.state = SirenState.STOPPED;
                }

                break;
        }
    }

    //Stops the siren when the chunk is unloaded.
    @Override
    public void onChunkUnload() {
        if (worldObj.isRemote && this.isPlaying()) this.stopSound();
        super.onChunkUnload();
    }

    //Stops the siren when the TileEntity object is invalidated.
    @Override
    public void invalidate() {
        if (worldObj.isRemote && this.isPlaying()) this.stopSound();
        super.invalidate();
    }

    //Create a new SirenSound object that the siren will use.
    @SideOnly(Side.CLIENT)
    private void setSound() {
        String resource = WarpDrive.MODID + ":siren_" + (isRaidSiren ? "raid" : "industrial");
        this.sound = new SirenSound(new ResourceLocation(resource), this.range, this.xCoord, this.yCoord, this.zCoord);
    }

    //Forces the siren to start playing its sound;
    @SideOnly(Side.CLIENT)
    public boolean startSound() {
        if (!isPlaying()) {
            try {
                Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else {
            return true;
        }
    }

    //Forces the siren to stop playing its sound.
    @SideOnly(Side.CLIENT)
    public void stopSound() {
        Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
    }

    //Checks if the siren is currently playing its sound.
    @SideOnly(Side.CLIENT)
    public boolean isPlaying() {
        return Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound);
    }

    //Checks if the siren is being powered by redstone.
    public boolean isPowered() {
        return worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
    }
}
