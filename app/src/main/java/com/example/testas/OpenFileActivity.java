package com.example.testas;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class OpenFileActivity extends AppCompatActivity {

    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mItemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);

        mListView = findViewById(R.id.list_view);

        loadDataFromFile();
    }

    private void loadDataFromFile() {
        File file = new File(getFilesDir(), "testas.txt");

        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                String line;
                mItemsList = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    mItemsList.add(line);
                }

                br.close();
                isr.close();
                fis.close();

                // ArrayAdapter display  loaded data in the ListView
                mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mItemsList);
                mListView.setAdapter(mAdapter);

                Toast.makeText(this, "File opened successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Failed to open file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
