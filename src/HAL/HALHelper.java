package HAL;

public class HALHelper {
	public static void sleep(long duration){
		{ try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}}
	}
}
