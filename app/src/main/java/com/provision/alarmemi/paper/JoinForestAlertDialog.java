package com.provision.alarmemi.paper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class JoinForestAlertDialog extends Activity {
	public interface JoinForestAlertDialogListener {
		void onOk(String name, String pw, String pw_chk);

		void onCancel();
	}

	public static JoinForestAlertDialogListener listener = null;
	boolean cancelable;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.join_forest_alert_dialog);
		final EditText name = (EditText) findViewById(R.id.forest_name);
		final EditText pw = (EditText) findViewById(R.id.forest_pw);
		final EditText pw_chk = (EditText) findViewById(R.id.forest_pw_chk);

		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (listener != null)
					listener.onOk(name.getText().toString(), pw.getText()
							.toString(), pw_chk.getText().toString());
				finish();
			}
		});
		findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null)
					listener.onCancel();
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (listener != null)
			listener.onCancel();
	}
}
