package State;

public enum MyState {
	
	LineSearchState,  // searches for a line, e.g. after an obstacle
	LineFollowState,  // follows a line
	BarcodeState,
	BridgeState,
	ElevatorState,
	FreeTrackState,
	HangingBridgeState,
	RockerState,
	RollBoxState,
	MazeState,
	FindLineState,
	SensorDataState,
	DriveByState,
	ObstacleEndState,
	BossState,
	ShutDownState,
	ExitState;
	
	public static MyState getFromBarcode(int barcode) {
		switch (barcode) {
		case 2:
			return MyState.LineSearchState;
		case 3:
			return MyState.BridgeState;
		case 4:
			return MyState.RockerState;
		case 5:
			return MyState.HangingBridgeState;
		case 6:
			return MyState.RollBoxState;
		case 1:
			return MyState.FreeTrackState;
		default:
			return MyState.LineSearchState;
		}
	}
	
	public static MyState getInitState(){
		return MyState.ObstacleEndState;
	}
}
