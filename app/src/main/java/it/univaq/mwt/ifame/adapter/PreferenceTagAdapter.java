package it.univaq.mwt.ifame.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.univaq.mwt.ifame.R;

public class PreferenceTagAdapter extends RecyclerView.Adapter<PreferenceTagAdapter.ViewHolder> {

    private ArrayList<PreferenceTag> preferenceTags = new ArrayList<>();

    private String locale = Locale.getDefault().getCountry();

    public PreferenceTagAdapter(ArrayList<String> categories, List<String> userCategory) {
        setupAdapter(categories, userCategory);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_preference_tag, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.checkFlag.setVisibility(preferenceTags.get(position).isChecked() ? View.VISIBLE : View.GONE);
        if (locale.equals("IT")) holder.tagName.setText(preferenceTags.get(position).getCategory());
        else holder.tagName.setText(preferenceTags.get(position).getCategory());
        holder.adapterContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.tagName.setText(preferenceTags.get(position).getCategory());
                preferenceTags.get(position).setChecked(!preferenceTags.get(position).isChecked());
                holder.checkFlag.setVisibility(preferenceTags.get(position).isChecked() ? View.VISIBLE : View.INVISIBLE);
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.preferenceTags.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tagName;
        public ImageView checkFlag;
        public ConstraintLayout adapterContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.tagName);
            checkFlag = itemView.findViewById(R.id.flag);
            adapterContainer = itemView.findViewById(R.id.adapterContainer);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            PreferenceTag pref = preferenceTags.get(getAdapterPosition());
            pref.setChecked(!pref.isChecked());
        }
    }

    public ArrayList<String> getSelected() {
        ArrayList<String> selected = new ArrayList<>();
        for (int i = 0; i < preferenceTags.size(); i++) {
            if (preferenceTags.get(i).isChecked()) {
                selected.add(preferenceTags.get(i).getCategory());
            }
        }
        return selected;
    }

    public void setupAdapter(ArrayList<String> categories, List<String> userCategory) {
        preferenceTags.clear();
        for (String category : categories) {
            PreferenceTag preferenceTag = new PreferenceTag(category);
            for (String c : userCategory) {
                if (c.equals(category)) {
                    preferenceTag.setChecked(true);
                }
            }
            preferenceTags.add(preferenceTag);
        }
    }

}
