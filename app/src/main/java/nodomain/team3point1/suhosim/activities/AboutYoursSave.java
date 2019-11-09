package nodomain.team3point1.suhosim.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import nodomain.team3point1.suhosim.R;

public class AboutYoursSave extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_yours_save);
        SharedPreferences user = getSharedPreferences("User", MODE_PRIVATE);

        String name = user.getString("name", null);
        TextView uname = (TextView) findViewById(R.id.username);
        uname.setText(name);

        String birth = user.getString("birth", null);
        TextView ubirth = (TextView) findViewById(R.id.userbirthday);
        ubirth.setText(birth);

        String address = user.getString("address", null);
        TextView uaddress = (TextView) findViewById(R.id.useraddress);
        uaddress.setText(address);

        String gender = user.getString("gender", null);
        TextView ugender = (TextView) findViewById(R.id.usergender);
        ugender.setText(gender);

        String blood = user.getString("blood", null);
        TextView ublood = (TextView) findViewById(R.id.userblood);
        ublood.setText(blood);

        String medical = user.getString("medical", null);
        TextView umedical = (TextView) findViewById(R.id.userMedical);
        umedical.setText(medical);

        String drug = user.getString("drug", null);
        TextView udrug = (TextView) findViewById(R.id.userdrug);
        udrug.setText(drug);

        String surgery = user.getString("surgery", null);
        TextView usurgery = (TextView) findViewById(R.id.usersurgery);
        usurgery.setText(surgery);

        String number = user.getString("phone", null);
        TextView unumber = (TextView) findViewById(R.id.guardianphone);
        unumber.setText(number);


    }
}
