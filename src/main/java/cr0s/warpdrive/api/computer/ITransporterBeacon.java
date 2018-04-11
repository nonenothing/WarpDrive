package cr0s.warpdrive.api.computer;

public interface ITransporterBeacon extends IEnergy {
	
	Boolean[] enable(final Object[] arguments);
	
	Boolean[] isActive(final Object[] arguments);
	
	boolean isActive();
	
	void energizeDone();
}
