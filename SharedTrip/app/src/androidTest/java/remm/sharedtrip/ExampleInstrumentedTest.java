package remm.sharedtrip;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import fragments.BrowseEventsFragment;
import models.UserEventModel;
import okhttp3.Call;
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

    // private static MainActivity.FbUserModel userModel;
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
    public ActivityTestRule<CreateEventActivity> createEventsActivityRule
            = new ActivityTestRule<>(
            CreateEventActivity.class,
            true,     // initialTouchMode
            false);   // launchActivity. False to customize the intent

    @Test
    public void CanCreateTripTest() throws IOException {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("user", "0")
                .add("location", "Test Suite")
                .add("name", "Test Event")
                .add("description", "This was created by an autotest")
                .add("total_cost", "10")
                .add("spots", "10")
                .add("start_date", "2017-11-12")
                .add("end_date", "2017-11-12")
                .add("private", "0")
                .add("picture", "null");

        final Request request = new Request.Builder()
                .url("http://146.185.135.219/requestrouter.php?hdl=event")
                .post(formBuilder.build())
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        String s = response.body().string();
        int createdId = 0;
        try {
            JSONArray initialArray = new JSONArray(s);
            Assert.assertEquals(initialArray.getString(0), "SUCCESS");
            JSONObject jsonResponse = initialArray.getJSONArray(2).getJSONObject(0);
            createdId = jsonResponse.getInt("event_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Request getRequest = new Request.Builder()
                .url("http://146.185.135.219/requestrouter.php?hdl=event&act=u&event=" + createdId)
                .build();
        call = client.newCall(getRequest);
        response = call.execute();
        s = response.body().string();
        try {
            JSONArray initialArray = new JSONArray(s);
            Assert.assertEquals(initialArray.getString(0), "SUCCESS");
            JSONArray jsonResponse = initialArray.getJSONArray(2).getJSONArray(0);
            Assert.assertEquals(jsonResponse.getInt(1), 0);
            Assert.assertEquals(jsonResponse.getString(2), "Test Suite");
            Assert.assertEquals(jsonResponse.getString(3), "Test Event");
            Assert.assertEquals(jsonResponse.getInt(4), 10);
            Assert.assertEquals(jsonResponse.getInt(5), 10);
            Assert.assertEquals(jsonResponse.getString(6), "This was created by an autotest");
            Assert.assertEquals(jsonResponse.getString(7), "2017-11-12 00:00:00");
            Assert.assertEquals(jsonResponse.getString(8), "2017-11-12 00:00:00");
            Assert.assertEquals(jsonResponse.getInt(9), 0);
            Assert.assertNotNull(jsonResponse.getString(10));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void CanJoinEvent() {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = null;
        formBuilder = new FormBody.Builder()
                .add("hdl", "event")
                .add("act", "join")
                .add("event", "120")
                .add("participator", "0");

        final Request request = new Request.Builder()
                .url("http://146.185.135.219/requestrouter.php")
                .post(formBuilder.build())
                .build();
        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            String s = response.body().string();
            JSONArray initialArray = new JSONArray(s);
            Assert.assertEquals(initialArray.getString(0), "SUCCESS");
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        final Request getRequest = new Request.Builder()
                .url("http://146.185.135.219/requestrouter.php?hdl=admin&act=pnd&event=" + 120)
                .build();
        call = client.newCall(getRequest);

        try {
            response = call.execute();
            String s = response.body().string();
            JSONArray initialArray = new JSONArray(s);
            Assert.assertEquals(initialArray.getString(0), "SUCCESS");
            JSONArray jsonResponse = initialArray.getJSONArray(2);
            JSONObject participator = jsonResponse.getJSONObject(0);
            Assert.assertEquals(participator.getInt("id"), 0);
            Assert.assertEquals(participator.getString("name"), "test");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
