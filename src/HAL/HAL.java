package HAL;

import java.util.Objects;

import lejos.hardware.lcd.LCD;

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

}
