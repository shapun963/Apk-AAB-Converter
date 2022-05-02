package com.shapun.apkaabconverter.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.tools.build.bundletool.model.SigningConfiguration
import com.shapun.apkaabconverter.databinding.FragmentSignOptionsBinding
import com.shapun.apkaabconverter.extension.contentResolver
import com.shapun.apkaabconverter.extension.toast
import com.shapun.apkaabconverter.util.SignUtils
import com.shapun.apkaabconverter.util.Utils
import kotlinx.coroutines.*
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.UnrecoverableKeyException
import java.util.*

class SignOptionsFragment : Fragment() {

    private lateinit var binding: FragmentSignOptionsBinding
    private var mJKSUri: Uri? = null
    private var  mAliases : MutableList<String>? = null
    private var mValidateKeyStoreJob : Job? = null
    private var mValidateKeyPasswordJob: Job? = null
    private var mKeyStore: KeyStore? = null
    private var mSigningConfig: SigningConfiguration? = null
    private lateinit var mDebugSigningConfig: SigningConfiguration
    private var mSignWithDebugKey = true
    private val mKeyStoreExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            lifecycleScope.launch(Dispatchers.Main) {
                when (throwable) {
                    is KeyStoreException -> {
                        binding.tilKsPath.error = "Unable to build a keystore instance: ${throwable.cause}"
                    }
                    is IOException -> {
                        if (throwable.cause is UnrecoverableKeyException) {
                            binding.tietKsPassword.error = "Incorrect keystore password."
                        } else throw throwable
                    }
                    else -> throw throwable
                }
        }
    }
    private val mKeyPasswordExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        lifecycleScope.launch(Dispatchers.Main) {
                binding.tietKeyPassword.error = if (throwable is UnrecoverableKeyException) {
                    "Incorrect key password."
                } else {
                    throwable.toString()
                }
        }
    }

    private val mResultLauncherSelectJKS = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val name: String = Utils.queryName(contentResolver, it)
            if (name.endsWith(".jks")) {
                binding.spinnerAliases.visibility = View.GONE
                binding.tilKsPassword.visibility = View.GONE
                mJKSUri = it
                binding.tietKsPath.setText(name)
            } else {
                toast("Selected file is not a JKS file")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mDebugSigningConfig = SignUtils.getDebugSigningConfiguration(requireContext())
        binding = FragmentSignOptionsBinding.inflate(inflater, container, false)
        binding.llCustomKsOptions.visibility = View.GONE
        binding.spinnerAliases.visibility = View.GONE
        binding.tilKsPassword.visibility = View.GONE
        binding.rbSignCustom.setOnCheckedChangeListener { _, value ->
            binding.llCustomKsOptions.visibility = if (value) View.VISIBLE else View.GONE
        }
        binding.tilKsPath.setEndIconOnClickListener {
            mResultLauncherSelectJKS.launch("*/*")
        }
        binding.rgSignType.setOnCheckedChangeListener { _, id ->
            mSignWithDebugKey = (id == binding.rbSignDebug.id)
        }
        binding.tietKsPassword.addTextChangedListener {
            if (mJKSUri == null) {
                binding.tilKsPath.error = "No JKS file selected"
            } else {
                binding.spinnerAliases.visibility = View.GONE
                binding.tilKeyPassword.visibility = View.GONE
                mValidateKeyStoreJob?.cancel()
                mValidateKeyStoreJob = CoroutineScope(Dispatchers.Default).launch(mKeyStoreExceptionHandler){
                    mKeyStore = getKeyStore(mJKSUri!!,it.toString())
                        withContext(Dispatchers.Main) {
                            mAliases = Collections.list(mKeyStore!!.aliases())
                            binding.spinnerAliases.adapter = ArrayAdapter(
                                requireContext(), android.R.layout.simple_spinner_item,
                                mAliases!!
                            )
                            binding.spinnerAliases.visibility = View.VISIBLE
                            binding.tilKeyPassword.visibility = View.VISIBLE
                        }
                }
            }
        }

        binding.tietKeyPassword.addTextChangedListener {
            binding.tilKeyPassword.error = null
            mValidateKeyPasswordJob?.cancel()
            mValidateKeyStoreJob = CoroutineScope(Dispatchers.Default).launch(mKeyPasswordExceptionHandler){
                SignUtils.getSigningConfig(mKeyStore!!,binding.spinnerAliases.selectedItem as String,it.toString())
            }
        }
        return binding.root
    }

    private  fun getKeyStore(uri: Uri,password: String):KeyStore{
            contentResolver.openInputStream(uri).use {
                    return  SignUtils.getKeyStore(it!!, password)
            }
    }


    fun isSigningConfigAvailable():Boolean {
        return if (mSignWithDebugKey){
            true
        }else{
            mSigningConfig != null
        }
    }

    fun getSigningConfig():SigningConfiguration?  {
        return if (mSignWithDebugKey){
            mDebugSigningConfig
        }else{
            mSigningConfig
        }
    }
}