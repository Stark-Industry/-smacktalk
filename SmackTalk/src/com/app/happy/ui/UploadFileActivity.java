package com.app.happy.ui;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.happy.FriendsList;
import com.app.happy.R;
import com.app.happy.util.FileUtils;
import com.app.happy.util.XmppTool;

public class UploadFileActivity extends Activity {

	private static final String TAG = "UploadFileActivity";
	
	public static final int FILE_SELECT_CODE = 0;
	private Button btn_selectFile;
	private Button btn_uploadFile;
	private Button btn_friendList;
	private String selectedFile = null;
	private TextView txt_uploadFile;
	private XMPPConnection con;
	private String user;
	private  FileTransferManager manager = null;
	
//	private java.io.File saveFile = null;
//	private static final String receiveFileDir = "receive";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.formupload);
		btn_uploadFile = (Button) findViewById(R.id.btn_uploadFile);
		btn_selectFile = (Button) findViewById(R.id.btn_selectFile);
		txt_uploadFile = (TextView) findViewById(R.id.txt_uploadFile);
		btn_friendList = (Button) findViewById(R.id.btn_friendList);
		
 		if(getIntent()!=null){
			user =getIntent().getStringExtra("USERID");
			Log.d(TAG, "current user is "+user);
			con = new XmppTool().getConnection();
			String user1 = con.getUser();
			Log.d(TAG, "c user is "+user1); //  yu@yu-pc/Smack 
		}
 		try {
 			manager = new FileTransferManager(con);
 			manager.addFileTransferListener(new RecFileTransferListener(this));
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		
		btn_selectFile.setOnClickListener(new View.OnClickListener() {
	    
			@Override
			public void onClick(View arg0) {
				showFileChooser();
			}
		});
		
		btn_friendList.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(UploadFileActivity.this, FriendsList.class);
				startActivity(intent);
			}
		});
		
		
		btn_uploadFile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, "upload file");
				if(!selectedFile.isEmpty()){
					try {
						sendFile(con, "ruan@yu-pc/Smack", new java.io.File(selectedFile) , UploadFileActivity.this);
					} catch (XMPPException e) {
						Log.e(TAG, "send file XMPPException "+e.getMessage());
						e.printStackTrace();
					} catch (Exception e) {
						Log.e(TAG, "send file error "+e.getMessage());
						e.printStackTrace();
					}
				}else{
					Toast.makeText(UploadFileActivity.this, "upload file is null", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		 XmppTool.closeConnection();
	}
	
	
	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		try {
			startActivityForResult(Intent.createChooser(intent, "Please Select a File to Upload"), FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
	    switch (requestCode) {
	        case FILE_SELECT_CODE:      
	        if (resultCode == RESULT_OK) {  
	            // Get the Uri of the selected file 
	            Uri uri = data.getData();
	           selectedFile = FileUtils.getPath(this, uri);
	            txt_uploadFile.setText(selectedFile);
	            btn_uploadFile.setVisibility(View.VISIBLE);
	        }           
	        break;
	    }
	super.onActivityResult(requestCode, resultCode, data);
	}

	public static void sendFile(XMPPConnection connection,  
            String user, java.io.File file, Context context) throws XMPPException, InterruptedException {  
//		 yu@yu-pc/Smack 
        System.out.println("发送文件开始"+file.getName());  
        final FileTransferManager manager=	new FileTransferManager(connection);  
        
       // Create the listener
       manager.addFileTransferListener(new RecFileTransferListener(context));
                   // Check to see if the request should be accepted
//                   if(shouldAccept(request)) {
//                         // Accept it
//                         IncomingFileTransfer transfer = request.accept();
//                         transfer.recieveFile(new File("shakespeare_complete_works.txt"));
//                   } else {
//                         // Reject it
//                         request.reject();
//                   }
       
        System.out.println("发送文件给: "+user);  
//        其中的userID包含三部分，.
//        A fully-qualified jabber ID consists of a node, a domain, and a resource, 
//        the user must be connected to the resource in order to be able to recieve the file transfer。
//        其实也可以说两部分就是xmpp地址+对方客户端
        OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(connection.getUser());
       
        try{
        transfer.sendFile(file, file.getName()); 
        while(!transfer.isDone()) {
        	if(transfer.getStatus().equals(Status.error)) {
        		System.out.println("ERROR!!! " + transfer.getError());
        	} else {
        		System.out.println(transfer.getStatus());
        		System.out.println(transfer.getProgress());
        	}
        	Thread.currentThread().sleep(1000);
        }
         // yu@yu-pc/Smack 
        }catch(Exception e){
        	 Toast.makeText(context, "发送失败", Toast.LENGTH_SHORT).show();
        	 Log.e(TAG, "发送失败 :"+e.getMessage());
        }finally{
        	 if(transfer.getStatus().equals(Status.error)){  
        		 Toast.makeText(context, "发送失败", Toast.LENGTH_SHORT).show();
            	 Log.e(TAG, "发送失败 ");
//                 ChatMessage.append(dateUtils.getHM()+"  自己: 传输出错"+"\n");  
             }else if(transfer.getStatus().equals(Status.complete)){  
            	 Toast.makeText(context, "发送成功", Toast.LENGTH_SHORT).show();
            	 Log.e(TAG, "发送成功 ");
//                  ChatMessage.append(dateUtils.getHM()+"  自己: "+fileName+" 传输完成\n");  
             }else if(transfer.getStatus().equals(Status.cancelled)){  
            	 Toast.makeText(context, "取消发送", Toast.LENGTH_SHORT).show();
            	 Log.e(TAG, "取消发送 ");
//                  ChatMessage.append(dateUtils.getHM()+frindsXmppAddress+"  "+fileName+" 传输取消\n");  
             }  
        }
//        System.out.println("//////////");  
//        System.out.println(transfer.getStatus());  
//        System.out.println(transfer.getProgress());  
//        System.out.println(transfer.isDone());  
//        System.out.println("//////////");  
        System.out.println("发送文件结束");  
    } 
	
	
//	public class SendFileTransferListener implements FileTransferListener{
//
//		@Override
//		public void fileTransferRequest(FileTransferRequest request) {
//			Log.d(TAG, request.getFileName());
//			Log.d(TAG, request.getMimeType());
//			Log.d(TAG, request.getFileSize()+"");
//			 // Check to see if the request should be accepted
////            if(shouldAccept(request)) {
////                  // Accept it
////                  IncomingFileTransfer transfer = request.accept();
////                  transfer.recieveFile(new File("shakespeare_complete_works.txt"));
////            } else {
////                  // Reject it
////                  request.reject();
////            }
//			
//		}
		
	}
	
	
	 class RecFileTransferListener implements FileTransferListener {  
		 
		 Context mContext;
		 
		 public RecFileTransferListener(Context context) {
			 super();
			 mContext = context;
		}
		 
		 private static final String receiveFileDir = "receive";
	    public String getFileType(String fileFullName)  
	    {  
	        if(fileFullName.contains("."))  
	        {  
	            return "."+fileFullName.split("//.")[1];  
	        }else{  
	            return fileFullName;  
	        }  
	          
	    }

		@Override
		public void fileTransferRequest(FileTransferRequest request) {
			
			Toast.makeText(mContext, "接收文件开始.....", Toast.LENGTH_SHORT).show();
			
			
		        final IncomingFileTransfer inTransfer = request.accept();  
		        final String fileName = request.getFileName();  
		        long length = request.getFileSize();   
		        final String fromUser = request.getRequestor().split("/")[0];  
		        System.out.println("文件大小:"+length + "  "+request.getRequestor());  
		        System.out.println(""+request.getMimeType());  
		        java.io.File externalFile = Environment.getExternalStorageDirectory();
		         java.io.File saveFile = null;
//					new AlertDialog.Builder(mContext) 
//					.setTitle(String.format("来自[%s]", request.getRequestor()) )
//					.setMessage(String.format("文件名: %s, 文件大小：%s\n, 确定吗？\n", fileName,length) )
//					.setPositiveButton("是", new DialogInterface.OnClickListener() {  
                 
//public void onClick(DialogInterface dialog, int which) {  
//dialog.dismiss();  
//}  
//} )
//					.setNegativeButton("否", null)
//					.show();
//		         
					
		        if(externalFile!=null){
		        	
		        	saveFile = new java.io.File(externalFile, receiveFileDir );
		        	 Log.d("UploadFileActivity", "external file Dir: "+externalFile + " save file"+saveFile);
		        	if(!saveFile.exists()){
		        		saveFile.mkdir();
		        	}
		        }else{
		        Toast.makeText(mContext, "doesn't exist sd card", Toast.LENGTH_SHORT).show();
			       java.io.File fileDirFile =  mContext.getFilesDir();
			        saveFile = new java.io.File(fileDirFile, receiveFileDir ); 
			        Log.d("UploadFileActivity", "file Dir: "+fileDirFile + "save file"+saveFile);
		        }
//		        Message message = new Message();  
//                message.setFrom(fromUser);  
//                message.setProperty("REC_SIGN", "SUCCESS");  
//                message.setBody("["+fromUser+"]发送文件: "+fileName+"/r/n"+"存储位置: "+file.getAbsolutePath()+ getFileType(fileName)); 
		        System.out.println("接收文件开始.....");  
		        try {
					inTransfer.recieveFile(new java.io.File(saveFile, fileName) );
					 Toast.makeText(mContext, "接收成功!", Toast.LENGTH_SHORT).show();
				} catch (XMPPException e) {
					  Toast.makeText(mContext, "接收失败!",
						      Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				} 
		        System.out.println("接收文件结束.....");  
		        }
//                        + getFileType(fileName)));  
//		        try {   
//		            JFileChooser chooser = new JFileChooser();   
//		            chooser.setCurrentDirectory(new File("."));   
//		            int result = chooser.showOpenDialog(null);  
//		            if(result==JFileChooser.APPROVE_OPTION)  
//		            {  
//		                final File file = chooser.getSelectedFile();  
//		                System.out.println(file.getAbsolutePath());  
//		                    new Thread(){  
//		                        public void run()  
//		                        {  
//		                        try {  
//		  
//		                            System.out.println("接受文件: " + fileName);  
//		                            inTransfer  
//		                                    .recieveFile(new File(file  
//		                                            .getAbsolutePath()  
//		                                            + getFileType(fileName)));  
//		  
//		                            Message message = new Message();  
//		                            message.setFrom(fromUser);  
//		                            message.setProperty("REC_SIGN", "SUCCESS");  
//		                            message.setBody("["+fromUser+"]发送文件: "+fileName+"/r/n"+"存储位置: "+file.getAbsolutePath()+ getFileType(fileName));  
//		                            if (Client.isChatExist(fromUser)) {  
//		                                Client.getChatRoom(fromUser).messageReceiveHandler(  
//		                                        message);  
//		                            } else {  
//		                                ChatFrameThread cft = new ChatFrameThread(  
//		                                        fromUser, message);  
//		                                cft.start();  
//		                                  
//		                            }  
//		                        } catch (Exception e2) {  
//		                            e2.printStackTrace();  
//		                        }  
//		                        }  
//		                    }.start();  
//		            }else{  
//		                  
//		                System.out.println("拒绝接受文件: "+fileName);  
//		                  
//		                request.reject();  
//		                Message message = new Message();  
//		                message.setFrom(fromUser);  
//		                message.setBody("拒绝"+fromUser+"发送文件: "+fileName);  
//		                message.setProperty("REC_SIGN", "REJECT");  
//		                if (Client.isChatExist(fromUser)) {  
//		                    Client.getChatRoom(fromUser)  
//		                            .messageReceiveHandler(message);  
//		                } else {  
//		                    ChatFrameThread cft = new ChatFrameThread(  
//		                            fromUser, message);  
//		                    cft.start();  
//		                }  
//		            }  
		            /* InputStream in = inTransfer.recieveFile(); 
		             String fileName = "r"+inTransfer.getFileName(); 
		             OutputStream out = new FileOutputStream(new File("d:/receive/"+fileName)); 
		             byte[] b = new byte[512]; 
		             while(in.read(b) != -1) 
		             { 
		                 out.write(b); 
		                 out.flush(); 
		             } 
		              
		             in.close(); 
		             out.close();*/  
//		        } catch (Exception e) {  
//		            e.printStackTrace();  
//		        }  
//		          
		       
		  
//		    }  
//		}
		
		
		
		
	}
	
	 
	 
	 
	 
	 
//}