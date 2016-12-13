package client.encry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.res.ClientConst;
import client.util.ClientUtils;

/**
 * ������ Ű��ȯ�� �̷������ Ŭ����
 * 
 * @author �����
 *
 */
public class KeyExchangeClient {

	private BufferedInputStream bis;
	private BufferedOutputStream bos;

	private int readCount;
	private int dataSize;
	private int bodyLength;
	// ������ ���� ���� DES256 Ű
	private String desKey = null;
	private String cipherKey;

	private byte[] pubKey;
	private byte[] privKey;
	private byte[] receiveKey;
	private byte[] header;

	public KeyExchangeClient(BufferedInputStream bis, BufferedOutputStream bos) {
		this.bis = bis;
		this.bos = bos;
	}

	public void initialize() {
		try {
			RSAKeyGen encKey = new RSAKeyGen();
			pubKey = encKey.getPublicKey();
			privKey = encKey.getPrivateKey();
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			// RSA Ű������ ����
		}
	}

	/**
	 * 
	 * @param index
	 *            �ε��� 1 : �������� RSA ����Ű �����Ҷ� 
	 *            �ε��� 2 : Ű ��ȯ�� �̷������ Hello World �׽�Ʈ
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private void sendToServer(int index) throws IOException {
		String msgEncryString = null;

		if (index == 1) {
			msgEncryString = ClientUtils.makeJSONMessageForEncry(EncryUtils.byteArrayToHex(pubKey), new JSONObject(),
					new JSONObject());
		}
		// Ű ��ȯ�� �̷������ ������ȣȭ �׽�Ʈ
		else if (index == 2) {
			msgEncryString = ClientUtils.makeJSONMessageForEncry(AESUtils.AES_Encode("Hello World", desKey),
					new JSONObject(), new JSONObject());
		}

		byte[] msgEncryByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgEncryString.getBytes(ClientConst.CHARSET).length],
				msgEncryString);
		bos.write(msgEncryByte);
		bos.flush();

	}

	/**
	 * 
	 * @param index
	 *            �ε��� 1 : �����κ��� RSA ����Ű�� ��ȣȭ�� DES256 ���Ű�� ������ 
	 *            �ε��� 2 : �����κ��� Hello World �׽�Ʈ�� ���� ���������� ������
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	private void receiveForServer(int index) throws IOException {
		readCount = bis.read(header);
		dataSize = ClientUtils.byteToInt(header);
		byte[] body = new byte[dataSize];
		bodyLength = bis.read(body);

		if (index == 1) {
			cipherKey = ClientUtils.parseEncryMessage(new JSONParser(), new String(body, ClientConst.CHARSET));
		} else if (index == 2) {
			String msg = ClientUtils.parseEncryMessage(new JSONParser(), new String(body, ClientConst.CHARSET));
			//���� �����Ϳ� ���� �����Ͱ� �ٸ��ٸ� Ű��ȯ�� ������ �ִٰ� �Ǵ��ϴ�
			//������ �ʿ��ϴ�.
		}

	}

	private void decryptionDes256Key() {
		try {
			receiveKey = EncryUtils.hexToByteArray(cipherKey);
			RSADecryption rd = new RSADecryption(receiveKey, privKey);
			desKey = rd.getDESkey();
			System.out.println("AESŰ : "+desKey);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchProviderException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String start() {
		try {
			// ��� ����
			header = new byte[ClientConst.HEADER_LENTH];
			// Ű ������ �ʱ�ȭ
			initialize();
			// ������ RSA ����Ű ����
			sendToServer(1);
			// ������ ���� RSA ����Ű�� ��ȣȭ�� DES256 ���Ű ����
			receiveForServer(1);
			// ��ȣȭ�� DES256 ���Ű ��ȣȭ
			decryptionDes256Key();
			// Hello World ��ȣ�� ����
			sendToServer(2);
			// Hello World ��ȣ�� ���� �ޱ�
			receiveForServer(2);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			// desŰ ���� ����
		}
		return desKey;
	}

}
