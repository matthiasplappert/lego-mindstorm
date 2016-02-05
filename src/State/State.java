package State;

public enum State {
	
	BossState,
	BridgeState,
	ElevatorState,
	FreeTrackState,
	HangingBridgeState,
	LineSearch,
	MazeState,
	RockerState,
	RollBoxState,
	FindLineState,
	mazeState, 
	SensorDataState;
	public static State getInitState(){
		// TODO: change this
		return State.SensorDataState;
	}
}
