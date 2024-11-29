package com.example.appipi;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.location.Address;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import android.Manifest;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class MainActivity extends AppCompatActivity implements LocationListener {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private Spinner departmentSpinner, semesterSpinner;
    private Button submitDataButton;
    private int c=1;
    private String department, semester;
    private TextView userDetailsText, appname;
    private Toolbar toolbar;
    private LinearLayout courseLayout;
    private LocationManager locationManager;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Permission is granted, start requesting location updates
            startLocationUpdates();
        }

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            // User is already signed in, skip sign-in process
//            handleUserSignedIn(currentUser);
//        } else {
//            // Show Sign-In button if no user is signed in
//            findViewById(R.id.signIn).setVisibility(View.VISIBLE);
//        }

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // Replace with your actual client ID from Firebase Console
                .requestEmail()
                .build();

        // Initialize GoogleSignInClient
        googleSignInClient = GoogleSignIn.getClient(this, gso);




        // Initialize Views
        departmentSpinner = findViewById(R.id.departmentSpinner);
        semesterSpinner = findViewById(R.id.semesterSpinner);
        submitDataButton = findViewById(R.id.submitDataButton);
        courseLayout=findViewById(R.id.courseDetailsLayout);
        appname=findViewById(R.id.app_name_text);

        ObjectAnimator slideIn = ObjectAnimator.ofFloat(appname, "translationX", -1000f, 0f);
        slideIn.setDuration(2000);
        slideIn.start();

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        appname.startAnimation(fadeInAnimation);
        // Hide some views initially
        findViewById(R.id.departmentLabel).setVisibility(View.GONE);
        findViewById(R.id.semesterLabel).setVisibility(View.GONE);
        departmentSpinner.setVisibility(View.GONE);
        semesterSpinner.setVisibility(View.GONE);
        submitDataButton.setVisibility(View.GONE);

        courseLayout.setVisibility(View.GONE);

        // Set up Spinners with sample data (replace with your actual data)
        ArrayAdapter<CharSequence> departmentAdapter = ArrayAdapter.createFromResource(this,
                R.array.departments, android.R.layout.simple_spinner_item);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpinner.setAdapter(departmentAdapter);

        ArrayAdapter<CharSequence> semesterAdapter = ArrayAdapter.createFromResource(this,
                R.array.semesters, android.R.layout.simple_spinner_item);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        semesterSpinner.setAdapter(semesterAdapter);

        // Set click listener for Sign-In button
        findViewById(R.id.signIn).setOnClickListener(view -> signIn());

        // Set up submit button to store data
        submitDataButton.setOnClickListener(view -> submitData());

        // Initialize the Toolbar and set it as the ActionBar
        Toolbar toolbar = new Toolbar(this);

        toolbar.setTitle("Attendify");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);

        // Add the toolbar to the activity layout
        RelativeLayout layout = findViewById(R.id.main_layout); // Ensure the root layout has this ID
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layout.addView(toolbar, params);

        // Add Navigation Drawer setup (if applicable)
        // Assuming you have a Navigation Drawer layout
        // Set up the drawer and listener for navigation items (Google Account, Department, Semester, etc.)
    }
    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, // Use GPS provider
                    1000,  // Minimum time interval between updates (1 second)
                    10,    // Minimum distance between updates (10 meters)
                    this   // Pass the LocationListener (current activity)
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
//    private void handleUserSignedIn(FirebaseUser user) {
//        // Hide Google Sign-In button
//        findViewById(R.id.signIn).setVisibility(View.GONE);
//
//        // Display welcome message
//        String displayName = user.getDisplayName();
//        Toast.makeText(this, "Welcome back, " + displayName, Toast.LENGTH_SHORT).show();
//
//        courseLayout=findViewById(R.id.courseDetailsLayout);
//        courseLayout.setVisibility(View.VISIBLE);
//        appname=findViewById(R.id.app_name_text);
//        appname.setVisibility(View.GONE);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the result to Google Sign-In API
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                username=account.getEmail();
                Toast.makeText(this, "Account: " + username, Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.drawer_menu, menu);

        // Access the menu items after they are inflated
        MenuItem logout = menu.findItem(R.id.action_google_logout);
        MenuItem changeacademics = menu.findItem(R.id.action_change_academics);
        MenuItem about = menu.findItem(R.id.action_about);
        MenuItem location = menu.findItem(R.id.action_location);

        // Optionally modify the menu items
        logout.setTitle("Logout");
        changeacademics.setTitle("Change Academics");
        about.setTitle("About");
        location.setTitle("Location");

        SpannableString googleAccountTitle = new SpannableString("Logout");
        googleAccountTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, googleAccountTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        logout.setTitle(googleAccountTitle);

        SpannableString changeAcademicsTitle = new SpannableString("Change Academics");
        changeAcademicsTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, changeAcademicsTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        changeacademics.setTitle(changeAcademicsTitle);

        SpannableString aboutTitle = new SpannableString("About");
        aboutTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, aboutTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        about.setTitle(aboutTitle);

        SpannableString locationTitle = new SpannableString("Location");
        locationTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, aboutTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        location.setTitle(locationTitle);

        // Returning true indicates the menu has been successfully created
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle menu item clicks using if-else statements
        if (item.getItemId() == R.id.action_google_logout) {
            // Log the user out
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                mAuth.signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                // Restart the activity to reset UI
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            });
            return true;
        } else if (item.getItemId() == R.id.action_change_academics) {
            // Handle Change Department action
            Toast.makeText(this, "Change Academics", Toast.LENGTH_SHORT).show();

            courseLayout=findViewById(R.id.courseDetailsLayout);
            courseLayout.setVisibility(View.GONE);
            departmentSpinner.setVisibility(View.VISIBLE);
            semesterSpinner.setVisibility(View.VISIBLE);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(semesterSpinner, "alpha", 0f, 1f);
            fadeIn.setDuration(2000);
            fadeIn.start();
            ObjectAnimator fadeInn = ObjectAnimator.ofFloat(departmentSpinner, "alpha", 0f, 1f);
            fadeInn.setDuration(2000);
            fadeInn.start();


            findViewById(R.id.departmentLabel).setVisibility(View.VISIBLE);
            findViewById(R.id.semesterLabel).setVisibility(View.VISIBLE);
            submitDataButton.setVisibility(View.VISIBLE);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(submitDataButton, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(submitDataButton, "scaleY", 1f, 1.1f, 1f);
            scaleX.setDuration(1000);
            scaleY.setDuration(1000);
            scaleX.setRepeatCount(ObjectAnimator.INFINITE);
            scaleY.setRepeatCount(ObjectAnimator.INFINITE);
            scaleX.start();
            scaleY.start();


            return true;

        }  else if (item.getItemId() == R.id.action_about) {
            // Handle About action
            Toast.makeText(this, "About Us", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;

        }
        else if (item.getItemId() == R.id.action_location) {
            // Handle Location action
            Toast.makeText(this, "About You", Toast.LENGTH_SHORT).show();

            // Retrieve the state and address from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("location_data", MODE_PRIVATE);
            String state = sharedPreferences.getString("state", "tamil nadu");
            String address = sharedPreferences.getString("address", "Chennai, Tamil Nadu, India");

            // Create an Intent to pass data to AboutLocation activity
            Intent intent = new Intent(this, AboutLocation.class);
            intent.putExtra("state", state); // Pass state information
            intent.putExtra("address", address); // Pass full address
            startActivity(intent);

            return true;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Hide Google Sign-In button after successful sign-in
                            findViewById(R.id.signIn).setVisibility(View.GONE);
                            // Display username at top-right corner
                            String displayName = user.getDisplayName();
                            Toast.makeText(MainActivity.this, "Welcome, " + displayName, Toast.LENGTH_SHORT).show();

                            departmentSpinner.setVisibility(View.VISIBLE);
                            semesterSpinner.setVisibility(View.VISIBLE);

                            findViewById(R.id.departmentLabel).setVisibility(View.VISIBLE);
                            findViewById(R.id.semesterLabel).setVisibility(View.VISIBLE);
                            submitDataButton.setVisibility(View.VISIBLE);

                            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(semesterSpinner, "alpha", 0f, 1f);
                            fadeIn.setDuration(1000);
                            fadeIn.start();
                            ObjectAnimator fadeInn = ObjectAnimator.ofFloat(departmentSpinner, "alpha", 0f, 1f);
                            fadeInn.setDuration(1000);
                            fadeInn.start();


                            ObjectAnimator scaleX = ObjectAnimator.ofFloat(submitDataButton, "scaleX", 1f, 1.1f, 1f);
                            ObjectAnimator scaleY = ObjectAnimator.ofFloat(submitDataButton, "scaleY", 1f, 1.1f, 1f);
                            scaleX.setDuration(1000);
                            scaleY.setDuration(1000);
                            scaleX.setRepeatCount(ObjectAnimator.INFINITE);
                            scaleY.setRepeatCount(ObjectAnimator.INFINITE);
                            scaleX.start();
                            scaleY.start();


                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void adduser(){
        department = departmentSpinner.getSelectedItem().toString();
        semester = semesterSpinner.getSelectedItem().toString();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        if (currentUser != null) {
            // Get the UID and displayName of the current user
            String userId = currentUser.getUid();
            String username = currentUser.getDisplayName(); // Fetch the username

            // Check if displayName is null (handle if not set during signup)
            if (username == null || username.isEmpty()) {
                username = "Anonymous"; // Default to "Anonymous" if username is not set
            }
            // Create a map of the user data
            Map<String, Object> user = new HashMap<>();
            user.put("username", username);
            user.put("department", department);
            user.put("semester", semester);

            // Reference Firestore instance
            // Store the data in Firestore
            db.collection("users")
                    .document(userId) // Set document ID as user's UID
                    .set(user)        // Save the data into Firestore
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "User data written successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error writing user data", e);
                    });
        } else {
            Log.e("FirebaseAuth", "No authenticated user found.");
        }
    }


    private void submitData() {
        // Get selected department and semester
        department = departmentSpinner.getSelectedItem().toString();
        semester = semesterSpinner.getSelectedItem().toString();




        adduser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (!department.isEmpty() && !semester.isEmpty()) {
            String semesterDocument = "Semester " + semester;

            // Fetch the courses for the selected department and semester
            db.collection(department)
                    .document(semesterDocument)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Hide UI elements
                            departmentSpinner.setVisibility(View.GONE);
                            semesterSpinner.setVisibility(View.GONE);
                            submitDataButton.setVisibility(View.GONE);
                            findViewById(R.id.departmentLabel).setVisibility(View.GONE);
                            findViewById(R.id.semesterLabel).setVisibility(View.GONE);
                            appname.setVisibility(View.GONE);

                            // Get the courses data
                            Map<String, Object> courses = documentSnapshot.getData();
                            if (courses != null) {
                                // Create a container layout for the courses if it doesn't exist
                                LinearLayout courseContainer = findViewById(R.id.courseDetailsLayout);
                                courseContainer.setVisibility(View.VISIBLE);
                                courseLayout.setVisibility(View.VISIBLE);// Show the course details layout
                                courseContainer.removeAllViews();  // Clears existing courses

                                // Iterate through the courses and add each one dynamically
                                for (Map.Entry<String, Object> entry : courses.entrySet()) {
                                    // Get the course name and details (e.g., course name and credits)
                                    String courseName = entry.getKey();
                                    String courseDetails = entry.getValue().toString();

                                    // Add a new course dynamically
                                    addCourse(courseName, courseDetails);
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "No courses found for this semester.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Error fetching courses.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(MainActivity.this, "Please select department and semester.", Toast.LENGTH_SHORT).show();
        }
    }


    // Declare a HashMap to keep track of whether the email has been sent for each course
    private Map<String, Boolean> emailSentMap = new HashMap<>();

    private void addCourse(String courseName, String courseDetails) {
        // Create a new course layout by inflating the activity_course layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View courseView = inflater.inflate(R.layout.activity_course, null);

        // Initialize course views
        TextView courseNameTextView = courseView.findViewById(R.id.courseName);
        TextView hoursMissedTextView = courseView.findViewById(R.id.hoursMissed);
        TextView attendanceTextView = courseView.findViewById(R.id.attendance);
        TextView skipsLeftTextView = courseView.findViewById(R.id.skipsLeft);

        // Set the course details
        courseNameTextView.setText(courseName); // Set course name
        hoursMissedTextView.setText("Hours Missed: 0"); // Default missed hours
        attendanceTextView.setText("Attendance: 100%"); // Default attendance

        // Default skips left calculation
        int totalHours = getTotalHours(courseName, courseDetails); // Calculate total hours based on course credits or type
        int skipsLeft = (int) (totalHours * 0.25); // Calculate skips left as 25% of total hours

        skipsLeftTextView.setText("Skips Left: " + skipsLeft);

        // Add increment and decrement logic for hours missed
        Button incrementButton = courseView.findViewById(R.id.incrementButton);
        Button decrementButton = courseView.findViewById(R.id.decrementButton);

        incrementButton.setOnClickListener(v -> {
            int currentHours = Integer.parseInt(hoursMissedTextView.getText().toString().split(": ")[1]);
            if (currentHours < totalHours) {
                // Update missed hours
                hoursMissedTextView.setText("Hours Missed: " + (currentHours + 1));

                // Calculate remaining skips left dynamically based on hours missed
                int updatedSkipsLeft = skipsLeft - (currentHours + 1);
                skipsLeftTextView.setText("Skips Left: " + updatedSkipsLeft);

                // Update attendance percentage dynamically
                double attendancePercentage = ((totalHours - (currentHours + 1)) / (double) totalHours) * 100;
                attendanceTextView.setText(String.format("Attendance: %.2f%%", attendancePercentage));

                // Check if attendance is below 75% and email has not been sent for this course yet
                if (attendancePercentage < 75 && !emailSentMap.containsKey(courseName)) {
                    // Call the email sending function
                    String toEmail = username; // student's email address
                    Toast.makeText(MainActivity.this, "Your Attendance is below 75% in "+courseName, Toast.LENGTH_SHORT).show();

                    // Mark that the email has been sent for this course
                    emailSentMap.put(courseName, true);

                    // Send the email
                    String subject = "Low Attendance Alert";
                    String body = "Dear Student,\n\nYour attendance percentage is below 75% in "+courseName+". Please make sure to attend your classes regularly.\n\nBest Regards,\nAttendify";
                    sendEmail(toEmail, subject, body);
                    Toast.makeText(MainActivity.this, "Low attendance email sent.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        decrementButton.setOnClickListener(v -> {
            int currentHours = Integer.parseInt(hoursMissedTextView.getText().toString().split(": ")[1]);
            if (currentHours > 0) {
                // Update missed hours
                hoursMissedTextView.setText("Hours Missed: " + (currentHours - 1));

                // Calculate remaining skips left dynamically based on hours missed
                int updatedSkipsLeft = skipsLeft - (currentHours - 1);
                skipsLeftTextView.setText("Skips Left: " + updatedSkipsLeft);

                // Update attendance percentage dynamically
                double attendancePercentage = ((totalHours - (currentHours - 1)) / (double) totalHours) * 100;
                attendanceTextView.setText(String.format("Attendance: %.2f%%", attendancePercentage));


            }
        });

        // Add the dynamically created course view to the course container
        LinearLayout courseContainer = findViewById(R.id.courseDetailsLayout);
        courseContainer.addView(courseView);
    }




    // Function to get total hours based on course type
    private int getTotalHours(String courseName, String coursedetails) {
        // Default total hours
        int totalHours = 0;

        // Check for course credits or name to determine total hours
        if (courseName.contains("Laboratory")) {
            totalHours = 45; // Laboratory course has 45 total hours
        } else if (coursedetails.contains("3")) {
            totalHours = 45; // 3 Credit courses have 45 hours
        } else if (coursedetails.contains("4")) {
            totalHours = 56; // 4 Credit courses have 56 hours
        } else if (coursedetails.contains("2")) {
            totalHours = 37; // 2 Credit courses have 37 hours
        }
        return totalHours;
    }

    public void sendEmail(String recipientEmail, String subject, String message) {

        // Run email sending in a background thread
        new Thread(() -> {
            try {
                // Sender's email and password should be securely managed, not hardcoded
                String senderEmail = "jpmytreyan@gmail.com"; // Secure this
                String senderPassword = "hmrt noql mgvl objf";    // Secure this

                // Call the EmailSender to send email
                EmailSender.sendEmail(senderEmail, senderPassword, recipientEmail, subject, message);
                Log.d("Email", "Email sent successfully!");
            } catch (Exception e) {
                // Handle any exceptions that occur during email sending
                Log.e("Email", "Failed to send email", e);
            }
        }).start();
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Called when the location changes
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Get the state (admin area) from the address
                String state = address.getAdminArea();
                String fullAddress = address.getAddressLine(0);

                // Save address and state to SharedPreferences or a class variable
                SharedPreferences sharedPreferences = getSharedPreferences("location_data", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("state", state);
                editor.putString("address", fullAddress);
                editor.apply();

            } else {
                Toast.makeText(this, "Unable to retrieve address", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start requesting location updates
                startLocationUpdates();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied, cannot access location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop receiving location updates when the activity is paused
        locationManager.removeUpdates(this);
    }
}
