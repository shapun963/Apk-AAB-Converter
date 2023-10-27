package com.shapun.apkaabconverter.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.lifecycle.lifecycleScope
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
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class AABToApkDialogFragment : BaseDialogFragment<DialogAabToApkBinding>() {

    private lateinit var mTempDir: Path
    private lateinit var mTempInputPath: Path
    private lateinit var mTempOutputPath: Path
    private var mAABUri: Uri? = null
    private var mApkUri: Uri? = null
    private var mLogger: Logger? = null
    private val mResultLauncherSelectApkPath = registerForActivityResult(
        CreateDocument("application/octet-stream")
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
    }

    private fun startAABToApk() {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            mLogger!!.add(throwable.toString())
            showErrorDialog(throwable.toString())
            doFinallyAfterConvert()
        }
        lifecycleScope.launch(errorHandler) {
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
            doFinallyAfterConvert()
        }
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
        builder.setSigningCertInfo(signOptionsFragment.getSigningCertInfo())
        builder.build().start()
        Utils.copy(requireContext(), mTempOutputPath, mApkUri!!)
    }

    companion object {
        @JvmStatic
        fun newInstance(): AABToApkDialogFragment {
            return AABToApkDialogFragment()
        }
    }
}