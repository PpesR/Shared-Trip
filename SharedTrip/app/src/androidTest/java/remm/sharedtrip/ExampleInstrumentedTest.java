package remm.sharedtrip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import models.EventModel;
import models.UserEventModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.junit.Assert.*;


/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static MainActivity.FbUserModel userModel;
    static String titleString, descriptionString, destinationString, spotsString, costString;
    static String creator_id;
    private List<UserEventModel> events;
    static boolean private_event;
    static UserEventModel model;

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("remm.sharedtrip", appContext.getPackageName());
    }


    @Rule
    public ActivityTestRule<BrowseEvents> browseEventsActivityTestRuleRule
            = new ActivityTestRule<>(
            BrowseEvents.class,
            true,     // initialTouchMode
            false);   // launchActivity. False to customize the intent
    @Rule
    public ActivityTestRule<CreateEvent> createEventsActivityRule
            = new ActivityTestRule<>(
            CreateEvent.class,
            true,     // initialTouchMode
            false);   // launchActivity. False to customize the intent
    @Test
    public void createEvent() throws Exception{
        userModel = new MainActivity.FbUserModel("1", "test", "comp", "00");
        Intent browseIntent = new Intent();
        browseIntent.putExtra("user", new Gson().toJson(userModel));
        browseEventsActivityTestRuleRule.launchActivity(browseIntent);

        events = getEventsfromDB();
        int testiarv = events.size();
        Intent createIntent = new Intent();
        createEventsActivityRule.launchActivity(createIntent);

        postEventsToDb();
        events = getEventsfromDB();
        assertEquals(events.size() - 1, testiarv);
        assertEquals(events.get(events.size() - 1).getName(), "autoTest");
    }

    private List<UserEventModel> getEventsfromDB() {

        BrowseEvents.EventRetrievalTask<Void> asyncTask = new BrowseEvents.EventRetrievalTask<>();
        try {
            return asyncTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void postEventsToDb() {
        creator_id = "13";
        titleString = "autoTest";
        destinationString = "tEstland";
        descriptionString = "automated test";
        spotsString = "0";
        costString = "0";
        private_event =  false;
        model = new UserEventModel();
        model.setStartDate(null);
        model.setEndDate(null);
        model.setImageLink(null);
        EventCreationTask<String> asyncTask = new EventCreationTask<>();
        try {
            String s = asyncTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static class EventCreationTask<String> extends AsyncTask<EventModel, Void, String> {

        @SafeVarargs
        @Override
        protected final String doInBackground(EventModel... events) {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("user", creator_id+"")
                    .add("location", destinationString)
                    .add("name", titleString)
                    .add("description", descriptionString)
                    .add("total_cost", costString)
                    .add("spots", spotsString)
                    .add("start_date", model.getStartDate()+"")
                    .add("end_date", model.getEndDate()+"")
                    .add("private", private_event ? "1" : "0")
                    .add("picture", model.getImageLink()+"");

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php?hdl=event")
                    .post(formBuilder.build())
                    .build();
            Call call = client.newCall(request);

            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        JSONArray actual = array.getJSONArray(2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            return null;
        }
    }
}
