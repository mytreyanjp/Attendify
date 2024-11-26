package com.example.appipi;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutLocation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Get the address and state from the Intent
        String address = getIntent().getStringExtra("address");
        String state = getIntent().getStringExtra("state");

        // Display the address in a TextView
        TextView addressTextView = findViewById(R.id.addressTextView);
        TextView universityTextView = findViewById(R.id.universityTextView);

        if (address=="Unkown"){
            address="Chennai, Tamil Nadu, India";
            state="tamil nadu";
        }

        addressTextView.setText("Address: " + address);

        // Determine university based on state
        String university = getUniversityForState(state);
        universityTextView.setText(" Possible University: \n" + university);
    }

    // A method to determine the university based on the state
    private String getUniversityForState(String state) {
        String university = "Unknown University";

        if (state != null) {
            switch (state.toLowerCase()) {
                case "california":
                    university = "University of California, Berkeley";
                    break;
                case "texas":
                    university = "University of Texas, Austin";
                    break;
                case "new york":
                    university = "New York University";
                    break;
                case "tamil nadu":
                    university="Anna university ,Chennai";
                            break;
                // Add more states and universities here
                default:
                    university = "Unknown University";
                    break;
            }
        }

        return university;
    }
}
