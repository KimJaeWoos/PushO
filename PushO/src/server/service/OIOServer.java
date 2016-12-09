package server.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

import server.exception.EmptyResultDataException;
import server.observer.DBObserver;
import server.observer.DBThread;
import server.res.ServerConst;

/**
 * @author �ֺ�ö
 * @Description ���� ���α׷�, OIO����� Socket���, ������ Controller Ŭ���� �ټ��� Ŭ���̾�Ʈ���� ������
 *              �����ϴ� ������� ArrayList<Socket>�� ����� Socket���� �� ���� ����� ������ �̱���
 *              �������� ������ {@link AuthClientHandler}�� ��� ������ �������� ���
 *              {@link EmptyResultDataException}�� ���� ���� ���� ������ ���� �Ŀ��� ������ ��
 *              Ŭ���̾�Ʈ�� ����ϴ� {@link ProcessCilentRequest}�� ���� 
 * @TODO ��Ƽ�����带 ���� �ټ��� Ŭ���̾�Ʈ ���� ����(Thread pooling) {@link AuthClientHandler}��
 *       �̱����̾��� ��� �����߻� ���� ��� Ŭ���̾�Ʈ�� ������ �Ǿ��� ���� �ʱ�ȭ �۾�(����, ��ȣȭ ��) Ÿ�Ӿƿ��� 
 *       �߻��Ͽ��� ��� �ڿ����� ��Ŀ����
 */
public class OIOServer implements DBObserver {

	public static void main(String[] args) {
		new OIOServer();
	}

	private ServerSocket serverSocket;
	private Socket socket;
	private DBThread dbThread;
	private static boolean survival;

	public HashMap<String, ProcessCilentRequest> socketList = new HashMap<String, ProcessCilentRequest>();
	private Iterator<String> keySetIterator;

	private String userName;

	public OIOServer() {
		try {

			serverSocket = new ServerSocket(ServerConst.PORT_NUM);
			survival = true;
			System.out.println("��������...");

			dbThread = new DBThread(this);
			dbThread.start();
			
			//������ ���� ���Ͻ� Ŭ������ �ν��Ͻ� ȹ��
			AuthClientHandler authHandler = AuthClientHandler.getInstance();

			while (true) {
				// ���ŷ ����
				socket = serverSocket.accept();
				// ��Ʈ���� ���� Ÿ�Ӿƿ� ����
				// socket.setSoTimeout(Const.STREAM_TIME_OUT);
				try {
					// ������ ����(DB��ȸ) �� �����Ѵٸ� Ŭ���̾�Ʈ ��ûó�� ������ ����
					System.out.println("������ ���� ����");
					ProcessCilentRequest thread = authHandler.getClientSocketThread(socket, this);
					thread.start();
					// ����Ʈ�� ����
					socketList.put(userName, thread);
					System.out.println("Client ����ó�� ������ : " + thread.getId() + " , " + thread.getName());
				} catch (EmptyResultDataException e) {
					// TODO ������ ���� ���� ������� ��� ó�� ����
					e.printStackTrace();
					socket.close();
					// socketList.remove(thread);
					System.out.println("���� ����, ���� ����");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�������� ���� : " + e.getMessage());
		} finally {
			try {
				socket.close();
				serverSocket.close();
				dbThread.obserberStop();
				socketList = null;
				survival = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void msgPush(String msg) {
		// System.out.println("Ǫ�������� : " + msg);
		keySetIterator = socketList.keySet().iterator();
		while (keySetIterator.hasNext()) {
			String userID = keySetIterator.next();
			// thread�� setPush
			socketList.get(userID).setPush(msg);
		}
	}

	@Override
	public void setUser(String id) {
		userName = id;
	}

	public static boolean isSurvival() {
		return survival;
	}

}
