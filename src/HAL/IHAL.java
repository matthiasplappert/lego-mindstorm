package HAL;

import Behaviors.LineType;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.utility.Delay;

public interface IHAL {

	void printOnDisplay(String text, int row, long waitDuration);
	
	void setSpeed(Speed speed);
	void backward();
	void forward();
	void stop();
	void rotate(int angle);
	void rotateTo(int angle, boolean rotateFastestWay);
	void turn(int angle);
	void turn(int angle, boolean reverse);
	void turnTo(int angle, boolean turnFastestWay);
	
	void moveDistanceSensorToPosition(DistanceSensorPosition position);
	void moveDistanceSensorToPosition(int angle);
	boolean motorsAreMoving();
	boolean isRotating();
	
	/**
	 * Course-based navigation.
	 * 
	 * Usage:
	 * this.hal.setCourseFollowingAngle(10);
	 * this.hal.resetGyro();
	 * while (!this.suppressed) {
	 *     this.hal.performCourseFollowingStep();
	 *     if (someCondition) {
	 *         break;
	 *     }
	 *     Delay.msDelay(10);
	 * }
	 */
	void setCourseFollowingAngle(int followAngle);
	void performCourseFollowingStep();
	void performCourseFollowingStep(boolean reverse);
	
//	float getRGB();
	float getMeanDistance();	
	void resetGyro();
	float getCurrentGyro();
	float getMeanGyro();
	float getCurrentDistance();
	
	EV3ColorSensor getColorSensor(); //remove later
	EV3UltrasonicSensor getUltrasonicSensor();

	boolean isTouchButtonPressed();

	LineType getLineType();

	float getMeanColor();

	ColorMode getColorMode();

	void setColorMode(ColorMode cm);

	boolean isRedColorMode();
	
	void resetLeftTachoCount();
	void resetRightTachoCount();
	int getLeftTachoCount();
	int getRightTachoCount();
	float getLeftTachoDistance();
	float getRightTachoDistance();
	
	float convertTachoCountToDistance(int tachoCount);
}
