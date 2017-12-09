package fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import adapters.MyEventsAdapter.MyEventsManager;
import models.MyEventModel;
import remm.sharedtrip.R;

/**
 * Created by Mark on 12.11.2017.
 */

public class ParticipatorsFragment extends Fragment {

    public void setManager(MyEventsManager manager) {
        this.manager = manager;
    }

    public MyEventModel eventModel;
    public TextView pendingBadge;
    private MyEventsManager manager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.fragment_participators, container, false);
        manager.setSubAdapter((RecyclerView) me.findViewById(R.id.admin_event_participators_results), eventModel, pendingBadge);
        return me;
    }


}
