package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO:
 * 2. API Submit & (Clear values, toast)
 */
public class ExpensePageFragment extends Fragment {

    private AutoCompleteTextView categoriesInput;
    private ArrayAdapter<Category> adapter;
    private EditText budgetAmountText, datePickerText, notesText;
    private long datePickerValue;
    private SwitchMaterial recurringExpenseToggle;
    private MaterialButton uploadExpenseButton;
    private FirebaseHelper firebaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_page, container, false);

        // Firebase helper
        firebaseHelper = new FirebaseHelper();

        // Budget Field
        budgetAmountText = view.findViewById(R.id.add_expense_budget);

        // Categories Field, Values
        categoriesInput = view.findViewById(R.id.add_expense_category_text);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        categoriesInput.setAdapter(adapter);

        firebaseHelper.getCategories(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot budgetsSnapshot) {
                Set<String> categories = new HashSet<>();

                for (DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
                    String category = budgetSnapshot.child("category").getValue(String.class);
                    categories.add(category);
                }

                // update categories dropdown
                updateCategoryOptions(new ArrayList<>(categories));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Ok", Toast.LENGTH_SHORT).show();
            }
        });

        // Upload Button
        uploadExpenseButton = view.findViewById(R.id.add_expense_upload_button);

        // Date Field
        datePickerText = view.findViewById(R.id.add_expense_date_picker);
        datePickerText.setOnClickListener(v -> showDatePickerDialog());
        datePickerValue = 0;

        // Notes Field
        notesText = view.findViewById(R.id.add_expense_notes);

        // Recurring Toggle Field
        recurringExpenseToggle = view.findViewById(R.id.add_expense_recurring_toggle);

        // Clear Button
        MaterialButton clearExpenseButton = view.findViewById(R.id.add_expense_clear_button);
        clearExpenseButton.setOnClickListener(v -> onClear());

        // Submit Button
        MaterialButton addExpenseButton = view.findViewById(R.id.add_expense_submit_button);
        addExpenseButton.setOnClickListener(v -> checkValues());

        return view;
    }

    /**
     * Check appropriate fields for validity. Submit if all valid.
     */
    private void checkValues() {
        if (checkValidField(budgetAmountText) && checkValidField(categoriesInput) &&
                checkValidField(datePickerText) && checkValidField(notesText)) {
            onSubmit();
        }
    }

    /**
     * Check if a text field is valid (non-empty). if it is invalid, set the error as "required"
     *
     * @param textViewField text field
     * @return whether the field is valid or not (non-empty)
     */
    private boolean checkValidField(EditText textViewField) {
        if (textViewField.getText().toString().trim().isEmpty()) {
            textViewField.setError("required");
            return false;
        } else {
            textViewField.setError(null);
            return true;
        }
    }

    private void onSubmit() {
        String category = categoriesInput.getText().toString();
        double amount = Double.parseDouble(budgetAmountText.getText().toString());
        String description = notesText.getText().toString();
        Long date = datePickerValue;
        String imageUrl = "";

        // create valid expense
        Expense expense = new Expense(category, amount, description, date, imageUrl);

        firebaseHelper.createExpense(expense, v -> {
            Toast.makeText(getContext(), "Expense Created!", Toast.LENGTH_SHORT).show();
            onClear();
        });
    }

    /**
     * Clear/reset values. Clears errors as well.
     */
    private void onClear() {
        budgetAmountText.setText("");
        categoriesInput.setText("");
        datePickerText.setText("");
        datePickerValue = 0;
        notesText.setText("");
        recurringExpenseToggle.setChecked(false);

        // clear errors
        budgetAmountText.setError(null);
        categoriesInput.setError(null);
        datePickerText.setError(null);
        notesText.setError(null);
    }

    public void showDatePickerDialog() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            datePickerValue = selection;

            // update date picker text on modal positive button click
            datePickerText.setText(datePicker.getHeaderText());
        });

        // show picker date
        datePicker.show(getParentFragmentManager(), "EXPENSE_PAGE_DATE_PICKER");
    }

    private void updateCategoryOptions(List<String> options) {
        List<Category> budgetCategories = new ArrayList<>();

        // convert List<String> to List<BudgetCategory>
        options.forEach(category -> budgetCategories.add(new Category(category)));

        adapter.clear();
        adapter.addAll(budgetCategories);
        adapter.notifyDataSetChanged();
    }
}
