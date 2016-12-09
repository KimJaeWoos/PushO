package server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author �ֺ�ö
 * @Description ������ �� Ŭ���̾�Ʈ�� ��������� �ϸ� �۾��� �����ϴ� ������
 * @TODO ������� ���� ��Ʈ���� ���ڽ�Ʈ��->����Ʈ��Ʈ�� ���� ��ȯ ������ ��ƿŬ���� ������� �޽��� �ۼ���� ���� Ÿ�Ӿƿ� ���ܰ�
 *       �߻����� ��� �˸��޽����� �����ϰ� �������� �� �ڿ�ȸ�� ��Ŀ���� ����ó������ ���� ���� try-catch���� ����
 */
public class ProcessCilentRequest extends Thread {

	private Socket connectedSocketWithClient;
	private byte[] msgPingByte;
	private byte[] msgPongByte;
	private byte[] msgPushByte;

	private BufferedOutputStream bos;
	private BufferedInputStream bis;

	public ProcessCilentRequest(Socket socket) {
		this.connectedSocketWithClient = socket;
	}

	@Override
	public void run() {
		try {
			bos = new BufferedOutputStream(connectedSocketWithClient.getOutputStream());
			bis = new BufferedInputStream(connectedSocketWithClient.getInputStream());
			
			/**
			 * ���� �����͸� �ְ� �޴� �κ� 
			 * @TODO : �����Ӹ� �ƴ϶� ���� �����͸� �ְ�޴� ����
			 */
			startPingPong();
		} catch (SocketTimeoutException timeoutE) {
			// TODO Ÿ�Ӿƿ� �߻� �ÿ� �ڿ�ȸ�� �� ó�� ��Ŀ����
			timeoutE.printStackTrace();
			try {
				bos.write(msgPingByte);
				System.out.println("Ping ����");

			} catch (IOException e) {
				e.printStackTrace();
				try {
					bis.close();
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startPingPong() throws IOException, InterruptedException {
		String msgPingString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), true);
		msgPingByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + msgPingString.getBytes().length], msgPingString);
		String msgPongString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), false);
		msgPongByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + msgPongString.getBytes().length], msgPongString);

		byte[] buf = new byte[ServerConst.HEADER_LENTH];
		byte[] body;
		int readCount = 0;
		int length = 0;
		int bodylength = 0;

		bos.write(msgPingByte);
		bos.flush();
		System.out.println("������ ���� Ping ����");

		while ((readCount = bis.read(buf)) != -1) {
			length = ServerUtils.byteToInt(buf);
			body = new byte[length];
			bodylength = bis.read(body);
			String pp = ServerUtils.parseJSONMessage(new JSONParser(), new String(body));
			if (pp.equals(ServerConst.JSON_VALUE_PING)) {
				bos.write(msgPongByte);
				System.out.println("Pong ����");
			}
			if (pp.equals(ServerConst.JSON_VALUE_PONG)) {
				bos.write(msgPingByte);
				System.out.println("Ping ����");
			}
			bos.flush();
		}
	}

	/**
	 * Ŭ���̾�Ʈ���� �˸��� �����ϴ� �޼ҵ� String �����͸� �����ڷ� split�Ͽ� ������ ���Ŀ� �°� �����Ѵ�.
	 * 
	 * @param msg
	 *            �ֹ�����
	 */
	public void setPush(String msg) {
		try {
			msgPushByte = ServerUtils.makeMessageStringToByte(
					new byte[ServerConst.HEADER_LENTH + msg.getBytes(ServerConst.CHARSET).length], msg);

			bos.write(msgPushByte);
			bos.flush();
			System.out.println("Ǫ���Ϸ�:" + this.getName());
		} catch (IOException e) {
			// ��� Ŭ���̾�Ʈ ������ �������� �߻�
			// �׿� ���� HashMap�� ����Ǿ��ִ� ���� Thread�� ����� �۾��� �ʿ���
			System.out.println("setPush() Ǫ���߼��� ����" + e.getMessage());
		}
	}
}
