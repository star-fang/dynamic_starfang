package com.starfang.utilities.reply;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import java.util.ArrayList;

public class ReplyAction implements Parcelable {

    private final PendingIntent pendingIntent;
    private final boolean isQuickReply;
    private final ArrayList<RemoteInputParcel> remoteInputs = new ArrayList<>();
    private String sendCat;
    private String forumName;
    private String content;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(pendingIntent, flags);
        dest.writeByte((byte) (isQuickReply ? 1 : 0));
        dest.writeTypedList(remoteInputs);
        dest.writeString(sendCat);
        dest.writeString(forumName);
        dest.writeString(content);
    }

    private ReplyAction(Parcel in) {
        pendingIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        isQuickReply = in.readByte() != 0;
        in.readTypedList(remoteInputs, RemoteInputParcel.CREATOR);
        sendCat = in.readString();
        forumName = in.readString();
        content = in.readString();
    }

    /*
    public ReplyAction( PendingIntent pendingIntent, RemoteInput remoteInput, boolean isQuickReply) {
        this.pendingIntent = pendingIntent;
        this.isQuickReply = isQuickReply;
        remoteInputs.add(new RemoteInputParcel(remoteInput));
    }
     */
    public ReplyAction(NotificationCompat.Action action
            , String sendCat, String forumName, String content
            , boolean isQuickReply) {
        this.pendingIntent = action.actionIntent;
        this.sendCat = sendCat;
        this.forumName = forumName;
        this.content = content;
        if (action.getRemoteInputs() != null) {
            int size = action.getRemoteInputs().length;
            for (int i = 0; i < size; i++)
                remoteInputs.add(new RemoteInputParcel(action.getRemoteInputs()[i]));
        }
        this.isQuickReply = isQuickReply;
    }

    public void sendReply(Context context, String msg) throws PendingIntent.CanceledException {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        ArrayList<RemoteInput> actualInputs = new ArrayList<>();

        for (RemoteInputParcel input : remoteInputs) {
            //Log.i("", "RemoteInput: " + input.getLabel());
            bundle.putCharSequence(input.getResultKey(), msg);
            RemoteInput.Builder builder = new RemoteInput.Builder(input.getResultKey());
            builder.setLabel(input.getLabel());
            builder.setChoices(input.getChoices());
            builder.setAllowFreeFormInput(input.isAllowFreeFormInput());
            builder.addExtras(input.getExtras());
            actualInputs.add(builder.build());
        }

        RemoteInput[] inputs = actualInputs.toArray(new RemoteInput[0]);
        RemoteInput.addResultsToIntent(inputs, intent, bundle);
        if (pendingIntent != null)
            pendingIntent.send(context, 0, intent);
    }

    /*
    public ArrayList<RemoteInputParcel> getRemoteInputs() {
        return remoteInputs;
    }

    public PendingIntent getQuickReplyIntent() {
        return isQuickReply ? pendingIntent : null;
    }
     */
    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<ReplyAction> CREATOR = new Creator<ReplyAction>() {
        @Override
        public ReplyAction createFromParcel(Parcel in) {
            return new ReplyAction(in);
        }

        @Override
        public ReplyAction[] newArray(int size) {
            return new ReplyAction[size];
        }
    };

    public String getForumName() {
        return forumName;
    }

    public String getSendCat() {
        return sendCat;
    }

    public String getContentText() {
        return content;
    }
}
