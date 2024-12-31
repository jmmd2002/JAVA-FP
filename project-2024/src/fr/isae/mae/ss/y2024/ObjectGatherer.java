package fr.isae.mae.ss.y2024;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

import java.time.LocalDate;
import java.time.Year;

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
        		
        		allObjects.get(nO-1).addParameters(i,rAsc,e,argPer,theta,n); //add parameters to space object
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
		AbsoluteDate UTCDate = new AbsoluteDate(year,month,day,hour,minute,second, TimeScalesFactory.getTAI());
		return UTCDate;
	}
	
	/**
	 * Used to store the characteristics of a space object and its orbit received from a line of text
	 * from SpaceTrack's .txt catalog. The values are meant to be stored
	 * in SI units.
	 * 
	 * @returns SpaceObject with name, epoch time of data sampling (s), orbit inclination, 
	 * right ascension of ascending node, argument of the perigee, mean anomaly, eccentricity, mean motion, period
	 * and semi-major axis, in SI units
	 * @since 29/12/2024
	 * @author joaom
	 */
	public static class SpaceObject {
		
		private String name; //name of space object
		private AbsoluteDate date; //epoch UTC time of the data sampling with accuracy to the second
		
		private double i, rAsc, argPer, theta; //inclination, right ascending node, argument of the perigee, mean anomaly (rad)
		private double e; //eccentricity
		private double n; //mean motion (rad/s)
		private double T; //period (s)
		private double a; //semi-major axis (m)
		
		/**
		 * Constructor of SpaceObject. Initialise with name.
		 * 
		 * @param objectName String containing the object's name
		 * @since 29/12/2024
		 * @author joaom
		 */
		public SpaceObject(String objectName) {
			name = objectName;
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
		 * Adds the essential orbit parameters, in SI units, to space object. With the essential orbit parameters
		 * any other parameter can be computed. Computes adittional necessary parameters.
		 * 
		 * @param objectI inclination (rad)
		 * @param objectRAsc right ascension of ascending node (rad)
		 * @param objectE eccentricity
		 * @param objectArgPer argument of the perigee (rad)
		 * @param objectTheta mean anomaly (rad)
		 * @param mean motion (rad/s)
		 * @since 29/12/2024
		 */
		public void addParameters(double objectI, double objectRAsc, double objectE, double objectArgPer, 
				                  double objectTheta, double objectN) {
			// Essential parameters
			i = objectI; rAsc = objectRAsc; e = objectE; argPer = objectArgPer; theta = objectTheta; n = objectN;
			
			// Other parameters
			T = (2*Math.PI/n); //period (s)
			a = Math.pow(Math.cbrt(T*Math.sqrt(Constants.WGS84_EARTH_MU)/(2*Math.PI)),2); //semi-major axis (m)
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
		 * Returns essential orbit parameters in SI units
		 * 
		 * @return double vector with: 
		 * <br> inclination (rad)
		 * <br> right ascension of ascending node (rad)
		 * <br> eccentricity
		 * <br> argument of the perigee (rad)
		 * <br> mean anomaly (rad)
		 * <br> mean motion (rad/s)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double[] getEssentialParameters() {
			double[] essentialParameters = new double[6];
			essentialParameters[0] = i;
			essentialParameters[1] = rAsc;
			essentialParameters[2] = e;
			essentialParameters[3] = argPer;
			essentialParameters[4] = theta;
			essentialParameters[5] = n;
			
			return essentialParameters;
		}
		
		/**
		 * Returns the object's orbital period, in seconds.
		 * 
		 * @return Orbital period (s)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getPeriod() {
			return T;
		}
		
		/**
		 * Returns the orbit's semi-major axis, in meters.
		 * 
		 * @return Semi-major axis (m)
		 * @since 29/12/2024
		 * @author joaom
		 */
		public double getSemiMajorAxis() {
			return a;
		}
	}
}
