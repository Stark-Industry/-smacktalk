package com.app.happy.util;

import javax.net.SocketFactory;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;
import android.util.Log;

import com.app.constant.ServerSetting;

/**
 * @author Sam.Io
 * @time 2011/11/18
 * @project AdXmpp
 */
public class XmppTool {

	private static final String TAG = "XmppTool";

	private static XMPPConnection mCon = null;

	private ConnectionConfiguration mConnConfig = null;

	private void initConnConfig(){
		mConnConfig = new ConnectionConfiguration(ServerSetting.HOST, ServerSetting.PORT);
		/** 是否启用压缩 */
//		mConnConfig.setCompressionEnabled(true);
		/** 是否启用安全验证 */
		mConnConfig.setSASLAuthenticationEnabled(true);
		// mConnConfig.setReconnectionAllowed(true);
		// mConnConfig.setRosterLoadedAtLogin(true);
//		/** 是
//		 * 否启用调试 */
//		mConnConfig.setDebuggerEnabled(false);
	}
	
	public XmppTool() {
		if (mCon == null) {
			try {
				Log.d(TAG, "begin to connect to server...");
				configure(ProviderManager.getInstance());
				initConnConfig();
				mCon = new XMPPConnection(mConnConfig);
//				mCon.addConnectionListener(new ConnectionListener() {
//
//					@Override
//					public void reconnectionSuccessful() {
//						Log.d(TAG, "reconnectionSuccessful");
//					}
//
//					@Override
//					public void reconnectionFailed(Exception arg0) {
//						Log.d(TAG, "reconnectionFailed");
//					}
//
//					@Override
//					public void reconnectingIn(int arg0) {
//						Log.d(TAG, "reconnectingIn");
//					}
//
//					@Override
//					public void connectionClosedOnError(Exception arg0) {
//						Log.d(TAG, "connectionClosedOnError");
//					}
//
//					@Override
//					public void connectionClosed() {
//						Log.d(TAG, "connectionClosed");
//					}
//				});
				mCon.DEBUG_ENABLED = true;
				mCon.connect();
			} catch (XMPPException xe) {
				Log.e(TAG, "connect error :"+xe.getMessage() );
				xe.printStackTrace();
			}
			Log.d(TAG, mCon.getConnectionID());
		}
	}

	public XMPPConnection getConnection() {
		
		if(mCon == null){
			new XmppTool();
		}
		if (!mCon.isConnected()) {
		 mCon = new XMPPConnection(mConnConfig);;
		} else {
		}
		Log.d(TAG, "get connection"+ mCon.isConnected() );
		return mCon;
	}

	public static void closeConnection() {
		if (mCon != null) {
			mCon.disconnect();
		}
		mCon = null;
	}

	public void configure(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses", "http://jabber.org/protocol/address", new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.SessionExpiredError());
	}

	public static void closeConnect() {
		if (mCon != null) {
			mCon.disconnect();
			mCon = null;
		}
	}

	/**
	 * <b>function:</b> ConnectionConfiguration 的基本配置相关信息
	 */
	public void testConfig() {
		Log.d(TAG, String.format("PKCS11Library: " + mConnConfig.getPKCS11Library()));
		Log.d(TAG, String.format("ServiceName: %s", mConnConfig.getServiceName()));
		// ssl证书密码
		Log.d(TAG, String.format("TruststorePassword: %s", mConnConfig.getTruststorePassword()));
		Log.d(TAG, String.format("TruststorePath: %s", mConnConfig.getTruststorePath()));
		Log.d(TAG, String.format("TruststoreType: %s", mConnConfig.getTruststoreType()));

		SocketFactory socketFactory = mConnConfig.getSocketFactory();
		Log.d(TAG, String.format("SocketFactory: %s", socketFactory) );
		/*
		 * try { fail("createSocket: {0}",
		 * socketFactory.createSocket("localhost", 3333)); } catch (IOException
		 * e) { e.printStackTrace(); }
		 */
	}
	
//	  public void testUser() {
//	        try {
//	            /** 用户登陆，用户名、密码 */
//	            connection.login("hoojo", "hoojo");
//	        } catch (XMPPException e) {
//	            e.printStackTrace();
//	        }
//	        /** 获取当前登陆用户 */
//	        fail("User:", connection.getUser());
//	        
//	        /** 所有用户组 */
//	        Roster roster = connection.getRoster();
//	        
//	        /** 好友用户组，你可以用Spark添加用户好友，这样这里就可以查询到相关的数据 */
//	        Collection<RosterEntry> rosterEntiry = roster.getEntries();
//	        Iterator<RosterEntry> iter = rosterEntiry.iterator();
//	        while (iter.hasNext()) {
//	            RosterEntry entry = iter.next();
//	            fail("Groups: {0}, Name: {1}, Status: {2}, Type: {3}, User: {4}", entry.getGroups(), entry.getName(), entry.getStatus(), entry.getType(), entry);
//	        }
//	        
//	        fail("-------------------------------");
//	        /** 未处理、验证好友，添加过的好友，没有得到对方同意 */
//	        Collection<RosterEntry> unfiledEntries = roster.getUnfiledEntries();
//	        iter = unfiledEntries.iterator();
//	        while (iter.hasNext()) {
//	            RosterEntry entry = iter.next();
//	            fail("Groups: {0}, Name: {1}, Status: {2}, Type: {3}, User: {4}", entry.getGroups(), entry.getName(), entry.getStatus(), entry.getType(), entry);
//	        }
//	    }
//	
//	  public void testPacket() {
//	        try {
//	            connection.login("hoojo", "hoojo");
//	        } catch (XMPPException e) {
//	            e.printStackTrace();
//	        }
//	        
//	        //Packet packet = new Data(new DataPacketExtension("jojo@" + server, 2, "this is a message"));
//	        //connection.sendPacket(packet);
//	        
//	        /** 更改用户状态，available=true表示在线，false表示离线，status状态签名；当你登陆后，在Spark客户端软件中就可以看到你登陆的状态 */
//	        Presence presence = new Presence(Presence.Type.available);
//	        presence.setStatus("Q我吧");
//	        connection.sendPacket(presence);
//	        
//	        Session session = new Session();
//	        String sessid = session.nextID();
//	        connection.sendPacket(session);
//	        /** 向jojo@192.168.8.32 发送聊天消息，此时你需要用Spark软件登陆jojo这个用户，
//	         * 这样代码就可以向jojo这个用户发送聊天消息，Spark登陆的jojo用户就可以接收到消息
//	         **/
//	        /** Type.chat 表示聊天，groupchat多人聊天，error错误，headline在线用户； */
//	        Message message = new Message("jojo@" + server, Type.chat);
//	        //Message message = new Message(sessid, Type.chat);
//	        message.setBody("h!~ jojo, I'am is hoojo!");
//	        connection.sendPacket(message);
//	        
//	        try {
//	            Thread.sleep(1);
//	        } catch (InterruptedException e) {
//	            e.printStackTrace();
//	        }
//	    }

//	OfflineMessageManager offlineManager = new OfflineMessageManager(  
//            mCon);  
//    try {  
//        Iterator<org.jivesoftware.smack.packet.Message> it = offlineManager  
//                .getMessages();  
//
//        System.out.println(offlineManager.supportsFlexibleRetrieval());  
//        System.out.println("离线消息数量: " + offlineManager.getMessageCount());  
//
//          
//        Map<String,ArrayList<Message>> offlineMsgs = new HashMap<String,ArrayList<Message>>();  
//          
//        while (it.hasNext()) {  
//            org.jivesoftware.smack.packet.Message message = it.next();  
//            System.out  
//                    .println("收到离线消息, Received from 【" + message.getFrom()  
//                            + "】 message: " + message.getBody());  
//            String fromUser = message.getFrom().split("/")[0];  
//
//            if(offlineMsgs.containsKey(fromUser))  
//            {  
//                offlineMsgs.get(fromUser).add(message);  
//            }else{  
//                ArrayList<Message> temp = new ArrayList<Message>();  
//                temp.add(message);  
//                offlineMsgs.put(fromUser, temp);  
//            }  
//        }  
//
//        //在这里进行处理离线消息集合......  
//        Set<String> keys = offlineMsgs.keySet();  
//        Iterator<String> offIt = keys.iterator();  
//        while(offIt.hasNext())  
//        {  
//            String key = offIt.next();  
//            ArrayList<Message> ms = offlineMsgs.get(key);  
//            TelFrame tel = new TelFrame(key);  
//            ChatFrameThread cft = new ChatFrameThread(key, null);  
//            cft.setTel(tel);  
//            cft.start();  
//            for (int i = 0; i < ms.size(); i++) {  
//                tel.messageReceiveHandler(ms.get(i));  
//            }  
//        }  
//          
//          
//        offlineManager.deleteMessages();  
//    } catch (Exception e) {  
//        e.printStackTrace();  
//    }  
	
	
	
}
