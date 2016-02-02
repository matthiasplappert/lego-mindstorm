package test.HAL;

import static org.junit.Assert.fail;

import HAL.IHAL;

public class DefaultTestHAL implements IHAL{

	@Override
	public void printOnDisplay(String text, long waitDuration) {
		fail();
	}

	@Override
	public boolean isTouchButtonPressed() {
		// TODO Auto-generated method stub
		return false;
	}

}
