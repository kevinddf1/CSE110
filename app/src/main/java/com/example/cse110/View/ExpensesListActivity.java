package com.example.cse110.View;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cse110.Controller.Category;
import com.example.cse110.Controller.Expense;
import com.example.cse110.Controller.MonthlyData;
import com.example.cse110.R;
import com.example.cse110.Model.Database;
import com.example.cse110.Model.FormattingTool;
import com.example.cse110.View.graphs.GraphsActivity;
import com.example.cse110.View.history.HistoryActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * The app's page for displaying a user's expenses and adding new ones.
 *
 * @author Peter Thuycam Erdong Minxuan
 */
public class ExpensesListActivity extends AppCompatActivity {

    /**
     * Keys for pulling info into the page and push info to new pages.
     */
    public static final String MONTHLY_DATA_INTENT = "ExpenseListActivity monthlyData";
    private static final String HISTORY_DATA_INTENT = "HistoryActivity monthlyData";
    public static final String CATEGORY_NAME_INTENT = "ExpenseListActivity categoryName";
    private static final String Graphs_DATA_INTENT = "GraphsActivity monthlyData";
    private static final String LIST_OF_MONTHS = "List of Months"; //For past months in HistoryActivity.java

    /**
     * Constants for error checking
     */
    private static final int MAX_EXPENSE_VALUE = 9999999;
    private static final int MAX_BUDGET = 7;    //Our max allowable int is 9,999,999 which is 7 place values
    private static final double DOUBLE = 100.00;


    /**
     * Front-end components to display a user's info
     */
    private EditText expenseName, expenseCost;
    private EditText categoryBudget, categoryName;     //Minxuan
    private TextView totalExpensesDisplay;
    private Button btnAdd;

    /**
     * Adapter to connect backend objects to the frontend renders.
     */
    //List Structure
    private ExpenseListAdapter expenseAdapter;

    /**
     * Backend objects to retrieve and update a user's information
     */
    private MonthlyData monthlyData;
    private MonthlyData thisMonthsData;
    private Category category;

    /**
     * Gets our Database singleton.
     */
    private final Database base = Database.getInstance();

    /**
     * Formatting tool for money displays.
     */
    private final FormattingTool formattingTool = new FormattingTool();

    /**
     * Main method that handles front-end interactions and directs new changes.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenselist);

        //Initializes the current category being displayed
        initializeCurrentCategory();

        //Render the initial static info (budget and category CAN be changed)
        renderStaticComponents();

        //Set up navBar components
        navBarSetUp();

        //Render dynamic components like the button and user inputs
        renderVariableComponents();

        //Set up the ListView to display the user's expenses and attach adapter
        setUpList();

        //Handle the user clicking on budget EditText
        handleBudgetChanges();

        //handle the user clicking on the CategoryName EditText
        handleCategoryNameChanges();

        //Handle the user pressing the '+' button which indicates adding an expense
        handleExpenseAdditions();
    }

    /**
     * Extracts the MonthlyData from incoming intents and identifies which category is being displayed
     *
     * @return The name of the category we are currently displaying.
     */
    private void initializeCurrentCategory() {

        //Initialize user's current Month
        Intent intent = getIntent();
        monthlyData = intent.getParcelableExtra(MONTHLY_DATA_INTENT);

        //Identify and initialized selected month
        final String categoryNameFromParent = intent.getStringExtra(CATEGORY_NAME_INTENT);
        category = monthlyData.getCategory(categoryNameFromParent);

    }

    /**
     * Handle the user pressing the '+' and adding expenses.
     * Error checks for expenses that are too large or missing info.
     */
    private void handleExpenseAdditions() {
        // Set Event Handler to add items to the list
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create Date Object
                Calendar today = Calendar.getInstance();

                // Ensure that both fields are filled.
                if (!expenseCost.getText().toString().isEmpty() && !expenseName.getText().toString().isEmpty()) {

                    //Check that we do not go over the max allowed expense
                    try {
                        if (Double.parseDouble(expenseCost.getText().toString()) > MAX_EXPENSE_VALUE)
                            throw new Exception();

                        // Create new item and update adapter
                        category.createExpense(expenseName.getText().toString(), Double.parseDouble(expenseCost.getText().toString()), today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
                        // Update total expenses for this category
                        double currentTotalExpense = category.getTotalExpenses() / DOUBLE;
                        String totalExpensesRendering = "$" + formattingTool.formatMoneyString(formattingTool.formatDecimal(Double.toString(currentTotalExpense)));
                        totalExpensesDisplay.setText(totalExpensesRendering);

                        // Displays a Toast message if the user goes over their budget when adding an expense
                        if (category.getBudget() < currentTotalExpense) {
                            Toast.makeText(getBaseContext(), "Uh oh! The total has exceeded the " + category.getName() + " budget.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getBaseContext(), "Item added.", Toast.LENGTH_SHORT).show();
                        }

                        //Update the database with most current expenses
                        base.insertTotalExpense(monthlyData.getYear(), monthlyData.getIntMonth(), monthlyData.getTotalExpensesAsCents());


                        //Clear all inputs the user entered
                        expenseName.getText().clear();
                        expenseCost.getText().clear();

                        //Update adapter
                        expenseAdapter.notifyDataSetChanged();
                    } catch (Exception overflow) {
                        Toast.makeText(getBaseContext(), "Please provide expense cost less than $9,999,999", Toast.LENGTH_LONG).show();
                    }

                } else {
                    // Insufficient number of filled fields results in an error warning.
                    Toast missingInformationWarning = Toast.makeText(getBaseContext(), "Please fill in expense name and cost.", Toast.LENGTH_SHORT);
                    missingInformationWarning.show();
                }
            }
        });
    }

    //MINXUAN
    private void handleCategoryNameChanges() {
        //Detect User Changes for category NAME
        categoryName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && categoryName.getText().toString() != null) {
                    if (categoryName.getText().toString().isEmpty()) {
                        categoryName.setText(category.getName());
                    } else {
                        // Check if the "new" NAME already exists
                        if (monthlyData.checkNameExists(categoryName.getText().toString())) {
                            categoryName.setText(category.getName());
                            Toast.makeText(getBaseContext(), "Category name already exists!", Toast.LENGTH_LONG).show();
                        } else {
                            //Retrieve current category name
                            String previousCatName = category.getName();
                            String newCatName = categoryName.getText().toString();


                            //Update current category name in the monthly category list
                            monthlyData.renameCategory(previousCatName, newCatName);

                            //Get recently change name
                            category.setName(newCatName);

                            /* Reflect the NAME change in database */
                            int year, month;
                            year = monthlyData.getYear();
                            month = monthlyData.getIntMonth();

                            //insert "new" category
                            base.insertCategoryName(newCatName, year, month);
                            base.insertCategoryBudget(category.getBudget(), newCatName, year, month);
                            base.insertCategoryDate(year, month, newCatName);

                            //insert expenses from the "old" category
                            for (Expense ex : category.getExpenses()) {
                                base.insertExpense(ex.getCost(), ex.getName(), newCatName, year, month, ex.getDay(), ex.getId());
                            }

                            // Delete the "old" category
                            base.delete_cate(previousCatName, year, month);

                            //App display the new name
                            categoryName.setText(newCatName);
                            Toast.makeText(getBaseContext(), "Category successfully renamed!", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    categoryName.getText().clear();
                }
            }
        });
    }

    /**
     * Handle the user selecting to change budget.
     */
    private void handleBudgetChanges() {
        //Detect User Changes for category BUDGET
        categoryBudget.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                //If the user clicked on the budget clear existing display
                if (hasFocus) {
                    categoryBudget.getText().clear();
                }
                //If the user clicked away from budget
                else {
                    //Check for too large of a budget
                    if (categoryBudget.getText().toString().length() > MAX_BUDGET) {
                        Toast.makeText(getBaseContext(), "A category cannot have a budget greater than $9,999,999.", Toast.LENGTH_LONG).show();
                    }
                    //If the user did not input any text, revert to old budget
                    else if (categoryBudget.getText().toString().isEmpty()) {
                        String categoryBudgetRendering = "$" + formattingTool.formatIntMoneyString(category.getBudgetAsString());
                        categoryBudget.setText(categoryBudgetRendering);
                    } else {
                        //Ensure valid input
                        try {
                            // Create new item and update adapter
                            category.setBudget(Integer.parseInt(categoryBudget.getText().toString()));

                            //Update monthly totalBudget
                            monthlyData.calculateTotalBudget();
                            //Update new budget info to database
                            base.insertTotalBudget(monthlyData.getYear(), monthlyData.getIntMonth(), monthlyData.getTotalBudget());

                            //Render new budget
                            String newCategoryBudgetRendering = "$" + formattingTool.formatIntMoneyString(category.getBudgetAsString());
                            categoryBudget.setText(newCategoryBudgetRendering);

                            //Check if the user overspent
                            overSpendingChecker();

                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Invalid input", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }


    /**
     * Compares budget to total expenses and makes user aware of overspending.
     */
    private void overSpendingChecker() {
        // Sends a Toast message if the user changes the category's budget and the new budgets is less than total expenses
        if (category.getBudget() < category.getTotalExpenses() / 100.00) {
            Toast.makeText(getBaseContext(), "Uh oh! The total has exceeded the " + category.getName() + " budget.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), "Category budget successfully updated!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Set up a ListView to display a user's expenses and attach an adapter.
     */
    private void setUpList() {
        // Initialize List
        final ArrayList<Expense> arrayOfItems = category.getExpenses();
        expenseAdapter = new ExpenseListAdapter(this, arrayOfItems, category);
        ListView expensesList = findViewById(R.id.history_expenses);

        //Attach adapter
        expensesList.setAdapter(expenseAdapter);
    }

    /**
     * Render user input components and the '+' button
     */
    private void renderVariableComponents() {
        // Bind element from XML file
        expenseName = findViewById(R.id.expense_name);
        expenseCost = findViewById(R.id.expense_cost);
        btnAdd = findViewById(R.id.AddToList);
    }

    /**
     * Render front end components that are rendered initially
     */
    private void renderStaticComponents() {
        //Render category name
        categoryName = findViewById(R.id.category_name);
        categoryName.setText(category.getName());

        //Render category budget
        categoryBudget = findViewById((R.id.budget_display_history)); //Brent
        String categoryBudgetRendering = "$" + formattingTool.formatIntMoneyString(category.getBudgetAsString());
        categoryBudget.setText(categoryBudgetRendering);


        //Render category's total expenses
        totalExpensesDisplay = findViewById(R.id.total_expenses);
        String totalExpensesRendering = "$" + formattingTool.formatMoneyString(formattingTool.formatDecimal(Double.toString(category.getTotalExpenses()/100.00)));
        totalExpensesDisplay.setText(totalExpensesRendering); //Account for initial lack of decimal values
    }

    /**
     * Handler for an intent into this page from a backpress or navBar press
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                //Instantiate the most current MonthlyData for the user
                monthlyData = data.getParcelableExtra(ExpensesListActivity.MONTHLY_DATA_INTENT);
            }
        }
    }

    /**
     * Attaches most recent MonthlyData of the user, -> CategoriesListActivity
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(MONTHLY_DATA_INTENT, monthlyData);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }


    //Handle pressing away from setting category
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    assert imm != null;
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    /**
     * Update the display for expenseList, used by the adapter.
     *
     * @param toDisplay The new total expenses for a category once an item was deleted.
     */
    public void updateTotalExpenseDisplay(String toDisplay) {
        totalExpensesDisplay.setText(toDisplay);
        // Displays a Toast message that confirms the expense was deleted
        Toast.makeText(getBaseContext(), "Item deleted.", Toast.LENGTH_SHORT).show();

        //Update the database with most current expenses
        base.insertTotalExpense(monthlyData.getYear(), monthlyData.getIntMonth(), monthlyData.getTotalExpensesAsCents());

    }

    /**
     * Set up navBar
     */
    private void navBarSetUp() {
        //create the nav bar view
        BottomNavigationView navView = findViewById(R.id.nav_view);
        //make all page names visible
        navView.setLabelVisibilityMode(1);
        //check the lists icon
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        navView.setOnNavigationItemSelectedListener(navListener);
    }

    /**
     * Helper method to contain the logic for navigation bar to navigate to the lists page
     */
    private void homePageHandler() {
        //create new intent
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        setResult(RESULT_OK, intent);
        //put extra monthly data into new intent
        intent.putExtra(CategoriesListActivity.MONTHLY_DATA_INTENT, monthlyData);
        startActivityForResult(intent, 1);
        //avoid shifting
        overridePendingTransition(0, 0);
    }

    /**
     * Helper method to contain the logic for navigation bar to navigate to the history page
     */
    private void historyPageHandler() {
        ValueEventListener Listener = new ValueEventListener() {
            //The onDataChange() method is called every time data is changed at the specified database reference, including changes to children.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent i = new Intent(getBaseContext(), HistoryActivity.class);
                //set up the date for monthly data
                Calendar today = Calendar.getInstance();
                int month = today.get(Calendar.MONTH);
                int year = today.get(Calendar.YEAR);
                //Retrieve the monthly data from the database
                thisMonthsData = base.RetrieveDataCurrent(dataSnapshot, thisMonthsData, year, month);
                //put extra data into new intent
                i.putExtra(HISTORY_DATA_INTENT, thisMonthsData);
                //Add the past month's history (includes current)e
                i.putExtra(LIST_OF_MONTHS, base.getPastMonthSummary(dataSnapshot));
                startActivityForResult(i, 1);
                //avoid shifting
                overridePendingTransition(0, 0);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
            }
        };
        base.getMyRef().addListenerForSingleValueEvent(Listener);
    }

    /**
     * Helper method to contain the logic for navigation bar to navigate to the graph page
     */
    private void graphPageHandler() {
        base.getMyRef().addListenerForSingleValueEvent(new ValueEventListener() {
            //The onDataChange() method is called every time data is changed at the specified database reference, including changes to children.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent i = new Intent(getBaseContext(), GraphsActivity.class);
                //set up the date for monthly data
                Calendar today = Calendar.getInstance();
                int month = today.get(Calendar.MONTH);
                int year = today.get(Calendar.YEAR);
                //Retrieve the monthly data from the database
                thisMonthsData = base.RetrieveDataCurrent(dataSnapshot, thisMonthsData, year, month);
                //Add the past month's history (includes current)e
                i.putExtra(Graphs_DATA_INTENT, thisMonthsData);
                //Add the past month's history (includes current)e
                i.putExtra(LIST_OF_MONTHS, base.getPastMonthSummary(dataSnapshot));
                startActivityForResult(i, 1);
                //avoid shifting
                overridePendingTransition(0, 0);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
            }
        });
    }

    // BOTTOM NAVIGATION
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            homePageHandler();
                            return true;
                        case R.id.navigation_lists:
                            return true;
                        case R.id.navigation_history:
                            historyPageHandler();
                            return true;
                        case R.id.navigation_graphs:
                            graphPageHandler();
                            return true;
                        case R.id.navigation_settings:
                            //create new intent for settings activity
                            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            return true;
                    }
                    return false;
                }
            };
}
