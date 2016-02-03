package HAL;

import lejos.hardware.sensor.EV3ColorSensor;

public class DefaultHAL implements IHAL {

	@Override
	public void printOnDisplay(String text, int row, long waitDuration) {
		
	}

	@Override
	public void backward() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forward() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rotate(int angle, boolean returnImmediately) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean motorsAreMoving() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean isTouchButtonPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getRGB() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveDistanceSensorToPosition(int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetGyro() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getGyroValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public EV3ColorSensor getColorSensor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRotating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void turn(int angle, boolean stopInnerChain, boolean immediateReturn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getRedColorSensorValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void enableRedMode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forward(Speed speed) {
		// TODO Auto-generated method stub
		
	}
}
