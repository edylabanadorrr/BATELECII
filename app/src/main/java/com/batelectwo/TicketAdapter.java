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

public class TicketAdapter extends ArrayAdapter<TicketListItem> {

    public TicketAdapter(Context context, List<TicketListItem> ticketList) {
        super(context, 0, ticketList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate the custom layout
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        // Get the current ticket item
        TicketListItem ticketItem = getItem(position);

        // Bind data to the layout views
        TextView textLocation = convertView.findViewById(R.id.textLocation);
        TextView textIssue = convertView.findViewById(R.id.textIssue);
        TextView textDetails = convertView.findViewById(R.id.textDetails);
        TextView textEmail = convertView.findViewById(R.id.textEmail);
        TextView textUid = convertView.findViewById(R.id.textUid);

        if (ticketItem != null) {
            textLocation.setText("Location: " + ticketItem.getLocation());
            textIssue.setText("Issue: " + ticketItem.getIssue());
            textDetails.setText("Details: " + ticketItem.getDetails());
            textEmail.setText("Email: " + ticketItem.getEmail());
            textUid.setText("UID: " + ticketItem.getUid());
        }

        return convertView;
    }
}

