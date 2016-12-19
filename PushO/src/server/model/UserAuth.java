package server.model;

/**
 * @author		�ֺ�ö
 * @Description	�׽�Ʈ ���̺��� �ʵ尪���� ���� Model
 * TODO			���� ������ ���� �ʵ尡 ����� ��� ����
 */
public class UserAuth {
	private String id;
	private String passwd_salt;
	private String passwd;
	
	public UserAuth() {
	}
	
	public UserAuth(String id, String name, String passwd) {
		this.id = id;
		this.passwd_salt = name;
		this.passwd = passwd;
	}

	@Override
	public String toString() {
		return "[ ���� ���� : "+this.id+" , "+this.passwd_salt+" , "+this.passwd+" ]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPasswd_salt() {
		return passwd_salt;
	}

	public void setPasswd_salt(String name) {
		this.passwd_salt = name;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	

}
