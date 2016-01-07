import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//チャットサーバ
public class GoodPointAnalyzer {

	//開始
	public void start(int port) {
		ServerSocket   	server;//サーバソケット
		Socket         	socket;//ソケット
		Session 		thread;//スレッド

		try {
			server=new ServerSocket(port);
			System.err.println("チャットサーバ実行開始:"+port);
			while(true) {
				try {
					//接続待機
					socket=server.accept();

					//チャットサーバスレッド開始
					thread=new Session(socket);
					thread.start();
				} catch (IOException e) {
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	//メイン
	public static void main(String[] args) {
		GoodPointAnalyzer server=new GoodPointAnalyzer();
		server.start(8081);
	}
}