package server.model;

/**
 * @author		�ֺ�ö
 * @Description	�׽�Ʈ ���̺��� �ʵ尪���� ���� Model
 * TODO			���� ������ ���� �ʵ尡 ����� ��� ����
 */
public class UserAuth {
	private String id;
	private String name;
	private String passwd;
	
	public UserAuth() {
	}
	
	public UserAuth(String id, String name, String passwd) {
		this.id = id;
		this.name = name;
		this.passwd = passwd;
	}

	@Override
	public String toString() {
		return "[ ���� ���� : "+this.id+" , "+this.name+" , "+this.passwd+" ]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	

}
