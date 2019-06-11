package br.com.lddm.vigilantesurbanos.home;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.squareup.picasso.Picasso;

import br.com.lddm.vigilantesurbanos.R;
import br.com.lddm.vigilantesurbanos.model.User;


public class ProfileFragment extends Fragment {

    FirebaseUser user;
    ImageView profilePicture;
    TextView username;
    TextView email;
    TextView tvOap;
    Button applyButton;
    Uri pictureUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        username = view.findViewById(R.id.username);
        email = view.findViewById(R.id.email);
        profilePicture = view.findViewById(R.id.profile_picture);
        applyButton = view.findViewById(R.id.apply);
        tvOap = view.findViewById(R.id.tvOap);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        username.setText(user.getDisplayName());
        email.setText(user.getEmail());

        if (getActivity() != null
                && ((HomeActivity)getActivity()).getOapUser() != null) {

            tvOap.setVisibility(((HomeActivity)getActivity()).getOapUser().isOap() ? View.VISIBLE : View.GONE);
        }

        if (pictureUri != null) {
            Picasso.get()
                    .load(pictureUri)
                    .resize(100, 100)
                    .centerCrop()
                    .into(profilePicture);
        }

        applyButton.setOnClickListener(v -> updateProfile());
    }

    public void updateProfile() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(username.getText().toString())
                .setPhotoUri(pictureUri)
                .build();

        user.updateProfile(request);
    }

    public void setProfilePicture(Bitmap bitmap) {
        profilePicture.setImageBitmap(bitmap);
    }

    public void setProfilePicture(Uri uri) {
        this.pictureUri = uri;
        if (profilePicture != null) {
            Picasso.get()
                    .load(uri)
                    .resize(100, 100)
                    .centerCrop()
                    .into(profilePicture);
        }
    }
}
