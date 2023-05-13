package com.timusandrei.smartpot;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.type.DateTime;
import com.timusandrei.smartpot.models.DeviceData;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Graphs extends AppCompatActivity implements View.OnClickListener {

    private String productId;
    private LineChart lineChart;
    private List<Entry> moistureEntryArrayList;
    private List<Entry> temperatureEntryArrayList;
    private List<String> labelsNames;
    private List<DeviceData> deviceData;
    private TextView startDate;
    private TextView endDate;
    private TextView perDay;
    private TextView perMonth;
    private TextView perYear;
    private TextView allTime;
    private DatePickerDialog.OnDateSetListener startDatePickerDialog;
    private DatePickerDialog.OnDateSetListener endDatePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        perDay = findViewById(R.id.per_day);
        perMonth = findViewById(R.id.per_month);
        perYear = findViewById(R.id.per_year);
        allTime = findViewById(R.id.all_time);

        productId = getIntent().getExtras().get("productId").toString();
        lineChart = findViewById(R.id.line_chart);
        deviceData = new ArrayList<>();
        labelsNames = new ArrayList<>();
        moistureEntryArrayList = new ArrayList<>();
        temperatureEntryArrayList = new ArrayList<>();
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisRight().setTextColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getLegend().setTextColor(Color.WHITE);
        fillWithData();

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
                fillChart();
            }
        };

        endDatePickerDialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String m = String.format("%02d", month);
                String d = String.format("%02d", dayOfMonth);
                String date = d + "/" + m + "/" + year;

                endDate.setText(date);
                fillChart();
            }
        };



    }

    private void fillWithData() {
        deviceData.clear();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            deviceData.add(new DeviceData(20, 20, LocalDate.now(), LocalTime.now()));
//        }

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
                    fillChart();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG);
            }
        });
    }
    private void fillChart() {

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

        moistureEntryArrayList.clear();
        temperatureEntryArrayList.clear();
        labelsNames.clear();
        for (int i = 0; i < copyOfDeviceData.size(); ++i)
        {
            String date = copyOfDeviceData.get(i).getDate().toString() + "\n" + deviceData.get(i).getTime();
            long moisture = copyOfDeviceData.get(i).getMoisture();
            double temperature = copyOfDeviceData.get(i).getTemperature();
            moistureEntryArrayList.add(new BarEntry(i, moisture));
            temperatureEntryArrayList.add(new BarEntry(i, (float)temperature));
            labelsNames.add(date);
        }

        LineDataSet moistureLineDataSet = new LineDataSet(moistureEntryArrayList, "Moisture");
        LineDataSet temperatureLineDataSet = new LineDataSet(temperatureEntryArrayList, "Temperature");
        moistureLineDataSet.setValueTextColor(Color.WHITE);
        moistureLineDataSet.setColors(ColorTemplate.LIBERTY_COLORS);
        temperatureLineDataSet.setValueTextColor(Color.WHITE);
        temperatureLineDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        Description description = new Description();
        description.setText("Your plant results");
        description.setTextColor(Color.WHITE);
        lineChart.setDescription(description);
        LineData lineData = new LineData(moistureLineDataSet);
        lineData.addDataSet(temperatureLineDataSet);
        lineChart.setData(lineData);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsNames));
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labelsNames.size());
        xAxis.setLabelRotationAngle(270);
        lineChart.animateY(2000);
        lineChart.invalidate();
    }

    private void setStartDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int mounth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                Graphs.this,
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
                Graphs.this,
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
            case R.id.start_date:
                setStartDate();
                break;
            case R.id.end_date:
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
        fillChart();
    }
}