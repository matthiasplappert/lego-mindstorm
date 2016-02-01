package test;


import org.junit.Test;
import static org.junit.Assert.*;
import com.google.common.eventbus.Subscribe;

import State.SharedState;
import State.State;
import State.StateChangeEvent;

public class SharedStateTest {

	@Test
	public void testStateChangeEvents() {
		SharedState s = new SharedState(State.getInitState());
		s.register(new Object(){
			@Subscribe
			void handle(StateChangeEvent sce){
				if(!sce.getNewState().equals(State.TestState)){
					fail();
				}
			}
		});
	}

}
