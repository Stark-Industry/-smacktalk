package com.app.happy.ui;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.happy.FriendsList;
import com.app.happy.R;
import com.app.happy.Register;
import com.app.happy.R.id;
import com.app.happy.R.layout;
import com.app.happy.util.XmppTool;

public class MainActivity extends Activity {
	
	private static final String TAG = "HappySpringActivity";
	private EditText useridText, pwdText;
	private LinearLayout layout1, layout2;
	private XmppTool xmppTool;
	private XMPPConnection connect;
	private Button register;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("HappySpringActivity");
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.formlogin);
		this.useridText = (EditText) findViewById(R.id.formlogin_userid);
		this.pwdText = (EditText) findViewById(R.id.formlogin_pwd);
		this.layout1 = (LinearLayout) findViewById(R.id.formlogin_layout1);
		this.layout2 = (LinearLayout) findViewById(R.id.formlogin_layout2);

		xmppTool = new XmppTool();
		connect = xmppTool.getConnection();
		if (connect == null) {
			Toast.makeText(MainActivity.this, "connect failure", 0).show();
			finish();
		}
		Button btsave = (Button) findViewById(R.id.formlogin_btsubmit);
		btsave.setOnClickListener(new BtsaveListener());
		Button btcancel = (Button) findViewById(R.id.formlogin_btcancel);
		btcancel.setOnClickListener(new BtcancelListener());
		register = (Button) findViewById(R.id.formlogin_register);
		register.setOnClickListener(new RegisterListener());

	}

	public class RegisterListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			startActivity(new Intent(MainActivity.this, Register.class));

		}

	}

	public class BtsaveListener implements OnClickListener {

		public void onClick(View v) {
			new LoginThread().start();

		}

	}

	public class BtcancelListener implements OnClickListener {

		public void onClick(View v) {
			finish();
		}

	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				layout1.setVisibility(View.VISIBLE);
				layout2.setVisibility(View.GONE);
				break;
			case 2:
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.VISIBLE);
				Toast.makeText(MainActivity.this, "login failure", 0).show();
				break;
			default:
				break;
			}
		};
	};

	public class LoginThread extends Thread {
		final String USERID = useridText.getText().toString();
		final String PWD = pwdText.getText().toString();

		// final String USERID = "lexus3";
		// final String PWD = "1";

		public void run() {
			handler.sendEmptyMessage(1);
			try {
				connect.login(USERID, PWD);
				Presence presence = new Presence(Presence.Type.available);
				connect.sendPacket(presence);
				Intent intent = new Intent(MainActivity.this, FriendsList.class);
				intent.putExtra("USERID", USERID);
				startActivity(intent);
				MainActivity.this.finish();
			} catch (Exception e) {
				Log.e(TAG, "in exception "+ e.getMessage());
				connect.disconnect();
				handler.sendEmptyMessage(2);
			}
		}
	}

}