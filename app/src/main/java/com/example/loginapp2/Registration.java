package com.example.loginapp2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registration extends AppCompatActivity {

    private String user = "";
    private String password = "";
    private String url = "";
    private String classes = "";
    Connection connection = null;
    Hash hash = new Hash();
    EditText personNameEditText;
    EditText personPasswordEditText;
    EditText emailEditText;
    Button regButton;
    private volatile boolean stopThread = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Intent i = getIntent();
        user = i.getStringExtra("user");
        password = i.getStringExtra("password");
        url = i.getStringExtra("url");
        classes = i.getStringExtra("classes");

        personNameEditText = findViewById(R.id.editTextTextPersonName);
        personPasswordEditText = findViewById(R.id.editTextTextPassword);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        regButton = findViewById(R.id.regButon);
    }

    public void addUserToDB(View view) throws UnsupportedEncodingException, NoSuchAlgorithmException {


        // Getting data from text views


        String personName = personNameEditText.getText().toString();
        String personPassword = personPasswordEditText.getText().toString();
        String personEmail = emailEditText.getText().toString();


        //Checking generally if data is correct
        //if something is empty
        if (TextUtils.isEmpty(personName)) {
            personNameEditText.setError("please give login");

            return;
        } else if (TextUtils.isEmpty(personPassword)) {
            personPasswordEditText.setError("please give password");
            return;
        } else if (TextUtils.isEmpty(personEmail)) {
            emailEditText.setError("please give email");
            return;
        } else if (personPassword.length() < 5) {
            //if password is short

            personPasswordEditText.setError("password too short, min 5 symbols");
            return;
        } else {

            //email veritifaction
            String emailVerification = personEmail;
            Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
            Matcher mat = pattern.matcher(emailVerification);

            if (mat.matches() == false) {
                // if email if email

                emailEditText.setError("not valid email");

                return;
            } else {


                //hashing sensitive data
                String hashedLogin = hash.getHash(personName);
                String hashedUserPassword = hash.getHash(personPassword);

                String sqlQuerry = "INSERT into Logowanie (login, password, email) values (\"" + hashedLogin + "\" , \"" + hashedUserPassword + "\" , \"" + personEmail + "\");";

                AddToDb runnable = new AddToDb(sqlQuerry);
                new Thread(runnable).start();


            }
        }
    }


    public void stopThread() {
        stopThread = true;
    }

// to powinno być zmienione na metode Handler Looper
    // ale działa :)
    class AddToDb implements Runnable {

        String sqlQuerry;

        public AddToDb(String sqlQuerry) {
            this.sqlQuerry = sqlQuerry;
        }

        @Override
        public void run() {

            if (stopThread) return;


            //connecting to DB and adding user
            // tO powinno być poporzez aplikacje webową aby nie dawać hasla do aplikacji.
            try {
                Class.forName(classes);
                connection = DriverManager.getConnection(url, user, password);
                Statement statement = connection.createStatement();
                statement.executeUpdate(sqlQuerry);

                connection.close();


            } catch (ClassNotFoundException | SQLException e) {


                e.printStackTrace();
                return;
            }


            //success
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    personNameEditText.setText("");
                    personPasswordEditText.setText("");
                    emailEditText.setText("");

                    regButton.setClickable(false);
                    regButton.setText("Success");

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();   // to powinno być zmienione na Broadcast reciever


                }
            });


        }
    }

}

















