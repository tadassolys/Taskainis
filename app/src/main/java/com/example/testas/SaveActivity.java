package com.example.testas;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SaveActivity extends AppCompatActivity {

    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mItemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        mListView = findViewById(R.id.list_view);

        String scores = getIntent().getStringExtra("scoresOnClickListener");

        // Split scores data into individual items and add them to the list
        mItemsList = new ArrayList<>();
        String[] items = scores.split("\n");
        for (String item : items) {
            mItemsList.add(item);
        }
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mItemsList);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_manu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_save1) {
            saveTextToFile();
            return true;
        } else if (id == R.id.menu_load) {
            openFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void openFile() {
        File file = new File(getFilesDir(), "duomenys.txt");

        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                br.close();
                isr.close();
                fis.close();

                String loadedData = sb.toString();

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("loadedData", loadedData);
                startActivity(intent);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to read the file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveTextToFile() {
        File file = new File(getFilesDir(), "duomenys.txt");
        FileWriter fw;

        try {
            fw = new FileWriter(file);
            for (int i = 0; i < mAdapter.getCount(); i++) {
                fw.write(mAdapter.getItem(i) + "\n");
            }
            fw.close();
            Toast.makeText(this, "Išsaugota sėkmingai", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Nepavyko išsaugoti", Toast.LENGTH_SHORT).show();
        }
    }
}
