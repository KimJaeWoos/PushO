package test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import res.Const;
import util.Utils;

public class StreamTestClient {
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Socket socket = new Socket(Const.SERVER_IP, Const.PORT_NUM);
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
//		BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
		
		String msg1Str = Utils.makeJSONMessageForAuth("�̸�", "��й�ȣ",
				new JSONObject(), new JSONObject());
		byte[] msg1 = Utils.makeMessageStringToByte(
				new byte[Const.HEADER_LENTH+msg1Str.getBytes().length], msg1Str);
		
		String msg2Str = Utils.makeJSONMessageForAuth("�̸��Դϴ�.", "��й�ȣ�Դϴ�.",
				new JSONObject(), new JSONObject());
		byte[] msg2 = Utils.makeMessageStringToByte(
				new byte[Const.HEADER_LENTH+msg2Str.getBytes().length], msg2Str);
		
		String msg3Str = Utils.makeJSONMessageForPingPong(new JSONObject(), true);
		byte[] msg3 = Utils.makeMessageStringToByte(
				new byte[Const.HEADER_LENTH+msg3Str.getBytes().length], msg3Str);
		
		
		System.out.println(msg1.length);
		System.out.println(new String(msg1));
		bos.write(msg1);
		bos.flush();
		Thread.sleep(1000);
		System.out.println(msg2.length);
		System.out.println(new String(msg2));
		bos.write(msg2);
		bos.flush();
		Thread.sleep(1000);
		System.out.println(msg3.length);
		System.out.println(new String(msg3));
		bos.write(msg3);
		bos.flush();
		Thread.sleep(1000);
		String msgPingString = Utils.makeJSONMessageForPingPong(new JSONObject(), true);
		byte[] msgPingByte = Utils.makeMessageStringToByte(
				new byte[Const.HEADER_LENTH+msgPingString.getBytes().length], msgPingString);
		String msgPongString = Utils.makeJSONMessageForPingPong(new JSONObject(), false);
		byte[] msgPongByte = Utils.makeMessageStringToByte(
				new byte[Const.HEADER_LENTH+msgPongString.getBytes().length], msgPongString);
		
		byte[] buf = new byte[Const.HEADER_LENTH];
		int readCount = 0;
		int length = 0;
		int bodylength = 0;
		/*
		bos.write(msgPingByte);
		System.out.println("Ping ����");
		while((readCount=bis.read(buf))!=-1){
			length = Utils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			String pp = Utils.parseJSONMessage(new JSONParser(), new String(body));
			if(pp.equals(Const.JSON_VALUE_PING)){
				bos.write(msgPongByte);
				System.out.println("Pong ����");
			}
			if(pp.equals(Const.JSON_VALUE_PONG)){
				bos.write(msgPingByte);
				System.out.println("Ping ����");
			}
		}
		*/
		Thread.sleep(1000);
		bos.close();
		socket.close();
	}
}
