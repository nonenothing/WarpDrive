package cr0s.warpdrive.api;

public interface IBeamFrequency {
	// get beam frequency, return -1 if invalid 
	public abstract int getBeamFrequency();
	
	// sets beam frequency
	public abstract void setBeamFrequency(int beamFrequency);
}
