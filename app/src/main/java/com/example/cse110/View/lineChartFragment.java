package com.example.cse110.View;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cse110.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class lineChartFragment extends Fragment {

    public lineChartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_line_chart, container, false);
    }
}