package HAL;

import java.util.Objects;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;

public class HAL implements IHAL{
	private RegulatedMotor motorLeft;
	private RegulatedMotor motorRight;
	
	public HAL() {
		this.motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
		this.motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
	}
	
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
	public void forward() {
		this.motorLeft.forward();
		this.motorRight.forward();
	}
	
	@Override
	public void backward() {
		this.motorLeft.backward();
		this.motorRight.backward();
	}
	
	@Override
	public void stop() {
		this.motorLeft.stop();
		this.motorRight.stop();
	}
	
	@Override
	public void rotate(int angle, boolean immediateReturn) {
		this.motorLeft.rotate(angle, true);
		this.motorRight.rotate(-angle, true);
		if (immediateReturn) {
			return;
		}
		while (this.motorsAreMoving()) Thread.yield();
	}
	
	@Override
	public boolean motorsAreMoving() {
		return this.motorLeft.isMoving() || this.motorRight.isMoving();
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
