package server.observer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import server.dao.JDBCTemplate;
import server.model.PushInfo;
import server.service.Pushable;
import server.util.ServerUtils;

/**
 * 
 * @author �����
 * @Description ������ �����带 �����Ͽ� ����� �ֹ����̺��� �׻� �����ϰ� ����ڿ��� PUSH �ؾ� �� �����͸�
 *              return�ϰ��ִ�.
 */
public class DBThread extends Thread {

	private Pushable pushable;
	public JDBCTemplate db;

	private String msgPushJson;

	private List<PushInfo> pushList = new ArrayList<>();

	public DBThread(Pushable pushable) {
		this.pushable = pushable;
		this.db = new JDBCTemplate();
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {

				Thread.sleep(5000);
				pushList = db.executeQuery_ORDER();

				if (ServerUtils.isEmpty(pushList)) {
					System.out.println("Ǫ�� ������ ����");
				} else {
					for (PushInfo orderNum : pushList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(orderNum.getOrder_num()));
						setPush(orderNum);
					}
				}
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				try {
					db.closeDBSet();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} finally {
				pushList.clear();
			}
		}
	}

	/**
	 * �Ѱ��� �ֹ��� ���� �������� ���´� 
	 * @param msg �ֹ��� ���� ������
	 */
	public void setPush(PushInfo msg) {
		// �ֹ������� JSON�������� �ٲ۴�.
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		// �˸��� ������ ���� DB�� �˸����¸� ���������� �ٲ۴�.
		db.executeQuery_PUSH_STATUS_UPDATE(msg.getOrder_num());
		// �˸��޽����� ������.
		pushable.sendPushAll(msgPushJson);
	}
}
