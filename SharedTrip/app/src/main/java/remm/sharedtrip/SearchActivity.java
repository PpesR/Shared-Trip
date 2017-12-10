package remm.sharedtrip;

        import android.annotation.SuppressLint;
        import android.content.Context;
        import android.os.Bundle;
        import android.support.annotation.NonNull;
        import android.support.v4.app.FragmentManager;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.GridLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.SearchView;


        import com.google.gson.Gson;

        import java.util.List;
        import java.util.concurrent.ExecutionException;

        import adapters.EventAdapter;
        import adapters.SearchResultAdapter;
        import fragments.BrowseEventsFragment;
        import models.MyEventModel;
        import models.UserEventModel;
        import utils.BrowseUtil;

/**
 * Created by MihkelV on 10.12.2017.
 */

public class SearchActivity extends AppCompatActivity {



    private int userModelId;
    private String apiPrefix;
    private RecyclerView recyclerView;
    private List<UserEventModel> events;
    private GridLayoutManager gridLayout;
    private SearchResultAdapter adapter;
    private Gson gson = new Gson();
    private MainActivity.FbGoogleUserModel userModel;
    private SearchView searchView;
    private Context context;



        @SuppressLint("RestrictedApi")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            context = this;
            super.onCreate(savedInstanceState);
            userModel = gson.fromJson(
                    getIntent().getStringExtra("user")
                    , MainActivity.FbGoogleUserModel.class);
            setContentView(R.layout.activity_search);
            userModelId = Integer.valueOf(getIntent().getStringExtra("userid"));
            apiPrefix = getIntent().getStringExtra("prefix");
            events = getEventsfromDB(getIntent().getStringExtra("filter"));


            recyclerView = findViewById(R.id.eventSearchResults);
            gridLayout = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(gridLayout);
            adapter = new SearchResultAdapter(this, events);
            adapter.browseActivity = this;
            recyclerView.setAdapter(adapter);


            searchView = findViewById(R.id.searchActivityView); //can search new events after old search
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()  {
                @Override
                public boolean onQueryTextSubmit(String filter) {
                    events = getEventsfromDB(filter);
                    adapter = new SearchResultAdapter(context, events);
                    recyclerView.setAdapter(adapter);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String filter) {
                    return false;
                }
            });

        }



        private List<UserEventModel> getEventsfromDB(String filter) {
                BrowseUtil.EventRetrievalTask<Void> asyncTask = new BrowseUtil.EventRetrievalTask<>(userModelId, filter, apiPrefix);
                try {
                        return asyncTask.execute().get();
                } catch (InterruptedException e) {
                        e.printStackTrace();
                } catch (ExecutionException e) {
                        e.printStackTrace();
                }
                return null;
        }
    public String getApiPrefix() {
        return apiPrefix;
    }

    public MainActivity.FbGoogleUserModel getUserModel() {
        return userModel;
    }
}
