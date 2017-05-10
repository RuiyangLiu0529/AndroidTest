package com.twofours.surespot.identity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.NotificationManager;
import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;

import com.twofours.surespot.R;
import com.twofours.surespot.common.FileUtils;
import com.twofours.surespot.common.SurespotConstants;
import com.twofours.surespot.common.SurespotLog;
import com.twofours.surespot.common.Utils;

public class IdentityBackupAgent extends BackupAgent {
	private static final String TAG = null;

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {

		List<String> names = IdentityController.getIdentityNames(this);
		List<String> deletedNames = IdentityController.getDeletedIdentityNames(this);

		List<String> backedUp = new ArrayList<String>(names.size());
		Iterator<String> iterator = names.iterator();

		while (iterator.hasNext()) {
			String name = iterator.next();
			if (getSharedPreferences(name, MODE_PRIVATE).getBoolean("pref_auto_android_backup_enabled", false)) {
				String filename = FileUtils.getIdentityDir(this) + File.separator + name + IdentityController.IDENTITY_EXTENSION;
				SurespotLog.v(TAG, "backing up identity: " + filename);

				synchronized (IdentityController.IDENTITY_FILE_LOCK) {
					//identity
					FileInputStream fis = new FileInputStream(filename);
					byte[] buffer = Utils.inputStreamToBytes(fis);
					int len = buffer.length;
					data.writeEntityHeader("identity:" + name, len);
					data.writeEntityData(buffer, len);
					fis.close();

					//shared prefs
					filename = this.getApplicationInfo().dataDir + File.separator + "shared_prefs" + File.separator + name + ".xml";
					SurespotLog.v(TAG, "backing up shared prefs: " + filename);
					fis = new FileInputStream(filename);

					buffer = Utils.inputStreamToBytes(fis);
					len = buffer.length;
					data.writeEntityHeader("sharedPref:" + name, len);
					data.writeEntityData(buffer, len);
					fis.close();
				}
				
				backedUp.add(name);

			}
			else {
				//delete the backup if there is one
				deletedNames.add(name);
			}
		}


		iterator = deletedNames.iterator();

		while (iterator.hasNext()) {
			String name = iterator.next();
			SurespotLog.v(TAG, "deleting identity backup for: %s", name);

			synchronized (IdentityController.IDENTITY_FILE_LOCK) {
				//delete identity backup
				data.writeEntityHeader("identity:" + name, -1);
				String filename = FileUtils.getIdentityDir(this) + File.separator + name + IdentityController.IDENTITY_DELETED_EXTENSION;
				new File(filename).delete();

				//delete shared prefs backup
				data.writeEntityHeader("sharedPref:" + name, -1);
				filename = this.getApplicationInfo().dataDir + File.separator + "shared_prefs" + File.separator + name + ".xml";
				SurespotLog.v(TAG, "deleting shared prefs backup for: %s", name);
				new File(filename).delete();
			}
		}

		if (backedUp.size() > 0) {
			createBackedupNotification(backedUp);
		}

	}

	public void createBackedupNotification(List<String> backedUp) {

		int icon = R.drawable.surespot_logo;
		String message = "";
		// don't get big notifications till 4.1
		if (backedUp.size() == 1) {
			message = "identity " + backedUp.get(0) + " backed up";
		}
		else {
			message = String.valueOf(backedUp.size()) + " identities backed up";
		}

		NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(icon)
				.setContentTitle("identity backup complete").setContentText(message);

		notificationManager.notify(SurespotConstants.IntentRequestCodes.BACKUP_NOTIFICATION, builder.build());
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {

		String identitydirname = FileUtils.getIdentityDir(this);
		File dir = new File(identitydirname);
		dir.mkdirs();

		while (data.readNextHeader()) {
			String key = data.getKey();

			if (key.startsWith("identity:")) {
				String[] split = key.split(":");
				String name = split[1];

				String filename = identitydirname + File.separator + name + IdentityController.IDENTITY_EXTENSION;
				int dataSize = data.getDataSize();

				synchronized (IdentityController.IDENTITY_FILE_LOCK) {
					FileOutputStream fos = new FileOutputStream(filename);
					SurespotLog.v(TAG, "restoring identity: " + filename);

					byte[] dataBuf = new byte[dataSize];
					data.readEntityData(dataBuf, 0, dataSize);

					fos.write(dataBuf);
					fos.close();
				}
			}
			else {
				if (key.startsWith("sharedPref:")) {
					String[] split = key.split(":");
					String name = split[1];
					String sharedPrefsFile = this.getApplicationInfo().dataDir + File.separator + "shared_prefs" + File.separator + name
							+ ".xml";

					FileOutputStream fos = new FileOutputStream(sharedPrefsFile);
					SurespotLog.v(TAG, "restoring shared prefs: " + sharedPrefsFile);

					int dataSize = data.getDataSize();

					byte[] dataBuf = new byte[dataSize];
					data.readEntityData(dataBuf, 0, dataSize);

					fos.write(dataBuf);
					fos.close();
				}
			}
		}

	}

}
