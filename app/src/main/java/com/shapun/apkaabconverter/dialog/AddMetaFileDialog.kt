package com.shapun.apkaabconverter.dialog

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.shapun.apkaabconverter.databinding.DialogAddMetaFileBinding
import com.shapun.apkaabconverter.extension.contentResolver
import com.shapun.apkaabconverter.fragment.ApkToAABDialogFragment
import com.shapun.apkaabconverter.model.MetaData
import com.shapun.apkaabconverter.util.Utils
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


class AddMetaFileDialog : DialogFragment() {

    private lateinit var binding: DialogAddMetaFileBinding
    private var mMetaDataUri: Uri? = null
    private var mPath: Path? = null
    private val mResultLauncherSelectMetaData =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                mMetaDataUri = it
                binding.tietMetadataPath.setText(Utils.queryName(contentResolver, it))
                val path = requireContext().cacheDir.absolutePath + "/temp/"+UUID.randomUUID().toString()
                mPath = Paths.get(path)
                Utils.copy(requireContext(),it,mPath!!)
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        binding = DialogAddMetaFileBinding.inflate(layoutInflater)
        binding.tilMetadataPath.setEndIconOnClickListener {
            mResultLauncherSelectMetaData.launch("*/*")
        }
        return AlertDialog.Builder(requireActivity())
            .setTitle("Add Meta file")
            .setPositiveButton("Add") { _, _ -> }
            .setView(binding.root)
            .setNegativeButton("Cancel"){_,_->dismiss()}
            .create()
    }

    override fun onResume() {
        super.onResume()
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{
            if(mPath == null){
                binding.tilMetadataPath.error= "No file selected"
                return@setOnClickListener
            }
            if(binding.tietMetadataDirectoryPathInAab.text.toString().isEmpty()){
                binding.tilMetadataDirectoryPathInAab.error = "Directory path cant be empty"
                return@setOnClickListener
            }
            if(binding.tietMetadataNameInAab.text.toString().isEmpty()){
                binding.tilMetadataNameInAab.error = "Name cant be empty"
                return@setOnClickListener
            }
            if(parentFragment is ApkToAABDialogFragment){
                (parentFragment as ApkToAABDialogFragment).addMetaData(
                    MetaData(
                        binding.tietMetadataPath.text.toString(),
                        mPath!!,
                        binding.tietMetadataDirectoryPathInAab.text.toString() ,
                        binding.tietMetadataNameInAab.text.toString())
                )
            }
            dismiss()
        }
    }
    companion object {
        private val KEY_ORIGINAL_FILE_NAME = "key_original_file_name"
        private val KEY_PATH = "key_path"
        private val KEY_DIRECTORY = "key_directory"
        private val KEY_FILE_NAME = "key_file_name"
        @JvmStatic
        fun newInstance() = AddMetaFileDialog()
        /*
        fun newInstance(metaData: MetaData){
            val fragment = AddMetaFileDialog()
            val bundle = Bundle()
            bundle.putString(KEY_ORIGINAL_FILE_NAME,metaData.originalFileName)
            bundle.putString(KEY_PATH,metaData.path.absolutePathString())
            bundle.putString(KEY_DIRECTORY,metaData.directory)
            bundle.putString(KEY_FILE_NAME,metaData.fileName)
            fragment.arguments = bundle
        }
         */
    }
}