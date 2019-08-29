package pl.edu.mimuw.chatnfc.security;

import pl.edu.mimuw.chatnfc.tools.BinaryData;

public abstract class EncryptedDataPackage extends BinaryData
{
	public EncryptedDataPackage()
	{
	}
	
	public EncryptedDataPackage(byte[] data)
	{
		super(data);
	}
	
	public abstract byte[] getDataEncrypted();
	public abstract byte[] getDataDecrypted(byte[] key) throws Exception;
}
