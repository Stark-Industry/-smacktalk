package com.app.happy;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;

import com.app.happy.util.XmppTool;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class Register extends Activity {

	private XMPPConnection connect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		connect = new XmppTool().getConnection();

		Registration reg = new Registration();
		reg.setType(IQ.Type.SET);
		reg.setTo(connect.getServiceName());
		reg.setUsername("lexus18");// username.getText().toString());
		reg.setPassword("1");// password.getText().toString());
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
			Toast.makeText(getApplicationContext(), "������û�з��ؽ��", Toast.LENGTH_LONG)
					.show();
		} else if (result.getType() == IQ.Type.ERROR) {
			if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
				Toast.makeText(getApplicationContext(), "����˺��Ѿ�����", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(getApplicationContext(), "��ϲ�㣬ע��ʧ��", Toast.LENGTH_LONG)
						.show();
			}
		} else if (result.getType() == IQ.Type.RESULT) {
			System.out.println("good");
			Toast.makeText(getApplicationContext(), "Ԥף�������", Toast.LENGTH_LONG)
					.show();
		}

	}

}
