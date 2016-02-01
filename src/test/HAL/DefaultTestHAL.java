package test.HAL;

import com.google.common.base.Optional;

import HAL.IHAL;
import static org.junit.Assert.*;

public class DefaultTestHAL implements IHAL{

	@Override
	public void printOnDisplay(String text, Optional<Long> waitDuration) {
		fail();
	}

}
