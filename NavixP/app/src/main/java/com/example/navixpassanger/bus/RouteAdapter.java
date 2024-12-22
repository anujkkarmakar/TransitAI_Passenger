package com.example.navixpassanger.bus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.navixpassanger.R;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {
    private List<BusRoute> routes;
    private double directDistance;

    public RouteAdapter(List<BusRoute> routes, double directDistance) {
        this.routes = routes;
        this.directDistance = directDistance;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BusRoute route = routes.get(position);
        holder.routeNumber.setText("Route: " + route.getRoute_no());
        holder.numberOfStops.setText("Number of stops: " + route.getStops().size());
        holder.totalDistance.setText(String.format("Total distance: %.2f km", route.getDistance()));
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeNumber, numberOfStops, totalDistance;

        ViewHolder(View view) {
            super(view);
            routeNumber = view.findViewById(R.id.routeNumber);
            numberOfStops = view.findViewById(R.id.numberOfStops);
            totalDistance = view.findViewById(R.id.totalDistance);
        }
    }
}