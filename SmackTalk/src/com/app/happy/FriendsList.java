package com.app.happy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.app.constant.ServerSetting;
import com.app.happy.ui.FormClientActivity;
import com.app.happy.util.ChatContent;
import com.app.happy.util.ChatMsg;
import com.app.happy.util.TimeRender;
import com.app.happy.util.XmppTool;

public class FriendsList extends Activity {

	private XMPPConnection connect;
	private ListView list;
	private ImageView chatImage;
	private String useName;
	private ArrayList<HashMap<String, Object>> listItem;
	private ArrayList<String> nameList;
	private ArrayList<String> rosterList;
	private String temp;
	private SimpleAdapter listItemAdapter;
	private android.os.Message msg;
	private ChatContent cContent;
	private ChatMsg cMsg;
	private ChatManager cm;
	private CmChatListener cmListener;
	private Roster roster;
	private Collection<RosterEntry> entries;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.formfriend_list);

		setTitle("Happy Every Day");
		useName = getIntent().getStringExtra("USERID") + "@" + ServerSetting.HOST;
		connect = new XmppTool().getConnection();
		cContent = new ChatContent();
		list = (ListView) findViewById(R.id.ListView01);
		cmListener = new CmChatListener();
		// ��ɶ�̬���飬�������
		listItem = new ArrayList<HashMap<String, Object>>();
		nameList = new ArrayList<String>();
		rosterList = new ArrayList<String>();

		roster = connect.getRoster();
		roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

		myGetRost();

		// �����������Item�Ͷ�̬�����Ӧ��Ԫ��
		listItemAdapter = new SimpleAdapter(this, listItem,// ���Դ
				R.layout.friend_adapter,// ListItem��XMLFʵ��
				// ��̬������ImageItem��Ӧ������
				new String[] { "ItemImage", "ItemTitle", "ItemText" },
				// ImageItem��XML�ļ������һ��ImageView,����TextView ID
				new int[] { R.id.ItemImage, R.id.ItemTitle, R.id.ItemText });

		// ��Ӳ�����ʾ
		list.setAdapter(listItemAdapter);

		// ��ӵ��
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				chatImage = (ImageView) arg1.findViewById(R.id.chatSuggest);
				chatImage.setVisibility(View.GONE);
				TextView myText = (TextView) arg1.findViewById(R.id.ItemText);
				System.out.println(myText.getText().toString());
				Intent mIntent = new Intent();
				mIntent.putExtra("TALKID", myText.getText().toString());
				mIntent.putExtra("USERID", useName);
				mIntent.setClass(FriendsList.this, FormClientActivity.class);
				startActivity(mIntent);
			}
		});

		// ��ӳ������
		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("�����˵�-ContextMenu");
				menu.add(0, 0, 0, "���������˵�0");
				menu.add(0, 1, 0, "���������˵�1");
			}
		});

		cm = connect.getChatManager();
		cm.addChatListener(cmListener);
		roster.addRosterListener(new QuickRosterListener());
	}

	public void myCreateRost(String name) {
		if (name.indexOf("@") == -1) {
			name = name + "@" + ServerSetting.HOST;
		}
		try {
			roster.createEntry(name, null, new String[] { "Friends" });
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getAllUsers() {
		try {
			UserSearchManager search = new UserSearchManager(connect);
			String searchf = "search." + ServerSetting.HOST;
			Form searchForm = search.getSearchForm(searchf);
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("search", "lexus");
			ReportedData data = search.getSearchResults(answerForm, searchf);

			Iterator<Row> it = data.getRows();
			Row row = null;
			String ansS = "";
			while (it.hasNext()) {
				row = it.next();
				ansS = row.getValues("Username").next().toString();
				if (!rosterList.contains(ansS)) {
					myCreateRost(ansS);
				}
				Log.d("x", row.getValues("Username").next().toString());
			}
		} catch (Exception e) {
			Toast.makeText(this,
					e.getMessage() + " " + e.getClass().toString(),
					Toast.LENGTH_LONG).show();
		}

	}

	public void myAddItem(String title, String text) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ItemTitle", title);
		rosterList.add(title);
		map.put("ItemText", text);
		map.put("ItemImage", R.drawable.folder);
		listItem.add(map);
	}

	public void myGetRost() {
		// TODO Auto-generated method stub
		// ��ȡ�����

		entries = roster.getEntries();
		listItem.clear();
		rosterList.clear();
		String rName;
		int i = 0;
		for (RosterEntry entry : entries) {
			temp = entry.getUser();
			System.out.println(temp + "group" + entry.getGroups());
			i = temp.indexOf("@");
			if (i == -1) {
				System.out.println("is wrong");
				return;
			}
			rName = temp.substring(0, i);
			if (!temp.equals(useName)) {
				myAddItem(rName, temp);
			}
		}
	}

	public class CmChatListener implements ChatManagerListener {
		String chatName;

		@Override
		public void chatCreated(Chat chat, boolean arg1) {
			// TODO Auto-generated method stub
			chat.addMessageListener(new MessageListener() {
				@Override
				public void processMessage(Chat chat, Message message) {
					if (message.getBody() != null) {
						System.out.println(" 2: Received from ��"
								+ message.getFrom() + "�� message: "
								+ message.getBody());
						temp = message.getFrom();
						chatName = temp.substring(0, temp.indexOf("@"));
						if (rosterList.contains(chatImage)) {
							msg = handler.obtainMessage();
							msg.what = 1;
							msg.obj = chatName;
							msg.sendToTarget();

							cContent.putMap();
							System.out.println("today " + temp);
							cContent.putChat(chatName);
							cMsg = new ChatMsg(chatName, message.getBody(),
									TimeRender.getDate(), "IN");
							cContent.addChat(chatName, cMsg);
							cContent.putChat(chatName);
							cContent.putMap();
						} else {
							myCreateRost(temp);
						}
					}
				}
			});
		}
	}

	public class QuickRosterListener implements RosterListener {

		@Override
		public void entriesAdded(Collection<String> arg0) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(2);
		}

		@Override
		public void entriesDeleted(Collection<String> arg0) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(2);
		}

		@Override
		public void entriesUpdated(Collection<String> arg0) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(2);

		}

		@Override
		public void presenceChanged(Presence arg0) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(2);
		}

	}

	// �Ƴ���ճ���ռ�е���Դ
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		new XmppTool().closeConnection();
		System.exit(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, 1, 1, "��������");
		menu.add(0, 2, 2, "ˢ���б�");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) {
			final EditText diaEdit = new EditText(this);
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle("������")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(diaEdit)
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									temp = diaEdit.getText().toString();
									myCreateRost(temp);
								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();

			dialog.show();

		} else if (item.getItemId() == 2) {
			getAllUsers();
		}
		return true;
	}

	public int getChildItem(String str) {

		int i = rosterList.indexOf(str);
		return i > 0 ? i : -1;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				temp = (String) msg.obj;
				int i = getChildItem(temp);
				if (i == -1) {
					return;
				}
				chatImage = (ImageView) list.getChildAt(i).findViewById(
						R.id.chatSuggest);
				chatImage.setVisibility(View.VISIBLE);
				setTitle("dear, you have the new message");
				break;
			case 2:
				myGetRost();
				System.out.println("dasfdfsdfsdfsdf in listener3");
				listItemAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};
	
//	public static void sendFile(XMPPConnection connection,  
//            String user, File file) throws XMPPException, InterruptedException {  
//          
//        System.out.println("发送文件开始"+file.getName());  
//        FileTransferManager transfer = new FileTransferManager(Client.getConnection());  
//        System.out.println("发送文件给: "+user+Client.getServiceNameWithPre());  
//        OutgoingFileTransfer out = transfer.createOutgoingFileTransfer(user+Client.getServiceNameWithPre()+"/Smack");//  
//          
//        out.sendFile(file, file.getName());  
//          
//        System.out.println("//////////");  
//        System.out.println(out.getStatus());  
//        System.out.println(out.getProgress());  
//        System.out.println(out.isDone());  
//          
//        System.out.println("//////////");  
//          
//        System.out.println("发送文件结束");  
//    }  

}
