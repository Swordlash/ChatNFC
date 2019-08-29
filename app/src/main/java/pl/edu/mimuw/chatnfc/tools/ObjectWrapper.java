package pl.edu.mimuw.chatnfc.tools;

public class ObjectWrapper<T>
{
	private T object = null;
	
	public T getObject()
	{
		return object;
	}
	
	public void setObject(T object)
	{
		this.object = object;
	}
}
