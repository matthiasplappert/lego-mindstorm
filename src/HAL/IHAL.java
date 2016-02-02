package HAL;


public interface IHAL {

	void printOnDisplay(String text, long waitDuration);
	
	boolean isTouchButtonPressed();

}
