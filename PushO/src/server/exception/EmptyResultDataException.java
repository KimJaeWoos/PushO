package server.exception;

/**
 * @author		�ֺ�ö
 * @Description	DB�� �˻� ����� ���� ��� �߻��ϴ� ����� ���� ����
 */
public class EmptyResultDataException extends RuntimeException{
	public EmptyResultDataException(String msg) {
		super(msg);
	}
}
