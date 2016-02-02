package HAL;


public interface IHAL {

	void printOnDisplay(String text, long waitDuration);
	
	void backward();
	void forward();
	void stop();
	void rotate(int angle, boolean returnImmediately);
	boolean motorsAreMoving();

	boolean isTouchButtonPressed();
}
