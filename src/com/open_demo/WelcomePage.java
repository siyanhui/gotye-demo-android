package com.open_demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeDelegate;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.open_demo.main.MainActivity;
import com.open_demo.util.ProgressDialogUtil;

public class WelcomePage extends FragmentActivity implements OnGestureListener {
	private Fragment loginSetting, loginPage;
	private GestureDetector mGesture = null;

	private boolean onLogin = true;

	private int width;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//初始化
		int code = GotyeAPI.getInstance().init(this, MyApplication.APPKEY);
		// -1 offline, API will reconnect   when network becomes valid
		// 0 not login or logout already
		// 1 online
		//判断当前登陆状态
		int state = GotyeAPI.getInstance().isOnline();
		Log.d("login", "state=" + state);
		//GotyeAPI.getInstance().resetUserSearch();
		//GotyeAPI.getInstance().enableLog(false, true, false);
		//已经登陆了就直接跳转了
		GotyeUser us = GotyeAPI.getInstance().getLoginUser();
		Log.d("login", "us = "+us.getName());
//		if (state == GotyeUser.NETSTATE_ONLINE&&GotyeAPI.getInstance().getLoginUser()!=null) {
//			//已经登陆或离线可以直接跳到主界面
//			Intent i = new Intent(this, MainActivity.class);
//			startActivity(i);
//			//启动service保存service长期活动
//			Intent toService = new Intent(this, GotyeService.class);
//			startService(toService);
//			finish();
//			return;
//		}
		String user1[] = LoginPage.getUser(WelcomePage.this);
		String hasUserName = user1[0];
		boolean hasLogin = MyApplication.getHasLogin(this);
		if(hasUserName != null && hasLogin == true){
			if(state == GotyeUser.NETSTATE_ONLINE || state == GotyeUser.NETSTATE_OFFLINE){
				Intent i = new Intent(this, MainActivity.class);
				startActivity(i);
				//启动service保存service长期活动
				Intent toService = new Intent(this, GotyeService.class);
				startService(toService);
				finish();
				return;
			}else if(state == GotyeUser.NETSTATE_BELOWLINE){
				GotyeAPI.getInstance().login(hasUserName, null);
			}
		}
		//没有登陆需要显示登陆界面
		setContentView(R.layout.layout_welcome);
		//注意添加LoginListener
		GotyeAPI.getInstance().addListener(mDelegate);
		loginSetting = new LoginSettingPage();
		loginPage = new LoginPage();
		//显示login Fragment
		showLogin();
		mGesture = new GestureDetector(this, this);
		width=getResources().getDisplayMetrics().widthPixels/2;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	public void showLogin() {
		onLogin = true;
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		ft.setCustomAnimations(R.anim.back_left_in,R.anim.back_right_out); 
		ft.replace(R.id.fragment_container, loginPage, "login");
		
		ft.addToBackStack(null);
		
		ft.commit();
	}

	public void showSetting() {
		if (!onLogin) {
			return;
		}
		onLogin = false;
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		ft.setCustomAnimations(R.anim.push_left_in,R.anim.push_left_out); 
		ft.replace(R.id.fragment_container, loginSetting, "setting");
		ft.commit();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return mGesture.onTouchEvent(event);
	}

	@Override
	protected void onDestroy() {
		// 移除监听
		GotyeAPI.getInstance().removeListener(mDelegate);
		super.onDestroy();
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() - e2.getX() > width) {// 向左滑，右边显示
			// this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,
			// R.anim.push_left_in));
			// this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
			// R.anim.push_left_out));
			showSetting();
		}
		//if (e1.getX() - e2.getX() < -120) {// 向右滑，左边显示
			//showSetting();
		//}
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	// 单击
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	// 长按
	public void onLongPress(MotionEvent e) {

	}
	
	private GotyeDelegate mDelegate = new GotyeDelegate(){
		
		public void onLogin(int code, GotyeUser user) {
			ProgressDialogUtil.dismiss();
			// 判断登陆是否成功
			if (code == GotyeStatusCode.CodeOK //0
					|| code == GotyeStatusCode.CodeReloginOK //5
					|| code == GotyeStatusCode.CodeOfflineLoginOK) {  //6
				
				// 传入已登过的状态
				String user1[] = LoginPage.getUser(WelcomePage.this);
				String hasUserName = user1[0];
				String hasPassWord = user1[1];
				LoginPage.saveUser(WelcomePage.this, hasUserName, hasPassWord, true);
				
				Intent i = new Intent(WelcomePage.this, MainActivity.class);
				startActivity(i);
				
				if (code == GotyeStatusCode.CodeOfflineLoginOK) {
					Toast.makeText(WelcomePage.this, "您当前处于离线状态", Toast.LENGTH_SHORT).show();
				} else if (code == GotyeStatusCode.CodeOK) {
					Toast.makeText(WelcomePage.this, "登录成功", Toast.LENGTH_SHORT).show();
				}
				WelcomePage.this.finish();
			} else {
				// 失败,可根据code定位失败原因
				Toast.makeText(WelcomePage.this, "登录失败 code=" + code, Toast.LENGTH_SHORT)
						.show();
			}
		}

		public void onLogout(int code) {
		}

		public void onReconnecting(int code, GotyeUser currentLoginUser) {
		}
	};
}
