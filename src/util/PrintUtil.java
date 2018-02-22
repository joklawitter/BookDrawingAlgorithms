package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PrintUtil {

	public static void printStartTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		System.out.println("Start: " +  dateFormat.format(new Date()));	
	}
	
	public static void printEndTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		System.out.println("End:   " +  dateFormat.format(new Date()));	
	}
	
}
