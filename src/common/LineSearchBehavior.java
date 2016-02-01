package common;

public class LineSearchBehavior extends StateBehavior {
	public LineSearchBehavior(SharedState sharedState) {
		super(sharedState);
	}

	@Override
	public boolean takeControl() {
		return super.takeControl();
	}

	@Override
	public void action() {
		// TODO: search for line, follow it until we read barcode and then modify state
		// this.sharedState.setState(folowUpState);
	}

	@Override
	public void suppress() {
		
	}

	@Override
	State getTargetState() {
		return State.LineSearch;
	}
}
