package server.exception;

/**
 * @author		�ֺ�ö
 * @Description	�̹� ����� ������� ��� �߻��ϴ� ����
 */
public class AlreadyConnectedSocketException extends RuntimeException{
	public AlreadyConnectedSocketException(String msg) {
		super(msg);
	}
}
