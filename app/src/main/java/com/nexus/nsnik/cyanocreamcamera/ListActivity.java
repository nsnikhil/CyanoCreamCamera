package com.nexus.nsnik.cyanocreamcamera;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ListView gifList;
    private static final String mFolder = "Gifs";
    File folder = Environment.getExternalStoragePublicDirectory(mFolder);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        gifList = (ListView)findViewById(R.id.gifListView);
        ArrayList<String> files = new ArrayList<>();
        if(folder.isDirectory()){
            String[] chil = folder.list();
            for(int i=0;i<chil.length;i++){
                files.add(chil[i]);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,files);
        gifList.setAdapter(adapter);
        gifList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(folder.isDirectory()){
                    String[] chil = folder.list();
                    String name = String.valueOf(new File(folder,chil[position]));
                    startActivity(new Intent(ListActivity.this,GifActivity.class).putExtra(getResources().getString(R.string.gifurl),name));
                }
            }
        });
    }
}
