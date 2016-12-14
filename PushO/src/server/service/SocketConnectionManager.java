package server.service;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import server.model.UserAuth;
import server.observer.DBThread;

/**
 * @author �ֺ�ö
 * @Description {@link OIOServer}������ accept()�� ���� ���ῡ ���� ó������ �ϰ�
 *              {@link AuthClientHandler}���� ������ ��ģ �Ŀ�
 *              {@link ProcessCilentRequest}�� �����Ͽ� �ش� �����带 �����ϱ� ���� ThreadPool��
 *              Ŭ���̾�Ʈ���� �����ϴ� �ڷᱸ���� ���� {@link Pushable}�� �����Ͽ� Ư�� �Ǹ��ڿ��Ը� ������ �޼ҵ��
 *              ��ο��� ������ �޼ҵ带 ���� 
 * @TODO Ŭ���̾�Ʈ�� �����ϱ� ���� �ڷᱸ��(HashMap->
 *      				Collection.syncronized() Wrapping->ConcurrentHashMap) 
 *      Thread pooling {@link Pushable} ����
 */
public class SocketConnectionManager implements Pushable {
	// �Ŵ��� ��ü�� �̱���
	private static SocketConnectionManager instance = null;

	public static SocketConnectionManager getInstance() {
		if (instance == null) {
			instance = new SocketConnectionManager();
		}
		return instance;
	}

	// Ŭ���̾�Ʈ ó�� �����带 Map���� ����
	private Map<String, ProcessCilentRequest> conMap = new HashMap<String, ProcessCilentRequest>();
	// ������Ǯ �����κ�
	private ExecutorService executorService = Executors.newCachedThreadPool();

	Iterator<String> keySetIterator;
	
	private DBThread dbThread;

	public SocketConnectionManager() {
		dbThread = new DBThread(this);
		dbThread.start();
		System.out.println("DB���� ����...");
	}

	@Override
	public void sendPushAll(String msg) {
		keySetIterator = conMap.keySet().iterator();
		while (keySetIterator.hasNext()) {
			String userID = keySetIterator.next();
			// thread�� setPush
			conMap.get(userID).setPush(msg);
		}
	}

	@Override
	public void sendPushPartial(String id, String msg) {
		
	}
/*
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (!this.isInterrupted()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	/**
	 * ������Ǯ�� Map�� �߰���Ű�� �޼ҵ� ����Ÿ������ Future ��ü�� ��������� ���� ���� ���
	 * 
	 * @param name
	 *            Ŭ���̾�Ʈ ���̵�
	 * @param clientSocket
	 *            Ŭ���̾�Ʈ�� ����� ����
	 */
	public synchronized void add(String name, Socket clientSocket, String aesKey) {
		boolean duplicated = false;
		//�̹� ��ϵ� ��������� �˻�
		if (conMap.containsKey(name)) {
			duplicated = true;
		}
		if (!duplicated) {
			ProcessCilentRequest proClient = new ProcessCilentRequest(clientSocket, aesKey);
			this.executorService.submit(proClient);
			System.out.println("���� ���� Ŭ���̾�Ʈ�� �̸� : "+name);
			conMap.put(name, proClient);
			System.out.println("����� Ŭ���̾�Ʈ �� : "+conMap.size());
		} else {
			// TODO : ������ ���� �ʾҴٴ� �޽����� ������ ������ ����.
			System.out.println("�̹� �����ϴ� �����");
			try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized void closeAll() {
		dbThread.interrupt();
		try {
			dbThread.db.closeDBSet();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		executorService.shutdown();
//		this.interrupt();
		conMap = null;
	}

}
