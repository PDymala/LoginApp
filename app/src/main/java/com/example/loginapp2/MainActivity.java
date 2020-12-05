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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * LoginApp2 is a basic application that interacts with MySql database (google cloud sql).
 * Main login activity checks if given username and password exist in data base.
 * Registration activity add the user to database after hashing his personal information
 * Activity which shows after successful login, shows personal information from a database (different one that is used for login)
 * <p>
 * Note that TCP-IP connection to data base is not the most secure way of handling sensitive information.
 * Application requires all passwords etc which can be back-engineered. Should create a web application which
 * transfer information from mobile app and database and other way round.
 * <p>
 * The concurrency is done with Threads, Runnables and runOnUiThread. More appropriate way is to work with Handler Looper
 *
 * @author Piotr Dymala p.dymala@gmail.com
 * @version 1.0
 * @since 2020-22-27
 */
public class MainActivity extends AppCompatActivity {
    /*
    If you may use a different database than MySql, have in mind to check all below information and Gradle implementation.
    Check https://www.petefreitag.com/articles/jdbc_urls/#mysql for more information on different data bases.
    Remember to allow yourself for a connection in your database settings.

DB Preperation before app

create table Logowanie (
   id INT NOT NULL AUTO_INCREMENT,
   login VARCHAR(100) NOT NULL,
   email varchar(100) NOT NULL,
   PRIMARY KEY ( id )
   );

(adding users is from app level)

CREATE TABLE CUSTOMERS(
    ID INT  NOT NULL,
    NAME VARCHAR (20) NOT NULL,
    AGE  INT NOT NULL,
    ADDRESS  CHAR (25) ,
    PRIMARY KEY (ID) )

Insert into CUSTOMERS (ID, NAME, AGE, ADDRESS) value (1, "Dominique Persona", 31, " 41 Helms st");
Insert into CUSTOMERS (ID, NAME, AGE, ADDRESS) value (2, "Anna Unknowna", 32, " 1 Rottendam st");
Insert into CUSTOMERS (ID, NAME, AGE, ADDRESS) value (3, "Peter Moscowski", 31, "1/2 Sybir st");
Insert into CUSTOMERS (ID, NAME, AGE, ADDRESS) value (4, "Danny Treiko", 60, " Cosmos");
Insert into CUSTOMERS (ID, NAME, AGE, ADDRESS) value (5, "Peter Pan", 74, " 56 Downing st");

     */
    private String ip = ""; // server IP
    private String port = "";  // server port. For MySql is usually 3306
    private String classes = "com.mysql.jdbc.Driver";  // may be different if not mysql
    private String database = ""; //  your database name
    private String user = ""; // your username to database
    private String password = ""; // your password to database
    private String url = "jdbc:mysql://" + ip + ":" + port + "/" + database;
    Connection connection = null;
    Hash hash = new Hash();
    EditText personNameEditText;
    EditText personPasswordEditText;
    Button regButton;
    Button logInButton;
    private volatile boolean stopThread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        personNameEditText = findViewById(R.id.editTextTextPersonName);
        personPasswordEditText = findViewById(R.id.editTextTextPassword);
        regButton = findViewById(R.id.regButon);
        logInButton = findViewById(R.id.logInButton);


    }

    public void registrationActivity(View v) {

        Intent i = new Intent(this, Registration.class);
        i.putExtra("user", user);
        i.putExtra("password", password);
        i.putExtra("url", url);
        i.putExtra("classes", classes);
        startActivity(i);


    }

    public void logIn(View v) throws UnsupportedEncodingException, NoSuchAlgorithmException {

//data ini
        String personName = personNameEditText.getText().toString();
        String personPassword = personPasswordEditText.getText().toString();

        //data check

        if (TextUtils.isEmpty(personName)) {
            personNameEditText.setError("please give login");

            return;
        } else if (TextUtils.isEmpty(personPassword)) {
            personPasswordEditText.setError("please give password");
            return;

        } else {

            //hashing stuff

            //hashing sensitive data
            String hashedLogin = hash.getHash(personName);
            String hashedUserPassword = hash.getHash(personPassword);


            String sql1 = "SELECT id FROM Logowanie WHERE login = '" + hashedLogin + "' AND password = " + "'" + hashedUserPassword + "';";
            checkInDb cid = new checkInDb(sql1);
            new Thread(cid).start();


        }


    }


    public void stopThread() {
        stopThread = true;
    }

    // to powinno być zmienione na metode Handler Looper
    // ale działa :)
    class checkInDb implements Runnable {

        String sqlQuerry;
        boolean isInDb;

        public boolean isInDb() {
            return isInDb;
        }

        public checkInDb(String sqlQuerry) {
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


                ResultSet rs = statement.executeQuery(sqlQuerry);

                if (!rs.next()) {
                    rs.close();
                    connection.close();


                } else {
                    rs.close();
                    connection.close();

                    Intent o = new Intent(MainActivity.this, ViewForRegistered.class);
                    o.putExtra("user", user);
                    o.putExtra("password", password);
                    o.putExtra("url", url);
                    o.putExtra("classes", classes);

                    startActivity(o);


                }


                connection.close();


            } catch (ClassNotFoundException | SQLException e) {


                e.printStackTrace();
                return;
            }


            //success
            ;


        }
    }


}