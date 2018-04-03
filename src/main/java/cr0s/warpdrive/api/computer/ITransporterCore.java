package cr0s.warpdrive.api.computer;

import cr0s.warpdrive.api.IBeamFrequency;

public interface ITransporterCore extends IEnergy, IBeamFrequency {
	
	Object[] transporterName(final Object[] arguments);
	
	Object[] enable(final Object[] arguments);
	
	Object[] state();
	
	Object[] remoteLocation(final Object[] arguments);
	
	Object[] lock(final Object[] arguments);
	
	Object[] energyFactor(final Object[] arguments);
	
	Object[] getLockStrength();
	
	Object[] getEnergyRequired();
	
	Object[] energize(final Object[] arguments);
}
