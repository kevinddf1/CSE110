package com.example.cse110;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class CategoriesListActivity extends AppCompatActivity {
    public static final String MONTHLY_DATA_INTENT = "CategoriesListActivity monthlyData";

    EditText categoryName, categoryBudget;
    Button btnAdd;
    CategoriesListAdapter myAdapter;
    ListView categories;

    private MonthlyData monthlyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_list);

        // Bind element from XML file
        // Core elements of the activity
        categoryName = findViewById(R.id.category_name);
        categoryBudget = findViewById(R.id.category_budget);
        btnAdd = findViewById(R.id.AddToList);

        Intent intent = getIntent();
        monthlyData = intent.getParcelableExtra(MONTHLY_DATA_INTENT);

        // Initialize List
        ArrayList<Category> arrayOfItems = monthlyData.getCategoriesAsArray();
        // Checklist Structure
        myAdapter = new CategoriesListAdapter(this, arrayOfItems, monthlyData);
        categories = (ListView) findViewById(R.id.Categories);
        categories.setAdapter(myAdapter);

        categories.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("CLicked");

                Category currentItem = myAdapter.getItem(position);

                Intent i = new Intent(CategoriesListActivity.this, ExpensesListActivity.class);
                i.putExtra(ExpensesListActivity.MONTHLY_DATA_INTENT, monthlyData);
                i.putExtra(ExpensesListActivity.CATEGORY_NAME_INTENT, currentItem.getName());
                startActivityForResult(i, 1);
            }
        });

        // Set Event Handler to add items to the list
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Ensure that both fields are filled.
                if(!categoryBudget.getText().toString().isEmpty() && !categoryName.getText().toString().isEmpty() ) {

                    // Create new item and update adapter
                    monthlyData.createCategory(categoryName.getText().toString(), Integer.parseInt(categoryBudget.getText().toString()));
                    categoryName.getText().clear();
                    categoryBudget.getText().clear();
                    myAdapter.notifyDataSetChanged();
                }else{

                    // Insufficient number of filled fields results in an error warning.
                    Toast missingInformationWarning = Toast.makeText(getBaseContext(), "Missing Information", Toast.LENGTH_SHORT);
                    missingInformationWarning.show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                monthlyData = data.getParcelableExtra(ExpensesListActivity.MONTHLY_DATA_INTENT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(MONTHLY_DATA_INTENT, monthlyData);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
