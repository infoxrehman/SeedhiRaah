package com.raah.seedhiraah;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.raah.seedhiraah.databinding.ActivityEditMosqueBinding;

import java.util.HashMap;
import java.util.UUID;

public class EditMosqueActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    boolean isLadiesFacilityAvailable;
    private ActivityEditMosqueBinding binding;
    private ResultReceiver resultReceiver;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Uri imageUri = null;
    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                imageUri = data.getData();
                binding.addImageSrc.setImageURI(imageUri);
            } else {
                Toast.makeText(EditMosqueActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    });
    private ProgressDialog progressDialog;
    private String mosqueId, mosqueName, cityName, mosqueDescription, capacity, ownerName, loc, mosqueImage, imageId, email, password;
    private double lat, lon;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditMosqueBinding.inflate(getLayoutInflater());
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.background));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color='#FFFFFF'>" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        constants();

        mosqueId = getIntent().getStringExtra("mosqueId");

        resultReceiver = new AddressResultReceiver(new Handler());

        binding.addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(EditMosqueActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditMosqueActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    getCurrentLocation();
                }

            }
        });
        db.collection("Mosques").document(mosqueId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                mosqueName = documentSnapshot.getString("mosqueName");
                cityName = documentSnapshot.getString("cityName");
                mosqueDescription = documentSnapshot.getString("mosqueDescription");
                capacity = documentSnapshot.getString("mosqueCapacity");
                ownerName = documentSnapshot.getString("ownerName");
                loc = documentSnapshot.getString("location");
                mosqueImage = documentSnapshot.getString("mosqueImage");
                imageId = documentSnapshot.getString("imageId");

                if (TextUtils.isEmpty(imageId)) {
                    imageId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                } else {
                    imageId = documentSnapshot.getString("imageId");
                }

                isLadiesFacilityAvailable = documentSnapshot.getBoolean("isLadiesFacilityAvailable");
                lat = documentSnapshot.getDouble("latitude");
                lon = documentSnapshot.getDouble("longitude");

                binding.mosqueNameEt.setText(mosqueName);
                binding.mosqueDescriptionEt.setText(mosqueDescription);
                binding.cityNameEt.setText(cityName);
                binding.capacityEt.setText(capacity);
                binding.ownerNameEt.setText(ownerName);
                binding.locationEt.setText(loc);
                binding.isWomenAvailable.setChecked(isLadiesFacilityAvailable);

                Glide.with(EditMosqueActivity.this).load(mosqueImage).placeholder(R.drawable.image_src).into(binding.addImageSrc);
            }
        });

        db.collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                email = documentSnapshot.getString("email");
                if (email != null) {
                    binding.emailTv.setText(email);
                } else {
                    Toast.makeText(EditMosqueActivity.this, "Email is null", Toast.LENGTH_SHORT).show();
                }
                password = documentSnapshot.getString("password");
                binding.emailTv.setText(email);
            }
        });

        binding.addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                galleryActivityResultLauncher.launch(galleryIntent);
            }
        });

        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        binding.isWomenAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isLadiesFacilityAvailable = isChecked;
            }
        });

    }

    private void validateData() {

        String enterpassword = binding.cPasswordEt.getText().toString().trim();

        mosqueName = binding.mosqueNameEt.getText().toString().trim();
        ownerName = binding.ownerNameEt.getText().toString().trim();
        cityName = binding.cityNameEt.getText().toString().trim();
        capacity = binding.capacityEt.getText().toString().trim();

        if (email != null && firebaseUser != null && firebaseUser.getEmail() != null && enterpassword != null && email.equals(firebaseUser.getEmail()) && enterpassword.equals(password)) {
            if (TextUtils.isEmpty(mosqueName)) {
                Toast.makeText(this, "Mosque name required!", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(cityName)) {
                Toast.makeText(this, "City name required!", Toast.LENGTH_SHORT).show();
            } else {
                if (imageUri == null) {
                    updateData("");
                } else {
                    uploadImage();
                }
            }
        } else {
            Toast.makeText(this, "Please enter a correct password!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(EditMosqueActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(EditMosqueActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EditMosqueActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationServices.getFusedLocationProviderClient(EditMosqueActivity.this).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(EditMosqueActivity.this).removeLocationUpdates(this);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                    String test = String.format("Latitude: %s\nLongitude: %s", latitude, longitude);
                    lon = longitude;
                    lat = latitude;
                    Location location = new Location("providerNA");
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    fetchAddressFromLatLong(location);
                }
            }
        }, Looper.getMainLooper());
    }

    private void fetchAddressFromLatLong(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    private void updateData(String imageUrl) {
        progressDialog.setMessage("Updating mosque data");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("mosqueName", mosqueName);
        hashMap.put("ownerName", ownerName);
        hashMap.put("cityName", cityName);
        hashMap.put("mosqueCapacity", capacity);
        hashMap.put("isLadiesFacilityAvailable", isLadiesFacilityAvailable);
        hashMap.put("location", loc);
        hashMap.put("longitude", lon);
        hashMap.put("latitude", lat);
        hashMap.put("imageId", imageId);

        if (imageUri != null) {
            hashMap.put("mosqueImage", imageUrl);
        }

        db.collection("Mosques").document(mosqueId).update(hashMap).addOnSuccessListener(unused -> {
            progressDialog.dismiss();
            Toast.makeText(EditMosqueActivity.this, "Data updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(EditMosqueActivity.this, "Failed to update db due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadImage() {

        progressDialog.setMessage("Uploading mosque image");
        progressDialog.show();

        Toast.makeText(this, imageId, Toast.LENGTH_SHORT).show();
        String filePathAndName = "MosquesImages/" + imageId;

        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            String uploadedImageUrl = "" + uriTask.getResult();

            updateData(uploadedImageUrl);
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(EditMosqueActivity.this, "Failed to upload mosque image due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void constants() {
        binding.mosqueNameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.mosqueNameEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.mosqueNameEt.setTextColor(getResources().getColor(R.color.base));
                    binding.mosqueNameEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.mosqueNameEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.mosqueNameEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.mosqueNameEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.mosqueNameEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.mosqueNameEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });
        binding.ownerNameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.ownerNameEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.ownerNameEt.setTextColor(getResources().getColor(R.color.base));
                    binding.ownerNameEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.ownerNameEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.ownerNameEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.ownerNameEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.ownerNameEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.ownerNameEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });
        binding.cityNameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.cityNameEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.cityNameEt.setTextColor(getResources().getColor(R.color.base));
                    binding.cityNameEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.cityNameEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.cityNameEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.cityNameEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.cityNameEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.cityNameEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });
        binding.capacityEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.capacityEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.capacityEt.setTextColor(getResources().getColor(R.color.base));
                    binding.capacityEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.capacityEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.capacityEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.capacityEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.capacityEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.capacityEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });
        binding.mosqueDescriptionEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.mosqueDescriptionEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.mosqueDescriptionEt.setTextColor(getResources().getColor(R.color.base));
                    binding.mosqueDescriptionEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.mosqueDescriptionEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.mosqueDescriptionEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.mosqueDescriptionEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.mosqueDescriptionEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.mosqueDescriptionEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });
        binding.cPasswordEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.cPasswordEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.cPasswordEt.setTextColor(getResources().getColor(R.color.base));
                    binding.cPasswordEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.cPasswordEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.cPasswordEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.cPasswordEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.cPasswordEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.cPasswordEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Constants.SUCCESS_RESULT) {
                loc = resultData.getString(Constants.RESULT_DATA_KEY);
                binding.locationEt.setText(loc);
            } else {
            }
        }
    }
}