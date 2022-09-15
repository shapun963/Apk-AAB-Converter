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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shapun.apkaabconverter.convert.AABToApkConverter
import com.shapun.apkaabconverter.convert.Logger
import com.shapun.apkaabconverter.databinding.DialogAabToApkBinding
import com.shapun.apkaabconverter.extension.contentResolver
import com.shapun.apkaabconverter.extension.runOnUiThread
import com.shapun.apkaabconverter.extension.toast
import com.shapun.apkaabconverter.util.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

//ToDo: Use coroutines instead of Executors
class AABToApkDialogFragment : DialogFragment() {

    private lateinit var binding: DialogAabToApkBinding
    private lateinit var mTempDir: Path
    private lateinit var mTempInputPath: Path
    private lateinit var mTempOutputPath: Path
    private var mAABUri: Uri? = null
    private var mApkUri: Uri? = null
    private var mLogger: Logger? = null
    private val mResultLauncherSelectApkPath = registerForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAabToApkBinding.inflate(layoutInflater, container, false)
        val dirPath = requireContext().cacheDir!!.absolutePath + "/temp"
        mTempDir = Paths.get(dirPath)
        Files.createDirectories(mTempDir)
        mTempInputPath = Paths.get(dirPath, "input.aab")
        mTempOutputPath = Paths.get(dirPath, "output.apks")

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
        return binding.root
    }

    private fun startAABToApk() {
        val errorHandler = CoroutineExceptionHandler { _, throwable -> showErrorDialog(throwable.toString())}
            lifecycleScope.launch(errorHandler){
            isCancelable = false
                mLogger = Logger()
            ((binding.root.getChildAt(0) as ViewGroup)).apply {
                removeAllViews()
                addView(ProgressBar(requireContext()))
                val logTv = TextView(requireContext())
                addView(logTv)
                mLogger!!.setLogListener { runOnUiThread { logTv.append(it) } }
            }
            convert()
            toast("Successfully Converted AAB to Apk")
            (binding.root.getChildAt(0) as ViewGroup).removeViewAt(0)
            clearCache()
            isCancelable = true
        }
    }

    private fun clearCache(){
        val dirPath = "${requireContext().cacheDir.absolutePath}${File.separator}temp"
        Runtime.getRuntime().exec("rm -rf $dirPath")
    }

    private suspend fun convert() = withContext(Dispatchers.Default) {
            Utils.copy(requireContext(), mAABUri!!, mTempInputPath)
            val builder =
                AABToApkConverter.Builder(
                    requireContext(),
                    mTempInputPath,
                    mTempOutputPath
                )
                    .setLogger(mLogger)
                    .setVerbose(binding.cbVerbose.isChecked)
            val signOptionsFragment =
                childFragmentManager.findFragmentByTag("SignOptionsFragment") as SignOptionsFragment
            builder.setSignerConfig(signOptionsFragment.getSigningConfig())
            builder.build().start()
            Utils.copy(requireContext(), mTempOutputPath, mApkUri!!)
    }

    private fun showErrorDialog(error: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Failed to convert file")
            .setMessage(error)
            .setPositiveButton("Cancel", null)
            .show()
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