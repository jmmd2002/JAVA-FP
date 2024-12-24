package finalproject;

public class NumericalPropagationWithStopEvent {

    public static void main(String[] args) throws PatriusException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        // Recovery of the UTC time scale using a "factory" (not to duplicate such unique object)
        final TimeScale TUC = TimeScalesFactory.getUTC();
 
        // Date of the orbit given in UTC time scale)
        final AbsoluteDate date = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);
 
        // Getting the frame with wich will defined the orbit parameters
        // As for time scale, we will use also a "factory".
        final Frame GCRF = FramesFactory.getGCRF();
 
        // Initial orbit
        final double sma = 7200.e+3;
        final double exc = 0.02;
        final double per = sma*(1.-exc);
        final double apo = sma*(1.+exc);
        final double inc = FastMath.toRadians(98.);
        final double pa = FastMath.toRadians(0.);
        final double raan = FastMath.toRadians(0.);
        final double anm = FastMath.toRadians(180.);
        final double MU = Constants.WGS84_EARTH_MU;
 
        final ApsisRadiusParameters par = new ApsisRadiusParameters(per, apo, inc, pa, raan, anm, PositionAngle.MEAN, MU);
        final Orbit iniOrbit = new ApsisOrbit(par, GCRF, date);
 
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
 
//SPECIFIC
        // Definition of the Earth ellipsoid
        final Frame ITRF = FramesFactory.getITRF();
        final double AE = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        final BodyShape EARTH = new OneAxisEllipsoid(AE, Constants.WGS84_EARTH_FLATTENING, ITRF, "EARTH");
 
        // Adding an altitude stop event
        final double endAlt = 750.e+3;
        final AltitudeDetector stopEvent = new AltitudeDetector(endAlt, EARTH);
        propagator.addEventDetector(stopEvent);
//SPECIFIC
 
        // Propagating on one orbital period
        final double dt = iniOrbit.getKeplerianPeriod();
        final AbsoluteDate finalDate = date.shiftedBy(dt);
        final SpacecraftState finalState = propagator.propagate(finalDate);
        final Orbit finalOrbit = finalState.getOrbit();
 
        // Get geodetic coordinates (altitude, latitude, longitude)
        final GeodeticPoint iniGeodeticPoint = EARTH.transform(iniOrbit.getPVCoordinates().getPosition(), ITRF, date);
        final GeodeticPoint finalGeodeticPoint = EARTH.transform(finalOrbit.getPVCoordinates().getPosition(), ITRF, date);
 
        System.out.println();
        iniOrbit.getPVCoordinates(ITRF);
        System.out.println("Initial altitude = "+iniGeodeticPoint.getAltitude()/1000.+" km");
        System.out.println("New date = "+finalOrbit.getDate().toString(TUC)+" deg");
        System.out.println("Final altitude = "+finalGeodeticPoint.getAltitude()/1000.+" km");
 
    }

}
