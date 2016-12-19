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
			ServerConst.ACCESS_LOGGER.debug("Server Start, port:{}",ServerConst.PORT_NUM);

			new AuthClientHandler(socketQueue).start();
			ServerConst.ACCESS_LOGGER.debug("AuthHandler Start");
			
			while (true) {
				ServerConst.ACCESS_LOGGER.debug("Wating Client Request..");
				// ���ŷ ����
				socket = serverSocket.accept();
				ServerConst.ACCESS_LOGGER.debug("Client Access!");
				try {
					this.socketQueue.put(socket);
					ServerConst.ACCESS_LOGGER.info("Put Element into BlockingQueue for Authorization, Blocking Queue Size : {}",this.socketQueue.size());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ServerConst.ACCESS_LOGGER.error(e.getMessage());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ServerConst.ACCESS_LOGGER.error(e.getMessage());
		} finally {
			try {
				
				this.socketQueue.clear();
				socket.close();
				serverSocket.close();
				conManagerager.closeAll();
				ServerConst.ACCESS_LOGGER.debug("Close All Resource");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ServerConst.ACCESS_LOGGER.error(e.getMessage());
			}
		}
	}
}
