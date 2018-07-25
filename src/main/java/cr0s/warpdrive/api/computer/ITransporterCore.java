package cr0s.warpdrive.api.computer;

import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.api.IStarMapRegistryTileEntity;

public interface ITransporterCore extends IEnergyConsumer, IBeamFrequency, IStarMapRegistryTileEntity {
	
	Object[] state();
	
	Object[] remoteLocation(final Object[] arguments);
	
	Object[] lock(final Object[] arguments);
	
	Object[] energyFactor(final Object[] arguments);
	
	Object[] getLockStrength();
	
	Object[] energize(final Object[] arguments);
}
