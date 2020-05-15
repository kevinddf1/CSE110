package com.example.cse110.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.cse110.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class pieChartFragment extends Fragment {

    AnyChartView anyChartView;
    List<String> cateArrayList =new ArrayList<>();
    List<Integer> totalExpenseArrayList = new ArrayList<>();







    public pieChartFragment() {

        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment











        return inflater.inflate(R.layout.fragment_pie_chart, container, false);
    }


    public void onViewCreated(View view,  Bundle savedInstanceState) {
        anyChartView = (AnyChartView) getView().findViewById(R.id.pie_chart_view);
        GraphsActivity activity=(GraphsActivity) getActivity();
        cateArrayList=activity.getCateArrayList();
        totalExpenseArrayList=activity.getTotalExpenseArrayList();
        setupPieChart();
    }



    public void setupPieChart(){

        Pie pie= AnyChart.pie();
        List<DataEntry> dataEntries = new ArrayList<>();
        for (int i=0; i<cateArrayList.size(); i++){
            dataEntries.add(new ValueDataEntry(cateArrayList.get(i), totalExpenseArrayList.get(i)));
        }

        pie.data(dataEntries);
        anyChartView.setChart(pie);
    }

}
