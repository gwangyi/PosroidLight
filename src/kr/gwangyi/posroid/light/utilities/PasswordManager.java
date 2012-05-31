package kr.gwangyi.posroid.light.utilities;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public abstract class PasswordManager
{
	private static final String HEX = "0123456789ABCDEF";
	
	private static byte [] getRawKey(String seed)
	{
		try
		{
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
	        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
	        sr.setSeed(seed.getBytes());
	        kgen.init(128, sr); // 192 and 256 bits may not be available
	        SecretKey skey = kgen.generateKey();
	        return skey.getEncoded();
		}
		catch (NoSuchAlgorithmException e)
		{
			System.err.println("Couldn't get safe key!");
			String skey = "PleaseDoNotAbuseUnsafeThisKey!!!";
			while(skey.length() < 128) skey += skey;
			return skey.substring(0, 128).getBytes();
		}
	}
	
	public static String encrypt(String seed, String plain)
	{
		byte [] key = getRawKey(seed);
		
		byte [] encrypted;
		try
		{
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	        encrypted = cipher.doFinal(plain.getBytes());
		}
		catch (NoSuchAlgorithmException e)
		{
			System.err.println("Couldn't encrypt by AES!");
			encrypted = plain.getBytes();
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
			encrypted = plain.getBytes();
		}
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
			encrypted = plain.getBytes();
		}
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
			encrypted = plain.getBytes();
		}
		catch (BadPaddingException e)
		{
			e.printStackTrace();
			encrypted = plain.getBytes();
		}

		StringBuffer ret = new StringBuffer();
        for(byte ch : encrypted)
        {
        	ret.append(HEX.charAt((ch >> 4) & 15)).append(HEX.charAt(ch & 15));
        }
        return ret.toString();
	}
	
	public static String decrypt(String seed, String hex)
	{
		byte [] key = getRawKey(seed);
		
		byte [] encrypted = new byte[hex.length() / 2], decrypted;
		for(int i = 0; i < encrypted.length; i++)
			encrypted[i] = Integer.valueOf(hex.substring(i * 2, (i + 1) * 2), 16).byteValue();

		try
		{
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            decrypted = cipher.doFinal(encrypted);
		}
		catch (NoSuchAlgorithmException e)
		{
			System.err.println("Couldn't decrypt by AES!");
			decrypted = encrypted;
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
			decrypted = encrypted;
		}
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
			decrypted = encrypted;
		}
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
			decrypted = encrypted;
		}
		catch (BadPaddingException e)
		{
			e.printStackTrace();
			decrypted = encrypted;
		}

        return new String(decrypted);
	}
}
