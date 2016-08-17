package cr0s.warpdrive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class SirenSound extends MovingSound {
    ResourceLocation resource;
    float range;
    float x, y, z;

    /**x, y and z are the position of the tile entity. the actual sound is broadcast from
       xPosF, yPosF, zPosF, which is the location of the player.
       The volume is adjusted according to the distance to x, y, z.
       Why? Because Minecraft's sound system is complete and utter shit, and this
       is the easiest way which:
       1. Produces a sound audible from a specifiable range.
       2. Produces a sound which decreases in volume the farther you get away from it.
       3. Doesn't keep playing for you once you're half the world away.
       4. Doesn't completely spazz out the instant you try to actually use it.*/
    public SirenSound(ResourceLocation resource, float range, float x, float y, float z) {
        super(resource);

        this.resource = resource;
        this.range = range;

        this.x = x;
        this.y = y;
        this.z = z;

        this.xPosF = x;
        this.yPosF = y;
        this.zPosF = z;
    }

    public void update() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        this.xPosF = (float) player.posX;
        this.yPosF = (float) player.posY;
        this.zPosF = (float) player.posZ;

        if (player.getDistance(x, y, z) > range) {
            this.volume = 0.0F;
        } else {
            //TODO: Better distance/volume formula that has a better drop off rate.
            this.volume = 1.0F - scaleTo((float) player.getDistance(x, y, z), 0.0F, range, 0.0F, 1.0F);
        }
    }

    private float scaleTo(float num, float oldMin, float oldMax, float newMin, float newMax) {
        return ((newMax - newMin)*(num - oldMin)) / (oldMax - oldMin) + newMin;
    }
}
