package br.com.lddm.vigilantesurbanos.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.lddm.vigilantesurbanos.IncidentActivity;
import br.com.lddm.vigilantesurbanos.R;
import br.com.lddm.vigilantesurbanos.model.Incident;

public class IncidentsAdapter extends RecyclerView.Adapter<IncidentsAdapter.ViewHolder> {

    private List<Incident> data;
    private Activity activity;

    FirebaseStorage storage;
    StorageReference storageReference;

    public IncidentsAdapter(List<Incident> data, Activity activity) {
        this.data = data;
        this.activity = activity;

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_indicent_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Incident incident = data.get(i);
        viewHolder.description.setText(incident.getDescription());
        viewHolder.type.setText("Tipo: " + incident.getType() );
        viewHolder.date.setText(incident.getDate());


        storageReference.child("images/incident/"+ incident.getUuid()+"/picture.png")
                .getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get()
                        .load(uri)
                        .resize(120, 140)
                        .centerCrop()
                        .into(viewHolder.picture))
                .addOnFailureListener(e -> {

                });

        viewHolder.container.setOnClickListener(v -> {
            Intent intent = new Intent(activity.getBaseContext(), IncidentActivity.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean("readOnly", true);
            intent.putExtras(bundle);
            intent.putExtra("description", incident.getDescription());
            intent.putExtra("type", incident.getType());
            intent.putExtra("date", incident.getDate());
            intent.putExtra("uuid", incident.getUuid());
            intent.putExtra("address", incident.getAddress());
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void changeData(List<Incident> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout container;
        TextView description;
        TextView type;
        TextView date;
        ImageView picture;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            container = itemView.findViewById(R.id.container);
            description = itemView.findViewById(R.id.incident_description);
            type = itemView.findViewById(R.id.incident_type);
            date = itemView.findViewById(R.id.incident_date);
        }
    }
}
