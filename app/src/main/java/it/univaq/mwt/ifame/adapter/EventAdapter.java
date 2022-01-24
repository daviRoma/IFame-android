package it.univaq.mwt.ifame.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.activity.EventDetailActivity;
import it.univaq.mwt.ifame.model.relation.EventRelation;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.ImageManager;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    ArrayList<EventRelation> eventsList;
    Context context;

    public EventAdapter(ArrayList<EventRelation> eventsList, Context context) {
        this.eventsList = eventsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_event, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.title.setText(eventsList.get(position).event.getTitle());
        holder.hour.setText(eventsList.get(position).event.getHour());
        holder.place.setText(eventsList.get(position).restaurant.getName());
        holder.totalParticipants.setText(String.valueOf(eventsList.get(position).event.getMaxParticipants()));
        holder.currentParticipants.setText(String.valueOf(eventsList.get(position).participants.size()));

        if (eventsList.get(position).event.getImage() != null && !eventsList.get(position).event.getImage().isEmpty())
            holder.image.setImageBitmap(ImageManager.getInstance(context).loadImage("events", eventsList.get(position).event.getImage()));

        if (eventsList.get(position).event.getIdAuthor().equals(CurrentUser.user.id))
            holder.createdTag.setVisibility(View.VISIBLE);
        else holder.createdTag.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView title;
        public TextView place;
        public TextView hour;
        public TextView totalParticipants;
        public TextView currentParticipants;
        public ImageView image;
        public CardView createdTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.eventTitle);
            place = itemView.findViewById(R.id.eventPlace);
            hour = itemView.findViewById(R.id.eventHour);
            totalParticipants = itemView.findViewById(R.id.totalPraticipants);
            currentParticipants = itemView.findViewById(R.id.currentParticipants);
            image = itemView.findViewById(R.id.eventImage);
            createdTag = itemView.findViewById(R.id.createdTag);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), EventDetailActivity.class);
            intent.putExtra("event", eventsList.get(getAdapterPosition()));
            view.getContext().startActivity(intent);
        }
    }

}
