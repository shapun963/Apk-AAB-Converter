package com.shapun.apkaabconverter.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shapun.apkaabconverter.R;
import com.shapun.apkaabconverter.fragment.ApkAabConverterDialogFragment;

public class MainActivity extends AppCompatActivity {
	private MaterialButton btn_aab_to_apk;
	private MaterialButton btn_apk_to_aab;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		initializeViews();
		btn_aab_to_apk.setOnClickListener(v->{
			ApkAabConverterDialogFragment.newInstance(ApkAabConverterDialogFragment.AAB_TO_APK).show(getSupportFragmentManager(),ApkAabConverterDialogFragment.class.getSimpleName());
		});
		btn_apk_to_aab.setOnClickListener(v->{
			ApkAabConverterDialogFragment.newInstance(ApkAabConverterDialogFragment.APK_TO_AAB).show(getSupportFragmentManager(),ApkAabConverterDialogFragment.class.getSimpleName());
		});     
    }
	private void initializeViews(){
		btn_aab_to_apk = findViewById(R.id.btn_aab_to_apk);
		btn_apk_to_aab = findViewById(R.id.btn_apk_to_aab);
	}
}
