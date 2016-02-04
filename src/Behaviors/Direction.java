package Behaviors;

public enum Direction {
LEFT, RIGHT;
	public Direction getOppositeDirection(){
		return this.equals(Direction.LEFT)? Direction.RIGHT : Direction.LEFT;
	}
	public int getMultiplierForDirection(){
		return this.equals(Direction.RIGHT)? 1 : -1;
	}
}
