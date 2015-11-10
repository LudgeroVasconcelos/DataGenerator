
/**
 * Represents a unit information of a victim
 * 
 */
public class VictimNode {
	String node;
	double lat;
	double lon; 
	long time;
	String message;
	int steps; 
	int screen;
	int battery;
	boolean safe;
	public VictimNode(String node, double lat, double lon, long time,
			String message, int steps, int screen, int batery,
			boolean safe) {
		this.node=node;
		this.lat=lat;
		this.lon=lon; 
		this.time=time;
		this.message=message;
		this.steps=steps; 
		this.screen=screen;
		this.battery=batery;
		this.safe=safe;
	}

}
