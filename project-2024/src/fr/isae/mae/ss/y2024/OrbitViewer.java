package fr.isae.mae.ss.y2024;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import java.awt.Color;
import java.util.*;

import fr.cnes.sirius.patrius.utils.exception.PatriusException;



public class OrbitViewer extends ApplicationTemplate {
	
	public static void main(String[] args) {
		
		// Start application
		WorldWind.setOfflineMode(true); //avoid errors
		ApplicationTemplate.start("teste", AppFrame.class); //start WorldWind
	}
		
public static class AppFrame extends ApplicationTemplate.AppFrame {
		
		private static final long serialVersionUID = 1L;

		/**
		 * Initialise application.
		 */
		public AppFrame() throws PatriusException {
			super(false,false,false); //toggle some visual controls (status bar, layer panel, status panel)
			getWwd().setView(new FullOrbitView());
			
			// Add the combo box
	        addComboBox();
			
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
			insertBeforeCompass(getWwd(), orbitsLayer); //Add layer to model
			
			
			
			//Mark position of space objects - TO DO COMPLETE
			displayObjects(allObjects);
		}
		
		/**
	     * Adds a combo box to the application frame.
	     */
	    private void addComboBox() {
	        // Create a panel to hold the combo box
	        JPanel comboBoxPanel = new JPanel();
	        comboBoxPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Align it to the left

	        // Create a combo box with options
	        String[] options = {"Option 1", "Option 2", "Option 3"};
	        JComboBox<String> comboBox = new JComboBox<>(options);

	        // Add an action listener to handle option selection
	        comboBox.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String selectedOption = (String) comboBox.getSelectedItem();
	                JOptionPane.showMessageDialog(AppFrame.this, "You selected: " + selectedOption);
	            }
	        });

	        // Add the combo box to the panel
	        comboBoxPanel.add(new JLabel("Select an option:")); // Add a label
	        comboBoxPanel.add(comboBox);

	        // Add the panel to the frame
	        this.getContentPane().add(comboBoxPanel, BorderLayout.NORTH);
	    }// Place it at the top of the frame
		
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
			LayerList layers = getWwd().getModel().getLayers(); //layer list to add markers
			layers.add(markerLayer);
		}
	}
}
