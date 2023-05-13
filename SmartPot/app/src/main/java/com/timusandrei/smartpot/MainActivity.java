package com.timusandrei.smartpot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.slider.Slider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moistureProgress = findViewById(R.id.moisture_progress);
        moistureText = findViewById(R.id.percentage_moisture_text);
        temperatureProgress = findViewById(R.id.temperature_progress);
        temperatureText = findViewById(R.id.percentage_temperature_text);
        progressBar = findViewById(R.id.progress_bar);
        moistureSlider = findViewById(R.id.moisture_sliderbar);
        optimumMoistureText = findViewById(R.id.optimum_moisture);
        pumpSwitch = findViewById(R.id.pump_switch);

        lightStatus = findViewById(R.id.light_status);
        lightStatusText = findViewById(R.id.light_status_text);

        Button waterButton = findViewById(R.id.water_button);

        productId = getIntent().getExtras().get("productId").toString();
        updateActualData(productId);

        pumpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseDatabase.getInstance().getReference(productId+"/pump").setValue(isChecked);
            }
        });

        waterButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    waterButton.setBackgroundColor(getResources().getColor(R.color.purple_700));
                    FirebaseDatabase.getInstance().getReference(productId+"/waterNow").setValue(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    FirebaseDatabase.getInstance().getReference(productId+"/waterNow").setValue(false);
                    waterButton.setBackgroundColor(getResources().getColor(R.color.purple_500));
                }
                return true;
            }
        });

        moistureSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull @NotNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull @NotNull Slider slider) {
                optimumMoistureText.setText("Optimum moisture percentage: " + (int)slider.getValue() + "%");
                FirebaseDatabase.getInstance().getReference(productId+"/percentage").setValue(slider.getValue());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu, menu);
        MenuItem itemGraphs = menu.findItem(R.id.graphs);
        itemGraphs.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, Graphs.class);
                intent.putExtra("productId", productId);
                startActivity(intent);
                return true;
            }
        });
        MenuItem itemStatistics = menu.findItem(R.id.statistics);
        itemStatistics.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, Statistiscs.class);
                intent.putExtra("productId", productId);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    public void updateActualData(String id) {

        DecimalFormat df = new DecimalFormat("0.00");

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        db.getReference("/" + id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                boolean lightStatusValue = (boolean) snapshot.child("light").getValue();
                boolean pumpSwitchValue = (boolean) snapshot.child("pump").getValue();
                long moisturePercentageValue = (long) snapshot.child("percentage").getValue();

                pumpSwitch.setChecked(pumpSwitchValue);
                moistureSlider.setValue(moisturePercentageValue);
                optimumMoistureText.setText(String.valueOf("Optimum moisture percentage: " + moisturePercentageValue + "%"));

                if(lightStatusValue) {
                    lightStatus.setBackgroundResource(R.drawable.light_on);
                    lightStatusText.setText("HAS LIGHT");
                } else {
                    lightStatus.setBackgroundResource(R.drawable.light_off);
                    lightStatusText.setText("NO LIGHT");
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.e("Firebase error", error.getMessage());
            }
        });

        DatabaseReference actualRef = db.getReference("/" + id + "/Actual").getRef();

        actualRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                long moisture = (long) snapshot.child("moisture").getValue();
                double temperature = (double) snapshot.child("temperature").getValue();

                moistureText.setText(moisture + "%");
                temperatureText.setText(df.format(temperature) + "°C");

                moistureProgress.setProgress((int) moisture);
                temperatureProgress.setProgress((int) temperature);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.e("Firebase error", error.getMessage());
            }
        });
    }

    String productId;

    ProgressBar moistureProgress;
    ProgressBar temperatureProgress;

    TextView moistureText;
    TextView temperatureText;
    TextView optimumMoistureText;

    Slider moistureSlider;
    ProgressBar progressBar;

    SwitchCompat pumpSwitch;

    RelativeLayout lightStatus;
    TextView lightStatusText;
    //double moisturePercentageValue;
}


///Python scripy

//if(!Python.isStarted()){
//        Python.start(new AndroidPlatform(MainActivity.this));
//        }
//
//        Python py = Python.getInstance();
//        String code = "print(\"hello\")\n";
//
//        // 4. Obtain the system's input stream (available from Chaquopy)
//        PyObject sys = py.getModule("sys");
//        PyObject io = py.getModule("io");
//// Obtain the interpreter.py module
//        PyObject console = py.getModule("interpreter");
//
//// 5. Redirect the system's output stream to the Python interpreter
//        PyObject textOutputStream = io.callAttr("StringIO");
//        sys.put("stdout", textOutputStream);
//
//
//// 6. Create a string variable that will contain the standard output of the Python interpreter
//        String interpreterOutput = "";
//
//// 7. Execute the Python code
//        try {
//        //console.callAttrThrows("mainTextCode", code);
//
//        interpreterOutput = textOutputStream.callAttr("getvalue").toString();
//        }catch (PyException e){
//        // If there's an error, you can obtain its output as well
//        // e.g. if you mispell the code
//        // Missing parentheses in call to 'print'
//        // Did you mean print("text")?
//        // <string>, line 1
//        interpreterOutput = e.getMessage().toString();
//        } catch (Throwable throwable) {
//        throwable.printStackTrace();
//        }
//        Log.e("TAGG", interpreterOutput);
//        //System.out.println(interpreterOutput);
//        }

//                String dateText = (String) snapshot.child("date").getValue();
//                String timeText = (String) snapshot.child("time").getValue();

//                DateTimeFormatter dateFormatter = null;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy");
//                }
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    LocalDate date = LocalDate.parse(dateText, dateFormatter);
//                }
//
//                DateTimeFormatter timeFormatter = null;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
//                }
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    LocalTime time = LocalTime.parse(timeText, timeFormatter);
//                }