package server.encry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * ������ Ű��ȯ�� �̷������ Ŭ����
 * 
 * @author �����
 *
 */
public class KeyExchangeServer {

	private BufferedInputStream bis;
	private BufferedOutputStream bos;

	private int readCount;
	private int dataSize;
	private int bodyLength;

	private byte[] receiveKey;
	private byte[] header;

	private String cipherMsg;
	private String aesKey;
	private String hostAddress;

	public KeyExchangeServer(Socket socket) {
		try {
			this.bis = new BufferedInputStream(socket.getInputStream());
			this.bos = new BufferedOutputStream(socket.getOutputStream());
			this.hostAddress = socket.getInetAddress().getHostAddress();
		} catch (IOException e) {
			e.getStackTrace();
			ServerConst.ACCESS_LOGGER.error(e.getMessage());
		}
	}

	/**
	 * �����κ��� �����͸� �޴� �޼ҵ�
	 * 
	 * @param index
	 *            �ε��� 1 : Ŭ���̾�Ʈ�κ��� RSA ����Ű�� ������ / �ε��� 2 : Ŭ���̾�Ʈ�� Ű ��ȯ�� �̷������
	 *            ��ȣŰ�� ����� ������ ��ȣȭ�� �Ǵ��� Ȯ���Ҷ�
	 * @throws IOException
	 */
	private void receiveForServer(int index) throws IOException {
		readCount = bis.read(header);
		dataSize = ServerUtils.byteToInt(header);
		byte[] body = new byte[dataSize];
		bodyLength = bis.read(body);
		String msg = ServerUtils.parseEncryMessage(new JSONParser(), new String(body, ServerConst.CHARSET));

		if (index == 1) {
			receiveKey = EncryUtils.hexToByteArray(msg);
		} else if (index == 2) {
			cipherMsg = AESUtils.AES_Decode(msg, aesKey);
		}
	}

	/**
	 * Ŭ���̾�Ʈ�κ��� RSA ����Ű�� ������ ������ ���� DES256 ��ȣȭŰ�� RSA����Ű�� ��ȣȭ �� �Ŀ� ������ ������
	 * 
	 * @throws IOException
	 */
	private void sendEncryDES256KeyToClient() throws IOException {
		aesKey = Hex.encodeHexString(EncryUtils.get128bitKey().getEncoded());
		RSAEncryption ec = new RSAEncryption(receiveKey, aesKey);
		ServerConst.ACCESS_LOGGER.debug("AES Key Generating Start");
		ServerConst.ACCESS_LOGGER.info("Received AESkey[{}] from [{}] ",aesKey, hostAddress);
		String msgEncryString = ServerUtils.makeJSONMessageForEncry(EncryUtils.byteArrayToHex(ec.getkey()),
				new JSONObject(), new JSONObject());
		byte[] msgEncryByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + msgEncryString.getBytes(ServerConst.CHARSET).length],
				msgEncryString);
		bos.write(msgEncryByte);
		bos.flush();
	}

	/*
	 * Ŭ���̾�Ʈ�� ��ȣȭŰ�� ��ȯ�� �� Ŭ���̾�Ʈ���� Hello World�� ��ȣȭ�ؼ� ������ ������ �������� �Ϻ�ȣȭ�� �����
	 * �̷�������� Ȯ���� �ٽ� ���������� Ŭ���̾�Ʈ���� ���� ������ ��ȣȭ�ؼ� ������.
	 */
	private void sendToClient() throws IOException {
		cipherMsg = AESUtils.AES_Encode(cipherMsg, aesKey);
		String msgEncryString = ServerUtils.makeJSONMessageForEncry(cipherMsg, new JSONObject(), new JSONObject());
		byte[] msgEncryByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + msgEncryString.getBytes(ServerConst.CHARSET).length],
				msgEncryString);
		bos.write(msgEncryByte);
		bos.flush();
		ServerConst.ACCESS_LOGGER.info("Send Encryption data [{}] to [{}]", aesKey, hostAddress);
	}

	public String start() {
		try {
			header = new byte[ServerConst.HEADER_LENTH];
			// ������ ���� RSA ����Ű�� �޴´�
			receiveForServer(1);
			ServerConst.ACCESS_LOGGER.debug("Received RSAKey from [{}]", hostAddress);
			// ����Ű�� ��ȣȭ�� DES256 Ű�� ������
			sendEncryDES256KeyToClient();
			ServerConst.ACCESS_LOGGER.debug("Send Encrypted AESKey by Reecived RSAKey to [{}]", hostAddress);
			// ��ȣȭ�� Hello World ����
			receiveForServer(2);
			ServerConst.ACCESS_LOGGER.debug("Receive Encrypted data 'Hello World' for test from [{}]", hostAddress);
			// Hello World ��ȣȭ�� �ٽ� ��ȣȭ�ؼ� ����
			sendToClient();
			ServerConst.ACCESS_LOGGER.debug("Send Encrypted data 'Hello World' for test to [{}]", hostAddress);

		} catch (Exception e) {
			e.getStackTrace();
			ServerConst.ACCESS_LOGGER.error(e.getMessage());
		}

		return aesKey;
	}
}
