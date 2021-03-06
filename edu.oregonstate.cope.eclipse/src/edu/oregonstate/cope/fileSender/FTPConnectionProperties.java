package edu.oregonstate.cope.fileSender;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.PropertyResourceBundle;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.oregonstate.cope.eclipse.COPEPlugin;


public class FTPConnectionProperties extends AbstractUIPlugin {

	private static final char[] sk = { 't', 'h', 'i', 's', ' ', 'i', 's', ' ',
			'a', ' ', 's', 't', 'r', 'i', 'n', 'g', '.', ' ', 'r', 'e', 's', 'p',
			'e', 'c', 't', ' ', 'i', 't', '!' };

	private static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x9,
			(byte) 0x12, (byte) 0xda, (byte) 0x34, (byte) 0x8, (byte) 0x42, };

	protected final static String PROPERTIES_PATH = "resources" + File.separator + "ftp.properties";
	
	protected static PropertyResourceBundle ftpProperties;
		
	private static FTPConnectionProperties ftpConnectionProperties = null;
	
	public static FTPConnectionProperties getInstance() {
		if(ftpConnectionProperties == null) {
			ftpConnectionProperties = new FTPConnectionProperties();
		}
		return ftpConnectionProperties;
	}
	public static PropertyResourceBundle getProperties() {
		if (ftpProperties == null) {
			try {
				ftpProperties = new PropertyResourceBundle(
					FileLocator.openStream(
						FTPConnectionProperties.getInstance().getBundle(), new Path(PROPERTIES_PATH), false
					));
			} catch (IOException e) {
				COPEPlugin.getDefault().getLogger().error(FTPConnectionProperties.class, e.getMessage(), e);
			}
		}
		return ftpProperties;
	}
	
	public static String getHost() {
		return FTPConnectionProperties.getProperties().getString("host");
	}

	public static String getUsername() {
		return FTPConnectionProperties.getProperties().getString("username");
	}

	public static String getCronConfiguration() {
		return getFrequency();
	}

	public static String getFrequency() {
		return FTPConnectionProperties.getProperties().getString("frequency");
	}

	public static String getPassword() throws GeneralSecurityException,
			IOException {
		String encodedPassword = FTPConnectionProperties.getProperties().getString("password");
		return decrypt(encodedPassword);
	}

	private static String encrypt(String property)
			throws GeneralSecurityException, UnsupportedEncodingException {
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(sk));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
	}

	private static String decrypt(String property)
			throws GeneralSecurityException, IOException {
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(sk));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
	}

	private static String base64Encode(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}

	private static byte[] base64Decode(String property) throws IOException {
		return Base64.decodeBase64(property);
	}
}
