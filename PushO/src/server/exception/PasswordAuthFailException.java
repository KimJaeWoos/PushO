package server.exception;


/**
 * ��й�ȣ �� �� ��ġ���� ������ �߻��ϴ� ����� ���� ����
 * @author user �����
 *
 */
public class PasswordAuthFailException extends RuntimeException {
	public PasswordAuthFailException(String msg) {
		super(msg);
	}
}
