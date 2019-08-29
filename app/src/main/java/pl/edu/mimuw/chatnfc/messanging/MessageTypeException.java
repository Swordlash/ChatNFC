package pl.edu.mimuw.chatnfc.messanging;

public class MessageTypeException extends RuntimeException
{
	public MessageTypeException(Class<? extends Message> expected, Class<? extends Message> actual)
	{
		super("Exception in type descriptor in Message#fromByteArray(): expected " + expected
				.getSimpleName() + ", actual: " + actual.getSimpleName());
	}
}
