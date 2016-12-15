package server.service;

import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import server.exception.AlreadyConnectedSocketException;
import server.exception.PushMessageSendingException;
import server.model.PushInfo;
import server.observer.DBThread;
import server.res.ServerConst;

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
			ServerConst.SERVER_LOGGER.debug("�Ŵ��� ����");
		}
		return instance;
	}

	// Ŭ���̾�Ʈ ó�� �����带 Map���� ����
//	private Map<String, ProcessCilentRequest> conMap = new HashMap<String, ProcessCilentRequest>();
	private ConcurrentHashMap<String, ProcessCilentRequest> concurrentHashMap = 
									new ConcurrentHashMap<String, ProcessCilentRequest>();
	// ������Ǯ �����κ�
	private ExecutorService executorService = Executors.newCachedThreadPool();
	
	private DBThread dbThread;
	
	public LinkedBlockingQueue<String> receivedAckQueue = 
			new LinkedBlockingQueue<String>(ServerConst.RECEIVED_ACK_QUEUE_SIZE);
	
	private SocketConnectionManager() {
		dbThread = new DBThread(this, receivedAckQueue);
		ServerConst.SERVER_LOGGER.debug("DB������ ����");
		dbThread.start();
		ServerConst.SERVER_LOGGER.debug("DB������ ����");
	}

	@Override
	public synchronized void sendPushAll(String msg) {
		Iterator<String> keySetIterator = concurrentHashMap.keySet().iterator();
		// ����� ����ڿ��� ���������� ����ϱ����� �߰�
		int size = 0;
		int sizeTotal = 0;
		// �� ����� üũ
		while (keySetIterator.hasNext()) {
			sizeTotal++;
		}
		
		ServerConst.SERVER_LOGGER.debug("��� ����ڿ��� Push�޽��� ���� ����");
		while (keySetIterator.hasNext()) {
			size++;
			String userID = keySetIterator.next();
			ServerConst.SERVER_LOGGER.debug("��� ����ڿ��� ������" + "(" + size + "/" + sizeTotal + ")");
			sendPushPartial(userID,msg);
		}
		ServerConst.SERVER_LOGGER.debug("��� ����ڿ��� Push�޽��� ���� ��");
	}
	
	@Override
	public void sendPushPartial(String Id, String msg) {
		ServerConst.SERVER_LOGGER.debug( Id+", ����ڿ��� Push�޽��� ���� ����");
		try {
			concurrentHashMap.get(Id).setPush(msg);
		} catch (PushMessageSendingException e) {
			concurrentHashMap.remove(Id);
			e.printStackTrace();
			ServerConst.SERVER_LOGGER.error(e.getMessage()+", ����� "+Id+"�� �ʿ��� ����");
		}
		ServerConst.SERVER_LOGGER.debug( Id+", ����ڿ��� Push�޽��� ���� ��");
	}

	/**
	 * ������Ǯ�� Map�� �߰���Ű�� �޼ҵ� ����Ÿ������ Future ��ü�� ��������� ���� ���� ���
	 * 
	 * @param name
	 *            Ŭ���̾�Ʈ ���̵�
	 * @param clientSocket
	 *            Ŭ���̾�Ʈ�� ����� ����
	 */
	public synchronized void addClientSocket(String name, Socket clientSocket, String aesKey)
			throws AlreadyConnectedSocketException{

		boolean duplicated = false;
		//�̹� ��ϵ� ��������� �˻�
		if (concurrentHashMap.containsKey(name)) {
			duplicated = true;
			ServerConst.SERVER_LOGGER.debug(name+"�� �̹� �ʿ� ��ϵ�");
		}
		//�ߺ��� �ƴ϶�� �����带 �����ϰ� Map�� ����
		if (!duplicated) {
			ProcessCilentRequest proClient = new ProcessCilentRequest(clientSocket, aesKey, receivedAckQueue);
			this.executorService.submit(proClient);
			ServerConst.SERVER_LOGGER.info(proClient+"������ ����, Ŭ���̾�Ʈ �̸� : "+name);
			concurrentHashMap.put(name, proClient);
			ServerConst.SERVER_LOGGER.info("����� Ŭ���̾�Ʈ �� : "+concurrentHashMap.size());
		} else {
			// TODO : ������ ���� �ʾҴٴ� �޽����� ������ ������ �ݴ� ���� ����
			throw new AlreadyConnectedSocketException(name+"�� �̹� ����");
		}
	}

	public synchronized void closeAll() {
		ServerConst.SERVER_LOGGER.debug("�Ŵ����� �ڿ����� ����");
		dbThread.interrupt();
		executorService.shutdown();
		concurrentHashMap.clear();
		ServerConst.SERVER_LOGGER.debug("�Ŵ����� �ڿ����� ��");
	}


}
