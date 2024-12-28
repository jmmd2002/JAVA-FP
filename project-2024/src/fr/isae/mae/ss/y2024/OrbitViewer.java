package fr.isae.mae.ss.y2024;

public class OrbitViewer {

	public static void main(String[] args) {
		OrbitGatherer orbitsData = new OrbitGatherer("objectdata.txt");
		
		int n = 0;
		for(String name : orbitsData.objectName) {
			System.out.println(orbitsData.objectTime.get(n));
			n = n +1;
		}
	}
}
