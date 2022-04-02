package com.shapun.apkaabconverter.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.shapun.apkaabconverter.databinding.ActivityMainBinding
import com.shapun.apkaabconverter.fragment.AABToApkDialogFragment
import com.shapun.apkaabconverter.fragment.ApkToAABDialogFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnAabToApk.setOnClickListener {
            AABToApkDialogFragment.newInstance().show(
                supportFragmentManager,AABToApkDialogFragment::class.java.getSimpleName()
            )

        }
        binding.btnApkToAab.setOnClickListener {
            ApkToAABDialogFragment.newInstance().show(
                supportFragmentManager, ApkToAABDialogFragment::class.java.getSimpleName()
            )
        }
    }
}