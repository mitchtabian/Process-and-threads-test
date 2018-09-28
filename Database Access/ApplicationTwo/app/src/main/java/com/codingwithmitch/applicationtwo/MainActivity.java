package com.codingwithmitch.applicationtwo;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.arch.persistence.room.Database;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codingwithmitch.applicationtwo.persistence.AppDatabase;
import com.codingwithmitch.applicationtwo.persistence.NoteDataEntity;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //ui components
    private EditText mNoteInputText;
    private TextView mNotesDisplay;
//    private TextView mMessageDisplay;
    private ProgressBar mProgressBar;

    //vars
//    private Messenger mMessenger = null;
    private RetrieveDataAsyncTask mRetrieveDataAsyncTask = null;
    private SaveNoteAsyncTask mSaveNoteAsyncTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mMessageDisplay = findViewById(R.id.messageDisplay);
        mNoteInputText = findViewById(R.id.noteInput);
        mNotesDisplay = findViewById(R.id.notesDisplay);
        mProgressBar = findViewById(R.id.progress_bar);

        initExecutorThreadPool();
    }

//    private void startService(){
//        if(!isMyServiceRunning()){
//            Intent serviceIntent = new Intent(this, MyService.class);
//
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
//
//                MainActivity.this.startForegroundService(serviceIntent);
//            }else{
//                startService(serviceIntent);
//            }
//        }
//    }
//
//    private boolean isMyServiceRunning() {
//        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
//            if("com.codingwithmitch.applicationtwo.MyService".equals(service.service.getClassName())) {
//                Log.d(TAG, "isMyServiceRunning: service is already running.");
//                return true;
//            }
//        }
//        Log.d(TAG, "isMyServiceRunning: service is not running.");
//        return false;
//    }
//
//    public void startService(View view) {
//        startService();
//    }

    private ExecutorService mExecutorService = null;
    private ThreadPoolHandler mThreadPoolHandler = new ThreadPoolHandler();


    private void initExecutorThreadPool(){
        int numProcessors = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "initExecutorThreadPool: processors: " + numProcessors);
        mExecutorService = Executors.newFixedThreadPool(numProcessors);
    }


    private class ThreadPoolHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){

                case Constants.MSG_THREAD_POOL_TASK_COMPLETE:{
                    String notesToAppend = mNotesDisplay.getText().toString() + "\n"
                            + msg.getData().getString("note_data_from_thread_pool");
                    mNotesDisplay.setText(notesToAppend);
                    break;
                }
            }
        }
    }

    /**
     *
     * Making the expression
     0: -1, 0 and 1
     1: 1, 2 and 3
     2: 3, 4 and 5
     3: 5, 6 and 7

     */
    @SuppressLint("StaticFieldLeak")
    private void queryDatabaseWithThreadPool(){
        new AsyncTask<Void, Void, Integer>(){
            @Override
            protected Integer doInBackground(Void... voids) {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                int numNotes = db.noteDataDao().getNumRows();
                Log.d(TAG, "doInBackground: num rows: " + numNotes);
                return numNotes;
            }

            @Override
            protected void onPostExecute(Integer numRows) {
                super.onPostExecute(numRows);

                for(int i = 0; i <= (numRows / 2); i++){
                    DatabaseQueryRunnable runnable = new DatabaseQueryRunnable( 2*i);
                    mExecutorService.submit(runnable);
                }
            }
        }.execute();
    }

    private class DatabaseQueryRunnable implements Runnable{

        private int mStartingIndex;

        public DatabaseQueryRunnable(int mStartingIndex) {
            this.mStartingIndex = mStartingIndex;
        }

        @Override
        public void run() {
            retrieveSomeNotes(mStartingIndex);
        }
    }


    private void retrieveSomeNotes(int startingIndex){
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        List<NoteDataEntity> noteDataEntityList = db.noteDataDao().getSomeNotes(startingIndex);
        StringBuilder sb = new StringBuilder();
        for(NoteDataEntity noteDataEntity : noteDataEntityList){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "retrieveSomeNotes: data: " + noteDataEntity.note);
            sb.append(noteDataEntity.id + ": " + noteDataEntity.note + "\n");
        }
        Message message = Message.obtain(null, Constants.MSG_THREAD_POOL_TASK_COMPLETE);
        Bundle bundle = new Bundle();
        bundle.putString("note_data_from_thread_pool", sb.toString());
        message.setData(bundle);
        mThreadPoolHandler.sendMessage(message);
    }

    @SuppressLint("StaticFieldLeak")
    public void saveNote(View view) {

        if(!mNoteInputText.getText().toString().equals("")){

            mSaveNoteAsyncTask = new SaveNoteAsyncTask();
            mSaveNoteAsyncTask.execute(mNoteInputText.getText().toString());
        }

    }


    private class SaveNoteAsyncTask extends AsyncTask<String, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            NoteDataEntity noteDataEntity = new NoteDataEntity();
            noteDataEntity.note = strings[0];

            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            db.noteDataDao().insertNotes(noteDataEntity);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            super.onProgressUpdate(voids);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mNoteInputText.setText("");
            mNotesDisplay.setText("");
            retrieveData();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void retrieveData(){
        Log.d(TAG, "retrieveData: database path: " + getDatabasePath("notes.db"));

//        mRetrieveDataAsyncTask = new RetrieveDataAsyncTask();
//        mRetrieveDataAsyncTask.execute();

        queryDatabaseWithThreadPool();
    }

    private class RetrieveDataAsyncTask extends AsyncTask<Void, String, String>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<NoteDataEntity> noteDataEntityList = db.noteDataDao().getNotes();
            StringBuilder sb = new StringBuilder();
            int index = 1;
            for(NoteDataEntity noteDataEntity : noteDataEntityList){
                Log.d(TAG, "RetrieveDataAsyncTask: data: " + noteDataEntity.note);
                sb.append(noteDataEntity.id + ": " + noteDataEntity.note + "\n");
                try {
                    Thread.sleep(1000);
//                    publishProgress(noteDataEntityList.size(), index);
                    publishProgress(sb.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                index++;
            }
            return sb.toString();
        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//            mProgressBar.setMax(values[0]);
//            mProgressBar.setProgress(values[1]);
//        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mNotesDisplay.setText(values[0]);
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            Toast.makeText(MainActivity.this, "done", Toast.LENGTH_SHORT).show();
            mNotesDisplay.setText(string);
//            checkForIncomingIntent();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRetrieveDataAsyncTask.cancel(true);
        mSaveNoteAsyncTask.cancel(true);
    }

    private void checkForIncomingIntent(){
        // If it's just the launcher then ignore. Otherwise we know it's coming from app 1
        if(getIntent() != null && !getIntent().hasCategory("android.intent.category.LAUNCHER")){
            Intent intent = new Intent();
            String message = mNotesDisplay.getText().toString();
            intent.putExtra("message_text_from_app_2", message);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveData();
    }
}
