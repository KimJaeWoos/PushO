package server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

import org.json.simple.parser.JSONParser;

import server.dao.JDBCTemplate;
import server.encry.AESUtils;
import server.encry.KeyExchangeServer;
import server.exception.AlreadyConnectedSocketException;
import server.exception.EmptyResultDataException;
import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author �ֺ�ö
 * @Description ������ ���� ���Ͻ� Ŭ������ �̱������� ���� �� 
 * @TODO �̱������� ������ ��Ƽ������ ȯ�濡���� ���ü� ���� ����
 *              ������ ���� DB����� Blocking �ð� ���
 */
public class AuthClientHandler extends Thread {
	private SocketConnectionManager socketConnectionManagerager = SocketConnectionManager.getInstance();
	
	/*
	private static AuthClientHandler instance = null;
	
	public static AuthClientHandler getInstance() {
		if (instance == null) {
			instance = new AuthClientHandler();
			ServerConst.SERVER_LOGGER.debug("�ڵ鷯 ����");
		}
		return instance;
	}*/
	
	public ArrayBlockingQueue<Socket> socketQueue;
	
	public AuthClientHandler(ArrayBlockingQueue<Socket> socketQueue) {
		this.socketQueue = socketQueue;
		ServerConst.ACCESS_LOGGER.debug("AuthHandler Created!");
	}

	@Override
	public void run() {
		ServerConst.ACCESS_LOGGER.debug("AuthHandler Thread Start!");
		while(!this.isInterrupted()){
			try {
				Socket socket = this.socketQueue.take();
				ServerConst.ACCESS_LOGGER.info("Get Element from BlockingQueue for Authorization, BlockingQueue Size : {}",this.socketQueue.size());
				String aesKey = encryptionKeyChange(socket);
				ServerConst.ACCESS_LOGGER.info("Complete Key Exchage with [{}]",socket.getInetAddress().getHostName());
				ServerConst.ACCESS_LOGGER.info("Encryption Key : [{}] ", aesKey);
				authClientAndDelegate(socket, aesKey);
				ServerConst.ACCESS_LOGGER.debug("Complete Authorization!");
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ServerConst.ACCESS_LOGGER.error(e.getMessage());
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
	public synchronized void authClientAndDelegate(Socket socket, String aesKey){
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			byte[] buf = new byte[ServerConst.HEADER_LENTH];
			int readCount = 0;
			int length = 0;
			int bodylength = 0;

			readCount = bis.read(buf);
			length = ServerUtils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			String msg = new String(body, ServerConst.CHARSET);
			msg = AESUtils.AES_Decode(msg, aesKey);
			ServerConst.ACCESS_LOGGER.info("Received Message from Client : [{}]",msg);

			if (msg.contains(ServerConst.JSON_VALUE_AUTH)) {
				String name = ServerUtils.parseJSONMessage(new JSONParser(), msg);
				boolean authorized = false;
				try{
					//���� ���� �� ���ܰ� �߻��Ǵ� �κ�
					checkAuthorization(name);
					authorized = true;
					ServerConst.ACCESS_LOGGER.info("Client [{}] in Database",name);
					if(authorized){
						////�Ŵ����� �߰����ִ� �κ�.
						ServerConst.ACCESS_LOGGER.info("Delegate to Manager, Name:[{}], AESKey:[{}]", name, aesKey);
						socketConnectionManagerager.addClientSocket(name, socket, aesKey);
					}
				} catch(EmptyResultDataException e){
					//TODO Ŭ���̾�Ʈ���� �������� �ʾҴٴ� �޽����� ����
//					bos.write(b);
					ServerConst.ACCESS_LOGGER.error("Not Registered Client ERROR : {}",e.getMessage());
					socket.close();
					e.printStackTrace();
				} catch(AlreadyConnectedSocketException e){ 
					//TODO Ŭ���̾�Ʈ���� �̹� ����� ���̵��� �޽����� ����
//					bos.write(b);
					ServerConst.ACCESS_LOGGER.error("Aleady Connected Client ERROR : {} ",e.getMessage());
					socket.close();
					e.printStackTrace();
				}
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			ServerConst.ACCESS_LOGGER.error(e.getMessage());
			try {
				bis.close();
			} catch (IOException closeE) {
				closeE.printStackTrace();
				ServerConst.ACCESS_LOGGER.error(e.getMessage());
			}
		}
	}

	/**
	 * {@link JDBCTemplate}�� Ȱ���� ����� ����
	 * 
	 * @param name
	 *            ������ ���� ����� �̸�
	 * @throws EmptyResultDataException
	 *             ������ �ȵǾ��� ��� �߻�
	 */
	private void checkAuthorization(String name) throws EmptyResultDataException {
		new JDBCTemplate().executeQuery("select * from pj_member where mem_name = ?", 
				new SetPrepareStatement() {
			@Override
			public void setFields(PreparedStatement pstm) throws SQLException {
				ServerConst.ACCESS_LOGGER.debug("Access DataBase...");
				pstm.setString(1, name);
			}
		});
	}
}
