package com.dasudian.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class UserListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userlist);
	}
	
	public void onClick1(View view) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra("name", "user1");
		startActivity(intent);
	}
	
	public void onClick2(View view) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra("name", "user2");
		startActivity(intent);
	}
}
