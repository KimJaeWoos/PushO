package server.observer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;

import server.dao.JDBCTemplate;
import server.model.PushInfo;
import server.res.ServerConst;
import server.service.AuthClientHandler;
import server.service.ProcessCilentRequest;
import server.service.Pushable;
import server.util.ServerUtils;

/**
 * 
 * @author �����
 * @Description ������ �����带 �����Ͽ� ����� �ֹ����̺��� �׻� �����ϰ� ����ڿ��� PUSH �ؾ� �� �����͸�
 *          	SocketConnectionManager�� �����ְ��ִ�.    
 */
public class DBThread extends Thread {

	private Pushable pushable;
	public JDBCTemplate db;

	private String msgPushJson;

	private List<PushInfo> pushList = new ArrayList<>();
	
	private Iterator<String> iter;
	public LinkedBlockingQueue<String> receivedAckQueue;

	public DBThread(Pushable pushable, LinkedBlockingQueue<String> receivedAckQueue) {
		this.pushable = pushable;
		this.receivedAckQueue = receivedAckQueue;
		this.db = new JDBCTemplate();
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {
				
				//ť�� �ִ� �ڷḦ �� �����´�
				iter = receivedAckQueue.iterator();
				if(iter.hasNext()){
					//�ϳ��� ������ DB ���°��� ������ �Ϸ�Ǿ��ٰ� �ٲ۴�
					String receiveOrderNum = iter.next();
					db.executeQuery_PUSH_STATUS_UPDATE(receiveOrderNum, "Y");
				}
				
				pushList = db.executeQuery_ORDER();

				if (ServerUtils.isEmpty(pushList)) {
					ServerConst.SERVER_LOGGER.info("�߼��� �ֹ����� ����");
				} else {
					ServerConst.SERVER_LOGGER.info("�߼��� �ֹ����� " + pushList.size() + "�� �˻�" );
					for (PushInfo orderNum : pushList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(orderNum.getOrder_num()));
						//TODO �� �κ��� Ư�� ����ڿ��� �˸��� �����Ƿ� setPushPartial �ٲ���� 
						setPushAll(orderNum);
					}
				}
				
				// 5�� �������� �����尡 ����ȴ�.
				Thread.sleep(ServerConst.DB_THREAD_OBSERVER_TIME);
			} catch (InterruptedException e) {
				e.getStackTrace();
				ServerConst.SERVER_LOGGER.error(e.getMessage());
				try {
					db.closeDBSet();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					ServerConst.SERVER_LOGGER.error(e1.getMessage());
				}
			} finally {
				pushList.clear();
			}
		}
	}

	/**  
	 * HashMap�� ��ϵ� ������ڿ��� �˸��� ������ ����ϴ� �޼ҵ� 
	 * @param msg PushInfo Ÿ���� �ֹ� ������
	 */
	public void setPushAll(PushInfo msg) {
		// �ֹ������� JSON�������� �ٲ۴�.
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		// �˸��޽����� ������.
		pushable.sendPushAll(msgPushJson);
	}
	
	/**
	 * HashMap�� ��ϵ� Ư������ڿ��� �˸��� ������ ����ϴ� �޼ҵ�
	 * @param msg PushInfo Ÿ���� �ֹ� ������
	 */
	public void setPushPartial(PushInfo msg) {
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		pushable.sendPushPartial(msg.getOrder_seller(), msgPushJson);
	}
	
	
}
