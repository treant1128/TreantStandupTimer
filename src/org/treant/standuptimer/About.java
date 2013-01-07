package org.treant.standuptimer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class About extends Activity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.layout_about);
		View okButton = findViewById(R.id.about_ok);
		okButton.setOnClickListener(this);
		View projectUrl = findViewById(R.id.project_url);
		projectUrl.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.about_ok:
			finish();
			break;
		case R.id.project_url:
			openProjectUrlBrowser();
			break;
		}
	}

	private void openProjectUrlBrowser() {
		Uri uri = Uri.parse(this.getString(R.string.about_project_url));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
}
