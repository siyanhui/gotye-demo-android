package com.open_demo.activity;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeDelegate;
import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeMedia;
import com.gotye.api.GotyeNotify;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.open_demo.R;
import com.open_demo.adapter.NotifyListAdapter;
import com.open_demo.util.ProgressDialogUtil;
import com.open_demo.util.ToastUtil;
import com.open_demo.view.SwipeMenu;
import com.open_demo.view.SwipeMenuCreator;
import com.open_demo.view.SwipeMenuItem;
import com.open_demo.view.SwipeMenuListView;
import com.open_demo.view.SwipeMenuListView.OnMenuItemClickListener;

public class NotifyListPage extends Activity {
	private SwipeMenuListView listView;
	private NotifyListAdapter adapter;

	private List<GotyeNotify> notifies;
	public GotyeAPI api=GotyeAPI.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_notify_page);
		api.addListener(mDelegate);
		listView = (SwipeMenuListView) findViewById(R.id.listview);
		setListview();
		loadData();
	}

	private void setListview() {
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				createMenu(menu);
			}

			private void createMenu(SwipeMenu menu) {

				SwipeMenuItem item1 = new SwipeMenuItem(getApplicationContext());
				item1.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
						0xCE)));
				
				item1.setWidth(dp2px(90));
//				item1.setIcon(R.drawable.ic_action_about);

				// menu.addMenuItem(item1);

				SwipeMenuItem item2 = new SwipeMenuItem(getApplicationContext());
				item2.setBackground(new ColorDrawable(Color.rgb(0xE5, 0x18,
						0x5E)));
				item2.setWidth(dp2px(90));
//				item2.setIcon(R.drawable.ic_action_discard);

				// menu.addMenuItem(item2);

				int type = menu.getViewType();
				switch (type) {
				case 0:
					item2.setTitle("删除");
					menu.addMenuItem(item2);
					break;
				case 1:
					item1.setTitle("同意");
					menu.addMenuItem(item1);

					item2.setTitle("拒绝");
					menu.addMenuItem(item2);
					break;
				case 2:
					item2.setTitle("删除");
					menu.addMenuItem(item2);
					break;
				case 3:
					
					item1.setTitle("知道了");
					menu.addMenuItem(item1);
					break;
				case 4:
					item2.setTitle("删除");
					menu.addMenuItem(item2);
					break;
				case 5:
					item1.setTitle("同意");
					menu.addMenuItem(item1);

					item2.setTitle("拒绝");
					menu.addMenuItem(item2);
					
					break;
				default:
					break;
				}

			}
		};
		// set creator
		listView.setMenuCreator(creator);
		listView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				GotyeNotify notify = adapter.getItem(position);
				switch (notify.getType()) {
				case GroupInvite:
					if (notify.isRead()) {
						api.deleteNotify(notify);
						refresh();

					} else {
						if (index == 0) {
							api.joinGroup(new GotyeGroup(notify.getFrom().getId()));
							notify.setRead(true);
							api.markNotifyIsRead(notify,true);
							ProgressDialogUtil.showProgress(
									NotifyListPage.this, "正在加入..");
							refresh();
							
						} else {
							notify.setRead(true);
							api.markNotifyIsRead(notify,true);
							refresh();
						}
					}
					break;
				case JoinGroupReply:
					if (notify.isRead()) {
						api.deleteNotify(notify);
						refresh();
					} else {
						if(notify.isAgree()){
							api.joinGroup(new GotyeGroup(notify.getFrom().getId()));
						}
						notify.setRead(true);
						api.markNotifyIsRead(notify,true);
						refresh();
					}
					break;
				case JoinGroupRequest:
					if (notify.isRead()) {
						api.deleteNotify(notify);
						refresh();
					} else {
						if (index == 0) {
							notify.setRead(true);
							api.markNotifyIsRead(notify,true);
							refresh();
							api.replyJoinGroup(notify, "欢迎加入", true);
							 
						} else {
							notify.setRead(true);
							api.markNotifyIsRead(notify,true);
							refresh();
							api.replyJoinGroup(notify, "你是谁？", false);
						}
					}
				}
				return false;
			}
		});
	}

	private void loadData() {
		notifies = api.getNotifyList();
		if (notifies != null) {
			if (adapter == null) {
				adapter = new NotifyListAdapter(this, notifies);
				listView.setAdapter(adapter);
			} else {
				adapter.refreshData(notifies);
			}
		} else if (adapter != null) {
			adapter.clear();
		}

	}

//	@Override
//	public void onGetGroupDetail(int code, GotyeGroup group) {
//		// TODO Auto-generated method stub
//		super.onGetGroupDetail(code, group);
//		
//		if(adapter!=null){
//			adapter.notifyDataSetChanged();
//		}
//	}
	public void back(View view) {
		finish();
	}

	public void refresh() {
		loadData();
	}

	@Override
	protected void onDestroy() {
		api.removeListener(mDelegate);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadData();
	}
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
	
	private GotyeDelegate mDelegate = new GotyeDelegate(){
		
		@Override
		public void onGetGroupDetail(int code, GotyeGroup group) {
			// TODO Auto-generated method stub
			super.onGetGroupDetail(code, group);
			
			if(adapter!=null){
				adapter.notifyDataSetChanged();
			}
		}
		
		@Override
		public void onReceiveNotify(GotyeNotify notify) {
			loadData();
		}
		@Override
		public void onDownloadMedia(int code, GotyeMedia media) {
			// TODO Auto-generated method stub
			super.onDownloadMedia(code, media);
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onJoinGroup(int code, GotyeGroup group) {
			ProgressDialogUtil.dismiss();
			if (code == GotyeStatusCode.CodeOK) {
				if (notifies != null) {
					for (GotyeNotify notify : notifies) {
						if (notify.getFrom().getId() == group.getGroupID()) {
							notify.setRead(true);
							ToastUtil.show(NotifyListPage.this, "成功加入该群");
							if (adapter != null) {
								adapter.notifyDataSetChanged();
							}
							return;
						}
					}
				}
			}else if(code == GotyeStatusCode.CodeRepeatOper){
				ToastUtil.show(NotifyListPage.this, "重复操作");
			} else {
				ToastUtil.show(NotifyListPage.this, "加群失败");
			}
		}
		@Override
		public void onAddBlocked(int code, GotyeUser user) {
			// TODO Auto-generated method stub
			loadData();
		}

		@Override
		public void onRemoveBlocked(int code, GotyeUser user) {
			// TODO Auto-generated method stub
			loadData();
		}
		
		
	};
}
