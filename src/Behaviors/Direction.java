package Behaviors;

public enum Direction {
LEFT, RIGHT;
	public static Direction changeDirection(Direction d){
		return d.equals(Direction.LEFT)? Direction.RIGHT : Direction.LEFT;
	}
}
