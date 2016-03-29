package com.open_demo.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatTargetType;
import com.gotye.api.GotyeDelegate;
import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeGroupMsgConfig;
import com.gotye.api.GotyeGroupType;
import com.gotye.api.GotyeMedia;
import com.gotye.api.GotyeNotify;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.open_demo.MyApplication;
import com.open_demo.R;
import com.open_demo.adapter.GroupMemberAdapter;
import com.open_demo.util.BitmapUtil;
import com.open_demo.util.ImageCache;
import com.open_demo.util.PreferenceUitl;
import com.open_demo.util.ProgressDialogUtil;
import com.open_demo.util.ToastUtil;
import com.open_demo.util.URIUtil;
import com.open_demo.util.ViewHelper;
import com.open_demo.view.ChangeGroupOwnerDialog;

public class GroupInfoPage extends Activity {
	private GotyeGroup group;
	private static final int REQUEST_PIC = 1;
	private GroupMemberAdapter adapter;
	private GridView memberView;
	private ImageView ownerIcon;
	private GotyeUser groupOwner;
	private View delDialog;
	private String currentLoginName;
	private List<GotyeUser> members;
	private Button joinGroupBtn, dismissGroupBtn, leaveGroupBtn;

	private EditText groupName, infoVIew;
	private ImageView groupIcon;
	private PopupWindow mPopupWindow;
	private TextView mTxtGroupSelect;
	private CheckBox needValidate, isPublic;
	private boolean canModify = false;
	private ImageView imgOne, imgTwo, imgThree;
	private GotyeGroupMsgConfig configTag;
	private RelativeLayout groupSetting;
	private int intCode = -1;
	public GotyeAPI api=GotyeAPI.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		currentLoginName = api.getLoginUser().getName();
		group = (GotyeGroup) getIntent().getSerializableExtra("group");
		intCode = getIntent().getIntExtra("intcode", -1);
		group = api.getGroupDetail(group, true);

		if (currentLoginName.equals(group.getOwnerAccount())) {
			canModify = true;
		}
		setContentView(R.layout.layout_group_info);
		api.addListener(mDelegate);
		groupOwner = api.getUserDetail(group, true);
		initView();

	}

	private void initView() {
		mTxtGroupSelect = (TextView)findViewById(R.id.msg_setting_selected);
		groupSetting = (RelativeLayout)findViewById(R.id.group_setting);
		if(intCode ==100){
			groupSetting.setVisibility(View.GONE);
		}
		configTag = api.getGroupMsgConfig(group,true);
		memberView = (GridView) findViewById(R.id.members);
		((TextView) findViewById(R.id.owner_name)).setText(group
				.getOwnerAccount());
		((TextView) findViewById(R.id.group_name))
				.setText(group.getGroupName());

		groupName = (EditText) findViewById(R.id.for_modify_group_name);
		groupName.setText(group.getGroupName());
		groupName.setEnabled(canModify);
		groupName.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		groupName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {

				if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
					modify("");
					return true;
				}
				return false;
			}
		});

		infoVIew = (EditText) findViewById(R.id.info);
		infoVIew.setText(group.getInfo());
		infoVIew.setEnabled(canModify);
		infoVIew.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		infoVIew.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {

				if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
					modify("");
					return true;
				}
				return false;
			}
		});

		isPublic = (CheckBox) findViewById(R.id.is_public);
		isPublic.setChecked(group.getOwnerType() == GotyeGroupType.GotyeGroupTypePublic);
		isPublic.setEnabled(canModify);
		isPublic.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				modify("");
			}
		});

		needValidate = (CheckBox) findViewById(R.id.need_validate);
		needValidate.setChecked(group.isNeedAuthentication());
		needValidate.setEnabled(canModify);
		needValidate.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				modify("");
			}
		});
		groupIcon = (ImageView) findViewById(R.id.group_ioon);
		groupIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (canModify) {
					Intent intent;
					intent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image/jpeg");
					startActivityForResult(intent, REQUEST_PIC);
				}

			}
		});
		if (group.getIcon() != null) {
			Bitmap icon = BitmapUtil.getBitmap(group.getIcon().getPath());
			if (icon != null) {
				groupIcon.setImageBitmap(icon);
			}
			api.downloadMedia(group.getIcon());
		}

		CheckBox set_to_top = ((CheckBox) findViewById(R.id.set_to_top));
		boolean setTop = PreferenceUitl.getBooleanValue(this, "set_top_"
				+ group.getGroupID());
		set_to_top.setChecked(setTop);
		set_to_top.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				PreferenceUitl.setBooleanValue(GroupInfoPage.this, "set_top_"
						+ group.getGroupID(), arg1);
				api.markSessionIsTop(group, arg1);
			}
		});

		ownerIcon = (ImageView) findViewById(R.id.group_owner_icon);
		delDialog = findViewById(R.id.del_dialog);

		joinGroupBtn = (Button) findViewById(R.id.join_group);
		joinGroupBtn.setVisibility(View.GONE);
		dismissGroupBtn = (Button) findViewById(R.id.dismiss_group);
		leaveGroupBtn = (Button) findViewById(R.id.leave_group);
		if (members == null) {
			members = new ArrayList<GotyeUser>();
		}

		if (group.getOwnerAccount().equals(currentLoginName)) {
			dismissGroupBtn.setVisibility(View.VISIBLE);
			GotyeUser add = new GotyeUser();
			add.setName("");
			members.add(add);
		} else {
			dismissGroupBtn.setVisibility(View.GONE);
		}

		if (adapter == null) {
			adapter = new GroupMemberAdapter(this, group, members);
		}
		memberView.setAdapter(adapter);
		setListener();
		api.reqGroupMemberList(group, 0);
//		api.getGroupDetail(group.getGroupID(), true);
		api.getGroupDetail(group, true);
	}

	private void refreshValue() {
		groupName.setText(group.getGroupName());
		infoVIew.setText(group.getInfo());
		needValidate.setChecked(group.isNeedAuthentication());
		isPublic.setChecked(group.getOwnerType() == GotyeGroupType.GotyeGroupTypePublic);
		if (group.getIcon() != null) {
			Bitmap	icon = BitmapUtil.getBitmap(group.getIcon().getPath());
			if(icon==null){
				api.downloadMedia(group.getIcon());
			}else{
				ImageCache.getInstance().put(group.getId()+"", icon);
				groupIcon.setImageBitmap(icon);
			}
			
		}
	}

	private void setListener() {
		memberView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (group.getOwnerAccount().equals(currentLoginName)) {
					adapter.setDeleteFlag(true);
					return true;
				}
				return false;
			}
		});
		memberView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				GotyeUser user = (GotyeUser) adapter.getItem(arg2);
				if (user.getName().equals("")) {
					ArrayList<String> names = new ArrayList<String>();
					for (GotyeUser userInGroup : members) {
						if (!TextUtils.isEmpty(userInGroup.getName())) {
							names.add(userInGroup.getName());
						}
					}
					Intent intent = new Intent(GroupInfoPage.this,
							CreateGroupSelectUser.class);
					intent.putExtra("from", 1);
					intent.putStringArrayListExtra("members", names);
					startActivityForResult(intent, 0);
					adapter.setDeleteFlag(false);
					return;
				}
				if (adapter.isDeleteFlag()) {
					if (user.getName().equals(currentLoginName)) {
						return;
					}
					dialogToDeleteMember(user);
					return;
				}
			}
		});
		CheckBox showMemberName = ((CheckBox) findViewById(R.id.show_member_name));
		showMemberName.setChecked(PreferenceUitl.getBooleanValue(
				GroupInfoPage.this, "g_show_name_" + group.getGroupID()));
		showMemberName
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						PreferenceUitl.setBooleanValue(GroupInfoPage.this,
								"g_show_name_" + group.getGroupID(), arg1);
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
					}
				});

//		CheckBox disturb = ((CheckBox) findViewById(R.id.no_disturb));
//		disturb.setChecked(MyApplication.isGroupDontdisturb(group.getGroupID()));
//		disturb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
//				if (arg1) {
//					MyApplication.setGroupDontdisturb(group.getGroupID());
//				} else {
//					MyApplication.removeGroupDontdisturb(group.getGroupID());
//				}
//			}
//		});
		ImageView msgSetting = ((ImageView)findViewById(R.id.msg_selected));
		msgSetting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showPopupWindow(arg0);
			}
		});
		
	}
	
	public void showPopupWindow(View view) {
		Context context = GroupInfoPage.this;
		RelativeLayout reOne;
		RelativeLayout reTwo;
		RelativeLayout reThree;
		RelativeLayout reCancel;
		LayoutInflater mLayoutInflater = (LayoutInflater) context
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View view_popunwindow = LayoutInflater.from(context)
				.inflate(
						getResources().getLayout(
								R.layout.group_msg_mark_popwin), null);

		reOne = (RelativeLayout) view_popunwindow.findViewById(R.id.select_linear_one);
		reTwo = (RelativeLayout) view_popunwindow.findViewById(R.id.select_linear_two);
		reThree = (RelativeLayout) view_popunwindow.findViewById(R.id.select_linear_three);
		reCancel = (RelativeLayout)view_popunwindow.findViewById(R.id.select_linear_four);
		imgOne = (ImageView)view_popunwindow.findViewById(R.id.check_one);
		imgTwo = (ImageView)view_popunwindow.findViewById(R.id.check_two);
		imgThree = (ImageView)view_popunwindow.findViewById(R.id.check_three);
		if(configTag == GotyeGroupMsgConfig.ShieldingGroupMsg ){
			imgOne.setVisibility(View.GONE);
			imgTwo.setVisibility(View.GONE);
			imgThree.setVisibility(View.VISIBLE);
		}else if(configTag == GotyeGroupMsgConfig.ReceivingGroupMsg){
				imgOne.setVisibility(View.VISIBLE);
				imgTwo.setVisibility(View.GONE);
				imgThree.setVisibility(View.GONE);
		}else if(configTag == GotyeGroupMsgConfig.ReceivingGroupMsgAndNotice){
			imgOne.setVisibility(View.GONE);
			imgTwo.setVisibility(View.VISIBLE);
			imgThree.setVisibility(View.GONE);
		}
		reOne.setOnClickListener(new OnClikImpl());
		reTwo.setOnClickListener(new OnClikImpl());
		reThree.setOnClickListener(new OnClikImpl());
		reCancel.setOnClickListener(new OnClikImpl());

		 int screenWidth=getResources().getDisplayMetrics().widthPixels;
		 int screenHeight=getResources().getDisplayMetrics().heightPixels;
		mPopupWindow = new PopupWindow(view_popunwindow, 
				screenWidth-20, ViewHelper.getHeight(view_popunwindow), false);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(),
				(Bitmap) null));
		mPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.update();
	}
	
	public class OnClikImpl implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.select_linear_one:
				api.setGroupMsgConfig(group, GotyeGroupMsgConfig.ReceivingGroupMsg);
				MyApplication.setGroupDontdisturb(group.getGroupID());
				mPopupWindow.dismiss();
				break;
			case R.id.select_linear_two:
				api.setGroupMsgConfig(group, GotyeGroupMsgConfig.ReceivingGroupMsgAndNotice);
				MyApplication.removeGroupDontdisturb(group.getGroupID());
				mPopupWindow.dismiss();
				break;
			case R.id.select_linear_three:
				api.setGroupMsgConfig(group, GotyeGroupMsgConfig.ShieldingGroupMsg);
				mPopupWindow.dismiss();
				break;
			case R.id.select_linear_four:
				mPopupWindow.dismiss();
				break;
			default:
				break;
			}
		}
	}

	private void modify(String path) {
		String grounName = groupName.getText().toString().trim();
		String groupInfo = infoVIew.getText().toString().trim();
		boolean neewValidate = needValidate.isChecked();
		boolean publicState = isPublic.isChecked();

		if (TextUtils.isEmpty(grounName)) {
			ToastUtil.show(this, "群名不能改为空");
			return;
		}
		GotyeGroup forModify = new GotyeGroup(group.getGroupID());
		forModify.setGroupName(grounName);
		forModify.setInfo(groupInfo);
		forModify.setOwnerType(GotyeGroupType.values()[publicState ? 0 : 1]);
		forModify.setNeedAuthentication(neewValidate);
		int code = api.reqModifyGroupInfo(forModify, path);
		Log.d("Code", "code="+code);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (data != null) {
				Uri selectedImage = data.getData();
				if (selectedImage != null) {
					String path = URIUtil.toPath(this, selectedImage);
					if (!TextUtils.isEmpty(path)) {
						setPicture(path);
					} else {
						ToastUtil.show(this, "文件不存在");
					}
				}
			}
		} else {
			if (data != null) {
				String members = data.getStringExtra("member");
				if (members != null && !"".equals(members)) {
					String[] memberlist = members.split(",");
					for (String member : memberlist) {
						api.inviteUserToGroup(new GotyeUser(member), group,
								"进来聊聊");
					}
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setPicture(String path) {
		File f = new File(path);
		String smallImagePath = path;
		if (f.exists()) {
			if (f.length() > 400) {
				smallImagePath = BitmapUtil.compressImage(path);
			}
		}
		smallImagePath = BitmapUtil.check(smallImagePath);
		if (!TextUtils.isEmpty(smallImagePath)) {
			modify(smallImagePath);
		}
	}

	public void back(View view) {
		if (delDialog.getVisibility() == View.VISIBLE) {
			delDialog.setVisibility(View.GONE);
			return;
		}
		finish();
	}

	private void setGroupMember(List<GotyeUser> members) {
		if (members == null) {
			return;
		}
		this.members.addAll(0, members);
//		boolean isContain = false;
//		GotyeUser cuser = api.getCurrentLoginUser();
//		for (GotyeUser gotyeUser : members) {
//			if(gotyeUser.getName().equals(cuser.getName())){
//				isContain = true;
//				break;
//			}
//		}
//		if (!isContain) {
//			joinGroupBtn.setVisibility(View.VISIBLE);
//		} else {
//			leaveGroupBtn.setVisibility(View.VISIBLE);
//		}
		GotyeUser cUser = api.getLoginUser();
		if (!members.contains(cUser)) {
			joinGroupBtn.setVisibility(View.VISIBLE);
		} else {
			leaveGroupBtn.setVisibility(View.VISIBLE);
		}

		if (members.contains(cUser)) {
			joinGroupBtn.setVisibility(View.GONE);
		} else {
			joinGroupBtn.setVisibility(View.VISIBLE);
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		back(null);
	}

	@Override
	protected void onDestroy() {
//		api.removeListener(this);
		api.removeListener(mDelegate);
		super.onDestroy();
	}

	public void joinGroup(View view) {
		if (group.isNeedAuthentication()) {
			new AlertDialog.Builder(this)
					.setMessage("是否申请加入该群？")
					.setPositiveButton("申请",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									ProgressDialogUtil.showProgress(
											GroupInfoPage.this, "正在发送申请信息...");
									api.reqJoinGroup(group, "群主好人，求加入...");
								}
							}).setNegativeButton("取消", null).create().show();
		} else {
			ProgressDialogUtil.showProgress(this, "正在加入群...");
			api.joinGroup(group);
		}

	}

	public void dismissGroup(View view) {

		Dialog d = new AlertDialog.Builder(this).setMessage("确定解散该群?")
				.setPositiveButton("解散", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						api.dismissGroup(group);
					}
				}).setNegativeButton("取消", null).create();
		d.show();
	}

	public void leaveGroup(View view) {
		if (!currentLoginName.equals(group.getOwnerAccount())) {
			ProgressDialogUtil.showProgress(this, "正在离开群...");
			api.leaveGroup(group);
			return;
		}
		if (members.size() == 1) {
			ProgressDialogUtil.showProgress(this, "正在离开群...");
			api.leaveGroup(group);
			return;
		}

		if (members.size() == 2 && members.contains(new GotyeUser(""))) {
			ProgressDialogUtil.showProgress(this, "正在离开群...");
			api.leaveGroup(group);
			return;
		}

		List<GotyeUser> toSelected = new ArrayList<GotyeUser>();
		for (GotyeUser user : members) {
			if (user.getName().equals(group.getOwnerAccount())
					|| "".equals(user.getName())) {
				continue;
			}
			toSelected.add(user);
		}

		if (toSelected.size() == 0) {
			ProgressDialogUtil.showProgress(this, "正在离开群...");
			api.leaveGroup(group);
			return;
		}
		ChangeGroupOwnerDialog change = new ChangeGroupOwnerDialog(this);
		change.show();
		change.setMembers(group, toSelected);
	}

	public void dialogToDeleteMember(final GotyeUser member) {
		delDialog.setVisibility(View.VISIBLE);
		((TextView) delDialog.findViewById(R.id.content)).setText("您确定要将"
				+ member.getName() + "请出本群?");
		delDialog.findViewById(R.id.dialog_sure).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						api.kickoutGroupMember(group, member);
						delDialog.setVisibility(View.GONE);
						ProgressDialogUtil.showProgress(GroupInfoPage.this,
								"正在踢出成员：" + member.getName());
					}
				});
		delDialog.findViewById(R.id.dialog_cancel).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						delDialog.setVisibility(View.GONE);
					}
				});
	}

	
	private GotyeDelegate mDelegate = new GotyeDelegate(){
		
		@Override
		public void onJoinGroup(int code, GotyeGroup group) {
			ProgressDialogUtil.dismiss();
			if (code == 0) {
				ToastUtil.show(GroupInfoPage.this, "成功加入该群");
				joinGroupBtn.setVisibility(View.GONE);
				leaveGroupBtn.setVisibility(View.VISIBLE);
				if (members == null) {
					members = new ArrayList<GotyeUser>();
				}
				if (!members.contains(api.getLoginUser())) {
					members.add(api.getLoginUser());
				}
				adapter.notifyDataSetChanged();
			} else {
				ToastUtil.show(GroupInfoPage.this, "加群失败");
			}

		}

		@Override
		public void onLeaveGroup(int code, GotyeGroup group) {
			if (code == GotyeStatusCode.CodeOK) {
				ToastUtil.show(GroupInfoPage.this, "您成功离开了该群");
				finish();

				Intent i = new Intent(GroupInfoPage.this, GroupRoomListPage.class);
				i.putExtra("group_id", group.getGroupID());
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);

			} else {
				Toast.makeText(getBaseContext(), "离开群失败 code=" + code,
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onDismissGroup(int code, GotyeGroup group) {
			if (code == GotyeStatusCode.CodeOK) {
				Intent i = new Intent(GroupInfoPage.this, GroupRoomListPage.class);
				i.putExtra("group_id", group.getGroupID());
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Toast.makeText(getBaseContext(), "您成功解散了该群", Toast.LENGTH_SHORT)
						.show();
				finish();
				startActivity(i);
			} else {
				Toast.makeText(getBaseContext(), "解散群失败 code=" + code,
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onKickoutGroupMember(int code, GotyeGroup group,GotyeUser kickedMember) {
			if (code == 0) {
				members.clear();
				api.reqGroupMemberList(group, 0);
				adapter.setDeleteFlag(false);
				adapter.notifyDataSetChanged();
				ProgressDialogUtil.dismiss();
				ToastUtil.show(GroupInfoPage.this, "踢出成功!");
				GotyeUser add = new GotyeUser();
				add.setName("");
				members.add(add);
			}

		}

		@Override
		public void onDownloadMedia(int code, GotyeMedia media) {
			if (group.getIcon() != null
					&& media.getUrl().equals(group.getIcon().getUrl())) {
				Bitmap icon = BitmapUtil.getBitmap(group.getIcon().getPath());
				if (icon != null) {
					ImageCache.getInstance().put(group.getId()+"", icon);
					groupIcon.setImageBitmap(icon);
				}
				return;
			}
			if (GroupInfoPage.this.groupOwner != null
					&& groupOwner.getIcon().getUrl().equals(media.getUrl())) {
				Bitmap bmp = BitmapUtil.getBitmap(media.getPath());
				if (bmp != null) {
					ownerIcon.setImageBitmap(bmp);
				}
				return;
			}
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onGetGroupDetail(int code, GotyeGroup group) {
			if(GroupInfoPage.this.group.getId()==group.getId()){
				GroupInfoPage.this.group=group;
			}
		}
		
		@Override
		public void onGetUserDetail(int code, GotyeUser user) {
			adapter.notifyDataSetChanged();
			if (user.getName().equals(group.getOwnerAccount())) {
				groupOwner = user;
				if (user.getIcon() != null) {
					Bitmap userIcon = BitmapUtil
							.getBitmap(user.getIcon().getPath());
					if (userIcon != null) {
						ownerIcon.setImageBitmap(userIcon);
					} else {
						api.downloadMedia(user.getIcon());
					}
				}
			}
		}

		@Override
		public void onGetGroupMemberList(int code,GotyeGroup group, int pagerIndex, List<GotyeUser> allList,
				List<GotyeUser> curList) {
			setGroupMember(allList);
		}

		@Override
		public void onModifyGroupInfo(int code, GotyeGroup gotyeGroup) {
			if (code == 0) {
				ToastUtil.show(GroupInfoPage.this, "修改成功");
				GroupInfoPage.this.group = gotyeGroup;
				if (gotyeGroup.getIcon() != null) {
					if (!TextUtils.isEmpty(gotyeGroup.getIcon().getPath())) {
						Bitmap bmp = BitmapUtil.getBitmap(gotyeGroup.getIcon()
								.getPath());
						if (bmp != null) {
							ImageCache.getInstance().put(gotyeGroup.getId() + "",
									bmp);
						}
					}
				}
			} else {
				ToastUtil.show(GroupInfoPage.this, "修改失败");
			}
			refreshValue();
		}

		@Override
		public void onChangeGroupOwner(int code, GotyeGroup group,GotyeUser newOwner) {
			if (group.getGroupID() == GroupInfoPage.this.group.getGroupID()) {
				ProgressDialogUtil.dismiss();
				ToastUtil.show(GroupInfoPage.this, "您成功转让该群");
				ProgressDialogUtil.showProgress(GroupInfoPage.this, "正在退出群...");
				api.leaveGroup(group);
			}
		}

		@Override
		public void onUserJoinGroup(GotyeGroup group, GotyeUser user) {
			// TODO Auto-generated method stub
			if (group.getGroupID() == GroupInfoPage.this.group.getGroupID()) {
				if (adapter.isDeleteFlag()) {
					members.add(user);
				} else {
					if (!members.contains(user)) {
						members.add(members.size() - 1, user);
					}
				}
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onUserLeaveGroup(GotyeGroup group, GotyeUser user) {
			// TODO Auto-generated method stub
			if (group.getGroupID() == GroupInfoPage.this.group.getGroupID()) {
				members.remove(user);
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onSendNotify(int code, GotyeNotify notify) {
			// TODO Auto-generated method stub
			ProgressDialogUtil.dismiss();
			if (code == GotyeStatusCode.CodeOK) {
				ToastUtil.show(GroupInfoPage.this, "成功发送邀请，等待好友回应");
			} else {
				ToastUtil.show(GroupInfoPage.this, "发送请求失败!");
			}
		}

		@Override
		public void onUserDismissGroup(GotyeGroup group, GotyeUser user) {
			// TODO Auto-generated method stub
			if (group.getGroupID() == GroupInfoPage.this.group.getGroupID()) {
				Intent i = new Intent(GroupInfoPage.this, GroupRoomListPage.class);
				i.putExtra("group_id", group.getGroupID());
				i.putExtra("type", 1);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				if(!(user.getName()).equals(currentLoginName)){
//					Toast.makeText(getBaseContext(), "群主解散了该群", Toast.LENGTH_SHORT)
//					.show();
//				}
				finish();
				startActivity(i);
			}
		}
		
		@Override
		public void onUserKickedFromGroup(GotyeGroup group, GotyeUser kicked,
				GotyeUser actor) {
			if (group.getGroupID() == GroupInfoPage.this.group.getGroupID()) {
				if (kicked.getName().equals(currentLoginName)) {
					Intent i = new Intent(GroupInfoPage.this, GroupRoomListPage.class);
					i.putExtra("group_id", group.getGroupID());
					i.putExtra("type", 1);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Toast.makeText(getBaseContext(),
							"您被" + actor.getName() + "踢出该群了", Toast.LENGTH_SHORT)
							.show();
					finish();
					startActivity(i);
				}
			}
		}
		
		@Override
		public void onSetGroupMsgConfig(int code, GotyeGroup group, int config){
			Log.d("MsgConfig", "code = "+code);
			if(code == GotyeStatusCode.CodeOK){
				if(config == 1){
					configTag = GotyeGroupMsgConfig.ReceivingGroupMsg;
					mTxtGroupSelect.setText("接收不提醒");  
				}else if(config == 3){
					configTag = GotyeGroupMsgConfig.ReceivingGroupMsgAndNotice;
					mTxtGroupSelect.setText("接收并提醒");  
				}else if(config == 0){
					configTag = GotyeGroupMsgConfig.ShieldingGroupMsg;
					mTxtGroupSelect.setText("屏蔽群消息");  
				}
			}
		}
		
		@Override
		public void onGetGroupMsgConfig(int code, GotyeGroup group, int config){
			Log.d("MsgConfig", "code get= "+code);
			if(code == GotyeStatusCode.CodeOK){
				configTag = api.getGroupMsgConfig(group, false);
				if(configTag == GotyeGroupMsgConfig.ReceivingGroupMsg ){
					mTxtGroupSelect.setText("接收不提醒");
				}else if(configTag == GotyeGroupMsgConfig.ReceivingGroupMsgAndNotice){
					mTxtGroupSelect.setText("接收并提醒");
				}else if(configTag == GotyeGroupMsgConfig.ShieldingGroupMsg){
					mTxtGroupSelect.setText("屏蔽群消息");
				}else if(configTag.ordinal() == 65535){//第一次默认情况
					api.setGroupMsgConfig(group, GotyeGroupMsgConfig.ReceivingGroupMsg);
					mTxtGroupSelect.setText("接收并提醒");
					configTag = GotyeGroupMsgConfig.ReceivingGroupMsgAndNotice;
				}			
			}
		}
	};
}
