package fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import remm.sharedtrip.BrowseActivity;
import remm.sharedtrip.CreateEvent;
import remm.sharedtrip.MainActivity;
import remm.sharedtrip.ProfileView;
import remm.sharedtrip.R;
import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.EventAdapter;
import fragments.FriendsView;
import models.UserEventModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.BottomNavigationViewHelper;


public class BrowseEvents extends Fragment {


    private List<UserEventModel> events;
    private RecyclerView recyclerView;
    private RecyclerView searchRecyclerView;
    private GridLayoutManager searchGridLayout;
    private GridLayoutManager gridLayout;
    private EventAdapter adapter;
    private ProfileTracker profileTracker;
    private TextView t;
    private Intent ownIntent;
    static MainActivity.FbUserModel fbUserModel;
    private Gson gson = new Gson();
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getActivity().getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View view =  inflater.inflate(R.layout.fragment_browse_events, container, false);

        ownIntent = getActivity().getIntent();
        fbUserModel = ((BrowseActivity) getActivity()).getFbUserModel();


        recyclerView = (RecyclerView) view.findViewById(R.id.eventResults);
        events = ((BrowseActivity) getActivity()).getEventsfromDB();

        gridLayout = new GridLayoutManager(getActivity(), events.size());
        recyclerView.setLayoutManager(gridLayout);

        adapter = new EventAdapter(getActivity(), events);
        adapter.be = getActivity();
        recyclerView.setAdapter(adapter);

        t = (TextView) view.findViewById(R.id.user_header_name);
        t.append("  "+fbUserModel.name);
        t.setCompoundDrawablesWithIntrinsicBounds(R.drawable.com_facebook_button_icon_blue, 0, 0, 0);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(getActivity(), ProfileView.class);
                profileIntent.putExtra("user", ownIntent.getStringExtra("user"));
                getActivity().startActivity(profileIntent);
            }
        });

        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (gridLayout.findLastCompletelyVisibleItemPosition() == events.size() - 1)
                    events = getEventsfromDB();
            }
        });*/

        LoginButton loginButton = view.findViewById(R.id.header_logoff_button);
        loginButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.add_event_fbutton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), CreateEvent.class);
                getActivity().startActivity(myIntent);
            }
        });

        //registers when text is typed on search bar and calls onQuery... methods
        searchView = (SearchView) view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener((SearchView.OnQueryTextListener) getActivity());

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile==null) redirect();
            }
        };
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browse_events, container, false);
    }


    private void redirect() {
        Intent browseEvents = new Intent(getActivity(), MainActivity.class);
        startActivity(browseEvents);
    }

}
