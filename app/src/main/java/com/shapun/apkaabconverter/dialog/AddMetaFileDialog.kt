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
            .setPositiveButton("Add") { _, _ ->
                if(mPath == null){
                    binding.tilMetadataPath.error= "No file selected"
                }
                if(binding.tietMetadataDirectoryPathInAab.text.toString().isEmpty()){
                    binding.tietMetadataDirectoryPathInAab.error = "Directory path cant be empty"
                    return@setPositiveButton
                }
                if(binding.tietMetadataNameInAab.text.toString().isEmpty()){
                    binding.tietMetadataNameInAab.error = "Name cant be empty"
                    return@setPositiveButton
                }
                if(parentFragment is AddMetaFileDialog){
                    (parentFragment as ApkToAABDialogFragment).addMetaData(
                        MetaData(
                            mPath!!,
                            binding.tietMetadataDirectoryPathInAab.text.toString() ,
                            binding.tietMetadataNameInAab.text.toString())
                    )
                }
            }
            .setView(binding.root)
            .setNegativeButton("Cancel",null)
            .create()
    }
    companion object {
        @JvmStatic
        fun newInstance() = AddMetaFileDialog()
    }
}