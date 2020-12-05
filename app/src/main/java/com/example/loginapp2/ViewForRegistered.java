package com.example.loginapp2;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ViewForRegistered extends AppCompatActivity {
    private String user = "";
    private String password = "";
    private String url = "";
    private String classes = "";
    boolean stopThread = false;
    Connection connection = null;
    MyRecyclerViewAdapter adapter;
    ArrayList<String> secretNames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_for_registered);
        Intent i = getIntent();
        user = i.getStringExtra("user");
        password = i.getStringExtra("password");
        url = i.getStringExtra("url");
        classes = i.getStringExtra("classes");



        String sql1 = "Select * from CUSTOMERS;";
        checkInDb cid = new checkInDb(sql1);
        new Thread(cid).start();


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

                while (rs.next()) {

                    String temp = "id: " + rs.getInt("ID") + " | " + "Name: " + rs.getString("NAME") + " | " + "age: " + rs.getInt("AGE") + " | " + "Adress: " + rs.getString("ADDRESS");


                    secretNames.add(temp);


                }
                rs.close();
                connection.close();




            } catch (ClassNotFoundException | SQLException e) {


                e.printStackTrace();
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    RecyclerView recyclerView = findViewById(R.id.secretTabela);

                    recyclerView.setLayoutManager(new LinearLayoutManager(ViewForRegistered.this));
                    adapter = new MyRecyclerViewAdapter(ViewForRegistered.this, secretNames);

                    recyclerView.setAdapter(adapter);

                }
            });
            //success
            ;


        }
    }


}