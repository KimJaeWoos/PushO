package server.service;

import java.util.List;

import server.model.OrderInfo;
import server.model.PushInfo;

/**
 * @author		�ֺ�ö
 * @Description	Ǫ�ñ���� ���� �������̽��� ��ο��� ������ Ǫ�ø޼ҵ�� Ư�� �Ǹ��ڿ��� ������ Ǫ�ø޼ҵ尡 �ִ�
 * TODO			Ư�� �ڷᱸ���� ���� ������� ���Ͽ� �޽����� �߼��ϴ� ����
 */
public interface Pushable {
	/**
	 * ��� Ŭ���̾�Ʈ���� �����ϰ� �޽����� ������ �޼ҵ�
	 * @param msg	������ Ǫ�� �޽���
	 */
	public void sendPushAll(PushInfo msg);
	
	/**
	 * Ư�� Ŭ���̾�Ʈ���� �޽����� ������ �޼ҵ�
	 * @param msg	OrderInfo Ÿ���� �޽���
	 */
	public void sendPushPartial(List<OrderInfo> orderList);
}
