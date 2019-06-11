package br.com.lddm.vigilantesurbanos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import br.com.lddm.vigilantesurbanos.base.BaseActivity;
import br.com.lddm.vigilantesurbanos.model.Incident;
import br.com.lddm.vigilantesurbanos.model.api.LocationAPI;
import br.com.lddm.vigilantesurbanos.model.api.LocationService;
import br.com.lddm.vigilantesurbanos.model.api.RetrofitInstance;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidentActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, LocationListener {

    private static final int DATE_DIALOG_ID = 10;
    private static final int CAMERA_INTENT = 11;
    private ImageView picture;
    private TextView date;
    private TextView description;
    private TextView type;
    private TextView address;
    private Button dateButton;
    Date dateIncident;

    Bitmap bitmap;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private LocationManager locationManager;
    private Location location;
    private LocationAPI locationAPI;


    FirebaseStorage storage;
    StorageReference storageReference;

    public IncidentActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident);


        dateIncident = new Date();
        date = findViewById(R.id.date);
        dateButton = findViewById(R.id.buttonDate);
        picture = findViewById(R.id.picture);
        description = findViewById(R.id.incident_description);
        type = findViewById(R.id.incident_type);
        address = findViewById(R.id.address);
        LinearLayout llSave = findViewById(R.id.ll_save);
        Button saveButton = findViewById(R.id.save);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkLocationPermission();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Boolean onlyRead = getIntent().getBooleanExtra("readOnly", false);

        if (onlyRead) {
            date.setEnabled(false);
            dateButton.setEnabled(false);
            picture.setEnabled(false);
            description.setEnabled(false);
            type.setEnabled(false);
            llSave.setEnabled(false);
            saveButton.setEnabled(false);

            String sDesc = getIntent().getStringExtra("description");
            String sType = getIntent().getStringExtra("type");
            String sDate = getIntent().getStringExtra("date");
            String sUuid = getIntent().getStringExtra("uuid");
            String sAddres = getIntent().getStringExtra("address");

            description.setText(sDesc);
            type.setText(sType);
            date.setText(sDate);
            address.setText(sAddres);


            storageReference.child("images/incident/"+ sUuid +"/picture.png")
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        if (uri != null) {
                            try {
                                Picasso.get()
                                        .load(uri)
                                        .resize(240, 150)
                                        .centerCrop()
                                        .into(picture);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {

                    });
        }


    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onStart() {
        super.onStart();

        dateButton.setOnClickListener(v -> showDialog( DATE_DIALOG_ID ));

        Date dateTime = new Date();

        date.setText(String.format("%d/%d/%d", dateTime.getDay(), dateTime.getMonth(), dateTime.getYear()));

        picture.setOnClickListener(view -> {
            Intent pictureIntent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
            );
            if(pictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(pictureIntent, CAMERA_INTENT);
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            String provider = locationManager.getBestProvider(new Criteria(), false);
            locationManager.requestLocationUpdates(provider, 30000, 1, this);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_INTENT && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                bitmap = (Bitmap) data.getExtras().get("data");
                picture.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);

        switch (id) {
            case DATE_DIALOG_ID:
                Calendar calendario = Calendar.getInstance();

                int ano = calendario.get(Calendar.YEAR);
                int mes = calendario.get(Calendar.MONTH);
                int dia = calendario.get(Calendar.DAY_OF_MONTH);


                return new DatePickerDialog(this, this, ano, mes, dia);
            default:
                break;
        }
        return null;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date.setText(String.format("%d/%d/%d", dayOfMonth, month, year));
    }

    public void close(View view) {
        finish();
    }

    public void save(View view) {
        String description = this.description.getText().toString();
        String type = this.type.getText().toString();
        String date = this.date.getText().toString();


        boolean withLocation = this.location != null;
        if (!withLocation) {
            withLocation = this.checkLocationPermission();
        }


        if (!withLocation) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_location_permission)
                    .setMessage(R.string.withoutLocation)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {})
                    .create()
                    .show();
            return;
        }

        Incident incident = new Incident(description, type, date);
        incident.setUser(user.getUid());


        incident.setLatitude(this.location != null ? this.location.getLatitude() : 0d);
        incident.setLongitude(this.location != null ? this.location.getLongitude() : 0d);
        incident.setAddress(this.address.getText().toString());

        db.collection("incidents")
                .add(incident)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(),
                            "Incidente criado com sucesso", Toast.LENGTH_LONG).show();
                    Log.e("save: ", documentReference.getId());


                    try {
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bao); // bmp is bitmap from user image file
                        bitmap.recycle();
                        byte[] byteArray = bao.toByteArray();

                        StorageReference ref = storageReference.child("images/incident/"+ documentReference.getId()+"/picture.png");
                        ref.putBytes(byteArray)
                                .addOnSuccessListener(taskSnapshot -> Toast.makeText(IncidentActivity.this, "Uploaded", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(IncidentActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("save: ", e.getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(),
                            "Erro ao criar incidente", Toast.LENGTH_LONG).show();
                    Log.e("save: ", e.getMessage());
                });

        finish();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) ->
                                ActivityCompat.requestPermissions(IncidentActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            MY_PERMISSIONS_REQUEST_LOCATION))
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        String provider = locationManager.getBestProvider(new Criteria(), false);

                        locationManager.requestLocationUpdates(provider, 30000, 1, this);
                    }

                } else {
                    location = null;
                }
            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;

        LocationService locationService = RetrofitInstance.getInstance()
                .create(LocationService.class);

        Call<LocationAPI> locationAPICall =
                locationService.getLocation("2b3f91883c6d4cedb39ff1ecd1c09660", this.location.getLatitude() + ", " + this.location.getLongitude());

        locationAPICall.enqueue(new Callback<LocationAPI>() {
            @Override
            public void onResponse(@NonNull Call<LocationAPI> call, @NonNull Response<LocationAPI> response) {
                Log.d("onResponse: ", Objects.requireNonNull(response.body()).getResults().get(0).getComponents().toString());
                locationAPI = response.body();

                address.setText(locationAPI.getResults().get(0).getComponents().toString());
            }

            @Override
            public void onFailure(@NonNull Call<LocationAPI> call, @NonNull Throwable t) {
                t.printStackTrace();
                Log.e( "onFailure: ", t.getMessage());
            }
        });

        Log.d("onLocationChanged: ", location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
