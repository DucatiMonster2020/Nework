package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: AuthViewModel by ViewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupObservers()
        setupInputListeners()
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            login()
        }

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collectLatest { isAuthenticated ->
                if (isAuthenticated) {
                    findNavController().popBackStack()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                showError(error)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadingState.collectLatest { isLoading ->
                binding.progressBar.isVisible = isLoading
                binding.buttonLogin.isEnabled = !isLoading
                binding.buttonRegister.isEnabled = !isLoading
            }
        }
    }

    private fun setupInputListeners() {
        binding.textInputLogin.editText?.setOnEditorActionListener { _, _, _ ->
            login()
            true
        }

        binding.textInputPassword.editText?.setOnEditorActionListener { _, _, _ ->
            login()
            true
        }
    }

    private fun login() {
        val login = binding.textInputLogin.editText?.text?.toString()?.trim() ?: ""
        val password = binding.textInputPassword.editText?.text?.toString()?.trim() ?: ""

        if (login.isBlank()) {
            binding.textInputLogin.error = "Введите логин"
            return
        } else {
            binding.textInputLogin.error = null
        }

        if (password.isBlank()) {
            binding.textInputPassword.error = "Введите пароль"
            return
        } else {
            binding.textInputPassword.error = null
        }

        viewModel.login(login, password)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}