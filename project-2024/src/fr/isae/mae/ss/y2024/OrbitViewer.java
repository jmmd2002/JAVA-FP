package fr.isae.mae.ss.y2024;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import java.awt.Color;
import java.util.*;

import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import fr.isae.mae.ss.y2024.ObjectGatherer.SpaceObject;

/**
 * Main function of the program
 */
public class OrbitViewer extends ApplicationTemplate {
	
	public static void main(String[] args) {
		
		// Start application
		WorldWind.setOfflineMode(true); //avoid errors
		ApplicationTemplate.start("teste", AppFrame.class); //start WorldWind
	}
		
public static class AppFrame extends ApplicationTemplate.AppFrame {
		
		private static final long serialVersionUID = 1L;
		
		// Lists for filters
		private List<SpaceObject> allObjects = new ArrayList<>();
	    private final List<SpaceObject> satellites = new ArrayList<>();
	    private final List<SpaceObject> debris = new ArrayList<>();
	    private final List<SpaceObject> rocketBodies = new ArrayList<>();
	    private final List<SpaceObject> oneweb = new ArrayList<>();
	    private final List<SpaceObject> beidou = new ArrayList<>();
	    private final List<SpaceObject> starlink = new ArrayList<>();
	    private final List<SpaceObject> iridium = new ArrayList<>();
	    //TODO add gps; add TBD
	    
	    final private LayerList layers = getWwd().getModel().getLayers(); //layer list to add markers
	    private Layer currentLayer; //current layer being displayed

		/**
		 * Initialise application.
		 */
		public AppFrame() throws PatriusException {
			super(false,false,false); //toggle some visual controls (status bar, layer panel, status panel)
			getWwd().setView(new FullOrbitView()); //make objects appear all around Earth
			
			// Get space objects' information
			final ObjectGatherer orbitsData = new ObjectGatherer("3le.txt");
			allObjects = orbitsData.allObjects; //all space objects
			sortObjects(allObjects); //sort the elements into the filter layers
			
			// Add the combo box
	        addComboBox();
			
			// Marker to indicate current position
			final Marker mouseDot = spawnMouse(); 
			
			// Create orbits
			
			// Set up a timer to continuously print the mouse position
	        Timer positionTimer = new Timer(100, new ActionListener() { // Update every 100 ms
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                // Get the current position under the mouse
	                Position position = getWwd().getCurrentPosition();
	                if (position != null) {
	                    double latitude = position.getLatitude().degrees;
	                    double longitude = position.getLongitude().degrees;
	                    double altitude = position.getElevation(); // Altitude in meters
	                    System.out.printf("Mouse Position: Latitude = %.6f, Longitude = %.6f, Altitude = %.2f meters%n",
	                            latitude, longitude, altitude);
	                    
	                 // Update the green dot position
	                    mouseDot.setPosition(position);
	                    getWwd().redraw(); // Redraw the WorldWindow to reflect changes
	                }
	            }
	        });
	        
	        positionTimer.start(); // Start the timer
		}
		
		//TODO finish comments
		/**
		 * 
		 * @param spaceObjects
		 * @since 10/01/2025
		 * @author pedor
		 */
		private void sortObjects(List<SpaceObject> spaceObjects) {
		    for (SpaceObject obj : spaceObjects) {
		        switch (obj.getType()) {
		            case "SATELLITE":
		            	obj.setColor(Color.ORANGE); 
		                satellites.add(obj);
		                break;
		            case "DEBRIS":
		            	obj.setColor(Color.RED);
		                debris.add(obj);
		                break;
		            case "ROCKET_BODY":
		            	obj.setColor(Color.GRAY);
		                rocketBodies.add(obj);
		                break;
		            case "ONEWEB":
		            	obj.setColor(Color.YELLOW);
		            	satellites.add(obj);
		                oneweb.add(obj);
		                break;
		            case "IRIDIUM":
		            	obj.setColor(Color.WHITE);
		            	satellites.add(obj);
		                iridium.add(obj);
		                break;
		            case "BEIDOU":
		            	obj.setColor(Color.GREEN);
		            	satellites.add(obj);
		                beidou.add(obj);
		                break;
		            case "STARLINK":
		            	obj.setColor(Color.PINK);
		            	satellites.add(obj);
		                starlink.add(obj);
		                break;
		        }
		    }
		}
		
		//TODO finish comments
		/**
	     * Adds a combo box to the application frame.
	     */
	    private void addComboBox() {
	        // Create a panel to hold the combo box
	        final JPanel comboBoxPanel = new JPanel();
	        comboBoxPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Align it to the left

	        // Create a combo box with options
	        String[] options = {"Satellites", "Debris", "Rocket Bodies", "OneWeb", "Beidou", "Iridium", "Starlink"};
	        final JComboBox<String> comboBox = new JComboBox<>(options);
	        
	        //TODO add option to display all objects
	        comboBox.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String selectedOption = (String) comboBox.getSelectedItem();

	                switch (selectedOption) {
	                    case "Satellites":
	                    	 if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                         currentLayer = displayObjects(satellites);
	                        break;
	                    case "Debris":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(debris);
	                        break;
	                    case "Rocket Bodies":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(rocketBodies);
	                        break;
	                    case "OneWeb":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(oneweb);
	                        break;
	                    case "Iridium":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(iridium);
	                        break;
	                    case "Beidou":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(beidou);
	                        break;
	                    case "Starlink":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(starlink);
	                        break;
	                    case "All":
	                    displayObjects(allObjects);
	                        break;
	                }
	            }
	        });

	        
	        // Add the combo box to the panel
	        comboBoxPanel.add(new JLabel("Select an option:")); // Add a label
	        comboBoxPanel.add(comboBox);

	        // Add the panel to the frame
	        this.getContentPane().add(comboBoxPanel, BorderLayout.NORTH);
	    }// Place it at the top of the frame
		
		private Marker spawnMouse() {
			
			// Create a layer for the green dot
	        final MarkerLayer dotLayer = new MarkerLayer();
	        getWwd().getModel().getLayers().add(dotLayer);
	        
			//Set marker's attributes
			MarkerAttributes attrsDot = new BasicMarkerAttributes();
			attrsDot.setMaterial(new Material(Color.GREEN)); //colour
			attrsDot.setMarkerPixels(2d); //size
			
	        // Create a PointPlacemark to represent the green dot
	        Marker mouseDot = new BasicMarker(Position.fromDegrees(0, 0, 0), attrsDot);
	        List<Marker>mouseDotList = new ArrayList<>();
	        mouseDotList.add(mouseDot);
	        dotLayer.setMarkers(mouseDotList);
	        layers.add(dotLayer);
	        
	        return mouseDot;
		}
		
		/**
		 * Function to display desired space objects on world wind as markers.
		 * @param spaceObjects Space Objects to be drawn on WorldWind as markers.
		 * @return Layer marker layer added
		 * @since 10/01/2025
		 * @author joaom
		 */
		private Layer displayObjects(List<SpaceObject> spaceObjects) {
			
			System.out.println("Displaying " +  spaceObjects.size() + " objects.");
			final List<Marker> markers = new ArrayList<>(spaceObjects.size()); //list to store the markers
			
			//Create a marker for each object
			for (int k = 0; k < spaceObjects.size(); k++) {
				
				//Set marker's attributes
				MarkerAttributes attrs = new BasicMarkerAttributes();
				attrs.setMaterial(new Material(spaceObjects.get(k).getColor())); //colour
				attrs.setMarkerPixels(2d); //size
				
				//Create marker
				markers.add(new BasicMarker(Position.fromRadians(spaceObjects.get(k).getCurrentLat(), spaceObjects.get(k).getCurrentLon(), 
						                                         spaceObjects.get(k).getCurrentAlt()), attrs)); //create marker at starting position for object k
			}
			
			final MarkerLayer markerLayer = new MarkerLayer(); //marker layer
			markerLayer.setMarkers(markers); //add markers to layer
			layers.add(markerLayer); //add layer to worldwind
			
			//Temporary - code to display orbits
			//TODO Create button to toggle the orbits - similar process to combobox - do it outside of this function obviously
			final RenderableLayer orbitsLayer = new RenderableLayer(); //layer for all orbits
			orbitsLayer.setName("All orbits");
			for(int k = 0; k < spaceObjects.size(); k++) {
				
				allObjects.get(k).getPath().setVisible(false);
				orbitsLayer.addRenderable(spaceObjects.get(k).getPath()); //render orbits in respective layer
			}
			layers.add(orbitsLayer); //add layer to worldwind
			
			return markerLayer;
		}
	}
}
