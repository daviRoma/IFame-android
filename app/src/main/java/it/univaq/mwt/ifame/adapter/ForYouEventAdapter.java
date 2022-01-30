package it.univaq.mwt.ifame.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.activity.EventDetailActivity;
import it.univaq.mwt.ifame.fragment.home.HomeFragment;
import it.univaq.mwt.ifame.model.relation.EventRelation;
import it.univaq.mwt.ifame.utility.ImageManager;

public class ForYouEventAdapter extends RecyclerView.Adapter<ForYouEventAdapter.ViewHolder> {
    private static final String TAG = ForYouEventAdapter.class.getSimpleName();

    ArrayList<EventRelation> events;
    Context context;

    public ForYouEventAdapter(ArrayList<EventRelation> events, Context context) {
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_for_you_event, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.title.setText(events.get(position).event.getTitle());
        holder.place.setText(events.get(position).restaurant.getName());
        holder.day.setText(events.get(position).event.getDay());
        holder.hour.setText(events.get(position).event.getHour());

        if (events.get(position).event.getImage() != null) {
            holder.image.setImageBitmap(ImageManager.getInstance(context).loadImage("events", events.get(position).event.getImage()));
        }

    }

    @Override
    public int getItemCount() {
        return events.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;
        TextView place;
        TextView day;
        TextView hour;

        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.forYouTitle);
            place = itemView.findViewById(R.id.forYouPlace);
            day = itemView.findViewById(R.id.forYouDay);
            hour = itemView.findViewById(R.id.forYouHour);
            image = itemView.findViewById(R.id.forYouImage);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {

            Intent intent = new Intent(view.getContext(), EventDetailActivity.class);
            intent.putExtra("event", events.get(getAdapterPosition()));
            view.getContext().startActivity(intent);
        }
    }


}
