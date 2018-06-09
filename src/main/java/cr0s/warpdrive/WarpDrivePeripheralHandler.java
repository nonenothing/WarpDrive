package cr0s.warpdrive;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Optional.InterfaceList({
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "computercraft")
})
public class WarpDrivePeripheralHandler implements IPeripheralProvider {
	public void register() {
		ComputerCraftAPI.registerPeripheralProvider(this);
	}
	
	@Override
	@Optional.Method(modid = "computercraft")
	public IPeripheral getPeripheral(@Nonnull final World world, @Nonnull final BlockPos blockPos, @Nonnull final EnumFacing side) {
		final TileEntity tileEntity = world.getTileEntity(new BlockPos(blockPos));
		if (tileEntity instanceof IPeripheral) {
			return (IPeripheral) tileEntity;
		}
		return null;
	}
}