package common;

import java.util.Objects;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import lejos.hardware.lcd.LCD;

public class Output {
	/**
	 * Show the given text on the LCD Display. If waitDuration is not null, wait for waitDuration milliSeconds.
	 * @param text
	 * @param x
	 * @param y
	 * @param waitDuration
	 */
	public static void printOnDisplay(String text, int x, int y, Optional<Long> waitDuration){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(text));
		Objects.requireNonNull(waitDuration);
//		LCD.drawString(text,x,y);
		System.out.println(text);
		if (waitDuration.isPresent()) { try {
			Thread.sleep(waitDuration.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}}
		
	}
	public static void printOnDisplay(String text, Optional<Long> waitDuration){
		Output.printOnDisplay(text, 0, 0, waitDuration);
	}
	public static void printOnDisplay(String text){
		
		Output.printOnDisplay(text, 0, 0, Optional.<Long>absent());
	}


}
