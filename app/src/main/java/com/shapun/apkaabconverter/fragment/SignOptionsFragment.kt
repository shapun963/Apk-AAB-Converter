package com.shapun.apkaabconverter.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.apksig.ApkSigner
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

    private var mKSUri: Uri? = null
    private lateinit var binding: FragmentSignOptionsBinding
    private var mKeyStore: KeyStore? = null
    private var mGetKeyStoreJob: Job? = null
    private var mGetSignerConfigJob: Job? = null
    private var mCustomSignerConfig: ApkSigner.SignerConfig? = null
    private lateinit var mDebugSigner: Deferred<ApkSigner.SignerConfig>

    private  val mKSExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is KeyStoreException ->
                binding.tilKsPassword.error =
                    "Unable to build a keystore instance: " + throwable.message!!
            is IOException -> {
                    binding.tilKsPassword.error = "Incorrect password"
            }
            else ->
                binding.tilKsPath.error = throwable.toString()
        }
    }
    private val mSignerConfigExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable.cause is UnrecoverableKeyException) {
            binding.tilAliasPassword.error = "Incorrect password "
        } else {
            binding.tilAliasPassword.error = throwable.toString()
        }
        toast("error")
    }
    private val mResultLauncherSelectApk = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if (it != null) {
            mKeyStore = null
            val name: String = Utils.queryName(contentResolver, it)
            mKSUri = it
            binding.tietKsPath.setText(name)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mDebugSigner = lifecycleScope.async{SignUtils.getDebugSignerConfig(requireContext())}
        binding = FragmentSignOptionsBinding.inflate(layoutInflater, container, false)
        binding.llCustomKsOptions.visibility = View.GONE
        binding.spinnerAliases.visibility = View.GONE
        binding.tilAliasPassword.visibility = View.GONE
        binding.tilKsPath.setEndIconOnClickListener {
            mResultLauncherSelectApk.launch("*/*")
        }
        binding.rgSignType.setOnCheckedChangeListener { _, i ->
            binding.llCustomKsOptions.visibility =
                if (i == binding.rbSignDebug.id) View.GONE else View.VISIBLE
        }
        binding.tietKsPassword.doAfterTextChanged {
            mGetKeyStoreJob?.cancel()
            binding.tilKsPassword.error = null
            binding.spinnerAliases.visibility = View.GONE
            binding.tilAliasPassword.visibility = View.GONE
            mGetKeyStoreJob = lifecycleScope.launch(mKSExceptionHandler) {
                mKeyStore = getKeyStore()
                binding.spinnerAliases.visibility = View.VISIBLE
                binding.tilAliasPassword.visibility = View.VISIBLE
                binding.tilKsPath.error = null
                binding.spinnerAliases.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    Collections.list(mKeyStore!!.aliases())
                )
            }
        }
        binding.tietAliasPassword.doAfterTextChanged {
            mGetSignerConfigJob?.cancel()
            binding.tilAliasPassword.error = null
            mCustomSignerConfig = null
            mGetSignerConfigJob = lifecycleScope.launch(mSignerConfigExceptionHandler){
                mCustomSignerConfig = getCustomSignerConfig()
                binding.tilAliasPassword.error = null
            }
        }
        return binding.root
    }

    private suspend fun getKeyStore():KeyStore = withContext(Dispatchers.IO) {
            @Suppress("BlockingMethodInNonBlockingContext")
            contentResolver.openInputStream(mKSUri!!).use {
                 SignUtils.getKeyStore(it!!, binding.tietKsPassword.text.toString())
            }
    }

    private suspend fun getCustomSignerConfig(): ApkSigner.SignerConfig = withContext(Dispatchers.Default){
            SignUtils.getSignerConfig(
                mKeyStore!!,
                binding.spinnerAliases.selectedItem as String,
                binding.tietAliasPassword.text.toString()
            )
    }

    suspend fun getSigningConfig(): ApkSigner.SignerConfig?{
        return if (binding.rgSignType.checkedRadioButtonId == binding.rbSignDebug.id) {
            mDebugSigner.await()
        } else {
            mCustomSignerConfig
        }
    }
}