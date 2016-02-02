package HAL;

import java.util.Objects;

import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;

public class HAL implements IHAL{
	@Override
	public void printOnDisplay(String text, final  long waitDuration){
		if(text.isEmpty() || text ==null)
			throw new IllegalArgumentException();
		Objects.requireNonNull(waitDuration);
		LCD.drawString(text,0,0);
		if (waitDuration>0)
			HALHelper.sleep(waitDuration);		
	}

	@Override
	public boolean isTouchButtonPressed() {
		boolean result;
		EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S2);
		float[] sample = new float[1];
		
	    touchSensor.getTouchMode().fetchSample(sample, 0); //ger current sample
	    if(sample[0] == 0){ //not pressed
	    	result = false;
	    }else{ //pressed
	    	result = true;               
	    }
	    touchSensor.close();
	    return result;
	}

}
