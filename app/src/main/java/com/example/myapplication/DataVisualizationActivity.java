package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class DataVisualizationActivity extends AppCompatActivity {

    private static final List<String> CATEGORIES = Collections.unmodifiableList(
            Arrays.asList("Food", "Entertainment", "Travel", "School", "Utilities")
    );

    private TextView textDate, textBudget, textRemaining, textExpenses;
    private ViewPager2 categoryViewPager;
    private RecyclerView expensesRecyclerView;
    private Calendar currentCalendar = Calendar.getInstance();
    private FirebaseHelper firebaseHelper;
    private List<Expense> expensesList;
    private List<ExpenseItem> expensesForMonth;
    private List<Budget> budgetList;
    private ExpensesAdapter expensesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visualization);


        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.data);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.budget) {
                startActivity(new Intent(getApplicationContext(), BudgetPageActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.expense) {
                startActivity(new Intent(getApplicationContext(), ExpensePageActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.data) {
                return true;
            }
            else if (item.getItemId() == R.id.account) {
                startActivity(new Intent(getApplicationContext(), AccountPageActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });


        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(getText(R.string.data_viz_header));

        this.firebaseHelper = new FirebaseHelper();

        this.expensesList = new ArrayList();
        this.expensesForMonth = new ArrayList<>();
        this.budgetList = new ArrayList();

        this.textDate = findViewById(R.id.textDate);
        this.textBudget = findViewById(R.id.textBudgetValue);
        this.textRemaining = findViewById(R.id.textRemainingValue);
        this.textExpenses = findViewById(R.id.textExpenseValue);
        this.categoryViewPager = findViewById(R.id.categoryViewPager);

        this.expensesRecyclerView = findViewById(R.id.expensesRecyclerView);
        this.expensesRecyclerView.setHasFixedSize(true);
        this.expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.expensesAdapter = new ExpensesAdapter(new ArrayList<>());
        this.expensesRecyclerView.setAdapter(this.expensesAdapter);

        addCategoriesToPager();
        retrieveData();
        setupMonthIterationButtons();
        updateDateDisplay();
    }

    private void addCategoriesToPager() {
        this.categoryViewPager.setAdapter(new CategoryAdapter(CATEGORIES));

        this.categoryViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                retrieveData();
            }
        });
    }

    private boolean isCategorySame(Object obj) {
        try {
            // Get current category from swiper
            String category = CATEGORIES.get(this.categoryViewPager.getCurrentItem());
            Method getCategoryMethod = obj.getClass().getMethod("getCategory");
            return Objects.equals(getCategoryMethod.invoke(obj), category);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    private double updateBudget() {
        double budget = 0;

        for (Budget categoryBudget : this.budgetList) {
            if (isCategorySame(categoryBudget)) {
                budget = categoryBudget.getAmount();
                String formattedBudget = String.format(getResources().getString(R.string.formatted_currency), budget);
                this.textBudget.setText(formattedBudget);
                break;
            }
        }

        return budget;
    }

    private double updateExpenses() {
        double totalExpenses = 0;

        for (Expense expense : this.expensesList) {
            if (inCurrentMonth(expense.getDate()) && isCategorySame(expense)) {
                totalExpenses += expense.getAmount();
            }
        }

        String formattedExpenses = String.format(getResources().getString(R.string.formatted_currency), totalExpenses);
        this.textExpenses.setText(formattedExpenses);
        return totalExpenses;
    }

    private void updateRemaining(double remaining) {
        String formattedRemaining = String.format(getResources().getString(R.string.formatted_currency), remaining);
        this.textRemaining.setText(formattedRemaining);
    }

    private void updateExpenseDetails() {
        this.expensesForMonth.clear();

        for (Expense expense : this.expensesList) {
            if (inCurrentMonth(expense.getDate()) && isCategorySame(expense)) {
                this.expensesForMonth.add(new ExpenseItem(expense.getDescription(), expense.getAmount()));
            }
        }

        this.expensesAdapter.setExpenses(this.expensesForMonth);
        this.expensesAdapter.notifyDataSetChanged();
    }

    private void updateMonetaryValues() {
        double budget = updateBudget();
        double expense = updateExpenses();
        updateRemaining(budget - expense);
        updateExpenseDetails();
    }

    /*
    Forcing synchronization here, based on:
    https://stackoverflow.com/questions/44548932/grab-data-from-firebase-with-java
     */
    private void retrieveData() {
        CountDownLatch latch = new CountDownLatch(2);

        //TODO: Replace with current user
        String userId = "kartik";

        this.firebaseHelper.getUserExpenses(userId, expensesList -> {
            this.expensesList.clear();
            this.expensesList.addAll(expensesList);
            latch.countDown();
        });

        this.firebaseHelper.getUserBudgets(userId, budgetList -> {
            this.budgetList.clear();
            this.budgetList.addAll(budgetList);
            latch.countDown();
        });

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(this::updateMonetaryValues);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateDateDisplay() {
        String monthYear = this.currentCalendar.getDisplayName(
                Calendar.MONTH,
                Calendar.SHORT,
                Locale.getDefault()) +
                " " +
                this.currentCalendar.get(Calendar.YEAR);

        this.textDate.setText(monthYear);
    }

    private void setupMonthIterationButtons() {
        Button btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        Button btnNextMonth = findViewById(R.id.btnNextMonth);

        btnPreviousMonth.setOnClickListener(v -> {
            this.currentCalendar.add(Calendar.MONTH, -1);
            updateDateDisplay();
            retrieveData();
        });

        btnNextMonth.setOnClickListener(v -> {
            this.currentCalendar.add(Calendar.MONTH, 1);
            updateDateDisplay();
            retrieveData();
        });
    }

    private boolean inCurrentMonth(long epochTime) {
        Calendar epochCalendar = Calendar.getInstance();
        epochCalendar.setTimeInMillis(epochTime);

        int epochMonth = epochCalendar.get(Calendar.MONTH);
        int epochYear = epochCalendar.get(Calendar.YEAR);

        int currentMonth = this.currentCalendar.get(Calendar.MONTH);
        int currentYear = this.currentCalendar.get(Calendar.YEAR);

        return epochMonth == currentMonth && epochYear == currentYear;
    }
}
