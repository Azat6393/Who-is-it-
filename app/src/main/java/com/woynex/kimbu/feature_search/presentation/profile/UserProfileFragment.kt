package com.woynex.kimbu.feature_search.presentation.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.decode.SvgDecoder
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.FragmentUserProfileBinding
import com.woynex.kimbu.feature_search.presentation.adapter.TagsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private lateinit var _binding: FragmentUserProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val mAdapter: TagsAdapter by lazy { TagsAdapter() }

    private var isEditMode = false

    private var selectedImage: Uri? = null

    private val getContent: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri: Uri? ->
            selectedImage = imageUri
            _binding.profilePhotoIv.load(imageUri) {
                crossfade(false)
                placeholder(R.drawable.profile_photo)
                decoderFactory(SvgDecoder.Factory())
                transformations(CircleCropTransformation())
                scale(Scale.FILL)
                build()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)

        _binding.apply {
            backBtn.setOnClickListener {
                findNavController().popBackStack()
            }
            editBtn.setOnClickListener {
                changeEditMode(true)
            }
            saveBtn.setOnClickListener {
                saveChanges()
                changeEditMode(false)
            }
            profilePhotoCardView.setOnClickListener {
                if (isEditMode) {
                    getContent.launch("image/*")
                }
            }
        }
        initProfileDetails()
        initRecyclerView()
        viewModel.getTags(viewModel.user.phone_number!!)
        observe()
    }

    private fun saveChanges() {
        val oldName = "${viewModel.user.first_name} ${viewModel.user.last_name}"
        if (oldName != _binding.nameTv.text.toString()) {
            // save new name
            if (_binding.nameTv.text.toString().isNotBlank()) {
                viewModel.updateName(_binding.nameTv.text.toString())
            } else {
                requireContext().showToastMessage(getString(R.string.input_first_name))
            }
        }
        if (selectedImage != null) {
            // save new profile photo
            selectedImage?.let {
                viewModel.saveNewProfilePhoto(it)
            }
        }
    }

    private fun changeEditMode(state: Boolean) {
        isEditMode = state
        _binding.apply {
            addBtn.isVisible = state
            editBtn.isVisible = !state
            saveBtn.isVisible = state
            if (state) {
                nameTv.inputType = InputType.TYPE_CLASS_TEXT
                nameTv.isEnabled = true
            } else {
                nameTv.inputType = InputType.TYPE_NULL
                nameTv.isEnabled = false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initProfileDetails() {
        _binding.apply {
            if (!viewModel.user.profile_photo.isNullOrBlank()){
                profilePhotoIv.load(viewModel.user.profile_photo) {
                    crossfade(false)
                    placeholder(R.drawable.profile_photo)
                    decoderFactory(SvgDecoder.Factory())
                    transformations(CircleCropTransformation())
                    scale(Scale.FILL)
                    build()
                }
            }
            nameTv.setText("${viewModel.user.first_name} ${viewModel.user.last_name}")
            phoneNumberTv.text = viewModel.user.phone_number
            mailTv.text = viewModel.user.email
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tagsResponse.collect { result ->
                    when (result) {
                        is Resource.Empty -> _binding.progressBar.isVisible = false

                        is Resource.Error -> _binding.progressBar.isVisible = false
                        is Resource.Loading -> _binding.progressBar.isVisible = true
                        is Resource.Success -> {
                            _binding.progressBar.isVisible = false
                            mAdapter.submitList(result.data)
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadingResponse.collect { result ->
                    when (result) {
                        is Resource.Empty -> _binding.progressBarProfile.isVisible = false

                        is Resource.Error -> _binding.progressBarProfile.isVisible = false
                        is Resource.Loading -> _binding.progressBarProfile.isVisible = true
                        is Resource.Success -> {
                            _binding.progressBarProfile.isVisible = false
                        }
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        _binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity()
            .findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
            .visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        requireActivity()
            .findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
            .visibility = View.VISIBLE
    }
}