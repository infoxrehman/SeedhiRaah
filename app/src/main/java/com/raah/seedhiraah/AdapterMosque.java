package com.raah.seedhiraah;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.raah.seedhiraah.databinding.RowMosqueBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdapterMosque extends RecyclerView.Adapter<AdapterMosque.HolderMasjid> implements Filterable {
    private final Context context;
    private final ProgressDialog progressDialog;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;
    public ArrayList<ModelMosque> mosqueArrayList, filterList;
    private RowMosqueBinding binding;
    private FilterMosque filter;

    public AdapterMosque(Context context, ArrayList<ModelMosque> mosqueArrayList) {
        this.context = context;
        this.mosqueArrayList = mosqueArrayList;
        this.filterList = mosqueArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderMasjid onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowMosqueBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderMasjid(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMasjid holder, int position) {

        ModelMosque model = mosqueArrayList.get(position);
        String mosqueId = model.getMosqueId();
        String ownerId = model.getOwnerId();
        String mosqueImage = model.getMosqueImage();
        String mosqueName = model.getMosqueName();
        String cityName = model.getCityName();
        String mosqueDescription = model.getMosqueDescription();
        String ownerName = model.getOwnerName();
        String capacity = model.getMosqueCapacity();
        double lon = model.getLongitude();
        double lat = model.getLatitude();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null && currentUser.getUid().equals(ownerId)) {
            holder.menuBn.setVisibility(View.VISIBLE);
        } else {
            holder.menuBn.setVisibility(View.GONE);
        }

        try {
            Glide.with(context).load(mosqueImage).placeholder(R.drawable.default_mosque).into(holder.mosqueImg);
        } catch (Exception e) {
        }

        holder.mosqueNm.setText(mosqueName);
        holder.cityNm.setText(cityName);
        holder.cityNm.setText(cityName);

        holder.goBn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openMap(lat, lon);
            }

        });

        holder.menuBn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }

        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MosqueDetailsActivity.class);
                intent.putExtra("mosqueId", mosqueId);
                intent.putExtra("mosqueName", mosqueName);
                intent.putExtra("cityName", cityName);
                intent.putExtra("mosqueImage", mosqueImage);
                intent.putExtra("mosqueDescription", mosqueDescription);
                intent.putExtra("lon", lon);
                intent.putExtra("lat", lat);
                intent.putExtra("ownerId", ownerId);
                intent.putExtra("ownerName", ownerName);
                intent.putExtra("capacity", capacity);
                context.startActivity(intent);
            }
        });

        try {
            loadNamazTimeData(holder, mosqueId);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadNamazTimeData(@NonNull HolderMasjid holder, String mosqueId) throws ParseException {
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

                    try {
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
                            loadData(holder, "Fajr", mosqueId);
                        } else if (currentTimeDate.after(dhuhrTime1Date) && currentTimeDate.before(dhuhrTime2Date)) {
                            loadData(holder, "Dhuhr", mosqueId);
                        } else if (currentTimeDate.after(asrTime1Date) && currentTimeDate.before(asrTime2Date)) {
                            loadData(holder, "Asr", mosqueId);
                        } else if (currentTimeDate.after(magribTime1Date) && currentTimeDate.before(magribTime2Date)) {
                            loadData(holder, "Magrib", mosqueId);
                        } else if (currentTimeDate.after(ishaTime1Date) && currentTimeDate.before(ishaTime2Date)) {
                            loadData(holder, "Isha", mosqueId);
                        } else {
                            String noNamaz = "No namaz yet";
                            holder.currentNm.setText(noNamaz);
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void loadData(@NonNull HolderMasjid holder, String namaz, String mosqueId) {
        db.collection("Mosques").document(mosqueId).collection("Details").document(namaz).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String hour1 = documentSnapshot.getString("hour1");
                String hour2 = documentSnapshot.getString("hour2");

                if (hour1 == null && hour2 == null) {
                    String noNamaz = "No namaz yet";
                    holder.currentNm.setText(noNamaz);
                } else {
                    String time = namaz + " - " + hour2;
                    holder.currentNm.setText(time);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void openMap(double latitude, double longitude) {
        Uri mapUri = Uri.parse("https://www.google.com/maps?daddr=" + latitude + "," + longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
        context.startActivity(intent);
    }

    private void deleteClass(String mosqueId, String imageId) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("MosquesImages/" + imageId);
        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                db.collection("Mosques").document(mosqueId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Mosque data deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to delete mosque data due " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void moreOptionsDialog(ModelMosque model, HolderMasjid holder) {

        String mosqueId = model.getMosqueId();
        String mosqueImage = model.getMosqueImage();
        String mosqueName = model.getMosqueName();
        String imageId = model.getImageId();

        String[] options = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(context, EditMosqueActivity.class);
                    intent.putExtra("mosqueId", mosqueId);
                    context.startActivity(intent);
                } else if (which == 1) {
                    deleteClass(mosqueId, imageId);
                }
            }
        }).show();
    }

    @Override
    public int getItemCount() {
        return mosqueArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) filter = new FilterMosque(filterList, this);
        return filter;
    }

    class HolderMasjid extends RecyclerView.ViewHolder {
        ShapeableImageView mosqueImg;
        TextView mosqueNm, cityNm,currentNm;
        ImageButton menuBn;
        ImageView goBn;

        public HolderMasjid(@NonNull View itemView) {
            super(itemView);
            mosqueImg = binding.mosqueImage;
            mosqueNm = binding.mosqueName;
            cityNm = binding.cityName;
            currentNm = binding.currentNamaz;
            menuBn = binding.menuBtn;
            goBn = binding.goBtn;
        }
    }
}
