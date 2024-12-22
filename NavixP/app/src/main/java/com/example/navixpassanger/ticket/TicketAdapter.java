package com.example.navixpassanger.ticket;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navixpassanger.R;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<Ticket> tickets;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
    }

    public TicketAdapter(List<Ticket> tickets, OnTicketClickListener listener) {
        this.tickets = tickets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        holder.bind(tickets.get(position));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class TicketViewHolder extends RecyclerView.ViewHolder {
        private TextView pnrText, routeText, journeyTypeText, statusText;
        private CardView cardView;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            pnrText = itemView.findViewById(R.id.textPNR);
            routeText = itemView.findViewById(R.id.textRoute);
            journeyTypeText = itemView.findViewById(R.id.textJourneyType);
            statusText = itemView.findViewById(R.id.textStatus);
            cardView = itemView.findViewById(R.id.cardView);
        }

        void bind(Ticket ticket) {
            pnrText.setText("PNR: " + ticket.getPnr());
            routeText.setText(ticket.getFromStop() + " → " + ticket.getToStop());
            journeyTypeText.setText(ticket.getJourneyType());

            boolean isActive = (System.currentTimeMillis() - ticket.getTimestamp())
                    <= (4 * 60 * 60 * 1000);

            // Handle status text and color
            if (ticket.getStatus() != null && ticket.getStatus().equals("CANCELLED")) {
                statusText.setText("Cancelled");
                statusText.setTextColor(itemView.getContext().getColor(R.color.red));
                cardView.setAlpha(0.7f); // Make card appear disabled
                cardView.setClickable(false); // Disable clicking
            } else if (!isActive) {
                statusText.setText("Expired");
                statusText.setTextColor(itemView.getContext().getColor(R.color.red));
                cardView.setAlpha(0.7f); // Make card appear disabled
                cardView.setClickable(false); // Disable clicking
            } else {
                statusText.setText("Active");
                statusText.setTextColor(itemView.getContext().getColor(R.color.green));
                cardView.setAlpha(1.0f); // Make card appear enabled
                cardView.setClickable(true); // Enable clicking
                cardView.setOnClickListener(v -> listener.onTicketClick(ticket));
            }
        }
    }
}