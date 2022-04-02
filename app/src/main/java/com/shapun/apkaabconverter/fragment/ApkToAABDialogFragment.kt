package com.shapun.apkaabconverter.fragment

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shapun.apkaabconverter.convert.ApkToAABConverter
import com.shapun.apkaabconverter.convert.Logger
import com.shapun.apkaabconverter.databinding.DialogApkToAabBinding
import com.shapun.apkaabconverter.dialog.AddMetaFileDialog
import com.shapun.apkaabconverter.extension.contentResolver
import com.shapun.apkaabconverter.extension.runOnUiThread
import com.shapun.apkaabconverter.extension.toast
import com.shapun.apkaabconverter.model.MetaData
import com.shapun.apkaabconverter.util.Utils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors

class ApkToAABDialogFragment : DialogFragment() {

    // TODO: Use kotlin coroutines instead of Executors
    // ToDo: Add option to include multiple meta data files

    private lateinit var binding: DialogApkToAabBinding
    private lateinit var mTempDir: Path
    private lateinit var mTempInputPath: Path
    private lateinit var mTempOutputPath: Path
    private lateinit var mConfigPath: Path
    private lateinit var mMetaDataPath: Path
    private var mAABUri: Uri? = null
    private var mApkUri: Uri? = null
    private var mConfigUri: Uri? = null
    private var mMetaDataUri: Uri? = null
    private val mMetaData: MutableList<MetaData> = ArrayList()

    private val mResultLauncherSelectApk = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if (it != null) {
            val name :String = Utils.queryName(contentResolver, it)
            if (name.endsWith(".apk")) {
                mApkUri = it
                binding.tietApkPath.setText(name)
            } else {
                Utils.toast(requireContext(), "File name must end with .apk")
            }
        }
    }
    private val mResultLauncherSelectAABPath =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) {
            if (it != null) {
                val name: String = Utils.queryName(getContentResolver(), it)
                if (name.endsWith(".aab")) {
                    mAABUri = it
                    binding.tietAabPath.setText(name)
                } else {
                    Utils.toast(requireContext(), "Selected file is not a AAB (.aab) file")
                }
            }
        }
    private val mResultLauncherSelectConfig =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                val name: String = Utils.queryName(getContentResolver(), it)
                if (name.endsWith(".json")) {
                    mConfigUri = it
                    binding.tietConfigPath.setText(name)
                } else {
                    Utils.toast(requireContext(), "Selected file is not a JSON file")
                }
            }
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogApkToAabBinding.inflate(layoutInflater, container, false)
        val dirPath = requireContext().cacheDir.absolutePath + "/temp"
        mTempDir = Paths.get(dirPath)
        Files.createDirectories(mTempDir)
        mTempInputPath = Paths.get(dirPath, "input.apk")
        mTempOutputPath = Paths.get(dirPath, "output.aab")
        mConfigPath = Paths.get(dirPath, "BundleConfig.json")
        mMetaDataPath = Paths.get(dirPath, "meta-data")
        binding.btnApkToAab.setOnClickListener {
            when {
                mAABUri == null -> {
                    toast("Input can't be empty")
                }
                mApkUri == null -> {
                    toast("Output can't be empty")
                }
                else -> {
                    startApkToAAB()
                }
            }
        }

        binding.tilAabPath.setEndIconOnClickListener {
            var name = if (mApkUri == null) {
                "unknown.???"
            } else {
                Utils.queryName(getContentResolver(), mApkUri!!)
            }
            name = name.substring(0, name.lastIndexOf("."))
            mResultLauncherSelectAABPath.launch("$name.aab")
        }

        binding.tilApkPath.setEndIconOnClickListener {
            mResultLauncherSelectApk.launch("*/*")
        }

        binding.tilConfigPath.setEndIconOnClickListener {
            mResultLauncherSelectConfig.launch("*/*")
        }
       binding.btnAddMetaFile.setOnClickListener {
           AddMetaFileDialog.newInstance().show(childFragmentManager,AddMetaFileDialog::class.simpleName)
       }
        return binding.root
    }
    private fun showErrorDialog(error: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Failed to convert file")
            .setMessage(error)
            .setPositiveButton("Cancel", null)
            .show()
    }
    fun addMetaData(metaData: MetaData){
        mMetaData.add(metaData)
    }
    private fun startApkToAAB() {
        val logger = Logger()
        ((binding.root.getChildAt(0) as ViewGroup)).apply {
            removeAllViews()
            addView(ProgressBar(requireContext()))
            val logTv = TextView(requireContext())
            addView(logTv)
            logger.setLogListener { log ->
                runOnUiThread { logTv.append(log + "\n") }
            }
        }
        Executors.newSingleThreadExecutor().execute{
            Utils.copy(requireContext(), mApkUri!!, mTempInputPath)
            mConfigUri?.let { Utils.copy(requireContext(), it, mConfigPath) }
            mMetaDataUri?.let { Utils.copy(requireContext(), it, mMetaDataPath) }
            try {
                val builder =
                    ApkToAABConverter.Builder(
                        requireContext(),
                        mTempInputPath,
                        mTempOutputPath
                    )
                        .setLogger(logger)
                        .setVerbose(binding.cbVerbose.isChecked)
                if (mConfigUri != null) builder.setConfigFile(mConfigPath)
                //if (mMetaDataUri != null)
                    /*
                    builder.addMetaDataFile(
                        binding.tietMetadataDirectoryPathInAab.text.toString(),
                        binding.tietMetadataNameInAab.text.toString(),
                        mMetaDataPath
                    )*/
                val apkToAABConverter = builder.build()
                apkToAABConverter.start()
                Utils.copy(requireContext(), mTempOutputPath, mAABUri!!)
                runOnUiThread {
                    toast("Successfully Converted AAB to Apk")
                }
            } catch (e: Exception) {
                runOnUiThread { showErrorDialog(e.toString()) }
            } finally {
                runOnUiThread {
                    //removes Progressbar
                    (binding.root.getChildAt(0) as ViewGroup).removeViewAt(0)
                    isCancelable = true
                }
            }
        }
    }

    private fun getContentResolver() : ContentResolver = requireContext().contentResolver

    companion object {
        @JvmStatic
        fun newInstance():ApkToAABDialogFragment = ApkToAABDialogFragment()
    }
}