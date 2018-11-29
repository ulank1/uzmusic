package uz.audio.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import uz.audio.Activity.AnyAudioActivity;
import uz.audio.Activity.UpdateThemedActivity;
import uz.audio.Adapters.NavigationListAdapter;
import uz.audio.Config.Constants;
import uz.audio.R;
import uz.audio.SharedPreferences.SharedPrefrenceUtils;
import uz.audio.services.UpdateCheckService;
import uz.audio.ulan.Top100Activity;

import static uz.audio.ulan.Top100Activity.top100ActivityInstance;

/**
 * Created by Ankit on 2/22/2017.
 */

public class NavigationDrawerFragment extends Fragment {

    private Context context;
    private ListView listView;
    public static NavigationListAdapter navigationListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.navigation_drawer_fragment,null,false);
        listView = (ListView) v.findViewById(R.id.navigationListView);
        navigationListAdapter = new NavigationListAdapter(context);
        int selected = SharedPrefrenceUtils.getInstance(context).getSelectedNavIndex();
        navigationListAdapter.updateNavState(selected,true);
        listView.setAdapter(navigationListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPrefrenceUtils.getInstance(context).setSelectedNavIndex(i);
                ((NavigationListAdapter) adapterView.getAdapter()).updateNavState(i,true);
                int fragment = -1;
                String title = "";
                switch (i){
                    case 0:

                        fragment = Top100Activity.FRAGMENT_SEARCH;
                        title = "Узбекские песни";
                        break;

                    case 1:

                        fragment = Top100Activity.FRAGMENT_EXPLORE;
                        title = "Топ 100";
                        break;

                    case 2:

                        fragment = Top100Activity.FRAGMENT_FAVORITES;
                        title = "Избранное";
                        break;

                    case 3:

                        fragment = AnyAudioActivity.FRAGMENT_DOWNLOADS;
                        title = "AnyAudio";
                        break;

                  /*  case 2:

                        fragment = AnyAudioActivity.FRAGMENT_DOWNLOADS;
                        title = "AnyAudio";
                        break;

                    case 3:

                        fragment = AnyAudioActivity.FRAGMENT_SETTINGS;
                        title = "SETTINGS";
                        break;

                    case 4:

                        fragment = AnyAudioActivity.FRAGMENT_ABOUT_US;
                        title = "ABOUT US";
                        break;

                    case 5:

                        if(SharedPrefrenceUtils.getInstance(context).getNewVersionAvailibility()){
                               launchUpdateDialog();
                        }else{
                            Toast.makeText(context,"Checking For Updates.... We Will Inform You Once Available ",Toast.LENGTH_LONG).show();
                            checkForUpdate();
                        }
                        break;*/
                }


                    top100ActivityInstance.onNavItemSelected(fragment, title);


            }
        });

        return v;


    }

    private boolean canMakeSmores() {

        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.M);

    }

    private void checkForUpdate() {
        Intent i = new Intent(context,UpdateCheckService.class);
        i.setAction("ACTION_UPDATE");
        context.startService(i);
    }

    private void launchUpdateDialog(){

        //Collect updates info from SP
        SharedPrefrenceUtils utils = SharedPrefrenceUtils.getInstance(context);
        Intent updateIntent = new Intent(context, UpdateThemedActivity.class);
        updateIntent.putExtra(Constants.EXTRAA_NEW_UPDATE_DESC, utils.getNewVersionDescription());
        updateIntent.putExtra(Constants.KEY_NEW_UPDATE_URL, utils.getNewUpdateUrl());
        updateIntent.putExtra(Constants.KEY_NEW_ANYAUDIO_VERSION,utils.getLatestVersionName());
        updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(updateIntent);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    private int getCurrentVersion(){
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionCode;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }
}
