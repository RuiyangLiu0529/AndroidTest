package com.twofours.surespot.friends;

import org.json.JSONException;
import org.json.JSONObject;

import com.twofours.surespot.common.SurespotLog;
import com.twofours.surespot.ui.AlphanumComparator;

public class Friend implements Comparable<Friend> {
	public static final int INVITER = 32;
	public static final int MESSAGE_ACTIVITY = 16;
	public static final int CHAT_ACTIVE = 8;
	public static final int NEW_FRIEND = 4;
	public static final int INVITED = 2;
	public static final int DELETED = 1;

	private static final String TAG = "Friend";

	private String mName;
	private int mFlags;
	private int mLastViewedMessageId;
	private int mAvailableMessageId;
	private int mLastReceivedMessageControlId;
	private int mAvailableMessageControlId;
	private int mLastReceivedUserControlId;
	private String mImageUrl;
	private String mImageVersion;
	private String mImageIv;
	private static AlphanumComparator mComparator = new AlphanumComparator();
	private int mSelectedItem = -1;
	private int mSelectedTop = 0;
	

	public Friend(String name) {
		mName = name;

		SurespotLog.v(TAG, "constructor, friend: %s", this);
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;

	}

	public int getLastViewedMessageId() {
		return mLastViewedMessageId;
	}

	public void setLastViewedMessageId(int lastViewedMessageId) {
		if (lastViewedMessageId > 0) {
			mLastViewedMessageId = lastViewedMessageId;
		}
		else {
			mLastViewedMessageId = mAvailableMessageId;
		}
		SurespotLog.v(TAG, "setLastViewedMessageId, lastViewedMessageId: %d, friend: %s", lastViewedMessageId, this);
	}

	public int getAvailableMessageId() {
		return mAvailableMessageId;
	}

	public void setAvailableMessageId(int availableMessageId) {
		if (availableMessageId > 0 && isFriend() && !isDeleted()) {
			mAvailableMessageId = availableMessageId;

			// we received a message so we're not "new"
			setNewFriend(false);
		}
		SurespotLog.v(TAG, "setAvailableMessageId, %d, friend: %s", availableMessageId, this);
	}

	public int getAvailableMessageControlId() {
		return mAvailableMessageControlId;
	}

	public void setAvailableMessageControlId(int availableMessageControlId) {
		if (availableMessageControlId > 0) {
			mAvailableMessageControlId = availableMessageControlId;
		}

		SurespotLog.v(TAG, "setAvailabeMessageControlId, %d, friend: %s", availableMessageControlId, this);
	}

	public int getLastReceivedMessageControlId() {
		return mLastReceivedMessageControlId;
	}

	public void setLastReceivedMessageControlId(int lastReceivedMessageControlId) {
		mLastReceivedMessageControlId = lastReceivedMessageControlId;
		SurespotLog.v(TAG, "setLastReceivedMessageControlId, friend: %s", this);
	}

	public int getLastReceivedUserControlId() {
		return mLastReceivedUserControlId;
	}

	public void setLastReceivedUserControlId(int lastReceivedUserControlId) {
		mLastReceivedUserControlId = lastReceivedUserControlId;
		SurespotLog.v(TAG, "setLastReceivedUserControlId, friend: %s", this);
	}

	public void setChatActive(boolean set) {
		if (set) {
			mFlags |= CHAT_ACTIVE;
			setNewFriend(false);
		}
		else {
			mFlags &= ~CHAT_ACTIVE;
		}

		SurespotLog.v(TAG, "setChatActive, friend: %s", this);
	}

	// public void setMessageActivity(boolean set) {
	// if (set) {
	// mFlags |= MESSAGE_ACTIVITY;
	// }
	// else {
	// mFlags &= ~MESSAGE_ACTIVITY;
	// }
	// }

	public void setInviter(boolean set) {
		if (set) {
			mFlags |= INVITER;
		}
		else {
			mFlags &= ~INVITER;
		}

		SurespotLog.v(TAG, "setInviter, %b, friend: %s", set, this);
	}

	public boolean isInviter() {
		return (mFlags & INVITER) == INVITER;
	}

	public void setInvited(boolean set) {
		if (set) {
			mFlags |= INVITED;
		}
		else {
			mFlags &= ~INVITED;
		}
		SurespotLog.v(TAG, "setInvited, %b, friend: %s", set, this);
	}

	public boolean isInvited() {
		return (mFlags & INVITED) == INVITED;
	}

	public void setDeleted() {
		mFlags = DELETED;
		SurespotLog.v(TAG, "setDeleted, friend: %s", this);
	}

	public boolean isDeleted() {
		return (mFlags & DELETED) == DELETED;
	}

	public void setNewFriend(boolean set) {
		if (set) {
			mFlags |= NEW_FRIEND;
			mFlags &= ~INVITED;
			mFlags &= ~INVITER;
			mFlags &= ~DELETED;
		}
		else {
			mFlags &= ~NEW_FRIEND;
		}

		SurespotLog.v(TAG, "setNewFriend, set %b, friend: %s", set, this);
	}

	public boolean isNewFriend() {
		return (mFlags & NEW_FRIEND) == NEW_FRIEND;
	}

	public boolean isMessageActivity() {

		// SurespotLog.v(TAG, "isMessageActivity, %s", toString());
		if (isDeleted()) {
			return false;
		}
		return mAvailableMessageId - mLastViewedMessageId > 0;
	}

	public boolean isFriend() {
		return (!isInvited() && !isInviter());
	}

	public int getFlags() {
		return mFlags;
	}

	public void setFlags(int flags) {
		mFlags = flags;
		SurespotLog.v(TAG, "setInviter, friend: %s", this);
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setImageUrl(String imageUrl) {
		mImageUrl = imageUrl;
		// SurespotLog.v(TAG, "setImageUrl, friend: %s", this);
	}

	public String getImageVersion() {
		return mImageVersion;
	}

	public void setImageVersion(String imageVersion) {
		mImageVersion = imageVersion;
		// SurespotLog.v(TAG, "setImageVersion, friend: %s", this);
	}

	public String getImageIv() {
		return mImageIv;
	}

	public void setImageIv(String imageIv) {
		mImageIv = imageIv;
		// SurespotLog.v(TAG, "setImageIv, friend: %s", this);
	}

	public boolean isChatActive() {
		return (mFlags & CHAT_ACTIVE) == CHAT_ACTIVE;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != getClass())
			return false;

		Friend rhs = (Friend) obj;
		return this.getName().equals(rhs.getName());
	}

	@Override
	public int compareTo(Friend another) {
		// if the flags are the same sort by name
		// not active or invite, sort by name

		// for the purposes of sorting we'll add MESSAGE_ACTIVITY to the flags if they have new messages
		int myFlags = this.getFlags();
		if (this.isMessageActivity()) {
			myFlags |= MESSAGE_ACTIVITY;
		}

		int theirFlags = another.getFlags();
		if (another.isMessageActivity()) {
			theirFlags |= MESSAGE_ACTIVITY;
		}

		SurespotLog.v(TAG, "comparing %s %d to %s %d", this.getName(), myFlags, another.getName(), theirFlags);

		if ((theirFlags == myFlags) || (theirFlags < CHAT_ACTIVE && myFlags < CHAT_ACTIVE)) {
			return mComparator.compare(this.getName().toLowerCase(), another.getName().toLowerCase());
		}
		else {
			// sort by flag value
			return Integer.valueOf(theirFlags).compareTo(myFlags);
		}

	}

//	public boolean update(JSONObject jsonFriend) {
//
//		String status;
//
//		try {
//
//			String name = jsonFriend.getString("name");
//			if (name.equals(this.getName())) {
//				status = jsonFriend.getString("status");
//
//				if (status.equals("invited")) {
//					this.setInvited(true);
//				}
//
//				else {
//					if (status.equals("invitee")) {
//						this.setInviter(true);
//					}
//				}
//
//				this.setName(jsonFriend.getString("name"));
//				this.setImageUrl(jsonFriend.getString("imageUrl"));
//				this.setImageVersion(jsonFriend.getString("imageVersion"));
//
//				setNewFriend(false);
//
//				SurespotLog.v(TAG, "update <JSONObject>, friend: %s", this);
//				return true;
//			}
//
//		}
//		catch (JSONException e) {
//			SurespotLog.w(TAG, "update", e);
//		}
//		return false;
//	}

	public void update(Friend friend) {
		this.setNewFriend(false);
		this.setInvited(friend.isInvited());
		this.setInviter(friend.isInviter());
		this.setImageUrl(friend.getImageUrl());
		this.setImageVersion(friend.getImageVersion());
		this.setImageIv(friend.getImageIv());
		//this.setSelectedItem(friend.getSelectedItem());
		//this.setSelectedTop(friend.getSelectedTop());
		// this.setChatActive(friend.isChatActive());
		// this.setMessageActivity(friend.isMessageActivity());

		SurespotLog.v(TAG, "update <Friend>, friend: %s", this);
	}

	public static Friend toFriend(JSONObject jsonFriend) throws JSONException {
		Friend friend = new Friend(jsonFriend.getString("name"));

		friend.setImageUrl(jsonFriend.optString("imageUrl"));
		friend.setImageVersion(jsonFriend.optString("imageVersion"));
		friend.setImageIv(jsonFriend.optString("imageIv"));

		friend.setFlags(jsonFriend.optInt("flags"));
		friend.setLastReceivedMessageControlId(jsonFriend.optInt("lastReceivedMessageControlId"));
		friend.setAvailableMessageId(jsonFriend.optInt("lastAvailableMessageId"));
		friend.setLastReceivedUserControlId(jsonFriend.optInt("lastReceivedUserControlId"));
		friend.setLastViewedMessageId(jsonFriend.optInt("lastViewedMessageId"));
		friend.setSelectedItem(jsonFriend.optInt("selectedItem", -1));
		friend.setSelectedTop(jsonFriend.optInt("selectedTop", 0));

		return friend;
	}

	public JSONObject toJSONObject() {
		JSONObject jsonFriend = new JSONObject();

		try {

			jsonFriend.put("name", this.getName());
			jsonFriend.put("flags", this.getFlags());
			jsonFriend.put("lastReceivedMessageControlId", this.getLastReceivedMessageControlId());
			jsonFriend.put("lastAvailableMessageId", this.mAvailableMessageId);
			jsonFriend.put("lastReceivedUserControlId", this.getLastReceivedUserControlId());
			jsonFriend.put("lastViewedMessageId", this.getLastViewedMessageId());
			jsonFriend.put("imageVersion", this.getImageVersion());
			jsonFriend.put("imageUrl", this.getImageUrl());
			jsonFriend.put("imageIv", this.getImageIv());
			
			
			jsonFriend.put("selectedItem", this.getSelectedItem());
			jsonFriend.put("selectedTop", this.getSelectedTop());

			return jsonFriend;
		}
		catch (JSONException e) {
			SurespotLog.w(TAG, "toJSONObject", e);
		}
		return null;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nFriend:\n");
		sb.append("\tname: " + getName() + "\n");
		sb.append("\tflags: " + getFlags() + "\n");
		sb.append("\timageUrl: " + getImageUrl() + "\n");
		sb.append("\timageVersion: " + getImageVersion() + "\n");
		sb.append("\timageIv: " + getImageIv() + "\n");
		sb.append("\tlastViewedMessageId: " + getLastViewedMessageId() + "\n");
		sb.append("\tavailableMessageId: " + getAvailableMessageId() + "\n");
		sb.append("\tlastReceivedMessageControlId: " + getLastReceivedMessageControlId() + "\n");
		sb.append("\tavailableMessageControlId: " + getAvailableMessageControlId() + "\n");
		sb.append("\tlastReceivedUserControlId: " + getLastReceivedUserControlId() + "\n");
		sb.append("\tselectedItem: " + getSelectedItem() + "\n");
		sb.append("\tselectedTop: " + getSelectedTop() + "\n");

		return sb.toString();
	}

	public void setSelectedItem(int i) {
		//if (i == 0) {
			//SurespotLog.v(TAG, "SELECTED ITEM SET TO 0 FOR USER: %s", getName());
			//Utils.makeLongToast(MainActivity.getContext(), "SELECTED ITEM SET TO 0");
		//}
		//SurespotLog.v(TAG, "setSelectedItemAfter: %s", this);
		mSelectedItem = i;
	//	SurespotLog.v(TAG, "setSelectedItemBefore: %s", this);
		
	}

	public void setSelectedTop(int i) {
		mSelectedTop = i;
		
	}

	public int getSelectedItem() {

		return  mSelectedItem;
	}

	public int getSelectedTop() {
		return mSelectedTop;
	}

};
