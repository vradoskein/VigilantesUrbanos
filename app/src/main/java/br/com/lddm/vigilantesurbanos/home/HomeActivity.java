package br.com.lddm.vigilantesurbanos.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Objects;

import br.com.lddm.vigilantesurbanos.IncidentActivity;
import br.com.lddm.vigilantesurbanos.R;
import br.com.lddm.vigilantesurbanos.base.BaseActivity;
import br.com.lddm.vigilantesurbanos.model.User;

public class HomeActivity extends BaseActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private final int PICK_IMAGE_REQUEST = 71;

    private DashFragment dashFragment;

    private IncidentsFragment incidentsFragment;

    private ProfileFragment profileFragment;

    // OAP data
    private User oapUser;

    // User auth data
    FirebaseUser user;

    FirebaseStorage storage;
    StorageReference storageReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        incidentsFragment = IncidentsFragment.newInstance();
        dashFragment = DashFragment.newInstance();
        profileFragment = ProfileFragment.newInstance();


        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);
        navigationView.setSelectedItemId(R.id.navigation_dash);


        user = FirebaseAuth.getInstance().getCurrentUser();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        fetchOapUser();

        setFragmentDash();
    }

    private void fetchOapUser() {
        db.collection("user")
                .whereEqualTo("uuid", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       try {
                           incidentsFragment.fetchIncidents();
                           dashFragment.fetchIncidentsCount();

                           oapUser = Objects.requireNonNull(task.getResult()).toObjects(User.class).get(0);
                       } catch (Exception e) {
                           Log.e("fetchOapUser: ", e.getMessage());
                       }
                   }
                })
                .addOnFailureListener(e -> {

                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        downloadProfilePicture();
    }

    @Override
    public void onBackPressed() {
        setFragmentDash();
    }

    private void setFragmentIncidents() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, incidentsFragment)
                .commit();
    }
    private void setFragmentDash() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, dashFragment)
                .commit();
    }
    private void setFragmentProfile() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, profileFragment)
                .commit();
    }
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();

        if (id == R.id.navigation_incidents) {
            setFragmentIncidents();
            return true;
        } else if (id == R.id.navigation_dash) {
            setFragmentDash();
            return true;
        } else if (id == R.id.navigation_profile) {
            setFragmentProfile();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profileFragment.setProfilePicture(bitmap);

                uploadProfilePicture(filePath);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void changeProfilePicture(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void downloadProfilePicture() {
        storageReference.child("images/"+ user.getUid()+"/profile-picture.png")
                .getDownloadUrl()
                .addOnSuccessListener(uri -> profileFragment.setProfilePicture(uri))
                .addOnFailureListener(e -> {

                });
    }

    private void uploadProfilePicture(Uri filePath) {
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+ user.getUid()+"/profile-picture.png");
            ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(HomeActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(HomeActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    });
        }
    }

    protected User getOapUser() {
        return oapUser;
    }

    public void startIncidentActivity(View view) {
        startActivity(new Intent(getApplicationContext(), IncidentActivity.class));
    }

    public void logout(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sair do aplicativo");
        builder.setMessage("Tem certeza que deseja sair?");

        builder.setPositiveButton("Sair", (arg0, arg1) -> this.signOut());

        builder.setNegativeButton("Cancelar", (arg0, arg1) -> {

        });

        builder
            .create()
            .show();
    }
}
