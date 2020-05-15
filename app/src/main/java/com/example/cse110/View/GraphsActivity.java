package com.example.cse110.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.cse110.Controller.Category;
import com.example.cse110.Controller.Expense;
import com.example.cse110.Controller.MonthlyData;
import com.example.cse110.Controller.Settings;
import com.example.cse110.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A class representing the graphs for PiggyBank.
 * When user presses: See Graph, this page will appear.
 * @author Fan Ding
 * @version April 28
 *
 */
public class GraphsActivity extends AppCompatActivity {

    AnyChartView anyChartView;
    List<String> cateArrayList =new ArrayList<>();
    List<Integer> totalExpenseArrayList = new ArrayList<>();



    /**
     * Key for pulling an object of monthlyData in the HistoryDetailedActivity
     * @see #onCreate(Bundle)
     */
    public static final String Graphs_DATA_INTENT = "GraphsActivity monthlyData";
    public static final String MONTHLY_DATA_INTENT = "CategoriesListActivity monthlyData";
    public static final String HISTORY_DATA_INTENT = "HistoryActivity monthlyData";
    public static final String SETTINGS_INTENT = "SettingsActivity settings";

    private MonthlyData current_month;
    private Settings settings;

    private ArrayList<Category> categoryArrayList;




    /**
     * The only constructor for instantiating the pie chart page
     * @see AppCompatActivity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        //TabLayout implemented here, which allow u to switch between different graphs, like pie Chart and line chart.
        TabLayout tabLayout= findViewById(R.id.tabBar);
        TabItem pieChartTab= findViewById(R.id.pieChartTab);
        TabItem columnChartTab=findViewById(R.id.columnChartTab);
        TabItem lineChartTab= findViewById(R.id.lineChartTab);
        final ViewPager viewPager = findViewById(R.id.viewPager);

        PagerAdapter pagerAdapter=new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));





        //anyChartView=findViewById(R.id.any_chart_view);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setLabelVisibilityMode(1);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
        navView.setOnNavigationItemSelectedListener(navListener);

        //Retrieve passed in MonthlyData object and extract date/categories
        Intent intent = getIntent();
        current_month = intent.getParcelableExtra(Graphs_DATA_INTENT);
        settings = intent.getParcelableExtra(SETTINGS_INTENT);

        categoryArrayList= current_month.getCategoriesAsArray();
        for (int i=0; i<categoryArrayList.size();i++){
            Category c = categoryArrayList.get(i);
            Log.d("what", c.getName());
            cateArrayList.add(c.getName());
            Log.d("price", formatMoneyString(Double.toString(getTotalExpense(c)/100.00)));
            totalExpenseArrayList.add(getTotalExpense(c)/100);
        }

        //setupPieChart();
        //instead setup pie chart we need to put this 2 arraylist to pieChartFragment
    }

    public List<String> getCateArrayList(){
        return cateArrayList;
    }

    public List<Integer> getTotalExpenseArrayList(){
        return totalExpenseArrayList;
    }

    private int getTotalExpense(Category c) {
        int ret=0;
        ArrayList<Expense> expenseArray= c.getExpenses();
        for (int i=0; i<expenseArray.size(); i++){
            ret+=expenseArray.get(i).getCost();
        }

        return ret;
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

    private String formatMoneyString(String valueToFormat){
        // Add formatting for whole numbers
        if(valueToFormat.indexOf('.') == -1){
            valueToFormat = valueToFormat.concat(".00");
        }else{
            //Ensure only valid input
            int costLength = valueToFormat.length();
            int decimalPlace = valueToFormat.indexOf(".");

            // If the user inputs a number formatted as "<num>.", appends a 00 after the decimal
            if (costLength - decimalPlace == 1) {
                valueToFormat = valueToFormat.substring(0, decimalPlace + 1) +  "00";
            }
            // If the user inputs a number formatted as "<num>.1", where 1 could be any number,
            // appends a 0 to the end
            else if (costLength - decimalPlace == 2) {
                valueToFormat = valueToFormat.substring(0, decimalPlace + 1 + 1) + "0";
            }
            // If the user inputs a number with >= 2 decimal places, only displays up to 2
            else {
                valueToFormat = valueToFormat.substring(0, valueToFormat.indexOf(".") + 2 + 1);
            }
        }

        int hundredthComma = valueToFormat.length() - 6;
        int thousandthComma = valueToFormat.length() - 9;
        if(valueToFormat.length() <= 6){
            return valueToFormat;
        }else if(valueToFormat.length() <= 9){
            return valueToFormat.substring(0, hundredthComma) + "," + valueToFormat.substring(hundredthComma);
        }
        return valueToFormat.substring(0, thousandthComma) + "," + valueToFormat.substring(thousandthComma , hundredthComma) + "," + valueToFormat.substring(hundredthComma );
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:

                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
                            setResult(RESULT_OK, intent);
                            intent.putExtra(MONTHLY_DATA_INTENT, current_month);
                            startActivityForResult(intent, 1);
                            overridePendingTransition(0, 0);
                            return true;
                        case R.id.navigation_lists:
                            Intent in = new Intent(getBaseContext(), CategoriesListActivity.class);
                            in.putExtra(HISTORY_DATA_INTENT, current_month);
                            in.putExtra(MONTHLY_DATA_INTENT, current_month);
                            startActivityForResult(in, 1);
                            overridePendingTransition(0, 0);

                            return true;

                        case R.id.navigation_history:
                            Intent i = new Intent(getBaseContext(), HistoryActivity.class);
                            setResult(RESULT_OK, i);
                            //i.putExtra(CategoriesListActivity.MONTHLY_DATA_INTENT, monthlyData);
                            // TODO: grab this from the database

                            if (current_month == null) {
                                Calendar today = Calendar.getInstance();
                                current_month = new MonthlyData(today.get(Calendar.MONTH), today.get(Calendar.YEAR));
                            }

                            i.putExtra(HISTORY_DATA_INTENT, current_month);
                            startActivityForResult(i, 1);
                            overridePendingTransition(0, 0);
                            return true;

                        case R.id.navigation_graphs:
                            return true;
                        case R.id.navigation_settings:
                            Intent inten = new Intent(getBaseContext(), SettingsActivity.class);
                            if (settings == null) {
                                settings = new Settings();
                            }
                            inten.putExtra(SettingsActivity.SETTINGS_INTENT, settings);
                            startActivityForResult(inten, 1);
                            overridePendingTransition(0, 0);
                            return true;
                    }
                    return false;
                }
            };
}