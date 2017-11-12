package fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import models.AdminEventModel;
import models.ParticipatorModel;
import remm.sharedtrip.AdminEventActivity;
import remm.sharedtrip.R;
import utils.AdminEventUtils;

/**
 * Created by Mark on 12.11.2017.
 */

public class ParticipatorsFragment extends Fragment {

    public void setAea(AdminEventActivity aea) {
        this.aea = aea;
    }

    public AdminEventModel aem;
    public TextView badge;
    private AdminEventActivity aea;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.fragment_participators, container, false);
        aea.doAdapterThing((RecyclerView) me.findViewById(R.id.admin_event_participators_results), aem, badge);
        return me;
    }


}
