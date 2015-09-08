package com.pundroid.ttsprojecttest;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ScreenC extends AppCompatActivity implements View.OnClickListener,
        DialogFragmentText.OnTextEnteredListener, TextToSpeech.OnInitListener {
    public static final String TAG = ScreenC.class.getSimpleName();
    private TextView mTimerValue;
    private HashMap<String, String> mRenderSpeechMap = new HashMap<>();
    private SoundPool sp;
    private TextToSpeech mTextToSpeech;
    private String mEnteredText;
    private boolean isStart;
    private int mInitTimer = 16000;
    private int mInterval = 1000;
    private int ding;
    private long millis;


    private Button mBtnStop;
    private Button mBtnPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_c);

        //init TTS
        mTextToSpeech = new TextToSpeech(ScreenC.this, ScreenC.this);
        //Load sound "ding"
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        ding = sp.load(ScreenC.this, R.raw.ding, 1);


        Button btnEnterText = (Button) findViewById(R.id.btnEnterText);
        Button btnPlay = (Button) findViewById(R.id.btnPlay);
        mBtnPause = (Button) findViewById(R.id.btnPause);
        mBtnStop = (Button) findViewById(R.id.btnStop);

        btnEnterText.setOnClickListener(ScreenC.this);
        btnPlay.setOnClickListener(ScreenC.this);
        mBtnPause.setOnClickListener(ScreenC.this);
        mBtnStop.setOnClickListener(ScreenC.this);

        mTimerValue = (TextView) findViewById(R.id.timerValue);
        mTimerValue.setTextColor(Color.BLACK);

        if (getIntent() != null) {
            Intent intent = getIntent();
            isStart = intent.getBooleanExtra(ScreenB.START, false);
        }

        //start timer countdown
        if (isStart) {
            CounterClass counterClass = new CounterClass(mInitTimer, mInterval);
            counterClass.start();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_screen_c, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnterText:
                android.support.v4.app.DialogFragment dialogFragment
                        = new DialogFragmentText();
                dialogFragment.setCancelable(true);
                dialogFragment.show(getSupportFragmentManager(), "dialog_fragment_text");
                break;
            case R.id.btnPlay:
                speakOut();
                break;
            case R.id.btnPause:
                break;
            case R.id.btnStop:
                mTextToSpeech.stop();
                break;
        }

    }

    // entered text in dialog
    @Override
    public void OnTextEntered(String textEntered) {
        Toast.makeText(ScreenC.this, textEntered, Toast.LENGTH_SHORT).show();
        this.mEnteredText = textEntered;
        mBtnPause.setEnabled(true);
        mBtnStop.setEnabled(true);

    }

    @Override
    public void onInit(int status) {
        Log.d(TAG, "Status " + String.valueOf(status));
        //TTS is successfully initialized
        if (status == TextToSpeech.SUCCESS) {
            //Setting speech language
            int result = mTextToSpeech.setLanguage(Locale.US);
            //If your device doesn't support language you set above
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_LONG).show();
                Log.e("TTS", "Language is not supported");
            }
        } else {
            Toast.makeText(this, "TTS Initilization Failed", Toast.LENGTH_LONG).show();
            Log.e("TTS", "Initilization Failed");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakOut() {
        if (mEnteredText == null || mEnteredText.length() == 0) {
            mTextToSpeech.speak("You haven't typed text", TextToSpeech.QUEUE_FLUSH, null, "text_entered_id");
        } else {
            mTextToSpeech.speak(mEnteredText, TextToSpeech.QUEUE_FLUSH, null, "text_entered_id");
        }
    }


    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            millis = millisUntilFinished;

            String ms = String.format("%02d",
                    TimeUnit.MILLISECONDS.toSeconds(millis)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            mTimerValue.setText(ms);
            if (millis < 4000 && millis > 3500) {
                sp.play(ding, 1, 1, 1, 3, 1);
            }
        }

        @Override
        public void onFinish() {
            mTimerValue.setText("0");
            //finish();
        }
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        super.onDestroy();
    }


}
