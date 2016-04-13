package edu.sc.snacktrack;

/**
 * Created by Steven on 11/1/2015.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int position = getArguments().getInt("position");
        String[] tests = getResources().getStringArray(R.array.main_drawer_items);
        View v = inflater.inflate(R.layout.fragment_layout, container, false);
        TextView tv = (TextView) v.findViewById(R.id.tv_content);

        tv.setText(tests[position]);
        // TODO: Figure out why below line returns null pointer exception
        //getActivity().getActionBar().setTitle(tests[position]);

        return v;
    }
}