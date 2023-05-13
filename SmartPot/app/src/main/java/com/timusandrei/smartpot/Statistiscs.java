package com.timusandrei.smartpot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.timusandrei.smartpot.models.DeviceData;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Statistiscs extends AppCompatActivity implements View.OnClickListener {

    private List<DeviceData> deviceData;
    private String productId;
    private TextView startDate;
    private TextView endDate;
    private TextView perDay;
    private TextView perMonth;
    private TextView perYear;
    private TextView allTime;
    private TextView minMoisture;
    private TextView midMoisture;
    private TextView maxMoisture;
    private TextView minTemperature;
    private TextView midTemperature;
    private TextView maxTemperature;
    private DatePickerDialog.OnDateSetListener startDatePickerDialog;
    private DatePickerDialog.OnDateSetListener endDatePickerDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistiscs);

        productId = getIntent().getExtras().get("productId").toString();
        startDate = findViewById(R.id.start_date_stats);
        endDate = findViewById(R.id.end_date_stats);
        perDay = findViewById(R.id.per_day);
        perMonth = findViewById(R.id.per_month);
        perYear = findViewById(R.id.per_year);
        allTime = findViewById(R.id.all_time);

        minMoisture = findViewById(R.id.mstats_min);
        midMoisture = findViewById(R.id.mstats_mid);
        maxMoisture = findViewById(R.id.mstats_max);
        minTemperature = findViewById(R.id.tstats_min);
        midTemperature = findViewById(R.id.tstats_mid);
        maxTemperature = findViewById(R.id.tstats_max);

        deviceData = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            startDate.setText(dateTimeFormatter.format(LocalDate.now()));
            endDate.setText(dateTimeFormatter.format(LocalDate.now()));
        }

        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);
        perDay.setOnClickListener(this);
        perMonth.setOnClickListener(this);
        perYear.setOnClickListener(this);
        allTime.setOnClickListener(this);

        startDatePickerDialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String m = String.format("%02d", month);
                String d = String.format("%02d", dayOfMonth);
                String date = d + "/" + m + "/" + year;

                startDate.setText(date);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    fillTable();
                }
            }
        };

        endDatePickerDialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String m = String.format("%02d", month);
                String d = String.format("%02d", dayOfMonth);
                String date = d + "/" + m + "/" + year;

                endDate.setText(date);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    fillTable();
                }
            }
        };

        fillWithData();
    }

    private void fillWithData() {
        deviceData.clear();

        FirebaseDatabase.getInstance().getReference(productId + "/History").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            long moisture = (long) ds.child("moisture").getValue();
                            double temperature = (double) ds.child("temperature").getValue();
                            String dateString = ds.child("date").getValue().toString();
                            String timeString = ds.child("time").getValue().toString();
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy");
                            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                            LocalDate date = LocalDate.parse(dateString, dateFormatter);
                            LocalTime time = LocalTime.parse(timeString, timeFormatter);
                            deviceData.add(new DeviceData(moisture, temperature, date, time));
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        fillTable();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG);
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fillTable() {

        DecimalFormat df = new DecimalFormat("0.00");

        List<DeviceData> copyOfDeviceData = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String startDateString = startDate.getText().toString();
            String endDateString = endDate.getText().toString();
            copyOfDeviceData = deviceData.stream()
                    .filter(u -> u.getDate().isAfter(LocalDate.parse(startDateString, dateTimeFormatter).minusDays(1)) &&
                            u.getDate().isBefore(LocalDate.parse(endDateString, dateTimeFormatter).plusDays(1)))
                    .collect(Collectors.toList());
        }

        long minMoistureVal = copyOfDeviceData.stream().min(Comparator.comparing(DeviceData::getMoisture)).orElse(new DeviceData(0, 0, LocalDate.now(), LocalTime.now())).getMoisture();
        long sumMoistureVal = copyOfDeviceData.stream().mapToLong(DeviceData::getMoisture).sum();
        double midMoistureVal;
        if (copyOfDeviceData.size() > 0) {
            midMoistureVal = (double) sumMoistureVal / copyOfDeviceData.size();
        } else {
            midMoistureVal = 0;
        }
        long maxMoistureVal = copyOfDeviceData.stream().max(Comparator.comparing(DeviceData::getMoisture)).orElse(new DeviceData(0, 0, LocalDate.now(), LocalTime.now())).getMoisture();

        double minTemperatureVal = copyOfDeviceData.stream().min(Comparator.comparing(DeviceData::getTemperature)).orElse(new DeviceData(0, 0, LocalDate.now(), LocalTime.now())).getTemperature();
        double sumTemperatureVal = copyOfDeviceData.stream().mapToDouble(DeviceData::getTemperature).sum();
        double midTemperatureVal;
        if(copyOfDeviceData.size() > 0) {
            midTemperatureVal = sumTemperatureVal / copyOfDeviceData.size();
        } else {
            midTemperatureVal = 0;
        }
        double maxTemperatureVal = copyOfDeviceData.stream().max(Comparator.comparing(DeviceData::getTemperature)).orElse(new DeviceData(0, 0, LocalDate.now(), LocalTime.now())).getTemperature();

        minMoisture.setText(df.format(minMoistureVal));
        midMoisture.setText(df.format(midMoistureVal));
        maxMoisture.setText(df.format(maxMoistureVal));
        minTemperature.setText(df.format(minTemperatureVal));
        midTemperature.setText(df.format(midTemperatureVal));
        maxTemperature.setText(df.format(maxTemperatureVal));

    }

    private void setStartDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int mounth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                Statistiscs.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                startDatePickerDialog,
                year, mounth, day);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void setEndDate()
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int mounth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                Statistiscs.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                endDatePickerDialog,
                year, mounth, day);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.start_date_stats:
                setStartDate();
                break;
            case R.id.end_date_stats:
                setEndDate();
                break;
            case R.id.per_day:
                startDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                endDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                break;
            case R.id.per_month:
                startDate.setText(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                endDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                break;
            case R.id.per_year:
                startDate.setText(LocalDate.of(LocalDate.now().getYear(), 1, 1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                endDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                break;
            case R.id.all_time:
                LocalDate minDate = deviceData.stream().min(Comparator.comparing(DeviceData::getDate)).map(DeviceData::getDate).orElse(LocalDate.MIN);
                startDate.setText(minDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                endDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                break;
        }
        fillTable();
    }

}