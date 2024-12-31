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
	 * Propagates the orbit of a space object, computing its coordinates along the orbit.
	 * @param iniOrbit Orbit of space object to be propagated
	 * @return List of coordinates along the orbit in ITRF - latitude(rad), longitude (rad), altitude (m)
	 * @throws PatriusException
	 * @since 31/12/2024
	 * @author Professor
	 */
	public static List<GeodeticPoint> propagateOrbit(Orbit iniOrbit) throws PatriusException {
		
		SpacecraftState iniState = new SpacecraftState(iniOrbit); //initial conditions for the IVP
		
		//RK intergrator
		double stepRK = 10; //step for the RK integrator (s); can be large since there are no inputs or variations in the orbit
		FirstOrderIntegrator RKIntegrator = new ClassicalRungeKuttaIntegrator(stepRK);
		//Propagator
		NumericalPropagator propagator = new NumericalPropagator(RKIntegrator);
		propagator.resetInitialState(iniState);
		propagator.setOrbitType(OrbitType.CARTESIAN); //propagate with cartesian coordinates
		
		
		//Step handler
		final FactoryManagedFrame ITRF = FramesFactory.getITRF();
		final BodyShape EARTH = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
				Constants.WGS84_EARTH_FLATTENING, ITRF); //create Earth
		//step handler
		final ArrayList<GeodeticPoint> listOfStates = new ArrayList<>();
		PatriusFixedStepHandler myStepHandler = new PatriusFixedStepHandler() {
			private static final long serialVersionUID = 1L;

			public void init(SpacecraftState s0, AbsoluteDate t) {} //not necessary

			/** The step handler used to store every point */
			public void handleStep(SpacecraftState currentState, boolean isLast) throws PropagationException {

				GeodeticPoint geodeticPoint;
				try {
					geodeticPoint = EARTH.transform(currentState.getPVCoordinates().getPosition(), ITRF,
							currentState.getDate()); //latitude (rad), longitude (rad), altitude (m)
				} catch (PatriusException e) {
					throw new PropagationException(e);
				}
				// Adding S/C to the list
				listOfStates.add(geodeticPoint);
			}
		};
		
		//handler period is set on first argument - 1 point computed every x s
		propagator.setMasterMode(10., myStepHandler); //calls handlestep with some inputs
		
		// TO DO - COMPLETE
		double T = iniOrbit.getKeplerianPeriod(); //period (s)
		AbsoluteDate finalDate = iniOrbit.getDate().shiftedBy(T); //advance date by one full rotation around Earth (period)
		final SpacecraftState finalState = propagator.propagate(finalDate); //final point when one revolution is complete

		return listOfStates; //latitude(rad), longitude (rad), altitude (m)
	}
	
	/** 
	 * This method maps the points of Patrius to positions of WorldWind to draw the orbits.
	 * @param points List with Patrius points - latitude (rad), longitude (rad), altitude (m)
	 * @return List with WorldWind points.
	 * @since 31/12/2024
	 * @author Professor
	 */
	public static List<Position> glueBetweenPatriusAndWorldwind(List<GeodeticPoint> points) {
		
		//Create WroldWind points to draw orbit
		List<Position> pointsWW = new ArrayList<Position>(points.size()); 
		for (GeodeticPoint point : points) {
			pointsWW.add(Position.fromRadians(point.getLatitude(), point.getLongitude(), point.getAltitude()));
		}
		return pointsWW;
	}
	
	/**
	 * Frame of the application. Any characteristics of the application frame and control commands are 
	 * within this class
	 */
	public static class AppFrame extends ApplicationTemplate.AppFrame {
		
		/**
		 * Initialise application.
		 */
		public AppFrame() throws PatriusException {
			super(false,false,false); //toggle some visual controls (status bar, layer panel, status panel)
			
			// Get space objects' information
			ObjectGatherer orbitsData = new ObjectGatherer("objectdata.txt");
			
			// Create orbits
			RenderableLayer orbitsLayer = new RenderableLayer(); //layer for all orbits
			orbitsLayer.setName("All orbits");
			for (int k = 0; k < 10; k++) {
				
				//get parameters necessary to create the orbit for object k
				double[] par = orbitsData.allObjects.get(k).getEssentialParameters(); //i, ra, e, argper, theta, n
				double a = orbitsData.allObjects.get(k).getSemiMajorAxis(); 
				AbsoluteDate date = orbitsData.allObjects.get(k).getDate();
				
				//create orbit for object of index k
				Orbit objectOrbit = new KeplerianOrbit(a, par[2], par[0], par[3], par[1], par[4],
													   PositionAngle.MEAN,FramesFactory.getGCRF(),
													   date,Constants.WGS84_EARTH_MU);
				
				//Get WorldWind points
				List<GeodeticPoint> pointsPatrius = propagateOrbit(objectOrbit); //get coordinates alongside orbit; lat(rad), long(rad), alt(m)
				List<Position> pointsWW = glueBetweenPatriusAndWorldwind(pointsPatrius); //convert to WorldWind points
				
				//Add WorldWind orbits models to layer
				ShapeAttributes attrs = new BasicShapeAttributes(); //initialise shape and attributes
				attrs.setOutlineMaterial(new Material(Color.YELLOW)); //set colour
				attrs.setOutlineWidth(1d); //set thickness
				//Create a path, set some of its properties and set its attributes.
				Path path = new Path(pointsWW); //path has the previously obtained points
				path.setAttributes(attrs);
				path.setVisible(true);
				path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);  //altitude is measured relative to ground
				path.setPathType(AVKey.GREAT_CIRCLE); //orbits are eliptical - closest shape is circle
				orbitsLayer.addRenderable(path); //render orbits in respective layer
			}
			
			// Add the layer to the model.
			insertBeforeCompass(getWwd(), orbitsLayer);
			
			//Mark position of space objects - TO DO COMPLETE
			List<Marker> markers = new ArrayList<>(1);
			markers.add(new BasicMarker(Position.fromDegrees(90, 0), new BasicMarkerAttributes())); //create markers
			MarkerLayer markerLayer = new MarkerLayer(); //marker layer
			markerLayer.setMarkers(markers); //add markers to layer
			insertBeforeCompass(getWwd(), markerLayer); //add layer to WW
		}
		
	}
}
