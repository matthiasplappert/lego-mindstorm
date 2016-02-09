package communication.commands;

import java.io.IOException;

public interface Command<T> {

	public String getName();

	public T execute() throws IOException;
}
