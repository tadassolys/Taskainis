package com.example.testas;

import android.content.Intent;
import android.view.Menu;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Map<String, Player> players = new HashMap<>();
    private EditText playerNameInput;
    private EditText scoreInput;
    private TextView messageText;
    private TextView scoresText;
    private Spinner playerSpinner;
    private ArrayAdapter<String> spinnerAdapter;

    private int spinnerPosition = 0;
    private SharedPreferences mPrefs;



    private static final String STATE_PLAYERS = "players";
    private static final String STATE_SPINNER_POSITION = "spinner_position";
    private static final String STATE_SCORES_TEXT = "scores_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mPrefs = getApplicationContext().getSharedPreferences("mPrefs",MODE_PRIVATE);
        // Find views
        playerNameInput = findViewById(R.id.playerNameEditText);
        playerNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        scoreInput = findViewById(R.id.scoreInput);
        scoreInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        messageText = findViewById(R.id.messageText);
        scoresText = findViewById(R.id.scoresText);
        playerSpinner = findViewById(R.id.playerSpinner);
        Button prevPlayerButton = findViewById(R.id.prevPlayerButton);
        Button nextPlayerButton = findViewById(R.id.nextPlayerButton);



        playerNameInput.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isLetter(source.charAt(i))) {
                                return "";
                            }
                        }
                        return null;
                    }
                }
        });

        scoreInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        scoreInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerSpinner.setAdapter(spinnerAdapter);


        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerAdapter.getCount() == 0) {
                    Toast.makeText(MainActivity.this, "Pamiršai įvest žaidėjo vardą!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String playerName = playerSpinner.getSelectedItem().toString();
                String scoreString = scoreInput.getText().toString();

                if (scoreString.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Neįvestas rezultatas!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int score = Integer.parseInt(scoreString);

                Player player = players.get(playerName);
                if (player == null) {
                    player = new Player(playerName);
                    players.put(playerName, player);
                    spinnerAdapter.add(playerName);
                    //savePlayers(); // save players to SharedPreferences
                }

                player.addScore(score);
                messageText.setText(String.format("Pridėta %d prie %s rezultato. Dabartinis rezultatas: %d",
                        score, playerName, player.getScore()));

                scoreInput.setText("");
                saveDataToFile();
            }
        });

        prevPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPreviousPlayer();
            }
        });

        nextPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNextPlayer();
            }
        });

        Button showButton = findViewById(R.id.showButton);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Player> playerList = new ArrayList<>(players.values());
                Collections.sort(playerList, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return Integer.compare(p2.getScore(), p1.getScore());
                    }
                });
                StringBuilder sb = new StringBuilder();
                for (Player player : playerList) {
                    sb.append(String.format("%s: %d\n", player.getName(), player.getScore()));
                }
                scoresText.setText(sb.toString());
            }
        });


        Button addPlayerButton = findViewById(R.id.addPlayerButton);
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playerName = playerNameInput.getText().toString();
                if (!playerName.isEmpty()) {
                    if (!players.containsKey(playerName)) {
                        Player player = new Player(playerName);
                        players.put(playerName, player);
                        spinnerAdapter.add(playerName);
                        playerNameInput.setText("");
                        //savePlayers(); // save players to SharedPreferences
                    } else {
                        Toast.makeText(MainActivity.this, "Toks žaidėjas jau yra!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Neįvestas žaidėjas!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                for (Player player : players.values()) {
                    sb.append(player.getName()).append(": ");
                    for (int score : player.getScores()) {
                        sb.append(score).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length());
                    sb.append("\n");
                }
                scoresText.setText(sb.toString());
            }
        });
        // Check if intent has extra data, retrieve data, parse and update fields.
        if (getIntent().hasExtra("loadedData")) {
            String loadedData = getIntent().getStringExtra("loadedData");
            parseLoadedData(loadedData);
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                Intent intent = new Intent(this, SaveActivity.class);
                List<Player> playerList = new ArrayList<>(players.values());
                Collections.sort(playerList, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return Integer.compare(p2.getScore(), p1.getScore());
                    }
                });
                StringBuilder sb = new StringBuilder();
                for (Player player : playerList) {
                    sb.append(String.format("%s: %d\n", player.getName(), player.getScore()));
                }
                intent.putExtra("scoresOnClickListener", sb.toString());
                startActivity(intent);
                return true;
            case R.id.menu_clear:
                players.clear();
                spinnerAdapter.clear();

                messageText.setText("Pabandom iš naujo!");
                scoresText.setText("");

                playerNameInput.setText("");
                scoreInput.setText("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public static class Player {
        private String name;
        private int score;
        private ArrayList<Integer> scores = new ArrayList<>();

        public Player(String name) {
            this.name = name;
            this.score = 0;
        }

        public void addScore(int score) {
            this.score += score;
            this.scores.add(score);
        }

        public String getName() {
            return this.name;
        }

        public int getScore() {
            return this.score;
        }

        public ArrayList<Integer> getScores() {
            return this.scores;
        }

    }

    private void navigateToPreviousPlayer() {
        int currentPosition = playerSpinner.getSelectedItemPosition();
        if (currentPosition > 0) {
            int newPosition = currentPosition - 1;
            playerSpinner.setSelection(newPosition);
        }
    }

    private void navigateToNextPlayer() {
        int currentPosition = playerSpinner.getSelectedItemPosition();
        if (currentPosition < spinnerAdapter.getCount() - 1) {
            int newPosition = currentPosition + 1;
            playerSpinner.setSelection(newPosition);
        }
    }

    private void parseLoadedData(String loadedData) {
        // Split loaded data into lines
        String[] lines = loadedData.split("\n");

        for (String line : lines) {
            // Split each line into player name and scores
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String playerName = parts[0].trim();
                String[] scoreStrings = parts[1].trim().split(", ");
                ArrayList<Integer> scores = new ArrayList<>();

                for (String scoreString : scoreStrings) {
                    int score = Integer.parseInt(scoreString);
                    scores.add(score);
                }

                // Add player and scores to the map
                Player player = players.get(playerName);
                if (player == null) {
                    player = new Player(playerName);
                    players.put(playerName, player);
                    spinnerAdapter.add(playerName);
                }

                // Set the scores for the player
                for (int score : scores) {
                    player.addScore(score);
                }
            }
        }
    }
    private void saveDataToFile() {
        // StringBuilder build data to be saved
        StringBuilder sb = new StringBuilder();
        for (Player player : players.values()) {
            sb.append(player.getName()).append(": ");
            for (int score : player.getScores()) {
                sb.append(score).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length()); // Remove last comma and space
            sb.append("\n");
        }

        File file = new File(getFilesDir(), "duomenys.txt");
        FileWriter fw;

        try {
            fw = new FileWriter(file);
            fw.write(sb.toString());
            fw.close();
          //  Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
           // Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
        }
    }


}


