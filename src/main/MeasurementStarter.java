package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import HAL.HAL;
import HAL.IHAL;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;

public class MeasurementStarter {
	public static final int SAMPLE_RATE = 100;

	public static void main(String[] args) throws IOException{
		IHAL hal = new HAL();
		BufferedWriter out = null;
		
		final EV3ColorSensor sensor = hal.getColorSensor();
		final SensorMode sampleProvider = sensor.getRedMode();

		try {
			out = openWriter("sensor.log");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LCD.drawString("Press return to start", 0, 0);

		while(Button.ESCAPE.isDown() == false){//solange Knopf nicht gedrückt ist
			Delay.msDelay(100);
		}
		LCD.clear();
		LCD.drawString("MeasurementMode run", 0, 0);

		Sound.beep();
		Delay.msDelay(1000);
		Sound.beep();
		try{
			
		
		while(hal.isTouchButtonPressed() == false){
			//drive forward
			hal.forward();
			float[] valueBuffer = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(valueBuffer, 0);
			float currentVal = valueBuffer[0];
			writeToLogFile(currentVal,out);
			//measure data		
			LCD.drawString("currentVal: " + currentVal, 0, 1);

			out.flush();
			Delay.msDelay(SAMPLE_RATE);//wait for next sample
		}
		out.close();

		
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			sensor.close();
		}
		LCD.drawString("MeasurementMode finished", 0, 0);
		Delay.msDelay(1000);
		System.exit(0);

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