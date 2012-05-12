package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.formatGeoLocationToString;
import static org.mariotaku.twidere.util.Utils.getGeoLocationFromString;
import static org.mariotaku.twidere.util.Utils.getSpannedStatusString;
import static org.mariotaku.twidere.util.Utils.getSpannedStatusText;

import java.net.MalformedURLException;
import java.net.URL;

import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.User;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

public class ParcelableStatus implements Parcelable {

	public static final Parcelable.Creator<ParcelableStatus> CREATOR = new Parcelable.Creator<ParcelableStatus>() {
		@Override
		public ParcelableStatus createFromParcel(Parcel in) {
			return new ParcelableStatus(in);
		}

		@Override
		public ParcelableStatus[] newArray(int size) {
			return new ParcelableStatus[size];
		}
	};

	public long retweet_id, retweeted_by_id, status_id, account_id, user_id, status_timestamp, retweet_count,
			in_reply_to_status_id, in_reply_to_user_id;

	public boolean is_gap, is_retweet, is_favorite, is_protected, has_media;

	public String retweeted_by_name, retweeted_by_screen_name, text_plain, name, screen_name, in_reply_to_screen_name,
			source;
	public Spanned text;
	public GeoLocation location;

	public URL profile_image_url;

	public ParcelableStatus(Cursor cursor, StatusesCursorIndices indices) {
		retweet_id = cursor.getLong(indices.retweet_id);
		retweeted_by_id = cursor.getLong(indices.retweeted_by_id);
		status_id = cursor.getLong(indices.status_id);
		account_id = cursor.getLong(indices.account_id);
		user_id = cursor.getLong(indices.user_id);
		status_timestamp = cursor.getLong(indices.status_timestamp);
		retweet_count = cursor.getLong(indices.retweet_count);
		in_reply_to_status_id = cursor.getLong(indices.in_reply_to_status_id);
		in_reply_to_user_id = cursor.getLong(indices.in_reply_to_user_id);
		is_gap = cursor.getInt(indices.is_gap) == 1;
		is_retweet = cursor.getInt(indices.is_retweet) == 1;
		is_favorite = cursor.getInt(indices.is_favorite) == 1;
		is_protected = cursor.getInt(indices.is_protected) == 1;
		has_media = cursor.getInt(indices.has_media) == 1;
		retweeted_by_name = cursor.getString(indices.retweeted_by_name);
		retweeted_by_screen_name = cursor.getString(indices.retweeted_by_screen_name);
		text = Html.fromHtml(cursor.getString(indices.text));
		text_plain = text.toString();
		name = cursor.getString(indices.name);
		screen_name = cursor.getString(indices.screen_name);
		in_reply_to_screen_name = cursor.getString(indices.in_reply_to_screen_name);
		source = cursor.getString(indices.source);
		location = getGeoLocationFromString(cursor.getString(indices.location));
		profile_image_url = parseURL(cursor.getString(indices.profile_image_url));
	}

	public ParcelableStatus(Parcel in) {
		retweet_id = in.readLong();
		retweeted_by_id = in.readLong();
		status_id = in.readLong();
		account_id = in.readLong();
		user_id = in.readLong();
		status_timestamp = in.readLong();
		retweet_count = in.readLong();
		in_reply_to_status_id = in.readLong();
		in_reply_to_user_id = in.readLong();
		is_gap = in.readInt() == 1;
		is_retweet = in.readInt() == 1;
		is_favorite = in.readInt() == 1;
		is_protected = in.readInt() == 1;
		has_media = in.readInt() == 1;
		retweeted_by_name = in.readString();
		retweeted_by_screen_name = in.readString();
		text = Html.fromHtml(in.readString());
		text_plain = text.toString();
		name = in.readString();
		screen_name = in.readString();
		in_reply_to_screen_name = in.readString();
		source = in.readString();
		location = getGeoLocationFromString(in.readString());
		profile_image_url = parseURL(in.readString());
	}

	public ParcelableStatus(Status status, long status_account_id) {

		is_retweet = status.isRetweet();
		final Status retweeted_status = status.getRetweetedStatus();
		if (is_retweet && retweeted_status != null) {
			final User retweet_user = status.getUser();
			retweet_id = status.getId();
			retweeted_by_id = retweet_user.getId();
			retweeted_by_name = retweet_user.getName();
			retweeted_by_screen_name = retweet_user.getScreenName();
			status = retweeted_status;
		}
		final User user = status.getUser();
		if (user != null) {
			user_id = user.getId();
			name = user.getName();
			screen_name = user.getScreenName();
			profile_image_url = user.getProfileImageURL();
			is_protected = user.isProtected();
		}
		final MediaEntity[] medias = status.getMediaEntities();
		status_id = status.getId();
		account_id = status_account_id;
		if (status.getCreatedAt() != null) {
			status_timestamp = status.getCreatedAt().getTime();
		}
		text = getSpannedStatusText(status, account_id);
		text_plain = text.toString();
		retweet_count = status.getRetweetCount();
		in_reply_to_screen_name = status.getInReplyToScreenName();
		in_reply_to_status_id = status.getInReplyToStatusId();
		in_reply_to_user_id = status.getInReplyToUserId();
		source = status.getSource();
		location = status.getGeoLocation();
		is_favorite = status.isFavorited();
		has_media = medias != null && medias.length > 0;

	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(retweet_id);
		out.writeLong(retweeted_by_id);
		out.writeLong(status_id);
		out.writeLong(account_id);
		out.writeLong(user_id);
		out.writeLong(status_timestamp);
		out.writeLong(retweet_count);
		out.writeLong(in_reply_to_status_id);
		out.writeLong(in_reply_to_user_id);
		out.writeInt(is_gap ? 1 : 0);
		out.writeInt(is_retweet ? 1 : 0);
		out.writeInt(is_favorite ? 1 : 0);
		out.writeInt(is_protected ? 1 : 0);
		out.writeInt(has_media ? 1 : 0);
		out.writeString(retweeted_by_name);
		out.writeString(retweeted_by_screen_name);
		out.writeString(getSpannedStatusString(text));
		out.writeString(name);
		out.writeString(screen_name);
		out.writeString(in_reply_to_screen_name);
		out.writeString(source);
		out.writeString(formatGeoLocationToString(location));
		out.writeString(profile_image_url != null ? profile_image_url.toString() : null);
	}

	private URL parseURL(String url_string) {
		try {
			return new URL(url_string);
		} catch (MalformedURLException e) {
			// This should not happen.
		}
		return null;
	}
}