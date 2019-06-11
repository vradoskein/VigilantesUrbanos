package br.com.lddm.vigilantesurbanos.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.lddm.vigilantesurbanos.R;
import br.com.lddm.vigilantesurbanos.model.Incident;

public class IncidentsFragment extends Fragment {

    RecyclerView recyclerView;
    IncidentsAdapter incidentsAdapter;

    List<Incident> incidents = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public IncidentsFragment() {
        // Required empty public constructor
    }

    public static IncidentsFragment newInstance() {
        return new IncidentsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incidents, container, false);

        recyclerView = view.findViewById(R.id.incidents_list);
        LinearLayoutManager llm = new LinearLayoutManager(container.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);


        recyclerView.setLayoutManager(llm);


        incidentsAdapter = new IncidentsAdapter(incidents, getActivity());

        recyclerView.setAdapter(incidentsAdapter);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fetchIncidents();
    }

    protected void fetchIncidents() {
        if (getActivity() != null
                && ((HomeActivity)getActivity()).getOapUser() != null
                && !(((HomeActivity)getActivity()).getOapUser() ).isOap() ) {
            db.collection("incidents")
                    .whereEqualTo("user", user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            try {
                                List<Incident> incidents = new ArrayList<>();
                                for(QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    document.getData();
                                    Incident incident = document.toObject(Incident.class);
                                    incident.setUuid(document.getId());
                                    incidents.add(incident);
                                }
                                incidentsAdapter.changeData(incidents);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else {

            db.collection("incidents")
                    .whereEqualTo("user", user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            try {
                                List<Incident> incidents = new ArrayList<>();
                                for(QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    document.getData();
                                    Incident incident = document.toObject(Incident.class);
                                    incident.setUuid(document.getId());
                                    incidents.add(incident);
                                }
                                incidentsAdapter.changeData(incidents);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

    }
}
