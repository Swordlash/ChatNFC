package pl.edu.mimuw.chatnfc.security;

public class AuthenticationException extends CryptographicException
{
	public AuthenticationException()
	{
	}
	
	public AuthenticationException(String message)
	{
		super(message);
	}
}
