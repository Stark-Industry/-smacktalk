package com.app.happy.ui;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.happy.R;
import com.app.happy.util.XmppTool;

public class RegisterActivity extends Activity {


private XMPPConnection connect;
private EditText useridText, pwdText,pwdConfirmText;
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.formreg);
	Button btn_reg=(Button)findViewById(R.id.formlogin_register);
	btn_reg.setOnClickListener(new BtregListener());
	useridText=(EditText)findViewById(R.id.formlogin_userid);
	pwdText=(EditText)findViewById(R.id.formlogin_pwd);
	pwdConfirmText=(EditText)findViewById(R.id.formlogin_pwd_confirm);
	connect = new XmppTool().getConnection();
	if (connect == null) {
		Toast.makeText(RegisterActivity.this, "connect failure", 0)
				.show();
		finish();
	}
	

}
public class BtregListener implements OnClickListener {


	public void onClick(View v) {
		// TODO Auto-generated method stub
		new RegThread().start();

	}

}
private Handler handler = new Handler() {
	public void handleMessage(android.os.Message msg) {
		switch (msg.what) {
		case 1:
			Toast.makeText(RegisterActivity.this, "register succsed", 0)
			.show();
			break;
		case 2:
			Toast.makeText(RegisterActivity.this, "no data from server", 0)
					.show();
			break;
		case 3:
			Toast.makeText(RegisterActivity.this, "account exists", 0).show();
			break;
		case 4:
			Toast.makeText(RegisterActivity.this, "register failed", 0)
					.show();
			break;
		default:
			break;
		}
	}
};

public class RegThread extends Thread {
	final String USERID =  useridText.getText().toString();
	final String PWD = pwdText.getText().toString();
	final String PWDCONFIRM=pwdConfirmText.getText().toString();
//	final String USERID =  "lexus3";
//	final String PWD =  "1";
	@Override
	public void run() {
		//handler.sendEmptyMessage(1);
		try {
			if(PWD.equalsIgnoreCase(PWDCONFIRM))
			{
				Registration reg = new Registration();
				reg.setType(IQ.Type.SET);
				reg.setTo(connect.getServiceName());
				reg.setUsername(USERID);// username.getText().toString());
				reg.setPassword(PWD);// password.getText().toString());
				reg.addAttribute("android", "geolo_createUser_android");
				System.out.println("reg:" + reg);
				PacketFilter filter = new AndFilter(new PacketIDFilter(
						reg.getPacketID()), new PacketTypeFilter(IQ.class));
				PacketCollector collector = connect.createPacketCollector(filter);
				connect.sendPacket(reg);

				IQ result = (IQ) collector.nextResult(SmackConfiguration
						.getPacketReplyTimeout());
				// Stop queuing results
				collector.cancel();
				if (result == null) {
					/*Toast.makeText(Register.this, "ؾϱǷûԐ׵ܘޡڻ", Toast.LENGTH_LONG)
							.show();*/
					Log.d("ruan", "no result from server");
					handler.sendEmptyMessage(2);
				} else if (result.getType() == IQ.Type.ERROR) {
					if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
						/*Toast.makeText(Register.this, "֢ٶ֋ۅӑޭզ՚", Toast.LENGTH_LONG)
								.show();*/
						Log.d("ruan", "account exists");
						handler.sendEmptyMessage(3);
					} else {
						/*Toast.makeText(Register.this, "٧ϲţìעӡʧќ", Toast.LENGTH_LONG)
								.show();*/
						Log.d("ruan", "account register failed");
						handler.sendEmptyMessage(4);
					}
				} else if (result.getType() == IQ.Type.RESULT) {
					System.out.println("good");
					/*Toast.makeText(Register.this, "ԤףͬԤࠬ", Toast.LENGTH_LONG)
							.show();*/
					Log.d("ruan", "account register succsed");
					handler.sendEmptyMessage(1);
				}
			}
			else {
				/*Toast.makeText(Register.this, "pwd not same", 0).show();*/
			}
			
		} catch (Exception e) {
			/*Toast.makeText(Register.this, "register failed", Toast.LENGTH_LONG).show();*/
			Log.d("ruan", "account register failed");
			handler.sendEmptyMessage(3);
			connect.disconnect();
		}
	}
}
}