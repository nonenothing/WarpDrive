package cr0s.warpdrive.data;

import cr0s.warpdrive.WarpDrive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class JumpgatesRegistry {
	
	private File file;
	private ArrayList<Jumpgate> gates = new ArrayList<>();
	
	public JumpgatesRegistry() {
		file = new File("gates.txt");
		
		loadGates();
	}
	
	public void saveGates() {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(file));
	        
			// Write each string in the array on a separate line
			for (Jumpgate jg : gates) {
				out.println(jg);
			}
	        
			out.close();
		} catch (IOException exception) {
			exception.printStackTrace();
			WarpDrive.logger.error("Exception while saving jumpgates to disk");
		}
	}
	
	public void loadGates() {
		WarpDrive.logger.info("Loading jump gates from gates.txt...");
		try {
			if (file != null && !file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
			
			BufferedReader bufferedreader;
			bufferedreader = new BufferedReader(new FileReader(file));
			String s1;
	        
			while ((s1 = bufferedreader.readLine()) != null) {
				gates.add(new Jumpgate(s1));
			}
	        
			bufferedreader.close();
			WarpDrive.logger.info("Loaded " + gates.size() + " jump gates.");
		} catch (IOException exception) {
			exception.printStackTrace();
			WarpDrive.logger.error("Exception while loading jumpgates from disk");
		}
	}
	
	public void addGate(final Jumpgate jg) {
		gates.add(jg);
	}
	
	public boolean addGate(final String name, final int x, final int y, final int z) {
		// Gate already exists
		if (findGateByName(name) != null) {
			return false;
		}
        
		addGate(new Jumpgate(name, x, y, z));
        
		saveGates();
		
		return true;
	}
	
	public void removeGate(final String name) {
		Jumpgate jg;
        
		for (int i = 0; i < gates.size(); i++) {
			jg = gates.get(i);
            
			if (jg.name.equalsIgnoreCase(name))
			{
				gates.remove(i);
				return;
			}
		}
        
		saveGates();
	}
	
	public Jumpgate findGateByName(String name) {
		for (Jumpgate jg : gates) {
			if (jg.name.equalsIgnoreCase(name)) {
				return jg;
			}
		}
        
		return null;
	}
	
	public String JumpgatesList() {
		String result = "";
        
		for (Jumpgate jg : gates) {
			result += jg.toNiceString() + "\n";
		}
        
		return result;
   }
	
	public String commaList() {
		if (gates.isEmpty()) {
			return "<none> (check /generate to create one)";
		}
		
		final StringBuilder result = new StringBuilder();
		boolean isFirst = true;
		for (Jumpgate jg : gates) {
			if (isFirst) {
				isFirst = false;
			} else {
                result.append(", ");
            }
			result.append(jg.toNiceString());
		}
		return result.toString();
	}
	
	public Jumpgate findNearestGate(int x, int y, int z) {
		double minDistance2 = -1;
		Jumpgate res = null;
		
		for (Jumpgate jg : gates) {
			double dX = jg.xCoord - x;
			double dY = jg.yCoord - y;
			double dZ = jg.zCoord - z;
			double distance2 = dX * dX + dY * dY + dZ * dZ;
            
			if ((minDistance2 == -1) || (distance2 < minDistance2)) {
				minDistance2 = distance2;
				res = jg;
			}
		}
		
		return res;
	}
}
