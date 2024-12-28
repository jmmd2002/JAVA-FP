package fr.isae.mae.ss.y2024;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Reads the .txt file containing all the information about the space objects from SpaceTrack.org database and
 * stores all the necessary parameters to represent the objects' orbits using NASA WorldView JAVA package. The
 * data is in SpaeTrack's TLE (Two Line Element) format.
 * <p> 
 * The data can be requested through SpaceTrack's API or downloaded directly from (27/12/2024):
 * <br>
 * https://www.space-track.org/#recent
 * <p>
 * @param fileName String containing the name of the .txt file
 * @return Information about the space object and its orbit
 * @since 27/12/2024
 * @author joaom
 * @see
 */
public class OrbitGatherer {
	
    public final List<String> objectName = new ArrayList<>();
    public final List<Long> objectTime = new ArrayList<>();
    public final List<Double> objectI = new ArrayList<>();
    public final List<Double> objectRAsc = new ArrayList<>();
    public final List<Double> objectE = new ArrayList<>();
    public final List<Double> objectArgPer = new ArrayList<>();
    public final List<Double> objectTheta = new ArrayList<>();
    public final List<Double> objectN = new ArrayList<>();

	public OrbitGatherer(String fileName) {
		
		//Get file path
		String currentDir = System.getProperty("user.dir");
    	String filePath = currentDir + File.separator + fileName; 
    	
    	//Read and store lines from .txt file
        List<String> data = readFile(filePath);
        
        //Store the parameters from the txt
        for (String lineData : data) {
        	
        	//Each object has 3 lines associated with different info. The first element
        	//tells us which line we are at, and we store the rest of the info accordingly
        	switch (lineData.substring(0, 1)) {
        	
        	case "0": //line 0 only contains the name of the object
        		
        		String name = lineData.substring(1); //get name without row index
        		objectName.add(name); //add to list
        		break;
        		
        	case "1": //line 1 contains the epoch time (UTC)
        	
        		String date = lineData.substring(18,33); //date in the format yyddddddd....
        		date = date.replace(" ", ""); //remove white spaces if day < 100
        		long time = yearDayToSecond(date); //epoch time in seconds; precision to the second
        		
        		objectTime.add(time); //add to list
        		break;
        		
        	case "2": //line 2 contains the orbit parameters
        		
        		double i = Double.parseDouble(lineData.substring(8, 17).strip()); //inclination (deg)
        		double rAsc = Double.parseDouble(lineData.substring(17, 26).strip()); //right ascension of ascending node (deg)
        		String eString = "0." + lineData.substring(26, 34).strip();
        		double e = Double.parseDouble(eString); //eccentricity
        		double argPer = Double.parseDouble(lineData.substring(34, 43).strip()); //argument of the perigee (deg)
        		double theta = Double.parseDouble(lineData.substring(43,52).strip()); //mean anomaly (deg)
        		double n = Double.parseDouble(lineData.substring(52,64).strip()); //mean motion (rev/day)
        		
        		//Add parameters to respective lists
        		objectI.add(i);
        		objectRAsc.add(rAsc);
        		objectE.add(e);
        		objectArgPer.add(argPer);
        		objectTheta.add(theta);
        		objectN.add(n);
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
	 * Converts a date to seconds. The date must be in the following format: yydddddd....
	 * <br> This function only works from the year 2000 to 2999. It accepts decimals but the result is returned
	 * as a long, accurate to the second.
	 * 
	 * @param date String containing the last 2 digits of the year (starting from 2000) 
	 * in the first 2 characters and the day in the rest
	 * @return date converted to seconds as long class
	 * @since 28/12/2024
     * @author joaom
	 */
	public static long yearDayToSecond(String date) {
		
		int year = Integer.parseInt(date.substring(0, 2)) + 2000; //year starting from 2000
		double rest = Double.parseDouble(date.substring(2)); //days
		long second = (long) ((rest-1)*24*60*60); //convert days into seconds to second precision
		
		//Convert from UTC time to epoch seconds
		OffsetDateTime utcTime = OffsetDateTime.of(year,1,1,0,0,0,0, ZoneOffset.UTC); //convert year to seconds
		long timeInSeconds = utcTime.toEpochSecond() + second; //add remaining seconds to year in seconds
		return timeInSeconds;
	}
}