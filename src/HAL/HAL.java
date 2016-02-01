package HAL;

import java.util.Objects;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import lejos.hardware.lcd.LCD;

public class HAL implements IHAL{
	@Override
	public void printOnDisplay(String text, final Optional<Long> waitDuration){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(text));
		Objects.requireNonNull(waitDuration);
		LCD.drawString(text,0,0);
		if (waitDuration.isPresent())
			HALHelper.sleep(waitDuration.get());		
	}

}
