package test;

import java.nio.ByteOrder;

import server.util.ServerUtils;

public class MessageProtocolTest {
	public static void main(String[] args) {
		new MsgBasedOnProtocol();
	}
	
	
	static class MsgBasedOnProtocol{
		private static final String name = "�̸���";
		private static final String passwd = "��й�ȣ";
		public MsgBasedOnProtocol() {
			
			
			
//			byteTest();
		}
		
		private void byteTest() {
			byte[] bName = name.getBytes();
			byte[] bPasswd = passwd.getBytes();
			byte[] ret = new byte[bName.length+bPasswd.length];
			System.arraycopy(bPasswd, 0, ret, 0, bPasswd.length);
			System.arraycopy(bName, 0, ret, bPasswd.length, bName.length);
			
			System.out.println(
					ServerUtils.intTobyte(1202012312).length
					);
		}
	}
}
