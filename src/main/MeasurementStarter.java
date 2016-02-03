package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import HAL.HAL;
import HAL.IHAL;

public class MeasurementStarter {
	public static void main(String[] args) throws IOException{
		IHAL hal = new HAL();
		BufferedWriter out = openWriter("sensor.log");
		
//		while()
	}
	private static BufferedWriter openWriter(String filename) throws IOException{
			filename+= System.currentTimeMillis()+".data";
		File file = new File(filename);

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
	    return out;
	}

	private static void writeToLogFile(float sample, BufferedWriter out) throws IOException {
	    out.write(Float.toString(sample));
	    out.newLine();
	}
}