package HAL;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.google.common.base.Preconditions;

public class HALCompositionTask<I,O> implements  IHALTask<I,O>{
	private Queue<IHALTask> tasks;

	public HALCompositionTask(){
		this.tasks = new LinkedList<IHALTask>();
	}
	public void add(IHALTask task){
		this.tasks.add(task);
	}
	@Override
	public O action(I in) {
		Object output = in;
		Object input =null;
		for(IHALTask t : this.tasks){
			input = output;
			output = t.action(input);
		}
		return (O) output;
	}
	

}
