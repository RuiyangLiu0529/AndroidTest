package com.twofours.surespot.chat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.twofours.surespot.activities.MainActivity;
import com.twofours.surespot.common.FileUtils;
import com.twofours.surespot.common.SurespotLog;
import com.twofours.surespot.common.Utils;
import com.twofours.surespot.encryption.EncryptionController;
import com.twofours.surespot.identity.IdentityController;
import com.twofours.surespot.network.IAsyncCallback;
import com.twofours.surespot.ui.UIUtils;

public class ImageMessageMenuFragment extends SherlockDialogFragment {
	protected static final String TAG = "ImageMessageMenuFragment";
	private SurespotMessage mMessage;
	private MainActivity mActivity;
	private ArrayList<String> mItems;
	private Observer mMessageObserver;

	public void setActivityAndMessage(MainActivity activity, SurespotMessage message) {
		mMessage = message;
		mActivity = activity;
	}

	private void setButtonVisibility() {
		AlertDialog dialog = (AlertDialog) ImageMessageMenuFragment.this.getDialog();

		if (dialog != null) {
			ListView listview = dialog.getListView();

			ListIterator<String> li = mItems.listIterator();
			while (li.hasNext()) {
				String item = li.next();

				if (item.equals("save to gallery")) {
					listview.getChildAt(li.previousIndex()).setEnabled(mMessage.isShareable() && FileUtils.isExternalStorageMounted());
					return;
				}
			}
		}
		else {
			mActivity = null;
			mMessage.deleteObserver(mMessageObserver);
			mMessage = null;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		if (mMessage == null) {
			return null;
		}

		mItems = new ArrayList<String>(5);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// builder.setTitle(R.string.pick_color);

		// if we have an errored image we can resend it
		if (mMessage.getFrom().equals(IdentityController.getLoggedInUser()) && mMessage.getErrorStatus() > 0) {
			mItems.add("resend message");
		}

		// if it's not our message we can save it to gallery
		if (!mMessage.getFrom().equals(IdentityController.getLoggedInUser())) {
			mItems.add("save to gallery");

		}
		// if it's our message and it's been sent we can mark it locked or unlocked
		if (mMessage.getId() != null && mMessage.getFrom().equals(IdentityController.getLoggedInUser())) {
			mItems.add(mMessage.isShareable() ? "lock" : "unlock");
		}

		// can always delete
		mItems.add("delete message");

		builder.setItems(mItems.toArray(new String[mItems.size()]), new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialogi, int which) {
				if (mMessage == null)
					return;

				mMessage.deleteObservers();

				AlertDialog dialog = (AlertDialog) ImageMessageMenuFragment.this.getDialog();
				ListView listview = dialog.getListView();

				if (!listview.getChildAt(which).isEnabled()) {
					return;
				}

				String itemText = mItems.get(which);

				if (itemText.contains("lock")) {
					mActivity.getChatController().toggleMessageShareable(mMessage);
					return;
				}

				if (itemText.equals("save to gallery")) {

					// Utils.makeToast(mActivity, "saving image in gallery");
					new AsyncTask<Void, Void, Boolean>() {

						@Override
						protected Boolean doInBackground(Void... params) {
							try {

								File galleryFile = FileUtils.createGalleryImageFile(".jpg");
								FileOutputStream fos = new FileOutputStream(galleryFile);
								InputStream imageStream = MainActivity.getNetworkController().getFileStream(mActivity, mMessage.getData());

								EncryptionController.runDecryptTask(mMessage.getOurVersion(), mMessage.getOtherUser(),
										mMessage.getTheirVersion(), mMessage.getIv(), new BufferedInputStream(imageStream), fos);

								FileUtils.galleryAddPic(mActivity, galleryFile.getAbsolutePath());
								return true;

							}

							catch (IOException e) {
								SurespotLog.w(TAG, "onCreateDialog", e);

							}
							return false;
						}

						protected void onPostExecute(Boolean result) {
							if (result) {
								Utils.makeToast(mActivity, "image saved to gallery");
							}
							else {
								Utils.makeToast(mActivity, "error saving image to gallery");
							}
						};
					}.execute();
					return;

				}

				if (itemText.equals("delete message")) {
					SharedPreferences sp = mActivity.getSharedPreferences(IdentityController.getLoggedInUser(), Context.MODE_PRIVATE);
					boolean confirm = sp.getBoolean("pref_delete_message", true);
					if (confirm) {
						UIUtils.createAndShowConfirmationDialog(mActivity, "are you sure you wish to delete this message?",
								"delete message", "ok", "cancel", new IAsyncCallback<Boolean>() {
									public void handleResponse(Boolean result) {
										if (result) {
											mActivity.getChatController().deleteMessage(mMessage);
										}
										else {
											dialogi.cancel();
										}
									};
								});
					}
					else {
						mActivity.getChatController().deleteMessage(mMessage);
					}

					return;
				}

				if (itemText.equals("resend message")) {
					mActivity.getChatController().resendPictureMessage(mMessage);
					return;
				}

			}
		});

		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				setButtonVisibility();

			}
		});

		// TODO listen to message control events and handle delete as well
		mMessageObserver = new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				setButtonVisibility();

			}
		};
		if (mMessage != null) {
			mMessage.addObserver(mMessageObserver);
		}
		return dialog;
	}

}
