package test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerTest {

	public static void main(String[] args) {
		Timer timer = new Timer();
		// �������� (3���� �����ϸ�, ���� ���� �ð����� ���� 1�ʸ��� ������)
		timer.scheduleAtFixedRate(new HelloTask(), 3000, 1000);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timer.cancel();

	}
}

class HelloTask extends TimerTask {
	public void run() {
		System.out.println(new Date());
	}
}
