package com.raah.seedhiraah;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.raah.seedhiraah.databinding.FragmentAddMosqueBinding;

import java.util.HashMap;
import java.util.UUID;

public class AddMosqueFragment extends Fragment {
    private FragmentAddMosqueBinding binding;
    private Context mContext;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private ResultReceiver resultReceiver;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mosqueName, cityName, ownerName, email, loc, password, mosqueDescription, capacity, enterPassword;
    private double lat, lon;
    boolean isLadiesFacilityAvailable;
    private Uri imageUri;
    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                imageUri = data.getData();
                binding.addImageSrc.setImageURI(imageUri);
            } else {
                Toast.makeText(mContext, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    });
    private String imageId;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    public AddMosqueFragment() {
        // Required empty public constructor
    }

    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddMosqueBinding.inflate(LayoutInflater.from(mContext), container, false);
        Window window = requireActivity().getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        constants();

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        resultReceiver = new AddressResultReceiver(new Handler());

        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            getCurrentLocation();
        }

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

        db.collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                email = documentSnapshot.getString("email");
                if (email != null) {
                    binding.emailTv.setText(email);
                } else {
                    Toast.makeText(mContext, "Email is null", Toast.LENGTH_SHORT).show();
                }
                password = documentSnapshot.getString("password");
                binding.emailTv.setText(email);
            }
        });

        binding.isWomenAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isLadiesFacilityAvailable = isChecked;
            }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(mContext, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationServices.getFusedLocationProviderClient(mContext).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(mContext).removeLocationUpdates(this);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
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
        Intent intent = new Intent(mContext, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        mContext.startService(intent);
    }

    private void validateData() {
        enterPassword = binding.cPasswordEt.getText().toString().trim();
        mosqueName = binding.mosqueNameEt.getText().toString().trim();
        ownerName = binding.ownerNameEt.getText().toString().trim();
        mosqueDescription = binding.mosqueDescriptionEt.getText().toString().trim();
        cityName = binding.cityNameEt.getText().toString().trim();
        capacity = binding.capacityEt.getText().toString().trim();

        if (email != null && firebaseUser != null && firebaseUser.getEmail() != null && enterPassword != null && email.equals(firebaseUser.getEmail()) && enterPassword.equals(password)) {

            db.collection("Mosques").whereEqualTo("ownerId", firebaseUser.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    Toast.makeText(mContext, "You have already registered a class.", Toast.LENGTH_SHORT).show();
                } else {
                    if (TextUtils.isEmpty(mosqueName)) {
                        Toast.makeText(mContext, "Mosque name required!", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(cityName)) {
                        Toast.makeText(mContext, "City name required!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri == null) {
                            uploadData("", "");
                        } else {
                            uploadImage();
                        }
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(mContext, "Failed to check if mosque already exists: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(mContext, "Please enter a correct password!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadData(String imageUrl, String imageId) {

        progressDialog.setMessage("Uploading mosque data");
        progressDialog.show();

        String mosqueId = UUID.randomUUID().toString().replace("-","").substring(0,20);
        String ownerId = firebaseUser.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("mosqueId", mosqueId);
        hashMap.put("mosqueName", mosqueName);
        hashMap.put("cityName", cityName);
        hashMap.put("ownerId", ownerId);
        hashMap.put("ownerName", ownerName);
        hashMap.put("mosqueDescription", mosqueDescription);
        hashMap.put("isLadiesFacilityAvailable", isLadiesFacilityAvailable);
        hashMap.put("mosqueCapacity", capacity);
        hashMap.put("imageId", imageId);
        hashMap.put("longitude", lon);
        hashMap.put("latitude", lat);
        hashMap.put("location", loc);

        if (imageUri != null) {
            hashMap.put("mosqueImage", imageUrl);
        }

        db.collection("Mosques").document(mosqueId).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                resetData();
                Toast.makeText(mContext, "Successfully added", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadImage() {

        progressDialog.setMessage("Uploading mosque data");
        progressDialog.show();

        imageId = UUID.randomUUID().toString().replace("-","").substring(0,20);

        String filePathAndName = "MosquesImages/" + imageId;

        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            String uploadedImageUrl = String.valueOf(uriTask.getResult());

            uploadData(uploadedImageUrl, imageId);
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(mContext, "Failed to upload image due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            getActivity().finish();
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

    private void resetData() {
        enterPassword = "";
        mosqueName = "";
        ownerName = "";
        mosqueDescription = "";
        cityName = "";
        capacity = "";
        binding.mosqueNameEt.setText("");
        binding.ownerNameEt.setText("");
        binding.cityNameEt.setText("");
        binding.capacityEt.setText("");
        binding.mosqueDescriptionEt.setText("");
        binding.capacityEt.setText("");
        binding.cPasswordEt.setText("");
        imageUri = null;
        binding.addImageSrc.setImageResource(R.drawable.image_src);
        binding.isWomenAvailable.setChecked(false);
    }
}