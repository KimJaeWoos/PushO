package server;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import exception.EmptyResultDataException;
import res.Const;

/**
 * @author 		�ֺ�ö
 * @Description	���� ���α׷�, OIO����� Socket���, ������ Controller Ŭ����
 * 				�ټ��� Ŭ���̾�Ʈ���� ������ �����ϴ� ������� ArrayList<Socket>�� �����
 * 				Socket���� �� ���� ����� ������ �̱��� �������� ������ {@link AuthClientProxy}�� ���
 * 				������ �������� ��� {@link EmptyResultDataException}�� ���� ���� ����
 * 				������ ���� �Ŀ��� ������ �� Ŭ���̾�Ʈ�� ����ϴ� {@link ProcessCilentRequest}�� ����
 * TODO			��Ƽ�����带 ���� �ټ��� Ŭ���̾�Ʈ ���� ����(Thread pooling)
 * 				{@link AuthClientProxy}�� �̱����̾��� ��� �����߻� ���� ���
 * 				Ŭ���̾�Ʈ�� ������ �Ǿ��� ���� �ʱ�ȭ �۾�(����, ��ȣȭ ��)
 * 				Ÿ�Ӿƿ��� �߻��Ͽ��� ��� �ڿ����� ��Ŀ����
 */
public class OIOServer {
	
	public static void main(String[] args) {
		new OIOServer();
	}
	private ServerSocket serverSocket;
	private Socket socket;
	
	ArrayList<Socket> socketList = new ArrayList<Socket>();

	public OIOServer() {
		try {
			
			serverSocket = new ServerSocket(Const.PORT_NUM);
			System.out.println("��������...");
			
			//������ ���� ���Ͻ� Ŭ������ �ν��Ͻ� ȹ��
			AuthClientProxy authProxy = AuthClientProxy.getInstance();
			while (true) {
				//���ŷ ����
				socket = serverSocket.accept();
				//��Ʈ���� ���� Ÿ�Ӿƿ� ����
				socket.setSoTimeout(Const.STREAM_TIME_OUT);
				try {
					//������ ����(DB��ȸ) �� �����Ѵٸ� Ŭ���̾�Ʈ ��ûó�� ������ ����
					ProcessCilentRequest thread = authProxy.getClientSocketThread(socket);
					thread.start();
					//����Ʈ�� ����
					socketList.add(socket);
					System.out.println("Client ����ó�� ������ : " + thread.getId() + " , " + thread.getName());
				} catch (EmptyResultDataException e) {
					// TODO ������ ���� ���� ������� ��� ó�� ����
					e.printStackTrace();
					socket.close();
					socketList.remove(socket);
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
				socketList = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
