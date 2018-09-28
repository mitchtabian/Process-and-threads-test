package com.codingwithmitch.applicationtwo;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.codingwithmitch.applicationtwo.Constants;
import com.codingwithmitch.applicationtwo.persistence.AppDatabase;
import com.codingwithmitch.applicationtwo.persistence.NoteDataEntity;

import java.util.List;

import static com.codingwithmitch.applicationtwo.Constants.MSG_RECEIVED_NOTES;

public class MyService extends Service {

    private static final String TAG = "MyService";

    final private Messenger mMessenger = new Messenger(new IpcHandler());
    private String mNotes = "";
    private HandlerThread mLocalHandlerThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    @SuppressLint("HandlerLeak")
    class IpcHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: received an incoming message.");
            switch (msg.what) {
                case Constants.MSG_GET_NOTES:
                    getNotes(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    private void getNotes(final Message msg){
        Log.d(TAG, "getNotes: getting notes from database.");

        sendResponseToClient(msg, mNotes);
    }


    private void sendResponseToClient(Message msg, String notes){
        Log.d(TAG, "sendResponseToClient: sending notes to client.");
        Message msgResponse = Message.obtain(null, MSG_RECEIVED_NOTES);
        Bundle bundle = new Bundle();
        bundle.putString("ipc_notes", notes);
        msgResponse.setData(bundle);

        try {
            msg.replyTo.send(msgResponse);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");

        mLocalHandlerThread = new HandlerThread("Local Service Handler Thread");
        mLocalHandlerThread.start();
        final Handler handler = new Handler(mLocalHandlerThread.getLooper());

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                List<NoteDataEntity> noteDataEntityList = db.noteDataDao().getNotes();
                StringBuilder sb = new StringBuilder();
                for(NoteDataEntity noteDataEntity : noteDataEntityList){
                    Log.d(TAG, "run: data: " + noteDataEntity.note);
                    sb.append(noteDataEntity.id + ": " + noteDataEntity.note + "\n");
                }

                mNotes = sb.toString();

                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);

        return START_NOT_STICKY;
    }
}














