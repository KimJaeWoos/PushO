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
	
	// ���� �� �ش� ��������� �����ϴ� �Ŵ���Ŭ���� �ν��Ͻ� ȹ��
	private SocketConnectionManager conManagerager = SocketConnectionManager.getInstance();
	// ������ ���� ���Ͻ� Ŭ������ �ν��Ͻ� ȹ��
	private AuthClientHandler authHandler = AuthClientHandler.getInstance();

	private ServerSocket serverSocket;
	private Socket socket;

	public OIOServer() {
		try {
			serverSocket = new ServerSocket(ServerConst.PORT_NUM);
			System.out.println("��������...");

			authHandler.start();
			System.out.println("�ڵ鷯����...");
			
			while (true) {
				System.out.println("Ŭ���̾�Ʈ ���� ���");
				// ���ŷ ����
				socket = serverSocket.accept();
				System.out.println("������ ���� ����");
				try {
					ServerConst.SOCKET_QUEUE.put(socket);
					System.out.println("���ŷť put : "+socket.getClass().getName());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				authHandler.authClientAndDelegate(socket);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�������� ���� : " + e.getMessage());
		} finally {
			try {
				ServerConst.SOCKET_QUEUE.clear();
				socket.close();
				serverSocket.close();
				conManagerager.closeAll();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
