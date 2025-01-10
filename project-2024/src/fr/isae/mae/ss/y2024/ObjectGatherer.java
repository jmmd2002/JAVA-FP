package fr.isae.mae.ss.y2024;

import java.io.File;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.cnes.sirius.patrius.bodies.BodyShape;
import fr.cnes.sirius.patrius.bodies.GeodeticPoint;
import fr.cnes.sirius.patrius.bodies.OneAxisEllipsoid;
import fr.cnes.sirius.patrius.frames.FactoryManagedFrame;
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
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import fr.cnes.sirius.patrius.utils.exception.PropagationException;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Reads the .txt file containing all the information about the space objects from SpaceTrack.org database and
 * stores all the necessary parameters to represent the objects' orbits using NASA WorldView JAVA package. The
 * data is in SpaceTrack's TLE (Two Line Element) format. The units of the data are converted to SI
 * <p> 
 * The data can be requested through SpaceTrack's API or downloaded directly from (27/12/2024):
 * <br>
 * https://www.space-track.org/#recent
 * <p>
 * @param fileName String containing the name of the .txt file
 * @return Information about the space object and its orbit in SI units
 * @since 27/12/2024
 * @author joaom
 * @see
 */
public class ObjectGatherer {
	
	List<SpaceObject> allObjects = new ArrayList<>();

	public ObjectGatherer(String fileName) throws PatriusException {
		
		//Get file path
		String currentDir = System.getProperty("user.dir");
    	String filePath = currentDir + File.separator + fileName; 
    	
    	//Read and store lines from .txt file
        List<String> data = readFile(filePath);
        
        AbsoluteDate currentDateUTC = new AbsoluteDate(LocalDateTime.now(ZoneId.of("UTC")), TimeScalesFactory.getTAI()); //current UTC date
        //Store the parameters from the txt
        int nO = 0; //space object counter
        for (String lineData : data) {
        	
        	//Each object has 3 lines associated with different info. The first element
        	//tells us which line we are at, and we store the rest of the info accordingly
        	switch (lineData.substring(0, 1)) {
        	
        	case "0": //line 0 only contains the name of the object
        		
        		//create object when there is a name
        		SpaceObject object = new SpaceObject(lineData.substring(1)); //name without row index
        		allObjects.add(object); //add to list with all space objects
        		nO = nO + 1; //advance counter
        		break;
        		
        	case "1": //line 1 contains the epoch time (UTC)
        		
        		// Format time data
        		String date = lineData.substring(18,33); //date in the format yyddddddd....
        		date = date.replace(" ", ""); //remove white spaces if day < 100
        		AbsoluteDate UTCDate = yearDayToUTC(date); //epoch time in UTC; precision to the second
        		
        		allObjects.get(nO-1).addDate(UTCDate); //add time to space object nO
        		break;
        		
        	case "2": //line 2 contains the orbit parameters
        		
        		double i = Double.parseDouble(lineData.substring(8, 17).strip())*Math.PI/180; //inclination (rad)
        		double rAsc = Double.parseDouble(lineData.substring(17, 26).strip())*Math.PI/180; //right ascension of ascending node (rad)
        		String eString = "0." + lineData.substring(26, 34).strip();
        		double e = Double.parseDouble(eString); //eccentricity
        		double argPer = Double.parseDouble(lineData.substring(34, 43).strip())*Math.PI/180; //argument of the perigee (rad)
        		double theta = Double.parseDouble(lineData.substring(43,52).strip())*Math.PI/180; //mean anomaly (rad)
        		double n = Double.parseDouble(lineData.substring(52,64).strip())*2*Math.PI/(24*60*60); //mean motion (rad/s)
        		
        		allObjects.get(nO-1).addOrbit(i,rAsc,e,argPer,theta,n); //add orbit parameters and initial position to space object
        		allObjects.get(nO-1).addCurrentPosition(currentDateUTC, allObjects.get(nO-1).orbit); //add current position
        		System.out.println(nO + "/" + data.size()/3); //Display progress
        		break;
        		
        	default:
        	}
        }
	}
	
	/**
	 * Reads a .txt file and stores all the lines in a list of strings. Each element of the list
	 * corresponds to a line.
	 * <p>
	 * @param filePath String containing the full file path
	 * @return List of rows as strings
	 * @since 27/12/2024
	 * @author joaom
	 */
    private static List<String> readFile(String filePath) {
    	
    	//Open file and close at the end
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) { 
            String line = new String();
            
            //Read line and separate elements until file ends
            while ((line = br.readLine()) != null) {
                data.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
	
	/**
	 * Converts a date in UTC to an AbosoluteDate in UTC accurate to the second to be used by the KeplerianOrbit class. 
	 * The date must be in the following format: yydddddd....
	 * <br> This function only works from the year 2000 to 2999. 
	 * 
	 * @param date String containing the last 2 digits of the year (starting from 2000) 
	 * in the first 2 characters and the day in the rest
	 * @return date converted to AbsoluteDate accurate to the second
	 * @since 30/12/2024
     * @author joaom
	 */
	public static AbsoluteDate yearDayToUTC(String date) throws PatriusException{
		
		int year = Integer.parseInt(date.substring(0, 2)) + 2000; //year starting from 2000
		double rest = Double.parseDouble(date.substring(2)); //days in raw format
		int day = (int) rest; 
		rest = rest - day; //remove day; get decimals for hours
		LocalDate utcDate = LocalDate.ofYearDay(year, day); //get date in UTC
		
		day = utcDate.getDayOfMonth();
		int month = utcDate.getMonthValue(); 
		int hour = (int) (rest*24);
		rest = rest*24 - hour; //remove hour; get decimals for minutes
		int minute = (int) (rest*60);
		rest = rest*60 - minute; //remove minute; get decimals for seconds
		int second = (int) (rest*60); 

		//get UTC date in the correct format for the KeplerianOrbit class
		AbsoluteDate UTCDate = new AbsoluteDate(year,month,day,hour,minute,second);
		return UTCDate;
	}
	
	/**
	 * Used to store the characteristics of a space object and its orbit received from a line of text
	 * from SpaceTrack's .txt catalog. The values are meant to be stored
	 * in SI units.
	 * 
	 * @returns SpaceObject with corresponding name, last sampling date, orbit parameters and position in space
	 * @since 29/12/2024
	 * @author joaom
	 */
	public static class SpaceObject {
		
		private String name; //name of space object
		private String type; //type of space object (satellite, debris, etc..)
		private Color cor; //marker color for WorldWind
		private AbsoluteDate date; //epoch UTC time of the data sampling with accuracy to the second
		
		private KeplerianOrbit orbit; //Keplerian orbit
		private Path path; //path to be drawn; contains points of orbit as well (lat, long, alt)
		private double[] initialPos = new double[3]; //position of the object when data is read (lat long alt) in rad; m
		private double[] currentPos = new double[3]; //current position of the object (lat long alt) in rad; m
		
		
		/**
		 * Constructor of SpaceObject. Initialise with name.
		 * 
		 * @param objectName String containing the object's name and type.
		 * @since 29/12/2024
		 * @author joaom
		 */
		public SpaceObject(String objectName) {
			name = objectName;
			setType(objectName);
		}
		
		/**
		 * Sets space object last data sampling time, in UTC time date.
		 * 
		 * @param dataTime AbsoluteDate in UTC
		 * @since 29/12/2024
		 * @author joaom
		 */
		public void addDate(AbsoluteDate dataTime) {
			date = dataTime;
		}
		
		/**
		 * Stores the objects orbit as a KeplerianOrbit and a WorldWind path to be drawn on the application.
		 * 
		 * @param i inclination (rad)
		 * @param rAsc right ascension of ascending node (rad)
		 * @param e eccentricity
		 * @param argPer argument of the perigee (rad)
		 * @param theta mean anomaly (rad)
		 * @param n mean motion (rad/s)
		 * @since 01/01/2025
		 * @author joaom
		 * @throws PatriusException
		 */
		public void addOrbit(double i, double rAsc, double e, double argPer, 
                double theta, double n) throws PatriusException {
			
			//Get orbits and initial position
			orbit = computeOrbit(i,rAsc,e,argPer,theta,n); //add orbit to space object
			double T = orbit.getKeplerianPeriod(); //orbit period (s)
			
			//get patrius points; more points for more eccentric orbits
			List<GeodeticPoint> patriusPoints = propagateOrbit(orbit, T,Math.round(T/(1+e/0.005)));
			path = new Path(glueBetweenPatriusAndWorldwind(patriusPoints)); //convert to world wind path
			
			//Initial positions
			initialPos[0] = patriusPoints.get(0).getLatitude(); //latitude (rad)
			initialPos[1] = patriusPoints.get(0).getLongitude(); //longitude (rad)
			initialPos[2] = patriusPoints.get(0).getAltitude(); //altitude (m)

			//Set path's attributes
			ShapeAttributes attrs = new BasicShapeAttributes(); //initialise shape and attributes
			attrs.setOutlineMaterial(new Material(Color.YELLOW)); //set colour
			attrs.setOutlineWidth(0.5d); //set thickness
			path.setAttributes(attrs);
			path.setVisible(false); //starts not visible by default
			path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);  //altitude is measured relative to ground
			path.setPathType(AVKey.GREAT_CIRCLE); //orbits are eliptical - closest shape is circle
		}
		
		/**
		 * Adds the current position of each space object.
		 * @param currentDate AbsoluteDate corresponding the current date in UTC format
		 * @param orbit Orbit of respective space object
		 * @throws PatriusException
		 * @since 02/01/2025
		 * @author joaom
		 */
		public void addCurrentPosition (AbsoluteDate currentDate, Orbit orbit) throws PatriusException {
			
			//seconds passed since epoch date until current date - adjusted for orbit periods - program runs faster
			double timeDiff = currentDate.durationFrom(date); 
			double timeDiffAdjusted = adjustTime(timeDiff, orbit.getKeplerianPeriod());
			GeodeticPoint currentPoint = propagateOrbit(orbit,timeDiffAdjusted,timeDiffAdjusted).get(1);
			currentPos[0] = currentPoint.getLatitude(); //latitude (rad)
			currentPos[1] = currentPoint.getLongitude(); //longitude (rad) 
			currentPos[2] = currentPoint.getAltitude(); //altitude(rad)
		}
		
		/**
		 * Returns the object's name.
		 * 
		 * @return String containing name of the object
		 * @since 29/12/2024
		 * @author joaom
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Returns the object's type.
		 * 
		 * @return String containing type of the object
		 * @since 10/01/2025
		 * @author pedro
		 */
		public String getType() {
            return type;
        }
		
		/**
		 * Returns the object's marker color.
		 * 
		 * @return color 
		 * @since 10/01/2025
		 * @author pedro
		 */
		public Color getColor() {
			return cor;
		}
		
		/**
		 * Returns the object's last sampling date.
		 * 
		 * @return String containing sampling date of the object in UTC time
		 * @since 29/12/2024
		 * @author joaom
		 */
		public AbsoluteDate getDate() {
			return date;
		}
		
		/**
		 * Returns the object's orbital period, in seconds.
		 * 
		 * @return Orbital period (s)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getT() {
			return orbit.getKeplerianPeriod();
		}
		
		/**
		 * Returns the orbit's semi-major axis, in meters.
		 * 
		 * @return Semi-major axis (m)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getA() {
			return orbit.getA();
		}
		
		/**
		 * Returns the orbit's inclination in rad.
		 * 
		 * @return inclination (rad)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getI() {
			return orbit.getI();
		}
		
		/**
		 * Returns the orbit's right ascension of ascending node in rad.
		 * 
		 * @return right ascension of ascending node (rad)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getRAsc() {
			return orbit.getRightAscensionOfAscendingNode();
		}
		
		/**
		 * Returns the orbit's eccentricity.
		 * 
		 * @return orbit's eccentricity
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getE() {
			return orbit.getE();
		}
		
		/**
		 * Returns the orbit's argument of the perigee in rad.
		 * 
		 * @return argument of the perigee (rad)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getArgPer() {
			return orbit.getPerigeeArgument();
		}
		
		/**
		 * Returns the orbit's mean anomaly in rad.
		 * 
		 * @return mean anomaly (rad)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getTheta() {
			return orbit.getAnomaly(PositionAngle.MEAN);
		}
		
		/**
		 * Returns the mean motion of the space object associated with the orbit in rad/s.
		 * 
		 * @return mean motion (rad/s)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getN() {
			return orbit.getKeplerianMeanMotion();
		}
		
		/**
		 * Returns the latitude at epoch in radians.
		 * 
		 * @return latitude (rad)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getInitialLat() {
			return initialPos[0];
		}
		
		/**
		 * Returns the latitude at current time in radians.
		 * 
		 * @return latitude (rad)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getCurrentLat() {
			return currentPos[0];
		}
		
		/**
		 * Returns the longitude at epoch in radians.
		 * 
		 * @return longitude (rad)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getInitialLon() {
			return initialPos[1];
		}
		
		/**
		 * Returns the longitude at current time in radians.
		 * 
		 * @return longitude (rad)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getCurrentLon() {
			return currentPos[1];
		}
		
		/**
		 * Returns the altitude at epoch in meters.
		 * 
		 * @return altitude (m)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getInitialAlt() {
			return initialPos[2];
		}
	
		/**
		 * Returns the altitude at current time in meters.
		 * 
		 * @return laltitude (m)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getCurrentAlt() {
			return currentPos[2];
		}
		
		/**
		 * Returns the orbit's WorldWind path.
		 * 
		 * @return WorldWind orbit's path
		 * @since 01/01/2025
		 * @author joaom
		 */
		public Path getPath() {
			return path;
		}
		
		/**
		 * Set orbit visibility on WorldWind.
		 * 
		 * @param state true for visible or false for invisible
		 * @since 01/01/2025
		 * @author joaom
		 */
		public void setVisible(boolean state) {
			path.setVisible(state);
		}
		
		/**
		 * Set marker color for the space object
		 * @param selectedColor
		 * @since 10/01/2025
		 * @author pedro
		 */		 
		public void setColor(Color selectedColor) {
			cor = selectedColor;
		}
		

		//TODO add missing filters
		/**
		 * Sets the object's type with the name. 
		 * 
		 * @param name Object's name
		 * @since 10/01/2025
		 * @author pedro
		 */
		public void setType(String name){
            if (name.contains("R/B")) {
                type = "ROCKET_BODY";
            } else if (name.contains("DEB")) {
                type = "DEBRIS";
            } else if (name.contains("STARLINK")) {
                type = "STARLINK";
            } else if (name.contains("ONEWEB")) {
                type = "ONEWEB";
            } else if (name.contains("BEIDOU")) {
                type = "BEIDOU";
            } else if (name.contains("IRIDIUM")) {
                type = "IRIDIUM";  
            } else {
                type = "SATELLITE";
            }
		}
		
		/**
		 * Adjust time of an object in orbit to be within 1 period
		 * @param time
		 * @param T
		 * @return time equivalent to the same position within 1 period
		 */
		public double adjustTime(double time, double T) {
			
			while (time > T) {time -= T;} //adjust time to be smaller than one period
			return time;
		}
		
		/**
		 * Computes the object's orbit. Necessary to get WorldWind points to draw the orbit's path.
		 * 
		 * @param i inclination (rad)
		 * @param rAsc right ascension of ascending node (rad)
		 * @param e eccentricity
		 * @param argPer argument of the perigee (rad)
		 * @param theta mean anomaly (rad)
		 * @param n mean motion (rad/s)
		 * @since 01/01/2025
		 * @author joaom
		 */
		private KeplerianOrbit computeOrbit(double i, double rAsc, double e, double argPer, 
				                  double theta, double n) {
			
			// Period and semi-major axis necessary for orbit
			double T = (2*Math.PI/n); //period (s)
			double a = Math.pow(Math.cbrt(T*Math.sqrt(Constants.WGS84_EARTH_MU)/(2*Math.PI)),2); //semi-major axis (m)
			
			orbit = new KeplerianOrbit(a,e,i,argPer,rAsc,theta,PositionAngle.MEAN,FramesFactory.getGCRF(),
					   date,Constants.WGS84_EARTH_MU); //compute object orbit
			
			return orbit;
		}
		
		/**
		 * Propagates the orbit of a space object, computing its coordinates along the orbit.
		 * @param iniOrbit Orbit of space object to be propagated
		 * @return List of coordinates along the orbit in ITRF - latitude(rad), longitude (rad), altitude (m)
		 * @throws PatriusException
		 * @since 31/12/2024
		 * @author Professor
		 */
		public static List<GeodeticPoint> propagateOrbit(Orbit iniOrbit, double shift, double step) throws PatriusException {
			
			SpacecraftState iniState = new SpacecraftState(iniOrbit); //initial conditions for the IVP
			
			//RK intergrator
			double stepRK = 100; //step for the RK integrator;
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
			propagator.setMasterMode(step, myStepHandler); //calls handlestep with some inputs
			
			AbsoluteDate finalDate = iniOrbit.getDate().shiftedBy(shift+step); //advance date to final point + step to avoid holes in orbits
			propagator.propagate(finalDate); //propagate until desired time

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
	}
}
