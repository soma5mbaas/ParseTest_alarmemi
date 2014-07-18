package com.provision.alarmemi.paper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.provision.alarmemi.paper.fragments.MainFragment;

public class AlarmContextMenu extends Activity implements OnClickListener {
	int position;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.alarm_context_menu);
		Intent i = getIntent();
		((TextView) findViewById(R.id.header_time)).setText(i
				.getStringExtra("time"));
		String label = i.getStringExtra("label");
		if (label != null && label.length() > 0)
			((TextView) findViewById(R.id.header_label)).setText("(" + label
					+ ")");
		LinearLayout color_layout = (LinearLayout) findViewById(R.id.color);
		ShapeDrawable alarm_drawable = new ShapeDrawable(new OvalShape());
		alarm_drawable.getPaint().setColor(i.getIntExtra("color", -1));
		color_layout.setBackgroundDrawable(alarm_drawable);
		boolean enabled = i.getBooleanExtra("enabled", false);
		boolean isCloud = i.getBooleanExtra("isCloud", false);
		if (enabled) {
			((TextView) findViewById(R.id.enable_alarm))
					.setText(R.string.disable_alarm);
		}
		((ImageView) findViewById(R.id.clock_onoff))
				.setImageResource(isCloud ? (enabled ? R.drawable.alarm_on_cloud
						: R.drawable.alarm_off_cloud)
						: (enabled ? R.drawable.alarm_on : R.drawable.alarm_off));
		position = i.getIntExtra("position", -1);
		findViewById(R.id.enable_alarm).setOnClickListener(this);
		findViewById(R.id.edit_alarm).setOnClickListener(this);
		findViewById(R.id.delete_alarm).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		MainFragment.onContextItemSelected(view.getId(), position);
		finish();
	}
}
