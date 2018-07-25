package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.api.computer.IMultiBlockCoreOrController;
import cr0s.warpdrive.api.computer.IMultiBlockCore;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.Optional;

public abstract class TileEntityAbstractEnergyCoreOrController extends TileEntityAbstractEnergyConsumer implements IMultiBlockCoreOrController {
	
	// persistent properties
	// (none)
	
	// computed properties
	// (none)
	
	public TileEntityAbstractEnergyCoreOrController() {
		super();
		
		// (abstract) peripheralName = "xxx";
		addMethods(new String[] {
				"isAssemblyValid",
				});
	}
	
	public void onCoreUpdated(@Nonnull final IMultiBlockCore multiblockCore) {
		assert multiblockCore instanceof TileEntityAbstractEnergyCoreOrController;
		name = ((TileEntityAbstractEnergyCoreOrController) multiblockCore).name;
	}
	
	// Common OC/CC methods
	@Override
	abstract public Object[] isAssemblyValid();
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] isAssemblyValid(final Context context, final Arguments arguments) {
		return isAssemblyValid();
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "computercraft")
	public Object[] callMethod(@Nonnull final IComputerAccess computer, @Nonnull final ILuaContext context, final int method, @Nonnull final Object[] arguments) {
		final String methodName = CC_getMethodNameAndLogCall(method, arguments);
		
		switch (methodName) {
		case "isAssemblyValid":
			return isAssemblyValid();
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	@Override
	public String toString() {
		return String.format("%s \'%s\' %s %s",
		                     getClass().getSimpleName(),
		                     name,
		                     Commons.format(world, pos),
		                     connectedComputers == null ? "~NULL~" : connectedComputers );
	}
}
