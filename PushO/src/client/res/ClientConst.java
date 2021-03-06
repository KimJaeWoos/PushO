package client.res;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author 최병철
 * @Description 모든 클래스에서 사용되는 상수 값들을 정리, 한글은 별도의 파일로 관리해야 함 TODO
 */
public interface ClientConst {
	/**
	 * Socket Connection
	 */

	String SERVER_IP ="175.115.95.32";
//	String SERVER_IP ="127.0.0.1";
	int PORT_NUM = 9998;

	int STREAM_TIME_OUT = 30000;
	int SEND_WATING_TIME = 10000;

	/**
	 * File Read & Write
	 */
	String SRC_FILE_PATH = "D:" + File.separator + "test_text" + File.separator + "client" + File.separator;
	String SRC_FILE_NAME = "src_test.txt";
	String DEST_FILE_PATH = "D:" + File.separator + "test_text" + File.separator + "server" + File.separator;
	String DEST_FILE_NAME = "dest_test.txt";

	/**
	 * DB Connection
	 */
	String CLASS_FOR_NAME = "com.mysql.jdbc.Driver";
	String JDBC_URL = "jdbc:mysql://175.115.95.32:3306/";
	String DB_NAME = "push_o?useUnicode=true&characterEncoding=UTF-8";
	String DB_USER_ID = "wodn4131";
	String DB_USER_PASSWORD = "wogud00";

	/**
	 * JSON
	 */
	String JSON_KEY_SEND_TIME = "send_time";
	String JSON_KEY_DATA_CATEGORY = "data_category";
	String JSON_KEY_DATA = "data";
	String JSON_KEY_DATA_SIZE = "data_size";
	String JSON_KEY_AUTH_ID = "id";
	String JSON_KEY_AUTH_PASSWD = "passwd";
	String JSON_KEY_AUTH_ENCRY = "key";
	String JSON_KEY_ORDER_NUM = "order_num";
	String JSON_KEY_ORDER_DATE = "order_date";
	String JSON_KEY_ORDER_USER = "order_user";
	String JSON_KEY_ORDER_SELLER = "order_seller";
	String JSON_KEY_ORDER_PRICE = "order_price";
	String JSON_KEY_ORDER_LIST = "order_list";
	String JSON_KEY_ORDER_PRODUCT = "product";
	String JSON_KEY_ORDER_PRODUCT_COUNT = "product_count";
	String JSON_KEY_ORDER_RESPONSE= "response";

	String JSON_VALUE_AUTH = "auth";
	String JSON_VALUE_PING = "ping";
	String JSON_VALUE_PONG = "pong";
	String JSON_VALUE_PUSH = "push";
	String JSON_VALUE_ENCRY = "encry";

	String END_LINE = "\n";
	int HEADER_LENTH = 4;
	int KEY_LENTH = 256;
	/**
	 * IO Stream
	 */
	Charset CHARSET = Charset.forName("UTF-8");

	/**
	 * ClientLogger
	 */
	Logger CLIENT_LOGGER = LogManager.getLogger("client");
}
