package com.codingwithmitch.applicationone;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.codingwithmitch.applicationone.persistence.AppDatabase;
import com.codingwithmitch.applicationone.persistence.NoteDataEntity;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //ui components
    private TextView mNotesDisplay;
//    private EditText mMessageInput;

    //vars
//    Context mAppTwoContext;
//    private Messenger mMessenger = null;
//    private IpcHandler mIpcHandler = new IpcHandler();
//    private Messenger mIncomingMessenger = null;
//    private boolean mIsBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mMessageInput = findViewById(R.id.messageInput);
        mNotesDisplay = findViewById(R.id.notesDisplay);

//        setAppTwoContext();

    }

//    public void bindToAppTwoService(View view) {
//        bindService();
//    }

//    class IpcHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            Log.d(TAG, "handleMessage: got incoming message from server.");
//            Log.d(TAG, "handleMessage: what: " + msg.what);
//            switch (msg.what) {
//
//                case Constants.MSG_RECEIVED_NOTES:{
//                    Log.d(TAG, "handleMessage: received incoming notes.");
//
//                    String notes = msg.getData().getString("ipc_notes");
//                    mNotesDisplay.setText(notes);
//                    break;
//                }
//
//                default: {
//                    Log.d(TAG, "handleMessage: default case.");
//                    super.handleMessage(msg);
//                    break;
//                }
//            }
//        }
//    }

//    public void bindService(){
//        Intent serviceBindIntent =  new Intent();
//        serviceBindIntent.setComponent(new ComponentName("com.codingwithmitch.applicationtwo", "com.codingwithmitch.applicationtwo.MyService"));
//        bindService(serviceBindIntent, serviceConnection, 0);
//    }


//    private ServiceConnection serviceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder iBinder) {
//            Log.d(TAG, "ServiceConnection: connected to service.");
//            mMessenger = new Messenger(iBinder);
//            mIsBound = true;
//            mIncomingMessenger = new Messenger(mIpcHandler);
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            Log.d(TAG, "ServiceConnection: disconnected from service.");
//            mMessenger = null;
//            mIsBound = false;
//        }
//
//        @Override
//        public void onNullBinding(ComponentName name) {
//            Log.d(TAG, "ServiceConnection: onNullBinding: called.");
//        }
//
//        @Override
//        public void onBindingDied(ComponentName name) {
//            Log.d(TAG, "ServiceConnection: onBindingDied: called.");
//        }
//    };

//    @Override
//    protected void onStop() {
//        Log.d(TAG, "onStop: called.");
//        super.onStop();
//        if (mMessenger != null) {
//            unbindService(serviceConnection);
//            mMessenger = null;
//        }
//    }

    public void getNotesFromAppTwo(View view) {
//        retrieveData();
//        retrieveDataFromService();
        retrieveDataWithIntent();
    }


//    @SuppressLint("StaticFieldLeak")
//    private void retrieveDataFromService(){
//        Log.d(TAG, "retrieveDataFromService: asking service from application two for data.");
//        mNotesDisplay.setText("");
//
//        if (mIsBound) {
//            if (mMessenger != null) {
//                try {
//                    Message msg = Message.obtain(null, Constants.MSG_GET_NOTES);
//                    msg.replyTo = mIncomingMessenger;
//                    mMessenger.send(msg);
//                } catch (RemoteException e) {
//                    Log.e(TAG, "retrieveDataFromService: RemoteException: " + e.getMessage() );
//                    // Service crashed
//                }
//            }
//        }
//    }



//    private void setAppTwoContext(){
//        try{
//            mAppTwoContext =
//            this.createPackageContext("com.codingwithmitch.applicationtwo", Context.CONTEXT_INCLUDE_CODE);
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "retrieveData: " + e.getMessage());
//        }
//    }
//
//    @SuppressLint("StaticFieldLeak")
//    private void retrieveData(){
//
//        mNotesDisplay.setText("");
//
//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... voids) {
//
//                AppDatabase db = null;
//
//                db = Room.databaseBuilder(mAppTwoContext,
//                            AppDatabase.class, AppDatabase.DATABASE_NAME)
//                            .build();
//                List<NoteDataEntity> noteDataEntityList = db.noteDataDao().getNotes();
//                StringBuilder sb = new StringBuilder();
//                for(NoteDataEntity noteDataEntity : noteDataEntityList){
//                    Log.d(TAG, "retrieveData: data: " + noteDataEntity.note);
//                    sb.append(noteDataEntity.id + ": " + noteDataEntity.note + "\n");
//                }
//                Log.d(TAG, "retrieveData: data: \n" + sb.toString());
//
//                return sb.toString();
//            }
//
//            @Override
//            protected void onPostExecute(String string) {
//                super.onPostExecute(string);
//                mNotesDisplay.setText(string);
//            }
//
//        }.execute();
//    }


    private void retrieveDataWithIntent() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.PICK");
        intent.setType("vnd.codingwithmitch.text/vnd.codingwithmitch.intent-text");
        startActivityForResult(intent, 1234);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: called.");

        if (resultCode == RESULT_OK) {
            if(requestCode == 1234){

                String msg = data.getStringExtra("message_text_from_app_2");
                mNotesDisplay.setText(msg);
            }
        }
    }

}


