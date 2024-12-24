package fr.isae.mae.ss.y2024;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import fr.cnes.sirius.patrius.bodies.BodyShape;
import fr.cnes.sirius.patrius.bodies.GeodeticPoint;
import fr.cnes.sirius.patrius.bodies.OneAxisEllipsoid;
import fr.cnes.sirius.patrius.frames.FactoryManagedFrame;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.math.ode.FirstOrderIntegrator;
import fr.cnes.sirius.patrius.math.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import fr.cnes.sirius.patrius.orbits.KeplerianOrbit;
import fr.cnes.sirius.patrius.orbits.Orbit;
import fr.cnes.sirius.patrius.orbits.OrbitType;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.propagation.SpacecraftState;
import fr.cnes.sirius.patrius.propagation.numerical.NumericalPropagator;
import fr.cnes.sirius.patrius.propagation.sampling.PatriusFixedStepHandler;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TAIScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import fr.cnes.sirius.patrius.utils.exception.PropagationException;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
/**
 * The orbit viewer from the Teacher. Java is so cool.
 */
public class TheTeachersOrbitViewer extends ApplicationTemplate {

	/** Some constants */
	static TAIScale TUC;
    static FactoryManagedFrame ITRF;
    static Frame GCRF;
	static {
		try {
			// Using TAI because of absence of leap second history
			TUC = TimeScalesFactory.getTAI();
			ITRF = FramesFactory.getITRF();
			GCRF = FramesFactory.getGCRF();
		} catch (PatriusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
    final static AbsoluteDate date = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);

    /**
     * Propagating an orbit
     * @param iniOrbit	The orbit to propagate
     * @return	The Geodetic points
     * @throws PatriusException
     */
	static List<GeodeticPoint> letsPropagateSomeOrbit(Orbit iniOrbit) throws PatriusException {
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

        final BodyShape EARTH = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
        		Constants.WGS84_EARTH_FLATTENING, ITRF);
        
        // Creation of a fixed step handler
        List<GeodeticPoint> toReturn = new ArrayList<GeodeticPoint>();
        PatriusFixedStepHandler myStepHandler = new PatriusFixedStepHandler() {
            private static final long serialVersionUID = 1L;
            public void init(SpacecraftState s0, AbsoluteDate t) {
                // Nothing to do ...
            }
            public void handleStep(SpacecraftState currentState, boolean isLast)
                    throws PropagationException {
                // Adding S/C to the list
                GeodeticPoint geodeticPoint;
				try {
					geodeticPoint = EARTH.transform(currentState.getPVCoordinates().getPosition(), ITRF, date);
				} catch (PatriusException e) {
					throw new RuntimeException(e);
				}
            	toReturn.add(geodeticPoint);
            }
        };
        // The handler frequency is set to 10S
        propagator.setMasterMode(10., myStepHandler);
 
        // Propagating 100s
        final double dt = iniOrbit.getKeplerianPeriod();
        final AbsoluteDate finalDate = date.shiftedBy(dt);
        propagator.propagate(finalDate);
        return toReturn;
        
	}
	
	/**
	 * Method to go from Patrius World (with GeodeticPoints) to WorldWind world (Positions)
	 * @param points The Points
	 * @return The Positions
	 */
	static List<Position> transformToPositions(List<GeodeticPoint> points) {
		/*return points.stream().parallel()
				.map(point -> 
					Position.fromRadians(point.getLatitude(),
							point.getLongitude(),
							point.getAltitude()))
				.collect(Collectors.toList());
		/* or */
		List<Position> positions = new ArrayList<Position>();
		for (GeodeticPoint point: points) {
			positions.add(Position.fromRadians(point.getLatitude(),
					point.getLongitude(),
					point.getAltitude()));
		}
		return positions;
	}

	/**
	 * The graphical frame to display
	 */
    public static class OrbitViewFrame extends ApplicationTemplate.AppFrame {

        private static final long serialVersionUID = -5106522894846764945L;

		public OrbitViewFrame() throws PatriusException {
            super(true, true, false);
            
            // let's create an orbit
            Orbit myOrbit = new KeplerianOrbit(10000e3, 0.01, 
            		0, 0, 0, 0, PositionAngle.MEAN, GCRF, date, 
            		Constants.WGS84_EARTH_MU);
            List<GeodeticPoint> points = letsPropagateSomeOrbit(myOrbit);
            List<Position> positions = transformToPositions(points);
            
            RenderableLayer layer = new RenderableLayer();

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

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

        }
    }
    /**
     * Main method
     * @param args	Arguments to main
     */
    public static void main(String[] args) {
        ApplicationTemplate.start("Teacher's Orbit Viewer", OrbitViewFrame.class);
    }
}
