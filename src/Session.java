import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//チャットサーバスレッド
public class Session extends Thread {
	private static List<Session> threads= new ArrayList<Session>();//スレッド郡
	private Socket socket;//ソケット
	private String usrID;//ユーザのＩＤ
	private String usrPass;//ユーザのＰass
	private String usrName;//ユーザの名前
	private List<Data> usrData = new ArrayList<Data>();
	private List<Data> allData = new ArrayList<Data>();


	// コンストラクタ
	public Session(Socket socket) {
		super();
		this.socket=socket;
		threads.add(this);
	}

	//ログイン
	public boolean Login(String ID,String pass){
		try{
			File file = new File("GoodPointUserInfo.csv");
			if (checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				while((str = br.readLine()) != null){
					String[] strSplit = str.split(",");
					String tmpID = strSplit[0], tmpPass = strSplit[1], tmpName = strSplit[2];
					//					System.out.println(ID+"=="+tmpID+","+pass+"=="+tmpPass);
					//					System.out.println(ID.equals(tmpID));
					//					System.out.println(pass.trim().equals(tmpPass));
					if(ID.trim().equals(tmpID) && pass.trim().equals(tmpPass)){
						System.out.println("認証しました: "+tmpName);
						usrID = tmpID;
						usrPass = tmpPass;
						usrName = tmpName;
						br.close();
						return true;
					}
				}

				br.close();
			}else{
				System.out.println("ファイルが見つからないか開けません");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}

		System.out.println("認証失敗: 一致するＩＤとＰassがありません");
		return false;
	}
	//データベースに登録
	public boolean sighUp(String message){
		String[] messageSplit = message.split(",");
		if(messageSplit.length != 4){
			System.out.println("送信されたデータが正しくない");
			return false;
		}
		try{
			File file = new File("GoodPointUserInfo.csv");
			if (checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				while((str = br.readLine()) != null){
					String[] strSplit = str.split(",");
					String tmpID = strSplit[0], tmpPass = strSplit[1], tmpName = strSplit[2];
					//					System.out.println(ID+"=="+tmpID+","+pass+"=="+tmpPass);
					//					System.out.println(ID.equals(tmpID));
					//					System.out.println(pass.trim().equals(tmpPass));
					if(messageSplit[1].trim().equals(tmpID)){
						System.out.println("このIDはすでに使用されています: "+tmpID);
						br.close();
						return false;
					}
					else if(messageSplit[3].trim().equals(tmpName)){
						System.out.println("この名前はすでに使用されています: "+tmpName);
						br.close();
						return false;
					}
				}

				br.close();
			}else{
				System.out.println("ファイルが見つからないか開けません");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		try{
			String FS = File.separator;
			//File f = new File("c:"+FS+"Users"+FS+"Kurisu"+FS+"Downloads"+FS+"pleiades"+FS+"workspace"+FS+"TestSocket"+FS+"MultiHandGestureLog("+date+").csv");
			File f = new File("GoodPointUserInfo.csv");

			FileWriter fw = new FileWriter(f,true); //書き込むファイル指定。ファイルが既にあるなら、そのファイルの末尾に書き込む
			BufferedWriter bw = new BufferedWriter(fw); //バッファクラスでfwを包んであげる
			PrintWriter pw = new PrintWriter(bw); //さらに、PrintWriterで包む

			String writeStr = messageSplit[1]+","+messageSplit[2]+","+messageSplit[3];
			pw.write(writeStr);
			//pw.println();
			pw.close(); //ファイル閉じる
			return true;
		}catch(IOException e){
			System.out.println("エラー："+e);
			return false;
		}
	}
	//命令解析
	public int commandAnalyze(String message){
		String[] messageSplit = message.split(",");
		if(isNum(messageSplit[0])){
			int commandID = Integer.parseInt(messageSplit[0]);
			return commandID;
		}
		else{
			return 0;
		}

	}

	public boolean isNum(String number){
		try{
			Integer.parseInt(number);
			return true;
		}catch(NumberFormatException e){
			return false;
		}

	}
	//ファイルチェッカー
	private static boolean checkBeforeReadfile(File file){
		if(file.exists()){
			if(file.isFile() && file.canRead()){
				return true;
			}
		}
		return false;
	}
	//処理
	public void run() {
		InputStream in =null;
		String message;
		int size;
		byte[] w=new byte[10240];
		try {
			//ストリーム
			in =socket.getInputStream();

			while(true) {
				//受信待ち
				size=in.read(w);

				//切断
				if (size<=0) throw new IOException();

				//読み込み
				message=new String(w,0,size,"UTF8");
				System.out.println(message);
				//全員にメッセージ送信
				//sendMessageAll(message);

				//命令解析
				//終了命令
				if(commandAnalyze(message)==0){
					sendMessage(this,"quit");
					break;
				}
				//ログイン確認命令
				else if (commandAnalyze(message)==1) {
					String[] messageSplit = message.split(",");
					String tmpID = "", tmpPass = ",";
					if(messageSplit.length>2){
						tmpID = messageSplit[1];
						tmpPass = messageSplit[2];
					}
					if(Login(tmpID, tmpPass)){
						sendMessage(this,usrName);
						//System.out.println("aaa"+usrName);
						//						((Session)this).sendMessage(this,message);
						//System.out.println(this.isAlive());
						//						sendMessageAll(message);
					}
					else{
						sendMessage(this,"error: Login");
					}
				}
				//関連データ収集命令
				else if(commandAnalyze(message)==2){
					String[] messageSplit = message.split(",");
					String tmpID = "";
					if(messageSplit.length>1){
						tmpID = messageSplit[1];
					}
					if(readerDataBase(tmpID)){
						sendMessage(this, "収集完了"+usrData.size());
					}
					else{
						System.out.println("userIDが一致していない");
						sendMessage(this,"error: userID");
					}
				}

				//関連データの表示命令
				else if(commandAnalyze(message)==3){
					String[] messageSplit = message.split(",");
					String tmpID = "";
					if(messageSplit.length>1){
						tmpID = messageSplit[1];
					}
					sendMessage(this, "結果: "+showReferData(tmpID));
					System.out.println(showReferData(tmpID));
				}
				//自分の結果を返す命令
				else if(commandAnalyze(message)==4){
					String[] messageSplit = message.split(",");
					String tmpID = "";
					if(messageSplit.length>1){
						tmpID = messageSplit[1];
					}
					sendMessage(this, "結果: "+returnResult(tmpID));
					System.out.println(returnResult(tmpID));
				}
				//他社評価の結果を返す命令
				else if(commandAnalyze(message)==5){
					String[] messageSplit = message.split(",");
					String tmpID = "";
					if(messageSplit.length>1){
						tmpID = messageSplit[1];
					}
					sendMessage(this, "結果: "+returnOtherEval(tmpID));
					//System.out.println(returnOtherEval(tmpID));
				}

				//全データ収集命令
				else if(commandAnalyze(message)==6){
					String[] messageSplit = message.split(",");
					String tmpID = "";
					if(messageSplit.length>1){
						tmpID = messageSplit[1];
					}
					if(readerAllDataBase(tmpID)){;
					sendMessage(this, "すべてのデータを収集完了");
					}
					else{
						sendMessage(this, "error");
					}
				}
				//レアな特徴抽出命令
				else if(commandAnalyze(message)==7){
					String[] messageSplit = message.split(",");
					String tmpID = "";
					if(messageSplit.length>1){
						tmpID = messageSplit[1];
					}
					sendMessage(this, "結果: "+returnRareGoodPoint(tmpID));
					//System.out.println(returnOtherEval(tmpID));
				}
				//自分の正答率算出命令
				else if(commandAnalyze(message)==8){
					String[] messageSplit = message.split(",");
					String tmpID = "";
					if(messageSplit.length>1){
						tmpID = messageSplit[1];
					}
					sendMessage(this, "結果: "+calAccuracy(tmpID));
				}
				//自分に近い人検索命令
				else if(commandAnalyze(message)==9){
					String[] messageSplit = message.split(",");
					String tmpID = "";
					if(messageSplit.length>1){
						tmpID = messageSplit[1];
					}
					sendMessage(this, "結果: "+findSimilar(tmpID));
				}
				//書き込み命令
				else if(commandAnalyze(message)==10){
					String[] messageSplit = message.split(",");
					String tmpID = "";
					if(messageSplit.length>1){
						tmpID = messageSplit[1];
					}
					if(writeCSV(tmpID,message)){
						sendMessage(this, "書き込み完了");
					}
					else{
						System.out.println("書き込み失敗");
					}
				}
				//登録命令
				else if(commandAnalyze(message)==11){
					String[] messageSplit = message.split(",");
					String tmpID = "";

					if(sighUp(message)){
						sendMessage(this, "登録完了");
					}
					else{
						sendMessage(this, "登録失敗");
						System.out.println("登録失敗");
					}
				}

			}
			socket.close();
			threads.remove(this);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	//全員にメッセージ送信
	public void sendMessageAll(String message) {
		Session thread;
		for (int i=0;i<threads.size();i++) {
			thread=(Session)threads.get(i);
			if (thread.isAlive()) thread.sendMessage(this,message);
		}

		System.out.println(message);
		//writeCSV(message);
	}



	//メッセージをｃｓｖに書き込み
	public boolean writeCSV(String tmpID,String message){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String date = sdf.format(cal.getTime());
		try{
			String FS = File.separator;
			//File f = new File("c:"+FS+"Users"+FS+"Kurisu"+FS+"Downloads"+FS+"pleiades"+FS+"workspace"+FS+"TestSocket"+FS+"MultiHandGestureLog("+date+").csv");
			File f = new File("GoodPointDataBase.csv");

			FileWriter fw = new FileWriter(f,true); //書き込むファイル指定。ファイルが既にあるなら、そのファイルの末尾に書き込む
			BufferedWriter bw = new BufferedWriter(fw); //バッファクラスでfwを包んであげる
			PrintWriter pw = new PrintWriter(bw); //さらに、PrintWriterで包む
			String [] messageSplit = message.split(",");
			if(messageSplit.length < 9){
				pw.close();
				return false;
			}
			String writeStr = messageSplit[2]+","+messageSplit[3]+","+messageSplit[4]+","
					+ messageSplit[5]+","+messageSplit[6]+","+messageSplit[7]+","+messageSplit[8];
			pw.write(writeStr);
			pw.println();
			pw.close(); //ファイル閉じる
			return true;
		}catch(IOException e){
			System.out.println("エラー："+e);
			return false;
		}
	}
	//メッセージ送信
	public void sendMessage(Session talker,String message){
		try {
			OutputStream out=socket.getOutputStream();
			byte[] w=(message.trim()+"\r\n").getBytes("UTF8");
			out.write(w);
			//System.out.println(w);
			out.flush();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	//データベースから自分に関係するものを読み込み
	public boolean readerDataBase(String tmpID){
		if(!tmpID.equals(usrID)) return false;
		try{
			File file = new File("GoodPointDataBase.csv");
			if (checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				while((str = br.readLine()) != null){
					String[] strSplit = str.split(",");
					String tmpFromName = strSplit[0], tmpToName = strSplit[1];
					String[] tmpGoodPoint = {strSplit[2],strSplit[3],strSplit[4],strSplit[5],strSplit[6]};
					if(usrID.trim().equals(tmpFromName) || usrID.trim().equals(tmpToName)){
						Data tmpData = new Data();
						tmpData.setFromName(tmpFromName);
						tmpData.setToName(tmpToName);
						tmpData.setGoodPoint(tmpGoodPoint);
						usrData.add(tmpData);
					}
					else if(tmpToName.trim().equals(tmpFromName.trim())){
						Data tmpData = new Data();
						tmpData.setFromName(tmpFromName);
						tmpData.setToName(tmpToName);
						tmpData.setGoodPoint(tmpGoodPoint);
						usrData.add(tmpData);
					}
				}

				br.close();
			}else{
				System.out.println("ファイルが見つからないか開けません");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		System.out.println("関連データの収集完了");
		return true;
	}

	//データベースからすべてのデータを読み込み
	public boolean readerAllDataBase(String tmpID){
		if(!tmpID.equals(usrID)) {
			return false;
		}
		try{
			File file = new File("GoodPointDataBase.csv");
			if (checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				while((str = br.readLine()) != null){
					String[] strSplit = str.split(",");
					String tmpFromName = strSplit[0], tmpToName = strSplit[1];
					String[] tmpGoodPoint = {strSplit[2],strSplit[3],strSplit[4],strSplit[5],strSplit[6]};
					Data tmpData = new Data();
					tmpData.setFromName(tmpFromName);
					tmpData.setToName(tmpToName);
					tmpData.setGoodPoint(tmpGoodPoint);
					allData.add(tmpData);
				}

				br.close();
			}else{
				System.out.println("ファイルが見つからないか開けません");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		System.out.println("全データの収集完了");
		return true;
	}
	//自分の結果を返す
	public String returnResult(String tmpID){
		if(!tmpID.equals(usrID)) {
			return "no match ID";
		}
		for(int i = 0;i < usrData.size(); i++){
			String tmpFromName = usrData.get(i).getFromName();
			String tmpToName = usrData.get(i).getToName();
			if(usrID.trim().equals(tmpFromName.trim()) && usrID.trim().equals(tmpToName.trim())){
				String[] tmpGoodPoint = usrData.get(i).getGoodPoint().clone();
				String tmpResult = tmpGoodPoint[0]+","+tmpGoodPoint[1]+","+tmpGoodPoint[2]+","
						+ ""+tmpGoodPoint[3]+","+tmpGoodPoint[4];
				return tmpResult;
			}
		}
		return "no result";
	}

	//関連データの表示
	public String showReferData(String tmpID){
		if(!tmpID.equals(usrID)) {
			return "no match ID";
		}
		for(int i = 0;i < usrData.size(); i++){
			String tmpFromName = usrData.get(i).getFromName();
			String tmpToName = usrData.get(i).getToName();
			String[] tmpGoodPoint = usrData.get(i).getGoodPoint().clone();
			System.out.println("from:"+tmpFromName+" to: "+tmpToName+" GoodPoint: "+tmpGoodPoint[0]+","+tmpGoodPoint[1]+","
					+ ""+tmpGoodPoint[2]+","+tmpGoodPoint[3]+","+tmpGoodPoint[4]);
		}
		return "success";
	}

	//他社評価の結果を表示
	public String returnOtherEval(String tmpID){
		if(!tmpID.equals(usrID)) {
			return "no match ID";
		}
		int otherEvalNum = 0;
		//1.親密性, 2.冷静沈着, 3.受容力, 4.決断力, 5.悠然, 6.柔軟性
		//7.現実思考, 8.自己信頼, 9.バランス, 10.社交性, 11.高揚性, 12.自立
		//13.慎重性, 14.俊敏性, 15.継続力, 16.挑戦心, 17.感受性, 18.独創性
		int[] AllGoodPoint = new int[18];
		for (int i = 0; i < AllGoodPoint.length; i++) {
			AllGoodPoint[i] = 0;
		}
		int other = 0;

		for(int i = 0;i < usrData.size(); i++){
			String tmpFromName = usrData.get(i).getFromName();
			if(!usrID.trim().equals(tmpFromName.trim())){
				otherEvalNum++;
				String[] tmpGoodPoint = usrData.get(i).getGoodPoint().clone();
				for (int j = 0; j < tmpGoodPoint.length; j++) {
					int tmpNum = returnGoodPointToNum(tmpGoodPoint[j]);
					if(tmpNum < 18){
						AllGoodPoint[tmpNum]++;
					}
					else{
						other++;
					}
				}
			}
		}

		String[][] result = new String[18][2];
		//System.out.println("評価者数： "+otherEvalNum);
		for (int i = 0; i < AllGoodPoint.length; i++) {
			//System.out.println(returnNumToGoodPoint(i)+":\t"+(int)(AllGoodPoint[i]*100/otherEvalNum));
			result[i][0] = returnNumToGoodPoint(i);
			result[i][1] = ""+(int)(AllGoodPoint[i]*100/otherEvalNum);
		}

		for (int i = 0; i < AllGoodPoint.length; i++) {
			int tmpMax = Integer.parseInt(result[i][1]);
			for (int j = i + 1; j < AllGoodPoint.length; j++) {
				int tmpNow = Integer.parseInt(result[j][1]);
				if(tmpMax < tmpNow){
					String tmpStr0 = result[i][0];
					result[i][0] = result[j][0];
					result[j][0] = tmpStr0;
					String tmpStr1 = result[i][1];
					result[i][1] = result[j][1];
					result[j][1] = tmpStr1;
					tmpMax = tmpNow;
				}
			}
		}

		//ソート
		//System.out.println("ソート");
		String returnResult = result[0][0]+","+result[0][1];
		for (int i = 1; i < AllGoodPoint.length; i++) {
			//System.out.println(result[i][0]+":\t"+result[i][1]);
			returnResult = returnResult +","+ result[i][0]+","+result[i][1];
		}

		return returnResult;
	}

	//ＧｏｏｄＰｏｉｎｔに対応する数値を返す
	public int returnGoodPointToNum(String GoodPoint){
		if(GoodPoint.trim().equals("親密性")){
			return 0;
		}
		else if(GoodPoint.trim().equals("冷静沈着")){
			return 1;
		}
		else if(GoodPoint.trim().equals("受容力")){
			return 2;
		}
		else if(GoodPoint.trim().equals("決断力")){
			return 3;
		}
		else if(GoodPoint.trim().equals("悠然")){
			return 4;
		}
		else if(GoodPoint.trim().equals("柔軟性")){
			return 5;
		}
		else if(GoodPoint.trim().equals("現実思考")){
			return 6;
		}
		else if(GoodPoint.trim().equals("自己信頼")){
			return 7;
		}
		else if(GoodPoint.trim().equals("バランス")){
			return 8;
		}
		else if(GoodPoint.trim().equals("社交性")){
			return 9;
		}
		else if(GoodPoint.trim().equals("高揚性")){
			return 10;
		}
		else if(GoodPoint.trim().equals("自立")){
			return 11;
		}
		else if(GoodPoint.trim().equals("慎重性")){
			return 12;
		}
		else if(GoodPoint.trim().equals("俊敏性")){
			return 13;
		}
		else if(GoodPoint.trim().equals("継続力")){
			return 14;
		}
		else if(GoodPoint.trim().equals("挑戦心")){
			return 15;
		}
		else if(GoodPoint.trim().equals("感受性")){
			return 16;
		}
		else if(GoodPoint.trim().equals("独創性")){
			return 17;
		}
		else{
			return 18;
		}
	}

	//ＧｏｏｄＰｏｉｎｔに対応する数値を返す
	public String returnNumToGoodPoint(int num){
		if(num == 0){
			return "親密性";
		}
		else if(num == 1){
			return "冷静沈着";
		}
		else if(num == 2){
			return "受容力";
		}
		else if(num == 3){
			return "決断力";
		}
		else if(num == 4){
			return "悠然";
		}
		else if(num == 5){
			return "柔軟性";
		}
		else if(num == 6){
			return "現実思考";
		}
		else if(num == 7){
			return "自己信頼";
		}
		else if(num == 8){
			return "バランス";
		}
		else if(num == 9){
			return "社交性";
		}
		else if(num == 10){
			return "高揚性";
		}
		else if(num == 11){
			return "自立";
		}
		else if(num == 12){
			return "慎重性";
		}
		else if(num == 13){
			return "俊敏性";
		}
		else if(num == 14){
			return "継続力";
		}
		else if(num == 15){
			return "挑戦心";
		}
		else if(num == 16){
			return "感受性";
		}
		else if(num == 17){
			return "独創性";
		}
		else{
			return "エラー";
		}
	}

	//希少な順にＧｏｏｄＰｏｉｎｔを返す
	public String returnRareGoodPoint(String tmpID){
		if(!tmpID.equals(usrID)) {
			return "no match ID";
		}
		int usrSum = 0;
		int[] tmpRarity = new int[18];
		for (int j = 0; j < tmpRarity.length; j++) {
			tmpRarity[j] = 0;
		}
		for(int i = 0;i < usrData.size(); i++){
			String tmpFromName = usrData.get(i).getFromName();
			String tmpToName = usrData.get(i).getToName();
			if(tmpToName.trim().equals(tmpFromName.trim())){
				usrSum++;
				String[] tmpGoodPoint = usrData.get(i).getGoodPoint().clone();
				for (int j = 0; j < tmpGoodPoint.length; j++) {
					tmpRarity[returnGoodPointToNum(tmpGoodPoint[j])]++;
				}
			}
		}

		String[][] result = new String[18][2];
		System.out.println("評価者数： "+usrSum);
		for (int i = 0; i < tmpRarity.length; i++) {
			//System.out.println(returnNumToGoodPoint(i)+":\t"+(int)(AllGoodPoint[i]*100/otherEvalNum));
			result[i][0] = returnNumToGoodPoint(i);
			result[i][1] = ""+(int)(tmpRarity[i]);
		}

		for (int i = 0; i < tmpRarity.length; i++) {
			int tmpMin = Integer.parseInt(result[i][1]);
			for (int j = i + 1; j < tmpRarity.length; j++) {
				int tmpNow = Integer.parseInt(result[j][1]);
				if(tmpMin > tmpNow){
					String tmpStr0 = result[i][0];
					result[i][0] = result[j][0];
					result[j][0] = tmpStr0;
					String tmpStr1 = result[i][1];
					result[i][1] = result[j][1];
					result[j][1] = tmpStr1;
					tmpMin = tmpNow;
				}
			}
		}

		//ソート
		//System.out.println("ソート");
		String returnResult = result[0][0]+","+result[0][1];
		for (int i = 1; i < tmpRarity.length; i++) {
			System.out.println(result[i][0]+":\t"+result[i][1]);
			returnResult = returnResult +","+ result[i][0]+","+result[i][1];
		}

		return returnResult;
	}

	//自分の正答率算出
	public int calAccuracy(String tmpID){
		int usrSum = 0;
		int accuracySum = 0;
		if(!tmpID.equals(usrID)) {
			return 0;
		}
		else{
			for(int i = 0;i < usrData.size(); i++){
				String tmpFromName = usrData.get(i).getFromName();
				String tmpToName = usrData.get(i).getToName();
				if(tmpToName.trim().equals(usrID.trim()) && tmpFromName.trim().equals(usrID.trim())){

				}
				else if(tmpToName.trim().equals(tmpFromName.trim())){
					usrSum++;
					String[] tmpGoodPoint = usrData.get(i).getGoodPoint().clone();
					for(int j = 0;j < usrData.size(); j++){
						String accurateFromName = usrData.get(j).getFromName();
						String accurateToName = usrData.get(j).getToName();
						if(accurateFromName.trim().equals(usrID)&&accurateToName.trim().equals(tmpToName)){
							String[] accurateGoodPoint = usrData.get(j).getGoodPoint().clone();
							for (int k = 0; k < tmpGoodPoint.length; k++) {
								for (int k2 = 0; k2 < accurateGoodPoint.length; k2++) {
									if(accurateGoodPoint[k2].trim().equals(tmpGoodPoint[k])){
										accuracySum++;
									}

								}
							}
						}
					}
				}
			}
			return (int)((accuracySum *100)/ (usrSum*5));
		}
	}

	//近い人検索
	public String findSimilar(String tmpID){
		int usrSum = 0;
		String[] myGoodPoint = null;
		String[] tmpGoodPoint = null;
		if(!tmpID.equals(usrID)) {
			return "no match ID";
		}
		else{
			for(int i = 0;i < usrData.size(); i++){
				String myFromName = usrData.get(i).getFromName();
				String myToName = usrData.get(i).getToName();
				if(myToName.trim().equals(usrID.trim()) && myFromName.trim().equals(usrID.trim())){
					myGoodPoint = usrData.get(i).getGoodPoint().clone();
				}
				else if(myToName.trim().equals(myFromName.trim())){
					usrSum++;
				}
			}

			String[][] result = new String[usrSum][2];
			for (int i = 0; i < usrSum; i++) {
				result[i][0] = "";
				result[i][0] = "";
			}

			usrSum = 0;
			for(int i = 0;i < usrData.size(); i++){
				String tmpFromName = usrData.get(i).getFromName();
				String tmpToName = usrData.get(i).getToName();
				if(tmpToName.trim().equals(usrID.trim()) && tmpFromName.trim().equals(usrID.trim())){
					myGoodPoint = usrData.get(i).getGoodPoint().clone();
				}
				else if(tmpToName.trim().equals(tmpFromName.trim())){
					int matchGoodPoint = 0;
					tmpGoodPoint = usrData.get(i).getGoodPoint().clone();
					for (int j = 0; j < tmpGoodPoint.length; j++) {
						for (int k = 0; k < myGoodPoint.length; k++) {
							if(myGoodPoint[k].trim().equals(tmpGoodPoint[j])){
								matchGoodPoint++;
							}
						}
					}
					result[usrSum][0] = tmpToName;
					result[usrSum][1] = ""+matchGoodPoint;
					usrSum++;
				}
			}
			for (int i = 0; i < usrSum; i++) {
				int tmpMax = Integer.parseInt(result[i][1]);
				for (int j = i + 1; j < usrSum; j++) {
					int tmpNow = Integer.parseInt(result[j][1]);
					if(tmpMax < tmpNow){
						String tmpStr0 = result[i][0];
						result[i][0] = result[j][0];
						result[j][0] = tmpStr0;
						String tmpStr1 = result[i][1];
						result[i][1] = result[j][1];
						result[j][1] = tmpStr1;
						tmpMax = tmpNow;
					}
				}
			}

			//ソート
			//System.out.println("ソート");
			String returnResult = result[0][0]+","+result[0][1];
			for (int i = 1; i < usrSum; i++) {
				System.out.println(result[i][0]+":\t"+result[i][1]);
				returnResult = returnResult +","+ result[i][0]+","+result[i][1];
			}

			return returnResult;

		}
	}
}
