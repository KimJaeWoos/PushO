package server;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dao.JDBCTemplate;

/**
 * @author		�ֺ�ö
 * @Description	{@link JDBCTemplate}���� ������ �������� �ٲٱ� ���� �ݹ� �������̽�
 * TODO			���ٷ� ��ȯ ���
 */
public interface SetPrepareStatement {
	public void setFields(PreparedStatement pstm) throws SQLException;
}
