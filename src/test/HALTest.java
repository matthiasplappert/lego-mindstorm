package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


import HAL.IHAL;
import test.HAL.DefaultTestHAL;


	public class HALTest {

		@Test public void print(){
			final String hello_text = "Hello World";
			IHAL hal = new DefaultTestHAL(){
				@Override
				public void printOnDisplay(String text, long waitDuration) {
					assertEquals(hello_text,text);
				}
			};
			hal.printOnDisplay(hello_text, 0);
			
		}


}
