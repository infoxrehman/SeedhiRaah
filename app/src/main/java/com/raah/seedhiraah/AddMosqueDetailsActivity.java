package com.raah.seedhiraah;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.raah.seedhiraah.databinding.ActivityAddMosqueDetailsBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class AddMosqueDetailsActivity extends AppCompatActivity {
    String amPm;
    private ActivityAddMosqueDetailsBinding binding;
    private String mosqueId, mosqueName, detailId;
    private String fajrHour1, fajrHour2, dhuhrHour1, dhuhrHour2, asrHour1, asrHour2, magribHour1, magribHour2, ishaHour1, ishaHour2, jummahHour1, jummahHour2, taraweehHour1, taraweehHour2, eidFitr1, eidFitr2, eidAdha1, eidAdha2;
    private FirebaseFirestore db;
    private int mHours, mMinute;
    private Calendar calender;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMosqueDetailsBinding.inflate(getLayoutInflater());
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.background));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(binding.getRoot());

        mosqueId = getIntent().getStringExtra("mosqueId");
        mosqueName = getIntent().getStringExtra("mosqueName");

        db = FirebaseFirestore.getInstance();

        calender = Calendar.getInstance();

        mHours = calender.get(Calendar.HOUR_OF_DAY);
        mMinute = calender.get(Calendar.MINUTE);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        constants();
        fetchDetailIds(mosqueId);

        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void fetchDetailIds(String mosqueId) {
        if (mosqueId != null) {
            db.collection("Mosques").document(mosqueId).collection("Details").get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String detailId = documentSnapshot.getId();

                    loadExistingData(detailId);
                }
            }).addOnFailureListener(e -> {

                Toast.makeText(AddMosqueDetailsActivity.this, "Failed to fetch detailIds: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadExistingData(String Sid) {

        db.collection("Mosques").document(mosqueId).collection("Details").document(Sid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nam = documentSnapshot.getString("namaz");

                if ("Fajr".equals(nam)) {
                    String hour1 = documentSnapshot.getString("hour1");
                    String hour2 = documentSnapshot.getString("hour2");
                    binding.fajrHour01.setText(hour1 != null ? hour1 : "00:00");
                    binding.fajrHour02.setText(hour2 != null ? hour2 : "00:00");
                } else {
                    binding.fajrHour01.setHint("00:00");
                    binding.fajrHour02.setHint("00:00");
                }

                if ("Dhuhr".equals(nam)) {
                    String hour1 = documentSnapshot.getString("hour1");
                    String hour2 = documentSnapshot.getString("hour2");
                    binding.dhuhrHour01.setText(hour1 != null ? hour1 : "00:00");
                    binding.dhuhrHour02.setText(hour2 != null ? hour2 : "00:00");
                } else {
                    binding.dhuhrHour01.setHint("00:00");
                    binding.dhuhrHour02.setHint("00:00");
                }

                if ("Asr".equals(nam)) {
                    String hour1 = documentSnapshot.getString("hour1");
                    String hour2 = documentSnapshot.getString("hour2");
                    binding.asrHour01.setText(hour1 != null ? hour1 : "00:00");
                    binding.asrHour02.setText(hour2 != null ? hour2 : "00:00");
                } else {
                    binding.asrHour01.setHint("00:00");
                    binding.asrHour02.setHint("00:00");
                }

                if ("Magrib".equals(nam)) {
                    String hour1 = documentSnapshot.getString("hour1");
                    String hour2 = documentSnapshot.getString("hour2");
                    binding.magribHour01.setText(hour1 != null ? hour1 : "00:00");
                    binding.magribHour02.setText(hour2 != null ? hour2 : "00:00");
                } else {
                    binding.magribHour01.setHint("00:00");
                    binding.magribHour02.setHint("00:00");
                }

                if ("Isha".equals(nam)) {
                    String hour1 = documentSnapshot.getString("hour1");
                    String hour2 = documentSnapshot.getString("hour2");
                    binding.ishaHour01.setText(hour1 != null ? hour1 : "00:00");
                    binding.ishaHour02.setText(hour2 != null ? hour2 : "00:00");
                } else {
                    binding.ishaHour01.setHint("00:00");
                    binding.ishaHour02.setHint("00:00");
                }

                if ("Jumah".equals(nam)) {
                    String hour1 = documentSnapshot.getString("hour1");
                    String hour2 = documentSnapshot.getString("hour2");
                    binding.jummahHour01.setText(hour1 != null ? hour1 : "00:00");
                    binding.jummahHour02.setText(hour2 != null ? hour2 : "00:00");
                } else {
                    binding.jummahHour01.setHint("00:00");
                    binding.jummahHour02.setHint("00:00");
                }

                if ("Taraweeh".equals(nam)) {
                    String hour1 = documentSnapshot.getString("hour1");
                    String hour2 = documentSnapshot.getString("hour2");
                    binding.taraweehHour01.setText(hour1 != null ? hour1 : "00:00");
                    binding.taraweehHour02.setText(hour2 != null ? hour2 : "00:00");
                } else {
                    binding.taraweehHour01.setHint("00:00");
                    binding.taraweehHour02.setHint("00:00");
                }

                if ("EidFitr".equals(nam)) {
                    String hour1 = documentSnapshot.getString("hour1");
                    String hour2 = documentSnapshot.getString("hour2");
                    binding.eidFitrHour01.setText(hour1 != null ? hour1 : "00:00");
                    binding.eidFitrHour02.setText(hour2 != null ? hour2 : "00:00");
                } else {
                    binding.eidFitrHour01.setHint("00:00");
                    binding.eidFitrHour02.setHint("00:00");
                }

                if ("EidAdha".equals(nam)) {
                    String hour1 = documentSnapshot.getString("hour1");
                    String hour2 = documentSnapshot.getString("hour2");
                    binding.eidAdhaHour01.setText(hour1 != null ? hour1 : "00:00");
                    binding.eidAdhaHour02.setText(hour2 != null ? hour2 : "00:00");
                } else {
                    binding.eidAdhaHour01.setHint("00:00");
                    binding.eidAdhaHour02.setHint("00:00");
                }

            }
        }).addOnFailureListener(e -> {
            Toast.makeText(AddMosqueDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void constants() {

        binding.fajrHour01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.fajrHour01.setText(time);
                        fajrHour1 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.fajrHour02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.fajrHour02.setText(time);
                        fajrHour2 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.dhuhrHour01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.dhuhrHour01.setText(time);
                        dhuhrHour1 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.dhuhrHour02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.dhuhrHour02.setText(time);
                        dhuhrHour2 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.asrHour01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.asrHour01.setText(time);
                        asrHour1 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.asrHour02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.asrHour02.setText(time);
                        asrHour2 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.magribHour01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.magribHour01.setText(time);
                        magribHour1 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.magribHour02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.magribHour02.setText(time);
                        magribHour2 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.ishaHour01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.ishaHour01.setText(time);
                        ishaHour1 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.ishaHour02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.ishaHour02.setText(time);
                        ishaHour2 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.jummahHour01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.jummahHour01.setText(time);
                        jummahHour1 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.jummahHour02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.jummahHour02.setText(time);
                        jummahHour2 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.taraweehHour01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.taraweehHour01.setText(time);
                        taraweehHour1 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.taraweehHour02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.taraweehHour02.setText(time);
                        taraweehHour2 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.eidFitrHour01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.eidFitrHour01.setText(time);
                        eidFitr1 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.eidFitrHour02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.eidFitrHour02.setText(time);
                        eidFitr2 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.eidAdhaHour01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.eidAdhaHour01.setText(time);
                        eidAdha1 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });

        binding.eidAdhaHour02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMosqueDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calender.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calender.set(Calendar.MINUTE,minute);
                        calender.setTimeZone(TimeZone.getDefault());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String time = sdf.format(calender.getTime());

                        binding.eidAdhaHour02.setText(time);
                        eidAdha2 = time;
                    }
                }, mHours, mMinute, false);
                timePickerDialog.show();
            }
        });
    }
    private void validateData() {

        boolean hasData = false;

        if (!TextUtils.isEmpty(fajrHour1) && !TextUtils.isEmpty(fajrHour2)) {
            uploadData();
            hasData = true;
        }

        if (!TextUtils.isEmpty(dhuhrHour1) && !TextUtils.isEmpty(dhuhrHour2)) {
            uploadData();
            hasData = true;
        }

        if (!TextUtils.isEmpty(asrHour1) && !TextUtils.isEmpty(asrHour2)) {
            uploadData();
            hasData = true;
        }

        if (!TextUtils.isEmpty(magribHour1) && !TextUtils.isEmpty(magribHour2)) {
            uploadData();
            hasData = true;
        }

        if (!TextUtils.isEmpty(ishaHour1) && !TextUtils.isEmpty(ishaHour2)) {
            uploadData();
            hasData = true;
        }

        if (!TextUtils.isEmpty(jummahHour1) && !TextUtils.isEmpty(jummahHour2)) {
            uploadData();
            hasData = true;
        }

        if (!TextUtils.isEmpty(taraweehHour1) && !TextUtils.isEmpty(taraweehHour2)) {
            uploadData();
            hasData = true;
        }

        if (!TextUtils.isEmpty(eidFitr1) && !TextUtils.isEmpty(eidFitr2)) {
            uploadData();
            hasData = true;
        }

        if (!TextUtils.isEmpty(eidAdha1) && !TextUtils.isEmpty(eidAdha2)) {
            uploadData();
            hasData = true;
        }
    }

    private void uploadData() {

        detailId = UUID.randomUUID().toString();

        if (!TextUtils.isEmpty(fajrHour1) && !TextUtils.isEmpty(fajrHour2)) {
            uploadMosqueData("Fajr", fajrHour1, fajrHour2, detailId);
        }

        if (!TextUtils.isEmpty(dhuhrHour1) && !TextUtils.isEmpty(dhuhrHour2)) {
            uploadMosqueData("Dhuhr", dhuhrHour1, dhuhrHour2, detailId);
        }

        if (!TextUtils.isEmpty(asrHour1) && !TextUtils.isEmpty(asrHour2)) {
            uploadMosqueData("Asr", asrHour1, asrHour2, detailId);
        }

        if (!TextUtils.isEmpty(magribHour1) && !TextUtils.isEmpty(magribHour2)) {
            uploadMosqueData("Magrib", magribHour1, magribHour2, detailId);
        }

        if (!TextUtils.isEmpty(ishaHour1) && !TextUtils.isEmpty(ishaHour2)) {
            uploadMosqueData("Isha", ishaHour1, ishaHour2, detailId);
        }

        if (!TextUtils.isEmpty(jummahHour1) && !TextUtils.isEmpty(jummahHour2)) {
            uploadMosqueData("Jumah", jummahHour1, jummahHour2, detailId);
        }

        if (!TextUtils.isEmpty(taraweehHour1) && !TextUtils.isEmpty(taraweehHour2)) {
            uploadMosqueData("Taraweeh", taraweehHour1, taraweehHour2, detailId);
        }

        if (!TextUtils.isEmpty(eidFitr1) && !TextUtils.isEmpty(eidFitr2)) {
            uploadMosqueData("EidFitr", eidFitr1, eidFitr2, detailId);
        }

        if (!TextUtils.isEmpty(eidAdha1) && !TextUtils.isEmpty(eidAdha2)) {
            uploadMosqueData("EidAdha", eidAdha1, eidAdha2, detailId);
        }
    }

    private void uploadMosqueData(String namaz, String hour1, String hour2, String detailId) {

        progressDialog.setMessage("Uploading mosque data");
        progressDialog.show();
        long timestamp = System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("namaz", namaz);
        hashMap.put("hour1", hour1);
        hashMap.put("hour2", hour2);
        hashMap.put("timestamp", timestamp);

        db.collection("Mosques").document(mosqueId).collection("Details").document(namaz).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        db.collection("Mosques").document(mosqueId).collection("Details").document(namaz).update("hour1", hour1, "hour2", hour2, "timestamp", timestamp).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(AddMosqueDetailsActivity.this, "Successfully updated " + namaz, Toast.LENGTH_SHORT).show();
                                updateFirstNamazData(mosqueId, namaz, hour1, hour2);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddMosqueDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        hashMap.put("detailId", detailId);
                        db.collection("Mosques").document(mosqueId).collection("Details").document(namaz).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(AddMosqueDetailsActivity.this, "Successfully added for " + namaz, Toast.LENGTH_SHORT).show();
                                updateFirstNamazData(mosqueId, namaz, hour1, hour2);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddMosqueDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(AddMosqueDetailsActivity.this, "Error fetching document", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateFirstNamazData(String mosqueId, String namaz, String hour1, String hour2) {
        progressDialog.setMessage("Uploading");
        db.collection("Mosques").document(mosqueId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        db.collection("Mosques").document(mosqueId).update("currentNamaz", namaz + " : " + hour1 + " - " + hour2).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(AddMosqueDetailsActivity.this, "Successfully updated data", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddMosqueDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(AddMosqueDetailsActivity.this, "document does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddMosqueDetailsActivity.this, "Error fetching document", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            finish();
        } else if (id == R.id.reset) {
            resetData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetData() {
        resetMosqueData("Fajr", "00:00", "00:00");
        resetMosqueData("Dhuhr", "00:00", "00:00");
        resetMosqueData("Asr", "00:00", "00:00");
        resetMosqueData("Magrib", "00:00", "00:00");
        resetMosqueData("Isha", "00:00", "00:00");
        resetMosqueData("Jumah", "00:00", "00:00");
        resetMosqueData("Taraweeh", "00:00", "00:00");
        resetMosqueData("EidFitr", "00:00", "00:00");
        resetMosqueData("EidAdha", "00:00", "00:00");
    }

    private void resetMosqueData(String namaz, String hour1, String hour2) {
        progressDialog.setMessage("Resetting mosque data");
        progressDialog.show();
        long timestamp = System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("hour1", hour1);
        hashMap.put("hour2", hour2);

        db.collection("Mosques").document(mosqueId).collection("Details").document(namaz).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    db.collection("Mosques").document(mosqueId).collection("Details").document(namaz).update("hour1", hour1, "hour2", hour2, "timestamp", timestamp).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(AddMosqueDetailsActivity.this, "Reset successful", Toast.LENGTH_SHORT).show();
                            resetFirstNamazData(mosqueId,namaz,hour1,hour2);
                            onBackPressed();
                            finish();
                        }
                    });
                }
            }
        });
    }

    private void resetFirstNamazData(String mosqueId, String namaz, String hour1, String hour2) {

        progressDialog.setMessage("Resetting");
        db.collection("Mosques").document(mosqueId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        db.collection("Mosques").document(mosqueId).update("currentNamaz", "No namaz yet").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(AddMosqueDetailsActivity.this, "Reset successful", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddMosqueDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(AddMosqueDetailsActivity.this, "document does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddMosqueDetailsActivity.this, "Error fetching document", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}