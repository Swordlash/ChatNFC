package pl.edu.mimuw.chatnfc.security;

public class CryptographicException extends Exception
{
	public CryptographicException()
	{
	}
	
	public CryptographicException(String message)
	{
		super(message);
	}
	
	public CryptographicException(Throwable cause)
	{
		super(cause);
	}
}
