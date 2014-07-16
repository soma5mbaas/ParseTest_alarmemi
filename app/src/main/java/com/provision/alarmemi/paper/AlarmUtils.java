package com.provision.alarmemi.paper;


public class AlarmUtils {
  /*  public static void showTimeEditDialog(FragmentManager manager, final Alarm alarm) {
        final FragmentTransaction ft = manager.beginTransaction();
        final Fragment prev = manager.findFragmentByTag("time_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        final AlarmTimePickerDialog fragment = AlarmTimePickerDialog.newInstance(alarm);
        fragment.show(ft, "time_dialog");
    }*/
    
	public static boolean Check(String str) {
		if(str == null) return true;
		String banned[] = { "|", "&", "=" };
		for (int i = 0; i < banned.length; i++) {
			if(str.contains(banned[i])) return false;
		}
		return true;
	}
}
