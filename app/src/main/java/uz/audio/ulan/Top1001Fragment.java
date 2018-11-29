package uz.audio.ulan;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import uz.audio.Adapters.SearchResultsAdapter;
import uz.audio.Centrals.CentralDataRepository;
import uz.audio.Config.Constants;
import uz.audio.Models.ExploreItemModel;
import uz.audio.Models.ItemModel;
import uz.audio.Models.PlaylistItem;
import uz.audio.Models.ResultMessageObjectModel;
import uz.audio.Network.ConnectivityUtils;
import uz.audio.R;
import uz.audio.helpers.Shared;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Ankit on 1/24/2017.
 */

public class Top1001Fragment extends Fragment {

    Context context;
    private RecyclerView searchResultRecycler;
    private ProgressBar progressBar;
    private TextView progressBarMsgPanel;
    private SearchResultsAdapter searchResultAdapter;
    private SearchResultsAdapter.SearchResultActionListener searchActionListener;
    private CentralDataRepository repository;
    private String extraa;
    private String searchMode;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.search_fragment,null,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        Log.e("TOP100"," onViewCreated");

        searchResultRecycler = (RecyclerView) view.findViewById(R.id.search_recycler_view);
        progressBar = (ProgressBar) view.findViewById(R.id.search_progressBar);
        progressBarMsgPanel = (TextView) view.findViewById(R.id.searchMessage);
        searchResultRecycler.setLayoutManager(new LinearLayoutManager(context));
        searchResultRecycler.setAdapter(searchResultAdapter);
        //invokeSearchAction();

        Bundle bundle = getArguments();
        String searchText = bundle.getString("search");
        if (searchText.trim().isEmpty())
            getTop(searchText);
        else getSearch(searchText);
        //getTopOffline();
    }

    private void getSearch(String searchText) {
        ArrayList<ItemModel> itemModelArrayList = new ArrayList<>();
        ArrayList<PlaylistItem> playlistItems = new ArrayList<>();
        for (ItemModel itemModel:Shared.itemModels) {
            if (itemModel.Title.toLowerCase().trim().contains(searchText.toLowerCase().trim())){
                itemModelArrayList.add(itemModel);

                PlaylistItem playlistItem = new PlaylistItem(

                        itemModel.Video_id,
                        "ff",
                        itemModel.Title,
                        itemModel.UploadedBy

                );
                playlistItems.add(playlistItem);

            }
        }


        Shared.playlistItemsDontPrepared = playlistItems;
        searchResultAdapter.setItemList(itemModelArrayList);
        searchResultRecycler.setVisibility(RecyclerView.VISIBLE);
        progressBar.setVisibility(View.GONE);
        progressBarMsgPanel.setVisibility(View.GONE);

    }

    public void getTop(String searchText){
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url("http://5.23.54.120/api/main/get_top_tracks/")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json = response.body().string();
                Log.e("JSON",json);
                ItemModel item;

                try {
                    JSONObject jsonObject = new JSONObject(json);



                    JSONArray jsonArray = jsonObject.getJSONArray("music");
                    final ArrayList<ItemModel> itemModelArrayList = new ArrayList<>();
                    final ArrayList<PlaylistItem> playlistItems = new ArrayList<>();
                    for (int i = 0; i<jsonArray.length();i++){
                        JSONObject object = jsonArray.getJSONObject(i);
                        String title = object.getString("title");
                        String orderby = object.getString("parent_category");
                        String url = "http://5.23.54.120/media/"+object.getString("trackUrl");

                        item = new ItemModel(
                                title,
                                "3:31",
                                orderby,
                                "dd",
                                url,
                                "",
                                "",
                                orderby);
                        itemModelArrayList.add(item);

                        PlaylistItem playlistItem = new PlaylistItem(

                                url,
                                "ff",
                                title,
                                orderby

                        );
                        playlistItems.add(playlistItem);

                    }
                    Shared.itemModels = itemModelArrayList;
                    Shared.playlistItemsDontPrepared = playlistItems;

                    Log.e("SIZE",playlistItems.size()+"");


                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            // things to do on the main thread
                            searchResultAdapter.setItemList(itemModelArrayList);
                            searchResultRecycler.setVisibility(RecyclerView.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            progressBarMsgPanel.setVisibility(View.GONE);

                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        });

    }

    public void getTopOffline(){
        ItemModel item;






                    final ArrayList<ItemModel> itemModelArrayList = new ArrayList<>();
                    final ArrayList<PlaylistItem> playlistItems = new ArrayList<>();

            item = new ItemModel(
                    "title",
                    "3:31",
                    "orderby",
                    "dd",
                    "url",
                    "",
                    "",
                    "orderby");
            itemModelArrayList.add(item);

            PlaylistItem playlistItem = new PlaylistItem(

                    "url",
                    "ff",
                    "title",
                    "orderby"

            );
            playlistItems.add(playlistItem);
                    Shared.playlistItems = playlistItems;


                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            // things to do on the main thread
                            searchResultAdapter.setItemList(itemModelArrayList);
                            searchResultRecycler.setVisibility(RecyclerView.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            progressBarMsgPanel.setVisibility(View.GONE);

                        }
                    });







    }


    private void invokeSearchAction() {


        repository = CentralDataRepository.getInstance(context);

        Log.d("SearchTest"," se mode "+searchMode);
        int _action_to_invoke = searchMode.equals(Constants.SEARCH_MODE_SERVER)?CentralDataRepository.FLAG_SEARCH:CentralDataRepository.FLAG_RESTORE;

        repository.submitAction(_action_to_invoke,getHandlerInstance());

        searchResultRecycler.setVisibility(RecyclerView.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBarMsgPanel.setVisibility(View.VISIBLE);


        if (!ConnectivityUtils.getInstance(context).isConnectedToNet()) {
            searchResultRecycler.setVisibility(RecyclerView.GONE);
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        progressBarMsgPanel.setText("Searching For You...");



    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchResultAdapter = SearchResultsAdapter.getInstance(context);
        searchResultAdapter.setActionListener(searchActionListener);

    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        this.context = context;

    }


    private Handler getHandlerInstance(){

        return new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //WID: sets the main view adapter with data and disables progress bar

                Log.d("Search"," Receiving Message");
                Log.e("SEARCH",msg.toString());

                progressBar.setVisibility(View.INVISIBLE);
                ResultMessageObjectModel object = (ResultMessageObjectModel) msg.obj;
                ExploreItemModel item = object.data;

                if (object.Status == Constants.MESSAGE_STATUS_OK) {

                    // check if item is empty
                    if (item.getList() != null) {
                        if (item.getList().size() == 0 && searchResultAdapter.getItemCount() == 0) {
                            // hide the recycler view and Show Message
                            searchResultRecycler.setVisibility(RecyclerView.GONE);
                            progressBar.setVisibility(View.GONE);
                            progressBarMsgPanel.setVisibility(View.VISIBLE);
                            progressBarMsgPanel.setText("Troubling Getting Data......");

                        } else {

                            searchResultRecycler.setVisibility(RecyclerView.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            progressBarMsgPanel.setVisibility(View.GONE);

                        }
                    }

                    searchResultAdapter.setItemList(item.getList());

                } else {
                    //TODO: Collect Unexpected Error From CDR(Central Data Repository)
                }

            }
        };
    }


    //set by Home Activity
    public void setActionListener(SearchResultsAdapter.SearchResultActionListener actionListener){
        this.searchActionListener = actionListener;
    }




    public void setExtraa(String extraa) {
        this.extraa = extraa;
    }

    public void setSearchMode(String mode) {
        this.searchMode = mode;
    }
}
