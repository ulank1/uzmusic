package uz.audio.offline;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uz.audio.Config.Constants;
import uz.audio.Models.DownloadedItemModel;
import uz.audio.R;

import static android.os.Build.VERSION.SDK_INT;
import static uz.audio.offline.MediaPlayerService.ACTION_PAUSE;
import static uz.audio.offline.MediaPlayerService.ACTION_PLAY;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.valdioveliu.valdio.audioplayer.PlayNewAudio";
    public static final String Broadcast_STATUS = "com.valdioveliu.valdio.audioplayer.Status";

    private MediaPlayerService player;
    boolean serviceBound = false;
    ArrayList<Audio> audioList;
    ImageView pausePlay, next;
    TextView title, artist;
    boolean isPlay = false;
    LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pausePlay = (ImageView) findViewById(R.id.pause_play);
        next = (ImageView) findViewById(R.id.next);
        title = (TextView) findViewById(R.id.title);
        artist = (TextView) findViewById(R.id.artist);
        linearLayout = (LinearLayout) findViewById(R.id.line1);


        register_Status();


        if (checkAndRequestPermissions()) {
            loadAudioList();
        }


    }

    private void loadAudioList() {
        loadAudio1();
        initRecyclerView();
    }

    private boolean checkAndRequestPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            int permissionReadPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            List<String> listPermissionsNeeded = new ArrayList<>();

            if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        String TAG = "LOG_PERMISSION";
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions

                    if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            ) {
                        Log.d(TAG, "Phone state and storage permissions granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                        loadAudioList();
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                      //shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                            showDialogOK("Phone state and storage permissions required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    private void initRecyclerView() {
        if (audioList != null && audioList.size() > 0) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            RecyclerView_Adapter adapter = new RecyclerView_Adapter(audioList, getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addOnItemTouchListener(new CustomTouchListener(this, new onItemClickListener() {
                @Override
                public void onClick(View view, int index) {
                    playAudio(index);
                }
            }));
        }
    }

    /*private void loadCollapsingImage(int i) {
        TypedArray array = getResources().obtainTypedArray(R.array.images);
        collapsingImageView.setImageDrawable(array.getDrawable(i));
    }
*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();

        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
        StorageUtil storage = new StorageUtil(getApplicationContext());

        title.setText(storage.loadAudio().get(audioIndex).getTitle());
        artist.setText(storage.loadAudio().get(audioIndex).getArtist());

        isPlay = true;
        linearLayout.setVisibility(View.VISIBLE);


    }

    /**
     * Load audio files using {@link ContentResolver}
     * <p>
     * If this don't works for you, load the audio files to audioList Array your oun way
     */
    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));


                Log.e("Artist", artist + " " + album + " " + data);

                if (data.contains("uzmusic")) {



                    // Save to audioList
                    audioList.add(new Audio(data, title, album, artist));
                }
            }
        }
        if (cursor != null)
            cursor.close();
    }


    private void loadAudio1(){
        Boolean isSDPresent = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        //Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();

        if(isSDPresent)
        {

            audioList = new ArrayList<>();
            File dir = new File(Constants.DOWNLOAD_FILE_DIR);
            try {
                if (dir != null) {
                    for (File f : dir.listFiles()) {
                        String path = f.toString();
                        Log.d("Downloaded", "" + path);
                        audioList.add(new Audio(path, path.substring(path.lastIndexOf('/') + 1, path.length()), "", ""));

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, ""+e.toString(), Toast.LENGTH_SHORT).show();
            }
            // yes SD-card is present
        }
        else
        {
            Toast.makeText(this, "Память не доступна", Toast.LENGTH_SHORT).show();

        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
        unregisterReceiver(broadcastStatus);
    }

    public void onClickPause(View view) {
        if (isPlay) {
            Intent playbackAction = new Intent(this, MediaPlayerService.class);
            playbackAction.setAction(ACTION_PAUSE);
            startService(playbackAction);
            pausePlay.setImageResource(R.drawable.ic_action_play);

        }else {
            Intent playbackAction = new Intent(this, MediaPlayerService.class);
            playbackAction.setAction(ACTION_PLAY);
            startService(playbackAction);
            pausePlay.setImageResource(R.drawable.ic_action_pause);

        }
    }

    public void onClickNext(View view) {

        StorageUtil storage = new StorageUtil(getApplicationContext());
        int audioIndex = storage.loadAudioIndex()+1;
        if (audioIndex==storage.loadAudio().size())
            audioIndex=0;
        playAudio(audioIndex);
    }

    private BroadcastReceiver broadcastStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra("status", -1);
            Log.e("TAtus", status + "");
            if (status == 0) {
                isPlay = true;
                pausePlay.setImageResource(R.drawable.ic_action_pause);
            } else if (status == 1) {
                isPlay = false;
                pausePlay.setImageResource(R.drawable.ic_action_play);
            }
            if (status == 2) {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                int audioIndex = storage.loadAudioIndex();
                title.setText(storage.loadAudio().get(audioIndex).getTitle());
                artist.setText(storage.loadAudio().get(audioIndex).getArtist());
            }
        }
    };

    private void register_Status() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_STATUS);
        registerReceiver(broadcastStatus, filter);
    }





}
