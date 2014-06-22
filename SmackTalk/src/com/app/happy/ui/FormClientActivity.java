package com.app.happy.ui;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.MessageEventManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.app.happy.R;
import com.app.happy.util.ChatContent;
import com.app.happy.util.ChatMsg;
import com.app.happy.util.TimeRender;
import com.app.happy.util.XmppTool;

public class FormClientActivity extends Activity {

	
	private static final String TAG = "FormClientActivity";
	private MyAdapter adapter;
	private ArrayList<ChatMsg> listMsg;
	private ListView listView;
	private String pUSERID;
	private String pTalkerID;
	private String talkMsgList;
	private EditText msgText;
	private XMPPConnection connect;
	private ChatContent chatContent;
	private  Chat newchat  = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.formclient);

		listMsg = new ArrayList<ChatMsg>();
		chatContent = new ChatContent();
		connect = new XmppTool().getConnection();
		Log.d(TAG ,"connect "+connect.isConnected());
		this.msgText = (EditText) findViewById(R.id.formclient_text);
		this.listView = (ListView) findViewById(R.id.formclient_listview);
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		this.adapter = new MyAdapter(this);
		listView.setAdapter(adapter);

		pUSERID = getIntent().getStringExtra("USERID");
		pTalkerID = getIntent().getStringExtra("TALKID");
		talkMsgList = pTalkerID.substring(0, pTalkerID.indexOf("@"));
		listMsg = new ChatContent().getList(talkMsgList);

		System.out.println("talkID " + pTalkerID + "userid : " + pUSERID);

//		 Chat newchat  = null;
		if(connect.isConnected()){
		// message listener
		ChatManager cm = connect.getChatManager();
		newchat = cm.createChat(pTalkerID, null);
//		try {
//			 // Add to the message all the notifications requests (offline, delivered, displayed,
//		      // composing)
////			newchat.sendMessage("3dsa");
//		} catch (XMPPException e) {
//			e.printStackTrace();
//		}

		}else{
			Log.e(TAG, "connect is not connected!");
		}
		
		  
	      // User2 creates a MessageEventManager
//	      MessageEventManager messageEventManager = new MessageEventManager(conn2);
	      // User2 adds the listener that will react to the event notifications requests
//	      messageEventManager.addMessageEventRequestListener(new DefaultMessageEventRequestListener() {
//	          public void deliveredNotificationRequested(
//	              String from,
//	              String packetID,
//	              MessageEventManager messageEventManager) {
//	              super.deliveredNotificationRequested(from, packetID, messageEventManager);
//	              // DefaultMessageEventRequestListener automatically responds that the message was delivered when receives this request
//	              System.out.println("Delivered Notification Requested (" + from + ", " + packetID + ")");
//	          }
//
//	          public void displayedNotificationRequested(
//	              String from,
//	              String packetID,
//	              MessageEventManager messageEventManager) {
//	              super.displayedNotificationRequested(from, packetID, messageEventManager);
//	              // Send to the message's sender that the message was displayed
//	              messageEventManager.sendDisplayedNotification(from, packetID);
//	          }
//
//	          public void composingNotificationRequested(
//	              String from,
//	              String packetID,
//	              MessageEventManager messageEventManager) {
//	              super.composingNotificationRequested(from, packetID, messageEventManager);
//	              // Send to the message's sender that the message's receiver is composing a reply
//	              messageEventManager.sendComposingNotification(from, packetID);
//	          }
//
//	          public void offlineNotificationRequested(
//	              String from,
//	              String packetID,
//	              MessageEventManager messageEventManager) {
//	              super.offlineNotificationRequested(from, packetID, messageEventManager);
//	              // The XMPP server should take care of this request. Do nothing.
//	              System.out.println("Offline Notification Requested (" + from + ", " + packetID + ")");
//	          }
//	      });

		
		
		// send message
		Button btsend = (Button) findViewById(R.id.formclient_btsend);
		btsend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String msg = msgText.getText().toString();
				
				Chat chat = connect.getChatManager().createChat(pTalkerID, msgListener);
				if (msg.length() > 0) {
//					 Message msg = newchat.createMessage();
					Message msg1 =  new Message() ;
				      msg1.setSubject("Any subject you want");
				      msg1.setBody("An interesting body comes here...");
				      
					new ChatContent().addChat(talkMsgList, new ChatMsg(pUSERID,
							msg1.getBody(), TimeRender.getDate(), "OUT"));
					mhandler.sendEmptyMessage(11);
					try {
					  MessageEventManager.addNotificationsRequests(msg1, true, true, true, true);
					  chat.sendMessage(msg);
					} catch (XMPPException e) {
						Log.e(TAG, "send message "+e.getMessage() );
						e.printStackTrace();
					}
				}
				msgText.setText("");
			}
		});
		new RefreshUI().start();
	}

	private Handler mhandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 11:
				listMsg = new ChatContent().getList(talkMsgList);
				adapter.notifyDataSetChanged();
				break;
			case 2:
				break;
			default:
				break;
			}
		};
	};

	
	private MessageListener msgListener  
	 = new MessageListener()  
	        {  

				@Override
				public void processMessage(Chat chat, Message message)  
				{	
					if (message != null && message.getBody() != null)  
					{  
						System.out.println("收到消息:" + message.getBody()+ "error: "+message.getError().getMessage());  
						// 可以在这进行针对这个用户消息的处理，但是这里我没做操作，看后边聊天窗口的控制  
					}  
				}  
	        };  
	
	public class RefreshUI extends Thread {

		public void run() {
			while (true) {
				mhandler.sendEmptyMessage(11);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class MyAdapter extends BaseAdapter {

		private Context cxt;
		private LayoutInflater inflater;

		public MyAdapter(FormClientActivity formClient) {
			this.cxt = formClient;
		}

		@Override
		public int getCount() {
			if (listMsg != null) {
				return listMsg.size();
			}
			return -1;
		}

		@Override
		public Object getItem(int position) {
			if (listMsg != null) {
				return listMsg.get(position);
			}
			return -1;
		}

		@Override
		public long getItemId(int position) {
			if (listMsg != null) {
				return position;
			}
			return -1;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			this.inflater = (LayoutInflater) this.cxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (listMsg.get(position).from.equals("IN")) {
				convertView = this.inflater.inflate(
						R.layout.formclient_chat_in, null);
			} else {
				convertView = this.inflater.inflate(
						R.layout.formclient_chat_out, null);
			}
			TextView useridView = (TextView) convertView
					.findViewById(R.id.formclient_row_userid);
			TextView dateView = (TextView) convertView
					.findViewById(R.id.formclient_row_date);
			TextView msgView = (TextView) convertView
					.findViewById(R.id.formclient_row_msg);
			useridView.setText(listMsg.get(position).userid);
			dateView.setText(listMsg.get(position).date);
			msgView.setText(listMsg.get(position).msg);
			return convertView;
		}
	}
}