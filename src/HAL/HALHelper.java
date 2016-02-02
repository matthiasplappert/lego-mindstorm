package HAL;

public class HALHelper {
	public static void sleep(long duration){
		if(duration>0){
			{
				try {
				Thread.sleep(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}}
		}
	}
}
