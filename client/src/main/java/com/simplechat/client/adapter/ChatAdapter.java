package com.simplechat.client.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.simplechat.client.R;
import com.simplechat.client.domain.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rufim on 24.01.2015.
 */

public class ChatAdapter extends ArrayAdapter {

    private TextView chatText;
    private List<ChatMessage> chatMessageList = new ArrayList();
    private LinearLayout singleMessageContainer;

    public void add(ChatMessage message) {
        chatMessageList.add(message);
        super.add(message);
    }

    @Override
    public void clear() {
        super.clear();
        chatMessageList.clear();
    }

    public ChatAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ChatAdapter(Context context, int resource, List<ChatMessage> messages) {
        super(context, resource, messages);
        chatMessageList.addAll(messages);
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.message, parent, false);
        }
        singleMessageContainer = (LinearLayout) row.findViewById(R.id.singleMessageContainer);
        ChatMessage chatMessageObj = getItem(position);
        chatText = (TextView) row.findViewById(R.id.singleMessage);
        if (chatMessageObj.user == null) {
            chatText.setText(chatMessageObj.message);
        } else {
            chatText.setText(chatMessageObj.user + ": " + chatMessageObj.message);
        }
        chatText.setBackgroundResource(chatMessageObj.left ? R.drawable.bubble_a : R.drawable.bubble_b);
        singleMessageContainer.setGravity(chatMessageObj.left ? Gravity.LEFT : Gravity.RIGHT);
        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}