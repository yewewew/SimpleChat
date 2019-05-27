//https://github.com/yewewew/SimpleChat.git - github 저장소
import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	String [] ban = {"haha","hoho","hehe","huhu","hihi"}; // 금지어 5개 목록
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(test(line) == false){ 
					fw(line); 
				}// test라는 boolean method를 설정하여 금지어가 나오는 순간 false라고 설정하여 금지어를 출력한다. 
				//이 조건문을 통해, 내 서버에서는 경고문이 나타나고 다른 서버에서는 라인이 출력되지 않는다.
				else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else if(line.equals("/userlist")){
					send_userlist();
				}else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			PrintWriter obj = (PrintWriter)hm.get(id);
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if(obj == pw) continue;
				else pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
	public void send_userlist(){
		Object obj = hm.get(id);
		if(obj != null){
			PrintWriter pw = (PrintWriter)obj;
			Set keyset = hm.keySet();
			pw.println("사용자 목록 : "+keyset);
			pw.println("사용자 수 : "+hm.size());
			pw.flush();
		}	
	}//send_userlist - 사용자 목록은 keySet이라는 함수를 이용해 Key값을 받아 출력하였고, 사용자 수는 key값의 사이즈를 받아 출력하였다.

	private boolean test(String msg){
		for(int i = 0; i < ban.length; i++){
			if(msg.contains(ban[i])){
				return false; 
			}
		}return true;
	} // boolean 을 이용하여 금지어 리스트에 있는 단어가 포함되는 순간 false를 리턴한다. 아닐 경우 true를 리턴한다.
	public void fw(String msg){
		Object obj = hm.get(id);
		if(obj != null){
			PrintWriter pw = (PrintWriter)obj;
		 	pw.println("금지어입니다.");
			pw.flush();
		}
	} // fw - 금지어라는 경고문을 서버에 출력한다. 
}