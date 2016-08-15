package cr0s.warpdrive.block.detection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.SirenSound;
import cr0s.warpdrive.WarpDrive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class TileEntitySiren extends TileEntity {
    private static final int COOLDOWN_LENGTH_TICKS = 5;

    public enum SirenState {
        STOPPED, STARTING, STARTED, STOPPING, COOLDOWN;
    }

    private SirenState state = SirenState.STOPPED;
    private boolean isRaidSiren;
    private String name;
    private Object sound;
    private float range;
    private int cooldown = 0;

    /* Because the SirenSound exists only on the client, but the Tile Entity itself
     *  also exists on the server, we cannot declare a SirenSound object directly, thus
     *  the sound object is declared as type Object. */

    public TileEntitySiren(String name, boolean isRaidSiren, float range) {
        super();

        this.name = name;
        this.range = range;
        this.isRaidSiren = isRaidSiren;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!this.hasWorldObj() || !worldObj.isRemote) return;
        if (this.sound == null) this.setSound();

        //Siren sound logic.
        switch (this.state) {
            case STOPPED:
                if (this.isPowered() && !this.isPlaying()) {
                    this.startSound();
                } else if (!this.isPowered() && this.isPlaying()) {
                    //Better safe than sorry.
                    this.stopSound();
                }

                break;
            case STARTING:
                if (this.isPlaying()) {
                    this.state = SirenState.STARTED;
                }

                break;
            case STARTED:
                if (this.isPowered()) {
                    if (!this.isPlaying()) this.startSound();
                } else {
                    this.state = SirenState.STOPPING;
                }

                break;
            case STOPPING:
                this.stopSound();

                this.cooldown = COOLDOWN_LENGTH_TICKS;
                this.state = SirenState.COOLDOWN;

                break;
            case COOLDOWN:
                if (cooldown > 0) {
                    cooldown--;
                } else {
                    this.cooldown = 0;
                    this.state = SirenState.STOPPED;
                }

                break;
            default:
                this.stopSound();
                this.state = SirenState.STOPPED;
                break;
        }
    }

    //Stops the siren when the chunk is unloaded.
    @Override
    public void onChunkUnload() {
        if (worldObj.isRemote && this.isPlaying()) this.stopSound();
        super.onChunkUnload();
    }

    //Stops the siren when the TileEntity object is destroyed.
    //The siren should already be stopped by this point, but better safe than sorry.
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
    public void startSound() {
        try {
            Minecraft.getMinecraft().getSoundHandler().playSound((ISound) sound);
            this.state = SirenState.STARTING;
        } catch (IllegalArgumentException e) {
			/* If soundHandler.playSound() is called before the previous same ISound stops
			 * from a call to soundHandler.stopSound(), an IllegalArgumentException will
			 * be thrown. This happens when you spam the redstone trigger.
			 * We're trying to recover by forcing a stop. */

            this.state = SirenState.STOPPING;
        }
    }

    //Forces the siren to stop playing its sound.
    @SideOnly(Side.CLIENT)
    public void stopSound() {
        Minecraft.getMinecraft().getSoundHandler().stopSound((ISound) sound);
    }

    //Checks if the siren is currently playing its sound.
    @SideOnly(Side.CLIENT)
    public boolean isPlaying() {
        return Minecraft.getMinecraft().getSoundHandler().isSoundPlaying((ISound) sound);
    }

    //Checks if the siren is being powered by redstone.
    public boolean isPowered() {
        return worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
    }
}
