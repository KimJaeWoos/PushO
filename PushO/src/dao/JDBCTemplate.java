package dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import exception.EmptyResultDataException;
import model.UserAuth;
import res.Const;
import server.SetPrepareStatement;

/**
 * @author		�ֺ�ö
 * @Description	�����ͺ��̽��� �����Ͽ� ������ �����ϰ� ������� ��ȯ�ϴ� ���� ��� �۾��� ����
 * 				���� �� DBĿ�ؼ��� �غ��ϰ� �޼ҵ� ȣ�� �� ������ ����
 * 				�޼ҵ� ���ο��� {@link SetPrepareStatement} �ݹ� �������̽��� ����ϰ� 
 * 				Caller �ʿ� {@link EmptyResultDataException}�� 
 * 				���Ͽ� DB�� ��ϵ��� ���� �����(����X)���� �˸�
 * TODO			��Ƽ������ ȯ�濡�� ���ü��� ������ ���� DBĿ�ؼ� Ǯ ����
 * 				�ټ��� Ŭ���̾�Ʈ�� ���ÿ� ���� ���� �� �߻��� �� �ִ� ����ȭ ����
 */
public class JDBCTemplate {
	private Connection con = null;
	private PreparedStatement ps = null;
	private ResultSet rs;

	public JDBCTemplate() {
		if (con == null) {
			connectDB();
		}
	}

	private void connectDB() {
		try {
			Class.forName(Const.CLASS_FOR_NAME);
			con = DriverManager.getConnection(Const.JDBC_URL+Const.DB_NAME,
									Const.DB_USER_ID, Const.DB_USER_PASSWORD);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * sql���� �ش� �ʵ带 �����ϸ� ����� ������ Model ��ü�� �����Ͽ� ��ȯ�ϴ� �޼ҵ�
	 * ����� ���� ���ܸ� �߻����� ��ϵ��� ���� �����(����X)���� �˸�
	 * @param sql		������ sql��
	 * @param pstm		�ݹ� �������̽��� sql���� '?'�κп� ä���� �ʵ带 ����
	 * @return			DB�� ����� ������ �� ��ü�� �����Ͽ� ��ȯ
	 * @throws EmptyResultDataException	��ϵ��� ���� �����(����X)
	 * 
	 * TODO ����� {@link UserAuth}�� ����Ͽ� ������ ���� �������� �����Ǿ� �������� ����.
	 */
	public UserAuth executeQuery(String sql, SetPrepareStatement pstm) throws EmptyResultDataException {
		UserAuth userAuthResult = null;
		try {
			ps = con.prepareStatement(sql);
			
			//�ݹ� �������̽� ȣ��
			pstm.setFields(ps);

			rs = ps.executeQuery();
			
			//��� ���� ���ٸ� ������ ������ ������� �ʴ´�.
			if (rs.next()) {
				userAuthResult = new UserAuth();
				userAuthResult.setId(rs.getInt("id"));
				userAuthResult.setName(rs.getString("name"));
				userAuthResult.setIp(rs.getString("ip"));
				userAuthResult.setPort(rs.getInt("port"));
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				closeDBSet();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//������ ���� ������ �߻�
		if (userAuthResult == null) {
			throw new EmptyResultDataException("��ϵ� ����� �ƴ�");
		}
		return userAuthResult;
	}

	private void closeDBSet() throws SQLException {
		rs.close();
		ps.close();
		con.close();
	}
}
