package uz.audio.AnyAudioMains;

import android.app.Application;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import uz.audio.SharedPreferences.StreamSharedPref;

/**
 * Created by Ankit on 11/27/2016.
 */

public class AnyAudio extends Application {

    public AnyAudio() {
        super();
    }

    @Override
    public void onCreate() {

        Log.d("AnyAudioApp", "[Application] onCreate()");
        StreamSharedPref.getInstance(this).resetStreamInfo();
        StreamSharedPref.getInstance(this).setStreamUrlFetchedStatus(false);
        Fresco.initialize(this);
        super.onCreate();

    }
}
