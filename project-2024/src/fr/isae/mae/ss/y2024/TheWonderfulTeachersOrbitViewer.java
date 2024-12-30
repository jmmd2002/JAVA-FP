/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
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

/**
 * Example of {@link Path} usage. A Path is a line or curve between positions.
 * The path may follow terrain, and may be turned into a curtain by extruding
 * the path to the ground.
 *
 * @author tag
 * @version $Id: Paths.java 2292 2014-09-02 21:13:05Z tgaskins $
 */
public class TheWonderfulTeachersOrbitViewer extends ApplicationTemplate {

	/** The orbit propagator */
	public static List<GeodeticPoint> propagateMyWonderfulOrbit(Orbit iniOrbit) throws PatriusException {

		// We create a spacecratftstate
		final SpacecraftState iniState = new SpacecraftState(iniOrbit);

		// Initialization of the Runge Kutta integrator with a 2 s step
		final double pasRk = 2.;
		final FirstOrderIntegrator integrator = new ClassicalRungeKuttaIntegrator(pasRk);

		// Initialization of the propagator
		final NumericalPropagator propagator = new NumericalPropagator(integrator);
		propagator.resetInitialState(iniState);

		// Forcing integration using cartesian equations
		propagator.setOrbitType(OrbitType.CARTESIAN);

		final FactoryManagedFrame ITRF = FramesFactory.getITRF();

		final BodyShape EARTH = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
				Constants.WGS84_EARTH_FLATTENING, ITRF);
//SPECIFIC
		// Creation of a fixed step handler
		final ArrayList<GeodeticPoint> listOfStates = new ArrayList<>();
		PatriusFixedStepHandler myStepHandler = new PatriusFixedStepHandler() {
			private static final long serialVersionUID = 1L;

			public void init(SpacecraftState s0, AbsoluteDate t) {
				// Nothing to do ...
			}

			/** The step handler used to store every point */
			public void handleStep(SpacecraftState currentState, boolean isLast) throws PropagationException {

				GeodeticPoint geodeticPoint;
				try {
					geodeticPoint = EARTH.transform(currentState.getPVCoordinates().getPosition(), ITRF,
							currentState.getDate());
				} catch (PatriusException e) {
					throw new PropagationException(e);
				}
				// Adding S/C to the list
				listOfStates.add(geodeticPoint);
			}
		};
		// The handler frequency is set to 10S
		propagator.setMasterMode(10., myStepHandler);
//SPECIFIC

		// Propagating 100s
		final double dt = iniOrbit.getKeplerianPeriod();
		final AbsoluteDate finalDate = iniOrbit.getDate().shiftedBy(dt);
		final SpacecraftState finalState = propagator.propagate(finalDate);

		return listOfStates;

	}

	/** This method maps the points of Patrius to positions of WorldWind */
	public static List<Position> glueBetweenPatriusAndWorldwind(List<GeodeticPoint> points) {
		List<Position> positions = new ArrayList<Position>(points.size());
		for (GeodeticPoint point : points) {
			positions.add(Position.fromRadians(point.getLatitude(), point.getLongitude(), point.getAltitude()));
		}
		return positions;

	}

	/** The AppFrame of our application */
	public static class AppFrame extends ApplicationTemplate.AppFrame {

		/**
		 * Constructor
		 * 
		 * @throws PatriusException
		 */
		public AppFrame() throws PatriusException {
			super(true, true, false);
			
			ObjectGatherer orbitsData = new ObjectGatherer("objectdata.txt");
			
			RenderableLayer layer = new RenderableLayer();
			for (int k = 0; k < 100; k++) {
				
				//get parameters necessary to create the orbit for object k
				double[] par = orbitsData.allObjects.get(k).getEssentialParameters(); //i, ra, e, argper, theta, n
				double a = orbitsData.allObjects.get(k).getSemiMajorAxis(); 
				//create orbit for object of index k
				Orbit theOrbit = new KeplerianOrbit(a, par[2], par[0], par[3], par[1], par[4],
													   PositionAngle.MEAN,FramesFactory.getGCRF(),
													   new AbsoluteDate(),Constants.WGS84_EARTH_MU); 

				List<GeodeticPoint> points = propagateMyWonderfulOrbit(theOrbit);
				List<Position> positions = glueBetweenPatriusAndWorldwind(points);

				layer.setName("Some orbit");

				// Create and set an attribute bundle.
				ShapeAttributes attrs = new BasicShapeAttributes();
				attrs.setOutlineMaterial(new Material(Color.YELLOW));
				attrs.setOutlineWidth(2d);

				// Create a path, set some of its properties and set its attributes.

				Path path = new Path(positions); 
				path.setAttributes(attrs);
				path.setVisible(true);
				path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
				path.setPathType(AVKey.GREAT_CIRCLE);
				layer.addRenderable(path);
			}

			// Add the layer to the model.
			insertBeforeCompass(getWwd(), layer);

			List<Marker> markers = new ArrayList<>(1);
			markers.add(new BasicMarker(Position.fromDegrees(90, 0), new BasicMarkerAttributes()));
			MarkerLayer markerLayer = new MarkerLayer();
			markerLayer.setMarkers(markers);
			insertBeforeCompass(getWwd(), markerLayer);
		}
	}

	public static void main(String[] args) {
		WorldWind.setOfflineMode(true);
		ApplicationTemplate.start("The Teacher's Wonderful Orbit Displayer", AppFrame.class);
	}
}
