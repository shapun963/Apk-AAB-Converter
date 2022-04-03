package com.shapun.apkaabconverter.fragment

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
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.fragment.app.DialogFragment
import com.android.tools.build.bundletool.model.Password
import com.android.tools.build.bundletool.model.SigningConfiguration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shapun.apkaabconverter.convert.AABToApkConverter
import com.shapun.apkaabconverter.convert.Logger
import com.shapun.apkaabconverter.databinding.DialogAabToApkBinding
import com.shapun.apkaabconverter.extension.contentResolver
import com.shapun.apkaabconverter.extension.runOnUiThread
import com.shapun.apkaabconverter.extension.toast
import com.shapun.apkaabconverter.util.SignUtils
import com.shapun.apkaabconverter.util.Utils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors

//ToDo: Use coroutines instead of Executors
class AABToApkDialogFragment : DialogFragment() {

    private lateinit var binding: DialogAabToApkBinding
    private lateinit var mTempDir: Path
    private lateinit var mTempInputPath: Path
    private lateinit var mTempOutputPath: Path
    private lateinit var mTempJKSPath: Path
    private var mAABUri: Uri? = null
    private var mApkUri: Uri? = null
    private var mJKSUri: Uri? = null

    private val mResultLauncherSelectApkPath = registerForActivityResult(
        ActivityResultContracts.CreateDocument()) {
        if (it != null) {
            val contentResolver = requireContext().contentResolver
            val name = Utils.queryName(contentResolver, it)
            if (name.endsWith(".apks")) {
                mApkUri = it
                binding.tietApkPath.setText(name)
            } else {
                toast("File name must end with .apks")
            }

        }
    }
    private val mResultLauncherSelectAAB = registerForActivityResult(GetContent()) {
        if (it != null) {
            val name: String = Utils.queryName(contentResolver, it)
            if (name.endsWith(".aab")) {
                mAABUri = it
                binding.tietAabPath.setText(name)
            } else {
                Utils.toast(requireContext(), "Selected file is not a AAB file")
            }
        }
    }
    private val mResultLauncherSelectJKS = registerForActivityResult(GetContent()) {
        if (it != null) {
            val name: String = Utils.queryName(contentResolver, it)
            if (name.endsWith(".jks")) {
                mJKSUri = it
                binding.signOptions.tietKsPath.setText(name)
            } else {
                toast("Selected file is not a JKS file")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAabToApkBinding.inflate(layoutInflater, container, false)
        val dirPath = requireContext().cacheDir!!.absolutePath +"/temp"
        mTempDir = Paths.get(dirPath)
        Files.createDirectories(mTempDir)
        mTempInputPath = Paths.get(dirPath, "input.aab")
        mTempOutputPath = Paths.get(dirPath, "output.apks")
        mTempJKSPath = Paths.get(dirPath, "temp.jks")

        binding.btnConvertToApk.setOnClickListener {
            when {
                mAABUri == null -> toast("Input can't be empty")
                mApkUri == null -> toast("Output can't be empty")
                else -> startAABToApk()
            }
        }
        binding.tilAabPath.setEndIconOnClickListener {
            mResultLauncherSelectAAB.launch("*/*")
            binding.tilAabPath.requestFocus()
        }
        binding.tilApkPath.setEndIconOnClickListener {
            var name = if (mAABUri == null) {
                "unknown.???"
            } else {
                Utils.queryName(contentResolver, mAABUri!!)
            }
            name = name.substring(0, name.lastIndexOf("."))
            mResultLauncherSelectApkPath.launch("$name.apks")
        }
        binding.rbSignCustom.setOnCheckedChangeListener { _, value ->
            binding.signOptions.root.visibility = if (value) View.VISIBLE else View.GONE
        }
        binding.signOptions.tilKsPath.setEndIconOnClickListener {
            mResultLauncherSelectJKS.launch("*/*")
        }
        return binding.root
    }

    private fun startAABToApk(){
        isCancelable = false
        val logger = Logger()
        ((binding.root.getChildAt(0) as ViewGroup)).apply {
            removeAllViews()
            addView(ProgressBar(requireContext()))
            val logTv = TextView(requireContext())
            addView(logTv)
            logger.setLogListener { runOnUiThread { logTv.append(it)} }
        }
        Executors.newSingleThreadExecutor().execute {
            try {
                Utils.copy(requireContext(), mAABUri!!, mTempInputPath)
                val builder =
                    AABToApkConverter.Builder(
                        requireContext(),
                        mTempInputPath,
                        mTempOutputPath
                    )
                        .setLogger(logger)
                        .setVerbose(binding.cbVerbose.isChecked)
                if (binding.rgSignType.checkedRadioButtonId == binding.rbSignDebug.id) {
                    builder.setSignerConfig(SignUtils.getDebugSigningConfiguration(requireContext()))
                }else if(binding.rgSignType.checkedRadioButtonId == binding.rbSignCustom.id){
                    builder.setSignerConfig(getSigningConfig())
                }
                builder.build().start()
                Utils.copy(requireContext(), mTempOutputPath, mApkUri!!)
                runOnUiThread {
                    toast("Successfully Converted AAB to Apk")
                }
            }catch (e: Exception){
                runOnUiThread {showErrorDialog(e.toString())}
            } finally {
                runOnUiThread{
                    (binding.root.getChildAt(0) as ViewGroup).removeViewAt(0)
                    isCancelable = true
                }
            }
        }
    }

    private fun showErrorDialog(error: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Failed to convert file")
            .setMessage(error)
            .setPositiveButton("Cancel", null)
            .show()
    }

    private fun getSigningConfig() : SigningConfiguration{
        val signOptions = binding.signOptions
        if(mJKSUri==null){
            throw IllegalStateException("No JKS file selected")
        }
        if(signOptions.tietKeyAlias.text.isNullOrEmpty()){
            throw  IllegalStateException("Key Alias cant be empty")
        }
        Utils.copy(requireContext(), mJKSUri!!,mTempJKSPath)
        val ksPassword = if (signOptions.tietKsPassword.text.isNullOrEmpty()) {
           Optional.empty<Password>()
        } else {
            Optional.of(Password.createFromStringValue("pass:"+signOptions.tietKsPassword.text.toString()))
        }
        val keyPassword = if (signOptions.tietKeyPassword.text.isNullOrEmpty()) {
            Optional.empty<Password>()
        } else {
            Optional.of(Password.createFromStringValue("pass:"+signOptions.tietKeyPassword.text.toString()))
        }
       return SigningConfiguration.extractFromKeystore(
            mTempJKSPath,
            binding.signOptions.tietKeyAlias.text!!.toString(),
            ksPassword,
            keyPassword
            )
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
        fun newInstance(): AABToApkDialogFragment {
            return AABToApkDialogFragment()
        }
    }
}