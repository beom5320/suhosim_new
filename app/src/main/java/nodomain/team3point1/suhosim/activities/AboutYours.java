package nodomain.team3point1.suhosim.activities;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;

import nodomain.team3point1.suhosim.GBApplication;
import nodomain.team3point1.suhosim.R;
import nodomain.team3point1.suhosim.util.Prefs;

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
    private EditText nameE;
    private EditText birthE;
    private EditText addressE;
    private EditText medicalE;
    private EditText drugE;
    private EditText surgeryE;
    private EditText phoneE;
    private int rh;
    private int type;


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
                rh = rhspinner.getSelectedItemPosition();
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
                type = bloodspinner.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
///////////////////////blood 타입 끄읏///////////////////////////
        user = getSharedPreferences("User", MODE_PRIVATE);
        nameE = (EditText) findViewById(R.id.username);
        birthE = (EditText) findViewById(R.id.userbirth);
        addressE = (EditText) findViewById(R.id.useraddress);
        medicalE = (EditText) findViewById(R.id.userMedicalHistory);
        drugE = (EditText) findViewById(R.id.userdrug);
        surgeryE = (EditText) findViewById(R.id.usersurgery);
        phoneE = (EditText) findViewById(R.id.guardianphone);
        Boolean savecheck = false;

        //수정 누를 시
        savecheck = user.getBoolean("savecheck", false);
        if (savecheck) {
            String nameS = user.getString("name", null);
            nameE.setText(nameS);

            String birthS = user.getString("birth", null);
            birthE.setText(birthS);

            String addressS = user.getString("address", null);
            addressE.setText(addressS);

            RadioButton fb = (RadioButton) findViewById(R.id.female);
            RadioButton mb = (RadioButton) findViewById(R.id.male);

            String genderS = user.getString("gender", null);
            if (genderS.equals("여성"))
                fb.setChecked(true);
            else if (genderS.equals("남성"))
                mb.setChecked(true);

            int rhI = user.getInt("rh", -1);
            rhspinner.setSelection(rhI);
            int typeI = user.getInt("type", -1);
            bloodspinner.setSelection(typeI);

            String medicalS = user.getString("medical", null);
            medicalE.setText(medicalS);

            String drugS = user.getString("drug", null);
            drugE.setText(drugS);

            String surgeryS = user.getString("surgery", null);
            surgeryE.setText(surgeryS);

            String numberS = user.getString("phone", null);
            phoneE.setText(numberS);
        }

        //////저장 눌렀을 때////////
        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor = user.edit();

                String nameS = nameE.getText().toString();
                editor.putString("name", nameS);

                final String birthS = birthE.getText().toString();
                editor.putString("birth", birthS);

                final String addressS = addressE.getText().toString();
                editor.putString("address", addressS);

                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.usergender);
                if (radioGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getApplicationContext(), "빈 필수사항이 있습니다.", Toast.LENGTH_LONG).show();
                } else {
                    RadioButton selectedB = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
                    int ra = radioGroup.getCheckedRadioButtonId();
                    String selected = selectedB.getText().toString();
                    editor.putString("gender", selected);
                }


                editor.putInt("rh", rh);
                editor.putInt("type", type);
                bloodS = rhspinner.getSelectedItem().toString() + " " + bloodspinner.getSelectedItem().toString();
                editor.putString("blood", bloodS);

                String medicalS = medicalE.getText().toString();
                editor.putString("medical", medicalS);

                String drugS = drugE.getText().toString();
                editor.putString("drug", drugS);

                String surgeryS = surgeryE.getText().toString();
                editor.putString("surgery", surgeryS);

                String phoneS = phoneE.getText().toString();
                editor.putString("phone", phoneS);

                editor.putBoolean("savecheck", true);
                editor.commit();

                boolean check;
                String name = user.getString("name", null);
                String birth = user.getString("birth", null);
                String address = user.getString("address", null);
                String gender = user.getString("gender", null);
                String blood = user.getString("blood", null);
                String number = user.getString("phone", null);

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(birth) || TextUtils.isEmpty(address) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(blood) || TextUtils.isEmpty(number))
                    check = false;
                else
                    check = true;

                //화면전환 & 뒤로가기 제거
                Prefs prefs = GBApplication.getPrefs();
                if (!check) {
                    Toast.makeText(getApplicationContext(), "빈 필수사항이 있습니다.", Toast.LENGTH_LONG).show();
                } else if (prefs.getBoolean("firstrun", true)) {

                    Intent intent = new Intent(getApplicationContext(), ControlCenterv2.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                    prefs.getPreferences().edit().putBoolean("firstrun", false).apply();
                } else {
                    Intent intent = new Intent(getApplicationContext(), AboutYoursSave.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }


            }

        });


    }


}

