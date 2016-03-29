package com.open_demo.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.TextView;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeDelegate;
import com.gotye.api.GotyeMedia;
import com.gotye.api.GotyeRoom;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.open_demo.R;
import com.open_demo.adapter.MemberAdapter;
import com.open_demo.util.PreferenceUitl;
import com.open_demo.util.ProgressDialogUtil;

public class RoomInfoPage extends Activity {
	private GotyeRoom room;
	private MemberAdapter adapter;
	private GridView memberView;
	CheckBox showMemberName;
	public GotyeAPI api=GotyeAPI.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		room = (GotyeRoom) getIntent().getSerializableExtra("room");
		setContentView(R.layout.layout_room_info);
		api.addListener(mDelegate);
		memberView = (GridView) findViewById(R.id.members);
		((TextView) findViewById(R.id.room_name)).setText(room.getName());

		if (api.isInRoom(room)) {
			api.reqRoomMemberList(room, 0);
		} else {
			ProgressDialogUtil.showProgress(this, "正在进入房间...");
			api.enterRoom(room);
		}
		memberView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				GotyeUser user = (GotyeUser) adapter.getItem(arg2);
				Intent i = new Intent(getBaseContext(), UserInfoPage.class);
				i.putExtra("user", user);
				i.putExtra("from", 100);
				i.putExtra("room", room);
				startActivity(i);

			}
		});
		CheckBox set_to_top = ((CheckBox) findViewById(R.id.set_to_top));
		boolean setTop = PreferenceUitl.getBooleanValue(RoomInfoPage.this,
				"set_top_" + room.getId());
		set_to_top.setChecked(setTop);
		set_to_top.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				PreferenceUitl.setBooleanValue(RoomInfoPage.this, "set_top_"
						+ room.getId(), arg1);
				api.markSessionIsTop(room, arg1);
			}
		});
		showMemberName = ((CheckBox) findViewById(R.id.show_member_name));
		boolean showName = PreferenceUitl.getBooleanValue(RoomInfoPage.this,
				"r_show_name_" + room.getId());
		if (adapter != null) {
			adapter.showName = showName;
			adapter.notifyDataSetChanged();
		}
		showMemberName.setChecked(showName);
		showMemberName
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						PreferenceUitl.setBooleanValue(RoomInfoPage.this,
								"r_show_name_" + room.getId(), arg1);
						if (adapter != null) {
							adapter.showName = arg1;
							adapter.notifyDataSetChanged();
						}
					}
				});
		CheckBox disturb = ((CheckBox) findViewById(R.id.no_disturb));
		boolean dis = PreferenceUitl.getBooleanValue(RoomInfoPage.this,
				"r_disturb_" + room.getId());
		disturb.setChecked(dis);
		disturb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				PreferenceUitl.setBooleanValue(RoomInfoPage.this, "r_disturb_"
						+ room.getId(), arg1);
			}
		});
	}

	public void back(View view) {
		finish();
	}

	@Override
	protected void onDestroy() {
		api.removeListener(mDelegate);
		super.onDestroy();
	}

	private GotyeDelegate mDelegate = new GotyeDelegate(){
		
		@Override
		public void onEnterRoom(int code, GotyeRoom room) {
			ProgressDialogUtil.dismiss();
			if (code == GotyeStatusCode.CodeOK) {
				api.reqRoomMemberList(RoomInfoPage.this.room, 0);
			}
		}

		@Override
		public void onDownloadMedia(int code, GotyeMedia media) {
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onGetRoomMemberList(int code, GotyeRoom room, int pageIndex,
				List<GotyeUser> currentPageMembers, List<GotyeUser> totalMembers) {
			if (totalMembers != null) {
				adapter = new MemberAdapter(RoomInfoPage.this, totalMembers);
				adapter.showName = PreferenceUitl.getBooleanValue(
						RoomInfoPage.this, "r_show_name_" + room.getId());
				memberView.setAdapter(adapter);
			}
		}
		
	};

}
