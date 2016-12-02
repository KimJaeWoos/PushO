package test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import util.Utils;

public class StreamTestClient {
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Socket socket = new Socket("localhost", 30000);
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		byte[] msg = Utils.makeMessageStringToByte( 
				Utils.makeJSONMessageForAuth("�̸��̴�", "��й�ȣ��"));
		byte[] msg1 = Utils.makeMessageStringToByte( 
				Utils.makeJSONMessageForAuth("�̸��ΰ�����", "��й�ȣ�ΰ�����"));
		System.out.println(msg.length);
		System.out.println(new String(msg));
		System.out.println(msg1.length);
		System.out.println(new String(msg1));
		bos.write(
				Utils.mergeBytearrays(
						Utils.intTobyte(msg.length),msg));
		bos.flush();
		System.out.println(msg1.length);
		System.out.println(new String(msg1));
		bos.write(
				Utils.mergeBytearrays(
						Utils.intTobyte(msg1.length),msg1));
		bos.flush();
		
		Thread.sleep(1000);
		bos.close();
		socket.close();
	}
}
