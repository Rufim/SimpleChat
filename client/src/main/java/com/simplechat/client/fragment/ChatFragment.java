package com.simplechat.client.fragment;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.simplechat.client.R;
import com.simplechat.client.adapter.ChatAdapter;
import com.simplechat.client.dao.DatabaseHelper;
import com.simplechat.client.dao.HistoryDAO;
import com.simplechat.client.domain.ChatMessage;
import com.simplechat.client.net.Client;

import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Rufim
 *
 * A fragment containing a chat view.
 *
 */
public class ChatFragment extends Fragment {

    private static final String TAG = "ChatActivity";
    private static final String MESSAGES = "messages";
    private static ChatFragment instance;

    private ChatAdapter chatAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private Thread chatRunningTask;
    private String user = "User_" + getRandomString(3);
    private Handler handler;

    private Client client;
    //Data
    private ArrayList<ChatMessage> messages;
    private HistoryDAO historyDAO;


    public ChatFragment() {
        instance = this;
    }

    public static ChatFragment getInstance() {
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        buttonSend = (Button) rootView.findViewById(R.id.buttonSend);

        listView = (ListView) rootView.findViewById(R.id.listView1);
        if (savedInstanceState != null) {
            messages = (ArrayList<ChatMessage>) savedInstanceState.getSerializable(MESSAGES);
        }

        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(rootView.getContext());
            historyDAO = databaseHelper.getHistoryDAO();
            if (messages == null) {
                messages = new ArrayList<>();
                messages.addAll(historyDAO.getHistory());
            }
        } catch (Exception e) {
            Log.w(TAG, e.getLocalizedMessage(), e);
        }

        chatAdapter = new ChatAdapter(rootView.getContext(), R.layout.message, messages);
        listView.setAdapter(chatAdapter);

        chatText = (EditText) rootView.findViewById(R.id.chatText);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatAdapter);

        //to scroll the list view to bottom on data change
        chatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatAdapter.getCount() - 1);
            }
        });

        ViewTreeObserver observer = rootView.getViewTreeObserver();

        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                // Do what you need after view lunch here...
                handler = rootView.getHandler();
                chatRunningTask = new ChatThread();
                chatRunningTask.start();
            }
        });
        return rootView;
    }

    public String getRandomString(int length) {
        Random rng = new Random(System.currentTimeMillis());
        char[] chars = new char[length];
        String validChars = "abcdefghijklmnopqrstuvwxyz ABCEDFGHIJKLMNOPQRSTUVWXYZ1234567890";
        for (int i = 0; i < length; i++) {
            chars[i] = validChars.charAt(rng.nextInt(validChars.length()));
        }
        return new String(chars);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(MESSAGES, messages);
    }

    private boolean sendChatMessage() {
        String message = chatText.getText().toString();
        ChatMessage chatMessage = new ChatMessage(user, true, message);
        chatAdapter.add(chatMessage);
        try {
            historyDAO.create(chatMessage);
        } catch (SQLException e) {
            Log.e(SQLException.class.getSimpleName(), "Database error");
            Log.w(SQLException.class.getSimpleName(), e);
        }
        client.sendMessage(message);
        chatText.setText("");
        return true;
    }

    public void clearHistory(){
        try {
            DeleteBuilder builder = historyDAO.deleteBuilder();
            builder.where().isNotNull("message");
            builder.delete();
            messages.clear();
            chatAdapter.clear();
            chatAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            Log.e(SQLException.class.getSimpleName(), "Database error");
            Log.w(SQLException.class.getSimpleName(), e);
        }
    }


    class ChatThread extends Thread {

        public static final String CHAT_THREAD = "ChatThread";

        public ChatThread() {
            super(CHAT_THREAD);
        }

        @Override
        public void run() {
            super.run();
            try {
                client = new Client(new Socket("192.168.1.7", 8283), new Client.OnMessageListener() {
                    @Override
                    public void onMessage(final String from, final String message) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(from == null) {
                                    chatAdapter.add(new ChatMessage(null, false, message));
                                } else if(from.equals(user)) {
                                    //TODO: notify that message is received
                                } else {
                                    ChatMessage chatMessage = new ChatMessage(from, false, message);
                                    chatAdapter.add(chatMessage);
                                    try {
                                        historyDAO.create(chatMessage);
                                    } catch (SQLException e) {
                                        Log.e(SQLException.class.getSimpleName(), "Database error");
                                        Log.w(SQLException.class.getSimpleName(), e);
                                    }                                }
                            }
                        }) ;
                    }
                });
                client.runChat(user);
            } catch (Exception e) {
                Log.e(Exception.class.getSimpleName(), "Connection failed");
                Log.w(Exception.class.getSimpleName(), e);
                // try to reconnect
                SystemClock.sleep(3000);
                chatRunningTask = new ChatThread();
                chatRunningTask.start();
            }

        }

    }


}
