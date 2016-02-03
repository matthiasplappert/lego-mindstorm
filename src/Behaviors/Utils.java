package Behaviors;

public class Utils {
	public static boolean drawBoolean(){
		return Math.random()<=0.5;
	}
	public static Direction drawDirection(){
		return Utils.drawBoolean() ? Direction.LEFT: Direction.RIGHT;
	}
}
