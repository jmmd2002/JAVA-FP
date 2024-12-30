package fr.isae.mae.ss.y2024;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.awt.Color;

import java.util.*;

import fr.cnes.sirius.patrius.bodies.GeodeticPoint;
import fr.cnes.sirius.patrius.frames.FactoryManagedFrame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.orbits.KeplerianOrbit;
import fr.cnes.sirius.patrius.orbits.Orbit;
import fr.cnes.sirius.patrius.orbits.OrbitType;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.propagation.SpacecraftState;
import fr.cnes.sirius.patrius.propagation.numerical.NumericalPropagator;
import fr.cnes.sirius.patrius.propagation.sampling.PatriusFixedStepHandler;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import fr.cnes.sirius.patrius.utils.exception.PropagationException;
import fr.cnes.sirius.patrius.math.ode.*;
import fr.cnes.sirius.patrius.math.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import fr.cnes.sirius.patrius.bodies.*;

public class OrbitViewer extends ApplicationTemplate{

	public static void main(String[] args) {
		
		// Start application
		WorldWind.setOfflineMode(true); //avoid errors
		ApplicationTemplate.start("teste", AppFrame.class); //start WorldWind
	}
	
	/**
	 * Frame of the application. Any characteristics of the application frame and control commands are 
	 * within this class
	 */
	public static class AppFrame extends ApplicationTemplate.AppFrame {
		
		public AppFrame() throws PatriusException {
			super(false,false,false); //toggle some visual controls (status bar, layer panel, status panel)
			
			// Get space objects' information
			ObjectGatherer orbitsData = new ObjectGatherer("objectdata.txt");
			
			// Create orbits
			RenderableLayer orbitsLayer = new RenderableLayer(); //layer for all orbits
			for (int k = 0; k < orbitsData.allObjects.size(); k++) {
				
				//get parameters necessary to create the orbit for object k
				double[] par = orbitsData.allObjects.get(k).getEssentialParameters(); //i, ra, e, argper, theta, n
				double a = orbitsData.allObjects.get(k).getSemiMajorAxis(); 
				AbsoluteDate date = orbitsData.allObjects.get(k).getDate();
				
				//create orbit for object of index k
				Orbit objectOrbit = new KeplerianOrbit(a, par[2], par[0], par[3], par[1], par[4],
													   PositionAngle.MEAN,FramesFactory.getGCRF(),
													   date,Constants.WGS84_EARTH_MU); 
				System.out.println(date);
			}
		}
		
	}
}
