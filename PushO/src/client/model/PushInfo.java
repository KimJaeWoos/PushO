package client.model;

import java.util.List;

/**
 * @author		�����
 * @Description	���˸� ������ ���� Model
 * TODO			���� ����� �� ����
 */
public class PushInfo {
	
	private List<ProductList> order_list;

	public PushInfo(List<ProductList> order_list) {
		this.order_list = order_list;
	}	
	
	public List<ProductList> getOrder_list() {
		return order_list;
	}
	public void setOrder_list(List<ProductList> order_list) {
		this.order_list = order_list;
	}

	
}
