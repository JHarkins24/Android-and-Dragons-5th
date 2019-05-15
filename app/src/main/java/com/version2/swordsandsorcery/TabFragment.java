package com.version2.swordsandsorcery;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.AdapterView.OnItemSelectedListener;
import com.version2.swordsandsorcery.Database.CharacterBaseHelper;
import com.version2.swordsandsorcery.Database.CharacterDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.content.res.AssetManager;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.bouncycastle.util.Strings;
import org.w3c.dom.CharacterData;

import static com.version2.swordsandsorcery.Database.CharacterDB.CharacterTable.CharactersColumns.*;


public class TabFragment extends Fragment {
    ArrayList<String> equip;
    private SharedPreferences abilityScorePreferences;
    private SharedPreferences selectionAbilityScorePreference;
    int position;
    static CharacterDB character;
    final int POINT_BUY_MAX = 15;
    final int POINT_BUY_MIN = 8;
    final int POINT_BUY_MIDDLE = 13;
    short bla = 0;
    String selectionAbilityScore;
    File pdf = null;
    Intent pdfIntent = null;
    boolean permissionChecked = false;
    boolean permissionGranted = true;

    public static Fragment getInstance(CharacterDB newCharacter, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        TabFragment tabFragment = new TabFragment();
        tabFragment.setArguments(bundle);
        character = newCharacter;
        return tabFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        switch (position){
            case 0:

                return inflater.inflate(R.layout.fragment_character_creation_overview, container, false);
            case 1:
                selectionAbilityScorePreference = PreferenceManager.getDefaultSharedPreferences(this.getContext());
                String ability = selectionAbilityScorePreference.getString("abilityScore", "");
                switch(ability){
                    case"Point Buy":
                        return inflater.inflate(R.layout.fragment_character_creation_ability_scores_point_buy,container, false);
                    case "Roll":
                        return inflater.inflate(R.layout.fragment_character_creation_ability_scores_roll,container,false);
                    case "Manual":
                        return inflater.inflate(R.layout.fragment_character_creation_ability_scores_manual, container, false);
                }
                return inflater.inflate(R.layout.fragment_character_creation_ability_scores_manual, container, false);
            case 2:
                return inflater.inflate(R.layout.fragment_character_creation_items, container, false);
            case 3:
                return inflater.inflate(R.layout.fragment_character_creation_spells, container, false);
            case 4:
                return inflater.inflate(R.layout.fragment_character_creation_view, container, false);
        }
        return inflater.inflate(R.layout.fragment_character_creation_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        switch (position){
            case 0: {
                // spinner is implemented dynamically in the java activity file.
                final EditText name = view.findViewById(R.id.characterName);
                if(!character.getName().equals("")){
                    name.setText(character.getName());
                }
                name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        character.setName(name.getText().toString());
                    }
                });
                final Spinner lvlSpinner = (Spinner) view.findViewById(R.id.lvl_spinner);
                LinkedList<String> levels = new LinkedList<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"));
                // create arrayAdapter using the string array and a default
                ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, levels);
                lvlSpinner.setAdapter(levelAdapter);
                int lvlSpinnerPosition = 0;
                if (character.getLvl() == 0) {
                    lvlSpinnerPosition = levelAdapter.getPosition("1");
                    lvlSpinner.setSelection(lvlSpinnerPosition);
                } else {
                    lvlSpinnerPosition = levelAdapter.getPosition(Integer.toString(character.getLvl()));
                    lvlSpinner.setSelection(lvlSpinnerPosition);
                }


                lvlSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.v("lvl", (String) parent.getItemAtPosition(position));
                        character.setLvl(Integer.parseInt((String) lvlSpinner.getSelectedItem()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // auto generated program stub will set the initial to the object at index 0,
                        // could we make it so that there is some kind of interface between the settings
                        // screen and the drop down interface here? Boolean?
                    }
                });

                final Spinner classSpinner = (Spinner) view.findViewById(R.id.class_spinner);
                LinkedList<String> classes = new LinkedList<>(Arrays.asList("barbarian", "bard", "cleric", "druid", "fighter", "monk", "paladin", "ranger", "rogue", "sorcerer", "warlock", "wizard"));
                ArrayAdapter<String> classAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, classes);
                classSpinner.setAdapter(classAdapter);
                int classSpinnerPosition = 0;
                String s;
                if (character.getClassName().equals("")) {
                    classSpinnerPosition = classAdapter.getPosition("barbarian");
                    classSpinner.setSelection(classSpinnerPosition);
                } else {
                    classSpinnerPosition = classAdapter.getPosition(character.getClassName());
                    classSpinner.setSelection(classSpinnerPosition);
                    s = (String)classSpinner.getItemAtPosition(classSpinnerPosition);
                }

                classSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.v("class", (String) parent.getItemAtPosition(position));
                        character.setClassName((String) classSpinner.getSelectedItem());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // auto generated program stub will set the initial to the object at index 0,
                        // could we make it so that there is some kind of interface between the settings
                        // screen and the drop down interface here? Boolean?
                    }
                });
                final Spinner raceSpinner = (Spinner) view.findViewById(R.id.raceSpinner);
                LinkedList<String> racees = new LinkedList<>(Arrays.asList("dragonborn", "dwarf", "elf", "gnome", "half-elf", "halfing", "half-orc", "human", "tiefling"));
                ArrayAdapter<String> raceAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, racees);
                raceSpinner.setAdapter(raceAdapter);
                int raceSpinnerPosition = 0;
                if (character.getRace().equals("")) {
                    raceSpinnerPosition = raceAdapter.getPosition("dragonborn");
                    raceSpinner.setSelection(raceSpinnerPosition);
                } else {
                    raceSpinnerPosition = raceAdapter.getPosition(character.getRace());
                    raceSpinner.setSelection(raceSpinnerPosition);
                }


                raceSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.v("race", (String) parent.getItemAtPosition(position));
                        character.setRace((String) raceSpinner.getSelectedItem());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // auto generated program stub will set the initial to the object at index 0,
                        // could we make it so that there is some kind of interface between the settings
                        // screen and the drop down interface here? Boolean?
                    }
                });

                final Spinner backgroundSpinner = (Spinner) view.findViewById(R.id.backgroundSpinner);
                LinkedList<String> backgrounds = new LinkedList<>(Arrays.asList("acolyte", "charlatan", "criminal", "entertainer", "folk hero", "guild artisan", "hermit", "noble", "outlander", "sage", "sailor", "soldier", "urchin"));
                ArrayAdapter<String> backgroundAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, backgrounds);
                backgroundSpinner.setAdapter(backgroundAdapter);
                int bckgrndSpinnerPosition = 0;
                if (character.getBackground().equals("")){
                    bckgrndSpinnerPosition = backgroundAdapter.getPosition("acolyte");
                    backgroundSpinner.setSelection(bckgrndSpinnerPosition);
                }else{
                    bckgrndSpinnerPosition = backgroundAdapter.getPosition(character.getBackground());
                    backgroundSpinner.setSelection(bckgrndSpinnerPosition);
                }



                backgroundSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.v("background", (String) parent.getItemAtPosition(position));
                        character.setBackground((String)backgroundSpinner.getSelectedItem());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // auto generated program stub will set the initial to the object at index 0,
                        // could we make it so that there is some kind of interface between the settings
                        // screen and the drop down interface here? Boolean?
                    }
                });

            }
                break;
            case 1: {


                final TextView rollType = view.findViewById(R.id.rollType);
                selectionAbilityScorePreference = PreferenceManager.getDefaultSharedPreferences(this.getContext());
                String ability = selectionAbilityScorePreference.getString("abilityScore", "");
                if (ability != null) {
                    switch (ability) {

                        case "Point Buy": {

                            final TextView pointBuy = view.findViewById(R.id.pointsRemaining);//27
                            final Button strPlus = view.findViewById(R.id.strPlus);
                            final Button strMin = view.findViewById(R.id.strMin);
                            final TextView str = view.findViewById(R.id.strValue);//8

                            strPlus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyAdd(str, pointBuy);
                                }
                            });
                            strMin.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyMin(str, pointBuy);
                                }
                            });
                            final Button wisPlus = view.findViewById(R.id.wisPlus);
                            final Button wisMin = view.findViewById(R.id.wisMin);
                            final TextView wis = view.findViewById(R.id.wisValue);//8

                            wisPlus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyAdd(wis, pointBuy);
                                }
                            });
                            wisMin.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyMin(wis, pointBuy);
                                }
                            });
                            final Button intPlus = view.findViewById(R.id.intPlus);
                            final Button intMin = view.findViewById(R.id.intMin);
                            final TextView intelligence = view.findViewById(R.id.intValue);//8

                            intPlus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyAdd(intelligence, pointBuy);

                                }
                            });
                            intMin.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyMin(intelligence, pointBuy);
                                }
                            });
                            final Button chaPlus = view.findViewById(R.id.chaPlus);
                            final Button chaMin = view.findViewById(R.id.chaMin);
                            final TextView cha = view.findViewById(R.id.chaValue);//8

                            chaPlus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyAdd(cha, pointBuy);

                                }
                            });
                            chaMin.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyMin(cha, pointBuy);
                                }
                            });
                            final Button dexPlus = view.findViewById(R.id.dexPlus);
                            final Button dexMin = view.findViewById(R.id.dexMin);
                            final TextView dex = view.findViewById(R.id.dexValue);//8

                            dexPlus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyAdd(dex, pointBuy);
                                }
                            });
                            dexMin.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyMin(dex, pointBuy);

                                }
                            });

                            final Button conPlus = view.findViewById(R.id.conPlus);
                            final Button conMin = view.findViewById(R.id.conMin);
                            final TextView con = view.findViewById(R.id.conValue);//8

                            conPlus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyAdd(con, pointBuy);
                                }
                            });
                            conMin.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pointBuyMin(con, pointBuy);
                                }
                            });
                            final Button save = view.findViewById(R.id.saveButtonPointBuy);
                            save.setOnClickListener(new View.OnClickListener(){
                                public void onClick(View v){
                                    character.setAbilityScore(0,Integer.parseInt((String)str.getText()));
                                    character.setAbilityScore(0,Integer.parseInt((String)dex.getText()));
                                    character.setAbilityScore(0,Integer.parseInt((String)con.getText()));
                                    character.setAbilityScore(0,Integer.parseInt((String)intelligence.getText()));
                                    character.setAbilityScore(0,Integer.parseInt((String)wis.getText()));
                                    character.setAbilityScore(0,Integer.parseInt((String)cha.getText()));
                                }
                            });
                        }
                        break;
                            case "Roll": {
                            final int[] lastClicked = {-1};
                            final Button[] abilityScores = {
                                    view.findViewById(R.id.strength), view.findViewById(R.id.dexterity), view.findViewById(R.id.constitution),
                                    view.findViewById(R.id.intelligence), view.findViewById(R.id.wisdom), view.findViewById(R.id.charisma)};
                            final Button[] scoreTable = {
                                    view.findViewById(R.id.score0), view.findViewById(R.id.score1), view.findViewById(R.id.score2),
                                    view.findViewById(R.id.score3), view.findViewById(R.id.score4), view.findViewById(R.id.score5), view.findViewById(R.id.score6)};
                            {

                                scoreTable[0].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        lastClicked[0] = 0;
                                    }
                                });
                                scoreTable[1].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        lastClicked[0] = 1;
                                    }
                                });
                                scoreTable[2].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        lastClicked[0] = 2;
                                    }
                                });

                                scoreTable[3].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        lastClicked[0] = 3;
                                    }
                                });
                                scoreTable[4].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        lastClicked[0] = 4;
                                    }
                                });
                                scoreTable[5].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        lastClicked[0] = 5;
                                    }
                                });
                                scoreTable[6].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        lastClicked[0] = 6;
                                    }
                                });
                            }
                            int[] scores = character.rollAbilityScores();
                            for (int i = 0; i < scoreTable.length; i++) {
                                scoreTable[i].setText(Integer.toString(scores[i]));
                            }
                            {
                                abilityScores[0].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!abilityScores[0].getText().equals("")) {
                                            rollSetAbility(abilityScores[0], scoreTable[findFirstEmpty(scoreTable)]);
                                        } else {
                                            rollSetAbility(abilityScores[0], scoreTable[lastClicked[0]]);
                                            lastClicked[0] = -1;
                                        }

                                    }
                                });
                                abilityScores[1].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!abilityScores[1].getText().equals("")) {
                                            rollSetAbility(abilityScores[1], scoreTable[findFirstEmpty(scoreTable)]);
                                        } else {
                                            rollSetAbility(abilityScores[1], scoreTable[lastClicked[0]]);
                                            lastClicked[0] = -1;
                                        }

                                    }
                                });
                                abilityScores[2].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!abilityScores[2].getText().equals("")) {
                                            rollSetAbility(abilityScores[2], scoreTable[findFirstEmpty(scoreTable)]);
                                        } else {
                                            rollSetAbility(abilityScores[2], scoreTable[lastClicked[0]]);
                                            lastClicked[0] = -1;
                                        }

                                    }
                                });
                                abilityScores[3].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!abilityScores[3].getText().equals("")) {
                                            rollSetAbility(abilityScores[3], scoreTable[findFirstEmpty(scoreTable)]);
                                        } else {
                                            rollSetAbility(abilityScores[3], scoreTable[lastClicked[0]]);
                                            lastClicked[0] = -1;
                                        }

                                    }
                                });
                                abilityScores[4].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!abilityScores[4].getText().equals("")) {
                                            rollSetAbility(abilityScores[4], scoreTable[findFirstEmpty(scoreTable)]);
                                        } else {
                                            rollSetAbility(abilityScores[4], scoreTable[lastClicked[0]]);
                                            lastClicked[0] = 0;
                                        }

                                    }
                                });
                                abilityScores[5].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!abilityScores[5].getText().equals("")) {
                                            rollSetAbility(abilityScores[5], scoreTable[findFirstEmpty(scoreTable)]);
                                        } else {
                                            rollSetAbility(abilityScores[5], scoreTable[lastClicked[0]]);
                                            lastClicked[0] = 0;
                                        }

                                    }
                                });


                            }
                            final Button saveRoll = view.findViewById(R.id.saveRoll);
                            saveRoll.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    int[] abilityScoresForCharacter = new int[abilityScores.length];
                                    for (int i = 0; i < abilityScores.length; i++) {
                                        abilityScoresForCharacter[i] = Integer.parseInt((String) abilityScores[i].getText());
                                    }
                                    character.setAbilityScores(abilityScoresForCharacter);
                                }
                            });
                        }
                        break;
                        default: {
                            //Write Manual algorithm
                            final EditText str = view.findViewById(R.id.strength);
                            final EditText dex = view.findViewById(R.id.dexterity);
                            final EditText con = view.findViewById(R.id.constitution);
                            final EditText intelligence = view.findViewById(R.id.intelligence);
                            final EditText wis = view.findViewById(R.id.wisdom);
                            final EditText cha = view.findViewById(R.id.charisma);
                            if(character.getAbilityScore(0) != 0){
                                str.setText(Integer.toString(character.getAbilityScore(0)));
                                dex.setText(Integer.toString(character.getAbilityScore(1)));
                                con.setText(Integer.toString(character.getAbilityScore(2)));
                                intelligence.setText(Integer.toString(character.getAbilityScore(3)));
                                wis.setText(Integer.toString(character.getAbilityScore(4)));
                                cha.setText(Integer.toString(character.getAbilityScore(5)));

                            }
                            str.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if(checkForChars(str.getText().toString())){
                                        return;
                                    }
                                    if (!str.getText().toString().equals("")) {
                                        character.setAbilityScore(0, Integer.parseInt(str.getText().toString()));
                                    }
                                }
                            });
                            dex.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if(checkForChars(dex.getText().toString())){
                                        return;
                                    }
                                    if (!dex.getText().toString().equals("")) {
                                        character.setAbilityScore(1, Integer.parseInt(dex.getText().toString()));
                                    }
                                }
                            });
                            con.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if(checkForChars(con.getText().toString())){
                                        return;
                                    }
                                    if (!con.getText().toString().equals("")) {
                                        character.setAbilityScore(2, Integer.parseInt(con.getText().toString()));
                                    }
                                }
                            });
                            intelligence.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if(checkForChars(intelligence.getText().toString())){
                                        return;
                                    }
                                    if (!intelligence.getText().toString().equals("")) {
                                        character.setAbilityScore(3, Integer.parseInt(intelligence.getText().toString()));
                                    }
                                }
                            });
                            wis.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if(checkForChars(wis.getText().toString())){
                                        return;
                                    }
                                    if (!wis.getText().toString().equals("")) {
                                        character.setAbilityScore(4, Integer.parseInt(wis.getText().toString()));
                                    }
                                }
                            });
                            cha.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if(checkForChars(cha.getText().toString())){
                                        return;
                                    }
                                    if (!cha.getText().toString().equals("")) {
                                        character.setAbilityScore(5, Integer.parseInt(cha.getText().toString()));
                                    }
                                }
                            });
                        }
                        break;

                    }

                }
            }
            break;
            case 2:
//                try{
//                    Scanner scanWeapons = new Scanner(new File("weapons.txt"));
//                    Scanner scanArmor = new Scanner(new File("armor.txt"));
//
//                    String weapon = "";
//                    String armor = "";
//
//                    List<String> tempWeapons = new ArrayList<String>();
//                    List<String> tempArmor = new ArrayList<String>();
//
//                    while (scanWeapons.hasNext()){
//                        weapon = scanWeapons.next();
//                        tempWeapons.add(weapon);
//                    }
//                    scanWeapons.close();;
//
//                    while (scanArmor.hasNext()){
//                        armor = scanArmor.next();
//                        tempArmor.add(armor);
//                    }
//                    scanArmor.close();
//
//                    final String[] weaponsList = tempWeapons.toArray(new String[0]);
//                    final String[] armorList = tempArmor.toArray(new String[0]);
//
//                    GridView weaponsTable = view.findViewById(R.id.weaponsTable);
//                    GridView armorTable = view.findViewById(R.id.armorTable);
//
//                    ArrayAdapter<String> weaponAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, weaponsList);
//                    ArrayAdapter<String> armorAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, armorList);
//
//                    weaponsTable.setAdapter(weaponAdapter);
//                    armorTable.setAdapter(armorAdapter);
//
//                    weaponsTable.setOnItemClickListener(new AdapterView.OnItemClickListener()
//                        {
//                            @Override
//                            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
//                                {
//                                    String weaponData = (String) parent.getItemAtPosition(position);
//                                    character.addEquipment(weaponData);
//                                }
//                        });
//
//                    armorTable.setOnItemClickListener(new AdapterView.OnItemClickListener()
//                        {
//                            @Override
//                            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
//                                {
//                                    String armorData = (String) parent.getItemAtPosition(position);
//                                    character.addEquipment(armorData);
//                                }
//                        });
//
//                }
//                catch (FileNotFoundException fnfe){
//                    System.err.println(fnfe.getMessage());
//                }
                break;
            case 3:
                break;
            case 4: {

                final ImageButton save = view.findViewById(R.id.save_button);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Inserting values into the Database




                        //puts all the values into a new row


                    }
                });
                break;
            }
        }
    }
    //Ability Score algorithms//////////////////////////////////////////////////////
    private void pointBuyAdd(final TextView stat, final TextView pointBuy){

        int currentAbilityPoint = Integer.parseInt(stat.getText().toString());
        int currentBuyPoint = Integer.parseInt(pointBuy.getText().toString());

        if (currentAbilityPoint >= POINT_BUY_MIDDLE && !(currentAbilityPoint >= POINT_BUY_MAX || currentBuyPoint < 2)) {
            currentAbilityPoint++;
            stat.setText(Integer.toString(currentAbilityPoint));
            currentBuyPoint = currentBuyPoint - 2;
            pointBuy.setText(Integer.toString(currentBuyPoint));

        } else if (!(currentBuyPoint < 1) && (currentAbilityPoint < POINT_BUY_MAX)) {
            currentAbilityPoint++;
            stat.setText(Integer.toString(currentAbilityPoint));
            currentBuyPoint--;
            pointBuy.setText(Integer.toString(currentBuyPoint));
        }
    }
    private void pointBuyMin(final TextView stat, final TextView pointBuy){
        int currentAbilityPoint = Integer.parseInt(stat.getText().toString());
        int currentBuyPoint = Integer.parseInt(pointBuy.getText().toString());

        if (currentAbilityPoint > POINT_BUY_MIDDLE ) {
            currentAbilityPoint--;
            stat.setText(Integer.toString(currentAbilityPoint));
            currentBuyPoint = currentBuyPoint + 2;
            pointBuy.setText(Integer.toString(currentBuyPoint));

        } else if (!(currentAbilityPoint <= POINT_BUY_MIN)) {
            currentAbilityPoint--;
            stat.setText(Integer.toString(currentAbilityPoint));
            currentBuyPoint++;
            pointBuy.setText(Integer.toString(currentBuyPoint));
        }
    }

    private void rollSetAbility(final Button ability, final Button score) {
        if (score.getText().equals("")) {
            score.setText(ability.getText());
            ability.setText("");
        } else {
            ability.setText(score.getText());
            score.setText("");
        }
    }

    private int findFirstEmpty(final Button[] buttons) {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getText() == null || buttons[i].getText().equals("")) {
                return i;
            }
        }
        return -1;
    }
    private boolean checkForChars(String check){
        for(int i = 0; i < check.length(); i++){
            if(!Character.isDigit(check.charAt(i))){
                return true;
            }
        }
        return false;
    }


    //Ability Score algorithms//////////////////////////////////////////////////////



    //jon meathods
    public boolean onLongClick(View v){
        // create the ClipData.Item for the TextView from the TextView object's tag
        ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());

        //create the ClipData using the tag and the plain text MIME type with the already-created item
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData dragData = new ClipData(v.getTag().toString(), mimeTypes, item);

        //instantiate the dragShadow
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);

        // start the drag
        v.startDrag(dragData, shadow, v, 0);
        return true;
    }
    public boolean onDrag(View v, DragEvent event){
        // define a variable that will store the action for events
        int action = event.getAction();

        // switch on all of our expected events
        switch (action){
            case DragEvent.ACTION_DRAG_STARTED:
                // determine whether the view can accept the dragged data
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
                    // this is where we could also theoretically change the background color of our layout
                    return true;
                }
                return false;

            case DragEvent.ACTION_DRAG_ENTERED:
                // change the color of the view when something enters the spot
                v.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);

                // invalidate the forced redraw
                v.invalidate();
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                // ignore this event
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                // re-sets the color tint then returns true
                v.getBackground().clearColorFilter();
                v.invalidate();
                return true;

            case DragEvent.ACTION_DROP:
                // get the item that contains the dragged data
                ClipData.Item item = event.getClipData().getItemAt(0);

                // get the text from the data of the item
                String dragData = item.getText().toString();

                // turn off any color filters
                v.getBackground().clearColorFilter();
                v.invalidate();

                View vw = (View) event.getLocalState();
                ViewGroup owner = (ViewGroup) vw.getParent();
                owner.removeView(vw); // removes the dragged view from its previous location

                // cast the view to a Linear layout because the layout that will be accepting the items is a linear layout
                LinearLayout location = (LinearLayout) v;
                location.addView(vw); // add the dragged view
                vw.setVisibility(View.VISIBLE);
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                // turn off and color changes
                v.getBackground().clearColorFilter();
                v.invalidate();

                // does a getResult(), and toast if successful
//                if (event.getResult()){
//                    Toast.makeText(this, "The drop worked!!", Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    Toast.makeText(this, "The drop is fucked.", Toast.LENGTH_SHORT).show();
//                }
                return true;
            default:
                Log.e("Drag&Drop", "Unknown action received by the OnDragListener");
                break;
        }
        return false;
    }

}