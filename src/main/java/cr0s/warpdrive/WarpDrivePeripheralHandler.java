package cr0s.warpdrive;

import cpw.mods.fml.common.Optional;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

@Optional.InterfaceList({
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft")
})
public class WarpDrivePeripheralHandler implements IPeripheralProvider {
	public void register() {
		ComputerCraftAPI.registerPeripheralProvider(this);
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof IPeripheral && ((IPeripheral) tileEntity).getType() != null) {
			return (IPeripheral) tileEntity;
		}
		return null;
	}
}