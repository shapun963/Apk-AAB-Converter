package com.shapun.apkaabconverter.fragment

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.shapun.apkaabconverter.converter.AABToApkConverter
import com.shapun.apkaabconverter.converter.Logger
import com.shapun.apkaabconverter.databinding.DialogAabToApkBinding
import com.shapun.apkaabconverter.util.Utils
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.*
import java.util.concurrent.Executors

/**
 * A simple [DialogFragment] subclass.
 * Use the [AABToApkDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
//ToDo: Clean the messy threads related stuff
class AABToApkDialogFragment : DialogFragment() {

    private lateinit var binding: DialogAabToApkBinding
    private lateinit var mTempDir: File
    private lateinit var mTempInputFile: File
    private lateinit var mTempOutputFile: File
    private var mAABUri: Uri? = null
    private var mApkUri: Uri? = null


    private val mResultLauncherSelectApk = registerForActivityResult(
        ActivityResultContracts.CreateDocument(),
        ActivityResultCallback<Uri?> { result: Uri? ->
            if (result != null) {
                val contentResolver = requireContext().contentResolver
                val name = Utils.queryName(contentResolver, result)
                if (name.endsWith(".apks")) {
                    mApkUri = result
                    binding.tietApkPath.setText(name)
                } else {
                    Utils.toast(requireContext(), "File name must end with .apks")
                }
            }
        })
    private val mResultLauncherSelectAAB = registerForActivityResult(GetContent()) { result: Uri? ->
        if (result != null) {
            val cr = requireContext().contentResolver
            val name: String = Utils.queryName(cr, result)
            if (name.endsWith(".aab")) {
                mAABUri = result
                binding.tietAabPath.setText(name)
            } else {
                Utils.toast(requireContext(), "Selected file is not a AAB file")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAabToApkBinding.inflate(layoutInflater)
        mTempDir = File(requireContext().getExternalFilesDir(null), "temp")
        mTempDir.mkdirs()
        mTempInputFile = File(mTempDir, "input.aab")
        mTempOutputFile = File(mTempDir, "output.apks")

        binding.btnConvertToApk.setOnClickListener {
            if (mAABUri == null) {
                Utils.toast(requireContext(), "Input can't be empty")
                return@setOnClickListener
            }
            if (mApkUri == null) {
                Utils.toast(requireContext(), "Output can't be empty")
                return@setOnClickListener
            }
            val logger = Logger()
            ((binding.root.getChildAt(0) as ViewGroup)).apply {
                removeAllViews()
                addView(ProgressBar(requireContext()))
                val logTv = TextView(requireContext())
                addView(logTv)
                logger.setLogListener { log ->
                    ContextCompat.getMainExecutor(requireContext()).execute {
                        logTv.append(log)
                    }
                }
            }
            Executors.newSingleThreadExecutor().execute {
                getContentResolver().openInputStream(mAABUri!!).use { inputStream ->
                    Files.copy(
                        inputStream as InputStream,
                        Paths.get(mTempInputFile.absolutePath),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
                val aabToApkConverter =
                    AABToApkConverter.Builder(requireContext(), mTempInputFile, mTempOutputFile)
                        .setLogger(logger)
                        .build()
                var sucessfull = true;
                try {
                    aabToApkConverter.start()
                }catch(e : Exception){
                   sucessfull = false
                }
                getContentResolver().openOutputStream(mApkUri!!).use { outputStream ->
                    Files.copy(
                        Paths.get(mTempOutputFile.absolutePath),
                        outputStream as OutputStream
                    )
                }
                ContextCompat.getMainExecutor(requireContext()).execute {
                    //removes Progressbar
                    (binding.root.getChildAt(0) as ViewGroup).removeViewAt(0);
                    Utils.toast(
                        requireContext(), if (sucessfull) {
                            "Sucessfully Converted AAB to Apk"
                        } else {
                            "Failed to convert AAB to Apk"
                        }
                    )
                }
            }
        }

        binding.tilAabPath.setEndIconOnClickListener {
            mResultLauncherSelectAAB.launch("*/*")
            binding.tilAabPath.requestFocus()
        }

        binding.tilApkPath.setEndIconOnClickListener {
            val cr: ContentResolver = getContentResolver()
            var name = if (mAABUri == null) "unknown.???" else Utils.queryName(cr, mAABUri)
            name = name.substring(0, name.lastIndexOf("."))
            mResultLauncherSelectApk.launch("$name.apks")
            binding.tilApkPath.requestFocus()
        }
        return binding.root
    }

    private fun getContentResolver(): ContentResolver {
        return requireContext().contentResolver
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
        /**
         * Use this factory method to create a new instance of
         * this fragment.
         * @return A new instance of fragment AABToApkFragment.
         */
        @JvmStatic
        fun newInstance(): AABToApkDialogFragment {
            return AABToApkDialogFragment()
        }

    }
}