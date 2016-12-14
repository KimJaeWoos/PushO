package server.observer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;

import server.dao.JDBCTemplate;
import server.model.PushInfo;
import server.res.ServerConst;
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
	
	public LinkedBlockingQueue<PushInfo> receivedAckQueue;

	public DBThread(Pushable pushable, LinkedBlockingQueue<PushInfo> receivedAckQueue) {
		this.pushable = pushable;
		this.receivedAckQueue = receivedAckQueue;
		this.db = new JDBCTemplate();
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {
				
				/*
				//TODO ProcessClientRequest���� ���� ������� ť�� �۾��� ���� ��� �װ��� ������ DB�� ���¸� �����ϴ� ����
				if(this.receivedAckQueue.size()!=0){
					PushInfo receivedAckPushInfo = this.receivedAckQueue.take();
				}*/
				
				Thread.sleep(5000);
				pushList = db.executeQuery_ORDER();

				if (ServerUtils.isEmpty(pushList)) {
					ServerConst.SERVER_LOGGER.info("�߼��� �ֹ����� ����");
				} else {
					ServerConst.SERVER_LOGGER.info("�߼��� �ֹ����� " + pushList.size() + "�� �˻�" );
					for (PushInfo orderNum : pushList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(orderNum.getOrder_num()));
						setPushAll(orderNum);
					}
				}
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
	 * ��� �˸�
	 * @param msg �ֹ��� ���� ������
	 */
	public void setPushAll(PushInfo msg) {
		// �ֹ������� JSON�������� �ٲ۴�.
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		// �˸��޽����� ������.
		pushable.sendPushAll(msgPushJson);
	}
	
	/**
	 * �ֹ� �˸�
	 * @param msg
	 */
	public void setPushPartial(PushInfo msg) {
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		pushable.sendPushPartial(msg.getOrder_seller(), msgPushJson);
	}
	
	
}
