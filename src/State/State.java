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
	SensorDataState,
	BarcodeState;
	public static State getInitState(){
		// TODO: change this
		return State.SensorDataState;
	}
}
