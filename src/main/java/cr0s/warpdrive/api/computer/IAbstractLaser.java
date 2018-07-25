package cr0s.warpdrive.api.computer;

public interface IAbstractLaser extends IMachine {
	
	Object[] energy();
	
	Object[] laserMediumDirection();
	
	Object[] laserMediumCount();
}
