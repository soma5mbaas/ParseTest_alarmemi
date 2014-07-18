package com.provision.alarmemi.paper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SlideMenu extends Fragment {

    static SharedPreferences prefs;
    static String myUUID;
    static NotifyAdapter adapter;
    static ListView mNotifyList;
    static Handler handler;
    static Handler OnNotifyChanged;
    static int[] message_images = {R.drawable.alarm_add,
            R.drawable.alarm_edit, R.drawable.alarm_remove,
            R.drawable.member_add, R.drawable.member_remove,
            R.drawable.forest_invite};
    static int[] message_images_read = {R.drawable.alarm_add_read,
            R.drawable.alarm_edit_read, R.drawable.alarm_remove_read,
            R.drawable.member_add_read, R.drawable.member_remove_read,
            R.drawable.forest_invite_read};
    ImageView ic_notify_refresh;

    private void switchContent(Fragment f, boolean isSetAlarm) {
        if (SetAlarmFragment.isRunning)
            SetAlarmFragment.DontSaveDialog(true, f, isSetAlarm);
        else
            ((FragmentChangeActivity) getActivity()).switchContent(f);
    }
    
    private FragmentChangeActivity mActivity;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mActivity = (FragmentChangeActivity)getActivity();

        handler = new LoginHandler();
        OnNotifyChanged = new onNotifyChangedHandler();
        prefs = mActivity.getSharedPreferences("forest", Context.MODE_PRIVATE);
        myUUID = SplashActivity.myUUID;
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.slide_menu, null);

        LinearLayout menu_lists = (LinearLayout) inflater.inflate(
                R.layout.slide_menu_lists, null);
        mNotifyList = (ListView) root.findViewById(R.id.notify_list);
        mNotifyList.addHeaderView(menu_lists);

        // Main
        root.findViewById(R.id.menu_main).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                switchContent(new MainFragment(), false);
            }
        });

        // Add alarm
        root.findViewById(R.id.menu_add_alarm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mActivity.setAlarmGetIntent = new Intent();

                if (SetAlarmFragment.isRunning) switchContent(null, true);
                else switchContent(new SetAlarmFragment().setXmlId(R.xml.alarm_prefs), true);
            }
        });

        // Forest Account
        root.findViewById(R.id.menu_forest).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                switchContent(new CloudAccountFragment(), false);
            }
        });

        // About
        root.findViewById(R.id.menu_about).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                switchContent(new AboutFragment(), false);
            }
        });

        // Settings
        root.findViewById(R.id.menu_settings).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                switchContent(new SettingsFragment(), false);
            }
        });

        // Help
        root.findViewById(R.id.menu_help).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FragmentChangeActivity.welcomePrefs = "";
                SharedPreferences.Editor editor = FragmentChangeActivity.prefs.edit();
                editor.putString("welcome", FragmentChangeActivity.welcomePrefs);
                editor.commit();

                switchContent(new MainFragment(), false);
            }
        });

        // Exit
        root.findViewById(R.id.menu_exit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mActivity.finish();
            }
        });

        root.findViewById(R.id.notify_refresh).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new GetDataTask().execute();
            }
        });

        ic_notify_refresh = (ImageView) root.findViewById(R.id.ic_notify_refresh);
        MarkAlreadyInvite();

        String prefs_notify = prefs.getString("notify", "");
        if (!prefs_notify.equals("")) {
            List<String> notify_ = Arrays.asList(prefs_notify.substring(0, prefs_notify.length() - 2).split("\\|\\|"));
            android.util.Log.d("url", notify_.toString());
            ArrayList<String> notify = new ArrayList<String>(notify_);
            adapter = new NotifyAdapter(mActivity, R.layout.notify_row, notify);
            mNotifyList.setAdapter(adapter);
        } else {
            ArrayList<String> notify = new ArrayList<String>();
            adapter = new NotifyAdapter(mActivity, R.layout.notify_row, notify);
            mNotifyList.setAdapter(adapter);
        }

        return root;
    }

    private static void deleteNotify(int position) {
        FragmentChangeActivity.OnNotifyArrived.sendEmptyMessage(0);

        android.util.Log.d("asdf", position + " is pos");
        String prefs_notify = prefs.getString("notify", "");
        if (prefs_notify.equals(""))
            return;
        String origin_pref[] = prefs_notify.substring(0,
                prefs_notify.length() - 2).split("\\|\\|");
        String modified_pref = "";
        for (int i = 0; i < origin_pref.length; i++) {
            if (i == position)
                continue;
            modified_pref += origin_pref[i];
            modified_pref += "||";
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("notify", modified_pref);
        editor.commit();
        onNotifyChanged();
    }

    static boolean isPushing = false;

    static boolean GetPush(Context context) {
        if (isPushing)
            return false;
        isPushing = true;
        try {
            prefs = context
                    .getSharedPreferences("forest", Context.MODE_PRIVATE);
            if (!prefs.getString("name", "").equals("")) {
                String forest_name[] = prefs.getString("name", "").substring(1)
                        .split("\\|");

                for (int i = 0; i < forest_name.length; i++) {
                    android.util.Log.e("getnotify", "-----( " + i + " )-----");
                    String message_result;
                    String result = ServerUtilities.connect(
                            "http://alarmemi.appspot.com/alarmemi/notify/get?owner_name="
                                    + URLEncoder
                                    .encode(forest_name[i], "UTF-8")
                                    + "&owner_password="
                                    + prefs.getString(forest_name[i]
                                    + "_password", "") + "&no="
                                    + prefs.getInt(forest_name[i] + "_no", -1),
                            context
                    );
                    if (result == null) {
                        message_result = "FAILED";
                    } else if (result.equals("CONNECTION_FAILED")) {
                        message_result = "CONNECTION_FAILED";
                    } else if (result.equals("FAILED")) {
                        message_result = "FAILED";
                    } else {
                        JSONObject json2 = new JSONObject(result);
                        android.util.Log.d("getnotify", json2.toString());
                        int no = prefs.getInt(forest_name[i] + "_no", -1);
                        for (int j = json2.length() - 1; j >= 0; j--) {
                            JSONObject json3 = new JSONObject(
                                    json2.getString("notify" + j));
                            android.util.Log.d("url", json3.toString());
                            if (!json3.getString("uid").equals(myUUID)) {
                                Intent GCMintent = new Intent();
                                GCMintent.putExtra("message",
                                        json3.getString("message"));
                                GCMIntentService.on_Message(context, GCMintent,
                                        myUUID);
                            }
                            no = json3.getInt("no");
                        }

                        android.util.Log.d("NotifyPref",
                                prefs.getInt(forest_name[i] + "_no", -1) + "");
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(forest_name[i] + "_no", no);
                        editor.commit();
                        android.util.Log.d("NotifyPref",
                                prefs.getInt(forest_name[i] + "_no", -1) + "");
                        message_result = "SUCCEED";
                    }
                    if (!message_result.equals("SUCCEED")) {
                        isPushing = false;
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            isPushing = false;
            return false;
        }
        isPushing = false;
        return true;
    }

    Handler startRotate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            RotateAnimation anim = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(1000);
            anim.setRepeatCount(Animation.INFINITE);
            ic_notify_refresh.startAnimation(anim);
        }
    };

    Handler stopRotate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ic_notify_refresh.clearAnimation();
        }
    };

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        protected String[] doInBackground(Void... params) {
            startRotate.sendEmptyMessage(0);
            GetPush(mActivity);
            return null;
        }

        protected void onPostExecute(String[] result) {
            // Do some stuff here
            // Call onRefreshComplete when the list has been refreshed.
            onNotifyChanged();
            stopRotate.sendEmptyMessage(0);
            super.onPostExecute(result);
        }
    }

    public static void MarkAlreadyInvite() {
        String prefs_notify = prefs.getString("notify", "");
        if (prefs_notify.equals(""))
            return;
        String origin_pref[] = prefs_notify.substring(0,
                prefs_notify.length() - 2).split("\\|\\|");
        String modified_pref = "";
        for (int i = 0; i < origin_pref.length; i++) {
            String message_key_split[] = origin_pref[i].split("\\|");
            if (origin_pref[i].startsWith(Notify.FOREST_INVITE)
                    && !prefs.getString(
                    message_key_split[0].split(":")[1] + "_password",
                    "").equals("")
                    && message_key_split[message_key_split.length - 2]
                    .endsWith("ask")) {
                String modified_key = "";
                for (int j = 0; j < message_key_split.length - 2; j++) {
                    modified_key += message_key_split[j] + "|";
                }
                modified_key += "already|"
                        + message_key_split[message_key_split.length - 1];
                origin_pref[i] = modified_key;

            }
            modified_pref += origin_pref[i];
            modified_pref += "||";
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("notify", modified_pref);
        editor.commit();
    }

    static void NotifyReadCheck(String message_key, ArrayList<String> items,
                                int position) {
        FragmentChangeActivity.OnNotifyArrived.sendEmptyMessage(0);

        String message_key_split[] = message_key.split("\\|");
        String modified_key = "";
        for (int i = 0; i < message_key_split.length - 1; i++) {
            modified_key += message_key_split[i] + "|";
        }
        modified_key += "yes";
        String modified_pref = "";
        for (int i = 0; i < items.size(); i++) {
            if (i == position)
                modified_pref += modified_key;
            else
                modified_pref += items.get(i);
            modified_pref += "||";
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("notify", modified_pref);
        editor.commit();
        OnNotifyChanged.sendEmptyMessage(0);
    }

    public static void setReads(View v, boolean read, int img_no) {
        if (read) {
            v.setBackgroundColor(Color.parseColor("#e2e2e2"));
            ((ImageView) v.findViewById(R.id.icon))
                    .setImageResource(message_images_read[img_no]);
            ((TextView) v.findViewById(R.id.description)).setTextColor(Color
                    .parseColor("#b9b9b9"));
        } else {
            v.setBackgroundColor(Color.parseColor("#000000ff"));
            ((ImageView) v.findViewById(R.id.icon))
                    .setImageResource(message_images[img_no]);
            ((TextView) v.findViewById(R.id.description)).setTextColor(Color
                    .parseColor("#000000"));
        }
    }

    static class NotifyAdapter extends ArrayAdapter<String> {

        private ArrayList<String> items;
        private Context mContext;

        public NotifyAdapter(Context context, int textViewResourceId,
                             ArrayList<String> items) {
            super(context, textViewResourceId, items);
            mContext = context;
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.notify_row, null);
            }
            final String message_key = items.get(position);
            android.util.Log.d("url", ", " + message_key);
            int img_no = 0;
            for (int i = 0; i < Notify.MESSAGES.length; i++) {
                if (message_key.startsWith(Notify.MESSAGES[i])) {
                    img_no = i;
                    break;
                }
            }

            setReads(v, !message_key.endsWith("|no"), img_no);

            v.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    NotifyReadCheck(message_key, items, position);
                }
            });

            ((TextView) v.findViewById(R.id.description)).setText(Notify
                    .getString(mContext, message_key));

            ((LinearLayout) v.findViewById(R.id.invite_layout))
                    .setVisibility(View.GONE);

            if (message_key.startsWith(Notify.FOREST_INVITE)) {
                final String message_key_split[] = message_key.split("\\|");
                String result = message_key_split[message_key_split.length - 2];

                if (result.equals("ask")) {
                    ((LinearLayout) v.findViewById(R.id.invite_layout))
                            .setVisibility(View.VISIBLE);
                    ((LinearLayout) v.findViewById(R.id.accept))
                            .setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    FragmentChangeActivity.progressDialog
                                            .show();
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            String id, password_hash = message_key_split[1];
                                            try {
                                                id = URLEncoder.encode(
                                                        message_key_split[0]
                                                                .split(":")[1],
                                                        "UTF-8"
                                                );
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                                return;
                                            }

                                            // Connect to the server
                                            String result = ServerUtilities
                                                    .connect(
                                                            "http://alarmemi.appspot.com/alarmemi/forest/getinfo?name="
                                                                    + id
                                                                    + "&password="
                                                                    + password_hash,
                                                            mContext
                                                    );

                                            if (result != null
                                                    && !result
                                                    .equals("NOT_AVAILABLE")
                                                    && !result
                                                    .equals("CONNECTION_FAILED")) {
                                                String modified_key = "";
                                                for (int i = 0; i < message_key_split.length - 2; i++) {
                                                    modified_key += message_key_split[i]
                                                            + "|";
                                                }
                                                modified_key += "yes|yes";
                                                String modified_pref = "";
                                                for (int i = 0; i < items
                                                        .size(); i++) {
                                                    if (i == position)
                                                        modified_pref += modified_key;
                                                    else
                                                        modified_pref += items
                                                                .get(i);
                                                    modified_pref += "||";
                                                }
                                                SharedPreferences.Editor editor = prefs
                                                        .edit();
                                                editor.putString("notify",
                                                        modified_pref);
                                                editor.commit();
                                                OnNotifyChanged
                                                        .sendEmptyMessage(0);
                                            } else {
                                                NotifyReadCheck(message_key,
                                                        items, position);
                                            }

                                            // Send message to UI Thread
                                            Message m = new Message();
                                            m.obj = result;
                                            handler.sendMessage(m);
                                        }
                                    }.start();
                                }
                            });
                    ((LinearLayout) v.findViewById(R.id.reject))
                            .setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    String modified_key = "";
                                    for (int i = 0; i < message_key_split.length - 2; i++) {
                                        modified_key += message_key_split[i]
                                                + "|";
                                    }
                                    modified_key += "no|yes";
                                    String modified_pref = "";
                                    for (int i = 0; i < items.size(); i++) {
                                        if (i == position)
                                            modified_pref += modified_key;
                                        else
                                            modified_pref += items.get(i);
                                        modified_pref += "||";
                                    }
                                    SharedPreferences.Editor editor = prefs
                                            .edit();
                                    editor.putString("notify", modified_pref);
                                    editor.commit();
                                    onNotifyChanged();
                                }
                            });
                } else if (result.equals("yes")) {
                    ((TextView) v.findViewById(R.id.description))
                            .setText(Notify.getString(mContext, message_key)
                                    + mContext.getString(R.string.accepted));
                } else if (result.equals("no")) {
                    ((TextView) v.findViewById(R.id.description))
                            .setText(Notify.getString(mContext, message_key)
                                    + mContext.getString(R.string.rejected));
                } else if (result.equals("already")) {
                    ((TextView) v.findViewById(R.id.description))
                            .setText(Notify.getString(mContext, message_key)
                                    + mContext.getString(R.string.already_enter));
                }
            }

            v.findViewById(R.id.delete).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            deleteNotify(position);
                        }
                    }
            );
            return v;
        }
    }

    class onNotifyChangedHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            onNotifyChanged();
        }
    }

    class LoginHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String result = (String) msg.obj;
            int failed_message = -1;
            FragmentChangeActivity.progressDialog.dismiss();

            if (result == null) {
                // Show error dialog
                failed_message = R.string.server_error;
            } else if (result.equals("CONNECTION_FAILED")) {
                failed_message = R.string.connection_chk;
            } else if (!result.equals("NOT_AVAILABLE")) {

                try {
                    switchContent(new CloudAccountFragment(), false);

                    // Convert result to json
                    JSONObject json = new JSONObject(result);
                    String name = json.getString("name");

                    // Write to shared preferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("name", prefs.getString("name", "") + "|"
                            + name);
                    editor.putString(name + "_password",
                            json.getString("password"));
                    editor.putString(name + "_registeredDevice",
                            json.getString("registeredDevice"));
                    editor.commit();

                    CloudAccountFragment.needToRegister = name;
                    CloudAccountFragment.RegisterDevice(CloudAccountFragment.needToRegister);

                    MarkAlreadyInvite();
                    onNotifyChanged();
                } catch (JSONException e) {
                    // Show error dialog
                    failed_message = R.string.idpw_error;
                }
            } else if (result.equals("NOT_AVAILABLE")) {
                // Show error dialog
                failed_message = R.string.idpw_error;
            }
            if (failed_message != -1) {
                new AlertDialogBuilder(mActivity, R.string.login_error,
                        failed_message, false, null);
            }
        }
    }

    static void onNotifyChanged() {
        String prefs_notify = prefs.getString("notify", "");
        ArrayList<String> notify;
        if (!prefs_notify.equals("")) {
            List<String> notify_ = Arrays.asList(prefs_notify.substring(0,
                    prefs_notify.length() - 2).split("\\|\\|"));
            android.util.Log.d("url", notify_.toString());
            notify = new ArrayList<String>(notify_);
        } else {
            notify = new ArrayList<String>();
        }
        adapter.clear();
        for (int i = 0; i < notify.size(); i++) {
            adapter.add(notify.get(i));
        }
        // adapter.addAll(notify);
        adapter.notifyDataSetChanged();
    }

}