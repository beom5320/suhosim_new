package nodomain.team3point1.suhosim.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;

import android.content.Intent;
import android.widget.TextView;
import android.os.Bundle;

import nodomain.team3point1.suhosim.R;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AboutYours extends AppCompatActivity {
    private Button medicalHisoty;
    private EditText send;
    private Spinner rhspinner;
    private Spinner bloodspinner;
    private Button save;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_yours);

        final List<String> selectedItems = new ArrayList<String>();


        //병력사항
        medicalHisoty = (Button) findViewById(R.id.medicalSelect);


        medicalHisoty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] items = new String[]{"고혈압", "당뇨", "결핵", "간염", "심장질환", "알러지", "천식", "심부전증", "폐렴", "디스크", "간경화", "관절염", "협심증", "암", "갑상선염", "고지혈증", "골다공증", "과민성대장", "간암", "기관지염", "뇌졸증", "신장실환", "기타질환", "없음"};

                AlertDialog.Builder dialog = new AlertDialog.Builder(AboutYours.this);
                dialog.setTitle("현재 질환")
                        .setMultiChoiceItems(items,

                                new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},

                                new DialogInterface.OnMultiChoiceClickListener() {
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        if (isChecked) {

                                            selectedItems.add(items[which]);
                                        } else {
                                            selectedItems.remove(items[which]);
                                        }
                                    }
                                })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                String items = "";
                                for (String selectedItem : selectedItems) {
                                    items += (selectedItem + ", ");
                                }

                                selectedItems.clear();

                                items = items.substring(0, items.length() - 2);
                                send = (EditText) findViewById(R.id.userMedicalHistory);
                                send.setText(items);
                            }


                        }).create().show();


            }
        });


        //RH 타입
        String[] rhlist = getResources().getStringArray(R.array.rhSelect);
        rhspinner = (Spinner) findViewById(R.id.rhset);
        ArrayAdapter rhadapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, rhlist);
        rhadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rhspinner.setAdapter(rhadapter);

        rhspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //blood 타입
        final String[] bloodlist = getResources().getStringArray(R.array.bloodSelect);
        bloodspinner = (Spinner) findViewById(R.id.bloodset);
        ArrayAdapter bloodadapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, bloodlist);
        bloodadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodspinner.setAdapter(bloodadapter);

        bloodspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        save=(Button)findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),ControlCenterv2.class);
                startActivity(intent);
            }
        });

    }





}

