package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.dao.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Notification> notificationsReceived;
    private User user;

    public NotificationAdapter(Context context, ArrayList<Notification> notificationsReceived) {
        this.context = context;
        this.notificationsReceived = notificationsReceived;

    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notifications_rv_row, parent, false);
        return new ViewHolder(view);
        //inflating the layout (ie giving a look to our our items/rows)
        //return null;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        //assigning values to the views we created in the recycler view layout file based on the
        //position of the recycler view

        Notification notification = notificationsReceived.get(position);
        Log.d("adapter", notification.getMessage());
        holder.header.setText("Budget Alert");
        holder.message.setText(notification.getMessage());
        //Log.d("adapter", notification.getDate());
        Long date = Long.valueOf(notification.getDate());

        holder.date.setText(notification.formatDate(date));
        //holder.imageView.setImageDrawable(R.drawable.budget_limit_nearing_icon);

        int resourceId = holder
                .itemView
                .getContext()
                .getResources()
                .getIdentifier(
                        convertToLogoName(notification),
                        "drawable",
                        holder.itemView.getContext().getPackageName());
        holder.imageView.setImageResource(resourceId);



        //holder.imageView.setImageDrawable(R.drawable.budget_limit_nearing_icon);


    }

    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        return dateFormat.format(date);
    }

    public String convertToLogoName(Notification notification) {
        switch (notification.getType()) {
            case "Entertainment":
                return "entertainment_icon";
            case "Food":
                return "food_icon";
            case "Travel":
                return "travel_icon";
            case "School":
                return "school_icon";
            case "Utilities":
                return "utilities_icon";
            default:
                throw new IllegalArgumentException("Unknown category: " + notification.getType());
        }
    }

    @Override
    public int getItemCount() {
        // how many notifications does a user have
        return notificationsReceived.size();
    }

    public void setNotificationsReceived(ArrayList<Notification> notifications) {
        notificationsReceived.clear();
        notificationsReceived.addAll(notifications);
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    public void addNotification(Notification notification) {
        notificationsReceived.add(notification);
        notifyDataSetChanged(); // Notify the adapter that data has changed
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //grabbing the image view and text views and assigning them to their values
        ImageView imageView;
        TextView header, date, message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.categoryImageIcon);
            //write if condition based on image type
            header = itemView.findViewById(R.id.header_rv_text);
            message = itemView.findViewById(R.id.notification_message_rv_text);
            date = itemView.findViewById(R.id.date_rv_text);
        }
    }
}
