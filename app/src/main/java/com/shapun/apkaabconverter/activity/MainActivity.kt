package com.shapun.apkaabconverter.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.shapun.apkaabconverter.R
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
                supportFragmentManager,AABToApkDialogFragment::class.java.simpleName
            )
        }

        binding.btnApkToAab.setOnClickListener {
            ApkToAABDialogFragment.newInstance().show(
                supportFragmentManager, ApkToAABDialogFragment::class.java.simpleName
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val intent = Intent()
        intent.data = Uri.parse("https://github.com/shapun963/Apk-AAB-Converter")
        menu?.add(0,0,0,"GitHub")?.setIcon(R.drawable.ic_github)?.setIntent(intent)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }
}