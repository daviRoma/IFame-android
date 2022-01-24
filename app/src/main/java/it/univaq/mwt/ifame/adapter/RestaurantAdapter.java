package it.univaq.mwt.ifame.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Locale;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.model.Restaurant;
import it.univaq.mwt.ifame.utility.ImageManager;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private List<Restaurant> restaurants;
    private OnRestaurantClickListener clickListener;

    private DialogFragment dialogFragment;

    private Context context;

    private String locale = Locale.getDefault().getCountry();

    public RestaurantAdapter(DialogFragment dialogFragment, List<Restaurant> restaurants, Context context) {
        this.restaurants = restaurants;
        this.dialogFragment = dialogFragment;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.restaurantName.setText(restaurants.get(position).getName());
        holder.restaurantAddress.setText(restaurants.get(position).getAddress());

        holder.restaurantFlexBox.removeAllViews();

        for (String category : restaurants.get(position).getCategories()) {

            final Chip chip = new Chip(holder.restaurantFlexBox.getContext());
            if (locale.equals("IT")) chip.setText(category);
            else chip.setText(category);

            holder.restaurantFlexBox.addView(chip);
        }

        String image = restaurants.get(position).getImage();
        if (image != null) {
            holder.restaurantImage.setImageBitmap(ImageManager.getInstance(context).loadImage("restaurants", image));
        }

    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    public void setClickListener(OnRestaurantClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView restaurantName;
        public TextView restaurantAddress;
        public ImageView restaurantImage;
        public FlexboxLayout restaurantFlexBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            restaurantName = itemView.findViewById(R.id.restaurantName);
            restaurantAddress = itemView.findViewById(R.id.restaurantAddress);
            restaurantImage = itemView.findViewById(R.id.restaurantImage);
            restaurantFlexBox = itemView.findViewById(R.id.restaurantFlexBox);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (clickListener != null) clickListener.onClick(restaurants.get(getAdapterPosition()));
            if (dialogFragment != null) dialogFragment.dismiss();

        }
    }

    public interface OnRestaurantClickListener {

        void onClick(Restaurant restaurant);

    }

}
