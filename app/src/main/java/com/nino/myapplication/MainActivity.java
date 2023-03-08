package com.nino.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        onClick();
    }
    private void onClick(){
        findViewById(R.id.face_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this, ImageClassificationActivity.class);
                intent.putExtra(ImageClassificationActivity.INTENT_MODULE_ASSET_NAME,
                        "mobilenetV3small.ptl");
                intent.putExtra(ImageClassificationActivity.INTENT_INFO_VIEW_TYPE,
                        InfoViewFactory.INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_QMOBILENET);
                startActivity(intent);
            }
        });
    }
}
