package br.com.lddm.vigilantesurbanos.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import br.com.lddm.vigilantesurbanos.IncidentActivity;
import br.com.lddm.vigilantesurbanos.R;

public class DashFragment extends Fragment {

    TextView description;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public DashFragment() {
        // Required empty public constructor
    }

    public static DashFragment newInstance() {
        DashFragment fragment = new DashFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dash, container, false);

        description = view.findViewById(R.id.description);

        description.setText(String.format(getResources().getString(R.string.incidents_number), 0));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fetchIncidentsCount();

    }

    protected void fetchIncidentsCount() {

        if (getActivity() != null
                && ((HomeActivity)getActivity()).getOapUser() != null
                && !(((HomeActivity)getActivity()).getOapUser() ).isOap() ) {
            db.collection("incidents")
                    .whereEqualTo("user", user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            try {
                                description.setText(String.format(getResources().getString(R.string.incidents_number), Objects.requireNonNull(task.getResult()).size()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else {
            db.collection("incidents")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            try {
                                description.setText(String.format(getResources().getString(R.string.incidents_number), Objects.requireNonNull(task.getResult()).size()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

    }

    public void startIncidentActivity(View view) {
        view.getContext().startActivity(new Intent(getContext(), IncidentActivity.class));
    }
}
