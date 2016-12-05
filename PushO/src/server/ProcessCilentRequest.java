package server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dao.JDBCTemplate;
import exception.EmptyResultDataException;
import res.Const;
import util.Utils;

/**
 * @author		�ֺ�ö
 * @Description	������ �� Ŭ���̾�Ʈ�� ��������� �ϸ� �۾��� �����ϴ� ������
 * TODO			������� ���� ��Ʈ���� ���ڽ�Ʈ��->����Ʈ��Ʈ�� ���� ��ȯ
 * 				������ ��ƿŬ���� ������� �޽��� �ۼ���� ����
 * 				Ÿ�Ӿƿ� ���ܰ� �߻����� ��� �˸��޽����� �����ϰ� �������� �� �ڿ�ȸ�� ��Ŀ����
 * 				����ó������ ���� ���� try-catch���� ����
 */
public class ProcessCilentRequest extends Thread {

	private Socket connectedSocketWithClient;
	private BufferedReader br;
	private BufferedWriter bw;
	
	public ProcessCilentRequest(Socket socket) {
		this.connectedSocketWithClient = socket;
	}
	
	@Override
	public void run(){
		try {
			br = new BufferedReader(new InputStreamReader(
					connectedSocketWithClient.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(
					connectedSocketWithClient.getOutputStream()));
			
			bw.write(Utils.makeJSONMessageForPingPong(true));
			System.out.println("������ ���� Ping ����");
			bw.flush();
			
			//����������
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
		}catch (SocketTimeoutException timeoutE){
			//TODO Ÿ�Ӿƿ� �߻� �ÿ� �ڿ�ȸ�� �� ó�� ��Ŀ����
			timeoutE.printStackTrace();
			try {
				bw.write(Utils.makeJSONMessageForPingPong(true));
				System.out.println("Ping ����");
				
			} catch (IOException e) {
				e.printStackTrace();
				try {
					br.close();
					bw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
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
