package com.example.myapplication.wallet_watch_util;

import com.example.myapplication.dao.Expense;

public final class WalletWatchStorageUtil {

    /**
     * Get the image filename from an Expense object.
     * @param expense Expense
     * @return image filename
     */
    public static String expenseImageFileName(Expense expense) {
        return String.format("expense-%s.jpg", expense.getId());
    }

    /**
     * Get the image filename from an Expense object.
     * @param id Expense id
     * @return image filename
     */
    public static String expenseImageFileName(String id) {
        return String.format("expense-%s.jpg", id);
    }

    /**
     * Get the Expense id from an Expense image filename
     * @param fileName Expense filename
     * @return Expense id
     */
    public static String idFromExpenseImageFileName(String fileName) {
        return fileName.substring(0,8);
    }
}
