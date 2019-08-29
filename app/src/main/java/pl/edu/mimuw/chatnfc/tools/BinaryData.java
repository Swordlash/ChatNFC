package pl.edu.mimuw.chatnfc.tools;

public abstract class BinaryData
{
	public BinaryData()
	{
	}
	
	public BinaryData(byte[] data)
	{
		fromByteArray(data);
	}
	
	public abstract void fromByteArray(byte[] data);
	
	public abstract byte[] toByteArray();
}
