package com.raah.seedhiraah;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.raah.seedhiraah.databinding.FragmentHomeBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Context mContext;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private int count;
    private ArrayList<ModelMosque> mosqueArrayList;
    private AdapterMosque adapterMosque;
    private String[] diametersList;
    private String selectedDiameters;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    public HomeFragment() {
        // Required empty public constructor
    }

    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false);
        Window window = requireActivity().getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        fetchNamazTime();

        getCurrentLocation("1");

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterMosque.getFilter().filter(charSequence);
                } catch (Exception ignored) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }

    private void fetchNamazTime() {
        db.collection("NamazData").document("data").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    String fajrTime1 = documentSnapshot.getString("fajrHour1");
                    String fajrTime2 = documentSnapshot.getString("fajrHour2");
                    String dhuhrTime1 = documentSnapshot.getString("dhuhrHour1");
                    String dhuhrTime2 = documentSnapshot.getString("dhuhrHour2");
                    String asrTime1 = documentSnapshot.getString("asrHour1");
                    String asrTime2 = documentSnapshot.getString("asrHour2");
                    String magribTime1 = documentSnapshot.getString("magribHour1");
                    String magribTime2 = documentSnapshot.getString("magribHour2");
                    String ishaTime1 = documentSnapshot.getString("ishaHour1");
                    String ishaTime2 = documentSnapshot.getString("ishaHour2");

                    String fajrImage = documentSnapshot.getString("fajrImage");
                    String dhuhrImage = documentSnapshot.getString("dhuhrImage");
                    String asrImage = documentSnapshot.getString("asrImage");
                    String magribImage = documentSnapshot.getString("magribImage");
                    String ishaImage = documentSnapshot.getString("ishaImage");

                    try {
                        loadBanners(fajrTime1, fajrTime2, dhuhrTime1, dhuhrTime2, asrTime1, asrTime2, magribTime1, magribTime2, ishaTime1, ishaTime2, fajrImage, dhuhrImage, asrImage, magribImage, ishaImage);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void loadBanners(String fajrTime1, String fajrTime2, String dhuhrTime1, String dhuhrTime2, String asrTime1, String asrTime2, String magribTime1, String magribTime2, String ishaTime1, String ishaTime2, String fajrImage, String dhuhrImage, String asrImage, String magribImage, String ishaImage) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
        String currentTime = sdf.format(new Date());

        Date currentTimeDate = sdf.parse(currentTime);
        Date fajrTime1Date = sdf.parse(fajrTime1);
        Date fajrTime2Date = sdf.parse(fajrTime2);
        Date dhuhrTime1Date = sdf.parse(dhuhrTime1);
        Date dhuhrTime2Date = sdf.parse(dhuhrTime2);
        Date asrTime1Date = sdf.parse(asrTime1);
        Date asrTime2Date = sdf.parse(asrTime2);
        Date magribTime1Date = sdf.parse(magribTime1);
        Date magribTime2Date = sdf.parse(magribTime2);
        Date ishaTime1Date = sdf.parse(ishaTime1);
        Date ishaTime2Date = sdf.parse(ishaTime2);

        if (currentTimeDate.after(fajrTime1Date) && currentTimeDate.before(fajrTime2Date)) {
            Glide.with(mContext).load(fajrImage).placeholder(R.drawable.default_banner).into(binding.bannerImage);
        } else if (currentTimeDate.after(dhuhrTime1Date) && currentTimeDate.before(dhuhrTime2Date)) {
            Glide.with(mContext).load(dhuhrImage).placeholder(R.drawable.default_banner).into(binding.bannerImage);
        } else if (currentTimeDate.after(asrTime1Date) && currentTimeDate.before(asrTime2Date)) {
            Glide.with(mContext).load(asrImage).placeholder(R.drawable.default_banner).into(binding.bannerImage);
        } else if (currentTimeDate.after(magribTime1Date) && currentTimeDate.before(magribTime2Date)) {
            Glide.with(mContext).load(magribImage).placeholder(R.drawable.default_banner).into(binding.bannerImage);
        } else if (currentTimeDate.after(ishaTime1Date) && currentTimeDate.before(ishaTime2Date)) {
            Glide.with(mContext).load(ishaImage).placeholder(R.drawable.default_banner).into(binding.bannerImage);
        }else {
            Glide.with(mContext).load(R.drawable.default_banner).placeholder(R.drawable.default_banner).into(binding.bannerImage);
        }

    }

    private void showFilterDialog() {
        Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.filter_dialog);

        AppCompatSpinner filterSpinner = dialog.findViewById(R.id.filter_spinner);
        Button uploadBtn = dialog.findViewById(R.id.upload);

        diametersList = getResources().getStringArray(R.array.diameters);

        ArrayAdapter salutationAdapter = new ArrayAdapter(mContext, R.layout.slider, diametersList);
        salutationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        filterSpinner.setAdapter(salutationAdapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDiameters = diametersList[position];

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchClasses(selectedDiameters);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void fetchClasses(String selectedDiameter) {
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            getCurrentLocation(selectedDiameter);
        }
    }

    private void getCurrentLocation(String selectedDiameter) {
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            requestLocationUpdates(selectedDiameter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String selectedDiameter = "1";
                requestLocationUpdates(selectedDiameter);
            } else {
                Toast.makeText(mContext, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestLocationUpdates(String selectedDiameter) {
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
                    Location lastLocation = locationResult.getLastLocation();
                    double latitude = lastLocation.getLatitude();
                    double longitude = lastLocation.getLongitude();

                    loadData(selectedDiameter, longitude, latitude);
                }
            }
        }, Looper.getMainLooper());
    }

    private void loadData(String selectedDiameter, double userLongitude, double userLatitude) {

        mosqueArrayList = new ArrayList<>();

        progressDialog.setMessage("Fetching mosque data");
        progressDialog.show();

        double maxDistance = Double.parseDouble(selectedDiameter) * 1000;

        db.collection("Mosques")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

            mosqueArrayList.clear();

            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                double mosqueLatitude = documentSnapshot.getDouble("latitude");
                double mosqueLongitude = documentSnapshot.getDouble("longitude");
                double distance = calculateDistance(userLatitude, userLongitude, mosqueLatitude, mosqueLongitude);

                if (distance <= maxDistance) {
                    ModelMosque model = documentSnapshot.toObject(ModelMosque.class);
                    mosqueArrayList.add(model);
                }
            }

            if (mosqueArrayList.isEmpty()) {
                progressDialog.dismiss();
                binding.fetchedEt.setText("No mosque fetched...");
                binding.fetchedEt.setTextColor(getResources().getColor(R.color.black));
            } else {
                adapterMosque = new AdapterMosque(mContext, mosqueArrayList);
                binding.classesRv.setAdapter(adapterMosque);
                progressDialog.dismiss();
                count = mosqueArrayList.size();
                binding.fetchedEt.setText(count + " mosque fetched");
                binding.fetchedEt.setTextColor(getResources().getColor(R.color.black));
                adapterMosque.notifyDataSetChanged();
            }
            progressDialog.dismiss();

        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(mContext, "Failed to fetch mosques: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private double calculateDistance(double userLatitude, double userLongitude, double mosqueLatitude, double mosqueLongitude) {
        double R = 6371;

        double latDistance = Math.toRadians(mosqueLatitude - userLatitude);
        double lonDistance = Math.toRadians(mosqueLongitude - userLongitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(userLatitude)) * Math.cos(Math.toRadians(mosqueLatitude)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000;

        return distance;

    }

}