package com.twofours.surespot.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.twofours.surespot.R;
import com.twofours.surespot.SurespotApplication;
import com.twofours.surespot.common.Utils;
import com.twofours.surespot.identity.IdentityController;
import com.twofours.surespot.ui.UIUtils;

public class AutoBackupActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_backup);
		
		Utils.configureActionBar(this, "identity", "auto backup", true);

		String user = IdentityController.getLoggedInUser();	

		TextView t1 = (TextView) findViewById(R.id.helpAutoBackup1);
		UIUtils.setHtml(this, t1, R.string.help_auto_backup1);

		TextView t2 = (TextView) findViewById(R.id.helpAutoBackup2);
		UIUtils.setHtml(this, t2, R.string.help_auto_backup2);

		final SharedPreferences sp = getSharedPreferences(user, Context.MODE_PRIVATE);
		boolean abEnabled = sp.getBoolean("pref_auto_android_backup_enabled", false);

		CheckBox cb = (CheckBox) findViewById(R.id.cbAutoBackup);
		cb.setChecked(abEnabled);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor = sp.edit();
				editor.putBoolean("pref_auto_android_backup_enabled", isChecked);
				editor.commit();

				SurespotApplication.mBackupManager.dataChanged();

			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
}
