package cr0s.warpdrive.api;

public interface IBeamFrequency {
	int BEAM_FREQUENCY_SCANNING = 1420;
	int BEAM_FREQUENCY_MAX = 65000;
	
	// get beam frequency, return -1 if invalid 
	int getBeamFrequency();
	
	// sets beam frequency
	void setBeamFrequency(int beamFrequency);
}
