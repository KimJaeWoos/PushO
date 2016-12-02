package test;

import util.Utils;

public class JSONTest {
	public static void main(String[] args) {
		jsonTest();
	}
	
	private static void jsonTest() {
		System.out.println(
				Utils.parseJSONMessage(
						Utils.makeJSONMessageForAuth(
								"�׽�Ʈ���̵�", "�׽�Ʈ ��й�ȣ"))
				);
		System.out.println(
				Utils.parseJSONMessage(
						Utils.makeJSONMessageForPingPong(
								true))
				);
	}
}
