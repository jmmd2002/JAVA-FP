package fr.isae.mae.ss.y2024;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import fr.isae.mae.ss.y2024.ObjectGatherer.SpaceObject;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

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
	    private List<SpaceObject> activeFilter;
	    //TODO add gps; add TBD
	    
	    final private LayerList layers = getWwd().getModel().getLayers(); //layer list to add markers
	    private Layer currentLayer; //current layer being displayed

		/**
		 * Initialise application.
		 */
		public AppFrame() throws PatriusException {
			super(false,false,false); //toggle some visual controls (status bar, layer panel, status panel)
			getWwd().setView(new FullOrbitView()); //make objects appear all around Earth
			
			//Get space objects' information
			final ObjectGatherer orbitsData = new ObjectGatherer("3le.txt");
			allObjects = orbitsData.allObjects; //all space objects
			sortObjects(allObjects); //sort the elements into the filter layers
			
			//Add the combo box
	        addComboBox();
	        
	        //Create and move mouse pointer
	        spawnMouse();
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
	                         activeFilter = satellites;
	                        break;
	                    case "Debris":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(debris);
	                    	activeFilter = debris;
	                        break;
	                    case "Rocket Bodies":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(rocketBodies);
	                    	activeFilter = rocketBodies;
	                        break;
	                    case "OneWeb":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(oneweb);
	                    	activeFilter = oneweb;
	                        break;
	                    case "Iridium":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(iridium);
	                    	activeFilter = iridium;
	                        break;
	                    case "Beidou":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(beidou);
	                    	activeFilter = beidou;
	                        break;
	                    case "Starlink":
	                    	if (currentLayer != null) {getWwd().getModel().getLayers().remove(currentLayer);}
	                    	currentLayer = displayObjects(starlink);
	                    	activeFilter = starlink;
	                        break;
	                    case "All":
	                    	displayObjects(allObjects);
	                    	activeFilter = allObjects;
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
		
	    /**
	     * Creates and moves mouse pointer. Space and shift keys increase and decrease the altitude, respectively,
	     * arrow keys increase or decrease longitude and latitude
	     * @since 10/01/2025
	     * @author joaom
	     */
		private void spawnMouse() {
		
	        //Create mouse dot at initial position
	        PointPlacemark dot = new PointPlacemark(Position.fromDegrees(0, 0, 0)); //create a dot at 0 0 0
	        PointPlacemarkAttributes attr = new PointPlacemarkAttributes();
	        attr.setUsePointAsDefaultImage(true);
	        attr.setScale((double) 5); //size of the dot
	        attr.setLineMaterial(new Material(Color.GREEN)); //colour of the dot
	        dot.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND); //make dot leave Earth when altitude increases
	        dot.setAttributes(attr);
	        
	        //Add mouse dot to WorldWind
	        RenderableLayer mouseLayer = new RenderableLayer();
	        layers.add(mouseLayer);
	        mouseLayer.addRenderable(dot);
	        
	        //Attach a KeyListener to the WorldWindow to move dot
	        getWwd().getInputHandler().addKeyListener(new KeyAdapter() {
	            @Override
	            public void keyPressed(KeyEvent e) {
	                double latitude = dot.getPosition().getLatitude().degrees;
	                double longitude = dot.getPosition().getLongitude().degrees;
	                double altitude = dot.getPosition().getAltitude();
	                
	                boolean changed = false; //control variable to check if changes were made

	                switch (e.getKeyCode()) {
	                    case KeyEvent.VK_SPACE: //space increases altitude
	                        altitude += 50000; //m
	                        e.consume(); //prevent default action
	                        changed = true; //flag changed
	                        break;
	                    case KeyEvent.VK_SHIFT: //shift decreases altitude
	                        altitude -= 50000; //m
	                        if (altitude < 0) altitude = 0; //prevent altitude going into negative values
	                        e.consume(); //prevent default action
	                        changed = true; //flag changed
	                        break;
	                    case KeyEvent.VK_UP: //up arrow increases latitude
	                        latitude += 0.5; //deg
	                        e.consume(); //prevent default action
	                        changed = true; //flag changed
	                        break;
	                    case KeyEvent.VK_DOWN: //down arrow decreases latitude
	                        latitude -= 0.5; //deg
	                        e.consume(); //prevent default action
	                        changed = true; //flag changed
	                        break;
	                    case KeyEvent.VK_RIGHT: //right arrow increases longitude
	                        longitude += 0.5; //deg
	                        e.consume(); //prevent default action
	                        changed = true; //flag changed
	                        break;
	                    case KeyEvent.VK_LEFT: //left arrow decreases longitude
	                        longitude -= 0.5; //deg
	                        e.consume(); //prevent default action
	                        changed = true; //flag changed
	                        break;
	                }

	                //Update the position of the dot and WorldWind if changes were detected
	                if (changed) {
	                	dot.setPosition(Position.fromDegrees(latitude, longitude, altitude));
	                	checkContact(dot, activeFilter);
		                getWwd().redraw();
	                }
	            }
	        });
		}
		
		private void checkContact(PointPlacemark dot, List<SpaceObject> spaceObjects) {
			
			//Dot position
			double dotLat = Math.toRadians(dot.getPosition().getLatitude().degrees); //latitude (rad)
		    double dotLon = Math.toRadians(dot.getPosition().getLongitude().degrees); //longitude (rad)
		    double dotAlt = dot.getPosition().getAltitude(); //altitude (m)
		    //Convert dot's position to Cartesian coordinates
		    double dotX = (Constants.WGS84_EARTH_EQUATORIAL_RADIUS + dotAlt) * Math.cos(dotLat) * Math.cos(dotLon); //x (m)
		    double dotY = (Constants.WGS84_EARTH_EQUATORIAL_RADIUS + dotAlt) * Math.cos(dotLat) * Math.sin(dotLon); //y (m)
		    double dotZ = (Constants.WGS84_EARTH_EQUATORIAL_RADIUS + dotAlt) * Math.sin(dotLat); //z (m)
			
			if(spaceObjects != null) {
				for(SpaceObject obj : spaceObjects) {
					
					//Convert marker's position to Cartesian coordinates
					double markerX = (Constants.WGS84_EARTH_EQUATORIAL_RADIUS + obj.getCurrentAlt()) * Math.cos(obj.getCurrentLat()) * Math.cos(obj.getCurrentLon());
					double markerY = (Constants.WGS84_EARTH_EQUATORIAL_RADIUS + obj.getCurrentAlt()) * Math.cos(obj.getCurrentLat()) * Math.sin(obj.getCurrentLon());
					double markerZ = (Constants.WGS84_EARTH_EQUATORIAL_RADIUS + obj.getCurrentAlt()) * Math.sin(obj.getCurrentLat());
					
				    //Compute the distance between dot and marker
			        double distance = Math.sqrt(
			            Math.pow(dotX - markerX, 2) +
			            Math.pow(dotY - markerY, 2) +
			            Math.pow(dotZ - markerZ, 2)
			        );
					
					if(distance <= 100000) {
						displayOrbit(obj);
					}
				}
			}
		}
		
		private void displayOrbit(SpaceObject obj) {
			
			final RenderableLayer orbitsLayer = new RenderableLayer(); //layer for all orbits
			obj.getPath().setVisible(true);	
			ShapeAttributes attrs = obj.getPath().getActiveAttributes();
			attrs.setOutlineMaterial(new Material(obj.getColor()));
			obj.getPath().setAttributes(attrs);
			obj.getPath().getActiveAttributes().setOutlineMaterial(new Material(obj.getColor())); //change colour to be the same as marker
			orbitsLayer.addRenderable(obj.getPath()); //render orbits in respective layer
			layers.add(orbitsLayer); //add layer to worldwind
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
			
			return markerLayer;
		}
	}
}
