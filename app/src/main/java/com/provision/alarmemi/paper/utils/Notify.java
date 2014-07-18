package com.provision.alarmemi.paper.utils;

import android.content.Context;

import com.provision.alarmemi.paper.R;

public class Notify {
	public final static String ALARM_ADD = "alarmadd:";
	public final static String ALARM_EDIT = "alarmedit:";
	public final static String ALARM_REMOVE = "alarmremove:";
	public final static String MEMBER_ADD = "memberadd:";
	public final static String MEMBER_REMOVE = "memberremove:";
	public final static String FOREST_INVITE = "forestinvite:";
	public final static String[] MESSAGES = { ALARM_ADD, ALARM_EDIT, ALARM_REMOVE,
			MEMBER_ADD, MEMBER_REMOVE, FOREST_INVITE };


	public static String getString(Context context, String message) {
		try {
			if (message.startsWith(MEMBER_ADD)) {
				String proc1[] = message.split(":");
				String proc2[] = proc1[1].split("\\|");
				return String.format(context.getString(R.string.format_member_add), proc2[0],
						proc2[1]);
			} else if (message.startsWith(MEMBER_REMOVE)) {
				String proc1[] = message.split(":");
				String proc2[] = proc1[1].split("\\|");
				return String.format(context.getString(R.string.format_member_remove), proc2[0],
						proc2[1]);
			} else if (message.startsWith(ALARM_ADD)) {
				String proc1[] = message.split(":");
				String proc2[] = proc1[1].split("\\|");
				if (proc2[0].length() == 0)
					return "";
				else
					return String.format(context.getString(R.string.format_alarm_add), proc2[0],
							proc2[1]);
			} else if (message.startsWith(ALARM_EDIT)) {
				String proc1[] = message.split(":");
				String proc2[] = proc1[1].split("\\|");
				if (proc2[0].length() == 0)
					return "";
				else
					return String.format(context.getString(R.string.format_alarm_edit), proc2[0],
							proc2[1]);
			} else if (message.startsWith(ALARM_REMOVE)) {
				String proc1[] = message.split(":");
				String proc2[] = proc1[1].split("\\|");
				if (proc2[0].length() == 0)
					return "";
				else
					return String.format(context.getString(R.string.format_alarm_remove), proc2[0],
							proc2[1]);
			} else if (message.startsWith(FOREST_INVITE)) {
				String proc1[] = message.split(":");
				String proc2[] = proc1[1].split("\\|");
				if (proc2[0].length() == 0)
					return "";
				else
					return String.format(context.getString(R.string.format_forest_invite), proc2[2],
							proc2[0]);
			} else
				return "";
		} catch (Exception e) {
			return e.toString();
		}
	}
}
