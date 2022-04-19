/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-wallet-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by osarapulov on 5/7/21 10:10 AM
 */

package dgca.wallet.app.android.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentAuthBinding
import dgca.wallet.app.android.util.KEY_NAME
import dgca.wallet.app.android.util.generateSecretKey
import dgca.wallet.app.android.util.getCipher
import dgca.wallet.app.android.util.getSecretKey
import timber.log.Timber
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.util.concurrent.Executor
import javax.crypto.Cipher

class AuthFragment : BindingFragment<FragmentAuthBinding>() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAuthBinding =
        FragmentAuthBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        requireContext(),
                        "Authentication error: $errString",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.retry.isVisible = true
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    encryptSecretInformation(result.cryptoObject)
                    navigateToCodeReader()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                    binding.retry.isVisible = true
                }
            })

        binding.retry.setOnClickListener { showPrompt() }
        showPrompt()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    private fun showPrompt() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> showBiometricPrompt()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> biometricFallback()
            else -> biometricFallback()
        }
    }

    private fun showBiometricPrompt() {
        generateSecretKey(generateKeyGenParameterSpec(true))
        try {
            val cipher = initCipher()
            promptInfo = getPrompInfo(true)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (ex: Exception) {
            Timber.w(ex)
        }
    }

    private fun biometricFallback() {
        if (isDeviceSecure()) {
            showDeviceCredentialPrompt()
        } else {
            setupDeviceSecurity()
        }
    }

    private fun isDeviceSecure(): Boolean {
        val keyGuardManager =
            requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyGuardManager.isDeviceSecure
    }

    private fun showDeviceCredentialPrompt() {
        generateSecretKey(generateKeyGenParameterSpec())
        promptInfo = getPrompInfo()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun setupDeviceSecurity() {
        Timber.i("Device not secured")
        Toast.makeText(
            requireContext(),
            "Device not secured. Configure it in device settings.",
            Toast.LENGTH_SHORT
        ).show()

        Handler().postDelayed({
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }, 500)
    }

    private fun generateKeyGenParameterSpec(withBiometric: Boolean = false): KeyGenParameterSpec {
        val spec = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationValidityDurationSeconds(60)

        if (withBiometric) {
            spec.setInvalidatedByBiometricEnrollment(true)
        }

        return spec.build()
    }

    private fun getPrompInfo(withBiometric: Boolean = false): BiometricPrompt.PromptInfo {
        val prompt = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_dialog_title))
            .setSubtitle(getString(R.string.biometric_dialog_subtitle))

        if (withBiometric) {
            prompt.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            prompt.setNegativeButtonText(getString(R.string.biometric_dialog_cancel))
        } else {
            prompt.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        }

        return prompt.build()
    }

    private fun encryptSecretInformation(cryptoObject: BiometricPrompt.CryptoObject?) {
        try {
            val cipher = if (cryptoObject == null) {
                initCipher()
            } else {
                cryptoObject.cipher ?: initCipher()
            }

            val encryptedInfo: ByteArray = cipher.doFinal(
                "plaintext - string".toByteArray(Charset.defaultCharset())
            )

            Timber.i("EncryptedInfo: ${encryptedInfo.contentToString()}")

        } catch (e: InvalidKeyException) {
            Timber.w("Key is invalid.")
            biometricPrompt.authenticate(promptInfo)
        } catch (e: UserNotAuthenticatedException) {
            Timber.w("The key's validity timed out.")
            biometricPrompt.authenticate(promptInfo)
        } catch (ex: Exception) {
            Timber.w(ex, "Unknown exception")
        }
    }

    private fun initCipher(): Cipher {
        val cipher = getCipher()
        val secretKey = getSecretKey()

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    private fun navigateToCodeReader() {
        val action = AuthFragmentDirections.actionAuthFragmentToCertificatesFragment()
        findNavController().navigate(action)
    }
}
