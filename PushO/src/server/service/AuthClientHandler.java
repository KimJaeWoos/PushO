package server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

import org.json.simple.parser.JSONParser;

import server.dao.JDBCTemplate;
import server.encry.AESUtils;
import server.encry.KeyExchangeServer;
import server.exception.AlreadyConnectedSocketException;
import server.exception.EmptyResultDataException;
import server.exception.PasswordAuthFailException;
import server.model.UserAuth;
import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author �ֺ�ö
 * @Description ������ ���� ���Ͻ� Ŭ������ �̱������� ���� ��
 * @TODO �̱������� ������ ��Ƽ������ ȯ�濡���� ���ü� ���� ���� ������ ���� DB����� Blocking �ð� ���
 */
public class AuthClientHandler extends Thread {
	private SocketConnectionManager socketConnectionManagerager = SocketConnectionManager.getInstance();

	/*
	 * private static AuthClientHandler instance = null;
	 * 
	 * public static AuthClientHandler getInstance() { if (instance == null) {
	 * instance = new AuthClientHandler();
	 * ServerConst.SERVER_LOGGER.debug("�ڵ鷯 ����"); } return instance; }
	 */

	public ArrayBlockingQueue<Socket> socketQueue;

	private UserAuth userInfo;
	
	private String userPasswd;

	public AuthClientHandler(ArrayBlockingQueue<Socket> socketQueue) {
		this.socketQueue = socketQueue;
		ServerConst.SERVER_LOGGER.debug("�ڵ鷯 ����");
	}

	@Override
	public void run() {
		ServerConst.SERVER_LOGGER.debug("�ڵ鷯 ������ ����");
		while (!this.isInterrupted()) {
			try {
				Socket socket = this.socketQueue.take();
				ServerConst.SERVER_LOGGER.info("���� ���ŷ ť ���� �۾� ������, ť ũ�� : {}", this.socketQueue.size());
				String aesKey = encryptionKeyChange(socket);
				ServerConst.SERVER_LOGGER.info(" Ű ��ȯ�۾� �Ϸ� [{}]", socket.getInetAddress().getHostName());
				ServerConst.SERVER_LOGGER.debug("��ȣȭ Ű : {} ", aesKey);
				authClientAndDelegate(socket, aesKey);
				ServerConst.SERVER_LOGGER.debug("�����Ϸ�");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ServerConst.SERVER_LOGGER.error(e.getMessage());
			}
		}
	}

	private String encryptionKeyChange(Socket socket) {
		KeyExchangeServer kes = new KeyExchangeServer(socket);
		String key = kes.start();
		return key;
	}

	/**
	 * ������ ������ �����ϴ� �޼ҵ�
	 * 
	 * @param socket
	 *            ���� �޽����� ���� Stream�� ���� ������ socket
	 * @return Ŭ���̾�Ʈ ��ûó�� ������
	 * @throws EmptyResultDataException
	 *             ��ϵ� ����ڰ� �ƴ�(����X)
	 */
	public synchronized void authClientAndDelegate(Socket socket, String aesKey) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			byte[] buf = new byte[ServerConst.HEADER_LENTH];
			int readCount = 0;
			int length = 0;
			int bodylength = 0;
			ServerConst.SERVER_LOGGER.debug("��Ʈ�� �б� ����");

			readCount = bis.read(buf);
			length = ServerUtils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			String msg = new String(body, ServerConst.CHARSET);
			msg = AESUtils.AES_Decode(msg, aesKey);
			ServerConst.SERVER_LOGGER.info("���� �޽��� : {}", msg);

			if (msg.contains(ServerConst.JSON_VALUE_AUTH)) {
				// userInfo[0]�� ����� id, userInfo[1]�� ����� ��й�ȣ ��ȯ��
				String[] userInfo = ServerUtils.parseJSONMessage(new JSONParser(), msg).split("/");
				boolean authorized = false;
				try {
					// ���� ���� �� ���ܰ� �߻��Ǵ� �κ�
					checkAuthorization(userInfo[0]);
					checkPassword(userInfo[1]);
					authorized = true;
					ServerConst.SERVER_LOGGER.debug("DB��� �Ǿ�����");
					if (authorized) {
						//// �Ŵ����� �߰����ִ� �κ�.
						ServerConst.SERVER_LOGGER.debug("�Ŵ����� ���� ����");
						socketConnectionManagerager.addClientSocket(userInfo[0], socket, aesKey);
						ServerConst.SERVER_LOGGER.debug("�Ŵ����� ���� ��");
					}
				} catch (EmptyResultDataException e) {
					// TODO Ŭ���̾�Ʈ���� ��ϵ��� �ʾҴٴ� �޽����� ����
					// bos.write(b);
					ServerConst.SERVER_LOGGER.error("��ϵ��� ���� ����� {}", e.getMessage());
					socket.close();
					e.printStackTrace();
				} catch (AlreadyConnectedSocketException e) {
					// TODO Ŭ���̾�Ʈ���� �̹� ����� ���̵��� �޽����� ����
					// bos.write(b);
					ServerConst.SERVER_LOGGER.error("�̹� ����� ����� {} ", e.getMessage());
					socket.close();
					e.printStackTrace();
				} catch (PasswordAuthFailException e) {
					// TODO Ŭ���̾�Ʈ���� ��й�ȣ�� Ʋ�ȴٴ� �޽����� ����
					// bos.write(b);
					ServerConst.SERVER_LOGGER.error("�̹� ����� ����� {} ", e.getMessage());
					socket.close();
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
			ServerConst.SERVER_LOGGER.error(e.getMessage());
			try {
				bis.close();
			} catch (IOException closeE) {
				closeE.printStackTrace();
				ServerConst.SERVER_LOGGER.error(e.getMessage());
			}
		}
	}

	/**
	 * {@link JDBCTemplate}�� Ȱ���� ����� ����
	 * 
	 * @param id
	 *            ������ ���� ����� �̸�
	 * @throws EmptyResultDataException
	 *             ������ �ȵǾ��� ��� �߻�
	 */
	private void checkAuthorization(String id) throws EmptyResultDataException {
		userInfo = new JDBCTemplate().executeQuery("select mem_id, mem_pwd, mem_salt from pj_member where mem_id = ?",
				new SetPrepareStatement() {
					@Override
					public void setFields(PreparedStatement pstm) throws SQLException {
						ServerConst.SERVER_LOGGER.debug("DB����");
						pstm.setString(1, id);
					}
				});
	}
	
	/**
	 * ��ϵ� ��������� Ȯ���� ���۹��� ��й�ȣ�� salt�� �߰��� �� �ؽ̰��� ��´�.
	 * �׸��� DB�� ��ϵ� ���� ���ϴ� ������ ��ģ��.
	 * @throws PasswordAuthFailException
	 */
	private void checkPassword(String userPwd) throws PasswordAuthFailException {
		userPasswd = ServerUtils.getEncryptValue(userInfo.getPasswd() + userInfo.getPasswd_salt());
		
		if (userPwd == userPasswd) {
			//���� �Ϸ�
		} else {
			throw new PasswordAuthFailException("��й�ȣ�� ��ġ���� ����");
		}
	}
	
	
}
