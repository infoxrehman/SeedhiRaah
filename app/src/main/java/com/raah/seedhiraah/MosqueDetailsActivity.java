package com.raah.seedhiraah;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.raah.seedhiraah.databinding.ActivityMosqueDetailsBinding;

public class MosqueDetailsActivity extends AppCompatActivity {

    private ActivityMosqueDetailsBinding binding;
    private String mosqueName,mosqueImage, mosqueId, ownerId, ownerName,mosqueDescription,capacity;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private double  lon, lat ;
    boolean isLadiesFacilityAvailable;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMosqueDetailsBinding.inflate(getLayoutInflater());
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.background));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        mosqueId = getIntent().getStringExtra("mosqueId");
        mosqueName = getIntent().getStringExtra("mosqueName");
        mosqueImage = getIntent().getStringExtra("mosqueImage");
        mosqueDescription = getIntent().getStringExtra("mosqueDescription");
        lon = getIntent().getDoubleExtra("lon",0);
        lat = getIntent().getDoubleExtra("lat",0);
        ownerId = getIntent().getStringExtra("ownerId");
        ownerName = getIntent().getStringExtra("ownerName");
        capacity = getIntent().getStringExtra("capacity");

        if (firebaseUser != null && firebaseUser.getUid().equals(ownerId)) {
            binding.addFab.setVisibility(View.VISIBLE);
        } else {
            binding.addFab.setVisibility(View.GONE);
        }

        loadData();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color='#FFFFFF'>" + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.mosqueName.setText("Mosque Name: "+mosqueName);
        binding.mosqueDescription.setText("Mosque Description: "+mosqueDescription);
        binding.mosqueCapacity.setText("Mosque Capacity: "+capacity);

        db.collection("Mosques").document(mosqueId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                isLadiesFacilityAvailable = documentSnapshot.getBoolean("isLadiesFacilityAvailable");

                if (isLadiesFacilityAvailable == true){
                    binding.ladiesFacilityAvailable.setText("Availability  for ladies: Yes");
                }else {
                    binding.ladiesFacilityAvailable.setText("Availability  for ladies: No");
                }
            }
        });

        try {
            Glide.with(MosqueDetailsActivity.this).load(mosqueImage).placeholder(R.drawable.default_mosque).into(binding.loginUpper);
        } catch (Exception e) {
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null && currentUser.getUid().equals(ownerId)) {
            binding.addFab.setVisibility(View.VISIBLE);
        } else {
            binding.addFab.setVisibility(View.GONE);
        }

        binding.addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MosqueDetailsActivity.this, AddMosqueDetailsActivity.class);
                intent.putExtra("mosqueId", mosqueId);
                intent.putExtra("mosqueName", mosqueName);
                startActivity(intent);
            }
        });

        binding.goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap(lat, lon);
            }
        });

    }

    private void openMap(double latitude, double longitude) {
        Uri mapUri = Uri.parse("https://www.google.com/maps?daddr=" + latitude + "," + longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
        startActivity(intent);
    }

    private void loadData() {

        db.collection("Mosques").document(mosqueId).collection("Details").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String sub = documentSnapshot.getString("namaz");

                switch (sub) {
                    case "Fajr":
                        loadMosqueData(documentSnapshot, binding.fajrHour01, binding.fajrHour02);
                        break;
                    case "Dhuhr":
                        loadMosqueData(documentSnapshot, binding.dhuhrHour01, binding.dhuhrHour02);
                        break;
                    case "Asr":
                        loadMosqueData(documentSnapshot, binding.asrHour01, binding.asrHour02);
                        break;
                    case "Magrib":
                        loadMosqueData(documentSnapshot, binding.magribHour01, binding.magribHour02);
                        break;
                    case "Isha":
                        loadMosqueData(documentSnapshot, binding.ishaHour01, binding.ishaHour02);
                        break;
                    case "Jumah":
                        loadMosqueData(documentSnapshot, binding.jummahHour01, binding.jummahHour02);
                        break;
                    case "Taraweeh":
                        loadMosqueData(documentSnapshot, binding.taraweehHour01, binding.taraweehHour02);
                        break;
                    case "EidFitr":
                        loadMosqueData(documentSnapshot, binding.eidFitrHour01, binding.eidFitrHour02);
                        break;
                    case "EidAdha":
                        loadMosqueData(documentSnapshot, binding.eidAdhaHour01, binding.eidAdhaHour02);
                        break;
                    default:

                        break;
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(MosqueDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    private void loadMosqueData(DocumentSnapshot documentSnapshot, TextView hour1Et, TextView hour2Et) {
        String hour1 = documentSnapshot.getString("hour1");
        String hour2 = documentSnapshot.getString("hour2");
        if (hour1 != null && hour2 != null) {
            hour1Et.setText(hour1);
            hour2Et.setText(hour2);
        } else {
            hour1Et.setHint("-");
            hour2Et.setHint("-");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}