package com.open_demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeUser;
import com.open_demo.main.MainActivity;

import android.R.integer;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

 

public class MyApplication extends Application {
//	public static final String DEFAULT_APPKEY = "ccim1001-19f6-4bd8-9b29-e454fdbf4990";
	public static final String DEFAULT_APPKEY = "9c236035-2bf4-40b0-bfbf-e8b6dec54928";
//	public static final String DEFAULT_APPKEY = "029140c7-3acc-48df-b080-17b50ea8f082";
	public static String APPKEY = DEFAULT_APPKEY;
//	public static String IP = null;
	public static String IP = "120.132.60.176";
//	public static int PORT = -1;
	public static int PORT = 8888;
	private static SharedPreferences spf;
	private static final String SHARE_PREFERENCE_NAME = "gotye_config";
	// 是否有新消息提醒
	public static boolean newMsgNotify = true;
	// 不接收群消息
	public static boolean notReceiveGroupMsg = false;
	//群消息状态
	public static Map<Long, Integer> mapList = new HashMap<Long, Integer>();
	public static final String CONFIG = "markGroupTag";
	public static ArrayList<Long> disturbGroupIds = new ArrayList<Long>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		loadSelectedKey(this);
		CrashApplication.getInstance(this).onCreate();
		spf = getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
	}

	void onLoginCallBack(int code, GotyeUser currentLoginUser) {
		newMsgNotify = spf.getBoolean(
				"new_msg_notify_" + currentLoginUser.getName(), true);
		notReceiveGroupMsg = spf.getBoolean("not_receive_group_msg_"
				+ currentLoginUser.getName(), false);
	}

	public static void onLogoutCallBack(int code) {
		disturbGroupIds.clear();
	}

	public static boolean isNewMsgNotify() {
		return newMsgNotify;
	}
	/**
	 * 设置指定群组的群消息转态
	 * @param groupId
	 * @param tag
	 */
	public static void setGroupMarkTag(long groupId, int tag){
		if(mapList == null){
			mapList =  new HashMap<Long, Integer>();
		}
		mapList.put(groupId, tag);
	}
	
	public static void setGroupMarkTag1(Context context, String user, String groupId, int tag){
		SharedPreferences sp = context.getSharedPreferences(CONFIG,
				context.MODE_PRIVATE);
		SharedPreferences.Editor edit = sp.edit();
		if(groupId != null && user != null){
			edit.putInt(user + groupId, tag);
			edit.commit();
		}
	}
	
	public static int getGroupMarkTag1(Context context, String user, String groupId){
		int tag = -1;
		SharedPreferences sp = context.getSharedPreferences(CONFIG, context.MODE_PRIVATE);
		if(sp.contains(user + groupId)){
			tag = sp.getInt(user + groupId, 0);
		}
		return tag;
	}
	
	/**
	 * 获取指定群组的群消息状态
	 * @param groupId
	 * @return
	 */
	public static int getGroupMarKTag(long groupId){
		int code = -1;
		if(mapList != null){
			if(mapList.containsKey(groupId)){
				code = MyApplication.mapList.get(groupId);
			}
		}
		return code ; 
	}
	
	/**
	 * 设置群消息免打扰
	 */
	public static void setGroupDontdisturb(long groupId) {
		if (disturbGroupIds == null) {
			disturbGroupIds = new ArrayList<Long>();
		}
		if (!disturbGroupIds.contains(groupId)) {
			disturbGroupIds.add(groupId);
			String dontdisturbIds = spf.getString("groupDontdisturb", null);
			if (dontdisturbIds == null) {
				spf.edit()
						.putString("groupDontdisturb", String.valueOf(groupId))
						.commit();
			} else {
				dontdisturbIds += "," + String.valueOf(groupId);
				spf.edit().putString("groupDontdisturb", dontdisturbIds)
						.commit();
			}

		}
	}

	/**
	 * 移除群消息免打扰
	 * 
	 * @param groupId
	 */
	public static void removeGroupDontdisturb(long groupId) {
		if (disturbGroupIds == null) {
			return;
		} else {
			disturbGroupIds.remove(groupId);
		}
	}

	/**
	 * 判断是否设置群消息免打扰
	 * 
	 * @param groupId
	 * @return
	 */
	public static boolean isGroupDontdisturb(long groupId) {
		if (disturbGroupIds == null) {
			String dontdisturbIds = spf.getString("groupDontdisturb", null);
			if (dontdisturbIds == null) {
				return false;
			} else {
				disturbGroupIds = new ArrayList<Long>();
				String ids[] = dontdisturbIds.split(",");
				for (String id : ids) {
					disturbGroupIds.add(Long.parseLong(id));
				}
				return disturbGroupIds.contains(groupId);
			}
		} else {
			return disturbGroupIds.contains(groupId);
		}
	}

	/**
	 * 设置新消息是否提醒
	 * 
	 * @param newMsgNotify
	 */
	public static void setNewMsgNotify(boolean newMsgNotify_, String name) {
		newMsgNotify = newMsgNotify_;
		spf.edit().putBoolean("new_msg_notify_" + name, newMsgNotify).commit();
	}

	/**
	 * 设置群是否免打扰
	 * 
	 * @param groupId
	 * @param disturb
	 */
	public void setDisturb(long groupId, boolean disturb) {
		spf.edit().putBoolean(String.valueOf(groupId), disturb).commit();
	}

	/**
	 * 判断是否接收群消息
	 */
	public static boolean isNotReceiveGroupMsg() {
		return notReceiveGroupMsg;
	}

	/**
	 * 设置是否接收群消息
	 * 
	 * @param notReceiveGroupMsg
	 */
	public static void setNotReceiveGroupMsg(boolean notReceiveGroupMsg_,
			String loginName) {
		notReceiveGroupMsg = notReceiveGroupMsg_;
		spf.edit()
				.putBoolean("not_receive_group_msg_" + loginName,
						notReceiveGroupMsg).commit();
	}

	public static void loadSelectedKey(Context context) {
		SharedPreferences spf = context.getSharedPreferences("gotye_api",
				Context.MODE_PRIVATE);
		APPKEY = spf.getString("selected_key", DEFAULT_APPKEY);
		String ip_port = spf.getString("selected_ip_port", null);
		if (!TextUtils.isEmpty(ip_port)) {
			String[] ipPort = ip_port.split(":");
			if (ipPort != null && ipPort.length >= 2) {
				try {
					int port = Integer.parseInt(ipPort[1]);
					IP = ipPort[0];
					PORT = port;
				} catch (Exception e) {

				}

			}
		}

	}
	
	
	public static void setOfflineStatu(Context context, String user, int code){
		SharedPreferences sp = context.getSharedPreferences("offlineStatus", 
				context.MODE_PRIVATE);
		SharedPreferences.Editor edit = sp.edit();
		
		edit.putInt(user, code);
		edit.commit();
	}
	
	public static int getOfflineStatu(Context context, String user){
		SharedPreferences sp = context.getSharedPreferences("offlineStatus", 
				context.MODE_PRIVATE);
		return sp.getInt(user, 1000);
	}
	
	public static boolean getHasLogin(Context context){
		SharedPreferences sp = context.getSharedPreferences(LoginPage.CONFIG,
				Context.MODE_PRIVATE);
		return sp.getBoolean("haslogin", false);
	}
	
	public static void clearHasLogin(Context context){
		SharedPreferences sp = context.getSharedPreferences(LoginPage.CONFIG,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = sp.edit();
		
		edit.remove("haslogin");
		edit.commit();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
