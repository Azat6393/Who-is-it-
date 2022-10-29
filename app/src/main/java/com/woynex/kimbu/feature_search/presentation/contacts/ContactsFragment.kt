package com.woynex.kimbu.feature_search.presentation.contacts

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.woynex.kimbu.MainActivity
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.isAppDefaultDialer
import com.woynex.kimbu.core.utils.requestPermission
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.FragmentContactsBinding
import com.woynex.kimbu.feature_search.domain.model.Contact
import com.woynex.kimbu.feature_search.presentation.SearchFragment
import com.woynex.kimbu.feature_search.presentation.SearchViewModel
import com.woynex.kimbu.feature_search.presentation.adapter.ContactsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsFragment : Fragment(R.layout.fragment_contacts), ContactsAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentContactsBinding
    private val viewModel: ContactsViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val mAdapter: ContactsAdapter by lazy { ContactsAdapter(this) }

    private val requestCallPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCall()
            }
        }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    searchViewModel.updateCallLogs()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
                AppCompatActivity.RESULT_CANCELED -> {
                    Log.d(
                        "Default Dialer request",
                        "User declined request to become default dialer"
                    )
                }
                else -> Log.d("Default Dialer request", "Unexpected result code $result.resultCode")
            }
        }

    private var callNumber = ""

    private var clickedUserNumber = ""
    private var contactList = arrayListOf<Contact>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentContactsBinding.bind(view)


        if (requireContext().isAppDefaultDialer()) {
            initContent()
        } else {
            _binding.setAsDefaultBtn.setOnClickListener {
                if (_binding.chackBox.isChecked) {
                    searchViewModel.updateHasPermission(true)
                    offerReplacingDefaultDialer()
                } else {
                    requireContext().showToastMessage(getString(R.string.please_accept_permission))
                }
            }
        }
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (_binding.callBottomSheet.root.isVisible){
                        showCallBottomSheet(false)
                    }else{
                        requireActivity().finish()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun showCallBottomSheet(state: Boolean) {
        _binding.callBottomSheet.root.isVisible = state
        (requireParentFragment() as SearchFragment).showBarsVisibility(!state)
        if (!state){
            clickedUserNumber = ""
            callNumber = ""
            _binding.callBottomSheet.numberTv.text = ""
        }
    }

    override fun onPause() {
        super.onPause()
        showCallBottomSheet(false)
    }


    private fun initContent() {
        _binding.setAsDefaultView.visibility = View.GONE
        _binding.searchEditText.doAfterTextChanged {
            filterContactsByName(it.toString())
        }

        initRecyclerView()
        observe()
        viewModel.getAllContacts()
        _binding.searchEditText.setOnFocusChangeListener { view, b ->
            if (b) {
                (requireParentFragment() as SearchFragment).isSearchFragment = 2
            }
        }
        initButtons()
        (requireParentFragment() as SearchFragment)._binding.apply {
            callFab.setOnClickListener {
                showCallBottomSheet(true)
                (requireParentFragment() as SearchFragment)._binding.viewPager.setCurrentItem(2, false)
                hideKeyboard()
            }
        }
    }

    private fun hideKeyboard() {
        val view: View = requireView()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun offerReplacingDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(ROLE_SERVICE) as RoleManager?
            val intent = roleManager!!.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            resultLauncher.launch(intent)
        }
        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
            putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                requireContext().packageName
            )
        }
        resultLauncher.launch(intent)
        /*if (!requireContext().isAppDefaultDialer()){
            requestMultiplePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.CALL_PHONE
                )
            )
        }*/
    }

    private fun filterContactsByName(filter: String) {
        if (filter.isBlank() || filter == " ") {
            mAdapter.submitList(contactList)
        } else {
            val newList = contactList.filter {
                it.name.lowercase().contains(filter.lowercase())
            }
            mAdapter.submitList(newList)
        }
    }

    private fun filterContactsByNumber(filter: String) {
        if (filter.isBlank() || filter.equals(" ")) {
            mAdapter.submitList(contactList)
        } else {
            val newList = contactList.filter {
                it.number.contains(filter)
            }
            mAdapter.submitList(newList)
        }
    }

    private fun initRecyclerView() {
        _binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy != 0) {
                        if (dy >= 15 || dy <= -15)
                            showCallBottomSheet(false)
                    }
                }
            })
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contacts.collect { result ->
                    contactList.addAll(result)
                    mAdapter.submitList(result)
                }
            }
        }
    }

    private fun startCall() {
        if (clickedUserNumber.isNotBlank()) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$clickedUserNumber")
            requireActivity().startActivity(callIntent)
        }
    }

    private fun requestCallPermission() {
        requestPermission(
            requireActivity(),
            requireView(),
            Manifest.permission.CALL_PHONE,
            getString(R.string.call_permession_text)
        ) { granted ->
            if (granted) {
                startCall()
            } else {
                requestCallPermissionLauncher.launch(
                    Manifest.permission.CALL_PHONE
                )
            }
        }
    }

    private fun initButtons() {
        _binding.callBottomSheet.apply {
            deleteBtn.setOnClickListener {
                callNumber = callNumber.dropLast(1)
                updateCallNumber()
            }
            oneBtn.setOnClickListener {
                callNumber += "1"
                updateCallNumber()
            }
            twoBtn.setOnClickListener {
                callNumber += "2"
                updateCallNumber()
            }
            threeBtn.setOnClickListener {
                callNumber += "3"
                updateCallNumber()
            }
            fourBtn.setOnClickListener {
                callNumber += "4"
                updateCallNumber()
            }
            fiveBtn.setOnClickListener {
                callNumber += "5"
                updateCallNumber()
            }
            sixBtn.setOnClickListener {
                callNumber += "6"
                updateCallNumber()
            }
            sevenBtn.setOnClickListener {
                callNumber += "7"
                updateCallNumber()
            }
            eightBtn.setOnClickListener {
                callNumber += "8"
                updateCallNumber()
            }
            nineBtn.setOnClickListener {
                callNumber += "9"
                updateCallNumber()
            }
            zeroBtn.setOnClickListener {
                callNumber += "0"
                updateCallNumber()
            }
            zeroBtn.setOnLongClickListener {
                callNumber += "+"
                updateCallNumber()
                true
            }
            starBtn.setOnClickListener {
                callNumber += "*"
                updateCallNumber()
            }
            hashBtn.setOnClickListener {
                callNumber += "#"
                updateCallNumber()
            }
            numberBtn.setOnClickListener {
                callNumber = ""
                numberTv.text = ""
                showCallBottomSheet(false)
            }
            callBtn.setOnClickListener {
                if (callNumber.isNotBlank()) {
                    clickedUserNumber = callNumber
                    requestCallPermission()
                }
            }
        }
    }

    private fun updateCallNumber() {
        _binding.callBottomSheet.numberTv.text = callNumber
        filterContactsByNumber(callNumber)
    }

    override fun onClick(item: Contact) {
        clickedUserNumber = item.number
        requestCallPermission()
    }
}