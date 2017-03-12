package cr0s.warpdrive.api;

public interface IVideoChannel {
	int VIDEO_CHANNEL_MIN = 0;
	int VIDEO_CHANNEL_MAX = 999999999;
	String VIDEO_CHANNEL_TAG = "videoChannel";
	
	// get video channel, return -1 if invalid 
	int getVideoChannel();
	
	// set video channel
	void setVideoChannel(final int videoChannel);
}
