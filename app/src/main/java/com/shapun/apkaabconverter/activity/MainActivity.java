package com.shapun.apkaabconverter.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.shapun.apkaabconverter.databinding.ActivityMainBinding;
import com.shapun.apkaabconverter.fragment.ApkAabConverterDialogFragment;
import com.shapun.apkaabconverter.fragment.AABToApkDialogFragment;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnAabToApk.setOnClickListener(v->{
			AABToApkDialogFragment.newInstance().show(getSupportFragmentManager(),AABToApkDialogFragment.class.getSimpleName());
		});
		binding.btnApkToAab.setOnClickListener(v->{
			ApkAabConverterDialogFragment.newInstance(ApkAabConverterDialogFragment.APK_TO_AAB).show(getSupportFragmentManager(),ApkAabConverterDialogFragment.class.getSimpleName());
		});     
    }
}
