package com.shapun.apkaabconverter.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shapun.apkaabconverter.databinding.FragmentSignOptionsBinding
import com.shapun.apkaabconverter.extension.clearError
import com.shapun.apkaabconverter.extension.contentResolver
import com.shapun.apkaabconverter.extension.error
import com.shapun.apkaabconverter.extension.hide
import com.shapun.apkaabconverter.extension.show
import com.shapun.apkaabconverter.signing.helper.KeystoreHelper
import com.shapun.apkaabconverter.signing.helper.SignHelper
import com.shapun.apkaabconverter.signing.model.CertificateInfo
import com.shapun.apkaabconverter.signing.model.SigningConfig
import com.shapun.apkaabconverter.util.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import java.util.Collections

class SignOptionsFragment : Fragment() {

    private var mKSUri: Uri? = null
    private var mKeyStore: KeyStore? = null

    private var mGetKeyStoreJob: Job? = null
    private var mGetSigningCertInfoJob: Job? = null
    private var mCustomSigningCertInfo: CertificateInfo? = null
    private val mDebugSigningCertInfoDeferred by lazy {
        lifecycleScope.async { SignHelper.getDebugSigningCertInfo() }
    }

    private var _binding: FragmentSignOptionsBinding? = null
    private val binding get() = _binding!!

    private val mResultLauncherSelectKey = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> loadKeyAfterSelected(uri) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignOptionsBinding.inflate(layoutInflater, container, false)

        binding.tilKsPassword.hide()
        binding.spinnerAliases.hide()
        binding.tilAliasPassword.hide()
        binding.llCustomKsOptions.hide()

        binding.rgSignType.setOnCheckedChangeListener { _, i ->
            binding.llCustomKsOptions.isVisible = (i == binding.rbSignCustom.id)
        }
        val launchSelectKey = { _: View -> mResultLauncherSelectKey.launch("*/*") }
        binding.tilKsPath.setEndIconOnClickListener(launchSelectKey)
        binding.tilKsPath.setErrorIconOnClickListener(launchSelectKey)
        binding.tietKsPassword.doAfterTextChanged { loadKeystoreByPassword(it?.toString()) }
        binding.tietAliasPassword.doAfterTextChanged { loadCertByAliasPassword(it?.toString()) }

        return binding.root
    }

    private fun loadKeystoreByPassword(storePassword: String?) {
        mGetKeyStoreJob?.cancel()

        binding.spinnerAliases.hide()
        binding.tilAliasPassword.hide()
        binding.tilKsPath.clearError()

        if (storePassword.isNullOrEmpty()) {
            binding.tilKsPassword.clearError()
            return // do not load the key if password is null or empty
        }

        mGetKeyStoreJob = lifecycleScope.launch(CoroutineExceptionHandler { _, th ->
            val msg = th.message ?: th.toString().substringAfterLast('.')
            (if (msg.contains("password")) binding.tilKsPassword else binding.tilKsPath).error(msg)
        }) {
            mKeyStore = withContext(Dispatchers.IO) {
                contentResolver.openInputStream(mKSUri!!).use { kis ->
                    KeystoreHelper.loadKeyStore(kis!!, storePassword)
                }
            }
            binding.spinnerAliases.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                Collections.list(mKeyStore!!.aliases())
            )
            binding.spinnerAliases.show()
            binding.tilKsPassword.clearError()

            // We should assume that alias password is the same as the keystore password and that,
            // if this assumption is wrong, we should prompt for key password and retry loading
            // the key using that password.
            kotlin.runCatching {
                mCustomSigningCertInfo = getCustomKeyCertInfo(storePassword)
            }.onFailure { th ->
                if ((th.cause ?: th) is UnrecoverableKeyException) {
                    binding.tilAliasPassword.show()
                    // load cert with alias password when reselected
                    loadCertByAliasPassword(binding.tietAliasPassword.text?.toString())
                } else throw th
            }
        }
    }

    private fun loadCertByAliasPassword(aliasPassword: String?) {
        if (aliasPassword.isNullOrEmpty()) {
            binding.tilAliasPassword.clearError()
            return // do not load the cert if pwd is null or empty
        }

        mGetSigningCertInfoJob?.cancel()
        mCustomSigningCertInfo = null

        mGetSigningCertInfoJob = lifecycleScope.launch(CoroutineExceptionHandler { _, th ->
            binding.tilAliasPassword.error(th.message ?: th.toString().substringAfterLast('.'))
        }) {
            mCustomSigningCertInfo = getCustomKeyCertInfo(aliasPassword)
            binding.tilAliasPassword.clearError()
        }
    }

    private fun loadKeyAfterSelected(uri: Uri?) {
        if (uri != null) {
            mKSUri = uri
            mKeyStore = null
            binding.tietKsPath.setText(Utils.queryName(contentResolver, uri))
            binding.tilKsPassword.show() // let user to input password of keystore
            loadKeystoreByPassword(binding.tietKsPassword.text.toString()) // load keystore again when reselected
        }
    }

    private suspend fun getCustomKeyCertInfo(keyPassword: String) = withContext(Dispatchers.Default) {
        KeystoreHelper.getKeystoreCertInfo(
            mKeyStore!!,
            SigningConfig(
                storePassword = binding.tietKsPassword.text.toString(),
                keyPassword = keyPassword,
                keyAlias = binding.spinnerAliases.selectedItem as String
            )
        )
    }

    suspend fun getSigningCertInfo() = if (binding.rgSignType.checkedRadioButtonId == binding.rbSignDebug.id) {
        mDebugSigningCertInfoDeferred.await()
    } else mCustomSigningCertInfo

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
