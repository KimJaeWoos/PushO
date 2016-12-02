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

public class ProcessCilentRequest extends Thread {

	private Socket connectedSocketWithClient;
	private BufferedReader br;
	private BufferedWriter bw;
	
	public ProcessCilentRequest(Socket socket) {
		// TODO Auto-generated constructor stub
		this.connectedSocketWithClient = socket;
	}
	
	@Override
	public void run(){
		
		// TODO Auto-generated method stub
		try {
			br = new BufferedReader(new InputStreamReader(
					connectedSocketWithClient.getInputStream()));
			
			bw = new BufferedWriter(new OutputStreamWriter(
					connectedSocketWithClient.getOutputStream()));
			bw.write(Utils.makeJSONMessageForPingPong(true));
			System.out.println("������ ���� Ping ����");
			bw.flush();
			
			String text = null;
//			while ((text = br.readLine())!=null) {
//				System.out.println("Ŭ���̾�Ʈ�� ���� ���� ������ : "+text);
////				Utils.writeFile(Const.DEST_FILE_PATH+Const.DEST_FILE_NAME, text+"\r\n");
//			}
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
		}catch (SocketTimeoutException timeoutE){
			timeoutE.printStackTrace();
			try {
				bw.write(Utils.makeJSONMessageForPingPong(true));
				System.out.println("Ping ����");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					br.close();
					bw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

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
