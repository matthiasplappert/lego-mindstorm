package Behaviors;

public class Utils {
	public static boolean drawBoolean(){
		return Math.random()<=0.5;
	}
	public static Direction drawDirection(){
		return Utils.drawBoolean() ? Direction.LEFT: Direction.RIGHT;
	}
	public static int considerDirectionForRotation(int angle, Direction direction){
		return (direction == Direction.RIGHT) ? angle : -angle;

	}
}
