package cr0s.warpdrive.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.IRenderHandler;

public class RenderBlank extends IRenderHandler {
    private static RenderBlank INSTANCE = null;
    
    public static RenderBlank getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RenderBlank();
        }
        return INSTANCE;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
    }
}
