package com.batelectwo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class FeedbackAdapter extends ArrayAdapter<SentFeedbacks> {

    public FeedbackAdapter(Context context, List<SentFeedbacks> feedbackList) {
        super(context, 0, feedbackList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate the custom layout
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        // Get the current ticket item
        SentFeedbacks feedbackItem = getItem(position);

        // Bind data to the layout views
        TextView textFeedback = convertView.findViewById(R.id.textFeedback);
        TextView textEmail = convertView.findViewById(R.id.textEmail);
        TextView textUid = convertView.findViewById(R.id.textUid);

        if (feedbackItem != null) {
            textFeedback.setText("Feedback: " + feedbackItem.getFeedback());
            textEmail.setText("Email: " + feedbackItem.getEmail());
            textUid.setText("UID: " + feedbackItem.getUid());
        }

        return convertView;
    }
}


