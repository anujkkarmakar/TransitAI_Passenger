package com.example.navixpassanger.ticket;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.navixpassanger.R;

import java.util.List;

public class CancelTicketAdapter extends RecyclerView.Adapter<CancelTicketAdapter.ViewHolder> {
    private List<Ticket> tickets;
    private OnCancelClickListener listener;

    public interface OnCancelClickListener {
        void onCancelClick(Ticket ticket);
    }

    public CancelTicketAdapter(List<Ticket> tickets, OnCancelClickListener listener) {
        this.tickets = tickets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cancel_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(tickets.get(position));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView pnrText, routeText, journeyTypeText;
        private Button cancelButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            pnrText = itemView.findViewById(R.id.textPNR);
            routeText = itemView.findViewById(R.id.textRoute);
            journeyTypeText = itemView.findViewById(R.id.textJourneyType);
            cancelButton = itemView.findViewById(R.id.buttonCancel);
        }

        void bind(Ticket ticket) {
            pnrText.setText("PNR: " + ticket.getPnr());
            routeText.setText(ticket.getFromStop() + " → " + ticket.getToStop());
            journeyTypeText.setText(ticket.getJourneyType());

            cancelButton.setOnClickListener(v -> listener.onCancelClick(ticket));
        }
    }
}