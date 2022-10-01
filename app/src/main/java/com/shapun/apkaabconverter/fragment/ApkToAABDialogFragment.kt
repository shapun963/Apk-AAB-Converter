package com.shapun.apkaabconverter.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.android.bundle.Config
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shapun.apkaabconverter.adapter.MetaDataAdapter
import com.shapun.apkaabconverter.convert.ApkToAABConverter
import com.shapun.apkaabconverter.convert.Logger
import com.shapun.apkaabconverter.databinding.DialogApkToAabBinding
import com.shapun.apkaabconverter.dialog.AddMetaFileDialog
import com.shapun.apkaabconverter.extension.contentResolver
import com.shapun.apkaabconverter.extension.runOnUiThread
import com.shapun.apkaabconverter.extension.toast
import com.shapun.apkaabconverter.model.MetaData
import com.shapun.apkaabconverter.util.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

//ToDo: implement default gradle configs fully
class ApkToAABDialogFragment : DialogFragment() {

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
    private var mLogger: Logger? = null
    //default values AGP uses
    private val mFilesNotToCompress = listOf(
        "**.3[gG]2",
        "**.3[gG][pP]",
        "**.3[gG][pP][pP]",
        "**.3[gG][pP][pP]2",
        "**.[aA][aA][cC]",
        "**.[aA][mM][rR]",
        "**.[aA][wW][bB]",
        "**.[gG][iI][fF]",
        "**.[iI][mM][yY]",
        "**.[jJ][eE][tT]",
        "**.[jJ][pP][eE][gG]",
        "**.[jJ][pP][gG]",
        "**.[mM]4[aA]",
        "**.[mM]4[vV]",
        "**.[mM][iI][dD]",
        "**.[mM][iI][dD][iI]",
        "**.[mM][kK][vV]",
        "**.[mM][pP]2",
        "**.[mM][pP]3",
        "**.[mM][pP]4",
        "**.[mM][pP][eE][gG]",
        "**.[mM][pP][gG]",
        "**.[oO][gG][gG]",
        "**.[oO][pP][uU][sS]",
        "**.[pP][nN][gG]",
        "**.[rR][tT][tT][tT][lL]",
        "**.[sS][mM][fF]",
        "**.[tT][fF][lL][iI][tT][eE]",
        "**.[wW][aA][vV]",
        "**.[wW][eE][bB][mM]",
        "**.[wW][eE][bB][pP]",
        "**.[wW][mM][aA]",
        "**.[wW][mM][vV]",
        "**.[xX][mM][fF]"
    )
    private val mResultLauncherSelectApk = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if (it != null) {
            val name: String = Utils.queryName(contentResolver, it)
            if (name.endsWith(".apk")) {
                mApkUri = it
                binding.tietApkPath.setText(name)
            } else {
                toast("File name must end with .apk or file with same name already exists")
            }
        }
    }
    private val mResultLauncherSelectAABPath =
        registerForActivityResult(CreateDocument("*/*")) {
            if (it != null) {
                val name: String = Utils.queryName(contentResolver, it)
                if (name.endsWith(".aab")) {
                    mAABUri = it
                    binding.tietAabPath.setText(name)
                } else {
                    toast("Selected file is not a AAB (.aab) file")
                }
            }
        }
    private val mResultLauncherSelectConfig =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                val name: String = Utils.queryName(contentResolver, it)
                if (name.endsWith(".json")) {
                    mConfigUri = it
                    binding.tietConfigPath.setText(name)
                } else {
                    toast("Selected file is not a JSON file")
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogApkToAabBinding.inflate(layoutInflater, container, false)
        val dirPath = requireContext().cacheDir.absolutePath + "/temp"
        mTempDir = Paths.get(dirPath)
        Files.createDirectories(mTempDir)
        mTempInputPath = Paths.get(dirPath, "input.apk")
        mTempOutputPath = Paths.get(dirPath, "output.aab")
        mConfigPath = Paths.get(dirPath, "BundleConfig.json")
        mMetaDataPath = Paths.get(dirPath, "meta-data")
        binding.rvMetaFiles.adapter = MetaDataAdapter(mMetaData)
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
                Utils.queryName(contentResolver, mApkUri!!)
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
            AddMetaFileDialog.newInstance()
                .show(childFragmentManager, AddMetaFileDialog::class.simpleName)
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

    fun addMetaData(metaData: MetaData) {
        mMetaData.add(metaData)
        binding.rvMetaFiles.adapter!!.notifyItemInserted(mMetaData.size - 1)
        toast(metaData.toString())
    }

    private fun startApkToAAB() {
        val errorHandler =
            CoroutineExceptionHandler { _, throwable ->
                mLogger!!.add(throwable.toString())
                showErrorDialog(throwable.toString())
                (binding.root.getChildAt(0) as ViewGroup).removeViewAt(0)
                isCancelable = true
            }
        lifecycleScope.launch(errorHandler) {
            isCancelable = false
            ((binding.root.getChildAt(0) as ViewGroup)).apply {
                removeAllViews()
                addView(ProgressBar(requireContext()))
                val logTv = TextView(requireContext())
                addView(logTv)
                mLogger =  Logger()
                mLogger!!.setLogListener { log ->
                    runOnUiThread { logTv.append(log + "\n") }
                }
            }
            convert()
            toast("Successfully Converted AAB to Apk")
            //removes Progressbar
            (binding.root.getChildAt(0) as ViewGroup).removeViewAt(0)
            clearCache()
            isCancelable = true
        }
    }

    private fun clearCache(){
        val dirPath = "${requireContext().cacheDir.absolutePath}${File.separator}temp"
        File(dirPath).deleteRecursively()
    }

    private suspend fun convert() = withContext(Dispatchers.Default) {
            Utils.copy(requireContext(), mApkUri!!, mTempInputPath)
            mConfigUri?.let { Utils.copy(requireContext(), it, mConfigPath) }
            mMetaDataUri?.let { Utils.copy(requireContext(), it, mMetaDataPath) }
            ApkToAABConverter.Builder(
                requireContext(),
                mTempInputPath,
                mTempOutputPath
            ).apply {
                setLogger(mLogger)
                setVerbose(binding.cbVerbose.isChecked)
                setBundleConfig(getBundleConfig())
                    mMetaData.forEach(this::addMetaData)
                    val signOptionsFragment =
                        childFragmentManager.findFragmentByTag("SignOptionsFragment") as SignOptionsFragment
                    setSignerConfig(signOptionsFragment.getSigningConfig())
                    if (binding.cbAlign.isChecked) align()
                    build().start()
                    Utils.copy(requireContext(), mTempOutputPath, mAABUri!!)
            }
    }

    private suspend fun getBundleConfig() = withContext(Dispatchers.IO) {
        var bundleConfig: Config.BundleConfig? = null
        mConfigUri?.let { uri ->
            @Suppress("BlockingMethodInNonBlockingContext")
            contentResolver.openInputStream(uri).use {
                bundleConfig = Config.BundleConfig.parseFrom(it)
            }
        }
        if (bundleConfig == null) bundleConfig = Config.BundleConfig.newBuilder().build()
        if (binding.cbDefaultGradleConfig.isChecked) {
            val compressionBuilder = Config.Compression.newBuilder()
            val compression = compressionBuilder.addAllUncompressedGlob(mFilesNotToCompress).build()
            bundleConfig = bundleConfig!!.toBuilder().mergeCompression(compression).build()
        }
        bundleConfig

    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val window = dialog.window
            window!!.setBackgroundDrawable(null)
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            val margin = Utils.dpToPx(requireContext(), 25)
            val params = requireView().layoutParams as FrameLayout.LayoutParams
            params.setMargins(margin, margin, margin, margin)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ApkToAABDialogFragment = ApkToAABDialogFragment()
    }
}
