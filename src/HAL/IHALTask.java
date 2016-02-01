package HAL;


public interface IHALTask<I,O> {
	public O action(I input);
}
