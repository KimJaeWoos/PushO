package server.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import server.exception.EmptyResultDataException;
import server.res.ServerConst;

/**
 * @author �ֺ�ö
 * @Description ���� ���α׷�, OIO����� Socket���, ������ Controller Ŭ���� �ټ��� Ŭ���̾�Ʈ���� ������
 *              �����ϴ� ������� ArrayList<Socket>�� ����� Socket���� �� ���� ����� ������ �̱��� ��������
 *              ������ {@link AuthClientHandler}�� ��� ������ �������� ���
 *              {@link EmptyResultDataException}�� ���� ���� ���� ������ ���� �Ŀ��� ������ ��
 *              Ŭ���̾�Ʈ�� ����ϴ� {@link ProcessCilentRequest}�� ����
 * @TODO ��Ƽ�����带 ���� �ټ��� Ŭ���̾�Ʈ ���� ����(Thread pooling) {@link AuthClientHandler}��
 *       �̱����̾��� ��� �����߻� ���� ��� Ŭ���̾�Ʈ�� ������ �Ǿ��� ���� �ʱ�ȭ �۾�(����, ��ȣȭ ��) Ÿ�Ӿƿ��� �߻��Ͽ��� ���
 *       �ڿ����� ��Ŀ����
 */
public class OIOServer {

	public static void main(String[] args) {
		new OIOServer();
	}
	
	public ArrayBlockingQueue<Socket> socketQueue = 
			new ArrayBlockingQueue<Socket>(ServerConst.SOCKET_QUEUE_SIZE);
	// ���� �� �ش� ��������� �����ϴ� �Ŵ���Ŭ���� �ν��Ͻ� ȹ��
	private SocketConnectionManager conManagerager = SocketConnectionManager.getInstance();
	// ������ ���� ���Ͻ� Ŭ������ �ν��Ͻ� ȹ��
//	private AuthClientHandler authHandler = AuthClientHandler.getInstance();

	private ServerSocket serverSocket;
	private Socket socket;
	

	public OIOServer() {
		try {
			serverSocket = new ServerSocket(ServerConst.PORT_NUM);
			ServerConst.SERVER_LOGGER.debug("��������");

			new AuthClientHandler(socketQueue).start();
			ServerConst.SERVER_LOGGER.debug("�ڵ鷯����");
			
			while (true) {
				ServerConst.SERVER_LOGGER.debug("Ŭ���̾�Ʈ ���ӿ�û ���");
				// ���ŷ ����
				socket = serverSocket.accept();
				ServerConst.SERVER_LOGGER.debug("Ŭ���̾�Ʈ ���ӿϷ�");
				try {
					this.socketQueue.put(socket);
					ServerConst.SERVER_LOGGER.info("���ŷť�� ����, ť ũ�� : "+this.socketQueue.size());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ServerConst.SERVER_LOGGER.error(e.getMessage());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ServerConst.SERVER_LOGGER.error(e.getMessage());
		} finally {
			try {
				
				this.socketQueue.clear();
				socket.close();
				serverSocket.close();
				conManagerager.closeAll();
				ServerConst.SERVER_LOGGER.debug("����ڿ� ����");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ServerConst.SERVER_LOGGER.error(e.getMessage());
			}
		}
	}
}
