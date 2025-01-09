package fr.isae.mae.ss.y2024;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import fr.cnes.sirius.patrius.utils.exception.PatriusException;



public class OrbitViewer extends ApplicationTemplate {
	
	final WorldWindowGLCanvas worldWindCanvas = new WorldWindowGLCanvas(); //create custom canvas
	
	public static void main(String[] args) {
		
		// Start application
		WorldWind.setOfflineMode(true); //avoid errors
		OrbitViewer orbitViewer = new OrbitViewer();
		try {
			orbitViewer.startViewer();
		} catch (PatriusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	private void startViewer() throws PatriusException {
		
		worldWindCanvas.setView(new FullOrbitView()); //set view to 360 degrees
		
		// Run GUI setup on the Event Dispatch Thread
	    SwingUtilities.invokeLater(() -> {
	        // Create the JFrame to hold the WorldWind canvas
	        JFrame frame = new JFrame("NASA WorldWind Globe");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	        // Set layout to BorderLayout for proper positioning
	        frame.setLayout(new BorderLayout());

	        // Add the WorldWind canvas to the CENTER position
	        frame.add(worldWindCanvas, BorderLayout.CENTER);

	        // Set up the WorldWind model
	        BasicModel model = new BasicModel();
	        worldWindCanvas.setModel(model);	
	        
	        // Force canvas to occupy the remaining space
	        worldWindCanvas.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
	        //frame.setSize(1100, 630); // Adjust width for the control panel
	        //frame.setMinimumSize(new Dimension(1100, 630));
	        
	        // Set the view for the canvas
	        worldWindCanvas.setView(new FullOrbitView());
	        frame.pack();
	        frame.setVisible(true);
		});
	    
	    // Get space objects' information
	    ObjectGatherer orbitsData = new ObjectGatherer("teste.txt");
	 	List<ObjectGatherer.SpaceObject> allObjects = orbitsData.allObjects; //all space objects		
	 			
	 	//TO DO - ADD filtered lists
	 			
	 			
	 	// Create orbits
	 			
	 	RenderableLayer orbitsLayer = new RenderableLayer(); //layer for all orbits
	 	orbitsLayer.setName("All orbits");
	 			
	 			
	 	for(int k = 0; k < allObjects.size(); k++) {
	 		allObjects.get(k).getPath().setVisible(true);
	 		orbitsLayer.addRenderable(orbitsData.allObjects.get(k).getPath()); //render orbits in respective layer
	 	}
	 	worldWindCanvas.getModel().getLayers().add(orbitsLayer);
	 			
	 	//Mark position of space objects - TO DO COMPLETE
	 	displayObjects(allObjects);
	}
	
	private void displayObjects(List<ObjectGatherer.SpaceObject> spaceObjects) {
		
		List<Marker> markers = new ArrayList<>(spaceObjects.size()); //list to store the markers
		
		//Create a marker for each object
		for (int k = 0; k < spaceObjects.size(); k++) {
			
			//Set marker's attributes
			MarkerAttributes attrs = new BasicMarkerAttributes();
			attrs.setMaterial(new Material(Color.RED)); //colour
			attrs.setMarkerPixels(2d); //size
			
			//Create marker
			markers.add(new BasicMarker(Position.fromRadians(spaceObjects.get(k).getCurrentLat(), spaceObjects.get(k).getCurrentLon(), 
					                                         spaceObjects.get(k).getCurrentAlt()), attrs)); //create marker at starting position for object k
		}
		
		MarkerLayer markerLayer = new MarkerLayer(); //marker layer
		markerLayer.setMarkers(markers); //add markers to layer
		LayerList layers = worldWindCanvas.getModel().getLayers(); //layer list to add markers
		layers.add(markerLayer);
	}
}
