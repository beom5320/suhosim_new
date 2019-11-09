package nodomain.team3point1.suhosim.activities;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;

import nodomain.team3point1.suhosim.R;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;


import android.content.SharedPreferences;
import android.widget.Toast;


public class AboutYours extends AppCompatActivity {
    private Button medicalHisoty;
    private EditText send;
    private Spinner rhspinner;
    private Spinner bloodspinner;
    private Button save;
    private String bloodS;
    private SharedPreferences user;
    private SharedPreferences.Editor editor;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_yours);

        //////////////////////////병력사항 선택 시작//////////////////////////
        final List<String> selectedItems = new ArrayList<String>();
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
//////////////////////////병력사항 선택 끄읏//////////////////////////

        ////////////////////RH 타입 시작////////////////////
        String[] rhlist = getResources().getStringArray(R.array.rhSelect);
        rhspinner = (Spinner) findViewById(R.id.rhset);
        ArrayAdapter rhadapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, rhlist);
        rhadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rhspinner.setAdapter(rhadapter);

        rhspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bloodS = rhspinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
////////////////////RH 타입 끄읏////////////////////

        ///////////////////////blood 타입 시작///////////////////////////
        final String[] bloodlist = getResources().getStringArray(R.array.bloodSelect);
        bloodspinner = (Spinner) findViewById(R.id.bloodset);
        ArrayAdapter bloodadapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, bloodlist);
        bloodadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodspinner.setAdapter(bloodadapter);

        bloodspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bloodS += " " + bloodspinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
///////////////////////blood 타입 끄읏///////////////////////////


        //////저장 눌렀을 때////////
        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                user = getSharedPreferences("User", MODE_PRIVATE);

                editor = user.edit();

                EditText nameE = (EditText) findViewById(R.id.username);
                String nameS = nameE.getText().toString();
                editor.putString("name", nameS);

                EditText birthE = (EditText) findViewById(R.id.userbirth);
                String birthS = birthE.getText().toString();
                editor.putString("birth", birthS);

                EditText addressE = (EditText) findViewById(R.id.useraddress);
                String addressS = addressE.getText().toString();
                editor.putString("address", addressS);

                RadioGroup radioGroup= (RadioGroup) findViewById(R.id.usergender);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        switch (i) {
                            case R.id.female:
                                editor.putString("gender", "여성");
                                break;
                            case R.id.male:
                                editor.putString("gender", "남성");
                                break;
                        }
                    }
                });

                editor.putString("blood", bloodS);

                EditText medicalE = (EditText) findViewById(R.id.userMedicalHistory);
                String medicalS = medicalE.getText().toString();
                editor.putString("medical", medicalS);

                EditText drugE = (EditText) findViewById(R.id.userdrug);
                String drugS = drugE.getText().toString();
                editor.putString("drug", drugS);

                EditText surgeryE = (EditText) findViewById(R.id.usersurgery);
                String surgeryS = surgeryE.getText().toString();
                editor.putString("surgery", surgeryS);

                EditText phoneE = (EditText) findViewById(R.id.guardianphone);
                String phoneS = phoneE.getText().toString();
                editor.putString("phone", phoneS);
                editor.commit();


                boolean check;
                String name = user.getString("name", null);

                String birth = user.getString("birth", null);

                String address = user.getString("address", null);

                String gender = user.getString("gender", null);

                String blood = user.getString("blood", null);

                String number = user.getString("phone", null);

                Toast.makeText(getApplicationContext(), gender, Toast.LENGTH_LONG).show();

               if (TextUtils.isEmpty(name) || TextUtils.isEmpty(birth) || TextUtils.isEmpty(address) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(blood) || TextUtils.isEmpty(number))
                    check = false;
                else
                    check = true;
                //메인 화면으로 넘어가기 & 뒤로가기 제거
              /* if (!check) {
                    Toast.makeText(getApplicationContext(), "빈 필수사항이 있습니다.", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), ControlCenterv2.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                }*/

            }

        });


    }


}

