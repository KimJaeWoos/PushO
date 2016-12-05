package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import res.Const;
import util.Utils;

/**
 * @author 		�ֺ�ö
 * @Description	Ŭ���̾�Ʈ ���α׷�, ������ ���ÿ� ������������ ���� �� �ó������� ���� ���
 * TODO			�ۼ��� �ϴ� ��Ʈ���� ���ڽ�Ʈ������ ����Ʈ��Ʈ������ ��ȯ
 * 				Ÿ�Ӿƿ��̳� ��Ÿ ���Ṯ�� ���� ��Ŀ���� ���� �� ��Ÿ ����ó��
 * 				�̺�Ʈ�� �߻���Ű�ų� �����͸� ���� �� ���������� ������ View
 */
public class OIOClient {
	
	public static void main(String[] args) {
		new OIOClient();
	}
	
	private BufferedWriter bw;
	private Socket socket;
	private BufferedReader br;
	
	public OIOClient() {
		try {
			socket = new Socket(Const.SERVER_IP, Const.PORT_NUM);
			//�Է½�Ʈ���� ���� Ÿ�Ӿƿ� ����
			socket.setSoTimeout(Const.STREAM_TIME_OUT);
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			/*
			 * ���� ������ ���� �޽����� ������ �۽��ϴ� �κ�
			 * TODO : ��ȣȭ Ű ��ȯ, ����Ű ����, ����ũ�� ��ȯ ���� �ʱ�ȭ �۾�
			 */
			bw.write(Utils.makeJSONMessageForAuth("������", "1111"));
			System.out.println("���� ����");
			bw.flush();
			
			/**
			 * ���� �����͸� �ְ� �޴� �κ�
			 * TODO : �����Ӹ� �ƴ϶� ���� �����͸� �ְ�޴� ����
			 */
			String text = null;
			while((text = br.readLine())!=null){
				Thread.sleep(Const.SEND_WATING_TIME);
				String pp = Utils.parseJSONMessage(text);
				if(pp.equals(Const.JSON_VALUE_PING)){
					bw.write(Utils.makeJSONMessageForPingPong(false));
					System.out.println("Pong ����");
				}
				if(pp.equals(Const.JSON_VALUE_PONG)){
					bw.write(Utils.makeJSONMessageForPingPong(true));
					System.out.println("Ping ����");
				}
				bw.flush();
			}
			
			
//			autoPingPong(3);
//			text = br.readLine();
//			autoPingPong(3);
			
		}catch (SocketTimeoutException timeoutE){
			timeoutE.printStackTrace();
			//TODO Ÿ�Ӿƿ��� �߻����� ��� ���� ��Ŀ����
			try {
				bw.write(Utils.makeJSONMessageForPingPong(true));
				System.out.println("Ping ����");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					br.close();
					bw.close();
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * �׽�Ʈ�� ���� PingPong �޽��� �ۼ��� �޼ҵ�
	 * 
	 * @param time					�޽����� �����ϰ� ������ ���� Ƚ��
	 * @throws IOException			
	 * @throws InterruptedException
	 */
	private void autoPingPong(int time) throws IOException, InterruptedException {
		String text;
		for(int i = 0; i<time; i++){
			text = br.readLine();
			Thread.sleep(Const.SEND_WATING_TIME);
			String pp = Utils.parseJSONMessage(text);
			if(pp.equals(Const.JSON_VALUE_PING)){
				bw.write(Utils.makeJSONMessageForPingPong(false));
				System.out.println("Pong ����");
			}
			if(pp.equals(Const.JSON_VALUE_PONG)){
				bw.write(Utils.makeJSONMessageForPingPong(true));
				System.out.println("Ping ����");
			}
			bw.flush();
		}
	}
}
